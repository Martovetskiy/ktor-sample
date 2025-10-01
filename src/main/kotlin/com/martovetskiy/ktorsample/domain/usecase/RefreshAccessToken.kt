package com.martovetskiy.ktorsample.domain.usecase

import com.martovetskiy.ktorsample.domain.security.JwtService
import com.martovetskiy.ktorsample.domain.security.TokenPair

class RefreshAccessToken(
    private val jwtService: JwtService
) {
    suspend fun execute(refreshToken: String): TokenPair {
        return jwtService.refreshAccessToken(refreshToken)
    }
}
