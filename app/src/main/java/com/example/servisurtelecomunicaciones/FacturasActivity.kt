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
import com.google.firebase.database.*
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class FacturasActivity : AppCompatActivity() {

    companion object {
        private const val EMAIL_ORIGEN = "servisuravisosapp76@gmail.com"
        private const val PASSWORD_EMAIL = "bnlwvwgrxbpfnqma"
        private const val EMAIL_DESTINO = "telecomunicacionesservisur8@gmail.com"
    }

    private lateinit var dbRef: DatabaseReference
    private val lista = mutableListOf<Factura>()
    private lateinit var adapter: FacturaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facturas)

        val base = "https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com"
        dbRef = FirebaseDatabase.getInstance(base).getReference("facturas")

        val rv = findViewById<RecyclerView>(R.id.rvFacturas)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = FacturaAdapter(
            items = lista,
            onEstadoChanged = { f -> dbRef.child(f.id).child("estado").setValue(f.estado) },
            onDelete = { f -> dbRef.child(f.id).removeValue() },
            onEdit = { f -> showFormDialog(orig = f, isEdit = true, numeroAuto = f.numero) }
        )
        rv.adapter = adapter

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val nuevas = snap.children
                    .mapNotNull { it.getValue(Factura::class.java) }
                    .sortedByDescending { it.fecha }
                lista.clear()
                lista.addAll(nuevas)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(err: DatabaseError) {
                toastConLogo("Error: ${err.message}")
            }
        })

        findViewById<FloatingActionButton>(R.id.fabAddFactura).setOnClickListener {
            dbRef.get().addOnSuccessListener { snap ->
                val next = "F-" + (snap.childrenCount + 1).toString().padStart(5, '0')
                showFormDialog(orig = null, isEdit = false, numeroAuto = next)
            }
        }
    }

    private fun showFormDialog(orig: Factura?, isEdit: Boolean, numeroAuto: String) {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_factura_form, null)

        val tvHeader = v.findViewById<TextView>(R.id.tvFormHeader)
        val etNumero = v.findViewById<TextInputEditText>(R.id.etNumero)
        val etCliente = v.findViewById<TextInputEditText>(R.id.etClienteNombre)
        val etNIF = v.findViewById<TextInputEditText>(R.id.etClienteNIF)
        val etDir = v.findViewById<TextInputEditText>(R.id.etClienteDireccion)
        val etBase = v.findViewById<TextInputEditText>(R.id.etBase)
        val etIvaPct = v.findViewById<TextInputEditText>(R.id.etIvaPct)
        val etIvaCu = v.findViewById<TextInputEditText>(R.id.etIvaCuota)
        val etTotal = v.findViewById<TextInputEditText>(R.id.etTotal)
        val etPago = v.findViewById<TextInputEditText>(R.id.etFormaPago)
        val etObs = v.findViewById<TextInputEditText>(R.id.etObservaciones)

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
            etNumero.setText(numeroAuto)
            etIvaPct.setText("21.0")
        }

        etIvaCu.isEnabled = false
        etTotal.isEnabled = false

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val base = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val ivaPct = etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu = base * ivaPct / 100
                etIvaCu.setText(String.format(Locale.getDefault(), "%.2f", ivaCu))
                etTotal.setText(String.format(Locale.getDefault(), "%.2f", base + ivaCu))
            }

            override fun beforeTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        }

        etBase.addTextChangedListener(watcher)
        etIvaPct.addTextChangedListener(watcher)
        if (isEdit) watcher.afterTextChanged(null)

        AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton(if (isEdit) "Guardar" else "Crear") { dlg, _ ->
                val num = etNumero.text.toString().trim()
                val cli = etCliente.text.toString().trim()
                val nif = etNIF.text.toString().trim()
                val dir = etDir.text.toString().trim()
                val base = etBase.text.toString().toDoubleOrNull() ?: 0.0
                val ivaPct = etIvaPct.text.toString().toDoubleOrNull() ?: 0.0
                val ivaCu = etIvaCu.text.toString().toDoubleOrNull() ?: 0.0
                val tot = etTotal.text.toString().toDoubleOrNull() ?: 0.0
                val pago = etPago.text.toString().trim()
                val obs = etObs.text.toString().trim()

                if (num.isEmpty() || cli.isEmpty()) {
                    toastConLogo("Número y cliente obligatorios")
                } else {
                    if (!isEdit) {
                        val ref = dbRef.push()
                        val f = Factura(
                            id = ref.key ?: "",
                            numero = num,
                            fecha = System.currentTimeMillis(),
                            clienteNombre = cli,
                            clienteNIF = nif,
                            clienteDireccion = dir,
                            baseImponible = base,
                            tipoIva = ivaPct,
                            cuotaIva = ivaCu,
                            total = tot,
                            formaPago = pago,
                            observaciones = obs,
                            estado = "pendiente"
                        )
                        ref.setValue(f)

                        Thread {
                            try {
                                val props = Properties().apply {
                                    put("mail.smtp.auth", "true")
                                    put("mail.smtp.starttls.enable", "true")
                                    put("mail.smtp.host", "smtp.gmail.com")
                                    put("mail.smtp.port", "587")
                                }
                                val session = Session.getInstance(props, object : Authenticator() {
                                    override fun getPasswordAuthentication() =
                                        PasswordAuthentication(EMAIL_ORIGEN, PASSWORD_EMAIL)
                                })
                                val msg = MimeMessage(session).apply {
                                    setFrom(InternetAddress(EMAIL_ORIGEN))
                                    setRecipients(
                                        Message.RecipientType.TO,
                                        InternetAddress.parse(EMAIL_DESTINO)
                                    )
                                    subject = "Nueva factura #$num"
                                    setText("Factura #$num por ${"%.2f".format(tot)} € creada para $cli.")
                                }
                                Transport.send(msg)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.start()
                        toastConLogo("Factura creada correctamente")
                    } else {
                        val upd = orig!!.copy(
                            numero = num,
                            clienteNombre = cli,
                            clienteNIF = nif,
                            clienteDireccion = dir,
                            baseImponible = base,
                            tipoIva = ivaPct,
                            cuotaIva = ivaCu,
                            total = tot,
                            formaPago = pago,
                            observaciones = obs
                        )
                        dbRef.child(upd.id).setValue(upd)
                        toastConLogo("Factura actualizada correctamente")
                    }
                    dlg.dismiss()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toastConLogo(msg: String) {
        val layout = layoutInflater.inflate(R.layout.toast_custom_logo, findViewById(android.R.id.content), false)
        layout.findViewById<TextView>(R.id.toastText).text = msg

        Toast(applicationContext).apply {
            duration = Toast.LENGTH_SHORT
            view = layout
            setGravity(android.view.Gravity.CENTER, 0, 250)
            show()
        }
    }
}
