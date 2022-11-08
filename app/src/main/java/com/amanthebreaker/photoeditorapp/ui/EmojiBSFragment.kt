package com.amanthebreaker.photoeditorapp.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amanthebreaker.photoeditorapp.EditImageActivity
import com.amanthebreaker.photoeditorapp.R
import com.amanthebreaker.photoeditorapp.databinding.FragmentEmojiBsBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class EmojiBSFragment : BottomSheetDialogFragment()  {
    private var mEmojiListener: EmojiListener? = null
    interface EmojiListener {
        fun onEmojiClick(emojiUnicode: String?)
    }
    lateinit var binding : FragmentEmojiBsBinding

    private val mBottomSheetBehaviorCallback: BottomSheetBehavior.BottomSheetCallback = object :
        BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = FragmentEmojiBsBinding.inflate(layoutInflater)
        val contentView = View.inflate(context, R.layout.fragment_emoji_bs,null)
        dialog.setContentView(contentView)

        val params: CoordinatorLayout.LayoutParams =
            (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.getBehavior()
        if (behavior != null && behavior is BottomSheetBehavior) {
            (behavior as BottomSheetBehavior).setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(getResources().getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(getActivity(), 5)
        rvEmoji.setLayoutManager(gridLayoutManager)
        val emojiAdapter: EmojiAdapter = EmojiAdapter()
        rvEmoji.setAdapter(emojiAdapter)
    }
    fun setEmojiListener(emojiListener: EmojiListener) {
        mEmojiListener = emojiListener
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_emoji_bs, container, false)
    }

    inner class EmojiAdapter : RecyclerView.Adapter<EmojiAdapter.ViewHolder?>() {
        var emojisList: ArrayList<String> = getEmojis(requireContext())
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_emoji, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtEmoji.text = emojisList[position]
        }
        override fun getItemCount(): Int = emojisList.size



        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var txtEmoji: TextView

            init {
                txtEmoji = itemView.findViewById(R.id.txtEmoji)
                itemView.setOnClickListener {
                    if (mEmojiListener != null) {
                        mEmojiListener!!.onEmojiClick(emojisList[getLayoutPosition()])
                    }
                    dismiss()
                }
            }
        }

        fun getEmojis(context: Context): ArrayList<String> {
            val convertedEmojiList = java.util.ArrayList<String>()
            val emojiList = context.resources.getStringArray(R.array.photo_editor_emoji)
            for (emojiUnicode in emojiList) {
                convertEmoji(emojiUnicode)?.let { convertedEmojiList.add(it) }
            }
            return convertedEmojiList
        }

        private fun convertEmoji(emoji: String): String? {
            val returnedEmoji: String
            returnedEmoji = try {
                val convertEmojiToInt = emoji.substring(2).toInt(16)
                String(Character.toChars(convertEmojiToInt))
            } catch (e: NumberFormatException) {
                ""
            }
            return returnedEmoji
        }


    }


}