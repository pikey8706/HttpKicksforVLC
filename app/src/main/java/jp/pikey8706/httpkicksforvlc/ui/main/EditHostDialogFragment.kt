package jp.pikey8706.httpkicksforvlc.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.preference.PreferenceManager
import jp.pikey8706.httpkicksforvlc.R
import jp.pikey8706.httpkicksforvlc.kicks.Constants
import jp.pikey8706.httpkicksforvlc.kicks.Utility

class EditHostDialogFragment : DialogFragment() {
    interface EditHostDialogListener {
        fun onEditHostDialogDone(viewId: Int,
                                 keyHostName: String?,
                                 keyHost: String?,
                                 hostName: String?,
                                 host: String?, port: String?)
    }

    private var mEditHostDialogListener: EditHostDialogListener? = null
    fun setListener(listener: EditHostDialogListener?) {
        mEditHostDialogListener = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Get the layout inflater
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_edit_host, null)
        val keyEditHostName = requireArguments().getString(Constants.KEY_EDIT_HOST_NAME)
        val keyEditHost = requireArguments().getString(Constants.KEY_EDIT_HOST)
        val hostName = Utility.loadPref(keyEditHostName, null,
                PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val hostAddressPort = Utility.loadPref(keyEditHost, getString(R.string.protocol_http),
                PreferenceManager.getDefaultSharedPreferences(requireContext()))
        val hostPart = Utility.getHostPart(hostAddressPort)
        val portPart = Utility.getPortPart(hostAddressPort)
        (view.findViewById<View>(R.id.host_name) as EditText).setText(hostName)
        (view.findViewById<View>(R.id.host_address) as EditText).setText(hostPart)
        (view.findViewById<View>(R.id.host_port) as EditText).setText(portPart)
        val builder = AlertDialog.Builder(activity)
        builder.setView(view)
        builder.setMessage(R.string.edit_host_address)
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    val viewIdEditHost = requireArguments().getInt(Constants.KEY_EDIT_HOST_VIEW_ID)
                    val keyEditHost2 = requireArguments().getString(Constants.KEY_EDIT_HOST)
                    val alertDialog = dialog as AlertDialog
                    val hostName2 = alertDialog.findViewById<EditText>(R.id.host_name)
                    val hostAddress = alertDialog.findViewById<EditText>(R.id.host_address)
                    val hostPort = alertDialog.findViewById<EditText>(R.id.host_port)
                    mEditHostDialogListener?.onEditHostDialogDone(
                            viewIdEditHost,
                            keyEditHostName,
                            keyEditHost2,
                            hostName2.text.toString(),
                            hostAddress.text.toString(),
                            hostPort.text.toString())
                }
                .setNegativeButton(android.R.string.cancel, null)
        return builder.create()
    }
}