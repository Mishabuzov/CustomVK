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
import android.webkit.MimeTypeMap
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
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
import ru.home.customvk.utils.PostUtils.createFileToSaveBitmap
import java.io.File
import java.io.FileOutputStream
import kotlin.LazyThreadSafetyMode.NONE


class PostsFragment : Fragment() {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"

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
        .setPositiveButton(
            getString(android.R.string.ok)
        ) { dialog, _ ->
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
//        onShareAction = { pictureUrl -> cacheImageAndShare(pictureUrl) }
        onShareAction = { pictureUrl -> configShareDialogAndShow(pictureUrl) }
    )

    private fun configShareDialogAndShow(pictureUrl: String) {
        shareDialogView.findViewById<TextView>(R.id.saveToGalleryBottomDialogItem).setOnClickListener {
            saveImageWithPublicAccess(pictureUrl)
            shareDialog.dismiss()
        }
        shareDialogView.findViewById<TextView>(R.id.shareBottomDialogItem).setOnClickListener {
            cacheImageAndShare(pictureUrl)
            shareDialog.dismiss()
        }
        shareDialog.show()
    }

    private fun Intent.setDataAndTypeByUri(uri: Uri) = setDataAndType(uri, activity!!.contentResolver.getType(uri))

    private fun shareImage(internalBitmapUri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            setDataAndTypeByUri(internalBitmapUri)
            putExtra(Intent.EXTRA_STREAM, internalBitmapUri)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image_dialog_title)))
    }

    private fun getDirToSaveImage(isSaveToCacheDir: Boolean) = if (isSaveToCacheDir) {
        context!!.cacheDir
    } else {
        context!!.externalMediaDirs
    }

    /**
     * Saves image-bitmap to internal dir and returns its Uri.
     */
    private fun saveBitmapInternally(bitmap: Bitmap, bitmapFullName: String, isSaveToCacheDir: Boolean = true): Uri {
        val imageFile: File = createFileToSaveBitmap(bitmapFullName, getDirToSaveImage(isSaveToCacheDir), isSaveToCacheDir)
        FileOutputStream(imageFile).use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            return FileProvider.getUriForFile(context!!, POSTS_IMAGE_PROVIDER_AUTHORITIES, imageFile)
        }
    }

    private fun saveImageToPublicDirectory(bitmap: Bitmap, bitmapFullName: String) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver: ContentResolver = activity!!.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, bitmapFullName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
            resolver.openOutputStream(imageUri)!!
        } else {
            val imagesDir: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, bitmapFullName)
            FileOutputStream(image)
        }.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
        }

    private fun makeImagePublic(internalImageUri: Uri) {
        val publicationIntent = Intent(Intent.ACTION_VIEW).apply {
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            setDataAndTypeByUri(internalImageUri)
        }
        if (publicationIntent.resolveActivity(activity!!.packageManager) != null) {
            startActivity(publicationIntent)
//            Toast.makeText(activity, "Image is published", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveImageWithPublicAccess(imageUrl: String) =
        downloadAndProcessImage(imageUrl, isSaveToCacheDir = false) { internalImageUri -> makeImagePublic(internalImageUri) }

    private fun cacheImageAndShare(imageUrl: String) =
        downloadAndProcessImage(imageUrl) { internalImageUri -> shareImage(internalImageUri) }

    private fun downloadAndProcessImage(imageUrl: String, isSaveToCacheDir: Boolean = true, actionToProcess: (Uri) -> Unit) =
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    val bitmapName = PostUtils.generateFullImageName(MimeTypeMap.getFileExtensionFromUrl(imageUrl))
                    val internalImageUri: Uri = saveBitmapInternally(resource, bitmapName, isSaveToCacheDir = isSaveToCacheDir)
                    actionToProcess(internalImageUri)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })

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