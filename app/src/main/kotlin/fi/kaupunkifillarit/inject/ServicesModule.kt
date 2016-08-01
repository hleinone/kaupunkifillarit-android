package fi.kaupunkifillarit.inject

import com.crashlytics.android.answers.Answers
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.Tracker
import dagger.Module
import dagger.Provides
import fi.kaupunkifillarit.KaupunkifillaritApplication
import fi.kaupunkifillarit.R
import javax.inject.Singleton

@Module
class ServicesModule(private val application: KaupunkifillaritApplication) {
    @Provides
    @Singleton
    fun provideTracker(): Tracker {
        val analytics = GoogleAnalytics.getInstance(application)
        return analytics.newTracker(R.xml.tracker)
    }

    @Provides
    @Singleton
    fun provideAnswers(): Answers {
        return Answers.getInstance()
    }
}
