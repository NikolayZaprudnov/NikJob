package ru.netology.nikjob.activity

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import ru.netology.nikjob.databinding.FragmentNewJobBinding
import ru.netology.nikjob.dto.Job
import ru.netology.nikjob.viewmodel.UserJobViewModel
import java.util.*

class NewJobFragment : Fragment() {

    private val viewModel: UserJobViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val binding = FragmentNewJobBinding.inflate(
            inflater,
            container,
            false
        )

        val manager = parentFragmentManager
        var dateTime = ""
        var dateTimeFinish = ""
        var dateSelected = ""
        var date:Long? = 0L
        var time = ""


        binding.textNameJob.requestFocus()
        binding.textStartJob.setOnClickListener {
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


        binding.textFinishJob.setOnClickListener {
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
                dateTimeFinish = dateSelected + "T" + time
            }

            datePicker.addOnPositiveButtonClickListener {
                date = datePicker.selection
                timePicker.show(manager, "")
            }
            datePicker.show(manager, "")
        }
        binding.newpost.setOnClickListener {
            val newJob = Job(
                id = 0,
                name = binding.textNameJob.text.toString(),
                position = binding.textPositionJob.text.toString(),
                start = dateTime,
                finish = dateTimeFinish,
                link = binding.textLinkJob.text.toString(),
            )
            viewModel.createJob(newJob)
            findNavController().navigateUp()
        }
        return binding.root
    }
}