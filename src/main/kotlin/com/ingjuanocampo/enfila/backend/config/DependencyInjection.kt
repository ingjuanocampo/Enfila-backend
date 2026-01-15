package com.ingjuanocampo.enfila.backend.config

import com.ingjuanocampo.enfila.backend.data.repositories.*
import com.ingjuanocampo.enfila.backend.services.*
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
}

val appModule = module {
    // Repositories
    single<UserRepository> { UserRepositoryImpl() }
    single<ClientRepository> { ClientRepositoryImpl() }
    single<ShiftRepository> { ShiftRepositoryImpl() }
    single<CompanySiteRepository> { CompanySiteRepositoryImpl() }
    
    // Services
    single<UserService> { UserServiceImpl(get()) }
    single<ClientService> { ClientServiceImpl(get()) }
    single<ShiftService> { ShiftServiceImpl(get(), get()) }
    single<CompanySiteService> { CompanySiteServiceImpl(get()) }
    single<MessageService> { MessageServiceImpl() }
    single<MigrationService> { MigrationServiceImpl(get(), get(), get(), get()) }
}
