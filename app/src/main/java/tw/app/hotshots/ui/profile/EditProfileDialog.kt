package tw.app.hotshots.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.AvatarPickerListener
import tw.app.hotshots.activity.MainActivity
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.posts.user.UserSingleton
import tw.app.hotshots.database.user.UserDatabase
import tw.app.hotshots.database.user.UserDatabaseListener
import tw.app.hotshots.databinding.DialogEditProfileBinding
import tw.app.hotshots.util.CopyUtil
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class EditProfileDialog(
    context: Context,
    private val activity: MainActivity,
    private val listener: EditProfileListener
) : BottomSheetDialog(context), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private val binding = DialogEditProfileBinding.inflate(LayoutInflater.from(context))
    private val currentUser = UserSingleton.instance?.user!!

    private var newAvatarUrl = ""
    private var isNewAvatarUploaded = false
    private var isNewDescriptionEntered = false
    private var shouldDismissAfterUpload = false

    private lateinit var userDatabase: UserDatabase

    init {
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        userDatabase = UserDatabase(object : UserDatabaseListener {
            override fun onUpdated(user: User) {
                activity.toggleLoading(false).setMessage("")

                if (shouldDismissAfterUpload) {
                    shouldDismissAfterUpload = false
                    dismiss()
                }
            }

            override fun onError(exception: Exception) {

            }
        })

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    validate()
                }
            }
        }

        binding.profileDescriptionEditText.addTextChangedListener(watcher)
        binding.profileDescriptionEditText.setText(currentUser.description)
        binding.userNameTextView.text = currentUser.name
        binding.userUidTextView.text = currentUser.uid
        binding.copyUidButton.setOnClickListener {
            CopyUtil().copy(currentUser.uid, context, "Skopiowano UID")
        }
        binding.cameraImageView.setOnClickListener {
            activity.setAvatarPickerListener(object : AvatarPickerListener {
                override fun onCropped(croppedBitmap: Bitmap) {
                    binding.saveButton.isEnabled = false
                    binding.cameraImageView.visibility = View.GONE
                    binding.progressIndicator.visibility = View.VISIBLE
                    binding.avatarImageView.setImageBitmap(croppedBitmap)
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                }

                override fun onUploaded(fileUrl: String) {
                    newAvatarUrl = fileUrl
                    isNewAvatarUploaded = true
                    currentUser.avatar = fileUrl
                    UserSingleton.instance?.user = currentUser

                    activity.runOnUiThread {
                        binding.cameraImageView.visibility = View.VISIBLE
                        binding.progressIndicator.visibility = View.GONE
                        setCancelable(true)
                        setCanceledOnTouchOutside(true)
                        listener.onEdited(currentUser)

                        activity.toggleLoading(true).setMessage("Zmienianie zdjęcia")

                        launch {
                            userDatabase.changeAvatar(fileUrl)
                        }
                    }
                }
            })

            activity.pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        if (currentUser.avatar != "default") {
            Picasso.get().load(currentUser.avatar).into(binding.avatarImageView)
        }

        binding.saveButton.setOnClickListener {
            if (isNewAvatarUploaded)
                currentUser.avatar = newAvatarUrl

            if (isNewDescriptionEntered) {
                currentUser.description = binding.profileDescriptionEditText.text.toString()
                activity.toggleLoading(true).setMessage("Zapisywanie opisu profilu")
                shouldDismissAfterUpload = true
                launch {
                    userDatabase.changeDescription(currentUser.description)
                }
            }

            UserSingleton.instance?.user = currentUser
            listener.onEdited(currentUser)
            if (!shouldDismissAfterUpload) {
                dismiss()
            }
        }
    }

    private fun validate() {
        if (newAvatarUrl.isNotBlank() && currentUser.avatar != newAvatarUrl) {
            isNewAvatarUploaded = true
        }

        if (currentUser.description != binding.profileDescriptionEditText.text.toString()) {
            isNewDescriptionEntered = true
        }

        if (isNewAvatarUploaded && isNewDescriptionEntered) {
            binding.saveButton.isEnabled = true
        } else if (isNewAvatarUploaded && !isNewDescriptionEntered) {
            binding.saveButton.isEnabled = true
        } else if (!isNewAvatarUploaded && isNewDescriptionEntered) {
            binding.saveButton.isEnabled = true
        } else if (!isNewAvatarUploaded && !isNewDescriptionEntered) {
            binding.saveButton.isEnabled = false
        }
    }

    override fun dismiss() {
        super.dismiss()
    }
}

interface EditProfileListener {
    fun onEdited(user: User)
}