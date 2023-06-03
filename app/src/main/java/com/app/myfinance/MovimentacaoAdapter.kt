package com.app.myfinance

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.myfinance.fragments.HomeFragment

class MovimentacaoAdapter(private val movimentacoes: List<HomeFragment.Movimentacao>, private val fragment: HomeFragment) : RecyclerView.Adapter<MovimentacaoAdapter.MovimentacaoViewHolder>() {

    class MovimentacaoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtAmount: TextView = itemView.findViewById(R.id.txtAmount)
        private val txtTipo: TextView = itemView.findViewById(R.id.txtTipo)
        private val txtData: TextView = itemView.findViewById(R.id.txtData)
        val btnDeleteMovimentacao: ImageButton = itemView.findViewById(R.id.btnDeleteMovimentacao)
        val btnEditMovimentacao: ImageButton = itemView.findViewById(R.id.btnEditMovimentacao)

        fun bind(movimentacao: HomeFragment.Movimentacao) {
            txtTitle.text = movimentacao.title
            txtAmount.text = movimentacao.amount
            txtTipo.text = movimentacao.tipo
            txtData.text = movimentacao.data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovimentacaoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movimentacao, parent, false)

        return MovimentacaoViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovimentacaoViewHolder, position: Int) {
        val movimentacao = movimentacoes[position]

        holder.btnDeleteMovimentacao.setOnClickListener {
            fragment.deleteMovimentacao(movimentacao)
        }

        holder.btnEditMovimentacao.setOnClickListener {
            fragment.editarMovimento(movimentacao)
        }

        holder.bind(movimentacao)
    }

    override fun getItemCount(): Int {
        return movimentacoes.size
    }
}