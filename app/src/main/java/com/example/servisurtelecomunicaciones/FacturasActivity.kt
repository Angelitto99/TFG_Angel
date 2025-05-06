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
        private const val EMAIL_ORIGEN   = "servisuravisosapp76@gmail.com"
        private const val PASSWORD_EMAIL = "bnlwvwgrxbpfnqma"
        private const val EMAIL_DESTINO  = "telecomunicacionesservisur8@gmail.com"
    }

    private lateinit var dbRef: DatabaseReference
    private val lista = mutableListOf<Factura>()
    private lateinit var adapter: FacturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturas)

        // Inicializamos RTDB
        dbRef = FirebaseDatabase
            .getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")
            .getReference("facturas")

        // RecyclerView
        val rv = findViewById<RecyclerView>(R.id.rvFacturas)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = FacturaAdapter(
            items           = lista,
            onEstadoChanged = { f -> dbRef.child(f.id).child("estado").setValue(f.estado) },
            onDelete        = { f -> dbRef.child(f.id).removeValue() },
            onEdit          = { f -> showFormDialog(orig = f, isEdit = true) }
        )
        rv.adapter = adapter

        // Leer facturas de Firebase
        dbRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val nuevas = snap.children
                    .mapNotNull { it.getValue(Factura::class.java) }
                    .sortedByDescending { it.fecha }
                lista.clear()
                lista.addAll(nuevas)
                adapter.notifyDataSetChanged()
            }
            override fun onCancelled(err: DatabaseError) {
                Toast.makeText(
                    this@FacturasActivity,
                    "Error: ${err.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        // Botón “+” crear nueva factura
        findViewById<FloatingActionButton>(R.id.fabAddFactura)
            .setOnClickListener { showFormDialog(orig = null, isEdit = false) }
    }

    private fun showFormDialog(orig: Factura?, isEdit: Boolean) {
        val v = LayoutInflater.from(this)
            .inflate(R.layout.dialog_factura_form, null)

        // Referencias a los campos
        val tvHeader   = v.findViewById<TextView>(R.id.tvFormHeader)
        val etNumero   = v.findViewById<TextInputEditText>(R.id.etNumero)
        val etCliente  = v.findViewById<TextInputEditText>(R.id.etClienteNombre)
        val etNIF      = v.findViewById<TextInputEditText>(R.id.etClienteNIF)
        val etDir      = v.findViewById<TextInputEditText>(R.id.etClienteDireccion)
        val etBase     = v.findViewById<TextInputEditText>(R.id.etBase)
        val etIvaPct   = v.findViewById<TextInputEditText>(R.id.etIvaPct)
        val etIvaCu    = v.findViewById<TextInputEditText>(R.id.etIvaCuota)
        val etTotal    = v.findViewById<TextInputEditText>(R.id.etTotal)
        val etPago     = v.findViewById<TextInputEditText>(R.id.etFormaPago)
        val etObs      = v.findViewById<TextInputEditText>(R.id.etObservaciones)

        // Cabecera y valores por defecto
        if (isEdit) {
            tvHeader.text = "Editar Factura"
            orig!!.let { f ->
                etNumero.setText(f.numero)
                etCliente.setText(f.clienteNombre)
                etNIF.setText(f.clienteNIF)
                etDir.setText(f.clienteDireccion)
                etBase.setText(f.baseImponible.toString())
                etIvaPct.setText(f.tipoIva.toString())
                etPago.setText(f.formaPago)
                etObs.setText(f.observaciones)
            }
        } else {
            tvHeader.text = "Crear Factura"
            etIvaPct.setText("21.0")
        }

        // Desactivar edición de cuota y total
        etIvaCu.isEnabled = false
        etTotal.isEnabled = false

        // Recalcular IVA y total cada vez que cambie base o %IVA
        val watcher = object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val base   = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val ivaPct = etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu  = base * ivaPct / 100
                val tot    = base + ivaCu
                etIvaCu.setText(String.format(Locale.getDefault(), "%.2f", ivaCu))
                etTotal.setText(String.format(Locale.getDefault(), "%.2f", tot))
            }
        }
        etBase.addTextChangedListener(watcher)
        etIvaPct.addTextChangedListener(watcher)

        // Si es edición, rellena cuota/total inicial
        if (isEdit) watcher.afterTextChanged(null)

        AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton(if (isEdit) "Guardar" else "Crear") { dlg, _ ->
                val num  = etNumero.text.toString().trim()
                val cli  = etCliente.text.toString().trim()
                val nif  = etNIF.text.toString().trim()
                val dir  = etDir.text.toString().trim()
                val base = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val ivaPct= etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu = etIvaCu.text.toString().toDoubleOrNull() ?: 0.0
                val tot  = etTotal.text.toString().toDoubleOrNull() ?: 0.0
                val pago = etPago.text.toString().trim()
                val obs  = etObs.text.toString().trim()

                if (num.isEmpty() || cli.isEmpty()) {
                    Toast.makeText(this,
                        "Número y cliente obligatorios",
                        Toast.LENGTH_SHORT).show()
                } else {
                    if (isEdit) {
                        val upd = orig!!.copy(
                            numero           = num,
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            baseImponible    = base,
                            tipoIva          = ivaPct,
                            cuotaIva         = ivaCu,
                            total            = tot,
                            formaPago        = pago,
                            observaciones    = obs
                        )
                        dbRef.child(upd.id).setValue(upd)
                    } else {
                        val p = dbRef.push()
                        val f = Factura(
                            id               = p.key ?: "",
                            numero           = num,
                            fecha            = System.currentTimeMillis(),
                            clienteNombre    = cli,
                            clienteNIF       = nif,
                            clienteDireccion = dir,
                            baseImponible    = base,
                            tipoIva          = ivaPct,
                            cuotaIva         = ivaCu,
                            total            = tot,
                            formaPago        = pago,
                            observaciones    = obs,
                            estado           = "pendiente"
                        )
                        p.setValue(f)
                        // Envío de correo
                        Thread {
                            val admin = FirebaseAuth.getInstance().currentUser?.displayName
                                ?: FirebaseAuth.getInstance().currentUser?.email.orEmpty()
                            MailSender(EMAIL_ORIGEN, PASSWORD_EMAIL)
                                .sendMail(
                                    "Nueva factura - $admin",
                                    "Factura #$num por $tot € creada.",
                                    EMAIL_DESTINO
                                )
                        }.start()
                    }
                    dlg.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}
