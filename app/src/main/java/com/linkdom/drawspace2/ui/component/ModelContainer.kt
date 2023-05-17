package com.linkdom.drawspace2.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.linkdom.drawspace2.R
import com.linkdom.drawspace2.data.ModelProvider
import com.linkdom.drawspace2.data.model.ModelStatus

@Composable
fun ModelContainer(
    modifier: Modifier = Modifier,
    model: ModelProvider.ModelLanguageContainer,
    downloadedLanguageTags: Set<String> = setOf(),
    onClick: (String) -> Unit ,
) {
    Box (
        modifier = modifier
            .fillMaxWidth()
            .clickable {
                onClick(model.languageTag ?: "请选择模型")
            }
    ){
        // 将文本放在一个盒子里面，这样可以让文本居左对齐
        Text(
            text = model.toString(),
            style = when (model.languageTag != null) {
                true -> MaterialTheme.typography.body2 // 有语言标签的是模型
                false -> MaterialTheme.typography.h6  // 没有语言标签的是标题
            },
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .padding(10.dp)
        )
        if( model.languageTag != null) {
            Icon(
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .padding(end = 20.dp),
                painter = painterResource(
                    id = when (model.languageTag in downloadedLanguageTags) {
                        false -> R.drawable.baseline_download_24
                        true -> R.drawable.baseline_download_done_24
                    }
                ),
                contentDescription = null
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModelContainerPreview() {
    ModelContainer(
        model = ModelProvider.testEmojiModelContainer
    ){
        // do nothing
    }
}