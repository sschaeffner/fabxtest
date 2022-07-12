package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.device.infrastructure.DeviceDatabaseRepository
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.logging.LogbackLoggerFactory
import cloud.fabX.fabXaccess.qualification.infrastructure.QualificationDatabaseRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.user.infrastructure.UserDatabaseRepository
import cloud.fabX.fabXaccess.user.model.UserRepository
import kotlin.system.exitProcess

object AppConfiguration {

    private val log: Logger

    internal val loggerFactory: LoggerFactory
    private val deviceRepository: DeviceRepository
    private val qualificationRepository: QualificationRepository
    private val userRepository: UserRepository

    init {
        loggerFactory = LogbackLoggerFactory()
        log = loggerFactory.invoke(AppConfiguration::class.java)
        log.info("Configuring modules...")

        deviceRepository = DeviceDatabaseRepository()
        qualificationRepository = QualificationDatabaseRepository()
        userRepository = UserDatabaseRepository()

        configureDomain()
        configureRest()

        if (!DomainModule.isFullyConfigured()) {
            log.error("DomainModule not fully configured!")
            exitProcess(-1)
        }

        if (!RestModule.isFullyConfigured()) {
            log.error("RestModule not fully configured!")
            exitProcess(-1)
        }

        log.info("...all modules configured")
    }

    private fun configureDomain() {
        DomainModule.configure(loggerFactory)
        DomainModule.configure(deviceRepository)
        DomainModule.configure(qualificationRepository)
        DomainModule.configure(userRepository)
    }

    private fun configureRest() {
        RestModule.configure(loggerFactory)
    }
}

@Suppress("unused") // receiver T is required to infer class to create logger for
internal inline fun <reified T> T.logger(): Logger {
    return AppConfiguration.loggerFactory.invoke(T::class.java)
}