package com.locup.mvp

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.locup.mvp.di.AppContainer
import com.locup.mvp.platform.location.AndroidLocationProvider
import com.locup.mvp.presentation.FeedViewModelFactory
import com.locup.mvp.presentation.compose.ComposerSheet
import com.locup.mvp.presentation.feed.FeedScreen
import com.locup.mvp.presentation.feed.FeedViewModel
import com.locup.mvp.presentation.theme.TownTalkTheme

/**
 * Single-activity host.
 *
 * Owns the [AppContainer] because the classifier's native resources are
 * process-scoped: they must outlive individual ViewModels but be released when
 * the activity is destroyed for good.
 */
class MainActivity : ComponentActivity() {

    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        container = AppContainer(applicationContext)

        setContent {
            TownTalkTheme {
                TownTalkApp(container)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Only tear down when the activity is finishing for real, not on a
        // configuration change where it will be recreated immediately.
        if (isFinishing) {
            container.close()
        }
    }
}

/**
 * Wires state, location permission and the composer together.
 *
 * Collection uses `collectAsStateWithLifecycle`, so the flows stop being
 * collected when the screen is not visible and resume with fresh state when
 * it returns.
 */
@Composable
private fun TownTalkApp(container: AppContainer) {
    val context = LocalContext.current
    val locationProvider = remember { AndroidLocationProvider(context) }

    val viewModel: FeedViewModel = viewModel(
        factory = FeedViewModelFactory(container),
    )
    val feedState by viewModel.uiState.collectAsStateWithLifecycle()
    val composerState by viewModel.composer.collectAsStateWithLifecycle()

    var composerOpen by remember { mutableStateOf(false) }

    // Push whatever position we can get at startup into the ViewModel.
    LaunchedEffect(Unit) {
        viewModel.updateLocation(locationProvider.lastKnownLocation())
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        val allowed = granted[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                granted[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.updateLocation(locationProvider.lastKnownLocation())
        if (!allowed) {
            viewModel.updateLocation(null)
        }
    }

    FeedScreen(
        state = feedState,
        onFilterChange = viewModel::setFilter,
        onToggleSubscription = viewModel::toggleSubscription,
        onRequestLocation = {
            if (locationProvider.hasPermission()) {
                viewModel.updateLocation(locationProvider.lastKnownLocation())
            } else {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                    ),
                )
            }
        },
        onUpvote = viewModel::upvote,
        onDownvote = viewModel::downvote,
        onComposeClick = { composerOpen = true },
        modifier = Modifier.fillMaxSize(),
    )

    if (composerOpen) {
        ComposerSheet(
            state = composerState,
            areaLabel = feedState.areaLabel,
            hasLocation = feedState.hasLocation,
            onTextChange = viewModel::onComposerTextChanged,
            // Dismiss only once the post has actually been persisted.
            onPublish = {
                viewModel.publish(onSuccess = { composerOpen = false })
            },
            onDismiss = {
                composerOpen = false
                viewModel.dismissComposer()
            },
        )
    }
}