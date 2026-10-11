package com.example.criminalintent

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.example.criminalintent.databinding.FragmentCrimeDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.UUID

class CrimeDetailFragment : Fragment(R.layout.fragment_crime_detail) {
    private val model: CrimeDetailViewModel by viewModels()
    private var binding: FragmentCrimeDetailBinding? = null
    private var photoJob: Job? = null
    private var displayedPhoto: String? = null
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val name = model.pendingPhoto
        model.pendingPhoto = null
        if (name != null) {
            val file = photoFile(name)
            if (success && file.length() > 0) model.setPhoto(name)
            else file.delete()
        }
        if (binding != null && model.ready.value == true) render()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCrimeDetailBinding.bind(view)
        displayedPhoto = null
        val b = binding!!
        b.crimeTitle.doAfterTextChanged { model.setTitle(it?.toString().orEmpty()) }
        b.solved.setOnCheckedChangeListener { _, checked -> model.setSolved(checked) }
        b.dateButton.setOnClickListener {
            if (childFragmentManager.findFragmentByTag("date") == null)
                CrimeDateDialog.create(model.dateMillis).show(childFragmentManager, "date")
        }
        b.timeButton.setOnClickListener {
            if (childFragmentManager.findFragmentByTag("time") == null)
                CrimeTimeDialog.create(model.dateMillis).show(childFragmentManager, "time")
        }
        b.photoButton.setOnClickListener { launchCamera() }
        b.saveButton.setOnClickListener {
            if (model.title.isBlank()) b.crimeTitle.error = getString(R.string.title_required)
            else model.save()
        }
        childFragmentManager.setFragmentResultListener("dateResult", viewLifecycleOwner) { _, result ->
            model.setDate(result.getLong("date")); render()
        }
        childFragmentManager.setFragmentResultListener("confirmResult", viewLifecycleOwner) { _, result ->
            if (result.getBoolean("discard")) requireActivity().finish() else model.delete()
        }
        model.ready.observe(viewLifecycleOwner) { if (it) render() else setEnabled(false) }
        model.busy.observe(viewLifecycleOwner) { setEnabled(!it && model.ready.value == true) }
        model.error.observe(viewLifecycleOwner) { message ->
            if (message != null) { Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show(); model.error.value = null }
        }
        model.finished.observe(viewLifecycleOwner) { if (it) requireActivity().finish() }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (model.busy.value == true) return
                if (model.hasChanges()) showConfirm(true) else requireActivity().finish()
            }
        })
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) { inflater.inflate(R.menu.crime_detail, menu) }
            override fun onMenuItemSelected(item: MenuItem): Boolean {
                if (model.ready.value != true || model.busy.value == true) return false
                return when (item.itemId) {
                    R.id.shareCrime -> { shareReport(); true }
                    R.id.deleteCrime -> { showConfirm(false); true }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        model.load(requireArguments().getString("crimeId")!!)
    }
    private fun showConfirm(discard: Boolean) {
        if (childFragmentManager.findFragmentByTag("confirm") == null)
            ConfirmDialog.create(discard).show(childFragmentManager, "confirm")
    }
    private fun setEnabled(enabled: Boolean) {
        binding?.apply {
            crimeTitle.isEnabled = enabled; solved.isEnabled = enabled
            dateButton.isEnabled = enabled; timeButton.isEnabled = enabled
            photoButton.isEnabled = enabled; saveButton.isEnabled = enabled
        }
    }
    private fun render() {
        val b = binding ?: return
        if (b.crimeTitle.text.toString() != model.title) b.crimeTitle.setText(model.title)
        b.solved.isChecked = model.solved
        b.dateButton.text = DateFormat.getDateInstance(DateFormat.FULL).format(Date(model.dateMillis))
        b.timeButton.text = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(model.dateMillis))
        val name = model.photoName
        if (displayedPhoto != name || b.photo.drawable == null) {
            displayedPhoto = name
            photoJob?.cancel()
            b.photo.setImageDrawable(null)
            b.photoStatus.setText(if (name == null) R.string.no_photo else R.string.loading)
            if (name != null) {
                val file = photoFile(name)
                photoJob = viewLifecycleOwner.lifecycleScope.launch {
                    val bitmap = withContext(Dispatchers.IO) { PhotoLoader.load(file) }
                    binding?.photo?.setImageBitmap(bitmap)
                    binding?.photoStatus?.setText(if (bitmap == null) R.string.photo_unavailable else R.string.photo_attached)
                }
            }
        }
        setEnabled(model.busy.value != true)
    }
    private fun photoFile(name: String): File = File(requireContext().filesDir, "photos/$name")
    private fun launchCamera() {
        try {
            val name = "crime_${UUID.randomUUID()}.jpg"
            val file = photoFile(name)
            check(file.parentFile!!.exists() || file.parentFile!!.mkdirs())
            check(file.createNewFile())
            model.pendingPhoto = name
            val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", file)
            takePhoto.launch(uri)
        } catch (_: ActivityNotFoundException) { cameraError(R.string.camera_unavailable) }
        catch (_: Exception) { cameraError(R.string.camera_failed) }
    }
    private fun cameraError(message: Int) {
        model.pendingPhoto?.let { photoFile(it).delete() }; model.pendingPhoto = null
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
    private fun shareReport() {
        val title = model.title.ifBlank { getString(R.string.untitled) }
        val date = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.SHORT).format(Date(model.dateMillis))
        val status = getString(if (model.solved) R.string.solved_status else R.string.open_status)
        val photo = getString(if (model.photoName == null) R.string.photo_none else R.string.photo_attached)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.report_title, title))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.report_body, title, date, status, photo))
        }
        try { startActivity(Intent.createChooser(intent, getString(R.string.share_chooser))) }
        catch (_: ActivityNotFoundException) { Toast.makeText(requireContext(), R.string.share_unavailable, Toast.LENGTH_LONG).show() }
    }
    override fun onDestroyView() {
        photoJob?.cancel(); binding = null
        super.onDestroyView()
    }
}
