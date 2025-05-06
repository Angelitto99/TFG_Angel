package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class IncidenciaDetailActivity : AppCompatActivity() {

    private lateinit var incidencia: Incidencia
    private lateinit var tvTitulo: TextView
    private lateinit var tvFecha: TextView
    private lateinit var tvUsuario: TextView
    private lateinit var tvTelefono: TextView
    private lateinit var tvUbicacion: TextView
    private lateinit var tvDescripcion: TextView
    private lateinit var tvEstado: TextView
    private lateinit var btnCambiarEstado: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencia_detail)

        incidencia = intent.getParcelableExtra("incidencia")!!

        // refs
        tvTitulo        = findViewById(R.id.tvDetailTitulo)
        tvFecha         = findViewById(R.id.tvDetailFecha)
        tvUsuario       = findViewById(R.id.tvDetailUsuario)
        tvTelefono      = findViewById(R.id.tvDetailTelefono)
        tvUbicacion     = findViewById(R.id.tvDetailUbicacion)
        tvDescripcion   = findViewById(R.id.tvDetailDescripcion)
        tvEstado        = findViewById(R.id.tvDetailEstado)
        btnCambiarEstado= findViewById(R.id.btnCambiarEstado)

        // poblar datos
        tvTitulo.text      = incidencia.nombre
        tvFecha.text       = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            .format(Date(incidencia.timestamp))
        tvUsuario.text     = incidencia.usuarioEmail
        tvTelefono.text    = incidencia.telefono
        tvUbicacion.text   = incidencia.ubicacion
        tvDescripcion.text = incidencia.descripcion
        tvEstado.text      = incidencia.estado.replaceFirstChar { it.uppercase() }

        // cambiar estado en Firebase
        btnCambiarEstado.setOnClickListener {
            val opciones = arrayOf("Abierta", "Pendiente", "Cerrada")
            val current  = opciones.indexOfFirst { it.equals(incidencia.estado, true) }
            AlertDialog.Builder(this)
                .setTitle("Cambiar estado")
                .setSingleChoiceItems(opciones, if (current>=0) current else 0) { dlg, which ->
                    incidencia.estado = opciones[which].lowercase()
                    // actualizar en RTDB
                    FirebaseDatabase
                        .getInstance()
                        .getReference("incidencias")
                        .child(incidencia.id)
                        .child("estado")
                        .setValue(incidencia.estado)
                    tvEstado.text = opciones[which]
                    Toast.makeText(this, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    dlg.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
