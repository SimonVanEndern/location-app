package com.simonvanendern.tracking

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.simonvanendern.tracking.aggregation.RequestExecuter
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import javax.crypto.Cipher
import javax.inject.Singleton

@Module
class ApplicationModule(private val application: Context) {

    @Singleton
    @Provides
    fun provideWebservice(): WebService {
        fun String.toPrivateKey(): PrivateKey {
            val keyBytes: ByteArray = Base64.decode(this, Base64.DEFAULT)
            val spec = PKCS8EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance("RSA")

            return keyFactory.generatePrivate(spec)
        }

        val interceptor = Interceptor { chain ->
            {
                val request: Request = chain.request()
                val response = chain.proceed(request)
                val raw = response.body()?.string()
                val store = application.getSharedPreferences(application.getString(R.string.identifiers), Context.MODE_PRIVATE)
                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                val keyString = store.getString("private_key", "test")
                val key =  keyString!!.toPrivateKey()
                cipher.init(Cipher.DECRYPT_MODE, key)

                try {
                    val json = JSONObject(raw)
                    val value = json.getString("encryptedRequest")
                    val decrypted = cipher.doFinal(Base64.decode(value.toByteArray(), 0))
                } catch (e : JSONException) {
                    val newBody = ResponseBody.create(
                        response.body()?.contentType(),
                        raw!!
                    )
                    response.newBuilder().body(newBody).build()
                }

                try {
                    val json = JSONArray(raw)
                    for (i in json.length() - 1 downTo 0) {
                        json.getJSONObject(i).put("test", "test")
                    }
                    val newBody = ResponseBody.create(
                        response.body()?.contentType(),
                        json.toString()
                    )
                    response.newBuilder().body(newBody).build()
                } catch (e: JSONException) {
                    val newBody = ResponseBody.create(
                        response.body()?.contentType(),
                        raw!!
                    )
                    response.newBuilder().body(newBody).build()
                }
            }()
        }
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()
        return Retrofit.Builder()
            .baseUrl("https://privacy-research2-rested-fox.eu-gb.mybluemix.net/")
//            .baseUrl("http://localhost:8888/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(clientBuilder)
            .build()
            .create(WebService::class.java)
    }

    @Provides
    @Singleton
    fun provideContext() = application

    @Provides
    @Singleton
    fun provideDb(application: Context): TrackingDatabase {
        return Room.databaseBuilder(
            application,
            TrackingDatabase::class.java,
            "Location_database"
        ).build()
    }

    @Singleton
    @Provides
    fun provideRequestRepository(
        webService: WebService,
        db: TrackingDatabase
    ): RequestRepository {
        return RequestRepository(db, webService)
    }

    @Singleton
    @Provides
    fun provideRequestExecutor(db: TrackingDatabase): RequestExecuter {
        return RequestExecuter(db)
    }

    @Singleton
    @Provides
    fun provideActivityRepository(db: TrackingDatabase): ActivityRepository {
        return ActivityRepository(db)
    }
}
