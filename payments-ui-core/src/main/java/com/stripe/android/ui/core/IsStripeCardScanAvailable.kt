package com.stripe.android.ui.core

internal interface IsHCaptchaAvailable {
    operator fun invoke(): Boolean
}

internal class DefaultIsHCaptchaAvailable : IsHCaptchaAvailable {
    override fun invoke(): Boolean {
        return try {
            Class.forName("com.stripe.android.stripecardscan.cardscan.CardScanSheet")
            true
        } catch (_: Exception) {
            false
        }
    }
}
