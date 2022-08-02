package cloud.fabX.fabXaccess.device

import cloud.fabX.fabXaccess.common.addBasicAuth
import cloud.fabX.fabXaccess.common.withTestApp
import cloud.fabX.fabXaccess.device.ws.DeviceToServerNotification
import cloud.fabX.fabXaccess.device.ws.ToolUnlockedNotification
import cloud.fabX.fabXaccess.tool.givenTool
import cloud.fabX.fabXaccess.user.givenCardIdentity
import cloud.fabX.fabXaccess.user.givenUser
import cloud.fabX.fabXaccess.user.rest.CardIdentity
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.util.InternalAPI
import kotlinx.coroutines.delay
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

@InternalAPI
@ExperimentalSerializationApi
internal class ToolUnlockedNotificationViaWebsocketIntegrationTest {

    @Test
    fun `when receives ToolUnlockedNotification then logs tool unlocked`() = withTestApp {
        // given
        val mac = "aabb11cc22dd"
        val secret = "supersecret123"
        val deviceId = givenDevice(mac = mac, secret = secret)

        val toolId = givenTool()
        givenToolAttachedToDevice(deviceId, 1, toolId)

        val cardId = "11223344556677"
        val cardSecret = "EE334F5E740985180C9EDAA6B5A9EB159CFB4F19427C68336D6D23D5015547CE"
        val userId = givenUser()
        givenCardIdentity(userId, cardId, cardSecret)

        // when
        handleWebSocketConversation("/api/v1/device/ws", {
            addBasicAuth(mac, secret)
        }) { incoming, outgoing ->
            (incoming.receive() as Frame.Text).readText() // greeting text

            val notification = ToolUnlockedNotification(toolId, null, CardIdentity(cardId, cardSecret))

            outgoing.send(Frame.Text(Json.encodeToString<DeviceToServerNotification>(notification)))

            delay(100)
        }
    }
}