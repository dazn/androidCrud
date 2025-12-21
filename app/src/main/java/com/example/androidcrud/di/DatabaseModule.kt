package com.example.androidcrud.di

import android.content.Context
import androidx.room.Room
import com.example.androidcrud.data.local.AppDatabase
import com.example.androidcrud.data.local.EntryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "android_crud_db"
        ).build()
    }

    @Provides
    fun provideEntryDao(database: AppDatabase): EntryDao {
        return database.entryDao()
    }
}
