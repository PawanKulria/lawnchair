/*
 * Copyright 2021, Lawnchair
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.lawnchair

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.ViewTreeSavedStateRegistryOwner
import app.lawnchair.gestures.GestureController
import app.lawnchair.nexuslauncher.OverlayCallbackImpl
import app.lawnchair.preferences.PreferenceManager
import app.lawnchair.root.RootHelperManager
import app.lawnchair.root.RootNotAvailableException
import com.android.launcher3.*
import com.android.launcher3.R
import com.android.launcher3.statemanager.StateManager
import com.android.launcher3.uioverrides.QuickstepLauncher
import com.android.launcher3.uioverrides.states.OverviewState
import com.android.systemui.plugins.shared.LauncherOverlayManager
import com.google.ads.consent.*
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.nkart.neo.IconPackReceiver
import com.nkart.neo.dialogs.WelcomeScreenDialog
import com.nkart.neo.extra.Extra
import com.nkart.neo.utils.Config
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL

class LawnchairLauncher : QuickstepLauncher(), LifecycleOwner,
    SavedStateRegistryOwner, OnBackPressedDispatcherOwner {
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var consentForm: ConsentForm
    private lateinit var mInterstitialAd: InterstitialAd
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val _onBackPressedDispatcher = OnBackPressedDispatcher {
        super.onBackPressed()
    }
    val gestureController by lazy { GestureController(this) }
    private val defaultOverlay by lazy { OverlayCallbackImpl(this) }
    private val prefs by lazy { PreferenceManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        savedStateRegistryController.performRestore(savedInstanceState)
        super.onCreate(savedInstanceState)
        broadcastReceiver = IconPackReceiver()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        prefs.launcherTheme.subscribeChanges(this, ::updateTheme)

        if (prefs.autoLaunchRoot.get()) {
            lifecycleScope.launch {
                try {
                    RootHelperManager.INSTANCE.get(this@LawnchairLauncher).getService()
                } catch (e: RootNotAvailableException) {
                    // do nothing
                }
            }
        }
        if (!prefs.showStatusBar.get()) {
            val insetsController = WindowInsetsControllerCompat(launcher.window, rootView)
            insetsController.hide(WindowInsetsCompat.Type.statusBars())
            launcher.stateManager.addStateListener(object : StateManager.StateListener<LauncherState> {
                override fun onStateTransitionStart(toState: LauncherState) {
                    if (toState is OverviewState) {
                        insetsController.show(WindowInsetsCompat.Type.statusBars())
                    }
                }

                override fun onStateTransitionComplete(finalState: LauncherState) {
                    if (finalState !is OverviewState) {
                        insetsController.hide(WindowInsetsCompat.Type.statusBars())
                    }
                }
            })
        }
        val sharedPreferences = getSharedPreferences("first time", MODE_PRIVATE)
        val firstTime = sharedPreferences.getBoolean("first", true)
        val editor = sharedPreferences.edit()
        if (firstTime && savedInstanceState == null) {
            if (Extra.isInternetON()) {
                collectConsent()
            }
            val welcomeScreenDialog = WelcomeScreenDialog(this)
            welcomeScreenDialog.show()
            editor.putBoolean("first", false)
            editor.apply()
            loadAd()
        }
    }

    override fun setupViews() {
        super.setupViews()
        val launcherRootView = findViewById<LauncherRootView>(R.id.launcher)
        ViewTreeLifecycleOwner.set(launcherRootView, this)
        ViewTreeSavedStateRegistryOwner.set(launcherRootView, this)
    }

    override fun onStart() {
        super.onStart()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        val intentFilter = IntentFilter("com.nkart.launcher.setIconPack")
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onResume() {
        super.onResume()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        restartIfPending()

        dragLayer.viewTreeObserver.addOnDrawListener(object : ViewTreeObserver.OnDrawListener {
            var handled = false

            override fun onDraw() {
                if (handled) {
                    return
                }
                handled = true

                dragLayer.post {
                    dragLayer.viewTreeObserver.removeOnDrawListener(this)
                }
                depthController.reapplyDepth()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    }

    override fun onStop() {
        super.onStop()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onBackPressed() {
        _onBackPressedDispatcher.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        savedStateRegistryController.performSave(outState)
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun getSavedStateRegistry(): SavedStateRegistry {
        return savedStateRegistryController.savedStateRegistry
    }

    override fun getOnBackPressedDispatcher(): OnBackPressedDispatcher {
        return _onBackPressedDispatcher
    }

    override fun getDefaultOverlay(): LauncherOverlayManager {
        return defaultOverlay
    }

    private fun restartIfPending() {
        when {
            sRestartFlags and FLAG_RESTART != 0 -> lawnchairApp.restart(false)
            sRestartFlags and FLAG_RECREATE != 0 -> {
                sRestartFlags = 0
                recreate()
            }
        }
    }

    private fun scheduleFlag(flag: Int) {
        sRestartFlags = sRestartFlags or flag
        if (lifecycle.currentState === Lifecycle.State.RESUMED) {
            restartIfPending()
        }
    }

    fun scheduleRecreate() {
        scheduleFlag(FLAG_RECREATE)
    }

    fun scheduleRestart() {
        scheduleFlag(FLAG_RESTART)
    }

    fun recreateIfNotScheduled() {
        if (sRestartFlags == 0) {
            recreate()
        }
    }
    fun loadAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(this, Config.ADMOB_SPLASH_LAUNCHER_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd
                Log.i("TAG", "onAdLoaded")
                mInterstitialAd.setFullScreenContentCallback(object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        // Called when fullscreen content is dismissed.
                        Log.d("TAG", "The ad was dismissed.")
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        // Called when fullscreen content failed to show.
                        Log.d("TAG", "The ad failed to show.")
                    }

                    override fun onAdShowedFullScreenContent() {
                        // Called when fullscreen content is shown.
                        // Make sure to set your reference to null so you don't
                        // show it a second time.
                   //     mInterstitialAd = null
                        Log.d("TAG", "The ad was shown.")
                    }
                })
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                Log.i("TAG", loadAdError.message)
            //    mInterstitialAd = null
            }
        })
    }

    @SuppressLint("ResourceType")
    fun displayInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(this)
        }
        val myWallpaperManager = WallpaperManager.getInstance(applicationContext)
        try {
            // Change the current system wallpaper
            myWallpaperManager.setResource(R.drawable.default_wallpaper)
        } catch (e: IOException) {
            // TODO Auto-generated catch block
        }
    }
    private fun collectConsent() {
        val consentInformation = ConsentInformation.getInstance(this)
        //    ConsentInformation.getInstance(this).setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        val publisherIds = arrayOf("pub-8553297703763663")
        //    ConsentInformation.getInstance(getApplicationContext()).addTestDevice("F1F48DA734221EE385D1F6C00CCB685D");
        consentInformation.requestConsentInfoUpdate(publisherIds, object : ConsentInfoUpdateListener {
            override fun onConsentInfoUpdated(consentStatus: ConsentStatus) {
                // User's consent status successfully updated.
                ConsentInformation.getInstance(applicationContext).isRequestLocationInEeaOrUnknown
                //            Toast.makeText(getApplicationContext(), "User's consent status successfully updated.", Toast.LENGTH_SHORT).show();
                if (consentInformation.isRequestLocationInEeaOrUnknown) {
                    when (consentStatus) {
                        ConsentStatus.PERSONALIZED -> {
                        }
                        ConsentStatus.NON_PERSONALIZED -> {
                            Log.e("Log", consentStatus.toString())
                            val extras = Bundle()
                            extras.putString("npa", "1")
                            val request = AdRequest.Builder()
                                    .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                                    .build()
                        }
                        ConsentStatus.UNKNOWN -> {
                            consentForm = makeConsentForm(this@LawnchairLauncher)
                            consentForm.load()
                        }
                        else -> {
                            consentForm = makeConsentForm(this@LawnchairLauncher)
                            consentForm.load()
                        }
                    }
                }
            }

            override fun onFailedToUpdateConsentInfo(errorDescription: String) {
                // User's consent status failed to update.
                //            Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_SHORT).show();
            }
        })
    }

    private fun makeConsentForm(context: Context): ConsentForm {
        var privacyUrl: URL? = null
        try {
            privacyUrl = URL("http://pkulria.wixsite.com/nkart/single-post/2013/05/01/This-is-the-title-of-your-first-image-post")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return ConsentForm.Builder(context, privacyUrl)
                .withListener(object : ConsentFormListener() {
                    override fun onConsentFormLoaded() {
                        //                    Toast.makeText(getApplicationContext(), "onConsentFormLoaded", Toast.LENGTH_SHORT).show();
                        consentForm.show()
                    }

                    override fun onConsentFormOpened() {
                        //                    Toast.makeText(getApplicationContext(), "onConsentFormOpened", Toast.LENGTH_SHORT).show();
                    }

                    override fun onConsentFormClosed(consentStatus: ConsentStatus, userPrefersAdFree: Boolean) {
                        //    if (consentStatus== NON_PERSONALIZED)
                        when (consentStatus) {
                            ConsentStatus.PERSONALIZED -> {
                            }
                            ConsentStatus.NON_PERSONALIZED -> {
                                val extras = Bundle()
                                extras.putString("npa", "1")
                                val request = AdRequest.Builder()
                                        .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
                                        .build()
                            }
                            ConsentStatus.UNKNOWN -> {
                            }
                            else -> {
                            }
                        }
                    }

                    override fun onConsentFormError(errorDescription: String) {
                        Log.e("Log", errorDescription)
                        //                    Toast.makeText(getApplicationContext(),errorDescription,Toast.LENGTH_LONG).show();
                    }
                })
                .withNonPersonalizedAdsOption()
                .withPersonalizedAdsOption()
                .build()
    }

    companion object {
        private const val FLAG_RECREATE = 1 shl 0
        private const val FLAG_RESTART = 1 shl 1

        var sRestartFlags = 0

        val instance get() = LauncherAppState.getInstanceNoCreate()?.launcher as? LawnchairLauncher
    }
}

val Context.launcher: LawnchairLauncher
    get() = BaseActivity.fromContext(this)
