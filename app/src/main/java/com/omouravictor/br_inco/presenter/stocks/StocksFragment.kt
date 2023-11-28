package com.omouravictor.br_inco.presenter.stocks

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils.loadLayoutAnimation
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omouravictor.br_inco.R
import com.omouravictor.br_inco.databinding.FragmentInfoCardsBinding
import com.omouravictor.br_inco.presenter.base.DataSource
import com.omouravictor.br_inco.presenter.base.OptionsMenu
import com.omouravictor.br_inco.presenter.base.UiResultStatus
import com.omouravictor.br_inco.presenter.stocks.model.StockUiModel
import com.omouravictor.br_inco.util.FormatUtils.BrazilianFormats.brDateFormat
import com.omouravictor.br_inco.util.FormatUtils.BrazilianFormats.brNumberFormat
import com.omouravictor.br_inco.util.FormatUtils.BrazilianFormats.brTimeFormat
import com.omouravictor.br_inco.util.StringUtils.getVariationText

class StocksFragment : Fragment() {

    private lateinit var stockDetailsDialog: Dialog
    private lateinit var binding: FragmentInfoCardsBinding
    private val optionsMenu = OptionsMenu()
    private val stockViewModel: StocksViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentInfoCardsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initOptionsMenu()
        initStockDetailsDialog()
        initSwipeRefreshLayout()

        observeStocksResult()
    }

    override fun onResume() {
        super.onResume()
        (binding.recyclerView.adapter as? StocksAdapter)?.filterList("")
    }

    private fun observeStocksResult() {
        stockViewModel.stocksResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UiResultStatus.Success -> handleUiSuccessResult(result.data)
                is UiResultStatus.Error -> handleUiErrorResult(result.message)
                is UiResultStatus.Loading -> handleUiLoadingResult()
            }
        }
    }

    private fun handleUiSuccessResult(data: Pair<List<StockUiModel>, DataSource>) {
        configRecyclerView(data.first)
        configSwipeRefreshLayout(data.second)
        binding.swipeRefreshLayout.isRefreshing = false
        binding.recyclerView.isVisible = true
        binding.includeViewError.root.isVisible = false
    }

    private fun handleUiErrorResult(message: String) {
        binding.swipeRefreshLayout.isRefreshing = false
        binding.recyclerView.isVisible = false
        binding.includeViewError.root.isVisible = true
        binding.includeViewError.textViewErrorMessage.text = message
    }

    private fun handleUiLoadingResult() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.recyclerView.isVisible = false
        binding.includeViewError.root.isVisible = false
    }

    private fun initOptionsMenu() {
        optionsMenu.addOptionsMenu(requireActivity(), viewLifecycleOwner) { text ->
            (binding.recyclerView.adapter as? StocksAdapter)?.filterList(text)
        }
    }

    private fun initStockDetailsDialog() {
        stockDetailsDialog = Dialog(requireContext())
        stockDetailsDialog.setContentView(R.layout.details_stock_dialog)
        stockDetailsDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        stockDetailsDialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        stockDetailsDialog.window?.attributes?.windowAnimations =
            R.style.Animation_Design_BottomSheetDialog
    }

    private fun initSwipeRefreshLayout() {
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
        binding.swipeRefreshLayout.setColorSchemeColors(greenColor, greenColor, greenColor)
        binding.swipeRefreshLayout.setOnRefreshListener {
            (optionsMenu.searchMenuItem.actionView as SearchView).onActionViewCollapsed()
            stockViewModel.getStocks()
        }
    }

    private fun configRecyclerView(stockList: List<StockUiModel>) {
        binding.recyclerView.apply {
            layoutAnimation = loadLayoutAnimation(context, R.anim.layout_animation)
            adapter = StocksAdapter(stockList) { showStockDetailsDialog(it) }
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun configSwipeRefreshLayout(dataSource: DataSource) {
        binding.swipeRefreshLayout.isEnabled = dataSource == DataSource.LOCAL
    }

    private fun showStockDetailsDialog(stockUiModel: StockUiModel) {
        with(stockDetailsDialog) {
            val nameTextView = findViewById<TextView>(R.id.textViewStockPopupName)
            val fullNameTextView = findViewById<TextView>(R.id.textViewStockPopupFullName)
            val locationTextView = findViewById<TextView>(R.id.textViewStockPopupLocation)
            val pointsTextView = findViewById<TextView>(R.id.textViewStockPopupPoints)
            val variationTextView = findViewById<TextView>(R.id.textViewStockPopupVariation)
            val dateTimeTextView = findViewById<TextView>(R.id.textViewStockPopupDateTime)

            nameTextView.text = stockUiModel.name
            fullNameTextView.text = stockUiModel.fullName
            locationTextView.text = getString(
                R.string.stock_popup_full_location,
                stockUiModel.cityLocation,
                stockUiModel.countryLocation
            )
            pointsTextView.text = brNumberFormat.format(stockUiModel.points)
            variationTextView.text = getVariationText(stockUiModel.variation)
            dateTimeTextView.text = getString(
                R.string.popup_date_time,
                brDateFormat.format(stockUiModel.stockDate),
                brTimeFormat.format(stockUiModel.stockDate)
            )

            show()
        }
    }
}