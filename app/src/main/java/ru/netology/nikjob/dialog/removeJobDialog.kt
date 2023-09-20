package ru.netology.nikjob.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import ru.netology.nikjob.R

class removeJobDialog : DialogFragment() {
   interface DialogListener{
       fun ODialogPositiveClick(data:Boolean)
   }
    private lateinit var listener : DialogListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(getString(R.string.are_you_sure))
                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                    val data = true
                    listener.ODialogPositiveClick(data)

                }
                .setNegativeButton(getString(R.string.no)) { dialog, id ->
                    dialog.cancel()
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}