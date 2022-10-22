package pl.coopsoft.szambelan.domain.repository.network

import android.util.Log
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import pl.coopsoft.szambelan.domain.model.DataModel
import retrofit2.Call
import javax.inject.Inject


class RemoteStorageHelper @Inject constructor(private val service: RemoteStorageService) {

    private companion object {
        private const val TAG = "RemoteStorageHelper"
    }

    fun downloadData(user: String, done: (DataModel?) -> Unit) {
        val call = service.retrieve(user)
        call.enqueue(
            object : retrofit2.Callback<DataModel?> {
                override fun onResponse(
                    call: Call<DataModel?>,
                    response: retrofit2.Response<DataModel?>
                ) {
                    if (response.isSuccessful &&
                        (response.raw().body?.contentLength() ?: 0) > 0
                    ) {
                        Log.v(TAG, "Download success")
                        done(response.body())
                    } else {
                        Log.e(
                            TAG,
                            "Download error ${response.code()} contentLength=${response.raw().body?.contentLength()}"
                        )
                        done(null)
                    }
                }

                override fun onFailure(call: Call<DataModel?>, t: Throwable) {
                    Log.e(TAG, "Download error: ${t.message}")
                    done(null)
                }
            }
        )
    }

    fun uploadData(data: DataModel, user: String, done: (Throwable?) -> Unit) {
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
                    call: Call<ResponseBody?>,
                    response: retrofit2.Response<ResponseBody?>
                ) {
                    Log.v(TAG, "Upload success")
                    done(null)
                }

                override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                    Log.e(TAG, "Upload error: ${t.message}")
                    done(t)
                }
            }
        )
    }
}