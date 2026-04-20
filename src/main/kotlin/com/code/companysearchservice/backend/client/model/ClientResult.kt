package com.code.companysearchservice.backend.client.model

import com.code.companysearchservice.backend.service.model.ServiceStatus

sealed class ClientResult<out T> {
    data class Success<T>(val data: T) : ClientResult<T>()
    data object ServiceUnavailable : ClientResult<Nothing>()
}

fun <T> ClientResult<List<T>>.toServiceStatus(): ServiceStatus = when (this) {
    is ClientResult.Success -> if (data.isNotEmpty()) ServiceStatus.SUCCESS else ServiceStatus.EMPTY
    is ClientResult.ServiceUnavailable -> ServiceStatus.UNAVAILABLE
}
