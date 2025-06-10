package com.pixelsface.towntalk

import android.app.Application
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.pixelsface.towntalk.BuildConfig
import com.pixelsface.towntalk.core.common.lifecycle.AppLifecycleObserver
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TownTalkApplication : Application() {

    @Inject
    lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()

        // Install/update the security provider
        try {
            ProviderInstaller.installIfNeeded(applicationContext)
            Log.i("SecurityActions", "Security Provider installed/updated successfully.")
        } catch (e: GooglePlayServicesRepairableException) {
            Log.e("SecurityActions", "Google Play Services repairable error installing/updating provider", e)
            // Prompt user to update Google Play Services or handle appropriately.
        } catch (e: GooglePlayServicesNotAvailableException) {
            Log.e("SecurityActions", "Google Play Services not available for installing/updating provider", e)
            // Prompt user to install/enable Google Play Services or handle appropriately.
        } catch (e: Exception) {
            Log.e("SecurityActions", "Error installing/updating security provider", e)
        }

        FirebaseApp.initializeApp(this)

        val firebaseAppCheck = FirebaseAppCheck.getInstance()

        if (BuildConfig.DEBUG) {
            Log.d("AppCheck", "Initializing AppCheck with Debug provider")
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        }

        Log.d("AppCheck", "Initializing AppCheck with Play Integrity provider")
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        // Register AppLifecycleObserver for presence management
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        Log.d("AppLifecycle", "AppLifecycleObserver registered.")
    }
}