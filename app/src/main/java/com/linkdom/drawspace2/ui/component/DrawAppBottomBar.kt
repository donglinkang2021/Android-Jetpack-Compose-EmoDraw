package com.linkdom.drawspace2.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


// 这个控件纯粹是为了测试APP的功能才使用的，在正经的APP中是不会使用的
@Composable
fun DrawAppBottomBar(
    modifier: Modifier = Modifier,
    isShowBottomStatus: Boolean = true,
    isShowBottomButton: Boolean = false,
    statusShow: @Composable () -> Unit,
    onClear: () -> Unit,
    onDownload: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* 显示status */
        if (isShowBottomStatus) {statusShow()}
        if (isShowBottomButton) {
            Row{
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = onClear
                ) {
                    Text(text = "Clear")
                }
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = onDownload
                ) {
                    Text(text = "Download")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DrawAppBottomBarPreview() {
    DrawAppBottomBar(
        isShowBottomStatus = true,
        isShowBottomButton = true,
        statusShow = {
            Text(text = "status")
        },
        onClear = {},
        onDownload = {}
    )
}