package fi.kaupunkifillarit

import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import kotlinx.coroutines.Deferred

class LocationPermissionRationaleDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog =
            AlertDialog.Builder(requireContext(), R.style.AppTheme_AlertDialog)
                .setTitle(R.string.location_permission_rationale_title)
                .setMessage(R.string.location_permission_rationale_message)
                .setPositiveButton(R.string.location_permission_rationale_ok) { _, _ ->
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