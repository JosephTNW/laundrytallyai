package com.example.laundrytallyai

import android.app.Application
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@HiltAndroidApp
class MyApplication: Application() {

}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Remove the provideApplication function, since Hilt already provides the Application context
}