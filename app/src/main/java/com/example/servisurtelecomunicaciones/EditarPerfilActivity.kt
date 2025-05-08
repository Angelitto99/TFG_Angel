package com.example.servisurtelecomunicaciones

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.WorkManager
import com.google.android.material.textfield.TextInputEditText
import de.hdodenhof.circleimageview.CircleImageView

class EditarPerfilActivity : AppCompatActivity() {

    companion object {
        private const val PREFS = "prefs"
        private const val KEY_NAME = "user_name"
        private const val KEY_AVATAR = "user_avatar"
        private const val KEY_NOTIFS = "user_notifs"
        private const val KEY_PHONE = "user_phone"
        private const val KEY_COMPANY = "user_company"
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        ivAvatar = findViewById(R.id.ivAvatar)
        etNombre = findViewById(R.id.etNombre)
        etTelefono = findViewById(R.id.etTelefono)
        etEmpresa = findViewById(R.id.etEmpresa)
        swNotifs = findViewById(R.id.swNotifs)
        btnGuardar = findViewById(R.id.btnGuardarPerfil)
        btnCambiarPass = findViewById(R.id.btnCambiarPass)

        val prefs = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val isAdmin = prefs.getBoolean("is_admin", false)
        val nameKey = if (isAdmin) "admin_name" else KEY_NAME
        val avatarKey = if (isAdmin) "admin_avatar" else KEY_AVATAR
        val phoneKey = if (isAdmin) "admin_phone" else KEY_PHONE
        val companyKey = if (isAdmin) "admin_company" else KEY_COMPANY

        etNombre.setText(prefs.getString(nameKey, ""))
        etTelefono.setText(prefs.getString(phoneKey, ""))
        etEmpresa.setText(prefs.getString(companyKey, ""))
        swNotifs.isChecked = prefs.getBoolean(KEY_NOTIFS, false)

        prefs.getString(avatarKey, null)?.let {
            ivAvatar.setImageURI(Uri.parse(it))
        }

        ivAvatar.setOnClickListener { pickImage() }

        etTelefono.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val maxLength = 9
                s?.let {
                    if (it.length > maxLength) {
                        etTelefono.setText(it.substring(0, maxLength))
                        etTelefono.setSelection(maxLength)
                    }
                }
            }
        })

        btnGuardar.setOnClickListener {
            prefs.edit()
                .putString(nameKey, etNombre.text.toString())
                .putString(phoneKey, etTelefono.text.toString())
                .putString(companyKey, etEmpresa.text.toString())
                .putBoolean(KEY_NOTIFS, swNotifs.isChecked)
                .apply()

            if (swNotifs.isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        RC_NOTIF_PERM
                    )
                } else {
                    if (!isAdmin) ClienteNotifsWorker.scheduleWeekly(this)
                }
            } else {
                WorkManager.getInstance(this).cancelAllWorkByTag("cliente_revision_semanal")
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
        val isAdmin = getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("is_admin", false)
        val avatarKey = if (isAdmin) "admin_avatar" else KEY_AVATAR
        if (requestCode == RC_PICK_PHOTO && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                ivAvatar.setImageURI(uri)
                getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString(avatarKey, uri.toString())
                    .apply()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RC_NOTIF_PERM && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ClienteNotifsWorker.scheduleWeekly(this)
        }
    }
}