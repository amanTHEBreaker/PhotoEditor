package com.amanthebreaker.photoeditorapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.amanthebreaker.photoeditorapp.Filters.FilterListener
import com.amanthebreaker.photoeditorapp.Filters.FilterViewAdapter
import com.amanthebreaker.photoeditorapp.adapter.EditingToolsAdapter
import com.amanthebreaker.photoeditorapp.databinding.ActivityEditImageBinding
import com.amanthebreaker.photoeditorapp.model.ToolType
import com.amanthebreaker.photoeditorapp.ui.EmojiBSFragment
import com.amanthebreaker.photoeditorapp.ui.PropertiesBSFragment
import com.amanthebreaker.photoeditorapp.ui.StickerBSFragment
import com.amanthebreaker.photoeditorapp.ui.TextEditorDialogFragment
import com.google.android.material.snackbar.Snackbar

import ja.burhanrashid52.photoeditor.*

import java.io.File
import java.io.IOException

class EditImageActivity : AppCompatActivity(), OnPhotoEditorListener,
    PropertiesBSFragment.Properties,
    EmojiBSFragment.EmojiListener,
    StickerBSFragment.StickerListener,
    EditingToolsAdapter.OnItemSelected,

    FilterListener {
        private var mPhotoEditor: PhotoEditor? = null
    private var mPhotoEditorView: PhotoEditorView? = null

    private var mWonderFont: Typeface? = null
        private var mPropertiesBSFragment: PropertiesBSFragment? = null
        private var mEmojiBSFragment: EmojiBSFragment? = null
        private var mStickerBSFragment: StickerBSFragment? = null
        private val mFilterViewAdapter: FilterViewAdapter = FilterViewAdapter(this)
        private val mEditingToolsAdapter: EditingToolsAdapter = EditingToolsAdapter(this)
        private var mIsFilterVisible = false
    //    recycler
        private val mConstraintSet: ConstraintSet = ConstraintSet()

    //    binding
        lateinit var binding : ActivityEditImageBinding
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityEditImageBinding.inflate(layoutInflater)
            setContentView(binding.root)

            mWonderFont = Typeface.createFromAsset(getAssets(), "beyond_wonderland.ttf")
            mPropertiesBSFragment = PropertiesBSFragment()
            mEmojiBSFragment = EmojiBSFragment()
            mStickerBSFragment = StickerBSFragment()
            mStickerBSFragment!!.setStickerListener(this)
            mEmojiBSFragment!!.setEmojiListener(this)
            mPropertiesBSFragment!!.setPropertiesChangeListener(this)
            val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvConstraintTools!!.setLayoutManager(llmTools)
            binding.rvConstraintTools!!.setAdapter(mEditingToolsAdapter)
            val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.rvFilterView!!.setLayoutManager(llmFilters)
            binding.rvFilterView!!.setAdapter(mFilterViewAdapter)

            //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
            //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");
            mPhotoEditor = PhotoEditor.Builder(this, binding.photoEditorView!!)
                .setPinchTextScalable(true) // set flag to make text scalable when pinch
                //.setDefaultTextTypeface(mTextRobotoTf)
                //.setDefaultEmojiTypeface(mEmojiTypeFace)
                .build() // build photo editor sdk
            mPhotoEditor!!.setOnPhotoEditorListener(this)
        }

        override fun onAddViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
//            Log.d(
//                TAG,
//                "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
//            )
        }

        override fun onEditTextChangeListener(rootView: View?, text: String?, colorCode: Int) {
            val textEditorDialogFragment: TextEditorDialogFragment =
                TextEditorDialogFragment.show(this, text, colorCode)
            textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
                override fun onDone(inputText: String?, colorCode: Int) {
                    mPhotoEditor!!.editText(rootView!!, inputText, colorCode)
                    binding.txtCurrentTool.setText("Text")
                }
            })
        }



    fun onRemoveViewListener(numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onRemoveViewListener(viewType: ViewType?, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    override fun onStartViewChangeListener(viewType: ViewType?) {
        Log.d(
            TAG,
            "onStartViewChangeListener() called with: viewType = [$viewType]"
        )
    }

    override fun onStopViewChangeListener(viewType: ViewType?) {
        Log.d(
            TAG,
            "onStopViewChangeListener() called with: viewType = [$viewType]"
        )
    }

    override fun onTouchSourceImage(event: MotionEvent?) {
        Toast.makeText(this@EditImageActivity, "Touched!", Toast.LENGTH_SHORT).show()
    }


    fun onClick(v: View?) {
            binding.imgUndo.setOnClickListener {
                mPhotoEditor!!.undo()
            }

            binding.imgRedo.setOnClickListener {
                mPhotoEditor!!.redo()
            }

            binding.imgUndo.setOnClickListener {
                saveImage()
            }
        }

    @SuppressLint("MissingPermission")
    private fun saveImage() {
        if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            showLoading("Saving...")
            val file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + ""
                        + System.currentTimeMillis() + ".png"
            )
            try {
                file.createNewFile()
                val saveSettings: SaveSettings = SaveSettings.Builder()
                    .setClearViewsEnabled(true)
                    .setTransparencyEnabled(true)
                    .build()
                mPhotoEditor!!.saveAsFile(
                    file.absolutePath,
                    saveSettings,
                    object : PhotoEditor.OnSaveListener {
                        override fun onSuccess(@NonNull imagePath: String) {
                            hideLoading()
                            showSnackbar("Image Saved Successfully")
                            mPhotoEditorView!!.source.setImageURI(Uri.fromFile(File(imagePath)))
                        }

                        override fun onFailure(@NonNull exception: Exception) {
                            hideLoading()
                            showSnackbar("Failed to save Image")
                        }
                    })
            } catch (e: IOException) {
                e.printStackTrace()
                hideLoading()
                showSnackbar(e.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor!!.clearAllViews()
                    val photo = data!!.extras!!["data"] as Bitmap?
                    mPhotoEditorView!!.source.setImageBitmap(photo)
                }
                PICK_REQUEST -> try {
                    mPhotoEditor!!.clearAllViews()
                    val uri = data!!.data
                    val bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri)
                    mPhotoEditorView!!.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onFilterSelected(photoFilter: PhotoFilter?) {
        mPhotoEditor!!.setFilterEffect(photoFilter)
    }

    override fun onEmojiClick(emojiUnicode: String?) {
        mPhotoEditor!!.addEmoji(emojiUnicode)
        binding.txtCurrentTool!!.setText(R.string.label_emoji)
    }

    override fun onStickerClick(bitmap: Bitmap?) {
        mPhotoEditor!!.addImage(bitmap)
        binding.txtCurrentTool!!.setText(R.string.label_sticker)
    }

    override fun onToolSelected(toolType: ToolType?) {
        when (toolType) {
            ToolType.BRUSH -> {
                mPhotoEditor!!.setBrushDrawingMode(true)
                binding.txtCurrentTool!!.setText(R.string.label_brush)
                mPropertiesBSFragment!!.show(
                    getSupportFragmentManager(),
                    mPropertiesBSFragment!!.getTag()
                )
            }
            ToolType.TEXT -> {
                val textEditorDialogFragment: TextEditorDialogFragment =
                    TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object : TextEditorDialogFragment.TextEditor {
                    override fun onDone(inputText: String?, colorCode: Int) {
                        mPhotoEditor!!.addText(inputText, colorCode)
                        binding.txtCurrentTool!!.setText(R.string.label_text)
                    }
                })
            }
            ToolType.ERASER -> {
                mPhotoEditor!!.brushEraser()
                binding.txtCurrentTool!!.setText(R.string.label_eraser)
            }
            ToolType.FILTER -> {
                binding.txtCurrentTool!!.setText(R.string.label_filter)
                showFilter(true)
            }
            ToolType.EMOJI -> mEmojiBSFragment!!.show(getSupportFragmentManager(), mEmojiBSFragment!!.getTag())
            ToolType.STICKER -> mStickerBSFragment!!.show(
                getSupportFragmentManager(),
                mStickerBSFragment!!.getTag()
            )
            else -> {

            }
        }
    }

    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor!!.brushColor = colorCode
        binding.txtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor!!.setOpacity(opacity)
        binding.txtCurrentTool!!.setText(R.string.label_brush)
    }

    override fun onBrushSizeChanged(brushSize: Int) {
        mPhotoEditor!!.brushSize = brushSize.toFloat()
        binding.txtCurrentTool!!.setText(R.string.label_brush)
    }

    private var mProgressDialog: ProgressDialog? = null

    fun requestPermission(permission: String): Boolean {
        val isGranted = ContextCompat.checkSelfPermission(
            this,
            permission
        ) === PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            ActivityCompat.requestPermissions(
                this, arrayOf(permission),
                READ_WRITE_STORAGE
            )
        }
        return isGranted
    }

    open fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }

    }
    fun makeFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_WRITE_STORAGE -> isPermissionGranted(
                grantResults[0] == PackageManager.PERMISSION_GRANTED, permissions[0]
            )
        }
    }

    protected fun showLoading(message: String?) {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setMessage(message)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.show()
    }

    protected fun hideLoading() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    protected fun showSnackbar(message: String?) {
        val view: View? = findViewById(androidx.appcompat.R.id.content)
        if (view != null) {
            Snackbar.make(view, message!!, Snackbar.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(binding.rootView)
        if (isVisible) {
            mConstraintSet.clear(binding.rvFilterView!!.getId(), ConstraintSet.START)
            mConstraintSet.connect(
                binding.rvFilterView!!.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                binding.rvFilterView!!.getId(), ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                binding.rvFilterView!!.getId(), ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(binding.rvFilterView!!.getId(), ConstraintSet.END)
        }
        val changeBounds = ChangeBounds()
        changeBounds.setDuration(350)
        changeBounds.setInterpolator(AnticipateOvershootInterpolator(1.0f))
        TransitionManager.beginDelayedTransition(binding.rootView!!, changeBounds)
        mConstraintSet.applyTo(binding.rootView)
    }

    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            binding.txtCurrentTool!!.setText(R.string.app_name)
        } else if (!mPhotoEditor!!.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun showSaveDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Are you want to exit without saving image ?")
        builder.setPositiveButton("Save",
            DialogInterface.OnClickListener { dialog, which -> saveImage() })
        builder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, which -> dialog.dismiss() })
        builder.setNeutralButton("Discard",
            DialogInterface.OnClickListener { dialog, which -> finish() })
        builder.create().show()
    }

    companion object {
        const val READ_WRITE_STORAGE = 52
        private val TAG = EditImageActivity::class.java.simpleName
        const val EXTRA_IMAGE_PATHS = "extra_image_paths"
        private const val CAMERA_REQUEST = 52
        private const val PICK_REQUEST = 53
    }

}
