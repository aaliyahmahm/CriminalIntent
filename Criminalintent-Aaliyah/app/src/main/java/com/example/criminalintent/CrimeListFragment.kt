package com.example.criminalintent

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.criminalintent.databinding.FragmentCrimeListBinding
import java.util.UUID

class CrimeListFragment : Fragment(R.layout.fragment_crime_list) {
    private val model: CrimeListViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = FragmentCrimeListBinding.bind(view)
        val adapter = CrimeAdapter { crime ->
            startActivity(CrimeDetailActivity.newIntent(requireContext(), crime.id))
        }
        binding.crimeList.layoutManager = LinearLayoutManager(requireContext())
        binding.crimeList.adapter = adapter
        model.crimes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            binding.emptyText.isVisible = it.isEmpty()
        }
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) { inflater.inflate(R.menu.crime_list, menu) }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                if (item.itemId != R.id.newCrime) return false
                startActivity(CrimeDetailActivity.newIntent(requireContext(), UUID.randomUUID().toString(), true))
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}
