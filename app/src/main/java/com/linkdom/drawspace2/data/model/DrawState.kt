package com.linkdom.drawspace2.data.model

data class DrawState (
    val isFirstTime: Boolean = true,
    val resetCanvas : Boolean = false,
    val resetFinalText: Boolean = false,
    val isShowBottomStatus: Boolean = true,
    val isShowBottomButton: Boolean = false,
    val isShowInfo: Boolean = false,
    val currentScreen: Screen = Screen.DRAW,
    val statusText: String = "Model Download Status Text",
    val modelStatus: ModelStatus = ModelStatus.NOT_DOWNLOADED,
    val finalText: String = "",
    val predictions: List<String> = emptyList(),
    val activeModelName: String = "zxx-Zsye-x-emoji",
    val activeModelLabel: String = "Emoji",
    val downloadedLanguageTags: Set<String> = emptySet(),
)

// 测试样本
val testDrawState = DrawState(
    resetCanvas = false,
    statusText = "test",
    finalText = "test",
    predictions = listOf("🍤", "✋", "😚"),
    activeModelName = "zh-Hans"
)