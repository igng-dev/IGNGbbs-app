package net.igng.bbs

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import net.igng.bbs.core.data.BbsAppContainer
import net.igng.bbs.core.model.SiteEndpoints

class BbsApplication : Application(), SingletonImageLoader.Factory {

    lateinit var container: BbsAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = BbsAppContainer(
            context = this,
            endpoints = SiteEndpoints(
                apiBaseUrl = BuildConfig.IGNG_API_BASE_URL,
                ssoBaseUrl = BuildConfig.IGNG_SSO_BASE_URL,
                bbsWebBaseUrl = BuildConfig.IGNG_BBS_BASE_URL,
            ),
            appVersion = BuildConfig.VERSION_NAME,
        )
        NotificationSyncWorker.schedule(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(callFactory = { container.httpClient.mediaOkHttpClient }))
            }
            .build()
}
