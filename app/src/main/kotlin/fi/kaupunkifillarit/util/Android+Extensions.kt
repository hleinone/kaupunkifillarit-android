package fi.kaupunkifillarit.util

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import com.jakewharton.rxbinding2.view.clicks
import com.trello.rxlifecycle2.components.RxActivity
import fi.kaupunkifillarit.KaupunkifillaritApplication
import io.reactivex.Maybe
import io.reactivex.Single
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper

val Context.app: KaupunkifillaritApplication
    get() = applicationContext as KaupunkifillaritApplication

abstract class BaseActivity : RxActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}

/**
 * Emits {@link DialogInterface#BUTTON_POSITIVE}, {@link DialogInterface#BUTTON_NEGATIVE} or {@link DialogInterface#BUTTON_NEUTRAL}.
 */
fun AlertDialog.clicks(): Single<Int> =
        Maybe.merge(
                getButton(DialogInterface.BUTTON_POSITIVE).clicks().firstElement().map { DialogInterface.BUTTON_POSITIVE },
                getButton(DialogInterface.BUTTON_NEGATIVE).clicks().firstElement().map { DialogInterface.BUTTON_NEGATIVE },
                getButton(DialogInterface.BUTTON_NEUTRAL).clicks().firstElement().map { DialogInterface.BUTTON_NEUTRAL })
                .firstOrError()
                .doOnSuccess {
                    this.dismiss()
                }

