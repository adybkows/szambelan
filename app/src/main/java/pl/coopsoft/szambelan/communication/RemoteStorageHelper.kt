package pl.coopsoft.szambelan.communication

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import pl.coopsoft.szambelan.BuildConfig
import pl.coopsoft.szambelan.models.DataModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RemoteStorageHelper {
    private const val TAG = "RemoteStorageHelper"

    private fun createClient() =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
            .build()

    private fun createService() =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(createClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemoteStorageService::class.java)

    fun downloadData(user: String, done: (DataModel?) -> Unit) {
        val service = createService()
        val call = service.retrieve(user)
        call.enqueue(
            object : retrofit2.Callback<DataModel?> {
                override fun onResponse(
                    call: retrofit2.Call<DataModel?>,
                    response: retrofit2.Response<DataModel?>
                ) {
                    Log.v(TAG, "Download success")
                    done(response.body())
                }

                override fun onFailure(call: retrofit2.Call<DataModel?>, t: Throwable) {
                    Log.e(TAG, "Download error: ${t.message}")
                    done(null)
                }
            }
        )
    }

    fun uploadData(data: DataModel, user: String, done: (Throwable?) -> Unit) {
        // create upload service client
        val service = createService()

        // create RequestBody instance from data
        val requestFile: RequestBody = Gson().toJson(data)
            .toRequestBody("application/xml".toMediaType())

        // MultipartBody.Part is used to send also the actual file name
        val body = MultipartBody.Part.createFormData("upfile", "data_${user}.xml", requestFile)

        // finally, execute the request
        val call = service.upload(body)
        call.enqueue(
            object : retrofit2.Callback<ResponseBody?> {
                override fun onResponse(
                    call: retrofit2.Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>
                ) {
                    Log.v(TAG, "Upload success")
                    done(null)
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody?>, t: Throwable) {
                    Log.e(TAG, "Upload error: ${t.message}")
                    done(t)
                }
            }
        )
    }
}