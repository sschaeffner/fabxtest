package cloud.fabX.fabXaccess.user.application

import arrow.core.None
import arrow.core.left
import arrow.core.right
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.user.model.AdminFixture
import cloud.fabX.fabXaccess.user.model.GettingUserByWikiName
import cloud.fabX.fabXaccess.user.model.UserCreated
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import cloud.fabX.fabXaccess.user.model.UserRepository
import isNone
import isSome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class AddingUserTest {

    private val adminActor = AdminFixture.arbitrary()

    private val userId = UserIdFixture.arbitrary()

    private var logger: Logger? = null
    private var userRepository: UserRepository? = null
    private var gettingUserByWikiName: GettingUserByWikiName? = null

    private var testee: AddingUser? = null

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock userRepository: UserRepository,
        @Mock gettingUserByWikiName: GettingUserByWikiName
    ) {
        this.logger = logger
        this.userRepository = userRepository
        this.gettingUserByWikiName = gettingUserByWikiName

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureUserIdFactory { userId }
        DomainModule.configureUserRepository(userRepository)
        DomainModule.configureGettingUserByWikiName(gettingUserByWikiName)

        testee = AddingUser()
    }

    @Test
    fun `given valid values when adding user then sourcing event is created and stored`() {
        // given
        val firstName = "first"
        val lastName = "last"
        val wikiName = "wiki"

        val expectedSourcingEvent = UserCreated(
            userId,
            adminActor.id,
            firstName,
            lastName,
            wikiName
        )

        whenever(gettingUserByWikiName!!.getByWikiName(wikiName))
            .thenReturn(Error.UserNotFoundByWikiName("").left())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(None)

        // when
        val result = testee!!.addUser(
            adminActor,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result).isNone()
    }

    @Test
    fun `given domain error when adding user then returns domain error`() {
        // given
        val wikiName = "wiki"
        val otherUser = UserFixture.arbitrary(wikiName = wikiName)

        whenever(gettingUserByWikiName!!.getByWikiName(wikiName))
            .thenReturn(otherUser.right())

        val expectedDomainError = Error.WikiNameAlreadyInUse("Wiki name is already in use.")

        // when
        val result = testee!!.addUser(
            adminActor,
            "first",
            "last",
            wikiName
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(expectedDomainError)
    }

    @Test
    fun `given sourcing event cannot be stored when adding user then returns error`() {
        // given
        val firstName = "first"
        val lastName = "last"
        val wikiName = "wiki"

        val expectedSourcingEvent = UserCreated(
            userId,
            adminActor.id,
            firstName,
            lastName,
            wikiName
        )

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserByWikiName!!.getByWikiName(wikiName))
            .thenReturn(Error.UserNotFoundByWikiName("").left())

        whenever(userRepository!!.store(expectedSourcingEvent))
            .thenReturn(error.some())

        // when
        val result = testee!!.addUser(
            adminActor,
            firstName,
            lastName,
            wikiName
        )

        // then
        assertThat(result)
            .isSome()
            .isEqualTo(error)
    }
}