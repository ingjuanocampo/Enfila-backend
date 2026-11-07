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
    single<TipRepository> { TipRepositoryImpl() }
    single<NotificationRepository> { NotificationRepositoryImpl() }

    // Services
    single { MessageServiceImpl() }
    single<MessageService> { get<MessageServiceImpl>() }
    single<TurnReadyTransport> { get<MessageServiceImpl>() }
    single { TurnReadyNotifier(get()) }
    single { NotificationDeliveryWorker(get(), get()) }
    single<UserService> { UserServiceImpl(get()) }
    single<ClientService> { ClientServiceImpl(get(), get()) }
    single<ShiftService> { ShiftServiceImpl(get(), get(), get(), get()) }
    single<CompanySiteService> { CompanySiteServiceImpl(get()) }
    single<MigrationService> { MigrationServiceImpl(get(), get(), get(), get()) }
    single<UsageMilestoneEvaluator> {
        UsageMilestoneEvaluatorImpl(get(), get(), get(), get())
    }
    single<TipService> { TipServiceImpl(get(), get()) }
}
