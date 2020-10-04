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

public class EditChannelDialogFragment extends DialogFragment {

    public interface EditChannelDialogListener {
        void onEditChannelDialogDone(int indexEditChannel, String ch_name, String ch_id);
    }

    EditChannelDialogListener mEditChannelDialogListener;

    public void setListener(EditChannelDialogListener listener) {
        this.mEditChannelDialogListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_channel, null);
        String channelNameAndId = getArguments().getString(Constants.VALUE_EDIT_CHANNEL);
        String namePart = Utility.getChannelNamePart(channelNameAndId);
        String idPart = Utility.getChannelIdPart(channelNameAndId);
        ((EditText) view.findViewById(R.id.channel_name)).setText(namePart);
        ((EditText) view.findViewById(R.id.channel_id)).setText(idPart);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setMessage(R.string.edit_channel_name_and_id)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        int indexEditChannel = getArguments().getInt(Constants.INDEX_EDIT_CHANNEL);
                        AlertDialog alertDialog = (AlertDialog) dialog;
                        EditText channelName = alertDialog.findViewById(R.id.channel_name);
                        EditText channelId = alertDialog.findViewById(R.id.channel_id);
                        mEditChannelDialogListener.onEditChannelDialogDone(
                                indexEditChannel,
                                channelName.getText().toString(),
                                channelId.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        return builder.create();
    }


}
