package fi.kaupunkifillarit

import android.Manifest
import android.app.ActivityManager
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.ktx.awaitMap
import fi.kaupunkifillarit.databinding.ActivityMapsBinding
import fi.kaupunkifillarit.maps.rackMarker
import fi.kaupunkifillarit.rx.Api
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.InternalSerializationApi

class MapsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapsBinding

    private val statusBarHeight: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    return resources.getDimensionPixelSize(resourceId)
                }
            }
            return 0
        }

    private val navigationBarHeight: Int
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            val statusBarHeight = statusBarHeight
            if (realHeight - statusBarHeight > usableHeight) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val resourceId =
                        resources.getIdentifier("navigation_bar_height", "dimen", "android")
                    if (resourceId > 0) {
                        return resources.getDimensionPixelSize(resourceId)
                    }
                }
                return 0
            }
            return 0
        }

    private val navigationBarWidth: Int
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val usableWidth = metrics.widthPixels
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val realWidth = metrics.widthPixels
            if (realWidth > usableWidth) {
                return realWidth - usableWidth
            }
            return 0
        }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {

            } else {

            }
        }

    @FlowPreview
    @ObsoleteCoroutinesApi
    @InternalSerializationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        MapsInitializer.initialize(this)
        val primaryDark = ActivityCompat.getColor(this, R.color.primary_dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            @Suppress("DEPRECATION")
            setTaskDescription(
                ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                    primaryDark
                )
            )
        }

        binding.infoDescription.movementMethod = LinkMovementMethod.getInstance()
        binding.drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
            override fun onDrawerClosed(drawerView: View) {
                if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
                    when {
                        ContextCompat.checkSelfPermission(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            Log.d("FOO", "permissions granted")
                        }
                        ActivityCompat.shouldShowRequestPermissionRationale(
                            this@MapsActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) -> {
                            val fragment = supportFragmentManager.findFragmentByTag(
                                LOCATION_PERMISSION_RATIONALE_DIALOG_TAG
                            )
                            val ft = supportFragmentManager.beginTransaction()
                            if (fragment != null) {
                                ft.remove(fragment)
                            }
                            LocationPermissionRationaleDialogFragment()
                                .show(ft, LOCATION_PERMISSION_RATIONALE_DIALOG_TAG)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }
                }
            }
        })
        if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
            binding.drawer.openDrawer(GravityCompat.END)
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            val googleMap = mapFragment.awaitMap()
            val style = MapStyleOptions.loadRawResourceStyle(
                applicationContext,
                R.raw.map_style
            )
            googleMap.setMapStyle(style)

            Api.racks().collect { racks ->
                googleMap.clear()
                racks.forEach { rack ->
                    googleMap.addMarker(rackMarker(rack, this@MapsActivity))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayServices()
        updateInfoViewPadding()
        updateContentPadding()
    }

    private fun checkGooglePlayServices() {
        val resultCode = app.googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {

            val mapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.requireView().setPadding(0, statusBarHeight, 0, 0)
            if (app.googleApiAvailability.isUserResolvableError(resultCode)) {
                app.googleApiAvailability.showErrorNotification(this, resultCode)
            }
        }
    }

    private fun updateMapPadding() {
        binding.map.setPadding(0, statusBarHeight, 0, 0)
    }

    private fun updateInfoViewPadding() {
        binding.logo.layoutParams =
            (binding.logo.layoutParams as RelativeLayout.LayoutParams).apply {
                topMargin =
                    statusBarHeight + resources.getDimensionPixelSize(R.dimen.info_view_vertical_margin)
            }
        binding.closeInfoDrawer.layoutParams =
            (binding.closeInfoDrawer.layoutParams as RelativeLayout.LayoutParams).apply {
                topMargin = statusBarHeight
            }
        binding.infoContent.setPadding(
            binding.infoContent.paddingLeft,
            binding.infoContent.paddingTop,
            binding.infoContent.paddingRight,
            navigationBarHeight + resources.getDimensionPixelSize(R.dimen.info_view_vertical_margin)
        )
        binding.drawer.layoutParams =
            (binding.drawer.layoutParams as FrameLayout.LayoutParams).apply {
                marginStart = 0
                marginEnd = navigationBarWidth
            }
    }

    private fun updateContentPadding() {
        binding.content.layoutParams =
            (binding.content.layoutParams as FrameLayout.LayoutParams).apply {
                topMargin = statusBarHeight
                bottomMargin = navigationBarHeight
            }
    }

    companion object {
        const val FIRST_RUN = "first_run"
        const val LOCATION_PERMISSION_RATIONALE_DIALOG_TAG = "location_permission_rationale_dialog"
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}