package com.linkdom.drawspace2.ui.component.space

import com.linkdom.drawspace2.ui.theme.DrawColors
import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInteropFilter

// 表示画图事件的类，包括按下、移动和抬起三种事件
sealed class DrawEvent {
    data class Down(val x: Float, val y: Float): DrawEvent()
    data class Move(val x: Float, val y: Float): DrawEvent()
    object Up: DrawEvent()
}

// 表示绘制曲线的类，包括移动到指定点和曲线到指定点两种操作
private sealed class DrawPath {
    data class MoveTo(val x: Float, val y: Float): DrawPath()
    data class CurveTo(val prevX: Float, val prevY: Float, val x: Float, val y: Float): DrawPath()
}

// 绘图区域的 Composable 函数，用于绘制用户手指在屏幕上的画笔轨迹
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawSpace(
    modifier: Modifier = Modifier,  // 修饰符，用于设置绘图区域的大小、位置等属性
    reset: Boolean = false,  // 是否需要重置绘图区域（清空画布）
    onDrawEvent: (DrawEvent) -> Unit,  // 回调函数，用于通知外部代码绘图事件的发生
) {

    val path = remember { Path() }  // 用于保存画笔轨迹的路径
    var drawPath by remember { mutableStateOf<DrawPath?>(null) }  // 用于保存绘制曲线的操作
    var isDrawing by remember { mutableStateOf(false) }  // 用于保存当前是否正在绘制

    if (reset) {  // 如果需要重置绘图区域
        drawPath = null  // 清空当前的绘制操作
        path.reset()  // 清空画笔轨迹
    }

    Canvas(
        modifier = modifier  // 设置绘图区域的修饰符
            .background(Color.White)  // 设置绘图区域的背景色为白色
            .pointerInteropFilter { event ->  // 添加交互事件监听器
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {  // 当按下手指时
                        drawPath = DrawPath.MoveTo(event.x, event.y)  // 记录下移动到的位置
                        isDrawing = true  // 设置当前正在绘制
                        onDrawEvent.invoke(DrawEvent.Down(event.x, event.y))  // 触发按下事件的回调函数
                    }
                    MotionEvent.ACTION_MOVE -> {  // 当手指移动时
                        val prevX = when (drawPath) {  // 获取上一个点的横坐标
                            is DrawPath.MoveTo -> (drawPath as DrawPath.MoveTo).x
                            is DrawPath.CurveTo -> (drawPath as DrawPath.CurveTo).x
                            else -> 0f
                        }

                        val prevY = when (drawPath) {  // 获取上一个点的纵坐标
                            is DrawPath.MoveTo -> (drawPath as DrawPath.MoveTo).y
                            is DrawPath.CurveTo -> (drawPath as DrawPath.CurveTo).y
                            else -> 0f
                        }
                        // 如果移出了drawSpace的区域需要恢复原始状态
                        drawPath = if (event.x < 0f || event.y < 0f){
                            DrawPath.MoveTo(prevX, prevY)
                        }else{
                            if (isDrawing) {
                                DrawPath.CurveTo(prevX, prevY, event.x, event.y)  // 记录下曲线到的位置
                            } else {
                                DrawPath.MoveTo(event.x, event.y)  // 记录下移动到的位置
                            }
                        }
                        onDrawEvent.invoke(DrawEvent.Move(event.x, event.y))  // 触发移动事件的回调函数
                    }
                    MotionEvent.ACTION_UP -> {
                        // 当抬起手指时
                        isDrawing = false  // 设置当前不再绘制
                        onDrawEvent.invoke(DrawEvent.Up)  // 触发抬起事件的回调函数
                    }
                    else -> { /* do nothing */ }
                }
                // 输出x,y坐标的方便调试
//                println("x: ${event.x}, y: ${event.y}")
                return@pointerInteropFilter true  // 返回 true 表示此事件已被处理
            }
    ) {
        if (drawPath == null)  // 如果当前没有绘制操作，则直接返回
            return@Canvas

        when (drawPath) {
            is DrawPath.MoveTo -> {  // 如果是移动到指定点
                val (x, y) = drawPath as DrawPath.MoveTo
                path.moveTo(x, y)  // 移动到指定点
            }

            is DrawPath.CurveTo -> {  // 如果是曲线到指定点
                val (prevX, prevY, x, y) = drawPath as DrawPath.CurveTo
//                path.lineTo(x, y)  // 绘制直线
                path.quadraticBezierTo(prevX, prevY, (x + prevX)/2, (y + prevY)/2)  // 绘制二次贝塞尔曲线
//                path.cubicTo(prevX, prevY, (x + prevX)/2, (y + prevY)/2, x, y)  // 绘制三次贝塞尔曲线
            }

            else -> {}  // 其他情况下不需要做任何操作
        }

        drawPath(  // 绘制当前的画笔轨迹
            path = path,
            color = DrawColors.Stroke,  // 绘制轨迹的颜色
            style = Stroke(
                width = 8f,
                cap = StrokeCap.Round, // 绘制轨迹的端点样式(笔触)
                join = StrokeJoin.Round, // 设置画笔连接处为圆角
            )  // 绘制轨迹的样式
        )
    }
}