package ju.ma.controllers

import com.auth0.AuthenticationController
import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.BaseEncoding
import java.security.SecureRandom
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

private val logger = KotlinLogging.logger {}
private val rand = SecureRandom()

@RestController
class HugoController {
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
    @Value("\${hugo.github.token:}")
    lateinit var githubToken: String

    private val controller: AuthenticationController by lazy {
        AuthenticationController.newBuilder(authDomain, authClientId, authClientSecret)
            .withJwkProvider(JwkProviderBuilder(authDomain).build())
            .build()
    }

    val mapper = ObjectMapper()

    @RequestMapping(value = ["/hugo/auth"], method = [RequestMethod.GET])
    fun hugo(
        @RequestParam(value = "provider") provider: String,
        @RequestParam(value = "site_id") siteId: String,
        @RequestParam(value = "scope") scope: String,
        req: HttpServletRequest,
        res: HttpServletResponse
    ): ModelAndView {
        val redirectUrl = "http://localhost:8080/hugo-verify?provider=$provider&site_id=$siteId&scope=$scope"
        val authorizeUrl = controller.buildAuthorizeUrl(req, res, redirectUrl)
            .withScope(authScopes)
            .withAudience(authAudience)
            .build()
        return ModelAndView("redirect:$authorizeUrl")
    }

    @RequestMapping(value = ["/hugo-verify"], method = [RequestMethod.GET])
    @ResponseBody
    fun hugoVerify(
        @RequestParam(value = "provider") provider: String,
        @RequestParam(value = "site_id") siteId: String,
        @RequestParam(value = "scope") scope: String,
        req: HttpServletRequest,
        res: HttpServletResponse
    ): String {
        val properties = mutableMapOf<String, Any?>(
            "provider" to provider
        )
        val message = try {
            val redirectUrl = "http://localhost:8080/hugo-verify"
            val tokens = controller.handle(CustomRequesWrapper(redirectUrl, req), res)

            if (!tokens.accessToken.isNullOrBlank()) {
                val attributes = mapper.readValue(
                    BaseEncoding.base64Url().decode(tokens.accessToken.split(".", limit = 3)[1]),
                    Map::class.java
                )
                val permissions = attributes["permissions"]
                if (permissions is List<*>) {
                    val access = permissions.map { it.toString() }.toSet()
                    if (access.contains("write:content")) {
                        properties["token"] = this.githubToken
                        "authorization:$provider:success:${ObjectMapper().writeValueAsString(properties)}"
                    } else {
                        throw RuntimeException("You do not have permissions to perform the action!")
                    }
                } else {
                    throw RuntimeException("You don not have permissions to perform the action!")
                }
            } else {
                throw RuntimeException("No :accessToken in the request. You might not be authorised.")
            }
        } catch (e: Exception) {
            properties["error"] = e.message
            properties["exception"] = e.javaClass.simpleName
            "authorization:$provider:error:${ObjectMapper().writeValueAsString(properties)}"
        }
        return """
               <script>
    (function() {
      function recieveMessage(e) {
        console.log("recieveMessage %o", e)
        // send message to main window with da app
        window.opener.postMessage(
          ${ObjectMapper().writeValueAsString(message)},
          e.origin
        )
      } 
      window.addEventListener("message", recieveMessage, false)
      // Start handshare with parent
      console.log("Sending message: %o", "$provider")
      window.opener.postMessage("authorizing:$provider", "*")
    })()
    </script>
        """.trimIndent()
    }
}
