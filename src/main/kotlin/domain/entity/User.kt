package com.rednoir.domain.entity

import com.rednoir.domain.EmailInvalidException

data class User(
    override val id: Long = -1,
    val email: Email,
    val passwordHash: PasswordHash
): BaseEntity{
    override fun toString(): String {
        return "User"
    }
}

data class Email(override val value: String): ValueClass<String>{
    init{
        //TODO: Create validations
        if (!value.contains("@")) throw EmailInvalidException()
    }
}

data class PasswordHash(override val value: String): ValueClass<String>{
    override fun toString(): String {
        return "PasswordHash()"
    }
}