package fi.kaupunkifillarit

import com.crashlytics.android.answers.Answers
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import timber.log.Timber

class DebugKaupunkifillaritApplication : KaupunkifillaritApplication() {
    override val tracker: Tracker by lazy {
        val analytics = GoogleAnalytics.getInstance(this)
        analytics.setDryRun(true)
        analytics.newTracker(R.xml.tracker)
    }

    override val answers: Answers by lazy {
        Answers()
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
