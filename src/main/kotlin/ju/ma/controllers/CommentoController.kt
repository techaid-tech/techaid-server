package ju.ma.controllers

import com.auth0.AuthenticationController
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.BaseEncoding
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper
import javax.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.apache.commons.codec.binary.Hex
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

private val logger = KotlinLogging.logger {}
private val rand = SecureRandom()

@RestController
class CommentoController {
    @Value("\${commento.url}")
    lateinit var url: String
    @Value("\${commento.key}")
    lateinit var key: String
    @Value("\${commento.auth.domain}")
    lateinit var authDomain: String
    @Value("\${commento.auth.client-secret}")
    lateinit var authClientSecret: String
    @Value("\${commento.auth.client-id}")
    lateinit var authClientId: String
    @Value("\${commento.auth.audience}")
    lateinit var authAudience: String
    @Value("\${commento.auth.scopes}")
    lateinit var authScopes: String
    @Value("\${commento.auth.redirect:}")
    lateinit var authRedirect: String

    private val controller: AuthenticationController by lazy {
        AuthenticationController.newBuilder(authDomain, authClientId, authClientSecret)
            .withJwkProvider(JwkProviderBuilder(authDomain).build())
            .build()
    }

    val mapper = ObjectMapper()

    @RequestMapping(value = ["/commento"], method = [RequestMethod.GET])
    fun commento(
        @RequestParam(value = "token") token: String,
        @RequestParam(value = "hmac") hmac: String,
        req: HttpServletRequest,
        res: HttpServletResponse
    ): ModelAndView {
        val secretKey = Hex.decodeHex(key)
        val expectedMac = hmac256(Hex.decodeHex(token), secretKey)
        if (!macVerify(expectedMac, hmac)) {
            throw IllegalArgumentException("Invalid Mac")
        }

        val redirectUrl = if (authRedirect.isNotBlank()) {
            "$authRedirect?token=$token&hmac=$hmac"
        } else {
            req.scheme + "://" + req.serverName + ":" + req.serverPort + "/api/commento-verify?token=$token&hmac=$hmac"
        }
        val authorizeUrl = controller.buildAuthorizeUrl(req, res, redirectUrl)
            .withScope(authScopes)
            .withAudience(authAudience)
            .build()
        return ModelAndView("redirect:$authorizeUrl")
    }

    @RequestMapping(value = ["/commento-verify"], method = [RequestMethod.GET])
    fun commentoVerify(
        @RequestParam(value = "token") token: String,
        @RequestParam(value = "hmac") hmac: String,
        req: HttpServletRequest,
        res: HttpServletResponse
    ): ModelAndView {
        val secretKey = Hex.decodeHex(key)
        val expectedMac = hmac256(Hex.decodeHex(token), secretKey)
        if (!macVerify(expectedMac, hmac)) {
            throw IllegalArgumentException("Invalid Mac")
        }

        val redirectUrl = if (authRedirect.isNotBlank()) {
            "$authRedirect"
        } else {
            req.scheme + "://" + req.serverName + ":" + req.serverPort + "/api/commento-verify"
        }
        val tokens = controller.handle(CustomRequesWrapper(redirectUrl, req), res)
        val properties = mutableMapOf<String, Any?>(
            "token" to token
        )
        if (!tokens.idToken.isNullOrBlank()) {
            val attributes = mapper.readValue(
                BaseEncoding.base64Url().decode(tokens.idToken.split(".", limit = 3)[1]),
                Map::class.java
            )
            properties["name"] = attributes["nickname"] as String?
                ?: attributes["name"] as String?
                    ?: attributes["email"] as String?
            properties["email"] = attributes["email"] as String?
            attributes["picture"]?.let {
                if (it is String && !it.isNullOrBlank()) {
                    properties["photo"] = it
                }
            }
        }
        val json = ObjectMapper().writeValueAsString(properties)
        val payloadMac = hmac256(json.toByteArray(), secretKey)
        val payloadHex = Hex.encodeHexString(json.toByteArray())
        return ModelAndView("redirect:$url/api/oauth/sso/callback?payload=$payloadHex&hmac=$payloadMac")
    }

    private fun macVerify(a: String, b: String): Boolean {
        val secret = ByteArray(32)
        rand.nextBytes(secret)
        return MessageDigest.isEqual(
            hmac256(a.toByteArray(), secret).toByteArray(),
            hmac256(b.toByteArray(), secret).toByteArray()
        )
    }

    private fun hmac256(data: ByteArray, key: ByteArray): String {
        val signingKey = SecretKeySpec(key, "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        return Hex.encodeHexString(mac.doFinal(data))
    }
}

internal class CustomRequesWrapper(private val uri: String, request: HttpServletRequest) :
    HttpServletRequestWrapper(request) {
    override fun getRequestURL(): StringBuffer {
        return StringBuffer(uri)
    }
}
