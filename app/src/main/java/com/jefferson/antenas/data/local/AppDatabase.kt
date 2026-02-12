package com.jefferson.antenas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jefferson.antenas.data.local.dao.ProductDao
import com.jefferson.antenas.data.model.Product

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // ✅ AGORA TEM O DAO!
    abstract fun productDao(): ProductDao
}