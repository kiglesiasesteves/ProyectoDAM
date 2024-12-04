package com.example.menstruacionnavapp.ui.premium

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.menstruacionnavapp.MainActivity
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.android.sdk.payments.PaymentConfirmation
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import java.math.BigDecimal

class PremiumViewModel : ViewModel() {

    fun onButtonClick(activity: Activity) {
        val payment = PayPalPayment(
            BigDecimal("9.99"), "EUR", "CicloFit Premium",
            PayPalPayment.PAYMENT_INTENT_SALE
        )

        val intent = Intent(activity, PaymentActivity::class.java)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, MainActivity.config)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)
        activity.startActivityForResult(intent, 123)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 123) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm: PaymentConfirmation? = data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                if (confirm != null) {
                    // Manejar la confirmación del pago
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // El usuario canceló el pago
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                // Pago inválido
            }
        }
    }
}