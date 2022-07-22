package io.github.acidfox.kopringbootauthapi

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/")
class DefaultController {

    @Value("\${config.healthcheck-message}")
    lateinit var healthCheckMessage: String

    @GetMapping
    fun healthCheck(): String {
        return healthCheckMessage
    }
}
