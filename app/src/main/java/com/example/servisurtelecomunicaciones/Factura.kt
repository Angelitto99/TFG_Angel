package com.example.servisurtelecomunicaciones

data class Factura(
    val id: String = "",
    val numero: String = "",
    val fecha: Long = System.currentTimeMillis(),   // timestamp
    val clienteNombre: String = "",
    val clienteNIF: String = "",
    val clienteDireccion: String = "",
    val lineas: List<LineaFactura> = emptyList(),
    val baseImponible: Double = 0.0,
    val tipoIva: Double = 21.0,
    val cuotaIva: Double = 0.0,
    val total: Double = 0.0,
    val formaPago: String = "",
    val observaciones: String = "",
    var estado: String = "pendiente"                // pendiente / pagada
)

data class LineaFactura(
    val descripcion: String = "",
    val cantidad: Int = 1,
    val precioUnitario: Double = 0.0,
    val descuento: Double = 0.0
)
