package ju.ma.controllers

import ju.ma.auth.AppUser
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.ModelAndView

@RestController
class AuthController {
    @GetMapping("/", "/**/{path:[^\\.]*}")
    fun angularRouter(model: ModelMap): ModelAndView {
        return ModelAndView("forward:/index.html", model)
    }

    @RequestMapping(value = ["/auth/user"], method = [RequestMethod.GET])
    fun user(@RequestHeader httpHeaders: HttpHeaders): ResponseEntity<*> {
        val authUser = SecurityContextHolder.getContext().authentication.principal
        val responseBody = LinkedHashMap<String, Any?>()
        responseBody["status"] = HttpStatus.UNAUTHORIZED
        try {
            if (authUser != null) {
                if (authUser is AppUser) {
                    if (authUser.isAnonymous()) {
                        return ResponseEntity<Map<String, *>>(responseBody, HttpStatus.UNAUTHORIZED)
                    }
                    return ResponseEntity.ok(authUser)
                } else {
                    return ResponseEntity<Map<String, *>>(responseBody, HttpStatus.UNAUTHORIZED)
                }
            } else {
                return ResponseEntity<Map<String, *>>(responseBody, HttpStatus.UNAUTHORIZED)
            }
        } catch (e: Exception) {
            responseBody["message"] = e.message ?: ""
            responseBody["exception"] = "LOGIN_FAILED"
            return ResponseEntity<Map<String, *>>(responseBody, HttpStatus.UNAUTHORIZED)
        }
    }
}
