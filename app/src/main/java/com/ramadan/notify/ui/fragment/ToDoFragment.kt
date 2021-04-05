package com.ramadan.notify.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.ramadan.notify.R
import com.ramadan.notify.ui.viewModel.ToDoViewModel


class ToDoFragment : DialogFragment() {
    private val viewModel by lazy { ViewModelProvider(this).get(ToDoViewModel::class.java) }

    @SuppressLint("SetTextI18n")
    @NonNull
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(activity)
        val view: View = activity!!.layoutInflater.inflate(R.layout.dialog_edit_text, null)
        dialogBuilder.setView(view)
        val alertDialog = dialogBuilder.create()
        alertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.window!!.attributes.windowAnimations = R.style.SlideAnimation
        view.findViewById<TextView>(R.id.title).text = "Add ToDo"
        val name = view.findViewById<EditText>(R.id.input)
        val add = view.findViewById<TextView>(R.id.confirm)
        val cancel = view.findViewById<TextView>(R.id.cancel)
        add.setOnClickListener {
            if (!name.text.isNullOrEmpty())
                viewModel.insertToDo(view.context, name.text.toString())
            alertDialog.cancel()
        }
        cancel.setOnClickListener { alertDialog.cancel() }
        return alertDialog
    }


}