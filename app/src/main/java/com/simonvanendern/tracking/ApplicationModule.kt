package com.simonvanendern.tracking

import android.content.Context
import android.util.Base64
import androidx.room.Room
import com.google.gson.GsonBuilder
import com.simonvanendern.tracking.aggregation.RequestExecuter
import com.simonvanendern.tracking.communication.WebService
import com.simonvanendern.tracking.database.TrackingDatabase
import com.simonvanendern.tracking.repository.ActivityRepository
import com.simonvanendern.tracking.repository.RequestRepository
import dagger.Module
import dagger.Provides
import okhttp3.*
import okio.Buffer
import okio.BufferedSink
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
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

        fun ByteArray.toAESKey(): SecretKey {
            val spec = SecretKeySpec(this, "AES")
            return spec
        }

        val interceptor = Interceptor { chain ->
            {
                val request: Request = chain.request()
                val response = chain.proceed(request)
                val raw = response.body()?.string()
                val store =
                    application.getSharedPreferences(application.getString(R.string.identifiers), Context.MODE_PRIVATE)


//                var keyy = store.getString("public_key_complete", "test")!!
//                keyy = keyy.replace("-----BEGIN PUBLIC KEY-----\n", "")
//                    .replace("\n-----END PUBLIC KEY-----", "")
//                var publicKey = store.getString("public_key", "test")
//                val specs = X509EncodedKeySpec(Base64.decode(keyy, Base64.DEFAULT))
//                val keyFactory = KeyFactory.getInstance("RSA")
//                val generated = keyFactory.generatePublic(specs)






                val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
                val keyString = store.getString("private_key", "test")
                val key = keyString!!.toPrivateKey()
                cipher.init(Cipher.DECRYPT_MODE, key)

                try {
                    val json = JSONArray(raw)
                    for (i in json.length() - 1 downTo 0) {
                        val encryptedKey = json.getJSONObject(i).getString("encryptionKey")
                        val key = cipher.doFinal(Base64.decode(encryptedKey.toByteArray(), 0))
                        val sync = json.getJSONObject(i).getString("sync")
                        val buffer = Base64.decode(sync, 0)
                        val encryptedRequest = Base64.decode(json.getJSONObject(i).getString("encryptedRequest"), 0)
                        val synchronousCipher = Cipher.getInstance("AES/CBC/PKCS7PADDING")
                        val iv = json.getJSONObject(i).getString("iv")
                        synchronousCipher.init(
                            Cipher.DECRYPT_MODE,
                            key.toAESKey(),
                            IvParameterSpec(Base64.decode(iv, 0))
                        )
                        val plainText = String(synchronousCipher.doFinal(encryptedRequest))
                        val nestedJson = JSONObject(plainText)
                        nestedJson.keys().forEach { key -> json.getJSONObject(i).put(key, nestedJson.get(key)) }

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

        val authenticationInterceptor = Interceptor { chain ->
            {
                val request = chain.request()
                val method = request.method()
                if (method != "POST") {
                    val newRequest = request.newBuilder().build()
                    chain.proceed(newRequest)
                } else {
                    val buffer = Buffer()
                    request.body()?.writeTo(buffer)
                    val json = JSONObject(buffer.readUtf8())
                    try {
                        val store =
                            application.getSharedPreferences(application.getString(R.string.identifiers), Context.MODE_PRIVATE)

                        json.put("pk", store.getString("public_key_complete", "test")!!)
                        json.put("pw", store.getString("private_key", "test")!!)
                        val newRequest = request.newBuilder()
                            .post(RequestBody.create(request.body()?.contentType(),
                                json.toString()))
                            .build()
                        chain.proceed(newRequest)
                    } catch (e : JSONException) {
                        val newRequest = request.newBuilder().build()
                        chain.proceed(newRequest)
                    }
                }
            }()
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(authenticationInterceptor)
            .build()

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd")
            .create()
        return Retrofit.Builder()
            .baseUrl("https://privacy-research-sweet-porcupine.eu-gb.mybluemix.net/")
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
