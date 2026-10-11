package com.example.criminalintent

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import androidx.core.os.bundleOf
import java.util.Calendar

class CrimeTimeDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance().apply { timeInMillis = requireArguments().getLong("date") }
        return TimePickerDialog(requireContext(), { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour); calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
            parentFragmentManager.setFragmentResult("dateResult", bundleOf("date" to calendar.timeInMillis))
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), DateFormat.is24HourFormat(requireContext()))
    }
    companion object {
        fun create(date: Long) = CrimeTimeDialog().apply { arguments = bundleOf("date" to date) }
    }
}
