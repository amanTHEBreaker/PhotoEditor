package com.amanthebreaker.photoeditorapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.amanthebreaker.photoeditorapp.EditImageActivity
import com.amanthebreaker.photoeditorapp.R
import com.amanthebreaker.photoeditorapp.adapter.ColorPickerAdapter
import com.amanthebreaker.photoeditorapp.databinding.FragmentPropertiesBsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class PropertiesBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    lateinit var _binding: FragmentPropertiesBsBinding
    private val binding get() = _binding
    private var mProperties: Properties? = null

    interface Properties {
        fun onColorChanged(colorCode: Int)
        fun onOpacityChanged(opacity: Int)
        fun onBrushSizeChanged(brushSize: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentPropertiesBsBinding.inflate(inflater,container,false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sbOpacity.setOnSeekBarChangeListener(this)
        binding.sbSize.setOnSeekBarChangeListener(this)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL,false)

        binding.rvColors.layoutManager = layoutManager
        binding.rvColors.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)
        colorPickerAdapter.setOnColorPickerClickListener(object : ColorPickerAdapter.OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                if(mProperties != null) {
                    dismiss()
                    mProperties!!.onColorChanged(colorCode)
                }
            }
        })
        binding.rvColors.adapter = colorPickerAdapter
    }

    fun setPropertiesChangeListener(properties: EditImageActivity) {
        mProperties = properties
    }

    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sbOpacity -> if (mProperties != null) {
                mProperties!!.onOpacityChanged(i)
            }
            R.id.sbSize -> if (mProperties != null) {
                mProperties!!.onBrushSizeChanged(i)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}


}