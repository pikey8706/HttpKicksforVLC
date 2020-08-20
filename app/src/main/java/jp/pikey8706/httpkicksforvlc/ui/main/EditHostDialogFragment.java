package jp.pikey8706.httpkicksforvlc.ui.main;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import jp.pikey8706.httpkicksforvlc.R;
import jp.pikey8706.httpkicksforvlc.kicks.Constants;
import jp.pikey8706.httpkicksforvlc.kicks.Utility;

public class EditHostDialogFragment extends DialogFragment {

    public interface EditHostDialogListener {
        void onEditHostDialogDone(int viewId, String keyHost, String host, String port);
    }

    EditHostDialogListener mEditHostDialogListener;

    public void setListener(EditHostDialogListener listener) {
        this.mEditHostDialogListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_host, null);
        String keyEditHost = getArguments().getString(Constants.KEY_EDIT_HOST);
        String hostAddressPort = Utility.loadPref(keyEditHost, getString(R.string.protocol_http),
                PreferenceManager.getDefaultSharedPreferences(getContext()));
        String hostPart = Utility.getHostPart(hostAddressPort);
        String portPart = Utility.getPortPart(hostAddressPort);
        ((EditText) view.findViewById(R.id.host_address)).setText(hostPart);
        ((EditText) view.findViewById(R.id.host_port)).setText(portPart);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setMessage(R.string.edit_host_address)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int viewIdEditHost = getArguments().getInt(Constants.KEY_EDIT_HOST_VIEW_ID);
                        String keyEditHost = getArguments().getString(Constants.KEY_EDIT_HOST);
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        EditText hostAddress = alertDialog.findViewById(R.id.host_address);
                        EditText hostPort = alertDialog.findViewById(R.id.host_port);
                        mEditHostDialogListener.onEditHostDialogDone(
                                viewIdEditHost,
                                keyEditHost,
                                hostAddress.getText().toString(),
                                hostPort.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }


}
