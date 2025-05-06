package com.example.servisurtelecomunicaciones

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import android.widget.ImageView

class VideoporteroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_videoporteros)

        // Cargar imágenes con Glide para cada marca (ajustamos a 160x160)
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

        // Configurar onClick para cada ítem
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

        // Botón: Pedir Presupuesto (igual que antes)
        findViewById<Button>(R.id.btnPedirPresupuesto).setOnClickListener {
            // Para este ejemplo, puede abrir el formulario de aviso
            startActivity(Intent(this, FormularioAvisoActivity::class.java))
        }
    }

    private fun abrirInfoMarca(brand: String) {
        val intent = Intent(this, PorteroInfoActivity::class.java)
        intent.putExtra("brand", brand)
        startActivity(intent)
    }
}
