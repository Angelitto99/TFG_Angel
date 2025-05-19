package com.example.servisurtelecomunicaciones

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class VideoporteroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoporteros)

        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val isGuest = prefs.getBoolean("is_guest", false)

        Glide.with(this)
            .load(R.drawable.ic_tegui)
            .override(160, 160)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivTegui))

        Glide.with(this)
            .load(R.drawable.ic_comelit)
            .override(160, 160)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivComelit))

        Glide.with(this)
            .load(R.drawable.ic_fermax)
            .override(160, 160)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivFermax))

        Glide.with(this)
            .load(R.drawable.ic_golmar)
            .override(160, 160)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivGolmar))

        Glide.with(this)
            .load(R.drawable.ic_galak)
            .override(160, 160)
            .centerCrop()
            .into(findViewById<ImageView>(R.id.ivGalak))

        findViewById<LinearLayout>(R.id.itemTegui).setOnClickListener {
            abrirInfoMarca("Tegui")
        }

        findViewById<LinearLayout>(R.id.itemComelit).setOnClickListener {
            abrirInfoMarca("Comelit")
        }

        findViewById<LinearLayout>(R.id.itemFermax).setOnClickListener {
            abrirInfoMarca("Fermax")
        }

        findViewById<LinearLayout>(R.id.itemGolmar).setOnClickListener {
            abrirInfoMarca("Golmar")
        }

        findViewById<LinearLayout>(R.id.itemGalak).setOnClickListener {
            abrirInfoMarca("Galak")
        }

        findViewById<Button>(R.id.btnPedirPresupuesto).setOnClickListener {
            if (isGuest) {
                toastConLogo("Debes registrarte para usar esta función")
            } else {
                startActivity(Intent(this, FormularioAvisoActivity::class.java))
            }
        }
    }

    private fun abrirInfoMarca(brand: String) {
        val intent = Intent(this, PorteroInfoActivity::class.java)
        intent.putExtra("brand", brand)
        startActivity(intent)
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
