package cloud.fabX.fabXaccess

import cloud.fabX.fabXaccess.common.application.LoggerFactory
import cloud.fabX.fabXaccess.device.model.DeviceIdFactory
import cloud.fabX.fabXaccess.device.model.DeviceRepository
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFactory
import cloud.fabX.fabXaccess.qualification.model.QualificationRepository
import cloud.fabX.fabXaccess.tool.model.ToolIdFactory
import cloud.fabX.fabXaccess.tool.model.ToolRepository
import cloud.fabX.fabXaccess.user.model.UserIdFactory
import cloud.fabX.fabXaccess.user.model.UserRepository

object DomainModule {
    private var loggerFactory: LoggerFactory? = null

    private var deviceIdFactory: DeviceIdFactory? = null
    private var qualificationIdFactory: QualificationIdFactory? = null
    private var toolIdFactory: ToolIdFactory? = null
    private var userIdFactory: UserIdFactory? = null

    private var deviceRepository: DeviceRepository? = null
    private var qualificationRepository: QualificationRepository? = null
    private var toolRepository: ToolRepository? = null
    private var userRepository: UserRepository? = null


    fun isFullyConfigured(): Boolean {
        return loggerFactory != null
                && deviceIdFactory != null
                && qualificationIdFactory != null
                && toolIdFactory != null
                && userIdFactory != null
                && deviceRepository != null
                && qualificationRepository != null
                && toolRepository != null
                && userRepository != null
    }

    fun configureLoggerFactory(loggerFactory: LoggerFactory) {
        this.loggerFactory = loggerFactory
    }

    fun configureDeviceIdFactory(deviceIdFactory: DeviceIdFactory) {
        this.deviceIdFactory = deviceIdFactory
    }

    fun configureQualificationIdFactory(qualificationIdFactory: QualificationIdFactory) {
        this.qualificationIdFactory = qualificationIdFactory
    }

    fun configureToolIdFactory(toolIdFactory: ToolIdFactory) {
        this.toolIdFactory = toolIdFactory
    }

    fun configureUserIdFactory(userIdFactory: UserIdFactory) {
        this.userIdFactory = userIdFactory
    }

    fun configureDeviceRepository(deviceRepository: DeviceRepository) {
        this.deviceRepository = deviceRepository
    }

    fun configureQualificationRepository(qualificationRepository: QualificationRepository) {
        this.qualificationRepository = qualificationRepository
    }

    fun configureToolRepository(toolRepository: ToolRepository) {
        this.toolRepository = toolRepository
    }

    fun configureUserRepository(userRepository: UserRepository) {
        this.userRepository = userRepository
    }

    internal fun loggerFactory(): LoggerFactory {
        return require(loggerFactory)
    }

    internal fun deviceIdFactory(): DeviceIdFactory {
        return require(deviceIdFactory)
    }

    internal fun qualificationIdFactory(): QualificationIdFactory {
        return require(qualificationIdFactory)
    }

    internal fun toolIdFactory(): ToolIdFactory {
        return require(toolIdFactory)
    }

    internal fun userIdFactory(): UserIdFactory {
        return require(userIdFactory)
    }

    internal fun deviceRepository(): DeviceRepository {
        return require(deviceRepository)
    }

    internal fun qualificationRepository(): QualificationRepository {
        return require(qualificationRepository)
    }

    internal fun toolRepository(): ToolRepository {
        return require(toolRepository)
    }

    internal fun userRepository(): UserRepository {
        return require(userRepository)
    }

    private fun <T : Any> require(value: T?): T = requireNotNull(value) { "DomainModule has to be configured" }
}