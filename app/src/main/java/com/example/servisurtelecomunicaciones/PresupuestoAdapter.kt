package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class PresupuestoAdapter(
    private val items: MutableList<Presupuesto>,
    private val onEstadoChanged: (Presupuesto) -> Unit,
    private val onDelete:        (Presupuesto) -> Unit,
    private val onEdit:          (Presupuesto) -> Unit
) : RecyclerView.Adapter<PresupuestoAdapter.VH>() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    inner class VH(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvNum  = view.findViewById<TextView>(R.id.tvPresupuestoNumero)
        val tvCli  = view.findViewById<TextView>(R.id.tvPresupuestoCliente)
        val tvFec  = view.findViewById<TextView>(R.id.tvPresupuestoFecha)
        val tvEst  = view.findViewById<TextView>(R.id.tvPresupuestoEstado)
        val ivDel  = view.findViewById<android.widget.ImageView>(R.id.ivPresupuestoDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_presupuesto, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p   = items[position]
        val ctx = holder.itemView.context

        holder.tvNum.text = "Presupuesto #${p.numero}"
        holder.tvCli.text = p.clienteNombre
        holder.tvFec.text = dateFmt.format(Date(p.fecha))

        // Estado en color
        fun tint(e: String) {
            val colorRes = when (e.lowercase()) {
                "aceptado"  -> android.R.color.holo_green_dark
                "pendiente" -> android.R.color.holo_orange_dark
                "denegado"  -> android.R.color.holo_red_dark
                else        -> android.R.color.black
            }
            holder.tvEst.setTextColor(ctx.getColor(colorRes))
            holder.tvEst.text = e.replaceFirstChar { it.uppercase() }
        }
        tint(p.estado)

        // Al tocar la tarjeta: mostrar diálogo de detalle
        holder.itemView.setOnClickListener {
            showDetailDialog(ctx, p, position)
        }

        // Cambiar estado desde el item
        holder.tvEst.setOnClickListener {
            val opts = arrayOf("Pendiente","Aceptado","Denegado")
            val sel  = opts.indexOfFirst { it.lowercase()==p.estado.lowercase() }
                .takeIf{it>=0} ?: 0
            AlertDialog.Builder(ctx)
                .setTitle("Cambiar estado")
                .setSingleChoiceItems(opts, sel) { dlg, which ->
                    p.estado = opts[which].lowercase()
                    onEstadoChanged(p)
                    notifyItemChanged(position)
                    dlg.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Borrar
        holder.ivDel.setOnClickListener {
            AlertDialog.Builder(ctx)
                .setTitle("Borrar presupuesto")
                .setMessage("¿Seguro que quieres borrar #${p.numero}?")
                .setPositiveButton("Sí") { d,_ ->
                    onDelete(p)
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    d.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    /** Reproduce exactamente el detalle que hace Facturas */
    private fun showDetailDialog(ctx: Context, p: Presupuesto, pos: Int) {
        val v = LayoutInflater.from(ctx)
            .inflate(R.layout.dialog_presupuesto_form, null)

        // Rellenar campos
        v.findViewById<TextView>(R.id.tvDetNumero).text    = p.numero
        v.findViewById<TextView>(R.id.tvDetCliente).text   = p.clienteNombre
        v.findViewById<TextView>(R.id.tvDetNIF).text       = p.clienteNIF
        v.findViewById<TextView>(R.id.tvDetDireccion).text = p.clienteDireccion
        v.findViewById<TextView>(R.id.tvDetTipo).text      = p.tipoServicio
        v.findViewById<TextView>(R.id.tvDetPago).text      = p.formaPago
        v.findViewById<TextView>(R.id.tvDetObs).text       = p.observaciones
        v.findViewById<TextView>(R.id.tvDetFecha).text     =
            dateFmt.format(Date(p.fecha))
        // Estado
        val tvE = v.findViewById<TextView>(R.id.tvDetEstado)
        tvE.text = p.estado.replaceFirstChar { it.uppercase() }
        tvE.setTextColor(ctx.getColor(
            when(p.estado.lowercase()) {
                "aceptado"  -> android.R.color.holo_green_dark
                "pendiente" -> android.R.color.holo_orange_dark
                "denegado"  -> android.R.color.holo_red_dark
                else        -> android.R.color.black
            }
        ))

        AlertDialog.Builder(ctx)
            .setView(v)
            .setNegativeButton("Cambiar estado") { dlg,_ ->
                dlg.dismiss()
                // reaprovechamos el mismo método
                val opts = arrayOf("Pendiente","Aceptado","Denegado")
                val sel  = opts.indexOfFirst { it.lowercase()==p.estado.lowercase() }
                    .takeIf{it>=0} ?: 0
                AlertDialog.Builder(ctx)
                    .setTitle("Cambiar estado")
                    .setSingleChoiceItems(opts, sel) { d,which ->
                        p.estado = opts[which].lowercase()
                        onEstadoChanged(p)
                        notifyItemChanged(pos)
                        d.dismiss()
                    }
                    .show()
            }
            .setNeutralButton("Editar") { dlg,_ ->
                dlg.dismiss()
                onEdit(p)
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }
}
