package com.flawlesse.opzcalculator

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.flawlesse.opzcalculator.databinding.FragmentBasicFunctionsBinding
import com.google.android.material.snackbar.Snackbar

class BasicFunctionsFragment : Fragment() {
    private lateinit var binding: FragmentBasicFunctionsBinding
    private val expressionVM: ExpressionViewModel by activityViewModels()
    private var scienceAllowed: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBasicFunctionsBinding
            .inflate(inflater, container, false)
            .apply {
                divideOpBtn.setOnClickListener { onOperationClick("/") }
                multiplyOpBtn.setOnClickListener { onOperationClick("*") }
                minusOpBtn.setOnClickListener { onOperationClick("-") }
                plusOpBtn.setOnClickListener { onOperationClick("+") }
                leftScopeBtn.setOnClickListener { onOperationClick("(") }
                rightScopeBtn.setOnClickListener { onOperationClick(")") }
                dotBtn.setOnClickListener { onOperationClick(".") }

                num0Btn.setOnClickListener { onOperationClick("0") }
                num1Btn.setOnClickListener { onOperationClick("1") }
                num2Btn.setOnClickListener { onOperationClick("2") }
                num3Btn.setOnClickListener { onOperationClick("3") }
                num4Btn.setOnClickListener { onOperationClick("4") }
                num5Btn.setOnClickListener { onOperationClick("5") }
                num6Btn.setOnClickListener { onOperationClick("6") }
                num7Btn.setOnClickListener { onOperationClick("7") }
                num8Btn.setOnClickListener { onOperationClick("8") }
                num9Btn.setOnClickListener { onOperationClick("9") }

                toggleSciBtn.setOnClickListener { toggleScientificFuncs() }
                clrBtn.setOnClickListener { removeToken() }
                clrBtn.setOnLongClickListener { clearAllTokens(); true }

                eqBtn.setOnClickListener{ onCalculate() }

                if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
                {
                    toggleSciBtn.isEnabled = false
                }
            }

        return binding.root
    }

    private fun clearAllTokens() {
        expressionVM.clearAll()
    }

    private fun removeToken() {
        expressionVM.clearToken()
    }

    private fun toggleScientificFuncs() {
        val sciFragment = requireActivity().supportFragmentManager
            .findFragmentById(R.id.scientificFunctionsFragmentContainerView) as ScientificFunctionsFragment?

        if (scienceAllowed)
            requireActivity().supportFragmentManager.beginTransaction()
                .remove(sciFragment!!)
                .commit()
        else
            requireActivity().supportFragmentManager.beginTransaction()
                .add(R.id.scientificFunctionsFragmentContainerView, ScientificFunctionsFragment::class.java, null)
                .commit()
        scienceAllowed = !scienceAllowed
    }

    private fun onOperationClick(token: String) {
        if (!expressionVM.updateWithToken(token)) {
            Snackbar.make(requireView(), "Неправильный ввод!", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onCalculate(){
        try {
            expressionVM.calculate()
        } catch (ex: ArithmeticException) {
            Snackbar.make(requireView(), ex.message.toString(), Snackbar.LENGTH_SHORT).show()
        }
    }
}