package jp.pikey8706.httpkicksforvlc.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

class PageViewModel : ViewModel() {
    private val mChannel = MutableLiveData<String>()
    val text = Transformations.map(mChannel) { channel -> "CH: $channel" }

    fun setChannel(channel: String) {
        mChannel.value = channel
    }
}