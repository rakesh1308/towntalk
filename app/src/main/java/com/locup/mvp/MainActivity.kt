package com.locup.mvp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.locup.mvp.classifier.PostClassifierRepository
import com.locup.mvp.model.LocationProvider
import com.locup.mvp.repo.LocUpRepository
import com.locup.mvp.ui.LocUpApp

class MainActivity : ComponentActivity() {

    private val provider by lazy { LocationProvider(applicationContext) }
    private val classifier by lazy { PostClassifierRepository(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val repo = LocUpRepository.create(this, classifier)

        setContent {
            val theme = lightColorScheme()
            MaterialTheme(colorScheme = theme) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppRoot(classifier, provider, repo)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}

@Composable
private fun AppRoot(
    classifier: PostClassifierRepository,
    provider: LocationProvider,
    repo: LocUpRepository
) {
    var location by remember { mutableStateOf(provider.lastKnown()) }
    var errorBanner by remember { mutableStateOf<String?>(null) }
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        val ok = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (ok) {
            location = provider.lastKnown()
            errorBanner = if (location == null) "Permission granted but no location fix yet." else null
        } else {
            errorBanner = "Location permission denied — using demo coordinates."
        }
    }

    val requestLocation: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            location = provider.lastKnown()
        } else {
            launcher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    LocUpApp(
        location = location,
        errorBanner = errorBanner,
        repo = repo,
        classifier = classifier,
        onRequestLocation = requestLocation
    )
}
