package martian.riddles.di

import android.content.Context
import com.skydoves.sandwich.coroutines.CoroutinesResponseCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import martian.riddles.R
import martian.riddles.data.remote.ServerApi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import javax.inject.Singleton
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    private val baseUrl = "https://192.168.0.21"

    @Provides
    @Singleton
    fun getRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(CoroutinesResponseCallAdapterFactory())
                .client(okHttpClient)
                .build()
    }

    @Provides
    @Singleton
    fun getApiService(retrofit: Retrofit): ServerApi {
        return retrofit.create(ServerApi::class.java)
    }

    @Provides
    @Singleton
    fun generateSecureOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
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