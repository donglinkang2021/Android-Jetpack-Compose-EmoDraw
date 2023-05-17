package com.linkdom.drawspace2.ui.screen


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.linkdom.drawspace2.R
import com.linkdom.drawspace2.data.ModelProvider
import com.linkdom.drawspace2.ui.component.ModelContainer
import com.linkdom.drawspace2.ui.component.SearchBar


@Preview(showBackground = true)
@Composable
fun ModelSelectionScreen(
    modifier: Modifier = Modifier,
    downloadedLanguageTags: Set<String> = setOf(),
    onContainerSelected: (String) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        SearchBar(
            query = query,
            onQueryChange = {
                query = it
            }
        )
        LazyColumn{
            items(ModelProvider().getModelContainer(query)){
                ModelContainer(
                    model = it,
                    downloadedLanguageTags = downloadedLanguageTags,
                    onClick = onContainerSelected
                )
            }
        }
    }
}