package martian.riddles.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import martian.riddles.R
import android.content.DialogInterface
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.fragment.app.DialogFragment

class AssistantDialog(var typeDialog: Int) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var builder = AlertDialog.Builder(activity)
        when (typeDialog) {
            DIALOG_ALERT_INTERNET -> {
                builder.setTitle(R.string.no_internet)
                    .setPositiveButton(R.string.read_rules_ok_btn, null)
            }
            DIALOG_SERVER_ERROR -> {
                builder.setTitle(R.string.no_server)
                    .setPositiveButton(R.string.read_rules_ok_btn, null)
            }
            DIALOG_CHECK_ON_SERRVER_ALERT -> {
                builder.setTitle(R.string.alert_check_on_server_title)
                    .setMessage(R.string.alert_check_on_server)
                    .setPositiveButton(R.string.read_rules_ok_btn) { dialog: DialogInterface?, which: Int -> }
            }
            DIALOG_NO_TELEGRAM -> {
                builder = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                builder.setMessage(R.string.there_is_no_telegram)
                    .setPositiveButton(R.string.install_telegram_btn) { dialog: DialogInterface?, which: Int ->
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=org.telegram.messenger")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")
                                )
                            )
                        }
                    }
                    .setNeutralButton(R.string.dialog_review_no, null)
            }
            DIALOG_REVIEW -> {
                builder.setMessage(R.string.dialog_review_mes)
                    .setPositiveButton(R.string.dialog_review_yes) { dialog: DialogInterface?, which: Int ->
                        val appPackageName = requireActivity().packageName
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                    }
                    .setNegativeButton(R.string.dialog_review_no) { dialog: DialogInterface, id: Int -> dialog.cancel() }
            }
            DIALOG_UPDATE_APP -> {
                builder.setMessage(R.string.force_update_season)
                    .setPositiveButton(R.string.update_btn) { dialog: DialogInterface?, id: Int ->
                        val appPackageName = requireActivity().packageName
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                    }
            }
            DIALOG_FORCE_UPDATE -> {
                builder.setMessage(R.string.force_update_tech)
                    .setPositiveButton(R.string.update_btn) { dialog: DialogInterface?, id: Int ->
                        val appPackageName = requireActivity().packageName
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (anfe: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                    }
            }
        }
        return builder.create()
    }

    companion object {
        const val DIALOG_ALERT_INTERNET = 2
        const val DIALOG_REVIEW = 4
        const val DIALOG_SERVER_ERROR = 5
        const val DIALOG_UPDATE_APP = 6
        const val DIALOG_CHECK_ON_SERRVER_ALERT = 8
        const val DIALOG_NO_TELEGRAM = 9
        const val DIALOG_FORCE_UPDATE = 10
    }
}