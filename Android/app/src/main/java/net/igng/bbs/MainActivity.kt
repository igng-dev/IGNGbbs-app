package net.igng.bbs

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import net.igng.bbs.core.auth.OAuthCallbackResult

class MainActivity : ComponentActivity() {

    private val deepLinkEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    private val authMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as BbsApplication

        setContent {
            BbsAppRoot(
                container = app.container,
                incomingDeepLinks = deepLinkEvents,
                incomingAuthMessages = authMessages,
            )
        }

        handleIncomingIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        val app = application as BbsApplication

        if (uri.scheme == "igngbbs" && uri.host == "auth" && uri.path == "/callback") {
            lifecycleScope.launch {
                when (val result = app.container.oauthManager.handleCallbackUri(uri)) {
                    is OAuthCallbackResult.Success -> {
                        app.container.refreshBootstrap()
                        app.container.settleDailyLoginIfNeeded()
                        authMessages.tryEmit("欢迎回来，${result.account.displayName}")
                    }
                    is OAuthCallbackResult.Error -> {
                        authMessages.tryEmit(result.message)
                    }
                    OAuthCallbackResult.Ignored -> Unit
                }
            }
            return
        }

        AppRoutes.resolveDeepLink(uri)?.let { route ->
            deepLinkEvents.tryEmit(route)
        }
    }
}
