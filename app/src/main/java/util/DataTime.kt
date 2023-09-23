package util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import androidx.fragment.app.FragmentManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.coroutineScope
import ru.netology.nikjob.activity.AppActivity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.coroutines.coroutineContext

object DataTime {


    fun dataTimeDialog(manager: FragmentManager): String{
        var dateSelected = ""
        var date:Long? = 0L
        var time = ""
        var dateTime = ""

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
        while (true){
        datePicker.show(manager, "")
      if (!datePicker.showsDialog) break
        }
        return dateTime
    }

}