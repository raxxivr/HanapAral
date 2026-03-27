package com.example.hanaparal.auth

import android.content.Context
import com.example.hanaparal.R
import com.example.hanaparal.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context
) {
    val currentUser: com.google.firebase.auth.FirebaseUser? get() = auth.currentUser

    fun getGoogleSignInClient(): GoogleSignInClient {
        // We use R.string.default_web_client_id which is automatically generated 
        // by the Google Services plugin from your google-services.json file.
        val webClientId = try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            // Fallback if the resource is not found (usually means google-services.json is missing or not synced)
            ""
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .requestProfile()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun firebaseAuthWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Check if user exists in Firestore
                val userDoc = firestore.collection("users").document(firebaseUser.uid).get().await()
                
                val user = if (userDoc.exists()) {
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
                    firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                    newUser
                }
                Result.success(user)
            } else {
                Result.failure(Exception("Authentication failed: User is null"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        getGoogleSignInClient().signOut()
    }
}
