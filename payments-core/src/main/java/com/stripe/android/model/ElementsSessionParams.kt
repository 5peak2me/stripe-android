package com.stripe.android.model

import android.os.Parcelable
import androidx.annotation.RestrictTo
import kotlinx.parcelize.Parcelize
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Parcelize
sealed interface ElementsSessionParams : Parcelable {
    val type: String
    val clientSecret: String?
    val locale: String?

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Parcelize
    class PaymentIntentType(
        override val clientSecret: String,
        override val locale: String? = Locale.getDefault().toLanguageTag(),
    ) : ElementsSessionParams {
        override val type: String get() = "payment_intent"
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Parcelize
    class SetupIntentType(
        override val clientSecret: String,
        override val locale: String? = Locale.getDefault().toLanguageTag(),
    ) : ElementsSessionParams {
        override val type: String get() = "setup_intent"
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Parcelize
    class DeferredIntentType(
        override val clientSecret: String? = null,
        override val locale: String? = Locale.getDefault().toLanguageTag(),
        val deferredIntentParams: DeferredIntentParams
    ) : ElementsSessionParams {
        override val type: String get() = "deferred_intent"
    }
}