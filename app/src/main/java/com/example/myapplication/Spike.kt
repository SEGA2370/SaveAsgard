package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlin.random.Random

class Spike(context: Context) {
    private val spike = arrayOfNulls<Bitmap>(3)
    var spikeFrame = 0
    var spikeX: Int = 0
    var spikeY: Int = 0
    var spikeVelocity: Int = 0
    private val random: Random

    init {
        spike[0] = BitmapFactory.decodeResource(context.resources, R.drawable.spike0)
        spike[1] = BitmapFactory.decodeResource(context.resources, R.drawable.spike1)
        spike[2] = BitmapFactory.decodeResource(context.resources, R.drawable.spike2)

        val scaleFactor = 10 // Adjust this factor as needed
        spike[0] = spike[0]?.let { Bitmap.createScaledBitmap(it, it.width / scaleFactor, it.height / scaleFactor, false) }
        spike[1] = spike[1]?.let { Bitmap.createScaledBitmap(it, it.width / scaleFactor, it.height / scaleFactor, false) }
        spike[2] = spike[2]?.let { Bitmap.createScaledBitmap(it, it.width / scaleFactor, it.height / scaleFactor, false) }

        random = Random
        resetPosition()
    }

    fun getSpikeFrame(): Bitmap? {
        return spike[spikeFrame]
    }

    fun getSpikeWidth(): Int {
        return spike[0]?.width ?: 0
    }

    fun getSpikeHeight(): Int {
        return spike[0]?.height ?: 0
    }

    fun resetPosition() {
        spikeX = random.nextInt(GameView.dWidth - getSpikeWidth())
        spikeY = -200 - random.nextInt(600)
        spikeVelocity = 35 + random.nextInt(16)
    }
}
