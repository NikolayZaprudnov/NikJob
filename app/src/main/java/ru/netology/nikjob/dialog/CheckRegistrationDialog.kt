package ru.netology.nikjob.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import ru.netology.nikjob.R

class CheckRegistrationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Необходимо войти / зарегистрироваться")
                .setPositiveButton("Войти") { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                }
                .setNegativeButton("Зарегистрироваться") { _, _ ->
                    findNavController().navigate(R.id.action_feedFragment_to_registrationFragment)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}