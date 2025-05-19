package com.example.menstruacionnavapp.ui.premium

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.menstruacionnavapp.util.PayPalHelper
import com.paypal.android.sdk.payments.PayPalPayment
import com.paypal.android.sdk.payments.PayPalService
import com.paypal.android.sdk.payments.PaymentActivity
import com.paypal.android.sdk.payments.PaymentConfirmation
import java.math.BigDecimal

class PremiumViewModel : ViewModel() {
    
    companion object {
        const val PAYPAL_REQUEST_CODE = 7777
    }
    
    fun processPayment(activity: Activity, amount: Double) {
        val payment = PayPalPayment(
            BigDecimal(amount.toString()),
            "EUR",
            "Suscripción Premium",
            PayPalPayment.PAYMENT_INTENT_SALE
        )
        
        val intent = Intent(activity, PaymentActivity::class.java)
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment)
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, PayPalHelper.config)
        activity.startActivityForResult(intent, PAYPAL_REQUEST_CODE)
    }
    
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val confirm: PaymentConfirmation? = 
                    data?.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION)
                
                if (confirm != null) {
                    try {
                        val paymentDetails = confirm.toJSONObject().toString()
                        Log.d("PaymentDetails", paymentDetails)
                        return true
                    } catch (e: Exception) {
                        Log.e("PaymentError", "Error en el proceso de pago: ${e.message}")
                    }
                }
            }
            return false
        }
        return false
    }
}
