package com.example.criminalintent

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment

class ConfirmDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val discard = requireArguments().getBoolean("discard")
        return AlertDialog.Builder(requireContext())
            .setTitle(if (discard) R.string.discard else R.string.delete)
            .setMessage(if (discard) R.string.discard_question else R.string.delete_question)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(if (discard) R.string.leave else R.string.delete) { _, _ ->
                parentFragmentManager.setFragmentResult("confirmResult", bundleOf("discard" to discard))
            }.create()
    }
    companion object {
        fun create(discard: Boolean) = ConfirmDialog().apply { arguments = bundleOf("discard" to discard) }
    }
}
