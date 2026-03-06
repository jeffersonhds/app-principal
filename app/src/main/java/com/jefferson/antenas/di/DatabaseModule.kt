package com.jefferson.antenas.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jefferson.antenas.data.local.AppDatabase
import com.jefferson.antenas.data.local.dao.CartDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// ── Migrations ────────────────────────────────────────────────────────────────
//
// MIGRATION_1_2: criação da tabela cart_items
//   - Motivo: persistência do carrinho no Room (antes era só em memória)
//   - Colunas: productId (PK), name, price, quantity, imageUrl
//
// MIGRATION_2_3 (template — use quando a versão do AppDatabase subir para 3):
// val MIGRATION_2_3 = object : Migration(2, 3) {
//     override fun migrate(database: SupportSQLiteDatabase) {
//         // Exemplo: database.execSQL("ALTER TABLE products ADD COLUMN stock INTEGER NOT NULL DEFAULT 0")
//     }
// }
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS cart_items (
                productId TEXT NOT NULL PRIMARY KEY,
                name TEXT NOT NULL,
                price REAL NOT NULL,
                quantity INTEGER NOT NULL,
                imageUrl TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCartDao(database: AppDatabase): CartDao = database.cartDao()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "jefferson_antenas.db"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }
}
