package com.pixelsface.towntalk.core.common.lifecycle

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pixelsface.towntalk.features.auth.domain.repository.AuthRepository
import com.pixelsface.towntalk.features.auth.domain.usecase.UpdateUserPresenceUseCase
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Observes application lifecycle events to update user presence (online/offline status).
 */
@Singleton
class AppLifecycleObserver @Inject constructor(
    private val updateUserPresenceUseCase: UpdateUserPresenceUseCase,
    private val authRepository: AuthRepository
) : DefaultLifecycleObserver {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        scope.launch {
            // Check if user is logged in before updating presence
            val currentUser = authRepository.currentUser // Direct access to FirebaseUser for quick check
            if (currentUser != null) {
                Timber.d("App entered foreground, setting user online.")
                val result = updateUserPresenceUseCase(isOnline = true)
                if (result.isError()) {
                    Timber.w(result.exceptionOrNull(), "Failed to set user online on app start.")
                }
            } else {
                Timber.d("App entered foreground, but no user logged in.")
            }
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        scope.launch {
            // Check if user is logged in before updating presence
            val currentUser = authRepository.currentUser // Direct access to FirebaseUser for quick check
            if (currentUser != null) {
                Timber.d("App entered background, setting user offline.")
                val result = updateUserPresenceUseCase(isOnline = false)
                if (result.isError()) {
                    Timber.w(result.exceptionOrNull(), "Failed to set user offline on app stop.")
                }
            } else {
                Timber.d("App entered background, but no user logged in.")
            }
        }
    }
    // Note: SupervisorJob().cancel() could be called if this observer itself needs to be cleaned up,
    // but when registered with ProcessLifecycleOwner, it lives with the process.
} 