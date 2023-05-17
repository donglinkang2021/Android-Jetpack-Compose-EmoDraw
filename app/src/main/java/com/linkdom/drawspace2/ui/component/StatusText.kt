package com.linkdom.drawspace2.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun StatusText(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier,
        text = text
    )
}

@Preview(showBackground = true)
@Composable
fun StatusTextPreview() {
    StatusText(
        modifier = Modifier.fillMaxWidth(),
        text = "Hello World"
    )
}