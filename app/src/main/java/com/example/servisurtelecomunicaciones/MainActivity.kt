package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_VISIBLE

        FirebaseApp.initializeApp(this)
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
            val isAdmin = prefs.getBoolean("is_admin", false)
            val nextActivity = if (isAdmin) AdminHomeActivity::class.java else HomeActivity::class.java
            startActivity(Intent(this, nextActivity))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}
