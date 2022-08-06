package cloud.fabX.fabXaccess.device

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import cloud.fabX.fabXaccess.common.adminAuth
import cloud.fabX.fabXaccess.common.c
import cloud.fabX.fabXaccess.common.isErrorB
import cloud.fabX.fabXaccess.common.memberAuth
import cloud.fabX.fabXaccess.common.rest.ChangeableValue
import cloud.fabX.fabXaccess.common.rest.Error
import cloud.fabX.fabXaccess.common.withTestAppB
import cloud.fabX.fabXaccess.device.model.DeviceIdFixture
import cloud.fabX.fabXaccess.device.rest.Device
import cloud.fabX.fabXaccess.device.rest.DeviceCreationDetails
import cloud.fabX.fabXaccess.device.rest.DeviceDetails
import cloud.fabX.fabXaccess.device.rest.ToolAttachmentDetails
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.tool.model.ToolIdFixture
import io.ktor.client.call.body
import io.ktor.client.request.basicAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class DeviceIntegrationTest {

    @Test
    fun `given no authentication when get devices then returns http unauthorized`() = withTestAppB {
        // given

        // when
        val response = c().get("/api/v1/device")

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given invalid authentication when get devices then returns http unauthorized`() = withTestAppB {
        // given

        // when
        val response = c().get("/api/v1/device") {
            basicAuth("no.body", "secret123")
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given non-admin authentication when get devices then returns http forbidden`() = withTestAppB {
        // given

        // when
        val response = c().get("/api/v1/device") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isErrorB(
                "UserNotAdmin",
                "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin."
            )
    }

    @Test
    fun `given devices when get devices then returns devices`() = withTestAppB {
        // given
        val deviceId1 = givenDevice("device1", mac = "aabbccaabb01")
        val deviceId2 = givenDevice("device2", mac = "aabbccaabb02")
        val deviceId3 = givenDevice("device3", mac = "aabbccaabb03")

        // when
        val response = c().get("/api/v1/device") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Set<Device>>())
            .transform { devices -> devices.map { it.id } }
            .containsExactlyInAnyOrder(deviceId1, deviceId2, deviceId3)
    }

    @Test
    fun `given device when get device by id then returns device`() = withTestAppB {
        // given
        val deviceId = givenDevice("newDevice", mac = "001122334455")

        // when
        val response = c().get("/api/v1/device/$deviceId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.OK)
        assertThat(response.body<Device>())
            .isEqualTo(
                Device(
                    deviceId,
                    1,
                    "newDevice",
                    "https://example.com/bg.bmp",
                    "https://backup.example.com",
                    mapOf()
                )
            )
    }

    @Test
    fun `given device does not exist when get device by id then returns http not found`() = withTestAppB {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        // when
        val response = c().get("/api/v1/device/$invalidDeviceId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isErrorB(
                "DeviceNotFound",
                "Device with id DeviceId(value=$invalidDeviceId) not found.",
                mapOf("deviceId" to invalidDeviceId)
            )
    }

    @Test
    fun `given non-admin authentication when adding device then returns http forbidden`() = withTestAppB {
        // given
        val requestBody = DeviceCreationDetails(
            "device42",
            "https://example.com/bg42.bmp",
            "https://backup.example.com",
            "aabbccddeeff",
            "supersecret"
        )

        // when

        val response = c().post("/api/v1/device") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
    }

    @Test
    fun `given device when changing device then returns http no content`() = withTestAppB {
        // given
        val deviceId = givenDevice(mac = "aa00bb11cc22")

        val requestBody = DeviceDetails(
            ChangeableValue("newName"),
            ChangeableValue("https://example.com/newbg.bmp"),
            null
        )

        // when
        val response = c().put("/api/v1/device/$deviceId") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()

        val responseGet = c().get("/api/v1/device/$deviceId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<Device>())
            .isEqualTo(
                Device(
                    deviceId,
                    2,
                    "newName",
                    "https://example.com/newbg.bmp",
                    "https://backup.example.com",
                    mapOf()
                )
            )
    }

    @Test
    fun `given invalid device when changing device then returns http not found`() = withTestAppB {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        val requestBody = DeviceDetails(
            null,
            null,
            null
        )

        // when
        val response = c().put("/api/v1/device/$invalidDeviceId") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isErrorB(
                "DeviceNotFound",
                "Device with id DeviceId(value=$invalidDeviceId) not found.",
                mapOf("deviceId" to invalidDeviceId)
            )
    }

    @Test
    fun `given device when deleting device then returns http no content`() = withTestAppB {
        // given
        val deviceId = givenDevice(mac = "aabbcc001122")

        // when
        val response = c().delete("/api/v1/device/$deviceId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given unknown device when deleting device then returns http not found`() = withTestAppB {
        // given
        val invalidDeviceId = DeviceIdFixture.arbitrary().serialize()

        // when
        val response = c().delete("/api/v1/device/$invalidDeviceId") {
            adminAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        assertThat(response.body<Error>())
            .isErrorB(
                "DeviceNotFound",
                "Device with id DeviceId(value=$invalidDeviceId) not found.",
                mapOf("deviceId" to invalidDeviceId)
            )
    }

    @Test
    fun `given non-admin authentication when deleting device then returns http forbidden`() = withTestAppB {
        // given
        val deviceId = givenDevice(mac = "aabbcc001122")

        // when
        val response = c().delete("/api/v1/device/$deviceId") {
            memberAuth()
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isErrorB(
                "UserNotAdmin",
                "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin."
            )
    }

    @Test
    fun `when attaching tool then returns http no content`() = withTestAppB {
        // given
        val deviceId = givenDevice(mac = "aabbccddeeff")
        val pin = 2
        val toolId = givenTool()
        val requestBody = ToolAttachmentDetails(toolId)

        // when
        val response = c().put("/api/v1/device/$deviceId/attached-tool/$pin") {
            adminAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()

        val responseGet = c().get("/api/v1/device/$deviceId") {
            adminAuth()
        }
        assertThat(responseGet.status).isEqualTo(HttpStatusCode.OK)
        assertThat(responseGet.body<Device>())
            .isEqualTo(
                Device(
                    deviceId,
                    2,
                    "device",
                    "https://example.com/bg.bmp",
                    "https://backup.example.com",
                    mapOf(2 to toolId)
                )
            )
    }

    @Test
    fun `given non-admin authentication when attaching tool then returns http forbidden`() = withTestAppB {
        val deviceId = DeviceIdFixture.arbitrary().serialize()
        val pin = 2
        val requestBody = ToolAttachmentDetails(ToolIdFixture.arbitrary().serialize())

        // when
        val response = c().put("/api/v1/device/$deviceId/attached-tool/$pin") {
            memberAuth()
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isErrorB(
                "UserNotAdmin",
                "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin."
            )
    }

    @Test
    fun `when detaching tool then returns http no content`() = withTestAppB {
        // given
        val pin = 2
        val deviceId = givenDevice(mac = "001122334455aa")
        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, pin, toolId)

        // when
        val response = c().delete("/api/v1/device/$deviceId/attached-tool/$pin") {
            adminAuth()
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        assertThat(response.bodyAsText()).isEmpty()
    }

    @Test
    fun `given non-admin authentication when detaching tool then returns http forbidden`() = withTestAppB {
        // given
        val pin = 2
        val deviceId = DeviceIdFixture.arbitrary().serialize()

        // when
        val response = c().delete("/api/v1/device/$deviceId/attached-tool/$pin") {
            memberAuth()
            contentType(ContentType.Application.Json)
        }

        // then
        assertThat(response.status).isEqualTo(HttpStatusCode.Forbidden)
        assertThat(response.body<Error>())
            .isErrorB(
                "UserNotAdmin",
                "User UserId(value=c63b3a7d-bd18-4272-b4ed-4bcf9683c602) is not an admin."
            )
    }
}