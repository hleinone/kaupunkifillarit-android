package fi.kaupunkifillarit;

import android.Manifest;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.LayoutDirection;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ShareEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.rxbinding.view.RxView;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.psdev.licensesdialog.LicensesDialog;
import fi.kaupunkifillarit.analytics.ErrorEvents;
import fi.kaupunkifillarit.analytics.FeedbackEvents;
import fi.kaupunkifillarit.analytics.InfoDrawerEvents;
import fi.kaupunkifillarit.analytics.LocationPermissionsEvents;
import fi.kaupunkifillarit.analytics.MapsEvents;
import fi.kaupunkifillarit.maps.GoogleMaps;
import fi.kaupunkifillarit.maps.Maps;
import fi.kaupunkifillarit.maps.RackMarker;
import fi.kaupunkifillarit.maps.RackMarkerOptions;
import fi.kaupunkifillarit.model.MapLocation;
import fi.kaupunkifillarit.model.Rack;
import fi.kaupunkifillarit.rx.JacksonSharedPreferenceObservable;
import fi.kaupunkifillarit.rx.RackSetObservable;
import fj.data.HashMap;
import fj.data.Option;
import fj.data.Set;
import pl.charmas.android.reactivelocation.observables.location.LastKnownLocationObservable;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MapActivity extends BaseActivity {
    private static final String LAST_MAP_LOCATION = "last_map_location";
    private static final String FIRST_RUN = "first_run";
    private static final LatLng HELSINKI = new LatLng(60.173324, 24.9410248);
    private static final float DEFAULT_ZOOM_LEVEL = 15;
    private static final MapLocation DEFAULT_MAP_LOCATION = new MapLocation(
            HELSINKI.latitude, HELSINKI.longitude, DEFAULT_ZOOM_LEVEL, 0, 0);
    @IdRes
    public static final int MY_LOCATION_BUTTON_ID = 0x2;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    private Option<Maps.MapWrapper> map = Option.none();
    private Option<MapLocation> mapLocation = Option.none();

    @Inject
    ObjectMapper objectMapper;

    @Inject
    SharedPreferences sharedPreferences;

    @Inject
    OkHttpClient okHttpClient;

    @Inject
    GoogleApiAvailability googleApiAvailability;

    @Inject
    Tracker tracker;

    @Inject
    Answers answers;

    private boolean mapInitialized = false;
    private boolean mapMoved = false;
    private boolean touching = false;

    private HashMap<String, RackMarker> rackMarkers = HashMap.hashMap();

    private Subscription lastLocationSubscription;
    private final Observer<MapLocation> lastLocationObserver = new Observer<MapLocation>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Location fetching failed");
            MapActivity.this.mapLocation = mapLocation;
            if (!mapMoved && map.isSome()) {
                map.some().animateToMapLocation(DEFAULT_MAP_LOCATION);
            }
        }

        @Override
        public void onNext(MapLocation mapLocation) {
            if (MapActivity.this.mapLocation.isNone()) {
                MapActivity.this.mapLocation = Option.some(mapLocation);
                if (!mapMoved && map.isSome() && mapLocation.isWithinDesiredMapBounds()) {
                    map.some().animateToMapLocation(mapLocation);
                }
            }
        }
    };

    private Subscription racksSubscription;
    private final Observer<Set<Rack>> racksObserver = new Observer<Set<Rack>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Timber.e(e, "Rack retrieval failed");
        }

        @Override
        public void onNext(Set<Rack> update) {
            if (touching) {
                return;
            }

            synchronized (MapActivity.this) {
                for (Rack rack : update) {
                    if (rackMarkers.contains(rack.id)) {
                        rackMarkers.get(rack.id).some().update(rack);
                    } else {
                        rackMarkers.set(rack.id, new RackMarkerOptions(rack,
                                getResources(), map.some()).makeMarker(map.some()));
                    }
                }
            }
        }
    };

    private Subscription closeInfoDrawerClickSubscription;
    private Subscription shareClickSubscription;
    private Subscription infoOpenSourceLicensesClickSubscription;
    private Subscription permissionGrantedSubscription;

    @BindView(R.id.content)
    RelativeLayout content;
    @Nullable
    @BindView(MY_LOCATION_BUTTON_ID)
    View myLocationButton;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    @BindView(R.id.close_info_drawer)
    ImageButton closeInfoDrawer;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.share)
    ImageButton share;
    @BindView(R.id.info_content)
    LinearLayout infoContent;
    @BindView(R.id.info_description)
    TextView infoDescription;
    @BindView(R.id.info_open_source_licenses)
    TextView infoOpenSourceLicenses;

    private FeedbackForm feedbackForm;
    private UsageLogger usageLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        KaupunkifillaritApplication application = KaupunkifillaritApplication.get(this);
        MapsInitializer.initialize(application);
        application.component().inject(this);
        @SuppressWarnings("deprecation")
        int primaryDark = getResources().getColor(R.color.primary_dark);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                    primaryDark));
        }
        feedbackForm = new FeedbackForm();
        usageLogger = new UsageLogger();
        setUpInfo();
    }

    private void setUpInfo() {
        infoDescription.setMovementMethod(LinkMovementMethod.getInstance());
        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                tracker.send(InfoDrawerEvents.open());
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                tracker.send(InfoDrawerEvents.close());
                if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
                    if (shouldRequestLocationPermission()) {
                        requestLocationPermissions();
                    }
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        if (sharedPreferences.getBoolean(FIRST_RUN, true)) {
            drawer.openDrawer(GravityCompat.END);
        }
    }

    private boolean shouldRequestLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermissions() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentByTag(LocationPermissionRationaleDialogFragment.TAG);
            FragmentTransaction ft = fm.beginTransaction();
            if (fragment != null) {
                ft.remove(fragment);
            }
            new LocationPermissionRationaleDialogFragment().show(ft, LocationPermissionRationaleDialogFragment.TAG);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //noinspection MissingPermission
                    map.some().setMyLocationEnabled(true);
                    tracker.send(LocationPermissionsEvents.granted());
                    permissionGrantedSubscription = LastKnownLocationObservable.createObservable(this)
                            .map(location -> new MapLocation(location.getLatitude(), location.getLongitude(), DEFAULT_ZOOM_LEVEL, 0, 0))
                            .subscribe(onNext -> {
                                map.some().animateToMapLocation(onNext);
                            });
                } else {
                    tracker.send(LocationPermissionsEvents.denied());
                }
                sharedPreferences.edit().putBoolean(FIRST_RUN, false).apply();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        feedbackForm.start();
        usageLogger.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayServices();
        setUpMapIfNeeded();
        subscribeEverything();
        updateInfoViewPadding();
        updateContentPadding();
    }

    private void checkGooglePlayServices() {
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            CustomMapFragment mapFragment = (CustomMapFragment) getFragmentManager().findFragmentById(R.id.map);
            //noinspection ConstantConditions
            mapFragment.getView().setPadding(0, obtainStatusBarHeight(), 0, 0);
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.showErrorNotification(this, resultCode);
            } else {
                tracker.send(ErrorEvents.playServicesError(googleApiAvailability.getErrorString(resultCode)));
            }
        }
    }

    private void subscribeEverything() {
        lastLocationSubscription = Observable.concat(
                JacksonSharedPreferenceObservable.createObservable(objectMapper, sharedPreferences, LAST_MAP_LOCATION, MapLocation.class),
                LastKnownLocationObservable.createObservable(this)
                        .map(location -> new MapLocation(location.getLatitude(), location.getLongitude(), DEFAULT_ZOOM_LEVEL, 0, 0)),
                Observable.just(DEFAULT_MAP_LOCATION).delay(200, TimeUnit.MILLISECONDS))
                .take(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lastLocationObserver);
        Request request = new Request.Builder()
                .url("https://kaupunkifillarit.fi/api/stations")
                .get()
                .build();

        racksSubscription = RackSetObservable.createObservable(objectMapper, okHttpClient, request)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(racksObserver);
        closeInfoDrawerClickSubscription = RxView.clicks(closeInfoDrawer).subscribe(onClickEvent -> {
            drawer.closeDrawer(GravityCompat.END);
        });
        shareClickSubscription = RxView.clicks(share).subscribe(onClickEvent -> {
            answers.logShare(new ShareEvent());
            tracker.send(InfoDrawerEvents.shareClick());
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=fi.kaupunkifillarit");
            startActivity(Intent.createChooser(share, null));
        });
        infoOpenSourceLicensesClickSubscription = RxView.clicks(infoOpenSourceLicenses).subscribe(onClickEvent -> {
            new LicensesDialog.Builder(this)
                    .setTitle(R.string.open_source_licenses)
                    .setCloseText(R.string.close)
                    .setNotices(R.raw.notices)
                    .build()
                    .show();
        });
    }

    @Override
    protected void onPause() {
        unsubscribeEverything();
        try {
            String json = objectMapper.writeValueAsString(mapLocation.orSome(DEFAULT_MAP_LOCATION));
            sharedPreferences.edit().putString(LAST_MAP_LOCATION, json).apply();
        } catch (JsonProcessingException e) {
            Timber.w(e, "Storing location failed");
        }
        super.onPause();
    }

    private void unsubscribeEverything() {
        infoOpenSourceLicensesClickSubscription.unsubscribe();
        shareClickSubscription.unsubscribe();
        closeInfoDrawerClickSubscription.unsubscribe();
        racksSubscription.unsubscribe();
        lastLocationSubscription.unsubscribe();
        if (permissionGrantedSubscription != null) {
            permissionGrantedSubscription.unsubscribe();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateMapPadding();
        updateInfoViewPadding();
        updateContentPadding();
    }

    private void setUpMapIfNeeded() {
        if (map.isNone()) {
            CustomMapFragment mapFragment = (CustomMapFragment) getFragmentManager().findFragmentById(R.id.map);
            GoogleMaps.create(mapFragment).subscribe((Maps.MapWrapper onNext) -> {
                MapActivity.this.map = Option.some(onNext);
                setUpMap();
                mapFragment.setOnTouchListener((View v, MotionEvent e) -> {
                    switch (e.getAction()) {
                        case MotionEvent.ACTION_CANCEL:
                        case MotionEvent.ACTION_UP:
                            touching = false;
                            break;
                        default:
                            touching = true;
                            break;
                    }
                    return false;
                });
            });
        }
    }

    private int obtainStatusBarHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                return getResources().getDimensionPixelSize(resourceId);
            }
        }
        return 0;
    }

    private int obtainNavigationBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight - obtainStatusBarHeight() > usableHeight) {
            return obtainNavigationBarHeightFromProperty();
        }
        return 0;
    }

    private int obtainNavigationBarHeightFromProperty() {
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private int obtainNavigationBarWidth() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableWidth = metrics.widthPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realWidth = metrics.widthPixels;
        if (realWidth > usableWidth) {
            return realWidth - usableWidth;
        }
        return 0;
    }

    private void updateMapPadding() {
        if (map.isSome()) {
            map.some().setPadding(0, obtainStatusBarHeight(), 0, 0);
        }
    }

    private void updateInfoViewPadding() {
        RelativeLayout.LayoutParams logoParams = (RelativeLayout.LayoutParams) logo.getLayoutParams();
        logoParams.topMargin = obtainStatusBarHeight() + getResources().getDimensionPixelSize(R.dimen.info_view_vertical_margin);
        logo.setLayoutParams(logoParams);
        RelativeLayout.LayoutParams closeInfoDrawerParams = (RelativeLayout.LayoutParams) closeInfoDrawer.getLayoutParams();
        closeInfoDrawerParams.topMargin = obtainStatusBarHeight();
        closeInfoDrawer.setLayoutParams(closeInfoDrawerParams);
        infoContent.setPadding(infoContent.getPaddingLeft(), infoContent.getPaddingTop(), infoContent.getPaddingRight(), obtainNavigationBarHeight() + getResources().getDimensionPixelSize(R.dimen.info_view_vertical_margin));
        FrameLayout.LayoutParams drawerParams = (FrameLayout.LayoutParams) drawer.getLayoutParams();
        int layoutDirection = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault());
        drawerParams.leftMargin = layoutDirection == LayoutDirection.RTL ? obtainNavigationBarWidth() : 0;
        drawerParams.rightMargin = layoutDirection == LayoutDirection.LTR ? obtainNavigationBarWidth() : 0;
        drawer.setLayoutParams(drawerParams);
    }

    private void updateContentPadding() {
        FrameLayout.LayoutParams contentParams = (FrameLayout.LayoutParams) content.getLayoutParams();
        contentParams.topMargin = obtainStatusBarHeight();
        contentParams.bottomMargin = obtainNavigationBarHeight();
        content.setLayoutParams(contentParams);
    }

    private void setUpMap() {
        Maps.MapWrapper map = this.map.some();

        if (!shouldRequestLocationPermission()) {
            //noinspection MissingPermission
            map.setMyLocationEnabled(true);
        }
        map.setTrafficEnabled(false);
        map.setOnMarkerClickListener(marker -> true);
        if (this.mapLocation.isSome() && !mapMoved && mapLocation.some().isWithinDesiredMapBounds()) {
            MapLocation mapLocation = this.mapLocation.some();
            map.animateToMapLocation(mapLocation);
        } else if (!mapMoved) {
            map.animateToMapLocation(DEFAULT_MAP_LOCATION);
        }
        map.setOnMapLocationChangeListener((MapLocation mapLocation) -> {
            if (!mapInitialized) {
                mapInitialized = true;
            } else {
                mapMoved = true;
                if (this.mapLocation.isNone()) {
                    this.mapLocation = Option.some(mapLocation);
                } else {
                    MapLocation oldMapLocation = this.mapLocation.some();
                    oldMapLocation.latitude = mapLocation.latitude;
                    oldMapLocation.longitude = mapLocation.longitude;
                    oldMapLocation.zoom = mapLocation.zoom;
                    oldMapLocation.bearing = mapLocation.bearing;
                    oldMapLocation.tilt = mapLocation.tilt;
                }
            }
        });
        map.setOnMyLocationButtonClickListener(() -> {
            tracker.send(MapsEvents.myLocationClick());
            return false;
        });
        updateMapPadding();
    }

    private class FeedbackForm {
        public static final String FEEDBACK_ANSWER = "feedback_answer";

        public void start() {
            int feedBackAnswer = MapActivity.this.sharedPreferences.getInt(FEEDBACK_ANSWER, 0);

            if (shouldDisplayDialog(feedBackAnswer)) {
                FragmentManager fm = getFragmentManager();
                Fragment fragment = fm.findFragmentByTag(FeedbackDialogFragment.TAG);
                FragmentTransaction ft = fm.beginTransaction();
                if (fragment != null) {
                    ft.remove(fragment);
                }
                new FeedbackDialogFragment().show(ft, FeedbackDialogFragment.TAG);
                MapActivity.this.tracker.send(FeedbackEvents.showDialog());
            }
        }

        private boolean shouldDisplayDialog(int feedbackAnswer) {
            switch (feedbackAnswer) {
                case DialogInterface.BUTTON_POSITIVE:
                case DialogInterface.BUTTON_NEGATIVE:
                    return false;
                case DialogInterface.BUTTON_NEUTRAL:
                    return true;
                default:
                    long useCount = MapActivity.this.sharedPreferences.getLong(UsageLogger.USE_COUNT, 0);
                    if (useCount < 6) {
                        return false;
                    }
                    long lastUsed = MapActivity.this.sharedPreferences.getLong(UsageLogger.LAST_USED, 0);
                    long timeSinceLastUse = System.currentTimeMillis() - lastUsed;

                    return timeSinceLastUse >= TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS) &&
                            timeSinceLastUse < TimeUnit.MILLISECONDS.convert(3, TimeUnit.DAYS);
            }
        }
    }

    public static class FeedbackDialogFragment extends DialogFragment {
        public static final String TAG = FeedbackDialogFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DialogInterface.OnClickListener positiveClick = (DialogInterface dialog, int which) -> {
                ((MapActivity) getActivity()).sharedPreferences.edit().putInt(FeedbackForm.FEEDBACK_ANSWER, DialogInterface.BUTTON_POSITIVE).apply();
                ((MapActivity) getActivity()).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_POSITIVE));
                getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=fi.kaupunkifillarit")));
            };
            DialogInterface.OnClickListener neutralClick = (DialogInterface dialog, int which) -> {
                ((MapActivity) getActivity()).sharedPreferences.edit().putInt(FeedbackForm.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEUTRAL).apply();
                ((MapActivity) getActivity()).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEUTRAL));
            };
            DialogInterface.OnClickListener negativeClick = (DialogInterface dialog, int which) -> {
                ((MapActivity) getActivity()).sharedPreferences.edit().putInt(FeedbackForm.FEEDBACK_ANSWER, DialogInterface.BUTTON_NEGATIVE).apply();
                ((MapActivity) getActivity()).tracker.send(FeedbackEvents.buttonClick(DialogInterface.BUTTON_NEGATIVE));
            };
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return new android.app.AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                        .setTitle(R.string.rating_request_title)
                        .setMessage(R.string.rating_request_message)
                        .setPositiveButton(R.string.rating_request_rate, positiveClick)
                        .setNeutralButton(R.string.rating_request_later, neutralClick)
                        .setNegativeButton(R.string.rating_request_dont_remind, negativeClick)
                        .create();
            } else {
                return new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                        .setTitle(R.string.rating_request_title)
                        .setMessage(R.string.rating_request_message)
                        .setPositiveButton(R.string.rating_request_rate, positiveClick)
                        .setNeutralButton(R.string.rating_request_later, neutralClick)
                        .setNegativeButton(R.string.rating_request_dont_remind, negativeClick)
                        .create();
            }
        }
    }

    private class UsageLogger {
        public static final String LAST_USED = "last_used";
        public static final String USE_COUNT = "use_count";

        public void start() {
            MapActivity.this.sharedPreferences.edit()
                    .putLong(LAST_USED, System.currentTimeMillis())
                    .putLong(UsageLogger.USE_COUNT, MapActivity.this.sharedPreferences.getLong(UsageLogger.USE_COUNT, 0) + 1)
                    .apply();
        }
    }

    public static class LocationPermissionRationaleDialogFragment extends DialogFragment {
        public static final String TAG = LocationPermissionRationaleDialogFragment.class.getSimpleName();

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DialogInterface.OnClickListener positiveClick = (DialogInterface dialog, int which) -> {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            };

            Dialog dialog;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialog = new android.app.AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                        .setTitle(R.string.location_permission_rationale_title)
                        .setMessage(R.string.location_permission_rationale_message)
                        .setPositiveButton(R.string.location_permission_rationale_ok, positiveClick)
                        .create();
            } else {
                dialog = new android.support.v7.app.AlertDialog.Builder(getActivity(), R.style.AppTheme_AlertDialog)
                        .setTitle(R.string.location_permission_rationale_title)
                        .setMessage(R.string.location_permission_rationale_message)
                        .setPositiveButton(R.string.location_permission_rationale_ok, positiveClick)
                        .create();
            }
            setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            setCancelable(false);
            return view;
        }
    }
}
