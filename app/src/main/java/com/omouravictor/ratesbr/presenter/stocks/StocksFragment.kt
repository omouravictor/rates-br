package com.omouravictor.ratesbr.presenter.stocks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omouravictor.ratesbr.databinding.FragmentStocksBinding
import com.omouravictor.ratesbr.presenter.base.UiResultState
import com.omouravictor.ratesbr.presenter.stocks.model.StockUiModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StocksFragment : Fragment() {

    private lateinit var stockBinding: FragmentStocksBinding
    private val stockViewModel: StocksViewModel by activityViewModels()
    private lateinit var stockAdapter: StocksAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        stockBinding = FragmentStocksBinding.inflate(layoutInflater, container, false)
        return stockBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initSwipeRefreshLayout()

        stockViewModel.stocks.observe(viewLifecycleOwner) {
            when (it) {
                is UiResultState.Success -> {
                    initAdapter(it.data)
                    stockBinding.swipeRefreshLayout.isRefreshing = false
                    stockBinding.progressBar.isVisible = false
                    stockBinding.rvStocks.isVisible = true
                }
                is UiResultState.Error -> {
                    stockBinding.swipeRefreshLayout.isRefreshing = false
                    stockBinding.progressBar.isVisible = false
                    stockBinding.rvStocks.isVisible = false
                    Toast.makeText(context, it.e.message, Toast.LENGTH_SHORT).show()
                }
                is UiResultState.Loading -> {
                    stockBinding.swipeRefreshLayout.isRefreshing = false
                    stockBinding.progressBar.isVisible = true
                    stockBinding.rvStocks.isVisible = false
                }
            }
        }
    }

    private fun initSwipeRefreshLayout() {
        stockBinding.swipeRefreshLayout.setOnRefreshListener {
            stockViewModel.getStocks()
        }
    }

    private fun initAdapter(stockList: List<StockUiModel>) {
        stockAdapter = StocksAdapter(stockList)

        stockBinding.rvStocks.apply {
            adapter = stockAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }
}