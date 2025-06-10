package com.pixelsface.towntalk.features.profile.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.pixelsface.towntalk.core.domain.manager.UserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserManagerViewModel @Inject constructor(
    private val userManager: UserManager
) : ViewModel() {

    fun getCurrentUserId(): String? {
        return userManager.getCurrentUserId()
    }

    fun isUserLoggedIn(): Boolean {
        return userManager.isUserLoggedIn()
    }
    
    fun signOut() {
        // This is a placeholder implementation
        // In a real app, this would call a method on the UserManager interface
        // For now, we'll use Firebase Auth directly
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
    }
} 