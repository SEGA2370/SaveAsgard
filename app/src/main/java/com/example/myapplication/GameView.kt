package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import java.util.*
import kotlin.collections.ArrayList

@SuppressLint("ClickableViewAccessibility")
class GameView(context: Context) : View(context) {
    private val background: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.background)
    private val ground: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.ground)
    private val rabbit: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.rabbit)
    private val rectBackground: Rect = Rect(0, 0, dWidth, dHeight)
    private val rectGround: Rect = Rect(0, dHeight - ground.height, dWidth, dHeight)
    private val textPaint = Paint()
    private val healthPaint = Paint()
    private val spikes: ArrayList<Spike> = ArrayList()
    private val explosions: ArrayList<Explosion> = ArrayList()
    private val random = Random()
    private var rabbitX: Float = (dWidth / 2 - rabbit.width / 2).toFloat()
    private var rabbitY: Float = (dHeight - ground.height - rabbit.height / 2).toFloat()
    private var oldX: Float = 0.0f
    private var oldRabbitX: Float = 0.0f

    private val handler = android.os.Handler()
    private val runnable = Runnable { invalidate() }
    private var points = 0
    private var life = 3

    companion object {
        var dWidth: Int = 0
        var dHeight: Int = 0
        private const val TEXT_SIZE = 120f
        private const val UPDATE_MILLIS: Long = 30
    }


    init {
        val display = (context as Activity).windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        dWidth = size.x
        dHeight = size.y

        textPaint.color = Color.rgb(255, 165, 0)
        textPaint.textSize = TEXT_SIZE
        textPaint.textAlign = Paint.Align.LEFT
        healthPaint.color = Color.GREEN

        for (i in 0 until 3) {
            val spike = Spike(context)
            spikes.add(spike)
        }

        setOnTouchListener { _, event ->
            handleTouch(event)
        }

        handler.postDelayed(runnable, UPDATE_MILLIS)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(background, null, rectBackground, null)
        canvas.drawBitmap(ground, null, rectGround, null)
        canvas.drawBitmap(rabbit, rabbitX, rabbitY, null)

        handleSpikes(canvas)
        handleCollisions()
        handleExplosions(canvas)

        drawHealthBar(canvas)
        drawPoints(canvas)

        handler.postDelayed(runnable, UPDATE_MILLIS)
    }

    private fun handleTouch(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        if (touchY >= rabbitY) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldRabbitX = rabbitX
                }
                MotionEvent.ACTION_MOVE -> {
                    val shift = oldX - touchX
                    val newRabbitX = oldRabbitX - shift
                    rabbitX = when {
                        newRabbitX <= 0 -> 0f
                        newRabbitX >= dWidth - rabbit.width -> (dWidth - rabbit.width).toFloat()
                        else -> newRabbitX
                    }
                }
            }
        }
        return true
    }

    private fun handleSpikes(canvas: Canvas) {
        for (i in 0 until spikes.size) {
            val spike = spikes[i]
            spike.getSpikeFrame()
                ?.let { canvas.drawBitmap(it, spike.spikeX.toFloat(), spike.spikeY.toFloat(), null) }
            spike.spikeFrame++
            if (spike.spikeFrame > 2) {
                spike.spikeFrame = 0
            }
            spike.spikeY += spike.spikeVelocity

            if (spike.spikeY + spike.getSpikeHeight() >= dHeight - ground.height) {
                points += 10
                val explosion = Explosion(context)
                explosion.explosionX = spike.spikeX
                explosion.explosionY = spike.spikeY
                explosions.add(explosion)
                spike.resetPosition()
            }
        }
    }

    private fun handleCollisions() {
        for (i in 0 until spikes.size) {
            val spike = spikes[i]
            if (spike.spikeX + spike.getSpikeWidth() >= rabbitX
                && spike.spikeX <= rabbitX + rabbit.width
                && spike.spikeY + spike.getSpikeHeight() >= rabbitY
                && spike.spikeY + spike.getSpikeHeight() <= rabbitY + rabbit.height
            ) {
                life--
                spike.resetPosition()
                if (life == 0) {
                    val intent = Intent(context, GameOver::class.java)
                    intent.putExtra("points", points)
                    context.startActivity(intent)
                    (context as Activity).finish()
                }
            }
        }
    }

    private fun handleExplosions(canvas: Canvas) {
        val iterator = explosions.iterator()
        while (iterator.hasNext()) {
            val explosion = iterator.next()
            explosion.getExplosion(explosion.explosionFrame)?.let {
                canvas.drawBitmap(
                    it,
                    explosion.explosionX.toFloat(),
                    explosion.explosionY.toFloat(),
                    null
                )
            }
            explosion.explosionFrame++
            if (explosion.explosionFrame > 3) {
                iterator.remove()
            }
        }
    }

    private fun drawHealthBar(canvas: Canvas) {
        healthPaint.color = when (life) {
            2 -> Color.YELLOW
            1 -> Color.RED
            else -> Color.GREEN
        }
        val healthBarWidth = 60 * life
        canvas.drawRect(dWidth - 200.toFloat(), 30.toFloat(), (dWidth - 200 + healthBarWidth).toFloat(), 80.toFloat(), healthPaint)
    }

    private fun drawPoints(canvas: Canvas) {
        canvas.drawText("$points", 20.toFloat(), TEXT_SIZE, textPaint)
    }
}
