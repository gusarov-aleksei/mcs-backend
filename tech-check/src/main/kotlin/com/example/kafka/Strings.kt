package com.example.kafka

import java.text.SimpleDateFormat
import java.util.*

val dateFormat = SimpleDateFormat("YYYY-MM-dd HH:mm:ss.SSS", Locale.getDefault())

fun String.wrapWithDate() = "${dateFormat.format(Date())} $this"