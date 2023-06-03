package com.app.myfinance.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.app.myfinance.R
import com.google.firebase.auth.FirebaseAuth

class SignInFragment : Fragment() {
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonSignUp: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        auth = FirebaseAuth.getInstance()

        editTextEmail = view.findViewById(R.id.username)
        editTextPassword = view.findViewById(R.id.password)
        buttonLogin = view.findViewById(R.id.btn_login)
        buttonSignUp = view.findViewById(R.id.btn_criar_conta)

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(activity, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
            } else {
                signIn(email, password)
            }
        }

        buttonSignUp.setOnClickListener {
            navigateToSignUp()
        }

        return view
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    Toast.makeText(activity, "Falha no login. Verifique seu e-mail e senha.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome() {
        val action = SignInFragmentDirections.actionSignInFragmentToHomeFragment()
        findNavController().navigate(action)
    }

    private fun navigateToSignUp() {
        val action = SignInFragmentDirections.actionSignInFragmentToSignUnFragment()
        findNavController().navigate(action)
    }
}