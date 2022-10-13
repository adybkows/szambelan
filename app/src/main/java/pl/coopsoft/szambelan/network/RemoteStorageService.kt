package pl.coopsoft.szambelan.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import pl.coopsoft.szambelan.models.DataModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path


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