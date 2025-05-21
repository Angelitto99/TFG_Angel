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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class PresupuestosActivity : AppCompatActivity() {

    companion object {
        private const val EMAIL_ORIGEN = "servisuravisosapp76@gmail.com"
        private const val PASSWORD_EMAIL = "bnlwvwgrxbpfnqma"
        private const val EMAIL_DESTINO = "telecomunicacionesservisur8@gmail.com"
    }

    private lateinit var dbRef: DatabaseReference
    private val lista = mutableListOf<Presupuesto>()
    private lateinit var adapter: PresupuestoAdapter
    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presupuestos)

        val base = "https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com"
        dbRef = FirebaseDatabase.getInstance(base).getReference("presupuestos")
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        val rv = findViewById<RecyclerView>(R.id.rvPresupuestos)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = PresupuestoAdapter(
            items = lista,
            onEstadoChanged = { p -> dbRef.child(p.id).child("estado").setValue(p.estado) },
            onDelete = { p -> dbRef.child(p.id).removeValue() },
            onEdit = { p -> showFormDialog(orig = p, isEdit = true, numeroAuto = p.numero) }
        )
        rv.adapter = adapter

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                val nuevas = snap.children
                    .mapNotNull { it.getValue(Presupuesto::class.java) }
                    .filter { it.usuarioId == uid }
                    .sortedByDescending { it.fecha }

                lista.clear()
                lista.addAll(nuevas)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(err: DatabaseError) {
                toastConLogo("Error: ${err.message}")
            }
        })

        findViewById<FloatingActionButton>(R.id.fabAddPresupuesto).setOnClickListener {
            dbRef.get().addOnSuccessListener { snap ->
                val count = snap.children.count { it.hasChildren() }
                val next = "P-" + (count + 1).toString().padStart(5, '0')
                showFormDialog(orig = null, isEdit = false, numeroAuto = next)
            }
        }
    }

    private fun showFormDialog(orig: Presupuesto?, isEdit: Boolean, numeroAuto: String) {
        val v = LayoutInflater.from(this).inflate(R.layout.dialog_presupuesto_form, null)

        val tvHeader = v.findViewById<TextView>(R.id.tvFormHeader)
        val etNumero = v.findViewById<TextInputEditText>(R.id.etNumero)
        val etCliente = v.findViewById<TextInputEditText>(R.id.etCliente)
        val etNIF = v.findViewById<TextInputEditText>(R.id.etNIF)
        val etDir = v.findViewById<TextInputEditText>(R.id.etDireccion)
        val etTipo = v.findViewById<TextInputEditText>(R.id.etTipo)
        val etPago = v.findViewById<TextInputEditText>(R.id.etPago)
        val etObs = v.findViewById<TextInputEditText>(R.id.etObservaciones)

        if (isEdit) {
            tvHeader.text = "Editar Presupuesto"
            orig!!.let { p ->
                etNumero.setText(p.numero)
                etCliente.setText(p.clienteNombre)
                etNIF.setText(p.clienteNIF)
                etDir.setText(p.clienteDireccion)
                etTipo.setText(p.tipoPresupuesto)
                etPago.setText(p.formaPago)
                etObs.setText(p.observaciones)
            }
        } else {
            tvHeader.text = "Crear Presupuesto"
            etNumero.setText(numeroAuto)
        }

        AlertDialog.Builder(this)
            .setView(v)
            .setPositiveButton(if (isEdit) "Guardar" else "Crear") { dlg, _ ->
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@setPositiveButton

                val num = etNumero.text.toString().trim()
                val cli = etCliente.text.toString().trim()
                val nif = etNIF.text.toString().trim()
                val dir = etDir.text.toString().trim()
                val tipo = etTipo.text.toString().trim()
                val pago = etPago.text.toString().trim()
                val obs = etObs.text.toString().trim()

                if (num.isEmpty() || cli.isEmpty()) {
                    toastConLogo("Número y cliente obligatorios")
                } else {
                    if (!isEdit) {
                        val ref = dbRef.push()
                        val p = Presupuesto(
                            id = ref.key ?: "",
                            numero = num,
                            fecha = System.currentTimeMillis(),
                            clienteNombre = cli,
                            clienteNIF = nif,
                            clienteDireccion = dir,
                            tipoPresupuesto = tipo,
                            formaPago = pago,
                            observaciones = obs,
                            estado = "pendiente",
                            usuarioId = uid
                        )
                        ref.setValue(p)

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
                                    subject = "Nuevo presupuesto #$num"
                                    setText("Presupuesto #$num solicitado por $cli.")
                                }
                                Transport.send(msg)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }.start()

                        toastConLogo("Presupuesto creado y enviado correctamente")
                    } else {
                        val p = orig!!.copy(
                            numero = num,
                            clienteNombre = cli,
                            clienteNIF = nif,
                            clienteDireccion = dir,
                            tipoPresupuesto = tipo,
                            formaPago = pago,
                            observaciones = obs
                        )
                        dbRef.child(p.id).setValue(p)
                        toastConLogo("Presupuesto actualizado correctamente")
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
