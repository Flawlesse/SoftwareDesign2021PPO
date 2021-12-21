package com.flawlesse.opzcalculator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.flawlesse.opzcalculator.databinding.FragmentScientificFunctionsBinding
import com.google.android.material.snackbar.Snackbar

class ScientificFunctionsFragment: Fragment() {
    private lateinit var binding: FragmentScientificFunctionsBinding
    private val expressionVM: ExpressionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScientificFunctionsBinding
            .inflate(inflater, container, false)
            .apply {
                cosBtn.setOnClickListener { onClick("cos(") }
                sinBtn.setOnClickListener { onClick("sin(") }
                eBtn.setOnClickListener { onClick("E") }
                powBtn.setOnClickListener { onClick("^(") }
                tanBtn.setOnClickListener { onClick("tan(") }
                ctgBtn.setOnClickListener { onClick("ctg(") }
                piBtn.setOnClickListener { onClick("π") }
                sqrtBtn.setOnClickListener { onClick("√(") }
            }
        return binding.root
    }

    private fun onClick(token: String) {
        if (!expressionVM.updateWithToken(token)){
            Snackbar.make(requireView(), "Неправильный ввод!", Snackbar.LENGTH_SHORT).show()
        }
    }
}