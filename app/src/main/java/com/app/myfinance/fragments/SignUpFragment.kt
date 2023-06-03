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
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpFragment : Fragment() {
    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var buttonBackLogin: TextView

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_sign_up, container, false)
        editTextName = view.findViewById(R.id.cadastro_nome)
        editTextEmail = view.findViewById(R.id.cadastro_email)
        editTextPassword = view.findViewById(R.id.cadastro_password)

        buttonSignUp  = view.findViewById(R.id.btn_cadastrar)
        buttonSignUp.setOnClickListener {
            if(!validateSignUp()) {
                cadastrarUsuario(
                    editTextName.text.toString(),
                    editTextEmail.text.toString(),
                    editTextPassword.text.toString()
                )
            }
        }

        buttonBackLogin = view.findViewById(R.id.btn_back_login)
        buttonBackLogin.setOnClickListener {
            navigateToSignIn()
        }

        return view
    }

    private fun navigateToSignIn() {
        val action = SignUpFragmentDirections.actionSignUpFragmentToSignInFragment()
        findNavController().navigate(action)
    }

    fun validateSignUp(): Boolean {
        val string = when (true) {
            editTextName.text.isEmpty() -> getString(R.string.blank_name)
            (editTextName.text.length > 20) -> getString(R.string.big_name)
            editTextEmail.text.isEmpty() -> getString(R.string.blank_email)
            editTextPassword.text.isEmpty() -> getString(R.string.blank_password)
            (editTextPassword.text.length <= 6) -> getString(R.string.small_name)
            else -> ""
        }

        if(!string.isEmpty()) {
            Toast.makeText(activity, string, Toast.LENGTH_SHORT).show()
            return true
        }

        return false
    }

    fun cadastrarUsuario(nome: String, email: String, senha: String) {
        auth = FirebaseAuth.getInstance()

        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task: Task<AuthResult> ->
                if(task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nome)
                        .build()
                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask: Task<Void> ->
                            navigateToHome()
                        }
                }else{
                    Toast.makeText(activity, "Falha no login. Verifique seu e-mail e senha.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToHome() {
        val action = SignUpFragmentDirections.actionSignUpFragmentToHomeFragment()
        findNavController().navigate(action)
    }
}