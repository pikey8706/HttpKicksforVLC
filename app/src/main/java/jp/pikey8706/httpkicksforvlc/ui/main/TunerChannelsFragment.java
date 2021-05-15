package jp.pikey8706.httpkicksforvlc.ui.main;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;

import jp.pikey8706.httpkicksforvlc.R;
import jp.pikey8706.httpkicksforvlc.kicks.Constants;
import jp.pikey8706.httpkicksforvlc.kicks.Utility;

/**
 * A placeholder fragment containing a simple view.
 */
public class TunerChannelsFragment extends Fragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener, View.OnLongClickListener,
        EditHostDialogFragment.EditHostDialogListener,
        EditChannelDialogFragment.EditChannelDialogListener {
    public static final String TAG = TunerChannelsFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";

    private SharedPreferences mSharedPreference;
    private PageViewModel pageViewModel;

    private RadioGroup mRadioGroup;
    private RadioButton mRadioButton;
    private String mKeySelectedHost;
    private String[] mHostNameKeys;
    private String[] mHostKeys;

    private ListView mListViewChannels;
    private ArrayAdapter mArrayAdapter;

    private String[] mKeyChannels;
    private String[] mChannelNameAndIds;
    private String[] mChannelNames;
    private String[] mChannelIds;
    private ArrayList<String> mChannelNamesList = new ArrayList<>();

    public static TunerChannelsFragment newInstance(int index) {
        TunerChannelsFragment fragment = new TunerChannelsFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    private int getSectionNumber() {
        return getArguments().getInt(ARG_SECTION_NUMBER);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "onCreate: " + getSectionNumber());
        super.onCreate(savedInstanceState);
        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(getContext());
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        Constants.Companion.init();

        mKeySelectedHost = (index == 1) ? Constants.KEY_SELECTED_HOST_TS : Constants.KEY_SELECTED_HOST_BS;
        mHostNameKeys = (index == 1) ? Constants.KEY_HOST_NAMES_TS : Constants.KEY_HOST_NAMES_BS;
        mHostKeys = (index == 1) ? Constants.KEY_HOSTS_TS : Constants.KEY_HOSTS_BS;

        mKeyChannels = (index == 1) ? Constants.KEY_CHANNELS_TS : Constants.KEY_CHANNELS_BS;
        int listNamesResId = (index == 1) ? R.array.ts_channel_names : R.array.bs_channel_names;
        int listIdsResId = (index == 1) ? R.array.ts_channel_ids : R.array.bs_channel_ids;
        String[] defaultChannelNames = getResources().getStringArray(listNamesResId);
        String[] defaultChannelIds = getResources().getStringArray(listIdsResId);
        String[] defaultChannelNameAndIds = new String[mKeyChannels.length];
        for (int count = 0; count < defaultChannelNameAndIds.length; count++) {
            String channelName = count < defaultChannelNames.length ? defaultChannelNames[count] : "";
            String channelId = count < defaultChannelIds.length ? defaultChannelIds[count] : "";
            defaultChannelNameAndIds[count] =
                    Utility.getChannelNameAndId(channelName, channelId);
        }

        mChannelNameAndIds = new String[mKeyChannels.length];
        mChannelNames = new String[mKeyChannels.length];
        mChannelIds = new String[mKeyChannels.length];
        int count = 0;
        for (String keyChannel : mKeyChannels) {
            if (defaultChannelNameAndIds[count].equals("")) {
                break;
            }
            String channelNameAndId = Utility.loadPref(keyChannel, defaultChannelNameAndIds[count],
                    mSharedPreference);
            mChannelNameAndIds[count] = channelNameAndId;
            String channelName = Utility.getChannelNamePart(channelNameAndId);
            String channelId = Utility.getChannelIdPart(channelNameAndId);
            mChannelNames[count] = channelName;
            mChannelNamesList.add(count, channelName);
            mChannelIds[count] = channelId;
            count++;
        }

        mArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item, mChannelNamesList);
    }

    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView: " + getSectionNumber());
        View root = inflater.inflate(R.layout.fragment_tuner_channels, container, false);
        final TextView textView = root.findViewById(R.id.selected_channel);
        pageViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        mRadioGroup = root.findViewById(R.id.host_group);
        mRadioGroup.check(Utility.loadPref(mKeySelectedHost, 0, mSharedPreference));
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                Log.v(TAG, "onCheckedChanged");
                Utility.savePref(mKeySelectedHost, radioGroup.getCheckedRadioButtonId(), mSharedPreference);
            }
        });
        for (int index = 0; index < mRadioGroup.getChildCount(); index++) {
            RadioButton oneRadioButton = (RadioButton) mRadioGroup.getChildAt(index);
            String hostNameKey = mHostNameKeys[index];
            String hostKey = mHostKeys[index];
            oneRadioButton.setTag(R.id.view_id_host_name, hostNameKey);
            oneRadioButton.setTag(R.id.view_id_host_address, hostKey);
            String hostPortAddress = Utility.loadPref(hostKey,
                    getString(R.string.protocol_http), mSharedPreference);
            oneRadioButton.setText(hostPortAddress);
            oneRadioButton.setOnLongClickListener(TunerChannelsFragment.this);
        }

        mListViewChannels = root.findViewById(R.id.listViewChannels);
        mListViewChannels.setAdapter(mArrayAdapter);
        mListViewChannels.setOnItemClickListener(this);
        mListViewChannels.setOnItemLongClickListener(this);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.v(TAG, "onViewCreated: " + getSectionNumber());
        super.onViewCreated(view, savedInstanceState);
        ViewCompat.setNestedScrollingEnabled(mListViewChannels, true);
    }

    private RadioButton getSelectedRadioButton() {
        RadioGroup urlGroup = getView().findViewById(R.id.host_group);
        int selectedUrlId = urlGroup.getCheckedRadioButtonId();
        View view = urlGroup.findViewById(selectedUrlId);
        return (view instanceof RadioButton) ? (RadioButton) view : null;
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.v(TAG, "onAttachFragment: " + childFragment);
        if (childFragment instanceof EditHostDialogFragment) {
            ((EditHostDialogFragment) childFragment).setListener(this);
        } else if (childFragment instanceof EditChannelDialogFragment) {
            ((EditChannelDialogFragment) childFragment).setListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause: " + getSectionNumber());
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "onResume: " + getSectionNumber());
    }

    @Override
    public void onDestroyView() {
        Log.v(TAG, "onDestroyView: " + getSectionNumber());
        super.onDestroyView();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String channelName = ((TextView) view).getText().toString();
        String channelId = mChannelIds[i];

        pageViewModel.setChannel(channelName);

        String baseUrl = null;
        RadioButton selectedUrlButton = getSelectedRadioButton();
        if (selectedUrlButton != null) {
            baseUrl = selectedUrlButton.getText().toString();
        }
        String channelUrl = baseUrl + "/" + channelId;

        String msg = "onItemClick channel name: " + channelName + " id: " + channelId
                + " baseUrl: " + baseUrl;
        Log.v(TAG, msg);

        Utility.startMPEGHttpForVlc(getContext(), channelUrl);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int index, long longId) {
        Log.v(TAG, "onItemLongClick index: " + index);
        EditChannelDialogFragment editChannelDialogFragment = new EditChannelDialogFragment();
        Bundle bundle = new Bundle();
        String channelNameAndId = mChannelNameAndIds[index];
        bundle.putInt(Constants.INDEX_EDIT_CHANNEL, index);
        bundle.putString(Constants.VALUE_EDIT_CHANNEL, channelNameAndId);
        editChannelDialogFragment.setArguments(bundle);
        editChannelDialogFragment.show(getChildFragmentManager(), "editChannel");
        return true;
    }

    @Override
    public boolean onLongClick(View view) {
        int viewId = view.getId();
        Log.v(TAG, "onLongClick id: " + viewId);
        EditHostDialogFragment editHostDialogFragment = new EditHostDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.KEY_EDIT_HOST_NAME, (String) view.getTag(R.id.view_id_host_name));
        bundle.putString(Constants.KEY_EDIT_HOST, (String) view.getTag(R.id.view_id_host_address));
        bundle.putInt(Constants.KEY_EDIT_HOST_VIEW_ID, viewId);
        editHostDialogFragment.setArguments(bundle);
        editHostDialogFragment.show(getChildFragmentManager(), "editHost");
        return true;
    }

    @Override
    public void onEditHostDialogDone(int viewId, String keyHostName, String keyHost, String hostName, String host, String port) {
        String hostPortAddress = Utility.getHttpHostAddress(host, port);
        Log.v(TAG, "onEditHostDialogDone hostName: " + hostName + " hostPortAddress: " + hostPortAddress);
        Utility.savePref(keyHostName, hostName, mSharedPreference);
        Utility.savePref(keyHost, hostPortAddress, mSharedPreference);

        RadioButton radioButton = getView().findViewById(viewId);
        radioButton.setText(hostPortAddress);
    }

    @Override
    public void onEditChannelDialogDone(int indexEditChannel, String ch_name, String ch_id) {
        String channelNameAndId = Utility.getChannelNameAndId(ch_name, ch_id);
        Log.v(TAG, "onEditChannelDialogDone hostPortAddress: " + channelNameAndId);
        String keyChannel = mKeyChannels[indexEditChannel];
        Utility.savePref(keyChannel, channelNameAndId, mSharedPreference);

        mChannelNameAndIds[indexEditChannel] = channelNameAndId;
        mChannelNames[indexEditChannel] = ch_name;
        mChannelNamesList.set(indexEditChannel, ch_name);
        mChannelIds[indexEditChannel] = ch_id;
        mArrayAdapter.notifyDataSetChanged();
    }
}