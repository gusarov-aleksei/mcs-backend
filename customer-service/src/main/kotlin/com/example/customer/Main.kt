package com.example.customer

import com.example.customer.koin.customerModule
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.core.context.startKoin


fun main(args: Array<String>) {
    startKoin{
        fileProperties()
        environmentProperties()
        modules(customerModule)
    }
    embeddedServer(Netty, commandLineEnvironment(args)).start(wait = true)
}