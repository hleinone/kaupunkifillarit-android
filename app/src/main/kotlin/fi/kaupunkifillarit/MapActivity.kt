package fi.kaupunkifillarit

import android.Manifest
import android.app.ActivityManager
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.ActivityCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.text.method.LinkMovementMethod
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.bluelinelabs.logansquare.LoganSquare
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.ShareEvent
import com.google.android.gms.analytics.Tracker
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding.view.RxView
import com.trello.rxlifecycle.kotlin.bindToLifecycle
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
import fi.kaupunkifillarit.util.rx_object
import kotlinx.android.synthetic.main.activity_map.*
import pl.charmas.android.reactivelocation.observables.location.LastKnownLocationObservable
import rx.Observable
import rx.Observer
import rx.android.schedulers.AndroidSchedulers
import timber.log.Timber
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MapActivity : BaseActivity() {
    private var map: Maps.MapWrapper<Maps.MarkerWrapper<*>, Maps.MarkerOptionsWrapper<*>>? = null
    private var mapLocation: MapLocation? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    @Inject
    lateinit var tracker: Tracker

    @Inject
    lateinit var answers: Answers

    private var mapInitialized = false
    private var mapMoved = false
    private var touching = false

    private val rackMarkers = mutableMapOf<String, RackMarker>()

    private val lastLocationObserver = object : Observer<MapLocation> {
        override fun onCompleted() {
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
        override fun onCompleted() {
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

    val myLocationButton: View?
        get() = findViewById(MY_LOCATION_BUTTON_ID)

    lateinit private var feedbackForm: FeedbackForm
    lateinit private var usageLogger: UsageLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        (application as KaupunkifillaritApplication).component.inject(this)
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
        drawer.setDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
            }

            override fun onDrawerOpened(drawerView: View) {
                tracker.send(InfoDrawerEvents.open())
            }

            override fun onDrawerClosed(drawerView: View) {
                tracker.send(InfoDrawerEvents.close())
                if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
                    if (shouldRequestLocationPermission()) {
                        requestLocationPermissions()
                    }
                }
            }

            override fun onDrawerStateChanged(newState: Int) {
            }
        })
        if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //noinspection MissingPermission
                    map?.myLocationEnabled = true
                    tracker.send(LocationPermissionsEvents.granted())
                    LastKnownLocationObservable.createObservable(this)
                            .map { location ->
                                MapLocation(location.latitude, location.longitude, DEFAULT_ZOOM_LEVEL, 0f, 0f)
                            }
                            .bindToLifecycle(this)
                            .subscribe { onNext -> map?.animateToMapLocation(onNext) }
                } else {
                    tracker.send(LocationPermissionsEvents.denied())
                }
                sharedPreferences.edit().putBoolean(FIRST_RUN, false).apply()
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
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)
        if (resultCode != ConnectionResult.SUCCESS) {
            val mapFragment = fragmentManager.findFragmentById(R.id.map) as CustomMapFragment
            //noinspection ConstantConditions
            mapFragment.view!!.setPadding(0, statusBarHeight, 0, 0)
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.showErrorNotification(this, resultCode)
            } else {
                tracker.send(ErrorEvents.playServicesError(googleApiAvailability.getErrorString(resultCode)))
            }
        }
    }

    private fun subscribeEverything() {
        Observable.concat(
                sharedPreferences.rx_object(LAST_MAP_LOCATION, MapLocation::class.java),
                LastKnownLocationObservable.createObservable(this).map { location -> MapLocation(location.latitude, location.longitude, DEFAULT_ZOOM_LEVEL, 0f, 0f) },
                Observable.just(DEFAULT_MAP_LOCATION).delay(200, TimeUnit.MILLISECONDS))
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe(lastLocationObserver)

        RackSetObservable.racks
                .observeOn(AndroidSchedulers.mainThread())
                .bindToLifecycle(this)
                .subscribe(racksObserver)
        RxView.clicks(close_info_drawer)
                .bindToLifecycle(this)
                .subscribe { onClickEvent -> drawer.closeDrawer(GravityCompat.END) }
        RxView.clicks(share)
                .bindToLifecycle(this)
                .subscribe { onClickEvent ->
                    answers.logShare(ShareEvent())
                    tracker.send(InfoDrawerEvents.shareClick())
                    val share = Intent(Intent.ACTION_SEND)
                    share.type = "text/plain"
                    share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=fi.kaupunkifillarit")
                    startActivity(Intent.createChooser(share, null))
                }
        RxView.clicks(info_open_source_licenses)
                .bindToLifecycle(this)
                .subscribe { onClickEvent ->
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
            sharedPreferences.edit().putString(LAST_MAP_LOCATION, json).apply()
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
                mapFragment.setOnTouchListener(View.OnTouchListener { view, motionEvent ->
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
        if (!mapMoved && mapLocation?.isWithinDesiredMapBounds ?: false) {
            map.animateToMapLocation(mapLocation!!)
        } else if (!mapMoved) {
            map.animateToMapLocation(DEFAULT_MAP_LOCATION)
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
                tracker.send(MapsEvents.myLocationClick())
                return false
            }
        })
        updateMapPadding()
    }

    private inner class FeedbackForm {
        fun start() {
            val feedBackAnswer = this@MapActivity.sharedPreferences.getInt(FEEDBACK_ANSWER, 0)

            if (shouldDisplayDialog(feedBackAnswer)) {
                val fm = fragmentManager
                val fragment = fm.findFragmentByTag(FEEDBACK_DIALOG_TAG)
                val ft = fm.beginTransaction()
                if (fragment != null) {
                    ft.remove(fragment)
                }
                FeedbackDialogFragment().show(ft, FEEDBACK_DIALOG_TAG)
                this@MapActivity.tracker.send(FeedbackEvents.showDialog())
            }
        }

        private fun shouldDisplayDialog(feedbackAnswer: Int): Boolean {
            when (feedbackAnswer) {
                DialogInterface.BUTTON_POSITIVE, DialogInterface.BUTTON_NEGATIVE -> return false
                DialogInterface.BUTTON_NEUTRAL -> return true
                else -> {
                    val useCount = this@MapActivity.sharedPreferences.getLong(USE_COUNT, 0)
                    if (useCount < 6) {
                        return false
                    }
                    val lastUsed = this@MapActivity.sharedPreferences.getLong(LAST_USED, 0)
                    val timeSinceLastUse = System.currentTimeMillis() - lastUsed

                    return timeSinceLastUse >= TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS) && timeSinceLastUse < TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS)
                }
            }
        }
    }

    class FeedbackDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val positiveClick = { dialog: DialogInterface, which: Int ->
                (activity as MapActivity).sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_POSITIVE).apply()
                (activity as MapActivity).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_POSITIVE))
                activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=fi.kaupunkifillarit")))
            }
            val neutralClick = { dialog: DialogInterface, which: Int ->
                (activity as MapActivity).sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEUTRAL).apply()
                (activity as MapActivity).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEUTRAL))
            }
            val negativeClick = { dialog: DialogInterface, which: Int ->
                (activity as MapActivity).sharedPreferences.edit().putInt(MapActivity.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEGATIVE).apply()
                (activity as MapActivity).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEGATIVE))
            }
            return android.support.v7.app.AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog)
                    .setTitle(R.string.rating_request_title)
                    .setMessage(R.string.rating_request_message)
                    .setPositiveButton(R.string.rating_request_rate, positiveClick)
                    .setNeutralButton(R.string.rating_request_later, neutralClick)
                    .setNegativeButton(R.string.rating_request_dont_remind, negativeClick)
                    .create()
        }
    }

    private inner class UsageLogger {
        fun start() {
            this@MapActivity.sharedPreferences.edit()
                    .putLong(LAST_USED, System.currentTimeMillis())
                    .putLong(USE_COUNT, this@MapActivity.sharedPreferences.getLong(USE_COUNT, 0) + 1)
                    .apply()
        }
    }

    class LocationPermissionRationaleDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val positiveClick = { dialog: DialogInterface, which: Int ->
                ActivityCompat.requestPermissions(activity,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), MY_PERMISSIONS_REQUEST_LOCATION)
            }

            val dialog = android.support.v7.app.AlertDialog.Builder(activity, R.style.AppTheme_AlertDialog)
                    .setTitle(R.string.location_permission_rationale_title)
                    .setMessage(R.string.location_permission_rationale_message)
                    .setPositiveButton(R.string.location_permission_rationale_ok, positiveClick)
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
        @IdRes
        const val MY_LOCATION_BUTTON_ID = 0x2
        private val MY_PERMISSIONS_REQUEST_LOCATION = 1

        private val FEEDBACK_ANSWER = "feedback_answer"

        private val LAST_USED = "last_used"
        private val USE_COUNT = "use_count"

        private val FEEDBACK_DIALOG_TAG = FeedbackDialogFragment::class.java.name
        private val LOCATION_PERMISSION_RATIONALE_DIALOG_TAG = LocationPermissionRationaleDialogFragment::class.java.name
    }
}
