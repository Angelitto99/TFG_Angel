package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        findViewById<Button>(R.id.btnSaveProfile).setOnClickListener {
            // TODO: guarda los cambios de perfil (p.ej. en Firebase/user prefs)
            Toast.makeText(this, "Perfil guardado (pendiente de implementación)", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
