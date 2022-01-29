package jp.pikey8706.httpkicksforvlc.kicks

import android.os.Handler
import android.os.Looper
import jp.pikey8706.httpkicksforvlc.kicks.XHandler
import android.os.HandlerThread
import android.os.Message
import android.util.Log

class XHandler private constructor(looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_ID_1 -> onXCB()
            MSG_ID_2 -> onYCB()
            else -> {}
        }
    }

    fun onX() {
        sendEmptyMessage(MSG_ID_1)
    }

    fun onY() {
        sendEmptyMessage(MSG_ID_2)
    }

    private fun onXCB() {
        Log.d(TAG, "onXCB")
    }

    private fun onYCB() {
        Log.d(TAG, "onYCB")
    }

    companion object {
        private val TAG = XHandler::class.java.simpleName
        const val MSG_ID_1 = 0
        const val MSG_ID_2 = 1
        private var sHandler: XHandler? = null
        private const val tagForHandler = "x-handler"
        private var sHandlerThread: HandlerThread? = null
        val instance: XHandler?
            get() {
                if (sHandler == null && sHandlerThread == null) {
                    sHandlerThread = HandlerThread(tagForHandler)
                    sHandlerThread!!.start()
                    sHandler = XHandler(sHandlerThread!!.looper)
                }
                return sHandler
            }
    }
}