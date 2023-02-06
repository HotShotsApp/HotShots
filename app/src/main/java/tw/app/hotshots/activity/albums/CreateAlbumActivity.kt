package tw.app.hotshots.activity.albums

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import tw.app.hotshots.adapter.SwipeToOrderCallback
import tw.app.hotshots.adapter.recyclerview.MediaPickerAdapter
import tw.app.hotshots.adapter.recyclerview.MediaPickerListener
import tw.app.hotshots.adapter.recyclerview.OnStartDragListener
import tw.app.hotshots.database.albums.AddAlbum
import tw.app.hotshots.database.albums.OnAddAlbumListener
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.databinding.ActivityCreateAlbumBinding
import tw.app.hotshots.model.main.Album
import tw.app.hotshots.model.media.Image
import tw.app.hotshots.permissions.AppPermissions
import tw.app.hotshots.permissions.RequestPermissions
import tw.app.hotshots.storage.Upload
import tw.app.hotshots.storage.UploadListener
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.util.UidGenerator
import tw.app.hotshots.util.UriUtil
import kotlin.coroutines.CoroutineContext

class CreateAlbumActivity : AppCompatActivity(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private var _binding: ActivityCreateAlbumBinding? = null
    private val binding get() = _binding!!

    private var _swipeToOrderCallback: ItemTouchHelper? = null
    private val swipeToOrderCallback get() = _swipeToOrderCallback!!

    private var _loadingDialog: LoadingDialog? = null
    private val loadingDialog get() = _loadingDialog!!

    private var _layoutManager: GridLayoutManager? = null
    private val layoutManager get() = _layoutManager!!

    private var influencerUid = ""

    private var replaceImage = false
    private var replaceAt = 0
    private lateinit var adapter: MediaPickerAdapter

    private var pickMedia: ActivityResultLauncher<PickVisualMediaRequest>
    private val uriPermissionFlag = Intent.FLAG_GRANT_READ_URI_PERMISSION

    init {
        pickMedia =
            registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uriList ->
                for (uri in uriList) {
                    contentResolver.takePersistableUriPermission(uri, uriPermissionFlag)

                    val filePath: Uri = uri
                    val imagePath: String? = UriUtil(this@CreateAlbumActivity).getPath(filePath)
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

        _binding = ActivityCreateAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setup()
    }

    private fun setup() {
        influencerUid = intent.getStringExtra("uid").toString()

        _loadingDialog = LoadingDialog(this@CreateAlbumActivity)
        loadingDialog.build()

        _swipeToOrderCallback = ItemTouchHelper(SwipeToOrderCallback(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT, 0))
        swipeToOrderCallback.attachToRecyclerView(binding.mediaPickerRecyclerView)

        _layoutManager = GridLayoutManager(this@CreateAlbumActivity, 20)
        binding.mediaPickerRecyclerView.layoutManager = layoutManager
        adapter = MediaPickerAdapter(arrayListOf(), object : MediaPickerListener {
            override fun onPickClicked(position: Int) {
                AppPermissions.isAllowed(this@CreateAlbumActivity, object : RequestPermissions {
                    override fun onGranted() {
                        imageChooser()
                    }

                    override fun onDenied() {
                        AppPermissions.request(this@CreateAlbumActivity)
                    }
                })
            }
        })

        adapter.setOnStartDragListener(object : OnStartDragListener {
            override fun onStartDrag(holder: RecyclerView.ViewHolder) {
                swipeToOrderCallback.startDrag(holder)
            }
        })

        binding.mediaPickerRecyclerView.adapter = adapter

        binding.addPostButton.setOnClickListener {
            //addAlbum()
            debugAlbum()
        }
    }

    private fun debugAlbum() {
        for (i in 0..15) {
            var album: Album = Album()
            album.uid = UidGenerator.Generate()
            album.coverImage = "https://firebasestorage.googleapis.com/v0/b/walkboner-72c59.appspot.com/o/influAvatars%2F0ydvzdi5utxo.png?alt=media&token=e6838dd5-9b9c-4c58-817d-5a5836a87c59"
            loadingDialog.show()
            publishAlbum(album)
        }
    }

    private fun addAlbum() {
        loadingDialog.setTitle("Poczekaj")
        loadingDialog.setMessage("Przesyłanie (1/${adapter.getPaths().size})")
        loadingDialog.show()

        val uploadedImagesUrls: MutableList<Image> = arrayListOf()
        var currentPos = 1

        launch {
            val upload = Upload().Image(
                "albums/",
                adapter.getPaths(),
                this@CreateAlbumActivity,
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
                            val album = Album()
                            album.uid = UidGenerator.Generate()
                            album.createdBy = UserSingleton.instance?.user?.uid!!
                            album.description = binding.postDescriptionEdittext.text.toString()
                            album.name = binding.titleEdittext.text.toString()
                            album.coverImage = uploadedImagesUrls[0].imageUrl
                            album.images = uploadedImagesUrls

                            publishAlbum(album)
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

    private fun publishAlbum(album: Album) {
        Log.d("publishPost", "publishPost: start")
        launch {
            Log.d("publishPost", "publishPost: launched")
            AddAlbum().invoke(influencerUid, album, object : OnAddAlbumListener {
                override fun onAdded() {
                    loadingDialog.dismiss()
                    //finish()
                }

                override fun onError(reason: String) {
                    loadingDialog.setMessage(reason)
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

    override fun onDestroy() {
        super.onDestroy()

        _binding = null
        _swipeToOrderCallback = null
    }
}