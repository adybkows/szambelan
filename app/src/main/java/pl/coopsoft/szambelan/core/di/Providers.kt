package pl.coopsoft.szambelan.core.di

import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.coopsoft.szambelan.BuildConfig
import pl.coopsoft.szambelan.domain.repository.network.RemoteStorageService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Providers {

    @Provides
    @Singleton
    fun provideFirebaseAuth() = Firebase.auth

    @Provides
    @Singleton
    fun provideDatabaseReference(): DatabaseReference {
        val databaseUrl =
            "https://${FirebaseApp.getInstance().options.projectId}-default-rtdb.europe-west1.firebasedatabase.app"
        return Firebase.database(databaseUrl).reference
    }

    @Provides
    @Singleton
    fun provideRemoteStorageService(baseUrl: String = BuildConfig.BASE_URL): RemoteStorageService {
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BODY)
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RemoteStorageService::class.java)
    }

}