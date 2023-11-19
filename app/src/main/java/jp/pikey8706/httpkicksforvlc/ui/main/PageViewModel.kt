package jp.pikey8706.httpkicksforvlc.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class PageViewModel : ViewModel() {
    private val mChannel = MutableLiveData<String>()
    val text = mChannel.map { channel -> "CH: $channel" }

    fun setChannel(channel: String) {
        mChannel.value = channel
    }
}