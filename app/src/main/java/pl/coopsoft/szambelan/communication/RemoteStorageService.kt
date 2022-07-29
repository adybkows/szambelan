package pl.coopsoft.szambelan.communication

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.coopsoft.szambelan.models.DataModel
import retrofit2.Call
import retrofit2.http.*


interface RemoteStorageService {
    @GET("data_{user}.xml")
    fun retrieve(
        @Path("user") user: String
    ): Call<DataModel?>

    @Multipart
    @POST("upload.php")
    fun upload(
        @Part file: MultipartBody.Part?
    ): Call<ResponseBody?>
}