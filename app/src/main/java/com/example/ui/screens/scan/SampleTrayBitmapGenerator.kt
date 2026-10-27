package com.example.ui.screens.scan

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

object SampleTrayBitmapGenerator {
    fun generateTrayBitmap(trayId: String): Bitmap {
        val width = 800
        val height = 600
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Table marble / wood surface background
        paint.color = Color.rgb(235, 227, 215)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Subtle wood grain lines
        paint.color = Color.argb(25, 120, 80, 50)
        paint.strokeWidth = 3f
        for (i in 0..height step 40) {
            canvas.drawLine(0f, i.toFloat(), width.toFloat(), (i + 15).toFloat(), paint)
        }

        // 2. Tray / Luxury Box Base with shadow
        val trayRect = RectF(60f, 50f, 740f, 550f)
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawRoundRect(RectF(65f, 58f, 745f, 558f), 24f, 24f, paint)

        // Box border & velvet lining
        paint.color = Color.rgb(24, 10, 6) // Espresso box
        canvas.drawRoundRect(trayRect, 24f, 24f, paint)

        paint.color = Color.rgb(212, 175, 55) // Gold rim
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawRoundRect(trayRect, 24f, 24f, paint)
        paint.style = Paint.Style.FILL

        // Inner velvet gold-brown lining
        val innerRect = RectF(75f, 65f, 725f, 535f)
        paint.color = Color.rgb(45, 22, 14)
        canvas.drawRoundRect(innerRect, 18f, 18f, paint)

        // 3. Draw assorted handcrafted chocolates inside grid/cavities
        val rows = 3
        val cols = when (trayId) {
            "tray_signature_8" -> 3 // 8 pieces with one empty cavity
            "tray_boutique_16" -> 5 // 15-16 pieces
            else -> 4 // 12 pieces
        }

        val cellW = (innerRect.width() - 40f) / cols
        val cellH = (innerRect.height() - 40f) / rows

        val chocolateColors = listOf(
            Color.rgb(84, 46, 31),  // Hazelnut Truffle
            Color.rgb(34, 17, 11),  // Dark Ganache
            Color.rgb(200, 125, 50),// Salted Caramel
            Color.rgb(245, 236, 215),// White Passion
            Color.rgb(57, 107, 71), // Pistachio
            Color.rgb(90, 56, 37),  // Almond Rocher
            Color.rgb(184, 58, 75), // Ruby Heart
            Color.rgb(46, 26, 17)   // Ceylon Espresso
        )

        var itemIndex = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (trayId == "tray_signature_8" && r == 2 && c == 2) continue // 8 pieces

                val cx = innerRect.left + 20f + (c * cellW) + (cellW / 2)
                val cy = innerRect.top + 20f + (r * cellH) + (cellH / 2)
                val radius = (minOf(cellW, cellH) * 0.36f)

                // Cavity paper cup
                paint.color = Color.rgb(20, 8, 4)
                canvas.drawCircle(cx, cy, radius * 1.15f, paint)
                paint.color = Color.argb(60, 212, 175, 55)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                canvas.drawCircle(cx, cy, radius * 1.15f, paint)
                paint.style = Paint.Style.FILL

                // Chocolate piece
                val color = chocolateColors[itemIndex % chocolateColors.size]
                paint.color = color
                canvas.drawCircle(cx, cy, radius, paint)

                // Gold or white drizzle highlight
                paint.color = if (color == Color.rgb(245, 236, 215)) Color.rgb(229, 169, 16) else Color.rgb(249, 228, 150)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                canvas.drawLine(cx - radius * 0.5f, cy - radius * 0.3f, cx + radius * 0.5f, cy + radius * 0.3f, paint)
                canvas.drawLine(cx - radius * 0.4f, cy, cx + radius * 0.4f, cy, paint)
                paint.style = Paint.Style.FILL

                itemIndex++
            }
        }

        // Revello gold foil crest watermark on top left of tray
        paint.color = Color.rgb(212, 175, 55)
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("REVELLO SPECIALITY OUTLET", 95f, 95f, paint)

        return bitmap
    }
}
