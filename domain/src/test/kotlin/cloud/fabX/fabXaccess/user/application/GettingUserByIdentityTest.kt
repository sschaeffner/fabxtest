package cloud.fabX.fabXaccess.user.application

import arrow.core.left
import arrow.core.right
import assertk.assertThat
import assertk.assertions.isSameAs
import cloud.fabX.fabXaccess.DomainModule
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.ErrorFixture
import cloud.fabX.fabXaccess.common.model.Logger
import cloud.fabX.fabXaccess.common.model.SystemActor
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UsernamePasswordIdentity
import isLeft
import isRight
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.whenever

@MockitoSettings
internal class GettingUserByIdentityTest {
    private val correlationId = CorrelationIdFixture.arbitrary()

    private lateinit var logger: Logger
    private lateinit var gettingUserByIdentity: cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
    private lateinit var testee: GettingUserByIdentity

    @BeforeEach
    fun `configure DomainModule`(
        @Mock logger: Logger,
        @Mock gettingUserByIdentity: cloud.fabX.fabXaccess.user.model.GettingUserByIdentity
    ) {
        this.logger = logger
        this.gettingUserByIdentity = gettingUserByIdentity

        DomainModule.configureLoggerFactory { logger }
        DomainModule.configureGettingUserByIdentity(gettingUserByIdentity)

        testee = GettingUserByIdentity()
    }

    @Test
    fun `when getting by identity then returns user from repository`() {
        // given
        val identity = UsernamePasswordIdentity(
            "some.one",
            "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
        )
        val user = UserFixture.arbitrary()

        whenever(gettingUserByIdentity.getByIdentity(identity))
            .thenReturn(user.right())

        // when
        val result = testee.getUserByIdentity(
            SystemActor,
            correlationId,
            identity
        )

        // then
        assertThat(result)
            .isRight()
            .isSameAs(user)
    }

    @Test
    fun `given user not exists when getting by identity then returns error`() {
        // given
        val identity = UsernamePasswordIdentity(
            "some.one",
            "h6dLJxkiB+4phuKyqiNC1JBKI+2BXYWq0R8eLiBOYIg="
        )

        val error = ErrorFixture.arbitrary()

        whenever(gettingUserByIdentity.getByIdentity(identity))
            .thenReturn(error.left())

        // when
        val result = testee.getUserByIdentity(
            SystemActor,
            correlationId,
            identity
        )

        // then
        assertThat(result)
            .isLeft()
            .isSameAs(error)
    }
}