package cloud.fabX.fabXaccess.user.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import cloud.fabX.fabXaccess.common.model.ChangeableValue
import cloud.fabX.fabXaccess.common.model.Error
import cloud.fabX.fabXaccess.qualification.model.QualificationIdFixture
import isLeft
import isRight
import kotlin.test.Test

internal class UserTest {

    private val userId = UserIdFixture.arbitraryId()

    @Test
    fun `given valid values when constructing user then user is constructed`() {
        // given

        // when
        val user = User(
            userId,
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null,
            Member(listOf()),
            null,
            null
        )

        // then
        assertThat(user).isNotNull()
        assertThat(user.id).isEqualTo(userId)
    }

    @Test
    fun `when changing personal information then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId)

        val expectedSourcingEvent = UserPersonalInformationChanged(
            aggregateRootId = userId,
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changePersonalInformation(
            firstName = ChangeableValue.ChangeToValue("newFistName"),
            lastName = ChangeableValue.LeaveAsIs,
            wikiName = ChangeableValue.ChangeToValue("newWikiName"),
            phoneNumber = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `when changing lock state then expected sourcing event is returned`() {
        // given
        val user = UserFixture.arbitraryUser(userId)

        val expectedSourcingEvent = UserLockStateChanged(
            aggregateRootId = userId,
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // when
        val result = user.changeLockState(
            locked = ChangeableValue.ChangeToValue(true),
            notes = ChangeableValue.ChangeToValue(null)
        )

        // then
        assertThat(result).isEqualTo(expectedSourcingEvent)
    }

    @Test
    fun `given any user when getting as member then returns member`() {
        // given
        val qualifications = listOf(QualificationIdFixture.arbitraryId(), QualificationIdFixture.arbitraryId())
        val user = UserFixture.arbitraryUser(userId, memberQualifications = qualifications)

        // when
        val result = user.asMember()

        // then
        assertThat(result.qualifications).isEqualTo(qualifications)
    }

    @Test
    fun `given user without instructor when getting as instructor then returns error`() {
        // given
        val user = UserFixture.arbitraryUser(userId, instructorQualifications = null)

        // when
        val result = user.asInstructor()

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotInstructor("User $userId is not an instructor."))
    }

    @Test
    fun `given user with instructor when getting as instructor then returns instructor`() {
        // given
        val qualification1 = QualificationIdFixture.arbitraryId()
        val qualification2 = QualificationIdFixture.arbitraryId()

        val user = UserFixture.arbitraryUser(userId, instructorQualifications = listOf(qualification1, qualification2))

        // when
        val result = user.asInstructor()

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Instructor(listOf(qualification1, qualification2)))
    }

    @Test
    fun `given user without admin when getting as admin then returns error`() {
        // given
        val user = UserFixture.arbitraryUser(userId, isAdmin = false)

        // when
        val result = user.asAdmin()

        // then
        assertThat(result)
            .isLeft()
            .isEqualTo(Error.UserNotAdmin("User $userId is not an admin."))
    }

    @Test
    fun `given user with admin when getting as admin then returns admin`() {
        // given
        val user = UserFixture.arbitraryUser(userId, isAdmin = true)

        // when
        val result = user.asAdmin()

        // then
        assertThat(result)
            .isRight()
            .isEqualTo(Admin())
    }

    @Test
    fun `given valid user when stringifying then result is correct`() {
        // given
        val user = User(
            UserIdFixture.staticId(42),
            "Nikola",
            "Tesla",
            "nick",
            "00491234567890",
            false,
            null,
            Member(listOf(QualificationIdFixture.staticId(43))),
            Instructor(listOf(QualificationIdFixture.staticId(44))),
            Admin()
        )

        // when
        val result = user.toString()

        // then
        assertThat(result).isEqualTo(
            "User(id=UserId(value=f4a3f34c-e12a-395c-9fd2-23e167422c32), " +
                    "firstName=Nikola, " +
                    "lastName=Tesla, " +
                    "wikiName=nick, " +
                    "phoneNumber=00491234567890, " +
                    "locked=false, " +
                    "notes=null, " +
                    "member=Member(qualifications=[QualificationId(value=6ecbc07c-382b-3e04-a9b3-a86909f10e64)]), " +
                    "instructor=Instructor(qualifications=[QualificationId(value=a3dd6fd6-61a5-3c37-810c-8c68fe610bec)]), " +
                    "admin=Admin())"
        )
    }
}