package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    // Instancia de Firebase para autenticación
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Mostrar barra de navegación (IMPORTANTE)
        window.decorView.systemUiVisibility = (
                android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or android.view.View.SYSTEM_UI_FLAG_VISIBLE
                )

        // ✅ Forzar cierre de sesión para pruebas (Solo mientras depuramos)
        FirebaseAuth.getInstance().signOut()

        // ✅ Inicializar Firebase
        FirebaseApp.initializeApp(this)

        auth = FirebaseAuth.getInstance()

        // ✅ Añadir logs para comprobar estado de autenticación
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d("AUTH", "Usuario NO autenticado -> Mostrando pantalla de Login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            Log.d("AUTH", "Usuario autenticado -> Navegando a HomeActivity")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
    // Función para manejar el clic en el botón o imagen de una marca
    fun goToBrandInfo(view: View) {
        val intent = Intent(this, BrandInfoActivity::class.java)
        startActivity(intent)
    }
}
