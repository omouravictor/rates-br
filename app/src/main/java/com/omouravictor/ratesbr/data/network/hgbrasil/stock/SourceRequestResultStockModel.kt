package com.omouravictor.ratesbr.data.network.hgbrasil.stock

import com.google.gson.annotations.SerializedName

data class SourceRequestResultStockModel(
    @SerializedName("stocks")
    val resultsStocks: LinkedHashMap<String, SourceRequestStockItemModel>
)