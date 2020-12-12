package ru.home.customvk.presentation.posts_screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.fragment_posts.*
import ru.home.customvk.R
import ru.home.customvk.VkApplication
import ru.home.customvk.presentation.posts_screen.adapter.PostTouchHelperCallback
import ru.home.customvk.presentation.posts_screen.adapter.PostsAdapter
import ru.home.customvk.utils.AttachmentUtils
import ru.home.customvk.utils.AttachmentUtils.PUBLIC_IMAGES_DIR
import javax.inject.Inject

class PostsFragment : Fragment() {

    companion object {
        private const val ARG_FAVORITE = "is_favorite"
        private const val ARG_FIRST_LOADING = "is_first_loading"

        private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1

        fun newInstance(isFavorite: Boolean = false, isFirstLoading: Boolean = false): PostsFragment {
            return PostsFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_FAVORITE, isFavorite)
                    putBoolean(ARG_FIRST_LOADING, isFirstLoading)
                }
            }
        }
    }

    @Inject
    lateinit var postsViewModel: PostsViewModel

    private lateinit var adapter: PostsAdapter
    private lateinit var layoutManager: LinearLayoutManager

    private var isFavoritesFragment = false
    private var isFirstLoading = false

    private var postsFragmentInterractor: PostsFragmentInterractor? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (activity?.application as VkApplication).appComponent.postsFragmentSubComponentBuilder().with(this).build().inject(this)
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        isFavoritesFragment = requireArguments().getBoolean(ARG_FAVORITE)
        isFirstLoading = requireArguments().getBoolean(ARG_FIRST_LOADING)
        if (context is PostsFragmentInterractor) {
            postsFragmentInterractor = (context as PostsFragmentInterractor)
        }
        configureViewModel()
        configureLayout()
        if (savedInstanceState == null) {
            postsViewModel.onAttachViewModel(isFavoritesFragment, isFirstLoading)
        }
    }

    private fun configureViewModel() {
        postsViewModel.getStateLiveData().observe(viewLifecycleOwner, ::render)
        postsViewModel.getUiEffectsLiveData().observe(viewLifecycleOwner, ::handleUiEffect)
    }

    private fun configureLayout() {
        adapter = createAdapter()
        layoutManager = LinearLayoutManager(context)

        postsRecycler.adapter = adapter
        postsRecycler.layoutManager = layoutManager
        postsRecycler.addItemDecoration(createPostsDivider(layoutManager))
        ItemTouchHelper(PostTouchHelperCallback(adapter)).attachToRecyclerView(postsRecycler)

        postsRefresher.setOnRefreshListener(postsViewModel::refreshPosts)
    }

    private fun render(state: State) {
        loading.isVisible = state.isLoading

        if (state.isUpdatingPosts) {
            postsRecycler.post { emptyPostsScreen.isVisible = state.isEmptyState }
            adapter.posts = state.posts
        }
    }

    private fun handleUiEffect(uiEffect: UiEffect) {
        when (uiEffect) {
            is UiEffect.UpdateFavoritesVisibility -> postsFragmentInterractor?.updateFavoritesVisibility(uiEffect.areLikedPostsPresent)
            is UiEffect.FinishRefreshing -> postsRefresher.isRefreshing = false
            is UiEffect.ErrorUpdatingPosts -> showQueryErrorDialog()
            is UiEffect.ScrollRecyclerToPosition -> postsRecycler.post { postsRecycler.scrollToPosition(uiEffect.position) }
            is UiEffect.ShareImage -> shareImage(uiEffect.internalImageUri)
            is UiEffect.ErrorSharingImage -> showBitmapLoadingErrorDialog()
            is UiEffect.ShowDialogToOpenImageInOtherApp -> showDialogToOpenImageInOtherApp(uiEffect.imageUri, uiEffect.imageMimeType)
            is UiEffect.ShowSuccessSavingToGalleryNotification -> showSuccessSavingToGalleryNotification()
        }
    }

    private fun showBitmapLoadingErrorDialog() = showErrorDialog(R.string.share_image_error_message)

    private fun showQueryErrorDialog() = showErrorDialog(R.string.posts_loading_dialog_error_message)

    private fun showErrorDialog(@StringRes resErrorMessage: Int, @StringRes resTitle: Int = R.string.newsfeed_error_dialog_title) {
        AlertDialog.Builder(requireContext(), R.style.AlertDialogStyle)
            .setTitle(resTitle)
            .setMessage(resErrorMessage)
            .setPositiveButton(getString(android.R.string.ok)) { dialog, _ -> dialog.cancel() }
            .create()
            .show()
    }

    private fun createAdapter(): PostsAdapter {
        return PostsAdapter(
            onLikeListener = { postIndex -> postsViewModel.processLike(postIndex) },
            onRemoveSwipeListener = { postPosition -> postsViewModel.hidePost(postPosition) },
            onShareAction = { bitmap: Bitmap, imageUri: String -> setupShareImageDialog(bitmap, imageUri).show() }
        )
    }

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
            postsViewModel.cacheBitmapForSharing(bitmap, bitmapFullName)
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

    private fun showToast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    private lateinit var savingBitmapAfterRequestingPermissions: () -> Unit
    private fun configureOnSavingPictureActions(bitmap: Bitmap, bitmapFullName: String, imageMimeType: String) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                postsViewModel.saveBitmapThroughMediaStore(bitmap, bitmapFullName, imageMimeType)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED -> {
                savingBitmapAfterRequestingPermissions = { postsViewModel.saveBitmapInExternalStorageDir(bitmap, bitmapFullName) }
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
            }
            else -> postsViewModel.saveBitmapInExternalStorageDir(bitmap, bitmapFullName)
        }
    }

    private fun showSuccessSavingToGalleryNotification() {
        showToast(String.format(getString(R.string.successful_image_saving_notification_format), PUBLIC_IMAGES_DIR))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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

    private fun createPostsDivider(layoutManager: LinearLayoutManager): DividerItemDecoration {
        val divider = DividerItemDecoration(postsRecycler.context, layoutManager.orientation)
        divider.setDrawable(ShapeDrawable().apply {
            intrinsicHeight = resources.getDimensionPixelOffset(R.dimen.posts_divider_size)
            paint.color = Color.DKGRAY
        })
        return divider
    }

    override fun onStop() {
        postsViewModel.saveRecyclerPosition(layoutManager.findFirstVisibleItemPosition())
        super.onStop()
    }

    interface PostsFragmentInterractor {
        fun updateFavoritesVisibility(isFavoritesFragmentVisible: Boolean)
    }
}
