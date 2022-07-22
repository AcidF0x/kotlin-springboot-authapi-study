package io.github.acidfox.kopringbootauthapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KopringBootAuthApiApplication

fun main(args: Array<String>) {
    runApplication<KopringBootAuthApiApplication>(*args)
}
