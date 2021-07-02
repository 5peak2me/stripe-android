package com.stripe.android.payments.core.authentication

import android.app.Activity
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.stripe.android.model.StripeIntent
import com.stripe.android.networking.ApiRequest
import com.stripe.android.payments.core.ActivityResultLauncherHost
import com.stripe.android.view.AuthActivityStarterHost

/**
 * A unit to authenticate a [StripeIntent] base on its next_action.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface IntentAuthenticator : ActivityResultLauncherHost {

    /**
     * Authenticates an [StripeIntent] based on its next_action
     *
     * @param host the host([Activity] or [Fragment]) where client is calling from, used to redirect back to client.
     * @param stripeIntent the intent to authenticate
     * @param requestOptions configurations for the API request which triggers the authentication
     */
    suspend fun authenticate(
        host: AuthActivityStarterHost,
        stripeIntent: StripeIntent,
        requestOptions: ApiRequest.Options
    )
}
