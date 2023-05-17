package com.linkdom.drawspace2.data

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableSortedSet
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import java.util.Locale

// 借鉴了官方示例中的代码
class ModelProvider {

    // 获得langugaeTag对应的模型label
    fun getModelLabel(languageTag: String): String {
        if (NON_TEXT_MODELS.containsKey(languageTag)) {
            return NON_TEXT_MODELS[languageTag]!!
        } else {
            var label = ""
            for (modelContainer in getAllModelContainer()) {
                if (modelContainer.languageTag == languageTag) {
                    label = modelContainer.label
                    break
                }
            }
            return label
        }
    }


    // 查询包含query的语言标签的ModelContainer
    fun getModelContainer(
        query: String
    ): MutableList<ModelLanguageContainer> {
        if (query.isEmpty()) {
            return getAllModelContainer()
        }else if (query == "请搜索并选择你需要的模型") {
            return getAllModelContainer()
        }

        val languageAdapter: MutableList<ModelLanguageContainer> = ArrayList()
        for ( modelContainer in getAllModelContainer()) {
            if (modelContainer.toString().contains(query) || modelContainer.languageTag == null) {
                languageAdapter.add(modelContainer)
            }
        }
        return languageAdapter
    }


    // 用于获取所有模型标识符的列表
    private fun getAllModelContainer(): MutableList<ModelLanguageContainer> {
        val languageAdapter: MutableList<ModelLanguageContainer> = ArrayList()
//        languageAdapter.add(ModelLanguageContainer.createLabelOnly("选择语言"))

        // region 添加"非文本模型"
        // 添加“非文本模型”的标签
        languageAdapter.add(ModelLanguageContainer.createLabelOnly(" 非文本模型"))
        for (languageTag in NON_TEXT_MODELS.keys) {
            languageAdapter.add(
                ModelLanguageContainer.createModelContainer(NON_TEXT_MODELS[languageTag]!!, languageTag)
            )
        }
        // endregion

        // region 添加“文本模型”
        languageAdapter.add(ModelLanguageContainer.createLabelOnly(" 文本模型")) // 添加“文本模型”的标签
        val textModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>() // 创建一个排序的集合
        // 添加除已添加的模型之外的所有文本模型
        for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
            if (NON_TEXT_MODELS.containsKey(modelIdentifier.languageTag)) {
                // 如果模型标识符的语言标签已经存在于非文本模型中，则跳过
                continue
            }
            if (modelIdentifier.languageTag.endsWith(Companion.GESTURE_EXTENSION)) {
                // 如果模型标识符的语言标签以“手势”扩展名结尾，则跳过
                continue
            }
            textModels.add(buildModelContainer(modelIdentifier, "Script")) // 构建模型容器
        }
        languageAdapter.addAll(textModels.build()) // 将所有文本模型添加到适配器中
        // endregion

        // region 添加“手势模型”
        languageAdapter.add(ModelLanguageContainer.createLabelOnly(" 手势模型")) // 添加“手势模型”的标签
        val gestureModels = ImmutableSortedSet.naturalOrder<ModelLanguageContainer>() // 创建一个排序的集合
        // 添加所有手势模型
        for (modelIdentifier in DigitalInkRecognitionModelIdentifier.allModelIdentifiers()) {
            if (!modelIdentifier.languageTag.endsWith(GESTURE_EXTENSION)) {
                continue
            }
            gestureModels.add(buildModelContainer(modelIdentifier, "Script gesture classifier"))
        }
        languageAdapter.addAll(gestureModels.build()) // 将所有手势模型添加到适配器中
        // endregion

        return languageAdapter
    }

    private fun buildModelContainer(
        modelIdentifier: DigitalInkRecognitionModelIdentifier,
        labelSuffix: String
    ): ModelLanguageContainer {
        val label = StringBuilder() // 创建一个 StringBuilder 对象
        label.append(Locale(modelIdentifier.languageSubtag).displayName) // 添加语言子标记的本地化显示名称
        if (modelIdentifier.regionSubtag != null) {
            // 如果语言标记包含区域子标记，则添加区域子标记
            label.append(" (").append(modelIdentifier.regionSubtag).append(")")
        }
        if (modelIdentifier.scriptSubtag != null) {
            // 如果语言标记包含脚本子标记，则添加脚本子标记和标签后缀
            label.append(", ").append(modelIdentifier.scriptSubtag).append(" ").append(labelSuffix)
        }
        return ModelLanguageContainer.createModelContainer(
            label.toString(),
            modelIdentifier.languageTag
        )
    }

    // 在spinner中有很多这种container，每个container都有一个label和一个languageTag
    // 每个container都是一个ModelLanguageContainer
    class ModelLanguageContainer
    private constructor(val label: String, val languageTag: String?) :
        Comparable<ModelLanguageContainer> {

        // 是否已经下载
        var downloaded: Boolean = false

        override fun toString(): String {
            return when (languageTag) {
                null -> label
                else -> "    $label"
            }
        }

        override fun compareTo(other: ModelLanguageContainer): Int {
            return label.compareTo(other.label) // 按照标签进行比较
        }

        companion object {
            /** 创建并返回一个带有标签和语言标签的真实模型标识符 */
            fun createModelContainer(label: String, languageTag: String?): ModelLanguageContainer {
                // 为了更好的可读性，加了个语言标签
                return ModelLanguageContainer(label, languageTag)
            }

            /** 创建并返回一个只带有标签的对象，不含语言标签 */
            fun createLabelOnly(label: String): ModelLanguageContainer {
                return ModelLanguageContainer(label, null)
            }
        }
    }

    companion object {
        private val NON_TEXT_MODELS = // 非文本模型的映射表
            ImmutableMap.of(
                "zxx-Zsym-x-autodraw",
                "Autodraw",
                "zxx-Zsye-x-emoji",
                "Emoji",
                "zxx-Zsym-x-shapes",
                "Shapes"
            )
        private const val GESTURE_EXTENSION = "-x-gesture" // 手势模型的文件名后缀

        val testCNModelContainer = ModelLanguageContainer.createModelContainer("中文", "zh-Hani-CN")
        val testEmojiModelContainer = ModelLanguageContainer.createModelContainer("Emoji", "zxx-Zsye-x-emoji")
        val testENModelContainer = ModelLanguageContainer.createModelContainer("英文", "en-US")
    }
}

