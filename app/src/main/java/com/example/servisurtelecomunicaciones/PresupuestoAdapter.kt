package com.example.servisurtelecomunicaciones

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class PresupuestoAdapter(
    private val items: MutableList<Presupuesto>,
    private val onEstadoChanged: (Presupuesto) -> Unit,
    private val onDelete: (Presupuesto) -> Unit,
    private val onEdit: (Presupuesto) -> Unit
) : RecyclerView.Adapter<PresupuestoAdapter.VH>() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvTipo: TextView = view.findViewById(R.id.tvPresupuestoTipo)
        val tvNum: TextView = view.findViewById(R.id.tvPresupuestoNumero)
        val tvCli: TextView = view.findViewById(R.id.tvPresupuestoCliente)
        val tvCom: TextView = view.findViewById(R.id.tvPresupuestoComunidad)
        val tvFec: TextView = view.findViewById(R.id.tvPresupuestoFecha)
        val tvEst: TextView = view.findViewById(R.id.tvPresupuestoEstado)
        val ivDel: ImageView = view.findViewById(R.id.ivPresupuestoDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_presupuesto, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        val ctx = holder.itemView.context

        holder.tvTipo.text = p.tipoPresupuesto
        holder.tvNum.text = "Presupuesto #${p.numero}"
        holder.tvCli.text = p.clienteNombre
        holder.tvCom.text = p.clienteDireccion
        holder.tvFec.text = dateFmt.format(Date(p.fecha))

        val colorInline = when (p.estado.lowercase()) {
            "aceptado" -> android.R.color.holo_green_dark
            "pendiente" -> android.R.color.holo_orange_dark
            "denegado" -> android.R.color.holo_red_dark
            else -> android.R.color.black
        }

        holder.tvEst.setTextColor(ctx.getColor(colorInline))
        holder.tvEst.text = p.estado.replaceFirstChar { it.uppercase() }

        holder.itemView.setOnClickListener {
            showDetailDialog(ctx, p, position)
        }

        holder.tvEst.setOnClickListener {
            val opts = arrayOf("Pendiente", "Aceptado", "Denegado")
            val sel = opts.indexOfFirst { it.equals(p.estado, ignoreCase = true) }.coerceAtLeast(0)

            AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme)
                .setTitle("Cambiar estado")
                .setSingleChoiceItems(opts, sel) { dlg, which ->
                    p.estado = opts[which].lowercase()
                    onEstadoChanged(p)
                    notifyItemChanged(position)
                    ctx.toastConLogo("Estado cambiado correctamente")
                    dlg.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        holder.ivDel.setOnClickListener {
            AlertDialog.Builder(ctx)
                .setTitle("Borrar presupuesto")
                .setMessage("¿Seguro que quieres borrar #${p.numero}?")
                .setPositiveButton("Sí") { d, _ ->
                    onDelete(p)
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    d.dismiss()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun showDetailDialog(ctx: Context, p: Presupuesto, pos: Int) {
        val v = LayoutInflater.from(ctx).inflate(R.layout.dialog_presupuesto_detail, null)

        v.findViewById<TextView>(R.id.tvDetTipo).text = p.tipoPresupuesto
        v.findViewById<TextView>(R.id.tvDetNumero).text = p.numero
        v.findViewById<TextView>(R.id.tvDetCliente).text = p.clienteNombre
        v.findViewById<TextView>(R.id.tvDetNIF).text = p.clienteNIF
        v.findViewById<TextView>(R.id.tvDetDireccion).text = p.clienteDireccion
        v.findViewById<TextView>(R.id.tvDetFecha).text = dateFmt.format(Date(p.fecha))
        v.findViewById<TextView>(R.id.tvDetPago).text = p.formaPago
        v.findViewById<TextView>(R.id.tvDetObs).text = p.observaciones

        val colorRes = when (p.estado.lowercase()) {
            "aceptado" -> android.R.color.holo_green_dark
            "pendiente" -> android.R.color.holo_orange_dark
            "denegado" -> android.R.color.holo_red_dark
            else -> android.R.color.black
        }

        val tvE = v.findViewById<TextView>(R.id.tvDetEstado)
        tvE.text = p.estado.replaceFirstChar { it.uppercase() }
        tvE.setTextColor(ctx.getColor(colorRes))

        AlertDialog.Builder(ctx)
            .setView(v)
            .setNegativeButton("Cambiar estado") { dlg, _ ->
                dlg.dismiss()
                val opts = arrayOf("Pendiente", "Aceptado", "Denegado")
                val sel = opts.indexOfFirst { it.equals(p.estado, ignoreCase = true) }.coerceAtLeast(0)

                AlertDialog.Builder(ctx, R.style.MyAlertDialogTheme)
                    .setTitle("Cambiar estado")
                    .setSingleChoiceItems(opts, sel) { d, which ->
                        p.estado = opts[which].lowercase()
                        onEstadoChanged(p)
                        notifyItemChanged(pos)
                        ctx.toastConLogo("Estado cambiado correctamente")
                        d.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
            .setNeutralButton("Editar") { dlg, _ ->
                dlg.dismiss()
                onEdit(p)
            }
            .setPositiveButton("Cerrar", null)
            .show()
    }
}
