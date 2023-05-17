package com.linkdom.drawspace2.data


import android.util.Log
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.channels.Channel
import java.util.HashSet


class DigitalInkProvider {
    // 用于保存笔画的列表
    private val predictions = Channel<List<String>>(3)

    // 构建 Ink 对象
    //    private var inkBuilder = Ink.builder()
    var strokeBuilder = Ink.Stroke.builder()
    var inkBuilder = Ink.builder() // 创建Ink.Builder对象，用于构建墨水。

    // 模型和识别器在下载的时候才能初始化
    private var model: DigitalInkRecognitionModel? = null // 当前使用的数字墨水识别模型。
    var recognizer: DigitalInkRecognizer? = null // 当前使用的数字墨水识别器。

    // 获取远程模型管理器
    private val remoteModelManager = RemoteModelManager.getInstance()

    fun downloadedModelLanguages():Task<Set<String>> {
        return remoteModelManager
            .getDownloadedModels(DigitalInkRecognitionModel::class.java) // 获取已下载的数字墨水识别模型集合。
            .onSuccessTask(
                SuccessContinuation { remoteModels: Set<DigitalInkRecognitionModel>? ->
                    val result: MutableSet<String> = HashSet()
                    for (model in remoteModels!!) {
                        result.add(model.modelIdentifier.languageTag) // 将每个数字墨水识别模型的语言标签添加到结果集合中。
                    }
                    Log.i(
                        TAG,
                        "Downloaded models for languages:$result"
                    ) // 打印日志。
                    Tasks.forResult<Set<String>>(result.toSet()) // 返回一个已完成的任务，并携带结果集合。
                }
            )
    }// 获取已下载模型的语言标签集合。

    // 下载模型
    fun download(): Task<String?> {
        return if (model == null) {
            Tasks.forResult("Model not selected.") // 如果模型未设置，则返回一个已完成的任务。
        } else remoteModelManager
            .download(model!!, DownloadConditions.Builder().build()) // 下载数字墨水识别模型。
            .onSuccessTask { _: Void? ->
                Log.i(
                    TAG,
                    "Model download succeeded."
                ) // 打印日志。
                Tasks.forResult("Downloaded model successfully") // 将返回一个已完成的任务。
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    TAG,
                    "Error while downloading the model: $e"
                ) // 打印日志。
            }
    }

    // 删除模型
    fun deleteActiveModel(): Task<String?> {
        if (model == null) {
            Log.i(TAG, "Model not set") // 如果模型未设置，则返回一个已完成的任务。
            return Tasks.forResult("Model not set")
        }
        return checkIsModelDownloaded() // 检查当前使用的数字墨水识别模型是否已下载。
            .onSuccessTask { result: Boolean? ->
                if (!result!!) {
                    return@onSuccessTask Tasks.forResult("Model not downloaded yet") // 如果模型尚未下载，则返回一个已完成的任务。
                }
                remoteModelManager
                    .deleteDownloadedModel(model!!) // 删除已下载的数字墨水识别模型。
                    .onSuccessTask { _: Void? ->
                        Log.i(
                            TAG,
                            "Model successfully deleted"
                        ) // 打印日志。
                        Tasks.forResult("Model successfully deleted") // 返回一个已完成的任务。
                    }
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    TAG,
                    "Error while model deletion: $e"
                ) // 打印日志。
            }
    }

    fun deleteAllModels(): Task<String?> {
        return remoteModelManager
            .getDownloadedModels(DigitalInkRecognitionModel::class.java) // 获取已下载的数字墨水识别模型集合。
            .onSuccessTask { remoteModels: Set<DigitalInkRecognitionModel>? ->
                val tasks: MutableList<Task<Void>> = ArrayList()
                for (model in remoteModels!!) {
                    tasks.add(remoteModelManager.deleteDownloadedModel(model)) // 将删除每个数字墨水识别模型的任务添加到任务列表中。
                }
                Tasks.whenAll(tasks) // 返回一个任务，该任务在所有任务完成时完成。
            }
            .onSuccessTask { _: Void? ->
                Log.i(
                    TAG,
                    "All models successfully deleted"
                ) // 打印日志。
                Tasks.forResult("All models successfully deleted") // 返回一个已完成的任务。
            }
            .addOnFailureListener { e: Exception ->
                Log.e(
                    TAG,
                    "Error while deleting all models: $e"
                ) // 打印日志。
            }
    }


    // 获取 DigitalInkRecognizer 的实例
    // Specify the recognition model for a language

    fun setModel(languageTag: String): String {
        // 清除旧的模型和识别器。
        model = null
        recognizer?.close()
        recognizer = null

        // 尝试解析 languageTag 并从中获取模型。
        val modelIdentifier: DigitalInkRecognitionModelIdentifier?
        modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
        } catch (e: MlKitException) {
            Log.e(
                TAG,
                "Failed to parse language '$languageTag'"
            )
            return ""
        } ?: return "No model for language: $languageTag"

        // 初始化模型和识别器。
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model!!).build()
        )
        Log.i(
            TAG, "Model set for language '$languageTag' ('$modelIdentifier.languageTag')."
        )
        return "Model set for language: $languageTag"
    }

    // 原来的写法
    // region 记录笔画并build ink对象

    fun record(x: Float, y: Float) {
        val t = System.currentTimeMillis() // 时间t是可以省略的
        val point = Ink.Point.create(x, y, t)
        this.strokeBuilder.addPoint(point)
    }

    fun finishRecording() {
        inkBuilder.addStroke(this.strokeBuilder.build())
    }


    // endregion

    fun checkIsModelDownloaded(): Task<Boolean?> {
        // 检查当前使用的数字墨水识别模型是否已下载。
        return remoteModelManager.isModelDownloaded(model!!)
    }


    companion object {
        private const val TAG = "LK.Provider"
    }

}