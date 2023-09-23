package ru.netology.nikjob.activity

import android.app.Activity
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.auth.AppAuth
import ru.netology.nikjob.databinding.FragmentNewEventBinding
import ru.netology.nikjob.dto.CreateEventRequest
import ru.netology.nikjob.model.PhotoModel
import ru.netology.nikjob.viewmodel.EventViewModel
import util.DataTime
import util.StringArg
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NewEventFragment : Fragment() {


    companion object {
        var Bundle.textArg: String? by StringArg
    }


    private val viewModel: EventViewModel by activityViewModels()

    private val photoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> Toast.makeText(requireContext(),
                    "Error",
                    Toast.LENGTH_SHORT).show()
                Activity.RESULT_OK -> {
                    val uri = it.data?.data ?: return@registerForActivityResult
                    val file = uri.toFile()
                    viewModel.changePhoto(PhotoModel(uri, file))
                }
            }
        }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentNewEventBinding.inflate(
            inflater,
            container,
            false
        )
        val SAVE_DRAFT_FILENAME = "Draft"
        val SETTING = "settings"
        var dateSelected = ""
        var date:Long? = 0L
        var time = ""
        var dateTime = ""
        val manager = parentFragmentManager
        var savedDraft: SharedPreferences? = null
        var draft: String?
        savedDraft = context?.getSharedPreferences(SAVE_DRAFT_FILENAME, 0)
        val callback = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            draft = binding.editText.text.toString()
            val editor = savedDraft!!.edit()
            editor.putString(SETTING, draft)
            editor.apply()
            Toast.makeText(context, R.string.draftText, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.feedFragment)
        }

        draft = savedDraft!!.getString(SETTING, "").toString()
        binding.editText.setText(draft)
        binding.editText.requestFocus()

        viewModel.photoState.observe(viewLifecycleOwner) { photoModel ->
            if (photoModel == null) {
                binding.photoContainer.isVisible = false
                return@observe
            }
            binding.photoContainer.isVisible = true
            binding.preview.setImageURI(photoModel.uri)
        }


        binding.takePhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }
        binding.remotePhoto.setOnClickListener {
            viewModel.changePhoto(null)
        }
        binding.pickPhoto.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .galleryOnly()
                .compress(2048)
                .createIntent(photoLauncher::launch)
        }

        binding.dateTime.setOnClickListener {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()
            val timePicker = MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setTitleText("Select time")
                .build()
            timePicker.addOnPositiveButtonClickListener {
                val outputDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = TimeZone.getTimeZone("UTC")
                }
                time = timePicker.hour.toString() +":" + timePicker.minute.toString()
                dateSelected = outputDateFormat.format(date)
                dateTime = dateSelected + "T" + time
            }

            datePicker.addOnPositiveButtonClickListener {
                date = datePicker.selection
                timePicker.show(manager, "")
            }

            datePicker.show(manager, "")
        }
        binding.newpost.setOnClickListener {
       val newEvent = CreateEventRequest(
           id = 0,
           content = binding.editText.text.toString(),
           datetime = dateTime,
           type = "OFFLINE"
       )
            viewModel.createNewEvent(newEvent)
            findNavController().navigateUp()
        }

        viewModel.eventCreated.observe(viewLifecycleOwner) {
            viewModel.loadEvents()
            findNavController().navigateUp()
        }

        return binding.root
    }
}