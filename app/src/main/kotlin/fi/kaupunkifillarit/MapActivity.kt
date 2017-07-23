package fi.kaupunkifillarit

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.bluelinelabs.logansquare.LoganSquare
import com.crashlytics.android.answers.ShareEvent
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.support.v4.widget.drawerOpen
import com.jakewharton.rxbinding2.view.clicks
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import de.psdev.licensesdialog.LicensesDialog
import fi.kaupunkifillarit.analytics.*
import fi.kaupunkifillarit.maps.GoogleMaps
import fi.kaupunkifillarit.maps.Maps
import fi.kaupunkifillarit.maps.RackMarker
import fi.kaupunkifillarit.maps.RackMarkerOptions
import fi.kaupunkifillarit.model.MapLocation
import fi.kaupunkifillarit.model.Rack
import fi.kaupunkifillarit.rx.RackSetObservable
import fi.kaupunkifillarit.util.map
import fi.kaupunkifillarit.util.rx_getObject
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_map.*
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit

class MapActivity : BaseActivity() {
    private var map: Maps.MapWrapper<Maps.MarkerWrapper<*>, Maps.MarkerOptionsWrapper<*>>? = null
    private var mapLocation: MapLocation? = null

    private var mapInitialized = false
    private var mapMoved = false
    private var touching = false

    private val rackMarkers = mutableMapOf<String, RackMarker>()

    private val lastLocationObserver = object : Observer<MapLocation> {
        override fun onSubscribe(p0: Disposable?) {
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            Timber.e(e, "Location fetching failed")
            if (!mapMoved) {
                map?.animateToMapLocation(DEFAULT_MAP_LOCATION)
            }
        }

        override fun onNext(mapLocation: MapLocation) {
            if (this@MapActivity.mapLocation == null && !mapMoved && mapLocation.isWithinDesiredMapBounds) {
                this@MapActivity.mapLocation = mapLocation
                map?.animateToMapLocation(mapLocation)
            }
        }
    }

    private val racksObserver = object : Observer<Set<Rack>> {
        override fun onSubscribe(p0: Disposable?) {
        }

        override fun onComplete() {
        }

        override fun onError(e: Throwable) {
            Timber.e(e, "Rack retrieval failed")
        }

        override fun onNext(update: Set<Rack>) {
            if (touching) {
                return
            }

            synchronized(this@MapActivity) {
                for (rack in update) {
                    if (rackMarkers.contains(rack.id)) {
                        rackMarkers[rack.id]?.update(rack)
                    } else {
                        rackMarkers[rack.id] = RackMarkerOptions(rack,
                                resources, map!!).makeMarker(map!!)
                    }
                }
            }
        }
    }

    lateinit private var feedbackForm: FeedbackForm
    lateinit private var usageLogger: UsageLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        MapsInitializer.initialize(application)
        @Suppress("DEPRECATION")
        val primaryDark = resources.getColor(R.color.primary_dark)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(ActivityManager.TaskDescription(getString(R.string.app_name),
                    BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                    primaryDark))
        }
        feedbackForm = FeedbackForm()
        usageLogger = UsageLogger()
        setUpInfo()
    }

    private fun setUpInfo() {
        info_description.movementMethod = LinkMovementMethod.getInstance()
        @Suppress("DEPRECATION")
        drawer.drawerOpen(GravityCompat.END).bindToLifecycle(this).subscribe { open ->
            if (open) {
                app.tracker.send(InfoDrawerEvents.open())
            } else {
                app.tracker.send(InfoDrawerEvents.close())
                if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
                    if (shouldRequestLocationPermission()) {
                        requestLocationPermissions()
                    }
                }
            }
        }
        if (app.sharedPreferences.getBoolean(FIRST_RUN, true)) {
            drawer.openDrawer(GravityCompat.END)
        }
    }

    private fun shouldRequestLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) && !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            val fm = fragmentManager
            val fragment = fm.findFragmentByTag(LOCATION_PERMISSION_RATIONALE_DIALOG_TAG)
            val ft = fm.beginTransaction()
            if (fragment != null) {
                ft.remove(fragment)
            }
            LocationPermissionRationaleDialogFragment().show(ft, LOCATION_PERMISSION_RATIONALE_DIALOG_TAG)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults.first() == PackageManager.PERMISSION_GRANTED) {
                    //noinspection MissingPermission
                    map?.myLocationEnabled = true
                    app.tracker.send(LocationPermissionsEvents.granted())
                    app.rxLocation.location().lastLocation()
                            .map { location ->
                                val mapLocation = MapLocation(location.latitude, location.longitude, DEFAULT_ZOOM_LEVEL, 0f, 0f)
                                if (mapLocation.isWithinDesiredMapBounds) mapLocation else DEFAULT_MAP_LOCATION
                            }
                            .bindToLifecycle(this)
                            .subscribe { onNext -> map?.animateToMapLocation(onNext) }
                } else {
                    app.tracker.send(LocationPermissionsEvents.denied())
                }
                app.sharedPreferences.edit().putBoolean(FIRST_RUN, false).apply()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        feedbackForm.start()
        usageLogger.start()
    }

    override fun onResume() {
        super.onResume()
        checkGooglePlayServices()
        setUpMapIfNeeded()
        subscribeEverything()
        updateInfoViewPadding()
        updateContentPadding()
    }

    private fun checkGooglePlayServices() {
        val resultCode = app.googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            val mapFragment = fragmentManager.findFragmentById(R.id.map) as CustomMapFragment
            //noinspection ConstantConditions
            mapFragment.view!!.setPadding(0, statusBarHeight, 0, 0)
            if (app.googleApiAvailability.isUserResolvableError(resultCode)) {
                app.googleApiAvailability.showErrorNotification(this, resultCode)
            } else {
                app.tracker.send(ErrorEvents.playServicesError(app.googleApiAvailability.getErrorString(resultCode)))
            }
        }
    }

    private fun subscribeEverything() {
        Observable.concat(
                app.sharedPreferences.rx_getObject(LAST_MAP_LOCATION, MapLocation::class.java, null),
                app.rxLocation.location().lastLocation().toObservable().map { location -> MapLocation(location.latitude, location.longitude, DEFAULT_ZOOM_LEVEL, 0f, 0f) },
                Observable.just(DEFAULT_MAP_LOCATION).delay(200, TimeUnit.MILLISECONDS))
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe(lastLocationObserver)

        RackSetObservable.racks
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe(racksObserver)
        close_info_drawer.clicks()
                .bindToLifecycle(this)
                .subscribe { _ -> drawer.closeDrawer(GravityCompat.END) }
        share.clicks()
                .bindToLifecycle(this)
                .subscribe { _ ->
                    app.answers.logShare(ShareEvent())
                    app.tracker.send(InfoDrawerEvents.shareClick())
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "text/plain"
                    share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=fi.kaupunkifillarit")
                    startActivity(Intent.createChooser(share, null))
                }
        info_open_source_licenses.clicks()
                .bindToLifecycle(this)
                .subscribe { _ ->
                    LicensesDialog.Builder(this)
                            .setTitle(R.string.open_source_licenses)
                            .setCloseText(R.string.close)
                            .setNotices(R.raw.notices)
                            .build()
                            .show()
                }
    }

    override fun onPause() {
        try {
            val json = mapLocation.map { LoganSquare.serialize(it) }
            app.sharedPreferences.edit().putString(LAST_MAP_LOCATION, json).apply()
        } catch (e: IOException) {
            Timber.w(e, "Storing location failed")
        }

        super.onPause()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateMapPadding()
        updateInfoViewPadding()
        updateContentPadding()
    }

    private fun setUpMapIfNeeded() {
        if (map == null) {
            val mapFragment = fragmentManager.findFragmentById(R.id.map) as CustomMapFragment
            GoogleMaps.create(mapFragment).subscribe { onNext: Maps.MapWrapper<*, *> ->
                @Suppress("UNCHECKED_CAST")
                val next = onNext as Maps.MapWrapper<Maps.MarkerWrapper<*>, Maps.MarkerOptionsWrapper<*>>
                this@MapActivity.map = next
                setUpMap()
                mapFragment.setOnTouchListener(View.OnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> touching = false
                        else -> touching = true
                    }
                    false
                })
            }
        }
    }

    val statusBarHeight: Int
        get() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    return resources.getDimensionPixelSize(resourceId)
                }
            }
            return 0
        }

    val navigationBarHeight: Int
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            val usableHeight = metrics.heightPixels
            windowManager.defaultDisplay.getRealMetrics(metrics)
            val realHeight = metrics.heightPixels
            val statusBarHeight = statusBarHeight
            if (realHeight - statusBarHeight > usableHeight) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
                    if (resourceId > 0) {
                        return resources.getDimensionPixelSize(resourceId)
                    }
                }
                return 0
            }
            return 0
        }

    val navigationBarWidth: Int
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

    private fun updateMapPadding() {
        map?.setPadding(0, statusBarHeight, 0, 0)
    }

    private fun updateInfoViewPadding() {
        val logoParams = logo.layoutParams as RelativeLayout.LayoutParams
        logoParams.topMargin = statusBarHeight + resources.getDimensionPixelSize(R.dimen.info_view_vertical_margin)
        logo.layoutParams = logoParams
        val closeInfoDrawerParams = close_info_drawer.layoutParams as RelativeLayout.LayoutParams
        closeInfoDrawerParams.topMargin = statusBarHeight
        close_info_drawer.layoutParams = closeInfoDrawerParams
        info_content.setPadding(info_content.paddingLeft, info_content.paddingTop, info_content.paddingRight, navigationBarHeight + resources.getDimensionPixelSize(R.dimen.info_view_vertical_margin))
        val drawerParams = drawer.layoutParams as FrameLayout.LayoutParams
        drawerParams.marginStart = 0
        drawerParams.marginEnd = navigationBarWidth
        drawer.layoutParams = drawerParams
    }

    private fun updateContentPadding() {
        val contentParams = content.layoutParams as FrameLayout.LayoutParams
        contentParams.topMargin = statusBarHeight
        contentParams.bottomMargin = navigationBarHeight
        content.layoutParams = contentParams
    }

    private fun setUpMap() {
        val map = this.map!!

        if (!shouldRequestLocationPermission()) {
            //noinspection MissingPermission
            map.myLocationEnabled = true
        }
        map.trafficEnabled = false
        map.setOnMarkerClickListener(object : Maps.OnMarkerClickListener {
            override fun onMarkerClick(marker: Maps.MarkerWrapper<*>): Boolean {
                return true
            }
        })
        if (!mapMoved) {
            map.animateToMapLocation(if (mapLocation?.isWithinDesiredMapBounds ?: false) mapLocation!! else DEFAULT_MAP_LOCATION)
        }
        map.setOnMapLocationChangeListener(object : Maps.OnMapLocationChangeListener {
            override fun onMapLocationChange(mapLocation: MapLocation) {
                if (!mapInitialized) {
                    mapInitialized = true
                } else {
                    mapMoved = true
                    if (this@MapActivity.mapLocation == null) {
                        this@MapActivity.mapLocation = mapLocation
                    } else {
                        val oldMapLocation = this@MapActivity.mapLocation!!
                        oldMapLocation.latitude = mapLocation.latitude
                        oldMapLocation.longitude = mapLocation.longitude
                        oldMapLocation.zoom = mapLocation.zoom
                        oldMapLocation.bearing = mapLocation.bearing
                        oldMapLocation.tilt = mapLocation.tilt
                    }
                }
            }
        })
        map.setOnMyLocationButtonClickListener(object : Maps.OnMyLocationButtonClickListener {
            override fun onMyLocationButtonClick(): Boolean {
                app.tracker.send(MapsEvents.myLocationClick())
                return false
            }
        })
        updateMapPadding()
    }

    private inner class FeedbackForm {
        fun start() {
            val feedBackAnswer = this@MapActivity.app.sharedPreferences.getInt(FEEDBACK_ANSWER, 0)

            if (shouldDisplayDialog(feedBackAnswer)) {
                val fm = fragmentManager
                val fragment = fm.findFragmentByTag(FEEDBACK_DIALOG_TAG)
                val ft = fm.beginTransaction()
                if (fragment != null) {
                    ft.remove(fragment)
                }
                FeedbackDialogFragment().show(ft, FEEDBACK_DIALOG_TAG)
                this@MapActivity.app.tracker.send(FeedbackEvents.showDialog())
            }
        }

        private fun shouldDisplayDialog(feedbackAnswer: Int): Boolean {
            when (feedbackAnswer) {
                DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE -> return false
                DialogInterface.BUTTON_NEUTRAL -> return true
                else -> {
                    val useCount = this@MapActivity.app.sharedPreferences.getLong(USE_COUNT, 0)
                    if (useCount < 6) {
                        return false
                    }
                    val lastUsed = this@MapActivity.app.sharedPreferences.getLong(LAST_USED, 0)
                    val timeSinceLastUse = System.currentTimeMillis() - lastUsed

                    return timeSinceLastUse >= TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS) && timeSinceLastUse < TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS)
                }
            }
        }
    }

    class FeedbackDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return android.support.v7.app.AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog)
                    .setTitle(R.string.rating_request_title)
                    .setMessage(R.string.rating_request_message)
                    .setPositiveButton(R.string.rating_request_rate, { _, _ ->
                        (activity as MapActivity).app.sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_POSITIVE).apply()
                        (activity as MapActivity).app.tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_POSITIVE))
                        activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=fi.kaupunkifillarit")))
                    })
                    .setNeutralButton(R.string.rating_request_later, { _, _ ->
                        (activity as MapActivity).app.sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEUTRAL).apply()
                        (activity as MapActivity).app.tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEUTRAL))
                    })
                    .setNegativeButton(R.string.rating_request_dont_remind, { _, _ ->
                        (activity as MapActivity).app.sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEGATIVE).apply()
                        (activity as MapActivity).app.tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEGATIVE))
                    })
                    .create()
        }
    }

    private inner class UsageLogger {
        fun start() {
            this@MapActivity.app.sharedPreferences.edit()
                    .putLong(LAST_USED, System.currentTimeMillis())
                    .putLong(USE_COUNT, this@MapActivity.app.sharedPreferences.getLong(USE_COUNT, 0) + 1)
                    .apply()
        }
    }

    class LocationPermissionRationaleDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val dialog = android.support.v7.app.AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog)
                    .setTitle(R.string.location_permission_rationale_title)
                    .setMessage(R.string.location_permission_rationale_message)
                    .setPositiveButton(R.string.location_permission_rationale_ok, { _, _ ->
                        ActivityCompat.requestPermissions(activity,
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
                    })
                    .create()

            isCancelable = false
            dialog.setCanceledOnTouchOutside(false)
            return dialog
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            val view = super.onCreateView(inflater, container, savedInstanceState)
            isCancelable = false
            return view
        }
    }

    companion object {
        private val LAST_MAP_LOCATION = "last_map_location"
        private val FIRST_RUN = "first_run"
        private val HELSINKI = LatLng(60.173324, 24.9410248)
        private val DEFAULT_ZOOM_LEVEL = 15f
        private val DEFAULT_MAP_LOCATION = MapLocation(
                HELSINKI.latitude, HELSINKI.longitude, DEFAULT_ZOOM_LEVEL, 0f, 0f)

        private val MY_PERMISSIONS_REQUEST_LOCATION = 1

        private val FEEDBACK_ANSWER = "feedback_answer"

        private val LAST_USED = "last_used"
        private val USE_COUNT = "use_count"

        private val FEEDBACK_DIALOG_TAG = FeedbackDialogFragment::class.java.name
        private val LOCATION_PERMISSION_RATIONALE_DIALOG_TAG = LocationPermissionRationaleDialogFragment::class.java.name
    }
}
