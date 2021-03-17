package fi.kaupunkifillarit

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.*
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.coroutineScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import de.psdev.licensesdialog.LicensesDialog
import fi.kaupunkifillarit.databinding.ActivityMapsBinding
import fi.kaupunkifillarit.maps.rackMarker
import fi.kaupunkifillarit.model.MapLocation
import fi.kaupunkifillarit.rx.Api
import fi.kaupunkifillarit.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapsBinding

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            onPermissionResult(it)
        }

    @SuppressLint("MissingPermission")
    @ExperimentalCoroutinesApi
    @FlowPreview
    @ObsoleteCoroutinesApi
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
        if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
            binding.drawer.addDrawerListener(object : DrawerLayout.SimpleDrawerListener() {
                override fun onDrawerClosed(drawerView: View) {
                    val listener = this
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                        app.sharedPreferences.getBoolean(FIRST_RUN, true)
                    ) {
                        val ft = supportFragmentManager.findFragmentByTag(
                            LOCATION_PERMISSION_RATIONALE_DIALOG_TAG
                        )?.let {
                            val ft = supportFragmentManager.beginTransaction()
                            ft.remove(it)
                        } ?: supportFragmentManager.beginTransaction()

                        LocationPermissionRationaleDialogFragment().apply {
                            onPositiveButtonClickListener = { _ ->
                                app.sharedPreferences.edit().putBoolean(FIRST_RUN, false)
                                    .apply()
                                binding.drawer.removeDrawerListener(listener)
                                checkAndRequestPermissions()
                            }
                        }.show(ft, LOCATION_PERMISSION_RATIONALE_DIALOG_TAG)
                    } else {
                        app.sharedPreferences.edit().putBoolean(FIRST_RUN, false)
                            .apply()
                        binding.drawer.removeDrawerListener(listener)
                        checkAndRequestPermissions()
                    }
                }
            })
            binding.drawer.openDrawer(GravityCompat.END)
        } else {
            checkAndRequestPermissions()
        }

        binding.closeInfoDrawer.setOnClickListener {
            binding.drawer.closeDrawer(GravityCompat.END)
        }

        binding.share.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=fi.kaupunkifillarit"
                )
            }
            startActivity(Intent.createChooser(shareIntent, null))
        }

        binding.infoOpenSourceLicenses.setOnClickListener {
            LicensesDialog.Builder(this)
                .setThemeResourceId(R.style.AppTheme_Dialog_Alert)
                .setTitle(R.string.open_source_licenses)
                .setCloseText(R.string.close)
                .setNotices(R.raw.notices)
                .build()
                .show()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenCreated {
            val googleMap = mapFragment.awaitMap()
            val style = MapStyleOptions.loadRawResourceStyle(
                applicationContext,
                R.raw.map_style
            )
            googleMap.setMapStyle(style)
            googleMap.isTrafficEnabled = false
            googleMap.isIndoorEnabled = false
            googleMap.uiSettings.isMapToolbarEnabled = false
            googleMap.uiSettings.isZoomControlsEnabled = false

            googleMap.setOnMyLocationButtonClickListener {
                app.sharedPreferences.edit().remove(LAST_MAP_LOCATION).apply()
                false
            }

            if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
                googleMap.moveCamera(DEFAULT_MAP_LOCATION.let {
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(it.latitude, it.longitude))
                            .zoom(5f)
                            .bearing(it.bearing)
                            .tilt(it.tilt)
                            .build()
                    )
                })
            }

            launch {
                Api.racks().collect { racks ->
                    googleMap.clear()
                    racks.forEach { rack ->
                        googleMap.addMarker(rackMarker(rack, this@MapsActivity))
                    }
                }
            }

            launch {
                googleMap.cameraMoveStarted().filter {
                    it == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE
                }.flatMapLatest {
                    flowOf(googleMap.cameraIdle().first())
                }.collect {
                    val lastMapLocation = MapLocation(
                        googleMap.cameraPosition.target.latitude,
                        googleMap.cameraPosition.target.longitude,
                        googleMap.cameraPosition.zoom,
                        googleMap.cameraPosition.bearing,
                        googleMap.cameraPosition.tilt
                    )
                    app.sharedPreferences.edit()
                        .putObject(LAST_MAP_LOCATION, MapLocation.serializer(), lastMapLocation)
                        .apply()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyInsets()
        checkGooglePlayServices()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        applyInsets()
    }

    private fun applyInsets() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        ViewCompat.setOnApplyWindowInsetsListener(mapFragment.requireView()) { v, insets ->
            val buttonsContainer =
                v.findViewWithTag<View>("GoogleMapMyLocationButton")?.parent as? ViewGroup
            buttonsContainer?.setPadding(
                insets.mandatorySystemGestureInsets.left,
                insets.mandatorySystemGestureInsets.top,
                insets.mandatorySystemGestureInsets.right,
                insets.mandatorySystemGestureInsets.bottom
            )
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.drawer) { v, insets ->
            v.layoutParams = (v.layoutParams as FrameLayout.LayoutParams).apply {
                rightMargin = insets.mandatorySystemGestureInsets.right
            }
            insets.inset(0, 0, insets.mandatorySystemGestureInsets.right, 0)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.leftDrawer) { v, insets ->
            v.setPadding(
                0,
                insets.systemWindowInsetTop,
                insets.systemWindowInsetRight,
                0
            )
            insets
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.infoContent) { v, insets ->
            v.setPadding(
                0,
                0,
                0,
                insets.systemWindowInsetBottom + resources.getDimensionPixelSize(R.dimen.info_view_vertical_margin)
            )
            insets
        }
    }

    private fun checkGooglePlayServices() {
        val resultCode = app.googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS &&
            app.googleApiAvailability.isUserResolvableError(resultCode)
        ) {
            app.googleApiAvailability.showErrorNotification(this, resultCode)
        }
    }

    private fun checkAndRequestPermissions() {
        when (ContextCompat.checkSelfPermission(
            this@MapsActivity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )) {
            PackageManager.PERMISSION_GRANTED -> {
                onPermissionResult(true)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun onPermissionResult(isGranted: Boolean) {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        lifecycle.coroutineScope.launchWhenResumed {
            val googleMap = mapFragment.awaitMap()
            googleMap.isMyLocationEnabled = isGranted
            val lastMapLocation =
                app.sharedPreferences.getObject(LAST_MAP_LOCATION, MapLocation.serializer(), null)
            val cameraUpdate =
                (lastMapLocation
                    ?: (if (isGranted)
                        app.locationProviderClient.awaitLastLocation()?.let {
                            MapLocation(
                                it.latitude,
                                it.longitude,
                                MY_LOCATION_ZOOM_LEVEL,
                                0f,
                                0f
                            )
                        } else null)
                    ?: DEFAULT_MAP_LOCATION).let {
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(it.latitude, it.longitude))
                            .zoom(it.zoom)
                            .bearing(it.bearing)
                            .tilt(it.tilt)
                            .build()
                    )
                }
            googleMap.animateCamera(cameraUpdate)
        }
    }

    companion object {
        private const val FIRST_RUN = "first_run"
        private const val LAST_MAP_LOCATION = "last_map_location"
        private const val LOCATION_PERMISSION_RATIONALE_DIALOG_TAG =
            "location_permission_rationale_dialog"
        private const val MY_LOCATION_ZOOM_LEVEL = 15f
        private val FINLAND by lazy { LatLng(61.924100, 25.748200) }
        private val DEFAULT_MAP_LOCATION by lazy {
            MapLocation(
                FINLAND.latitude, FINLAND.longitude, 5f, 0f, 0f
            )
        }
    }
}