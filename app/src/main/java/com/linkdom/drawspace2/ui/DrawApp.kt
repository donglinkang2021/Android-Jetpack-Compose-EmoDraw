package com.linkdom.drawspace2.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.linkdom.drawspace2.data.datastore.StoreUserPreferences
import com.linkdom.drawspace2.data.datastore.UserPreferences
import com.linkdom.drawspace2.data.model.ModelStatus
import com.linkdom.drawspace2.data.model.Screen
import com.linkdom.drawspace2.ui.component.DrawAppBottomBar
import com.linkdom.drawspace2.ui.component.DrawAppTopBar
import com.linkdom.drawspace2.ui.component.StatusText
import com.linkdom.drawspace2.ui.screen.MainScreen
import com.linkdom.drawspace2.ui.screen.ModelSelectionScreen
import com.linkdom.drawspace2.ui.screen.PreferScreen
import com.linkdom.drawspace2.utils.makeToast
import kotlinx.coroutines.launch

@Composable
fun DrawApp() {
    DrawScreen()
}

@Preview(showBackground = true)
@Composable
fun DrawScreen(
    modifier: Modifier = Modifier,
    drawViewModel: DrawViewModel = viewModel()
) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // datastore 读取用户设置
    val storeUserPreferences = StoreUserPreferences(context)
    val userPreferences = storeUserPreferences.getUserPreferences.collectAsState(initial = UserPreferences())
    val drawState by drawViewModel.uiState.collectAsState()


    // 下面代码只触发一次，用于初始化
    if (drawState.isFirstTime) {
        drawViewModel.initByPreference(userPreferences.value)
    }


    drawViewModel.checkActiveModel(drawState.activeModelName) // 这一行不能删除，用于实时刷新图标
    drawViewModel.refreshDownloadedModelsStatus() // 这一行在这里会一直运行，每次都会刷新
    Scaffold(
        topBar = {
            DrawAppTopBar(
                title = { Text(text = "EmoDraw") },
                modelStatus = drawState.modelStatus,
                selectedModelName = drawState.activeModelLabel,
                onModelListExpandedChange = {
                    makeToast(drawState.isShowInfo,context, "展开模型列表")
                    drawViewModel.expandModelList()
                },
                onDownloadModelClick = {
                    when (drawState.modelStatus) {
                        ModelStatus.DOWNLOADING -> {
                            makeToast(drawState.isShowInfo,context, "模型下载中")
                        }
                        ModelStatus.DOWNLOADED -> {
                            makeToast(drawState.isShowInfo,context, "模型已下载完毕")
                        }
                        ModelStatus.NOT_DOWNLOADED -> {
                            makeToast(drawState.isShowInfo,context, "开始下载模型")
                            drawViewModel.setActiveModel(drawState.activeModelName)
                            drawViewModel.download()
                            drawViewModel.refreshDownloadedModelsStatus()
                        }
                    }
                },
                onDeleteClick = {
                    makeToast(drawState.isShowInfo,context, "模型已被删除")
                    drawViewModel.deleteActiveModel()
                    drawViewModel.refreshDownloadedModelsStatus()
                },
                onSettingPreferences = {
                    drawViewModel.expandPreference()
                }
            )
        },
        bottomBar = {
            DrawAppBottomBar(
                isShowBottomStatus = drawState.isShowBottomStatus,
                isShowBottomButton = drawState.isShowBottomButton,
                statusShow = { StatusText(text = drawState.statusText) },
                onClear = { drawViewModel.resetFinalText() },
                onDownload = {
                    drawViewModel.setActiveModel(drawState.activeModelName)
                    drawViewModel.download()
                }
            )
        }
    ) {
        PaddingValues ->when (drawState.currentScreen) {
            Screen.MODEL_SELECT ->{
                ModelSelectionScreen(
                    modifier = modifier.padding(PaddingValues),
                    downloadedLanguageTags = drawState.downloadedLanguageTags,
                    onContainerSelected = {
                        drawViewModel.setActiveModel(it)
                        drawViewModel.collapseModelList()
                    },
                )
            }
            Screen.DRAW ->{
                MainScreen(
                    modifier = modifier.padding(PaddingValues),
                    finalText = drawState.finalText,
                    onFinalTextChange = {
                        drawViewModel.onFinalTextChanged(it)
                    },
                    predictions = drawState.predictions,
                    onPredictionSelected = {
                        drawViewModel.onPredictionSelected(it)
                    },
                    onResetCanvas = drawState.resetCanvas,
                    onDrawEvent = {
                        drawViewModel.onDrawEvent(it)
                    }
                )
            }
            Screen.PREFERENCES ->{
                drawViewModel.setInitComplete()
                PreferScreen(
                    modifier = modifier.padding(PaddingValues),
                    debugText = {
//                        Text(text = userPreferences.value.toString())
                    },
                    onSaveSettings = {
                        scope.launch {
                            storeUserPreferences.saveUserPreferences(
                                UserPreferences(
                                    isShowStatus = drawState.isShowBottomStatus,
                                    isShowBottomButton = drawState.isShowBottomButton,
                                    isShowInfo = drawState.isShowInfo
                                )
                            )
                        }
                    },
                    onDeleteAllModel = {
                        drawViewModel.deleteAllModels()
                        drawViewModel.refreshDownloadedModelsStatus()
                    },
                    onExit = {
                        drawViewModel.collapsePreference()
                    },
                    isShowBottomStatus = drawState.isShowBottomStatus,
                    isShowBottomButton = drawState.isShowBottomButton,
                    isShowInfo = drawState.isShowInfo,
                    onShowStatusChange = { drawViewModel.onShowStatusChange(it) },
                    onShowBottomBarChange = { drawViewModel.onShowBottomButtonChange(it) },
                    onShowInfoChange = { drawViewModel.onShowInfoChange(it) },
                )
            }

            else -> {
                Text(text = "Error")
            }
        }
    }
//    Text(text = userSettings.value!!)
}

