package com.example.servisurtelecomunicaciones

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Incidencia(
    val id: String = "",
    val usuarioEmail: String = "",
    val nombre: String = "",
    val telefono: String = "",
    val ubicacion: String = "",
    val descripcion: String = "",
    var estado: String = "abierta",
    val timestamp: Long = System.currentTimeMillis()
) : Parcelable
