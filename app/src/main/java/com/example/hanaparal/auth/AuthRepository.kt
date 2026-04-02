package com.example.hanaparal.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.hanaparal.R
import com.example.hanaparal.models.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val appContext: Context
) {
    private val tag = "AuthRepository"
    private val credentialManager = CredentialManager.create(appContext)
    val currentUser: com.google.firebase.auth.FirebaseUser? get() = auth.currentUser

    suspend fun signInWithGoogle(activityContext: Context): Result<String> {
        return try {
            val webClientId = appContext.getString(R.string.default_web_client_id)
            
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            
            Log.d(tag, "Credential type received: ${credential.type}")

            when {
                credential is GoogleIdTokenCredential -> {
                    Result.success(credential.idToken)
                }
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    Result.success(googleIdTokenCredential.idToken)
                }
                else -> {
                    Log.e(tag, "Unexpected type: ${credential.type}")
                    Result.failure(Exception("Unexpected credential type. Please try again."))
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Credential Manager error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<User> {
        return try {
            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(firebaseCredential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Wrap Firestore calls in a timeout and try-catch to prevent hanging
                val user = withTimeout(10000) { // 10 second timeout
                    val userDoc = try {
                        firestore.collection("users").document(firebaseUser.uid).get().await()
                    } catch (e: Exception) { 
                        Log.e(tag, "Firestore get error: ${e.message}")
                        null 
                    }
                    
                    if (userDoc != null && userDoc.exists()) {
                        userDoc.toObject(User::class.java) ?: User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: ""
                        )
                    } else {
                        val newUser = User(
                            uid = firebaseUser.uid,
                            name = firebaseUser.displayName ?: "",
                            email = firebaseUser.email ?: ""
                        )
                        try {
                            firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                        } catch (e: Exception) {
                            Log.e(tag, "Firestore set error: ${e.message}")
                        }
                        newUser
                    }
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Auth failed: User null"))
            }
        } catch (e: Exception) {
            Log.e(tag, "Firebase/Firestore error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e(tag, "Sign out error", e)
        }
    }
}
