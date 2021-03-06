package com.example.stackhorizontalbarview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Color
import android.graphics.RectF
import android.graphics.Canvas
import android.content.Context
import android.app.Activity

val colors : Array<Int> = arrayOf(
    "#1abc9c",
    "#3498db",
    "#f1c40f",
    "#8e44ad",
    "#2980b9"
).map {
    Color.parseColor(it)
}.toTypedArray()
val strokeFactor : Float = 0.02f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")
val bars : Int = 5
val scGap : Float = 0.02f / bars

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n))
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()
fun Float.aboveScale(i : Int, n : Int) : Float = ((i + 1)..(n-1)).map {
    divideScale(it, n)
}.reduce { a : Float, b : Float -> a + b }


fun Canvas.drawStackHorizontalBars(i : Int, scale : Float, w : Float, h : Float, paint : Paint) {
    val hGap : Float = (h - paint.strokeWidth) / bars
    paint.textSize = hGap / 3
    val sf : Float = scale.sinify()
    for (j in 0..(bars - 1)) {
        val sfj : Float = sf.divideScale(j, bars)
        val sfjc : Float = sf.aboveScale(j, bars)
        save()
        translate(0f, (h - hGap * (1 + j)) * sfjc)
        paint.color = colors[i]
        paint.style = Paint.Style.FILL
        drawRect(
            RectF(
                strokeFactor / 2,
                0f,
                strokeFactor / 2 + (w - strokeFactor) * sfj,
                hGap
            ),
            paint
        )
        paint.color = backColor
        drawText(
            "${i + 1}",
            w / 2 - paint.measureText("${i + 1}"),
            hGap / 2 - paint.textSize / 2,
            paint)
        paint.style = Paint.Style.STROKE
        drawRect(
            RectF(
                strokeFactor / 2,
                0f,
                strokeFactor / 2 + (w - strokeFactor) * sfj,
                hGap
            ),
            paint
        )
        restore()
    }
}

fun Canvas.drawSHBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawStackHorizontalBars(i, scale, w, h, paint)
}

class StackHorizontalBarView(ctx : Context) : View(ctx) {

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }
}