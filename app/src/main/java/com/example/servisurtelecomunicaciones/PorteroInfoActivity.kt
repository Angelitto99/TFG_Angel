package com.example.servisurtelecomunicaciones

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PorteroInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_portero_info)

        // Recuperar la marca
        val brand = intent.getStringExtra("brand") ?: "Marca desconocida"

        // Título principal
        val tvBrand = findViewById<TextView>(R.id.tvBrand)
        tvBrand.text = "Información de $brand"

        // Subtítulo en rojo
        val tvSubtitle = findViewById<TextView>(R.id.tvSubtitle)
        tvSubtitle.text = getSubtitleText(brand)

        // Texto explicativo (diferencias)
        val tvDifferences = findViewById<TextView>(R.id.tvDifferences)
        tvDifferences.text = getDifferencesText(brand)

        // Precios para portero y videoportero
        val (pricePortero, priceVideoPortero) = getBrandPrices(brand)

        // Referencias a los TextViews de precios
        val tvPricePorteroValue = findViewById<TextView>(R.id.tvPricePorteroValue)
        val tvPriceVideoValue = findViewById<TextView>(R.id.tvPriceVideoValue)

        tvPricePorteroValue.text = pricePortero
        tvPriceVideoValue.text = priceVideoPortero

        // Cargar imágenes de placas
        val llPlates = findViewById<LinearLayout>(R.id.llPlates)
        val plateImages = getPlateImages(brand)
        for (imageRes in plateImages) {
            val ivPlate = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(dpToPx(250), dpToPx(250))
            layoutParams.setMargins(dpToPx(8), 0, dpToPx(8), 0)
            ivPlate.layoutParams = layoutParams
            ivPlate.scaleType = ImageView.ScaleType.CENTER_CROP

            Glide.with(this)
                .load(imageRes)
                .override(250, 250)
                .centerCrop()
                .into(ivPlate)

            llPlates.addView(ivPlate)
        }
    }

    // Subtítulo en rojo (puedes personalizarlo más para cada marca si quieres)
    private fun getSubtitleText(brand: String): String {
        return when (brand) {
            "Tegui" -> "Tecnología avanzada en Videoporteros"
            "Comelit" -> "Soluciones modernas y seguras"
            "Fermax" -> "Innovación y control de accesos"
            "Golmar" -> "Calidad de imagen y diseño"
            "Galak" -> "Seguridad avanzada y reconocimiento"
            else -> "Soluciones Innovadoras en Videoporteros"
        }
    }

    // Texto explicativo según la marca
    private fun getDifferencesText(brand: String): String {
        return when (brand) {
            "Tegui" ->
                "Tegui es líder en sistemas de videoporteros. Sus soluciones integran tecnología avanzada para el control de accesos, diferenciándose de los porteros tradicionales que solo ofrecen audio."
            "Comelit" ->
                "Comelit ofrece soluciones modernas, integrando comunicación visual y audio para mayor seguridad. Sus sistemas permiten un control más completo en comparación con los porteros convencionales."
            "Fermax" ->
                "Fermax destaca por su innovación en videoporteros, permitiendo comunicación efectiva y control remoto de accesos."
            "Golmar" ->
                "Golmar combina diseño y funcionalidad, ofreciendo videoporteros con alta calidad de imagen y sistemas de control integrados."
            "Galak" ->
                "Galak se centra en la seguridad avanzada, integrando videoporteros con reconocimiento y control de acceso."
            else ->
                "Los videoporteros ofrecen una ventaja significativa al permitir la identificación visual de quien llama."
        }
    }

    // Devuelve un par con los precios de portero y videoportero
    private fun getBrandPrices(brand: String): Pair<String, String> {
        return when (brand) {
            "Tegui" -> Pair("€150", "€200")
            "Comelit" -> Pair("€170", "€220")
            "Fermax" -> Pair("€160", "€210")
            "Golmar" -> Pair("€155", "€205")
            "Galak" -> Pair("€180", "€230")
            else -> Pair("€0", "€0")
        }
    }

    // Lista de imágenes (placas) para cada marca
    private fun getPlateImages(brand: String): List<Int> {
        return when (brand) {
            "Tegui" -> listOf(
                R.drawable.ic_tegui1
            )
            "Comelit" -> listOf(
                R.drawable.ic_comelit1,
                R.drawable.ic_comelit2,
                R.drawable.ic_comelit3,
                R.drawable.ic_comelit4
            )
            "Fermax" -> listOf(
                R.drawable.ic_fermax1
            )
            "Golmar" -> listOf(
                R.drawable.ic_golmar1,
                R.drawable.ic_golmar2
            )
            "Galak" -> listOf(
                R.drawable.ic_galak1,
                R.drawable.ic_galak2
            )
            else -> listOf(R.drawable.ic_videoportero_new)
        }
    }

    // Para convertir dp a px
    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
