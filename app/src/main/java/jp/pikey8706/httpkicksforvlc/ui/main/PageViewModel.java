package jp.pikey8706.httpkicksforvlc.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {

    private MutableLiveData<String> mChannel = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mChannel, new Function<String, String>() {
        @Override
        public String apply(String channel) {
            return "CH: " + channel;
        }
    });

    public void setChannel(String channel) {
        mChannel.setValue(channel);
    }

    public LiveData<String> getText() {
        return mText;
    }
}