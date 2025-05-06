package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class IncidenciasListActivity : AppCompatActivity() {

    companion object {
        // Correos de administrador autorizados
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

        // Inicializamos la referencia a la rama "incidencias"
        dbRef = FirebaseDatabase
            .getInstance("https://servisurtelecomunicacion-223b0-default-rtdb.firebaseio.com")
            .getReference("incidencias")

        // Configuramos RecyclerView
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
                            Toast.makeText(
                                this,
                                "Incidencia borrada correctamente",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Error al borrar: ${err.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        )
        rv.adapter = adapter

        // Cargamos únicamente las incidencias creadas por administradores,
        // y solo si el usuario autenticado también es admin.
        cargarIncidencias()
    }

    private fun cargarIncidencias() {
        val me = FirebaseAuth.getInstance().currentUser?.email
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nuevas = snapshot.children
                    .mapNotNull { it.getValue(Incidencia::class.java) }
                    // Muestro solo si yo soy admin…
                    .filter { me != null && ADMIN_EMAILS.contains(me) }
                    // …y la incidencia la creó un admin
                    .filter { ADMIN_EMAILS.contains(it.usuarioEmail) }
                    .sortedByDescending { it.timestamp }

                lista.clear()
                lista.addAll(nuevas)
                adapter.updateList(nuevas)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@IncidenciasListActivity,
                    "Error al leer incidencias: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
}
