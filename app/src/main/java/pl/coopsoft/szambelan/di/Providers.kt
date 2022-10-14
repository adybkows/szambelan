package pl.coopsoft.szambelan.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Locale

@Module
@InstallIn(SingletonComponent::class)
object Providers {

    @Provides
    fun provideLocale(): Locale =
        Locale.getDefault()
}