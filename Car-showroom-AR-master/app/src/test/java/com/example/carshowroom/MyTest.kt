package com.example.carshowroom

import io.github.sceneview.gesture.RotateGestureDetector
import org.junit.Test

import java.io.File

import java.io.File

class MyTest {
    @Test
    fun testMethods() {
        val file = File("methods.txt")
        val returnType = RotateGestureDetector::class.java.methods.find { it.name == "getRotation" }?.returnType?.name
        file.writeText("getRotation return type: $returnType")
    }
}
