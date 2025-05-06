// Utils.kt
package com.example.servisurtelecomunicaciones

import java.util.*

fun calculateNext10AMDelay(): Long {
    val now = Calendar.getInstance()
    val next = now.clone() as Calendar
    next.set(Calendar.HOUR_OF_DAY, 10)
    next.set(Calendar.MINUTE, 0)
    next.set(Calendar.SECOND, 0)
    if (next.before(now)) next.add(Calendar.DAY_OF_YEAR, 1)
    return next.timeInMillis - now.timeInMillis
}
