package com.example.servisurtelecomunicaciones

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class EditarPerfilActivity : AppCompatActivity() {

    companion object {
        private const val PREFS       = "prefs"
        private const val KEY_NAME    = "user_name"
        private const val KEY_AVATAR  = "user_avatar"
        private const val RC_PICK_PHOTO = 1001
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var etNombre: TextInputEditText
    private lateinit var btnGuardar: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        ivAvatar   = findViewById(R.id.ivAvatar)
        etNombre   = findViewById(R.id.etNombre)
        btnGuardar = findViewById(R.id.btnGuardarPerfil)

        loadUserProfile()

        ivAvatar.setOnClickListener { onChangeAvatar(it) }
        btnGuardar.setOnClickListener {
            saveProfileChanges()
            finish() // vuelve al Home
        }
    }

    private fun loadUserProfile() {
        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        // Carga nombre (o "Admin" por defecto)
        etNombre.setText(prefs.getString(KEY_NAME, "Admin"))
        // Carga avatar si ya guardaste una URI
        prefs.getString(KEY_AVATAR, null)?.let { uriString ->
            ivAvatar.setImageURI(Uri.parse(uriString))
        }
    }

    /** Lanza la galería para elegir foto */
    fun onChangeAvatar(view: View) {
        val pick = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            .setType("image/*")
        startActivityForResult(pick, RC_PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uri ->
                // Muestra la imagen seleccionada
                ivAvatar.setImageURI(uri)
                // Guarda la URI en prefs
                getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(KEY_AVATAR, uri.toString())
                    .apply()
            }
        }
    }

    private fun saveProfileChanges() {
        val nombre = etNombre.text.toString().trim()
        if (nombre.isEmpty()) {
            etNombre.error = "Requerido"
            return
        }
        getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_NAME, nombre)
            .apply()
        Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
    }
}
