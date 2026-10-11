package com.example.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.core.os.bundleOf
import java.util.Calendar

class CrimeDateDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance().apply { timeInMillis = requireArguments().getLong("date") }
        return DatePickerDialog(requireContext(), { _, year, month, day ->
            calendar.set(Calendar.YEAR, year); calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, day)
            parentFragmentManager.setFragmentResult("dateResult", bundleOf("date" to calendar.timeInMillis))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
    }
    companion object {
        fun create(date: Long) = CrimeDateDialog().apply { arguments = bundleOf("date" to date) }
    }
}
