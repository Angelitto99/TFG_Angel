package com.example.servisurtelecomunicaciones

data class Presupuesto(
    var id: String = "",
    val numero: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val clienteNombre: String = "",
    val clienteNIF: String = "",
    val clienteDireccion: String = "",
    val tipoPresupuesto: String = "",
    val formaPago: String = "",
    val observaciones: String = "",
    var estado: String = "pendiente"
)