package com.linkdom.drawspace2.ui.component

import android.app.AlertDialog
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true)
@Composable
fun AlertButton(
    modifier: Modifier = Modifier,
    onDeleteAllModel: () -> Unit = {}
){
    var isShowDialog by remember { mutableStateOf(false) }
    TextButton(
        modifier = modifier.padding(4.dp),
        onClick = {
            isShowDialog = true
        }
    ) {
        Text(text = "清空缓存")
    }


    MyAlertDialog(
        isShowDialog = isShowDialog,
        onConfirm = onDeleteAllModel,
        onDismiss = { isShowDialog = false }
    )
}

@Composable
fun MyAlertDialog(
    modifier: Modifier = Modifier,
    isShowDialog: Boolean = false,
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    if (isShowDialog) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            title = { Text(text = "清空缓存") },
            text = { Text(text = "确定要删除所有已下载模型吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: 清空缓存
                        onConfirm()
                        onDismiss()
                    }
                ) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(text = "取消")
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AlertDialogPreview(){
    MyAlertDialog(
        isShowDialog = true,
        onConfirm = {},
        onDismiss = {}
    )
}