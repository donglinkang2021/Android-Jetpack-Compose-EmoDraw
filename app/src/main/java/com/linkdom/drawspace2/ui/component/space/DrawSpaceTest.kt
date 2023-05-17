package com.linkdom.drawspace2.ui.component.space

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.linkdom.drawspace2.ui.theme.DrawSpace2Theme

@Composable
fun DrawSpaceScreen(
    modifier: Modifier = Modifier,
    reset: Boolean = false,
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.background
    ) {

        DrawSpace(
            modifier = Modifier.fillMaxSize(),
            reset = reset
        ){
            /* 外界响应在这里，通过页面事件响应传达给上面的应用 */
        }
    }
}

@Composable
fun Greetings(modifier: Modifier = Modifier, name: String) {
    Text(text = "Hello $name!")
}

@Composable
fun DrawSpaceApp(){
    var resetCanvas by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Draw Space") },
                backgroundColor = MaterialTheme.colors.primary
            )
        },
        bottomBar = {
            Row() {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        resetCanvas = true
                    }
                ) {
                    Text(text = "Reset")
                }
            }
        }
    ) {
        PaddingValues -> DrawSpaceScreen(
            Modifier
                .fillMaxSize()
                .padding(PaddingValues),
            reset = resetCanvas
        )
        if (resetCanvas) {
            resetCanvas = false
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DrawSpaceScreenPreview() {
    DrawSpace2Theme(){
        DrawSpaceApp()
    }
}