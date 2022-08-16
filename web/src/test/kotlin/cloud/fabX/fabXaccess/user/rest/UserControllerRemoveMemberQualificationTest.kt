package cloud.fabX.fabXaccess.user.rest

import arrow.core.None
import arrow.core.getOrElse
import arrow.core.some
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.model.CorrelationIdFixture
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.common.rest.c
import cloud.fabX.fabXaccess.common.rest.isError
import cloud.fabX.fabXaccess.common.rest.withTestApp
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import cloud.fabX.fabXaccess.user.application.RemovingMemberQualification
import cloud.fabX.fabXaccess.user.model.UserFixture
import cloud.fabX.fabXaccess.user.model.UserIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.UserPasswordCredential
import io.ktor.server.testing.ApplicationTestBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.kodein.di.bindInstance
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@MockitoSettings
internal class UserControllerRemoveMemberQualificationTest {
    private lateinit var removingMemberQualification: RemovingMemberQualification
    private lateinit var authenticationService: AuthenticationService

    private val username = "some.one"
    private val password = "supersecret123"

    private val actingUser = UserFixture.arbitrary(isAdmin = true)

    @BeforeEach
    fun `configure WebModule`(
        @Mock removingMemberQualification: RemovingMemberQualification,
        @Mock authenticationService: AuthenticationService
    ) {
        this.removingMemberQualification = removingMemberQualification
        this.authenticationService = authenticationService
    }

    private fun withConfiguredTestApp(block: suspend ApplicationTestBuilder.() -> Unit) = withTestApp({
        bindInstance(overrides = true) { removingMemberQualification }
        bindInstance(overrides = true) { authenticationService }
    }, block)

    @Test
    fun `when removing member qualification then returns http no content`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val qualificationId = QualificationIdFixture.arbitrary()

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingMemberQualification.removeMemberQualification(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(None)

        // when
        val response =
            c().delete("/api/v1/user/${userId.serialize()}/member-qualification/${qualificationId.serialize()}") {
                basicAuth(username, password)
            }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given no admin authentication when removing member qualification then returns http forbidden`() =
        withConfiguredTestApp {
            // given
            val message = "msg123"
            val error = Error.UserNotAdmin(message)

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(ErrorPrincipal(error))

            // when
            val response = c().delete(
                "/api/v1/user/${
                    UserIdFixture.arbitrary().serialize()
                }/member-qualification/${QualificationIdFixture.arbitrary().serialize()}"
            ) {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
            assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
                .isError(
                    "UserNotAdmin",
                    message
                )
        }

    @Test
    fun `given invalid user id when removing member qualification then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidUserId = "invalidUserId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().delete(
                "/api/v1/user/$invalidUserId/member-qualification/${
                    QualificationIdFixture.arbitrary().serialize()
                }"
            ) {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo("Required UUID parameter \"id\" not given or invalid.")
        }

    @Test
    fun `given invalid qualification id when removing member qualification then returns http bad request`() =
        withConfiguredTestApp {
            // given
            val invalidQualificationId = "invalidQualificationId"

            whenever(authenticationService.basic(UserPasswordCredential(username, password)))
                .thenReturn(UserPrincipal(actingUser))

            // when
            val response = c().delete(
                "/api/v1/user/${UserIdFixture.arbitrary().serialize()}/member-qualification/$invalidQualificationId"
            ) {
                basicAuth(username, password)
            }

            // then
            assertThat(response.status).isEqualTo(HttpStatusCode.BadRequest)
            assertThat(response.body<String>()).isEqualTo("Required UUID parameter \"qualificationId\" not given or invalid.")
        }

    @Test
    fun `given domain error when removing member qualification then returns mapped error`() = withConfiguredTestApp {
        // given
        val userId = UserIdFixture.arbitrary()
        val qualificationId = QualificationIdFixture.arbitrary()

        val correlationId = CorrelationIdFixture.arbitrary()
        val error = Error.MemberQualificationNotFound(
            "some message",
            qualificationId,
            correlationId
        )

        whenever(authenticationService.basic(UserPasswordCredential(username, password)))
            .thenReturn(UserPrincipal(actingUser))

        whenever(
            removingMemberQualification.removeMemberQualification(
                eq(actingUser.asAdmin().getOrElse { throw IllegalStateException() }),
                any(),
                eq(userId),
                eq(qualificationId)
            )
        ).thenReturn(error.some())

        // when
        val response =
            c().delete("/api/v1/user/${userId.serialize()}/member-qualification/${qualificationId.serialize()}") {
                basicAuth(username, password)
            }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.UnprocessableEntity)
        assertThat(response.body<cloud.fabX.fabXaccess.common.rest.Error>())
            .isError(
                "MemberQualificationNotFound",
                "some message",
                mapOf("qualificationId" to qualificationId.serialize()),
                correlationId.serialize()
            )
    }
}