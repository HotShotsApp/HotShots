package tw.app.hotshots.activity.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.adapter.recyclerview.MediaPickerAdapter
import tw.app.hotshots.adapter.recyclerview.MediaPickerListener
import tw.app.hotshots.database.posts.AddPost
import tw.app.hotshots.database.posts.AddPostListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.ActivityCreatePostBinding
import tw.app.hotshots.model.main.Post
import tw.app.hotshots.model.media.Image
import tw.app.hotshots.permissions.AppPermissions
import tw.app.hotshots.permissions.RequestPermissions
import tw.app.hotshots.storage.Upload
import tw.app.hotshots.storage.UploadListener
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.util.UidGenerator
import tw.app.hotshots.util.UriUtil
import kotlin.coroutines.CoroutineContext

class CreatePostActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityCreatePostBinding? = null
    private val binding get() = _binding!!

    private var replaceImage = false
    private var replaceAt = 0
    private lateinit var adapter: MediaPickerAdapter
    private lateinit var loadingDialog: LoadingDialog

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val uriPermissionFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION

    init {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uriList ->
                for (uri in uriList) {
                    contentResolver.takePersistableUriPermission(uri, uriPermissionFlag)

                    val filePath: Uri = uri
                    val imagePath: String? = UriUtil(this@CreatePostActivity).getPath(filePath)
                    if (replaceImage) {
                        val map = HashMap<String, Any?>()
                        map["imagePath"] = imagePath
                        map["isLocked"] = false
                        adapter.replaceImage(map, replaceAt)
                    } else {
                        val map: HashMap<String, Any?> = HashMap()
                        map["imagePath"] = imagePath
                        map["isLocked"] = false
                        adapter.addImage(map)
                        //validate()
                    }
                    replaceImage = false
                    //validate()
                }

                replaceImage = false
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog(this@CreatePostActivity)
        loadingDialog.build()

        setup()
    }

    private fun setup() {
        val layoutManager = GridLayoutManager(this@CreatePostActivity, 20)
        binding.mediaPickerRecyclerView.layoutManager = layoutManager
        adapter = MediaPickerAdapter(arrayListOf(), object : MediaPickerListener {
            override fun onPickClicked(position: Int) {
                AppPermissions.isAllowed(this@CreatePostActivity, object : RequestPermissions {
                    override fun onGranted() {
                        imageChooser()
                    }

                    override fun onDenied() {
                        AppPermissions.request(this@CreatePostActivity)
                    }
                })
            }
        })
        binding.mediaPickerRecyclerView.adapter = adapter

        binding.addPostButton.setOnClickListener {
            addPost()
        }
    }

    private fun addPost() {
        loadingDialog.setTitle("Poczekaj")
        loadingDialog.setMessage("Przesyłanie (1/${adapter.getPaths().size})")
        loadingDialog.show()

        val uploadedImagesUrls: MutableList<Image> = arrayListOf()
        var currentPos = 1

        launch {
            val upload = Upload().Image(
                "posts/",
                adapter.getPaths(),
                this@CreatePostActivity,
                object : UploadListener {
                    override fun onUploaded(fileUrl: String) {
                        val image = Image(
                            uid = UidGenerator.Generate(12),
                            imageUrl = fileUrl,
                            currentPos
                        )
                        uploadedImagesUrls.add(image)
                    }

                    override fun onFinished() {
                        runOnUiThread {
                            loadingDialog.setMessage("Tworzenie Postu...")
                            val post = Post()
                            post.uid = UidGenerator.Generate()
                            post.userUid = UserSingleton.instance?.user?.uid!!
                            post.description = binding.postDescriptionEdittext.text.toString()
                            post.images = uploadedImagesUrls

                            publishPost(post)
                        }
                    }

                    override fun progress(progress: Int) {

                    }

                    override fun onFileChanged(currentPosition: Int, size: Int) {
                        runOnUiThread {
                            loadingDialog.setMessage("Przesyłanie ($currentPosition/$size)")
                            currentPos = currentPosition
                        }
                    }

                    override fun onError(reason: String) {
                        runOnUiThread {
                            loadingDialog.setMessage(reason)
                        }
                    }
                })
        }
    }

    private fun publishPost(post: Post) {
        Log.d("publishPost", "publishPost: start")
        launch {
            Log.d("publishPost", "publishPost: launched")
            AddPost.invoke(post, object : AddPostListener {
                override fun onAdded() {
                    loadingDialog.dismiss()
                    finish()
                }

                override fun onError(e: Exception) {
                    super.onError(e)
                    loadingDialog.setMessage(e.message.toString())
                }
            })
        }
    }

    private fun imageChooser() {
        val mimeTypes = arrayOf("image/*", "video/*")
        val i = Intent()
        i.type = "*/*"
        i.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        i.action = Intent.ACTION_GET_CONTENT
        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo))
    }
}