package com.pixelsface.towntalk.features.auth.data.repository

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.pixelsface.towntalk.core.common.error.AuthenticationException
import com.pixelsface.towntalk.core.common.error.NetworkException
import com.pixelsface.towntalk.core.common.error.ResourceNotFoundException
import com.pixelsface.towntalk.core.common.error.Result
import com.pixelsface.towntalk.core.common.error.TownTalkException
import com.pixelsface.towntalk.core.common.error.UnknownException
import com.pixelsface.towntalk.core.common.error.ValidationException
import com.pixelsface.towntalk.features.auth.data.UserPreferences
import com.pixelsface.towntalk.features.auth.data.model.UserDto
import com.pixelsface.towntalk.features.auth.data.model.toDomain
import com.pixelsface.towntalk.features.auth.domain.model.User
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import com.google.firebase.auth.FirebaseAuthException

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = auth.currentUser

    override fun getAuthState(): Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser != null)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    private suspend fun createOrUpdateFirestoreUser(firebaseUser: FirebaseUser, name: String? = null): User {
        val userDto = UserDto(
            docId = firebaseUser.uid,
            name = name ?: firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: "",
            photoUrl = firebaseUser.photoUrl?.toString(),
            city = "", // Default city, user can update later
            // createdAt and updatedAt will be set by @ServerTimestamp in DTO if null
            createdAt = null,
            updatedAt = null,
            isOnline = false, // Default online status
            lastSeen = null // Default last seen
        )

        firestore.collection(USERS_COLLECTION)
            .document(firebaseUser.uid)
            .set(userDto) // Using .set() will overwrite or create, relying on @ServerTimestamp
            .await()

        val createdUser = userDto.toDomain() // Convert DTO to domain for return and preferences
        userPreferences.saveUser(createdUser)
        return createdUser
    }

    override suspend fun registerWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()
                it.updateProfile(profileUpdates).await()
                Result.success(createOrUpdateFirestoreUser(it, name))
            } ?: Result.error(AuthenticationException("Failed to create user"))
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                val firestoreUser = getUserById(it.uid).getOrNull()
                if (firestoreUser != null) {
                    userPreferences.saveUser(firestoreUser)
                    Result.success(firestoreUser)
                } else {
                    Result.success(createOrUpdateFirestoreUser(it))
                }
            } ?: Result.error(AuthenticationException("Authentication failed"))
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> {
        return try {
            val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                val firestoreUser = getUserById(it.uid).getOrNull()
                if (firestoreUser != null) {
                     userPreferences.saveUser(firestoreUser)
                     Result.success(firestoreUser)
                } else {
                    Result.success(createOrUpdateFirestoreUser(it))
                }
            } ?: Result.error(AuthenticationException("Google authentication failed"))
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun handleGoogleSignInResult(data: Intent): Result<User> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
            val idToken = account.idToken ?: throw ValidationException("Google Sign In failed: No ID token")
            signInWithGoogle(idToken)
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun getGoogleSignInIntent(): Result<Intent> {
        return try {
            val googleSignInClient = GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(WEB_CLIENT_ID) 
                    .requestEmail()
                    .build()
            )
            Result.success(googleSignInClient.signInIntent)
        } catch (e: Exception) {
            Result.error(UnknownException("Failed to get Google sign-in intent", e))
        }
    }

    override suspend fun signOut() {
        currentUser?.uid?.let { userId ->
            // Attempt to set user offline before signing out from Firebase Auth
            // This is a best-effort, as network might fail or app might close abruptly
            // More robust presence systems use Cloud Functions with Realtime Database listeners
            try {
                setOnlineStatus(userId, false).getOrNull() // .getOrNull() to not block signout on failure
            } catch (e: Exception) {
                Timber.w(e, "Failed to set user offline during sign out for user $userId")
            }
        }
        auth.signOut()
        GoogleSignIn.getClient(context, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()
        userPreferences.clearUser()
    }

    override suspend fun getCurrentUser(): Result<User?> = withContext(Dispatchers.IO) {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            Result.success(null)
        } else {
            val firestoreUserResult = getUserById(firebaseUser.uid)
            if (firestoreUserResult.isSuccess()) {
                 val firestoreUser = firestoreUserResult.getOrNull()
                 if(firestoreUser != null) {
                    userPreferences.saveUser(firestoreUser) 
                    Result.success(firestoreUser)
                 } else {
                    try {
                        Result.success(createOrUpdateFirestoreUser(firebaseUser))
                    } catch (e: Exception) {
                        Result.error(mapFirebaseException(e))
                    }
                 }
            } else {
                 Result.error(firestoreUserResult.exceptionOrNull() ?: UnknownException("Failed to fetch user from Firestore"))
            }
        }
    }

    override suspend fun updateProfile(name: String, photoUrl: String?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val firebaseUser = auth.currentUser ?: return@withContext Result.error(AuthenticationException("User not authenticated"))
            
            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .apply { photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) } }
                .build()
            firebaseUser.updateProfile(profileUpdates).await()

            val userDocRef = firestore.collection(USERS_COLLECTION).document(firebaseUser.uid)
            val updates = mutableMapOf<String, Any?>(
                "name" to name,
                "updated_at" to FieldValue.serverTimestamp() // Use FieldValue for server timestamp on update
            )
            photoUrl?.let { updates["photo_url"] = it }
            // if bio is updatable: bio?.let { updates["bio"] = it }
            // if city is updatable: city?.let { updates["city"] = it }
            
            userDocRef.update(updates).await()

            // Fetch the updated user to save to preferences
            getUserById(firebaseUser.uid).getOrNull()?.let {
                userPreferences.saveUser(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }
    
    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun sendPhoneVerificationCode(phoneNumber: String): Result<String> {
        val deferred = CompletableDeferred<Result<String>>()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    deferred.complete(Result.error(TownTalkException("Automatic verification completed, manual sign-in not expected here.")))
                }
                override fun onVerificationFailed(e: FirebaseException) {
                    deferred.complete(Result.error(mapFirebaseException(e)))
                }
                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    deferred.complete(Result.success(verificationId))
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        return try {
            withTimeout(65000) { // Timeout slightly longer than phone auth timeout
                deferred.await()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            Result.error(NetworkException("Phone verification timed out"))
        }
    }

    override suspend fun verifyPhoneNumber(verificationId: String, code: String): Result<User> {
         return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val result = auth.signInWithCredential(credential).await()
            result.user?.let {
                val firestoreUser = getUserById(it.uid).getOrNull()
                if (firestoreUser != null) {
                    userPreferences.saveUser(firestoreUser)
                    Result.success(firestoreUser)
                } else {
                    Result.success(createOrUpdateFirestoreUser(it))
                }
            } ?: Result.error(AuthenticationException("Phone verification failed"))
        } catch (e: Exception) {
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun getUserById(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        try {
            val documentSnapshot = firestore.collection(USERS_COLLECTION).document(userId).get().await()
            if (documentSnapshot.exists()) {
                val userDto = documentSnapshot.toObject(UserDto::class.java)
                Result.success(userDto?.toDomain())
            } else {
                Result.success(null) // User not found in Firestore
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching user $userId from Firestore")
            Result.error(mapFirebaseException(e))
        }
    }

    override suspend fun getAllUsers(): Result<List<User>> = withContext(Dispatchers.IO) {
        try {
            val querySnapshot = firestore.collection(USERS_COLLECTION).get().await()
            val users = querySnapshot.documents.mapNotNull {
                it.toObject(UserDto::class.java)?.toDomain()
            }
            Result.success(users)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching all users from Firestore")
            Result.error(mapFirebaseException(e))
        }
    }

    // User Presence Implementation
    override fun getUserStream(userId: String): Flow<Result<User?>> = callbackFlow {
        val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
        val listenerRegistration = userDocRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.error(mapFirebaseException(error)))
                channel.close(error) // Close the channel on error
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val userDto = snapshot.toObject(UserDto::class.java)
                trySend(Result.success(userDto?.toDomain()))
            } else {
                trySend(Result.success(null)) // User document doesn't exist
            }
        }
        awaitClose { listenerRegistration.remove() }
    }

    override suspend fun setOnlineStatus(userId: String, isOnline: Boolean, lastSeen: Long?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userDocRef = firestore.collection(USERS_COLLECTION).document(userId)
            val updates = mutableMapOf<String, Any?>(
                "is_online" to isOnline
            )
            if (!isOnline) {
                // Only set last_seen when user goes offline
                // FieldValue.serverTimestamp() ensures the server's time is used.
                updates["last_seen"] = FieldValue.serverTimestamp()
            } else {
                // Optionally, clear last_seen when user comes online, if desired by setting to null.
                // However, @ServerTimestamp on DTO.lastSeen handles setting it if DTO.lastSeen is null during a .set() operation.
                // For a partial update, explicitly setting to null is needed to clear it.
                // updates["last_seen"] = null // Uncomment if last_seen should be cleared when online
            }
            userDocRef.update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating online status for user $userId")
            Result.error(mapFirebaseException(e))
        }
    }

    // Implemented to fulfill the AuthRepository interface requirement
    override suspend fun signInWithPhone(verificationId: String, code: String): Result<User> {
        // Delegate to the existing verifyPhoneNumber method
        return verifyPhoneNumber(verificationId, code)
    }

    private fun mapFirebaseException(e: Exception): TownTalkException {
        // Check if the exception is already a TownTalkException
        if (e is TownTalkException) return e

        return when (e) {
            is FirebaseAuthException -> {
                when (e.errorCode) {
                    "ERROR_EMAIL_ALREADY_IN_USE", "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                        AuthenticationException("An account already exists with this email or sign-in method.", e)
                    "ERROR_INVALID_EMAIL", "ERROR_WRONG_PASSWORD", "ERROR_INVALID_CREDENTIAL", "ERROR_USER_NOT_FOUND", "ERROR_USER_DISABLED" ->
                        AuthenticationException("Invalid credentials or user not found/disabled.", e)
                    "ERROR_WEAK_PASSWORD" ->
                        ValidationException("Password is too weak. Please choose a stronger password.", e)
                    "ERROR_NETWORK_REQUEST_FAILED" ->
                        NetworkException("A network error occurred. Please check your connection.", e)
                    // Add more specific Firebase Auth error codes as needed
                    // https://firebase.google.com/docs/reference/android/com/google/firebase/auth/FirebaseAuthException
                    else -> AuthenticationException("Authentication failed: ${e.message}", e)
                }
            }
            is FirebaseException -> NetworkException("A Firebase related network error occurred: ${e.message}", e)
            // Add more specific non-Firebase exception mappings if needed
            else -> UnknownException("An unexpected error occurred: ${e.message}", e)
        }
    }
    
    companion object {
        private const val USERS_COLLECTION = "users"
        private const val WEB_CLIENT_ID = "954804488114-slq4q0af31oafl4eihs02g5i5pam475f.apps.googleusercontent.com" // TODO: Move to a config file or BuildConfig
    }
} 