package com.example.voiceapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.*
import android.view.View
import androidx.lifecycle.ViewModelProvider

/**
 * 主活动类，负责处理用户界面和语音识别/合成的交互
 */
class MainActivity : AppCompatActivity() {
    // UI 组件
    private lateinit var resultText: TextView
    private lateinit var startRecognitionButton: Button
    private lateinit var speakButton: Button
    
    // ViewModel 实例
    private val viewModel: VoiceViewModel by viewModels()
    
    // 权限请求码
    private val PERMISSION_REQUEST_CODE = 1

    /**
     * 权限请求启动器，处理录音权限的请求结果
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition()
        } else {
            Toast.makeText(this, "需要录音权限才能使用语音识别功能", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 语音识别结果启动器，处理语音识别的结果
     */
    private val speechRecognizer = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.stopSpeechRecognition()
        
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            results?.get(0)?.let { recognizedText ->
                viewModel.updateRecognizedText(recognizedText)
            }
        } else {
            Toast.makeText(this, "语音识别失败，请重试", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化 UI 组件
        resultText = findViewById(R.id.resultText)
        startRecognitionButton = findViewById(R.id.startRecognitionButton)
        speakButton = findViewById(R.id.speakButton)

        // 设置观察者和点击监听器
        setupObservers()
        setupClickListeners()

        // 观察识别文本变化
        viewModel.recognizedText.observe(this) { text ->
            resultText.text = text
        }

        // 观察大字体显示文本变化
        viewModel.displayText.observe(this) { text ->
            val displayTextView = findViewById<TextView>(R.id.displayText)
            if (text.isNotEmpty()) {
                displayTextView.text = text
                displayTextView.visibility = View.VISIBLE
            } else {
                displayTextView.visibility = View.GONE
            }
        }

        // 观察语音识别状态的变化
        viewModel.isListening.observe(this) { isListening ->
            startRecognitionButton.isEnabled = !isListening
        }

        // 观察语音合成状态的变化
        viewModel.ttsStatus.observe(this) { isReady ->
            speakButton.isEnabled = isReady
        }
    }

    /**
     * 设置 LiveData 观察者，用于更新 UI
     */
    private fun setupObservers() {
        // 观察识别文本的变化
        viewModel.recognizedText.observe(this) { text ->
            resultText.text = text
        }

        // 观察大字体显示文本变化
        viewModel.displayText.observe(this) { text ->
            val displayTextView = findViewById<TextView>(R.id.displayText)
            if (text.isNotEmpty()) {
                displayTextView.text = text
                displayTextView.visibility = View.VISIBLE
            } else {
                displayTextView.visibility = View.GONE
            }
        }

        // 观察语音识别状态的变化
        viewModel.isListening.observe(this) { isListening ->
            startRecognitionButton.isEnabled = !isListening
        }

        // 观察语音合成状态的变化
        viewModel.ttsStatus.observe(this) { isReady ->
            speakButton.isEnabled = isReady
        }
    }

    /**
     * 设置按钮点击监听器
     */
    private fun setupClickListeners() {
        // 开始识别按钮点击事件
        startRecognitionButton.setOnClickListener {
            if (checkPermission()) {
                startSpeechRecognition()
            } else {
                requestPermission()
            }
        }

        // 朗读按钮点击事件
        speakButton.setOnClickListener {
            val text = resultText.text.toString()
            if (text.isNotEmpty()) {
                viewModel.speakText(text)
            } else {
                Toast.makeText(this, "请先进行语音识别", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 检查是否已获得录音权限
     */
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 请求录音权限
     */
    private fun requestPermission() {
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * 启动语音识别
     */
    private fun startSpeechRecognition() {
        try {
            // 创建语音识别意图
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh-CN")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            viewModel.startSpeechRecognition()
            speechRecognizer.launch(intent)
        } catch (e: Exception) {
            viewModel.stopSpeechRecognition()
            Toast.makeText(this, "您的设备不支持语音识别: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
} 