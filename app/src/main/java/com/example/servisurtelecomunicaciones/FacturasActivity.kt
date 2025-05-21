package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class FacturasActivity : AppCompatActivity() {

    companion object {
        private const val BASE_URL = "https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com"
    }

    private lateinit var dbRef: DatabaseReference
    private val lista = mutableListOf<Factura>()
    private lateinit var adapter: FacturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturas)

        dbRef = FirebaseDatabase.getInstance(BASE_URL).getReference("facturas")
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvFacturas)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = FacturaAdapter(
            items = lista,
            onEstadoChanged = { f -> dbRef.child(f.id).child("estado").setValue(f.estado) },
            onDelete       = { f -> dbRef.child(f.id).removeValue() },
            onEdit         = { f -> showFormDialog(orig = f, isEdit = true, numeroAuto = f.numero) }
        )
        rv.adapter = adapter

        // Carga y filtro
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val filtradas = snapshot.children
                    .filter { it.hasChildren() }
                    .mapNotNull { it.getValue(Factura::class.java) }
                    .filter    { it.usuarioId == uid }
                    .sortedByDescending { it.fecha }

                lista.clear()
                lista.addAll(filtradas)
                adapter.notifyDataSetChanged()

                if (filtradas.isEmpty()) toastConLogo("No hay facturas para mostrar")
            }
            override fun onCancelled(error: DatabaseError) {
                toastConLogo("Error al leer facturas: ${error.message}")
            }
        })

        // Crear nueva factura, numeración basada en lista.size
        findViewById<FloatingActionButton>(R.id.fabAddFactura).setOnClickListener {
            val next = "F-" + (lista.size + 1).toString().padStart(5, '0')
            showFormDialog(orig = null, isEdit = false, numeroAuto = next)
        }
    }

    private fun showFormDialog(orig: Factura?, isEdit: Boolean, numeroAuto: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_factura_form, null)
        val tvHeader        = view.findViewById<TextView>(R.id.tvFormHeader)
        val etNumero        = view.findViewById<TextInputEditText>(R.id.etNumero)
        val etClienteNom    = view.findViewById<TextInputEditText>(R.id.etClienteNombre)
        val etClienteNIF    = view.findViewById<TextInputEditText>(R.id.etClienteNIF)
        val etClienteDir    = view.findViewById<TextInputEditText>(R.id.etClienteDireccion)
        val etBase          = view.findViewById<TextInputEditText>(R.id.etBase)
        val etIvaPct        = view.findViewById<TextInputEditText>(R.id.etIvaPct)
        val etIvaCu         = view.findViewById<TextInputEditText>(R.id.etIvaCuota)
        val etTotal         = view.findViewById<TextInputEditText>(R.id.etTotal)
        val etPago          = view.findViewById<TextInputEditText>(R.id.etFormaPago)
        val etObservaciones = view.findViewById<TextInputEditText>(R.id.etObservaciones)

        etIvaCu.isEnabled = false
        etTotal.isEnabled = false

        if (isEdit) {
            tvHeader.text = "Editar Factura"
            orig!!.let { f ->
                etNumero.setText(f.numero)
                etClienteNom.setText(f.clienteNombre)
                etClienteNIF.setText(f.clienteNIF)
                etClienteDir.setText(f.clienteDireccion)
                etBase.setText(f.baseImponible.toString())
                etIvaPct.setText(f.tipoIva.toString())
                etIvaCu.setText(f.cuotaIva.toString())
                etTotal.setText(f.total.toString())
                etPago.setText(f.formaPago)
                etObservaciones.setText(f.observaciones)
            }
        } else {
            tvHeader.text = "Crear Factura"
            etNumero.setText(numeroAuto)
            etIvaPct.setText("21.0")
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val base = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val pct  = etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu = base * pct / 100.0
                etIvaCu.setText("%.2f".format(ivaCu))
                etTotal.setText("%.2f".format(base + ivaCu))
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etBase.addTextChangedListener(watcher)
        etIvaPct.addTextChangedListener(watcher)
        if (isEdit) watcher.afterTextChanged(null)

        AlertDialog.Builder(this)
            .setView(view)
            .setPositiveButton(if (isEdit) "Guardar" else "Crear") { dlg, _ ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton
                val num = etNumero.text.toString().trim()
                val cli = etClienteNom.text.toString().trim()
                val nif = etClienteNIF.text.toString().trim()
                val dir = etClienteDir.text.toString().trim()
                val base = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val pct  = etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu = etIvaCu.text.toString().toDoubleOrNull() ?: 0.0
                val tot   = etTotal.text.toString().toDoubleOrNull() ?: 0.0
                val pago  = etPago.text.toString().trim()
                val obs   = etObservaciones.text.toString().trim()

                if (num.isEmpty() || cli.isEmpty()) {
                    toastConLogo("Número y cliente obligatorios")
                } else {
                    if (!isEdit) {
                        val ref = dbRef.push()
                        val f = Factura(
                            id               = ref.key ?: "",
                            numero           = num,
                            fecha            = System.currentTimeMillis(),
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            baseImponible    = base,
                            tipoIva          = pct,
                            cuotaIva         = ivaCu,
                            total            = tot,
                            formaPago        = pago,
                            observaciones    = obs,
                            estado           = "pendiente",
                            usuarioId        = uid
                        )
                        ref.setValue(f)
                        toastConLogo("Factura creada correctamente")
                    } else {
                        val updated = orig!!.copy(
                            numero           = num,
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            baseImponible    = base,
                            tipoIva          = pct,
                            cuotaIva         = ivaCu,
                            total            = tot,
                            formaPago        = pago,
                            observaciones    = obs
                        )
                        dbRef.child(updated.id).setValue(updated)
                        toastConLogo("Factura actualizada correctamente")
                    }
                    dlg.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(
            R.layout.toast_custom_logo,
            findViewById(android.R.id.content),
            false
        )
        layout.findViewById<TextView>(R.id.toastText).text = msg
        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(android.view.Gravity.CENTER, 0, 250)
            show()
        }
    }
}
