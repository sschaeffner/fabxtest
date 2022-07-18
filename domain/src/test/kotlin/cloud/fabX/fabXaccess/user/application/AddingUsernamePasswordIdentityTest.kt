package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByUsername
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentityAdded
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingUsernamePasswordIdentityTest {

    private val adminActor = AdminFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null
    private var gettingUserByUsername: GettingUserByUsername? = null

    private var testee: AddingUsernamePasswordIdentity? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByUsername: GettingUserByUsername
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByUsername = gettingUserByUsername
        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserRepository(userRepository)
        DomainModule.configureGettingUserByUsername(gettingUserByUsername)

        testee = AddingUsernamePasswordIdentity()
    }

    @Test
    fun `given user can be found when adding identity then sourcing event is created and stored`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val username = "username"
        val hash = "password42"

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            userId,
            2,
            adminActor.id,
            username,
            hash
        )

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByUsername!!.getByUsername(username))
            .thenReturn(Error.UserNotFoundByUsername("").left())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addUsernamePasswordIdentity(
            adminActor,
            userId,
            username,
            hash
        )

        // then
        assertThat(result).isNone()

        val inOrder = inOrder(userRepository!!)
        inOrder.verify(userRepository!!).getById(userId)
        inOrder.verify(userRepository!!).store(expectedSourcingEvent)
    }

    @Test
    fun `given user cannot be found when adding identity then returns error`() {
        // given
        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(error.left())

        // when
        val result = testee!!.addUsernamePasswordIdentity(
            adminActor,
            userId,
            "username",
            "password"
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given sourcing event cannot be stored when adding identity then returns error`() {
        // given
        val user = UserFixture.arbitrary(userId, aggregateVersion = 1, identities = setOf())

        val username = "username"
        val hash = "password42"

        val expectedSourcingEvent = UsernamePasswordIdentityAdded(
            userId,
            2,
            adminActor.id,
            username,
            hash
        )

        val error = Error.UserNotFound("message", userId)

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        whenever(gettingUserByUsername!!.getByUsername(username))
            .thenReturn(Error.UserNotFoundByUsername("").left())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.addUsernamePasswordIdentity(
            adminActor,
            userId,
            username,
            hash
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }

    @Test
    fun `given domain error when adding identity then returns domain error`() {
        // given
        val existingUsernamePasswordIdentity = UsernamePasswordIdentity("name", "hash")
        val user = UserFixture.arbitrary(
            userId,
            aggregateVersion = 1,
            identities = setOf(existingUsernamePasswordIdentity)
        )

        val expectedDomainError =
            Error.UsernamePasswordIdentityAlreadyFound("User already has a username password identity.")

        whenever(userRepository!!.getById(userId))
            .thenReturn(user.right())

        // when
        val result = testee!!.addUsernamePasswordIdentity(
            adminActor,
            userId,
            "username",
            "password42"
        )

        // then

        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }
}