package com.example.menstruacionnavapp.util

import android.app.Activity
import android.content.Intent
import com.paypal.android.sdk.payments.PayPalConfiguration
import com.paypal.android.sdk.payments.PayPalService

object PayPalHelper {
    // Configuración de PayPal en modo sandbox (pruebas)
    val config = PayPalConfiguration()
        .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
        // IMPORTANTE: Aquí debes usar tu ClientID de sandbox
        .clientId("TU_CLIENT_ID_DE_SANDBOX")
        .acceptCreditCards(true)

    fun startPayPalService(activity: Activity) {
        val intent = Intent(activity, PayPalService::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config)
        activity.startService(intent)
    }

    fun stopPayPalService(activity: Activity) {
        activity.stopService(Intent(activity, PayPalService::class.java))
    }
}