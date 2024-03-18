package com.app.agrohelper.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.agrohelper.MainActivity
import com.app.agrohelper.R
import com.app.agrohelper.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignInActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // If user is already authenticated, move to next activity
        if (mAuth.currentUser != null) {
            moveToNextActivity()
        }

        // Google sign-in configuration
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Set click listener for sign-in button
        binding.signInButton.setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }
    }

    // Handle result of Google sign-in
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN && data != null) {
            val account: GoogleSignInAccount?

            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                account = task.getResult(ApiException::class.java)
            } catch (exception: ApiException) {
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                return
            }

            // Authenticate with Firebase using Google credentials
            val credential = GoogleAuthProvider.getCredential(account?.idToken, null)

            mAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful && task.result?.user != null) {
                    // Authentication successful, move to next activity
                    moveToNextActivity()
                } else {
                    // Authentication failed, show error message
                    onError(task.exception?.message ?: "Some error occurred")
                }
            }
        }
    }

    // Move to main activity
    private fun moveToNextActivity() {
        val intent = Intent(this@SignInActivity, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Show error message
    private fun onError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val RC_SIGN_IN = 1001
    }
}
