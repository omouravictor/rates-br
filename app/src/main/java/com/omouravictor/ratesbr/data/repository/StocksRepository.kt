package com.omouravictor.ratesbr.data.repository

import com.omouravictor.ratesbr.data.local.AppDataBase
import com.omouravictor.ratesbr.data.local.entity.StockEntity
import com.omouravictor.ratesbr.data.network.ApiService
import com.omouravictor.ratesbr.data.network.base.NetworkResultStatus
import com.omouravictor.ratesbr.data.network.hgbrasil.stock.SourceRequestStockModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class StocksLocalRepository(private val database: AppDataBase) {
    fun getLocalStocks(): Flow<List<StockEntity>> = database.stockDao().getAllStocks()

    suspend fun insertStocks(listStockEntity: List<StockEntity>) {
        database.stockDao().insertStocks(listStockEntity)
    }
}

class StocksApiRepository(private val apiService: ApiService) {
    suspend fun getRemoteStocks(fields: String): Flow<NetworkResultStatus<SourceRequestStockModel>> {
        return withContext(Dispatchers.IO) {
            flow {
                emit(NetworkResultStatus.Loading)
                try {
                    val request = apiService.getStocks(fields)
                    emit(NetworkResultStatus.Success(request))
                } catch (e: Exception) {
                    emit(NetworkResultStatus.Error(Exception("Falha ao buscar os dados da internet :(")))
                }
            }
        }
    }
}