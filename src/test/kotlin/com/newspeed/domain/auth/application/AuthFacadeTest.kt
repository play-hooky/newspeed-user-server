package com.newspeed.domain.auth.application

import com.newspeed.domain.jwt.repository.RefreshTokenRepository
import com.newspeed.domain.user.repository.UserRepository
import com.newspeed.factory.auth.AuthFactory
import com.newspeed.global.exception.user.UserNotFoundException
import com.newspeed.template.IntegrationTestTemplate
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.core.api.SoftAssertions
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

@DisplayName("auth facade 내 ")
class AuthFacadeTest: IntegrationTestTemplate {

    @Autowired
    private lateinit var authFacade: AuthFacade

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Nested
    inner class `카카오 로그인에` {

        @Test
        fun `성공하면 사용자 정보와 발급한 JWT를 저장한다`() {
            // given
            val kakaoLoginRequest = AuthFactory.createKakaoLoginRequest()

            // when
            val actual = authFacade.login(kakaoLoginRequest)

            val actualUser = userRepository.findByIdOrNull(actual.userId)!!
            val actualRefreshToken = refreshTokenRepository.findByToken(actual.refreshToken)!!

            assertSoftly { softly: SoftAssertions ->
                softly.assertThat(actual.userId).isEqualTo(actualUser.id)
                softly.assertThat(actual.refreshToken).isEqualTo(actualRefreshToken.token)
            }
        }
    }

    @Nested
    inner class `사용자를 조회할 때` {

        @Test
        fun `userId가 유효하면 사용자 정보를 반환한다`() {
            // given
            val kakaoLoginRequest = AuthFactory.createKakaoLoginRequest()
            val loginResponse = authFacade.login(kakaoLoginRequest)
            val expected = userRepository.findByIdOrNull(loginResponse.userId)!!

            // when
            val actual = authFacade.getUserResponse(expected.id)

            // then
            assertSoftly { softly: SoftAssertions ->
                softly.assertThat(actual.email).isEqualTo(expected.email)
                softly.assertThat(actual.nickname).isEqualTo(expected.nickname)
                softly.assertThat(actual.profileImgUrl).isEqualTo(expected.profileImageUrl)
            }
        }

        @Test
        fun `userId가 존재하지 않는 사용자라면 예외를 던진다`() {
            assertThatThrownBy { authFacade.getUserResponse(Long.MIN_VALUE) }
                .isInstanceOf(UserNotFoundException::class.java)
        }
    }
}