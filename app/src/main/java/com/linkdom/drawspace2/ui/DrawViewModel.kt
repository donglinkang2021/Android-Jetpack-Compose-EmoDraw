package com.linkdom.drawspace2.ui

import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.Ink
import com.linkdom.drawspace2.data.DigitalInkProvider
import com.linkdom.drawspace2.data.ModelProvider
import com.linkdom.drawspace2.data.datastore.StoreUserPreferences
import com.linkdom.drawspace2.data.datastore.UserPreferences
import com.linkdom.drawspace2.data.model.DrawState
import com.linkdom.drawspace2.data.model.ModelStatus
import com.linkdom.drawspace2.data.model.Screen
import com.linkdom.drawspace2.ui.component.space.DrawEvent
import com.linkdom.drawspace2.utils.RecognitionTask
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class DrawViewModel() : ViewModel(){


    // 用于在笔画完成时完成记录的任务
    private var finishRecordingJob: Job? = null

    // 管理当前绘制的墨水。
    private var stateChangedSinceLastRequest = false // 记录自上次请求以来状态是否发生过变化。
    private var triggerRecognitionAfterInput = true // 是否在用户输入后自动触发识别。
    private var clearCurrentInkAfterRecognition = true // 是否在识别后清除当前墨水。


    // 定义UI状态
    private val _uiState = MutableStateFlow(DrawState())
    val uiState: StateFlow<DrawState> = _uiState.asStateFlow()
        .stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = DrawState(
            isFirstTime = true,
            currentScreen = Screen.DRAW,
            isShowBottomStatus = true,
            isShowBottomButton = true,
            isShowInfo = true,
        )
    )

    // 定义数字墨水提供者
    private val digitalInkProvider = DigitalInkProvider()

    // 用于处理识别和模型下载的任务。
    private var recognitionTask: RecognitionTask? = null

    // 根据用户偏好初始化
    fun initByPreference(userPreferences: UserPreferences) {
        _uiState.update {
            it.copy(
                isShowBottomStatus = userPreferences.isShowStatus,
                isShowBottomButton = userPreferences.isShowBottomButton,
                isShowInfo = userPreferences.isShowInfo,
            )
        }
    }

    fun setInitComplete(){
        _uiState.update {
            it.copy(
                isFirstTime = false,
            )
        }
    }


    // 定义UI事件

    // region 文本框操作
    fun resetFinalText() {
        _uiState.update { it.copy(finalText = "") }
    }

    fun onFinalTextChanged(userInput: String) {
        _uiState.update { it.copy(finalText = userInput) }
    }
    // endregion

    // region 画布清空
    private fun resetCanvas() {
        _uiState.update { it.copy(resetCanvas = true) }
    }

    private fun resetCanvasComplete() {
        _uiState.update { it.copy(resetCanvas = false) }
    }
    // endregion

    // region 预测输入选择
    fun onPredictionSelected(prediction: String) {
        _uiState.update { it.copy(finalText = it.finalText.plus(prediction)) }
    }
    // endregion

    // region 画布操作事件触发
    fun onDrawEvent(event: DrawEvent){
        when (event) {
            is DrawEvent.Down -> {
                this.finishRecordingJob?.cancel() // 取消完成记录的任务
                resetCanvasComplete()
                digitalInkProvider.record(event.x, event.y) // 记录笔画
            }

            is DrawEvent.Move -> {
                digitalInkProvider.record(event.x, event.y) // 记录笔画
            }

            is DrawEvent.Up -> {
                this.finishRecordingJob = viewModelScope.launch {
                    delay(DEBOUNCE_INTERVAL) // 等待一段时间以便完成记录
                    resetCanvas()
                    digitalInkProvider.finishRecording() // 完成记录
                    stateChangedSinceLastRequest = true // 标记自上次请求以来状态发生了变化。
                    if (triggerRecognitionAfterInput) { // 如果需要自动触发识别。
                        recognize() // 触发识别。
                    }
                }
            }
        }
    }
    // endregion

    // region 模型操作(激活、下载、删除、刷新)
    fun refreshDownloadedModelsStatus() {
        // app 运行开始的时候执行一次
        // 每删除的时候执行一次
        // 每下载的时候执行一次
        digitalInkProvider
            .downloadedModelLanguages() // 获取已下载模型的语言标签集合。
            .addOnSuccessListener {
                downloadedLanguageTags: Set<String> ->
                    _uiState.update {
                        it.copy(
                            downloadedLanguageTags = downloadedLanguageTags,
                        )
                    }
            }
    }

    fun download(): Task<Nothing?> {
        // 更新状态为“下载已开始”。
        _uiState.update {
            it.copy(
                statusText = "Download started.",
                modelStatus = ModelStatus.DOWNLOADING
            )
        }
        return digitalInkProvider
            .download() // 开始下载模型。
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        statusText = "Download success.",
                        modelStatus = ModelStatus.DOWNLOADED
                    )
                }
            } // 下载成功后刷新下载模型状态
            .onSuccessTask(
                SuccessContinuation { status: String? ->
                    // 更新状态
                    _uiState.update { it.copy(statusText = status?:"") }
                    return@SuccessContinuation Tasks.forResult(null)
                }
            )
    }

    fun setActiveModel(languageTag: String) {
        val status = digitalInkProvider.setModel(languageTag)
        _uiState.update {
            it.copy(
                statusText = status,
                activeModelName = languageTag,
            )
        }
        val label = ModelProvider().getModelLabel(languageTag)
        digitalInkProvider
            .checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                // 更新状态。
                _uiState.update {
                    it.copy(
                        activeModelLabel = label,
                        modelStatus = if (result == true) ModelStatus.DOWNLOADED else ModelStatus.NOT_DOWNLOADED
                    )
                }
                return@onSuccessTask Tasks.forResult(null)
            }
    }

    fun checkActiveModel(languageTag: String) {
        digitalInkProvider.setModel(languageTag)
        val label = ModelProvider().getModelLabel(languageTag)
        digitalInkProvider
            .checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                // 更新状态。
                _uiState.update {
                    it.copy(
                        activeModelLabel = label,
                        modelStatus = if (result == true) ModelStatus.DOWNLOADED else ModelStatus.NOT_DOWNLOADED
                    )
                }
                return@onSuccessTask Tasks.forResult(null)
            }
    }

    fun deleteAllModels(): Task<Nothing?> {
        return digitalInkProvider
            .deleteAllModels() // 删除所有模型。
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        statusText = "Delete all model success.",
                        modelStatus = ModelStatus.NOT_DOWNLOADED
                    )
                }
            } // 删除成功后刷新下载模型状态。
            .onSuccessTask(
                SuccessContinuation { status: String? ->
                    // 更新状态。
                    _uiState.update { it.copy(statusText = status?:"") }
                    return@SuccessContinuation Tasks.forResult(null)
                }
            )
    }

    fun deleteActiveModel(): Task<Nothing?> {
        return digitalInkProvider
            .deleteActiveModel() // 删除活动模型。
            .addOnSuccessListener {
                _uiState.update {
                    it.copy(
                        statusText = "Delete success.",
                        modelStatus = ModelStatus.NOT_DOWNLOADED
                    )
                }
            } // 删除成功后刷新下载模型状态。
            .onSuccessTask(
                SuccessContinuation { status: String? ->
                    // 更新状态。
                    _uiState.update { it.copy(statusText = status?:"") }
                    return@SuccessContinuation Tasks.forResult(null)
                }
            )
    }
    // endregion

    // region 识别（异步）
    private fun recognize(): Task<String?> {
        if (!stateChangedSinceLastRequest || digitalInkProvider.inkBuilder.isEmpty) { // 如果自上次请求以来状态未发生变化或墨水为空，则不执行识别。
            // 更新状态。
            _uiState.update { it.copy(statusText = "No recognition, ink unchanged or empty") }
            return Tasks.forResult(null) // 返回一个已完成的任务。
        }
        if (digitalInkProvider.recognizer == null) { // 如果识别器为空，则不执行识别。
            // 更新状态。
            _uiState.update { it.copy(statusText = "Recognizer not set") }
            return Tasks.forResult(null) // 返回一个已完成的任务。
        }
        return digitalInkProvider
            .checkIsModelDownloaded() // 检查模型是否已下载。
            .onSuccessTask { result: Boolean? ->
                if (!result!!) { // 如果模型未下载，则不执行识别。
                    // 更新状态。
                    _uiState.update { it.copy(statusText = "Model not downloaded yet") }
                    return@onSuccessTask Tasks.forResult<String?>(null) // 返回一个已完成的任务。
                }
                // 标记自上次请求以来状态未发生变化。
                stateChangedSinceLastRequest = false
                recognitionTask = RecognitionTask(
                    digitalInkProvider.recognizer, // 传递识别器。
                    digitalInkProvider.inkBuilder.build()
                )
                // 发送延时消息以触发 UI 超时
                uiHandler.sendMessageDelayed(
                    uiHandler.obtainMessage(TIMEOUT_TRIGGER),
                    DEBOUNCE_INTERVAL
                )
                // 启动识别任务
                recognitionTask!!.run()
            }
    }

    // 处理 UI 超时的 Handler。
    // 此 Handler 仅用于触发 UI 超时。每次发生 UI 交互时，
    // 都会通过清除此 Handler 上的队列并发送新的延迟消息（在 addNewTouchEvent 中）来重置计时器。
    private val uiHandler = Handler(
        Handler.Callback { msg: Message ->
            if (msg.what == TIMEOUT_TRIGGER) { // 如果是超时触发消息。
                Log.i(TAG, "Handling timeout trigger.")
                // 提交结果
                commitResult()
                commitResultList()
                return@Callback true
            }
            false
        }
    )

    private fun commitResult() {
        recognitionTask!!.result()?.let { result -> // 获取识别结果并判断是否为空。
            _uiState.update { it.copy(statusText = "Successful recognition: ${result.text}") }
            if (clearCurrentInkAfterRecognition) {
                resetCurrentInk() // 如果需要清除当前墨水，重置当前墨水。
            }
            // 如果内容更改监听器不为空，通知内容发生更改。
        }
    }

    private fun commitResultList() {
        recognitionTask!!.resultList()?.let { result -> // 获取识别结果并判断是否为空。
            _uiState.update { it.copy(predictions = result.predictions as List<String>) }
            if (clearCurrentInkAfterRecognition) {
                resetCurrentInk() // 如果需要清除当前墨水，重置当前墨水。
            }
            // 如果内容更改监听器不为空，通知内容发生更改。
        }
    }

    private fun resetCurrentInk() {
        digitalInkProvider.inkBuilder = Ink.builder() // 重置 Ink.Builder 对象。
        digitalInkProvider.strokeBuilder = Ink.Stroke.builder() // 重置 Stroke.Builder 对象。
        stateChangedSinceLastRequest = false // 重置自上次请求以来状态是否发生过变化的标志位。
    }
    // endregion

    // region 用户自定义的设置
    fun onShowBottomButtonChange(isShowBottomButton: Boolean) {
        _uiState.update { it.copy(isShowBottomButton = isShowBottomButton) }
    }

    fun onShowStatusChange(isShowStatus: Boolean) {
        _uiState.update { it.copy(isShowBottomStatus = isShowStatus) }
    }

    fun onShowInfoChange(isShowInfo: Boolean) {
        _uiState.update { it.copy(isShowInfo = isShowInfo) }
    }
    // endregion

    // region 切换屏幕
    fun expandModelList() {
        _uiState.update { it.copy(currentScreen = Screen.MODEL_SELECT) }
    }

    fun collapseModelList() {
        _uiState.update { it.copy(currentScreen = Screen.DRAW) }
    }

    fun expandPreference() {
        _uiState.update { it.copy(currentScreen = Screen.PREFERENCES) }
    }

    fun collapsePreference() {
        _uiState.update { it.copy(currentScreen = Screen.DRAW) }
    }
    // endregion

    init{
        // 初始化 ViewModel 时，将当前屏幕设置为绘制屏幕。
        // 同时也将用户自定义的设置应用到 UI 状态中。
        _uiState.update {
            it.copy(
                currentScreen = Screen.DRAW,
            )
        }
    }


    companion object {
        private const val TAG = "LK.ViewModel"
        private const val DEBOUNCE_INTERVAL = 1000L // 相当于1秒

        // 这是一个常量，用作消息标识符以触发超时。
        private const val TIMEOUT_TRIGGER = 1
    }

}