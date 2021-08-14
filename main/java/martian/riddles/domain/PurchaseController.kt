package martian.riddles.domain

import android.app.Activity
import android.content.Context
import android.text.TextUtils
import android.widget.Toast
import com.android.billingclient.api.*
import martian.riddles.data.local.StoredData
import java.util.ArrayList
import java.util.HashMap
import javax.inject.Inject

class PurchaseController @Inject constructor(
    private val context: Context,
    private val statisticsController: StatisticsController,
    private val attemptsController: AttemptsController
    ) {

    var billingClient: BillingClient
    private val mSkuDetailsMap: MutableMap<String, SkuDetails> = HashMap()
    var countPurchaseOffer: Int
        private set
    var isPayComplete = false
        private set
    private val mSkuId = "endless_attempts"

    val DATA_SHOW_PURCHASE = "show_purchase"

    private fun handlePurchase(purchase: Purchase) {
        // Acknowledge the purchase if it hasn't already been acknowledged.
        if (!purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult: BillingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    payComplete()
                    statisticsController.sendPurchase(attemptsController.countWrongAnswers)
                    //handler!!.sendEmptyMessage(HIDE_PURCHASE)
                } else {
                    /*Toast.makeText(
                        this@RiddlesActivity,
                        "Error with code " + billingResult.responseCode,
                        Toast.LENGTH_LONG
                    ).show()*/
                }
            }
        }
    }



    fun increaseCountPurchaseOffer() {
        StoredData.saveData(DATA_SHOW_PURCHASE, ++countPurchaseOffer)
    }

    fun buy() {
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(mSkuDetailsMap[mSkuId]!!)
            .build()
        billingClient.launchBillingFlow((context as Activity), billingFlowParams)
    }

    private fun payComplete() {
        attemptsController.isEndlessAttempts = true
        //animationController!!.setAttemptsOnScreen()
        isPayComplete = true
    }

    private fun querySkuDetails() {
        val skuDetailsParamsBuilder = SkuDetailsParams.newBuilder()
        val skuList: MutableList<String> = ArrayList()
        skuList.add(mSkuId)
        skuDetailsParamsBuilder.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
        billingClient.querySkuDetailsAsync(skuDetailsParamsBuilder.build()) { billingResult, list ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                for (skuDetails in list) {
                    mSkuDetailsMap[skuDetails.sku] = skuDetails
                }
            }
        }
    }

    init {
        countPurchaseOffer = StoredData.getDataInt(DATA_SHOW_PURCHASE, 0)
        billingClient = BillingClient.newBuilder(context)
            .enablePendingPurchases()
            .setListener { billingResult: BillingResult, list: List<Purchase>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    //сюда мы попадем когда будет осуществлена покупка
                    if (list[0].purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(list[0])
                    }
                }
            }.build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                try {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        querySkuDetails() //запрос о товарах
                        val purchasesList = queryPurchases() //запрос о покупках

                        //если товар уже куплен, предоставить его пользователю
                        for (i in purchasesList!!.indices) {
                            val purchaseId = purchasesList[i].sku
                            if (TextUtils.equals(mSkuId, purchaseId)) {
                                payComplete()
                            }
                        }
                    }
                } catch (ex: NullPointerException) {
                }
            }

            private fun queryPurchases(): List<Purchase>? {
                val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                return purchasesResult.purchasesList
            }

            override fun onBillingServiceDisconnected() {
                //сюда мы попадем если что-то пойдет не так
            }
        })
    }
}