package com.omouravictor.ratesbr.presenter.rates

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omouravictor.ratesbr.R
import com.omouravictor.ratesbr.databinding.FragmentRatesBinding
import com.omouravictor.ratesbr.presenter.base.UiResultStatus
import com.omouravictor.ratesbr.presenter.converter.ConverterViewModel
import com.omouravictor.ratesbr.presenter.rates.model.RateUiModel
import com.omouravictor.ratesbr.util.BrazilianFormats.currencyFormat
import com.omouravictor.ratesbr.util.BrazilianFormats.dateFormat
import com.omouravictor.ratesbr.util.BrazilianFormats.timeFormat
import com.omouravictor.ratesbr.util.StringUtils.getVariationText

class RatesFragment : Fragment() {

    private lateinit var rateBottomSheetDialog: Dialog
    private lateinit var rateDetailsDialog: Dialog
    private lateinit var binding: FragmentRatesBinding
    private val ratesViewModel: RatesViewModel by activityViewModels()
    private val converterViewModel: ConverterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRatesBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initRateBottomSheetDialog()
        initRateDetailsDialog()
        initSwipeRefreshLayout()

        ratesViewModel.ratesResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is UiResultStatus.Success -> {
                    configureRecyclerView(result.data)
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.recyclerViewRates.isVisible = true
                    binding.includeViewError.root.isVisible = false
                }
                is UiResultStatus.Error -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.recyclerViewRates.isVisible = false
                    binding.includeViewError.root.isVisible = true
                    binding.includeViewError.textViewErrorMessage.text = result.e.message
                }
                is UiResultStatus.Loading -> {
                    binding.swipeRefreshLayout.isRefreshing = true
                    binding.recyclerViewRates.isVisible = false
                    binding.includeViewError.root.isVisible = false
                }
            }
        }
    }

    private fun initRateBottomSheetDialog() {
        rateBottomSheetDialog = Dialog(requireContext())
        rateBottomSheetDialog.setContentView(R.layout.bottom_sheet_rate_dialog)
        rateBottomSheetDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        rateBottomSheetDialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
        rateBottomSheetDialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun initRateDetailsDialog() {
        rateDetailsDialog = Dialog(requireContext())
        rateDetailsDialog.setContentView(R.layout.details_rate_dialog)
        rateDetailsDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        rateDetailsDialog.window?.setLayout(MATCH_PARENT, WRAP_CONTENT)
    }

    private fun initSwipeRefreshLayout() {
        val greenColor = ContextCompat.getColor(requireContext(), R.color.green)
        binding.swipeRefreshLayout.setColorSchemeColors(greenColor, greenColor, greenColor)
        binding.swipeRefreshLayout.setOnRefreshListener { ratesViewModel.getRates() }
    }

    private fun configureRecyclerView(ratesList: List<RateUiModel>) {
        binding.recyclerViewRates.apply {
            adapter = RatesAdapter(ratesList) { showRateBottomSheetDialog(it) }
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun showRateBottomSheetDialog(rateUiModel: RateUiModel) {
        with(rateBottomSheetDialog) {
            val textViewCurrencyName =
                findViewById<TextView>(R.id.textViewCurrencyNameBottomSheetRateDialog)
            val layoutConverter =
                findViewById<ConstraintLayout>(R.id.converterLayoutBottomSheetRateDialog)
            val layoutDetails =
                findViewById<ConstraintLayout>(R.id.detailsLayoutBottomSheetRateDialog)

            textViewCurrencyName.text = rateUiModel.currencyName

            layoutConverter.setOnClickListener {
                rateBottomSheetDialog.dismiss()
                prepareAndNavigateToConverterFragment(rateUiModel)
            }

            layoutDetails.setOnClickListener {
                rateBottomSheetDialog.dismiss()
                showRateDetailsDialog(rateUiModel)
            }

            show()
        }
    }

    private fun prepareAndNavigateToConverterFragment(rateUiModel: RateUiModel) {
        converterViewModel.setInitialValues(rateUiModel)
        val action = RatesFragmentDirections.actionRatesFragmentToConverterFragment()
        findNavController().navigate(action)
    }

    private fun showRateDetailsDialog(rate: RateUiModel) {
        with(rateDetailsDialog) {
            val nameTextView = findViewById<TextView>(R.id.textViewRatePopupName)
            val currencyTermTextView = findViewById<TextView>(R.id.textViewRatePopupCurrencyTerm)
            val unitaryValueTextView = findViewById<TextView>(R.id.textViewRatePopupUnitaryValue)
            val variationTextView = findViewById<TextView>(R.id.textViewRatePopupVariation)
            val dateTimeTextView = findViewById<TextView>(R.id.textViewRatePopupDateTime)

            nameTextView.text = rate.currencyName
            currencyTermTextView.text = rate.currencyTerm
            unitaryValueTextView.text = currencyFormat.format(rate.unitaryRate)
            variationTextView.text = getVariationText(rate.variation)
            dateTimeTextView.text = getString(
                R.string.popup_date_time,
                dateFormat.format(rate.rateDate),
                timeFormat.format(rate.rateDate)
            )

            show()
        }
    }

}