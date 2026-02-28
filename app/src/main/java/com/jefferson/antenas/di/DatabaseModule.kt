package com.jefferson.antenas.di

import android.content.Context
import androidx.room.Room
import com.jefferson.antenas.data.local.AppDatabase
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
            "jefferson_antenas.db"
        ).fallbackToDestructiveMigration() // ATENÇÃO: apaga dados locais ao mudar schema — substituir por migrations antes de produção
            .build()
    }
}