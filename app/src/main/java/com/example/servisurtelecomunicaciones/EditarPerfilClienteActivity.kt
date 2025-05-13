// EditarPerfilClienteActivity.kt
package com.example.servisurtelecomunicaciones

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class EditarPerfilClienteActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_COMPANY = "user_company"
        private const val KEY_NOTIFS = "user_notifs"
        private const val RC_PICK_PHOTO = 1001
        private const val RC_NOTIF_PERM = 2001
    }

    private lateinit var ivAvatar: CircleImageView
    private lateinit var etNombre: TextInputEditText
    private lateinit var etTelefono: TextInputEditText
    private lateinit var etEmpresa: TextInputEditText
    private lateinit var swNotifs: Switch
    private lateinit var btnGuardar: Button
    private lateinit var btnCambiarPass: Button
    private lateinit var tvHolaUsuario: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_cliente)

        ivAvatar = findViewById(R.id.ivAvatar)
        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEmpresa = findViewById(R.id.etEmpresa)
        swNotifs = findViewById(R.id.swNotifs)
        btnGuardar = findViewById(R.id.btnGuardarPerfil)
        btnCambiarPass = findViewById(R.id.btnCambiarPass)
        tvHolaUsuario = findViewById(R.id.tvHolaUsuario)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)

        etNombre.setText(prefs.getString(KEY_NAME, ""))
        etTelefono.setText(prefs.getString(KEY_PHONE, ""))
        etEmpresa.setText(prefs.getString(KEY_COMPANY, ""))
        swNotifs.isChecked = prefs.getBoolean(KEY_NOTIFS, false)
        tvHolaUsuario.text = "Hola, ${prefs.getString(KEY_NAME, "Usuario")}"

        prefs.getString(KEY_AVATAR, null)?.let { path ->
            val file = File(path)
            if (file.exists()) {
                ivAvatar.setImageBitmap(BitmapFactory.decodeFile(file.absolutePath))
            } else {
                Log.d("EditarPerfil", "Avatar file not found at $path")
            }
        }

        ivAvatar.setOnClickListener { pickImage() }

        etTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    if (it.length > 9) {
                        etTelefono.setText(it.substring(0, 9))
                        etTelefono.setSelection(9)
                    }
                }
            }
        })

        btnGuardar.setOnClickListener {
            prefs.edit()
                .putString(KEY_NAME, etNombre.text.toString())
                .putString(KEY_PHONE, etTelefono.text.toString())
                .putString(KEY_COMPANY, etEmpresa.text.toString())
                .putBoolean(KEY_NOTIFS, swNotifs.isChecked)
                .putBoolean("is_admin", false)
                .apply()

            tvHolaUsuario.text = "Hola, ${etNombre.text}"

            if (swNotifs.isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        RC_NOTIF_PERM
                    )
                } else {
                    AlarmManagerCliente.programarAlarmaSemanal(this)
                }
            } else {
                AlarmManagerCliente.cancelarAlarma(this)
            }

            Toast.makeText(this, "Perfil guardado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCambiarPass.setOnClickListener {
            startActivity(Intent(this, CambiarPasswordActivity::class.java))
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, RC_PICK_PHOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                try {
                    val inputStream: InputStream? = contentResolver.openInputStream(uri)
                    val avatarFile = File(filesDir, "avatar_cliente.jpg")
                    val outputStream = FileOutputStream(avatarFile)
                    inputStream?.copyTo(outputStream)
                    outputStream.close()
                    inputStream?.close()

                    ivAvatar.setImageBitmap(BitmapFactory.decodeFile(avatarFile.absolutePath))

                    getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(KEY_AVATAR, avatarFile.absolutePath)
                        .apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_NOTIF_PERM && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            AlarmManagerCliente.programarAlarmaSemanal(this)
        }
    }
}
