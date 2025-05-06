package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class PresupuestosActivity : AppCompatActivity() {

    private lateinit var dbRef: DatabaseReference
    private val lista = mutableListOf<Presupuesto>()
    private lateinit var adapter: PresupuestoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presupuestos)

        // Inicializamos RTDB
        dbRef = FirebaseDatabase
            .getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")
            .getReference("presupuestos")

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvPresupuestos)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = PresupuestoAdapter(
            items           = lista,
            onEstadoChanged = { p -> dbRef.child(p.id).child("estado").setValue(p.estado) },
            onDelete        = { p -> dbRef.child(p.id).removeValue() },
            onEdit          = { p -> showFormDialog(orig = p, isEdit = true) }
        )
        rv.adapter = adapter

        // Leer presupuestos de Firebase (filtramos valores primitivos)
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val nuevas = snap.children
                    .filter { it.hasChildren() }
                    .mapNotNull { it.getValue(Presupuesto::class.java) }
                    .sortedByDescending { it.fecha }
                lista.clear()
                lista.addAll(nuevas)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(err: DatabaseError) {
                Toast.makeText(this@PresupuestosActivity,
                    "Error: ${err.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Botón “+” crear nuevo presupuesto
        findViewById<FloatingActionButton>(R.id.fabAddPresupuesto)
            .setOnClickListener { showFormDialog(orig = null, isEdit = false) }
    }

    /** Igual que Facturas: abre el formulario de creación/edición */
    private fun showFormDialog(orig: Presupuesto?, isEdit: Boolean) {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.dialog_presupuesto_form, null)

        val tvHeader = v.findViewById<TextView>(R.id.tvFormHeader)
        val etNumero = v.findViewById<TextInputEditText>(R.id.etNumero)
        val etCliente= v.findViewById<TextInputEditText>(R.id.etCliente)
        val etNIF    = v.findViewById<TextInputEditText>(R.id.etNIF)
        val etDir    = v.findViewById<TextInputEditText>(R.id.etDireccion)
        val etTipo   = v.findViewById<TextInputEditText>(R.id.etTipo)
        val etPago   = v.findViewById<TextInputEditText>(R.id.etPago)
        val etObs    = v.findViewById<TextInputEditText>(R.id.etObservaciones)

        if (isEdit) {
            tvHeader.text = "Editar Presupuesto"
            orig!!.let { p ->
                etNumero.setText(p.numero)
                etCliente.setText(p.clienteNombre)
                etNIF.setText(p.clienteNIF)
                etDir.setText(p.clienteDireccion)
                etTipo.setText(p.tipoServicio)
                etPago.setText(p.formaPago)
                etObs.setText(p.observaciones)
            }
        } else {
            tvHeader.text = "Crear Presupuesto"
        }

        AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton(if (isEdit) "Guardar" else "Crear") { dlg,_ ->
                val num  = etNumero.text.toString().trim()
                val cli  = etCliente.text.toString().trim()
                val nif  = etNIF.text.toString().trim()
                val dir  = etDir.text.toString().trim()
                val tipo = etTipo.text.toString().trim()
                val pago = etPago.text.toString().trim()
                val obs  = etObs.text.toString().trim()

                if (num.isEmpty() || cli.isEmpty()) {
                    Toast.makeText(this,
                        "Número y cliente obligatorios", Toast.LENGTH_SHORT).show()
                } else {
                    if (isEdit) {
                        val p = orig!!.copy(
                            numero           = num,
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            tipoServicio     = tipo,
                            formaPago        = pago,
                            observaciones    = obs
                        )
                        dbRef.child(p.id).setValue(p)
                    } else {
                        val ref = dbRef.push()
                        val p = Presupuesto(
                            id               = ref.key ?: "",
                            numero           = num,
                            fecha            = System.currentTimeMillis(),
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            tipoServicio     = tipo,
                            formaPago        = pago,
                            observaciones    = obs,
                            estado           = "pendiente"
                        )
                        ref.setValue(p)
                    }
                    dlg.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
