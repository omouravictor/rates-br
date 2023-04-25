package com.omouravictor.ratesbr.data.network

import com.omouravictor.ratesbr.data.network.hgbrasil.bitcoin.NetworkBitcoinsResponse
import com.omouravictor.ratesbr.data.network.hgbrasil.rates.NetworkRatesResponse
import com.omouravictor.ratesbr.data.network.hgbrasil.stock.NetworkStocksResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("finance")
    suspend fun getRates(
        @Query("fields") field: String
    ): NetworkRatesResponse

    @GET("finance")
    suspend fun getBitcoins(
        @Query("fields") field: String
    ): NetworkBitcoinsResponse

    @GET("finance")
    suspend fun getStocks(
        @Query("fields") field: String
    ): NetworkStocksResponse
}