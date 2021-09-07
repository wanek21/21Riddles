package martian.riddles.ui;

import android.animation.ObjectAnimator;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;

import martian.riddles.BuildConfig;
import martian.riddles.R;
import martian.riddles.data.remote.RequestController;
import martian.riddles.domain.StatisticsController;
import martian.riddles.dto.ResponseFromServer;

public class InfoActivity extends AppCompatActivity {

    private Button btnTelegram;
    private TextView tvInfoUpdate;
    private Button btnUpdate;
    private Button btnReview;

    private StatisticsController statisticsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        btnTelegram = findViewById(R.id.btnTelegram);
        tvInfoUpdate = findViewById(R.id.tvInfoUpdate);
        btnUpdate = findViewById(R.id.btnUpdateApp);
        btnReview = findViewById(R.id.btnReview);

        btnReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });
        if(btnTelegram != null) { // если кнопка телеграма есть в этой локализации
            btnTelegram.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        statisticsController.joinGroup();
                        String uri = "tg://resolve?domain=twenty_one_riddles";
                        if(Locale.getDefault().getLanguage().equals("ru")) uri = "tg://resolve?domain=twenty_one_riddles_ru";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(intent);
                    } catch (ActivityNotFoundException ex) {
                        AssistentDialog assistentDialog = new AssistentDialog(AssistentDialog.DIALOG_NO_TELEGRAM);
                        assistentDialog.show(getSupportFragmentManager(),null);
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("", "twenty_one_riddles");
                        clipboard.setPrimaryClip(clip);
                    }
                }
            });

            statisticsController = new StatisticsController(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        new CheckUpdateTask().execute();
    }

    private class CheckUpdateTask extends AsyncTask<Void,Void,Boolean> { // проверяет принудительные обновления

        int typeUpdate; // тип актуальности версии
        private final int CURRENT = 1;
        private final int FORCE_UPDATE = 6;
        private final int SOFT_UPDATE = 5;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            /*try {
                ResponseFromServer responseFromServer = RequestController.Companion
                        .getInstance()
                        .getApiService(InfoActivity.this)
                        .checkUpdate(BuildConfig.VERSION_CODE)
                        .execute().body();
                if(responseFromServer != null) {
                    typeUpdate = responseFromServer.getResultCode();
                    return true;
                }
            } catch (IOException e) {
                return false;
            }*/
            return false;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if(success) {
                switch (typeUpdate) {
                    case CURRENT: { // версия актуальна
                        tvInfoUpdate.setText(getString(R.string.current_version));
                        btnUpdate.setClickable(false);
                        ObjectAnimator tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate,"alpha",0f,1f);
                        tvInfo.setDuration(800);
                        tvInfo.start();
                        break;
                    }
                    case SOFT_UPDATE:
                    case FORCE_UPDATE: { // версия не актуальна
                        tvInfoUpdate.setText(R.string.older_version);
                        tvInfoUpdate.setTextColor(getResources().getColor(R.color.warning));
                        btnUpdate.setAlpha(1f);
                        btnUpdate.setClickable(true);
                        ObjectAnimator tvInfo = ObjectAnimator.ofFloat(tvInfoUpdate,"alpha",0f,1f);
                        tvInfo.setDuration(800);
                        tvInfo.start();
                        break;
                    }
                }
            } else Toast.makeText(InfoActivity.this,getString(R.string.download_info_error),Toast.LENGTH_LONG).show();
        }
    }
}
