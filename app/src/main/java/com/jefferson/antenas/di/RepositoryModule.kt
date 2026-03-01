package com.jefferson.antenas.di

import com.jefferson.antenas.data.repository.AddressRepository
import com.jefferson.antenas.data.repository.AddressRepositoryImpl
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.CartRepositoryImpl
import com.jefferson.antenas.data.repository.OrderRepository
import com.jefferson.antenas.data.repository.OrderRepositoryImpl
import com.jefferson.antenas.data.repository.ProductRepository
import com.jefferson.antenas.data.repository.ProductRepositoryImpl
import com.jefferson.antenas.data.repository.UserRepository
import com.jefferson.antenas.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds @Singleton
    abstract fun bindCartRepository(impl: CartRepositoryImpl): CartRepository

    @Binds @Singleton
    abstract fun bindOrderRepository(impl: OrderRepositoryImpl): OrderRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindAddressRepository(impl: AddressRepositoryImpl): AddressRepository
}
