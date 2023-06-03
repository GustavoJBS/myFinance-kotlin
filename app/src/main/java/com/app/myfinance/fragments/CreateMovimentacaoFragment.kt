package com.app.myfinance.fragments

import android.os.Bundle
import android.text.Editable
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.app.myfinance.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar

class CreateMovimentacaoFragment : Fragment() {
    private lateinit var textTitle: EditText
    private lateinit var textValue: EditText
    private lateinit var buttonBack: Button
    private lateinit var buttonSave: Button

    private lateinit var movimentacaoTypes: Spinner
    private lateinit var dt_calendar: CalendarView

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var movimentacoesRef: DatabaseReference
    val listaMovimentacoes = arrayOf("Receita", "Despesa")


    data class Movimentacao(
        val title: String,
        val amount: String,
        val tipo: String,
        val data: String?
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_movimentacao, container, false)

        init(view)

        if (arguments?.getString("id") != null)  {
            setMovimentacao()
        }

        return view
    }

    fun setMovimentacao() {
        val id = arguments?.getString("id")

        if (id != null) {
            movimentacoesRef.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(movimentacaoSnapshot: DataSnapshot) {
                    if (movimentacaoSnapshot.exists()) {
                        val title = movimentacaoSnapshot.child("title").getValue(String::class.java)
                        val amount = movimentacaoSnapshot.child("amount").getValue(String::class.java)
                        val tipo = movimentacaoSnapshot.child("tipo").getValue(String::class.java)
                        val date = movimentacaoSnapshot.child("data").getValue(String::class.java)

                        textValue.text = Editable.Factory.getInstance().newEditable(amount)

                        textTitle.text = Editable.Factory.getInstance().newEditable(title)
                        movimentacaoTypes.setSelection(listaMovimentacoes.indexOf(tipo))
                        if (date != null) {
                            dt_calendar.setDate(date.toLong())
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }

    fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if(userId != null)   {
            movimentacoesRef = database.getReference("movimentacoes").child(userId)
        }

        textValue = view.findViewById(R.id.txtMovimentacaoValue)
        textTitle = view.findViewById(R.id.txtMovimentacaoTitle)
        dt_calendar =  view.findViewById(R.id.dt_calendar)

        movimentacaoTypes = view.findViewById(R.id.list_types)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listaMovimentacoes)
        movimentacaoTypes.adapter = adapter

        buttonBack  = view.findViewById(R.id.btn_back_home)
        buttonBack.setOnClickListener {
            navigateHome()
        }

        buttonSave  = view.findViewById(R.id.btn_save)
        buttonSave.setOnClickListener {
            if (!validateMovimentacao()) {
                addMovimentacao(textTitle.text.toString(), textValue.text.toString(), movimentacaoTypes.selectedItem.toString(), dt_calendar.getDate().toString())
            }
        }

        dt_calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)
            dt_calendar.setDate(selectedDate.timeInMillis)
        }
    }

    fun validateMovimentacao(): Boolean {
        val string = when (true) {
            textTitle.text.isEmpty() -> getString(R.string.blank_title)
            (textTitle.text.length > 20) -> getString(R.string.big_title)
            textValue.text.isEmpty() -> getString(R.string.blank_value)
            movimentacaoTypes.selectedItem.toString().isEmpty() -> getString(R.string.blank_type)
            dt_calendar.date.toString().isEmpty()  -> getString(R.string.blank_date)
            else -> ""
        }

        if(!string.isEmpty()) {
            Toast.makeText(activity, string, Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }

    fun addMovimentacao(title: String, value: String, type: String, date: String) {
        val movimentacao = Movimentacao(title, value, type, date)
        val id = arguments?.getString("id")

        var novaMovimentacao = movimentacoesRef.push()

        if(id != null)  {
            novaMovimentacao = movimentacoesRef.child(id)
        }

        novaMovimentacao.setValue(movimentacao)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateHome()
                }
            }
    }

    fun navigateHome() {
        val action = CreateMovimentacaoFragmentDirections.actionCreateMovimentacaoFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}