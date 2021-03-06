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

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {


        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class SHBNode(var i : Int, val state : State = State()) {

        private var next : SHBNode? = null
        private var prev : SHBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = SHBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawSHBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : SHBNode {
            var curr : SHBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class StackHorizontalBar(var i : Int) {

        private var curr : SHBNode = SHBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : StackHorizontalBarView) {

        private val animator : Animator = Animator(view)
        private val shb : StackHorizontalBar = StackHorizontalBar(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            shb.draw(canvas, paint)
            animator.animate {
                shb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            shb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : StackHorizontalBarView {
            val view : StackHorizontalBarView = StackHorizontalBarView(activity)
            activity.setContentView(view)
            return view
        }
    }
}