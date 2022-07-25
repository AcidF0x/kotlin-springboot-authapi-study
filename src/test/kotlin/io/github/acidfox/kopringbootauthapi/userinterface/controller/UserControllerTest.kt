package io.github.acidfox.kopringbootauthapi.userinterface.controller

import io.github.acidfox.kopringbootauthapi.BaseControllerTestCase
import io.github.acidfox.kopringbootauthapi.domain.user.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal class UserControllerTest: BaseControllerTestCase()
{
    @Autowired
    lateinit var mvc: MockMvc

    lateinit var user: User

    @BeforeEach
    fun setUp() {
        user = User(
            "email",
            "password",
            "string",
            "name",
                "01011112222"
        )
        user.createdAt = LocalDateTime.now()

        val context = SecurityContextHolder.getContext()
        context.authentication = UsernamePasswordAuthenticationToken(user, null, null)
    }

    @Test
    @DisplayName("Get 요청으로 사용자의 정보를 조회 할 수 있다")
    fun testMe() {
        // Given
        val url = "/api/user/me"

        // When
        val result = mvc.perform(MockMvcRequestBuilders.get(url).contentType(MediaType.APPLICATION_JSON))

        // Then
        result.andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.email))
            .andExpect(MockMvcResultMatchers.jsonPath("$.nickname").value(user.nickname))
            .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(user.name))
            .andExpect(MockMvcResultMatchers.jsonPath("$.phone_number").value(user.phoneNumber))
            .andExpect(
                MockMvcResultMatchers.jsonPath("$.created_at")
                    .value(
                        user.createdAt
                            .atZone(ZoneId.of("Asia/Seoul"))
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    )
            )
    }
}
