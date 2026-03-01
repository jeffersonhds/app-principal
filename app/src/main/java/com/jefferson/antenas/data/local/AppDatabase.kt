package com.jefferson.antenas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jefferson.antenas.data.local.dao.CartDao
import com.jefferson.antenas.data.local.dao.ProductDao
import com.jefferson.antenas.data.model.Product

@Database(
    entities = [Product::class, CartItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}
