package martian.riddles.controllers

import android.content.Context
import android.net.ConnectivityManager
import martian.riddles.R
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.*
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.*


class RequestController private constructor() {

    private lateinit var retrofit: Retrofit
    private val baseUrl = "https://139.59.178.208"

    companion object {
        val instance = RequestController()
        @JvmStatic
        fun hasConnection(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            var wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            if (wifiInfo != null && wifiInfo.isConnected) {
                return true
            }
            wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            if (wifiInfo != null && wifiInfo.isConnected) {
                return true
            }
            wifiInfo = cm.activeNetworkInfo
            return wifiInfo != null && wifiInfo.isConnected
        }
    }

    fun getApiService(context: Context): ServerApi {
        retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(generateSecureOkHttpClient(context))
                .build()
        return retrofit.create(ServerApi::class.java)
    }

    private fun generateSecureOkHttpClient(context: Context): OkHttpClient {
        val certificateFactory = CertificateFactory.getInstance("X.509")

        val inputStream = context.resources.openRawResource(R.raw.riddles_cert) //(.crt)

        val certificate: Certificate? = certificateFactory.generateCertificate(inputStream)
        inputStream.close()

        // Create a KeyStore containing our trusted CAs

        // Create a KeyStore containing our trusted CAs
        val keyStoreType = KeyStore.getDefaultType()
        val keyStore = KeyStore.getInstance(keyStoreType)
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", certificate)

        // Create a TrustManager that trusts the CAs in our KeyStore.

        // Create a TrustManager that trusts the CAs in our KeyStore.
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm)
        trustManagerFactory.init(keyStore)

        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        val x509TrustManager: X509TrustManager = trustManagers[0] as X509TrustManager


        // Create an SSLSocketFactory that uses our TrustManager


        // Create an SSLSocketFactory that uses our TrustManager
        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf(x509TrustManager), null)
        var sslSocketFactory = sslContext.socketFactory

        //create Okhttp client
        val builder = OkHttpClient().newBuilder()



        builder.hostnameVerifier { hostname, session -> true }
        builder.sslSocketFactory(sslSocketFactory,x509TrustManager)


        return builder
                .build()
    }

}