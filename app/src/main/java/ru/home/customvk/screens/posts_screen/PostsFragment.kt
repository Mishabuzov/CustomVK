package ru.home.customvk.screens.posts_screen

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.news_fragment.*
import ru.home.customvk.R
import ru.home.customvk.utils.PostUtils
import ru.home.customvk.utils.PostUtils.POSTS_IMAGE_PROVIDER_AUTHORITIES
import ru.home.customvk.utils.PostUtils.createFileToCacheBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.LazyThreadSafetyMode.NONE


class PostsFragment : Fragment() {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"
        private val PUBLIC_IMAGES_DIR = Environment.DIRECTORY_PICTURES

        fun newInstance(isFavorite: Boolean = false): PostsFragment =
            PostsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FAVORITE, isFavorite)
                }
            }
    }

    private val postsViewModel: PostsViewModel by lazy(NONE) {
        ViewModelProvider(this, PostsViewModel.PostsViewModelFactory(isFavoritesFragment)).get(PostsViewModel::class.java)
    }

    private lateinit var shareDialog: BottomSheetDialog
    private lateinit var shareDialogView: View

    private lateinit var adapter: PostAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isFavoritesFragment = false

    private var postsFragmentInterractor: PostsFragmentInterractor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.news_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFavoritesFragment = requireArguments().getBoolean(ARG_FAVORITE)
        if (context is PostsFragmentInterractor) {
            postsFragmentInterractor = (context as PostsFragmentInterractor)
        }
        configureLayout()
        synchronizePostsIfNeeded()
        initObservers()
        initShareDialog()
    }

    private fun initShareDialog() {
        shareDialog = BottomSheetDialog(activity!!)
        shareDialogView = activity!!.layoutInflater.inflate(R.layout.share_bottom_sheet_dialog, null)
        shareDialog.setContentView(shareDialogView)
    }

    private fun initObservers() {
        postsViewModel.getPostsLiveData().observe(viewLifecycleOwner) { adapter.posts = it }
        postsViewModel.showErrorDialogAction.observe(viewLifecycleOwner) { showErrorDialog() }
        postsViewModel.onSynchronizationCompleteAction.observe(viewLifecycleOwner) { postsFragmentInterractor?.onSynchronizationComplete() }
        postsViewModel.finishUpdatingAction.observe(viewLifecycleOwner) {
            postsRefresher.isRefreshing = false
            postsRecycler.post { layoutManager.scrollToPosition(0) }
            postsFragmentInterractor?.onChangesMade()
        }
        postsViewModel.updateFavoritesVisibilityAction.observe(viewLifecycleOwner) { isFavoritesFragmentVisible ->
            postsFragmentInterractor?.updateFavoritesVisibility(isFavoritesFragmentVisible)
        }
    }

    private fun showErrorDialog() = AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
        .setTitle(R.string.posts_loading_dialog_error_title)
        .setMessage(R.string.posts_loading_dialog_error_message)
        .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
            dialog.cancel()
        }
        .create()
        .show()

    private fun configureLayout() {
        adapter = createAdapter()
        layoutManager = LinearLayoutManager(context)

        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener { postsViewModel.refreshPosts() }
    }

    private fun createAdapter() = PostAdapter(
        onLikeListener = { postIndex ->
            postsViewModel.processLike(postIndex)
            postsFragmentInterractor?.onChangesMade()
        },
        onRemoveSwipeListener = { postPosition ->
            postsViewModel.hidePost(postPosition)
            postsFragmentInterractor?.onChangesMade()
        },
        onShareAction = { imageUrl: String ->
            configShareDialogForImage(imageUrl)
            shareDialog.show()
        }
    )

    private fun configShareDialogForImage(imageUrl: String) {
        shareDialogView.findViewById<TextView>(R.id.saveToGalleryBottomDialogItem).setOnClickListener {
            downloadAndProcessImage(imageUrl) { bitmap, bitmapFullName ->
                val imageMimeType = PostUtils.getImageMimeTypeByUrl(imageUrl)
                val localImageUri = saveBitmapToPublicMediaDirectory(bitmap, bitmapFullName, imageMimeType)
                showDialogToOpenImageInOtherApp(localImageUri, imageMimeType)
            }
            shareDialog.dismiss()
        }
        shareDialogView.findViewById<TextView>(R.id.shareBottomDialogItem).setOnClickListener {
            downloadAndProcessImage(imageUrl) { bitmap, bitmapFullName ->
                val internalImageUri: Uri = cacheBitmapInternally(bitmap, bitmapFullName)
                shareImage(internalImageUri)
            }
            shareDialog.dismiss()
        }
    }

    private fun downloadAndProcessImage(imageUrl: String, actionToProcess: (Bitmap, String) -> Unit) = Glide.with(this)
        .asBitmap()
        .load(imageUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) =
                actionToProcess(resource, PostUtils.generateFullImageName(imageUrl))

            override fun onLoadCleared(placeholder: Drawable?) {}
        })

    private fun Intent.setDataAndTypeByUri(uri: Uri) = setDataAndType(uri, activity!!.contentResolver.getType(uri))

    private fun shareImage(internalBitmapUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setDataAndTypeByUri(internalBitmapUri)
            putExtra(Intent.EXTRA_STREAM, internalBitmapUri)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image_dialog_title)))
    }

    /**
     * Caches image-bitmap to internal dir and returns its Uri.
     */
    private fun cacheBitmapInternally(bitmap: Bitmap, bitmapFullName: String): Uri {
        val imageFile: File = createFileToCacheBitmap(bitmapFullName, context!!.cacheDir)
        FileOutputStream(imageFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            return FileProvider.getUriForFile(context!!, POSTS_IMAGE_PROVIDER_AUTHORITIES, imageFile)
        }
    }

    private fun showToast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    private fun saveBitmapToPublicMediaDirectory(bitmap: Bitmap, bitmapFullName: String, bitmapMimeType: String): Uri {
        val localImageUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = activity!!.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, bitmapFullName)
                put(MediaStore.MediaColumns.MIME_TYPE, bitmapMimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_IMAGES_DIR)
            }
            localImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            resolver.openOutputStream(localImageUri)!!
        } else {
            val imageFileToSave = File(Environment.getExternalStoragePublicDirectory(PUBLIC_IMAGES_DIR), bitmapFullName)
            localImageUri = imageFileToSave.toUri()
            FileOutputStream(imageFileToSave)
        }.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            showToast(String.format(getString(R.string.successful_image_saving_notification_format), PUBLIC_IMAGES_DIR))
        }
        return localImageUri
    }

    private fun showDialogToOpenImageInOtherApp(localImageUri: Uri, bitmapMimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(localImageUri, bitmapMimeType)
        }
        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun synchronizePostsIfNeeded() = postsFragmentInterractor?.isNeedToSyncPosts()?.let { isNeedToSync ->
        postsViewModel.synchronizePostsIfNeeded(isNeedToSync)
    }

    private fun createPostsDivider(layoutManager: LinearLayoutManager): DividerItemDecoration {
        val divider = DividerItemDecoration(postsRecycler.context, layoutManager.orientation)
        divider.setDrawable(ShapeDrawable().apply {
            intrinsicHeight = resources.getDimensionPixelOffset(R.dimen.posts_divider_size)
            paint.color = Color.DKGRAY
        })
        return divider
    }

    interface PostsFragmentInterractor {
        fun updateFavoritesVisibility(isFavoritesFragmentVisible: Boolean)
        fun onChangesMade()
        fun isNeedToSyncPosts(): Boolean
        fun onSynchronizationComplete()
    }
}