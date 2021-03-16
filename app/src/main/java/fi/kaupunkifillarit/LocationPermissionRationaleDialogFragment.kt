package fi.kaupunkifillarit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class LocationPermissionRationaleDialogFragment : DialogFragment() {
    var onPositiveButtonClickListener: ((DialogInterface) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.location_permission_rationale_title)
                .setMessage(R.string.location_permission_rationale_message)
                .setPositiveButton(R.string.location_permission_rationale_ok) { dialog, _ ->
                    onPositiveButtonClickListener?.invoke(dialog)
                }
                .create()

        isCancelable = false
        dialog.setCanceledOnTouchOutside(false)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        isCancelable = false
        return view
    }
}