package com.example.test

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer


data class Product(val id: String, val name: String, val price: Double, val description: String)

@Serializer(forClass=Product::class)
object ProductSerializer

@Serializer(forClass=CartItem::class)
object CartItemSerializer

@Serializable
data class CartItem(val productId: String, val name: String, var price: Double, val quantity:Int, val total:Double )

data class Cart(val id: String, var customerId: String?, var total: Double, val cartItems: List<CartItem>)

@Serializer(forClass=Cart::class) /*Cart::javaClass*/
object CartSerializer

