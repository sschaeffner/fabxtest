package cloud.fabX.fabXaccess.user.model

import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.SourcingEvent

sealed class UserSourcingEvent(override val aggregateRootId: UserId) : SourcingEvent {
    abstract fun processBy(eventHandler: EventHandler, user: User): User

    interface EventHandler {
        fun handle(event: UserPersonalInformationChanged, user: User): User
        fun handle(event: UserLockStateChanged, user: User): User
    }
}

data class UserPersonalInformationChanged(
    override val aggregateRootId: UserId,
    val firstName: ChangeableValue<String>,
    val lastName: ChangeableValue<String>,
    val wikiName: ChangeableValue<String>,
    val phoneNumber: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId) {
    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}

data class UserLockStateChanged(
    override val aggregateRootId: UserId,
    val locked: ChangeableValue<Boolean>,
    val notes: ChangeableValue<String?>
) : UserSourcingEvent(aggregateRootId) {
    override fun processBy(eventHandler: EventHandler, user: User): User =
        eventHandler.handle(this, user)
}