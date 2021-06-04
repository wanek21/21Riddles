package martian.riddles.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import martian.riddles.R;

public class AssistentDialog extends DialogFragment {


    public static final int DIALOG_RULES = 1;
    public static final int DIALOG_ALERT_INTERNET = 2;
    public static final int DIALOG_ALERT_LAST_LVL = 3;
    public static final int DIALOG_REVIEW = 4;
    public static final int DIALOG_SERVER_ERROR = 5;
    public static final int DIALOG_UPDATE_APP = 6;
    public static final int DIALOG_UPDATE_APP_TECH = 7;
    public static final int DIALOG_CHECK_ON_SERRVER_ALERT = 8;
    public static final int DIALOG_NO_TELEGRAM = 9;
    public static final int DIALOG_FORCE_UPDATE = 10;

    int typeDialog;
    String message;

    public static final String assist = "e.g.;"; // эта переменная используется для шифрования логина при отправке на сервер; не используется в данном классе(для запутывания злоумышленников)

    public AssistentDialog(int typeDialog) {
        this.typeDialog = typeDialog;
    }
    public AssistentDialog(int typeDialog, String msg) {
        this.typeDialog = typeDialog;
        this.message = msg;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        switch (typeDialog) {
            case DIALOG_ALERT_INTERNET: {
                builder.setTitle(R.string.no_internet)
                        .setPositiveButton(R.string.read_rules_ok_btn, null);
                break;
            }
            case DIALOG_SERVER_ERROR: {
                builder.setTitle(R.string.no_server)
                        .setPositiveButton(R.string.read_rules_ok_btn, null);
                break;
            }
            case DIALOG_CHECK_ON_SERRVER_ALERT: {
                builder.setTitle(R.string.alert_check_on_server_title)
                        .setMessage(R.string.alert_check_on_server)
                        .setPositiveButton(R.string.read_rules_ok_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                break;
            }
            case DIALOG_NO_TELEGRAM: {
                builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
                builder.setMessage(R.string.there_is_no_telegram)
                        .setPositiveButton(R.string.install_telegram_btn, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.telegram.messenger")));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=org.telegram.messenger")));
                                }
                            }
                        })
                        .setNeutralButton(R.string.dialog_review_no, null);
                break;
            }
            case DIALOG_REVIEW: {
                builder.setMessage(R.string.dialog_review_mes)
                        .setPositiveButton(R.string.dialog_review_yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        })
                        .setNegativeButton(R.string.dialog_review_no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                break;
            }
            case DIALOG_UPDATE_APP: {
                builder.setMessage(R.string.force_update_season)
                        .setPositiveButton(R.string.update_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        });
                break;
            }
            case DIALOG_FORCE_UPDATE: {
                builder.setMessage(R.string.force_update_tech)
                        .setPositiveButton(R.string.update_btn, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String appPackageName = getActivity().getPackageName();
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        });
                break;
            }
        }

        return builder.create();
    }
}
