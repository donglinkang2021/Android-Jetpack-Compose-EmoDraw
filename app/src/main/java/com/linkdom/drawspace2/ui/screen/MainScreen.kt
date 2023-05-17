package com.linkdom.drawspace2.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.linkdom.drawspace2.R
import com.linkdom.drawspace2.ui.component.Predictions
import com.linkdom.drawspace2.ui.component.space.DrawEvent
import com.linkdom.drawspace2.ui.component.space.DrawSpace

@Preview(showBackground = true)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    finalText: String = "",
    onFinalTextChange: (String) -> Unit = {},
    predictions: List<String> = listOf(),
    onPredictionSelected: (String) -> Unit = {},
    onResetCanvas: Boolean = false,
    onDrawEvent: (DrawEvent) -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize()
    ){
        // region 输入框
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = finalText,
            onValueChange = onFinalTextChange,
            placeholder = { Text(text = stringResource(id = R.string.input_info)) }
        )
        // endregion

        // region 预测列表
        Predictions(
            predictions = predictions, // testDrawState.predictions
            onClick = onPredictionSelected
        )
        // endregion

        // region 画布
        DrawSpace(
            Modifier.fillMaxSize(),
            reset = onResetCanvas,
            onDrawEvent = onDrawEvent
        )
        // endregion
    }
}