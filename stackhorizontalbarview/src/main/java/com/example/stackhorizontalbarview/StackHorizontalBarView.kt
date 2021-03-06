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
