package com.linkdom.drawspace2.utils

import android.util.Log
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionResult
import java.util.concurrent.atomic.AtomicBoolean

/** 异步执行以获取识别结果的任务 */
class RecognitionTask(private val recognizer: DigitalInkRecognizer?, private val ink: Ink) {
  private var currentResult: RecognizedInk? = null // 当前识别结果
  private var currentResultList: RecognizedInkList? = null // 当前识别结果候选列表
  private val cancelled: AtomicBoolean = AtomicBoolean(false) // 是否已取消识别的标志
  private val done: AtomicBoolean = AtomicBoolean(false) // 是否已完成识别的标志

  fun cancel() {
    cancelled.set(true) // 标记已取消识别
  }

  fun done(): Boolean {
    return done.get() // 返回是否已完成识别的标志
  }

  fun result(): RecognizedInk? {
    return currentResult // 返回当前最优识别结果
  }

  fun resultList(): RecognizedInkList? {
      return currentResultList // 返回当前识别结果候选列表
  }

  /** 存储墨水及其对应的识别文本的辅助类  */
  class RecognizedInk internal constructor(val text: String?)

  class RecognizedInkList internal constructor(val predictions: List<String>?)

  fun run(): Task<String?> {
    Log.i(TAG, "RecoTask.run")
    return recognizer!!
      .recognize(ink) // 调用识别器的 recognize() 方法执行识别
      .onSuccessTask(
        SuccessContinuation { result: RecognitionResult? ->
          if (cancelled.get() || result == null || result.candidates.isEmpty()) {
            // 如果已取消识别、识别结果为空或识别结果候选列表为空，则返回一个已完成的任务
            return@SuccessContinuation Tasks.forResult<String?>(null)
          }
          currentResult = RecognizedInk(
            result.candidates[0].text
          ) // 创建一个新的 RecognizedInk 对象，存储墨水及其对应的识别文本
          currentResultList = RecognizedInkList(
            result.candidates.map { it.text }
          ) // 创建一个新的 RecognizedInkList 对象，存储墨水及其对应的识别文本候选列表
          Log.i(
            TAG,
            "result: " + currentResult!!.text
          ) // 打印识别结果
          done.set(true) // 标记已完成识别
          return@SuccessContinuation Tasks.forResult<String?>(currentResult!!.text)
          // 返回一个已完成的任务，并携带识别文本
        }
      )
  }

  companion object {
    private const val TAG = "MLKD.RecognitionTask" // 日志标签
  }

}
