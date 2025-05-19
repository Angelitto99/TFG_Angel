package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class IncidenciasListActivity : AppCompatActivity() {

    companion object {
        private val ADMIN_EMAILS = listOf(
            "admin@servisur.com"
        )
    }

    private lateinit var adapter: IncidenciaAdapter
    private val lista = mutableListOf<Incidencia>()
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_incidencias_list)

        dbRef = FirebaseDatabase
            .getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")
            .getReference("incidencias")

        val rv = findViewById<RecyclerView>(R.id.rvIncidencias)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = IncidenciaAdapter(
            items = lista,
            onEstadoChanged = { inc ->
                dbRef.child(inc.id)
                    .child("estado")
                    .setValue(inc.estado)
            },
            onDelete = { inc ->
                dbRef.child(inc.id)
                    .removeValue { err, _ ->
                        if (err == null) {
                            toastConLogo("Incidencia borrada correctamente")
                        } else {
                            toastConLogo("Error al borrar: ${err.message}")
                        }
                    }
            }
        )
        rv.adapter = adapter

        cargarIncidencias()
    }

    private fun cargarIncidencias() {
        val me = FirebaseAuth.getInstance().currentUser?.email
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevas = snapshot.children
                    .mapNotNull { it.getValue(Incidencia::class.java) }
                    .filter { me != null && ADMIN_EMAILS.contains(me) }
                    .filter { ADMIN_EMAILS.contains(it.usuarioEmail) }
                    .sortedByDescending { it.timestamp }

                lista.clear()
                lista.addAll(nuevas)
                adapter.updateList(nuevas)
            }

            override fun onCancelled(error: DatabaseError) {
                toastConLogo("Error al leer incidencias: ${error.message}")
            }
        })
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
