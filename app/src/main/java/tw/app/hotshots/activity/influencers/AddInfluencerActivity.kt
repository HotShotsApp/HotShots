package tw.app.hotshots.activity.influencers

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.AvatarPickerListener
import tw.app.hotshots.database.influencers.AddInfluencer
import tw.app.hotshots.database.influencers.AddInfluencerListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.ActivityAddInfluencerBinding
import tw.app.hotshots.model.main.Influencer
import tw.app.hotshots.storage.Upload
import tw.app.hotshots.storage.UploadListener
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.util.TimeUtil
import tw.app.hotshots.util.UidGenerator
import kotlin.coroutines.CoroutineContext

class AddInfluencerActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityAddInfluencerBinding? = null
    private val binding get() = _binding!!

    private var _loading: LoadingDialog? = null
    private val loading get() = _loading!!

    private var uploadedAvatarUrl = ""

    // Avatar Picker
    var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private var listenerAvatar: AvatarPickerListener? = null
    private val uriPermissionFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            var bitmap =
                MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.uriContent);

            listenerAvatar!!.onCropped(bitmap)

            launch {
                Upload().Bitmap(
                    "influAvatars",
                    bitmap,
                    object : UploadListener {
                        override fun onUploaded(fileUrl: String) {
                            listenerAvatar!!.onUploaded(fileUrl)
                        }

                        override fun onError(reason: String) {
                            listenerAvatar!!.onUploadError(reason)
                        }
                    }
                )
            }
        } else {
            listenerAvatar!!.onUploadError(result.error?.message!!)
        }
    }

    init {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    contentResolver.takePersistableUriPermission(uri!!, uriPermissionFlag)

                    cropImage.launch(
                        options(uri = uri) {
                            setGuidelines(CropImageView.Guidelines.ON)
                            setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                        }
                    )
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityAddInfluencerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _loading = LoadingDialog(this@AddInfluencerActivity)
        loading.build()

        setup();

        listenerAvatar = object : AvatarPickerListener {
            override fun onCropped(croppedBitmap: Bitmap) {
                super.onCropped(croppedBitmap)

                binding.pickerAvatarImageView.setImageBitmap(croppedBitmap)
                binding.hintPickImageButton.visibility = GONE
                binding.hintUploadingImageView.visibility = VISIBLE
            }

            override fun onUploaded(fileUrl: String) {
                super.onUploaded(fileUrl)
                uploadedAvatarUrl = fileUrl

                if (binding.nickNameEditText.text.toString().isNotBlank()) {
                    binding.addButton.isEnabled = true
                }

                binding.hintPickImageButton.visibility = VISIBLE
                binding.hintUploadingImageView.visibility = GONE
            }

            override fun onUploadError(error: String) {
                super.onUploadError(error)

                Snackbar.make(binding.root, error, Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun setup() {
        binding.nickNameEditText.filters = arrayOf(InputFilter.LengthFilter(15))

        binding.nickNameEditText.addTextChangedListener {
                if (it != null) {
                    if (it.isNotEmpty()) {
                        if (uploadedAvatarUrl.isNotBlank()) {
                            binding.addButton.isEnabled = true
                        }
                    }
                }
        }

        binding.influencerDoesntHaveNickCheckBox.setOnClickListener {
            if (binding.influencerDoesntHaveNickCheckBox.isChecked) {
                var firstName = binding.firstNameEditText.text.toString()
                var lastName = binding.lastNameEditText.text.toString()

                if (firstName.isBlank() || lastName.isBlank()) {
                    Toast.makeText(this@AddInfluencerActivity, "Wypełnij Imię i Nazwisko!", Toast.LENGTH_LONG).show()
                    binding.influencerDoesntHaveNickCheckBox.isChecked = false
                    return@setOnClickListener
                }

                var fullName = "$firstName $lastName"

                binding.nickNameEditText.filters = arrayOf(InputFilter.LengthFilter(fullName.length))
                binding.nickNameTextInputLayout.counterMaxLength = fullName.length
                binding.nickNameEditText.setText(fullName)
                binding.firstNameEditText.isEnabled = false
                binding.firstNameTextInputLayout.isEnabled = false
                binding.lastNameEditText.isEnabled = false
                binding.lastNameTextInputLayout.isEnabled = false
                binding.nickNameEditText.isEnabled = false
                binding.nickNameTextInputLayout.isEnabled = false
            } else {
                binding.nickNameEditText.filters = arrayOf(InputFilter.LengthFilter(15))
                binding.nickNameTextInputLayout.counterMaxLength = 15
                binding.nickNameEditText.setText("")
                binding.firstNameEditText.isEnabled = true
                binding.firstNameTextInputLayout.isEnabled = true
                binding.lastNameEditText.isEnabled = true
                binding.lastNameTextInputLayout.isEnabled = true
                binding.nickNameEditText.isEnabled = true
                binding.nickNameTextInputLayout.isEnabled = true
            }
        }

        binding.hintPickImageButton.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.addButton.setOnClickListener {
            loading.setTitle("Poczekaj...")
            loading.show()

            var influencer: Influencer =
                Influencer(
                    uid = UidGenerator.Generate(16),
                    firstName = binding.firstNameEditText.text.toString().trim(),
                    lastName = binding.firstNameEditText.text.toString().trim(),
                    nickname = binding.nickNameEditText.text.toString().trim(),
                    avatar = uploadedAvatarUrl,
                    createdBy = UserSingleton.instance?.user!!.uid,
                    createdAt = TimeUtil.currentTimeToLong()
                )

            launch {
                AddInfluencer().invoke(influencer, object : AddInfluencerListener {
                    override fun onAdded() {
                        Toast.makeText(
                            this@AddInfluencerActivity,
                            "Wysłano! Decyzje dostaniesz w powiadomieniu.",
                            Toast.LENGTH_LONG
                        ).show()
                        loading.dismiss()
                        _loading = null
                        finish()
                    }

                    override fun onError(reason: String) {
                        Toast.makeText(this@AddInfluencerActivity, reason, Toast.LENGTH_LONG).show()
                        loading.dismiss()
                    }
                })
            }
        }
    }
}