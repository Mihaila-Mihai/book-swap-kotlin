package ro.example.bookswap.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import ro.example.bookswap.R

class ImageChangeMethodDialogFragment: DialogFragment() {


    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            builder.setView(inflater.inflate(R.layout.image_change_method_dialog, null))

            builder.create()



        } ?: throw IllegalStateException("Activity cannot be null")
    }


}