package ru.home.customvk.presentation.posts_screen

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
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
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.news_fragment.*
import ru.home.customvk.R
import ru.home.customvk.presentation.posts_screen.adapter.PostAdapter
import ru.home.customvk.presentation.posts_screen.adapter.PostTouchHelperCallback
import ru.home.customvk.utils.AttachmentUtils
import ru.home.customvk.utils.AttachmentUtils.compressBitmap
import ru.home.customvk.utils.PostUtils.POSTS_IMAGE_PROVIDER_AUTHORITIES
import ru.home.customvk.utils.PostUtils.areLikedPostPresent
import ru.home.customvk.utils.PostUtils.createFileToCacheBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.LazyThreadSafetyMode.NONE


class PostsFragment : Fragment() {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"
        private val PUBLIC_IMAGES_DIR = Environment.DIRECTORY_PICTURES
        private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

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
        postsViewModel.getStateLiveData().observe(viewLifecycleOwner, ::render)
    }

    private fun render(state: State) {
        loading.isVisible = state.isLoading

        postsRecycler.isVisible = state.posts.isNotEmpty()
        if (!state.posts.isNullOrEmpty()) {
            adapter.posts = state.posts
        }

        state.error?.let { showQueryErrorDialog() }

        postsFragmentInterractor?.updateFavoritesVisibility(state.posts.areLikedPostPresent())

        postsFragmentInterractor?.setNeedToSyncPosts(state.isSynchronizationNeeded)

        postsRefresher.isRefreshing = false
        if (state.isUpdatingFinished) {
            postsRecycler.post { layoutManager.scrollToPosition(0) }
        }
    }

    private fun showQueryErrorDialog() = showErrorDialog(R.string.posts_loading_dialog_error_message)

    private fun showErrorDialog(@StringRes resErrorMessage: Int, @StringRes resTitle: Int = R.string.default_dialog_error_title) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setTitle(resTitle)
            .setMessage(resErrorMessage)
            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    private fun configureLayout() {
        adapter = createAdapter()
        layoutManager = LinearLayoutManager(context)

        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))

        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener { postsViewModel.input.accept(Action.RefreshPosts) }
    }

    private fun createAdapter() = PostAdapter(
        onLikeListener = { postIndex -> postsViewModel.processLike(postIndex) },
        onRemoveSwipeListener = { postPosition -> postsViewModel.hidePost(postPosition) },
        onShareAction = { bitmap: Bitmap, imageUri: String ->
            val shareDialog = setupShareImageDialog(bitmap, imageUri)
            shareDialog.show()
        }
    )

    private fun setupShareImageDialog(bitmap: Bitmap, imageUrl: String): BottomSheetDialog {
        val shareDialog = BottomSheetDialog(activity!!)
        val shareDialogView = activity!!.layoutInflater.inflate(R.layout.share_bottom_sheet_dialog, null)
        shareDialog.setContentView(shareDialogView)

        val bitmapFullName: String = AttachmentUtils.generateFullImageName(imageUrl)
        shareDialogView.findViewById<TextView>(R.id.saveToGalleryBottomDialogItem).setOnClickListener {
            val imageMimeType = AttachmentUtils.getImageMimeTypeByUrl(imageUrl)
            configureOnSavingPictureActions(bitmap, bitmapFullName, imageMimeType)
            shareDialog.dismiss()
        }
        shareDialogView.findViewById<TextView>(R.id.shareBottomDialogItem).setOnClickListener {
            val internalImageUri: Uri = cacheBitmapInternally(bitmap, bitmapFullName)
            shareImage(internalImageUri)
            shareDialog.dismiss()
        }
        return shareDialog
    }

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

    private lateinit var savingBitmapAfterRequestingPermissions: () -> Unit
    private fun configureOnSavingPictureActions(bitmap: Bitmap, bitmapFullName: String, imageMimeType: String) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            val resolver: ContentResolver = activity!!.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, bitmapFullName)
                put(MediaStore.MediaColumns.MIME_TYPE, imageMimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, PUBLIC_IMAGES_DIR)
            }
            val localImageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            resolver.openOutputStream(localImageUri)!!.compressBitmap(bitmap)
            showDialogToOpenImageInOtherApp(localImageUri, imageMimeType)
            onSuccessBitmapGallerySavingNotification()
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED -> {
            savingBitmapAfterRequestingPermissions = { onLegacySavingActions(bitmap, bitmapFullName, imageMimeType) }
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
        else -> onLegacySavingActions(bitmap, bitmapFullName, imageMimeType)
    }

    private fun onSuccessBitmapGallerySavingNotification() =
        showToast(String.format(getString(R.string.successful_image_saving_notification_format), PUBLIC_IMAGES_DIR))

    private fun onLegacySavingActions(bitmap: Bitmap, bitmapFullName: String, imageMimeType: String) {
        @Suppress("DEPRECATION")
        val imageFileToSave = File(Environment.getExternalStoragePublicDirectory(PUBLIC_IMAGES_DIR), bitmapFullName)
        FileOutputStream(imageFileToSave).compressBitmap(bitmap)
        onSuccessBitmapGallerySavingNotification()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                savingBitmapAfterRequestingPermissions()
            } else {
                showErrorDialog(R.string.saving_to_gallery_error_message)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun showDialogToOpenImageInOtherApp(localImageUri: Uri, bitmapMimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(localImageUri, bitmapMimeType)
        }
        if (intent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun synchronizePostsIfNeeded() {
        postsFragmentInterractor?.isNeedToSyncPosts()?.let { isNeedToSync -> postsViewModel.synchronizePostsIfNeeded(isNeedToSync) }
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
        fun setNeedToSyncPosts(isNeedToSyncPosts: Boolean)
        fun isNeedToSyncPosts(): Boolean
    }
}