package com.rojdaurper.ingilizcemobil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            goMainActivity()
        }

        val emailEditText = findViewById<EditText>(R.id.etEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPassword)
        val signInButton = findViewById<Button>(R.id.btnSignIn)

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // SADECE GİRİŞ YAPMAYI DENE
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { loginTask ->
                        if (loginTask.isSuccessful) {
                            Toast.makeText(this, "Tekrar hoş geldiniz!", Toast.LENGTH_SHORT).show()
                            goMainActivity()
                        } else {
                            // Hata durumunda kayıt yapma, kullanıcıya hatayı söyle
                            Toast.makeText(this, "Giriş başarısız: Şifre hatalı veya hesap yok.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Lütfen alanları doldurun", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}