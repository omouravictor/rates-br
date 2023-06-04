package com.omouravictor.ratesbr.data.repository

import com.omouravictor.ratesbr.data.local.dao.RateDao
import com.omouravictor.ratesbr.data.local.entity.RateEntity
import com.omouravictor.ratesbr.data.network.base.NetworkResultStatus
import com.omouravictor.ratesbr.data.network.hgfinanceapi.ApiService
import com.omouravictor.ratesbr.data.network.hgfinanceapi.rates.ApiRatesResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.Date

class RatesRepository(
    private val rateDao: RateDao,
    private val apiService: ApiService
) {
    fun getLocalRates(): List<RateEntity> = rateDao.getAllRates()

    suspend fun insertRates(rateEntityList: List<RateEntity>) {
        rateDao.insertRates(rateEntityList)
    }

    suspend fun getRemoteRates(fields: String): Flow<NetworkResultStatus<ApiRatesResponse>> =
        flow {
            emit(NetworkResultStatus.Loading)
            try {
                val networkRatesResponse = apiService.getRates(fields)
                    .apply { rateDate = Date() }
                emit(NetworkResultStatus.Success(networkRatesResponse))
            } catch (e: Exception) {
                emit(NetworkResultStatus.Error("Falha ao buscar os dados na internet :("))
            }
        }
}