package com.example.voiceapp

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.*

/**
 * 语音识别和合成的 ViewModel 类
 * 负责处理语音识别和合成的业务逻辑
 */
class VoiceViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {
    // 识别文本的 LiveData
    private val _recognizedText = MutableLiveData<String>()
    val recognizedText: LiveData<String> = _recognizedText

    // 语音识别状态的 LiveData
    private val _isListening = MutableLiveData<Boolean>()
    val isListening: LiveData<Boolean> = _isListening

    // 语音合成状态的 LiveData
    private val _ttsStatus = MutableLiveData<Boolean>()
    val ttsStatus: LiveData<Boolean> = _ttsStatus

    // 语音合成引擎
    private var textToSpeech: TextToSpeech? = null

    init {
        // 初始化语音合成引擎
        textToSpeech = TextToSpeech(application, this)
    }

    /**
     * 开始语音识别
     * 更新识别状态为正在识别
     */
    fun startSpeechRecognition() {
        _isListening.value = true
    }

    /**
     * 停止语音识别
     * 更新识别状态为未识别
     */
    fun stopSpeechRecognition() {
        _isListening.value = false
    }

    /**
     * 更新识别文本
     * @param text 识别到的文本
     */
    fun updateRecognizedText(text: String) {
        _recognizedText.value = text
    }

    /**
     * 朗读文本
     * @param text 要朗读的文本
     */
    fun speakText(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    /**
     * 语音合成引擎初始化回调
     * @param status 初始化状态
     */
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // 设置语音合成语言为中文
            val result = textToSpeech?.setLanguage(Locale("zh", "CN"))
            // 更新语音合成状态
            _ttsStatus.value = result != TextToSpeech.LANG_MISSING_DATA && 
                             result != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            _ttsStatus.value = false
        }
    }

    /**
     * ViewModel 销毁时的清理工作
     */
    override fun onCleared() {
        // 停止并关闭语音合成引擎
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onCleared()
    }
} 