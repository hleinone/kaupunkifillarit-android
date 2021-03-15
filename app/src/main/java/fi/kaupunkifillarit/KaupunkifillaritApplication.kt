package fi.kaupunkifillarit

import android.app.Application
import android.content.Context

open class KaupunkifillaritApplication : Application() {
}

val Context.app: KaupunkifillaritApplication
    get() = applicationContext as KaupunkifillaritApplication
