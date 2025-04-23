import android.content.Context
import com.android.peoplefinder.client.AppController
import com.android.peoplefinder.client.NetworkInterceptor
import com.android.peoplefinder.helper.Constants
import com.android.peoplefinder.interfaces.ApiInterface
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkProvider {

    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null
    private var gson: Gson? = null
    private var apiService: ApiInterface? = null

    fun init(context: Context) {
        getOkHttpClient(context)
        getRetrofit(context)
        getApiService()
    }

    fun getHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    fun getGson(): Gson {
        return gson ?: GsonBuilder()
            .setLenient()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
            .also { gson = it }
    }

    fun getOkHttpClient(context: Context): OkHttpClient {
        return okHttpClient ?: OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(getHttpLoggingInterceptor())
            .addInterceptor(NetworkInterceptor(context))
            .build()
            .also { okHttpClient = it }
    }

    fun getRetrofit(context: Context): Retrofit {
        return retrofit ?: Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .client(getOkHttpClient(context))
            .build()
            .also { retrofit = it }
    }

    fun getApiService(): ApiInterface {
        return apiService ?: getRetrofit(AppController.appContext)
            .create(ApiInterface::class.java)
            .also { apiService = it }
    }
}


