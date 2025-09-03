package com.rugiserl.paindiary

import androidx.compose.ui.graphics.Color

fun mixColors(c1: Color, c2: Color, t: Float): Color {
    assert(t>=0.0f && t<= 1.0f)
    return Color(((t*c1.red + (1-t)*c2.red)*255).toInt(), ((t*c1.green + (1-t)*c2.green)*255).toInt(), ((t*c1.blue + (1-t)*c2.blue)*255).toInt(), ((t*c1.alpha + (1-t)*c2.alpha)*255).toInt())
}
fun getGreenRedGradient(painLevel: Float): Color {
    return mixColors(Color.Red, Color.Green, painLevel/10.0f)
}
