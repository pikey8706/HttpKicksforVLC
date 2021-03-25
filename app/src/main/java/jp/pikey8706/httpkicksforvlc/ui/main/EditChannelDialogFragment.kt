package jp.pikey8706.httpkicksforvlc.ui.main

import android.app.AlertDialog
import android.app.Dialog
import jp.pikey8706.httpkicksforvlc.ui.main.EditChannelDialogFragment.EditChannelDialogListener
import android.os.Bundle
import android.view.LayoutInflater
import jp.pikey8706.httpkicksforvlc.R
import android.widget.EditText
import android.content.DialogInterface
import android.view.View
import androidx.fragment.app.DialogFragment
import jp.pikey8706.httpkicksforvlc.kicks.Constants
import jp.pikey8706.httpkicksforvlc.kicks.Utility

class EditChannelDialogFragment : DialogFragment() {
    interface EditChannelDialogListener {
        fun onEditChannelDialogDone(indexEditChannel: Int, ch_name: String?, ch_id: String?)
    }

    var mEditChannelDialogListener: EditChannelDialogListener? = null
    fun setListener(listener: EditChannelDialogListener?) {
        mEditChannelDialogListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_channel, null)
        val channelNameAndId = requireArguments().getString(Constants.VALUE_EDIT_CHANNEL)
        val namePart = Utility.getChannelNamePart(channelNameAndId)
        val idPart = Utility.getChannelIdPart(channelNameAndId)
        (view.findViewById<View>(R.id.channel_name) as EditText).setText(namePart)
        (view.findViewById<View>(R.id.channel_id) as EditText).setText(idPart)
        val builder = AlertDialog.Builder(activity)
        builder.setView(view)
        builder.setMessage(R.string.edit_channel_name_and_id)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    val indexEditChannel = requireArguments().getInt(Constants.INDEX_EDIT_CHANNEL)
                    val alertDialog = dialog as AlertDialog
                    val channelName = alertDialog.findViewById<EditText>(R.id.channel_name)
                    val channelId = alertDialog.findViewById<EditText>(R.id.channel_id)
                    mEditChannelDialogListener?.onEditChannelDialogDone(
                            indexEditChannel,
                            channelName.text.toString(),
                            channelId.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }
}