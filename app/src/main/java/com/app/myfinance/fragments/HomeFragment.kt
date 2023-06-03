package com.app.myfinance.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.myfinance.MovimentacaoAdapter
import com.app.myfinance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var movimentacoesRef: DatabaseReference

    private lateinit var buttonLogout: Button
    private lateinit var buttonAdd: Button

    private lateinit var username: TextView
    private lateinit var home_amount: TextView

    private lateinit var currencyFormat: NumberFormat
    private lateinit var dateFormat: SimpleDateFormat

    data class Movimentacao(
        val id: String,
        val title: String,
        val amount: String,
        val tipo: String,
        val data: String?
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        init(view)

        buttonLogout = view.findViewById(R.id.btn_logout)
        buttonLogout.setOnClickListener {
            auth.signOut()

            val action = HomeFragmentDirections.actionHomeFragmentToSignInFragment()
            findNavController().navigate(action)
        }

        buttonAdd  = view.findViewById(R.id.btn_add)
        buttonAdd.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToCreateMovimentacaoFragment()
            findNavController().navigate(action)
        }


        return view
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()

        currencyFormat = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))

        username = view.findViewById(R.id.home_username)
        username.text = auth.currentUser?.displayName

        home_amount = view.findViewById(R.id.home_amount)
        home_amount.text = currencyFormat.format(0)

        getGastos(view)
    }

     fun getGastos(view: View) {
        database = FirebaseDatabase.getInstance()
        movimentacoesRef = database.getReference("movimentacoes")
        val currentUser = auth.currentUser
        val userId = currentUser?.uid
        val movimentacoesList = mutableListOf<Movimentacao>()

        if (userId != null) {
            var totalAmount: Double = 0.0

            movimentacoesRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {

                    for (movimentacaoSnapshot in dataSnapshot.children) {
                        val title = movimentacaoSnapshot.child("title").getValue(String::class.java)
                        val amount = movimentacaoSnapshot.child("amount").getValue(String::class.java)
                        val tipo = movimentacaoSnapshot.child("tipo").getValue(String::class.java)
                        val date = movimentacaoSnapshot.child("data").getValue(String::class.java)

                        if (movimentacaoSnapshot.key != null && title != null && amount != null && tipo != null && date != null) {
                            val movimentacao = Movimentacao(
                                movimentacaoSnapshot.key.toString(),
                                title,
                                currencyFormat.format(amount.toDouble()),
                                tipo,
                                dateFormat.format(Date(date.toLong())).toString()
                            )
                            movimentacoesList.add(movimentacao)

                            if (movimentacao.tipo == "Receita") {
                                totalAmount += amount.toDouble()
                            }else {
                                totalAmount -= amount.toDouble()
                            }
                        }
                    }

                    val recyclerView = view.findViewById<RecyclerView>(R.id.list_movimentacoes)
                    val llm = LinearLayoutManager(requireContext())

                    llm.orientation = LinearLayoutManager.VERTICAL
                    recyclerView.layoutManager = llm
                    recyclerView.adapter = MovimentacaoAdapter(movimentacoesList.sortedByDescending { it.data },this@HomeFragment)

                    home_amount.text = currencyFormat.format(totalAmount)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Ocorreu um erro ao recuperar os dados
                }
            })

        }
     }

    fun deleteMovimentacao(movimentacao: Movimentacao) {
        showConfirmationDialog(requireContext(), "Você deseja deletar essa Movimentação?", movimentacao)
    }

    fun showConfirmationDialog(context: Context, title: String, movimentacao: Movimentacao) {
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setTitle(title)

        alertDialogBuilder.setPositiveButton("Confirmar") { dialog, _ ->
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            val userId = currentUser?.uid
            val database = FirebaseDatabase.getInstance()
            if (userId != null) {
                database.getReference("movimentacoes").child(userId).child(movimentacao.id).removeValue()
            }
            view?.let { this.getGastos(it) }
            dialog.dismiss()
        }

        alertDialogBuilder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    fun editarMovimento(movimentacao: Movimentacao) {
        val bundle = Bundle()
        bundle.putString("id", movimentacao.id)

        findNavController().navigate(R.id.action_homeFragment_to_createMovimentacaoFragment, bundle)
    }
}
