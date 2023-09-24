package ru.netology.nikjob.activity

import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nikjob.R
import ru.netology.nikjob.databinding.FragmentNewEventBinding
import ru.netology.nikjob.viewmodel.EventViewModel
import util.StringArg
import java.util.*

@AndroidEntryPoint
class EditEventFragment: Fragment() {
    companion object {
        var Bundle.textArg: String? by StringArg
    }

    private val viewModel: EventViewModel by activityViewModels()

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
        var dateSelected = ""
        var date:Long? = 0L
        var time = ""
        var dateTime = ""
        val manager = parentFragmentManager

        binding.newpost.visibility = View.GONE
        binding.editText.setText(arguments?.textArg.toString())
        binding.editText.requestFocus()

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
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.new_post_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.ok -> {
                        viewModel.changeContent(binding.editText.text.toString(), dateTime)
                        viewModel.save()
                        findNavController().navigateUp()
                        true
                    }
                    R.id.not -> {
                        findNavController().navigateUp()
                    }
                    else -> false

                }


        }, viewLifecycleOwner)
        return binding.root
    }
}