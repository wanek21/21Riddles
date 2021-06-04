package martian.riddles.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import martian.riddles.R;
import martian.riddles.controllers.GetContextClass;
import martian.riddles.controllers.Progress;
import martian.riddles.controllers.RequestController;
import martian.riddles.controllers.StoredData;
import martian.riddles.dto.GetEmail;
import martian.riddles.dto.GetPlace;
import martian.riddles.dto.Player;
import okhttp3.ResponseBody;

public class DoneActivity extends AppCompatActivity { // активити появляется после прохождения всех уровней

    private TextView tvPlace;
    private TextView tvFinalPhrase;
    private TextView tvEmail;
    private Button btnSendReview;
    private Group emailGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.done_activity);

        int place = StoredData.getDataInt(StoredData.DATA_PLACE,0);
        tvPlace = findViewById(R.id.tvPlace);
        btnSendReview = findViewById(R.id.btnSendReview);
        tvFinalPhrase = findViewById(R.id.tvCongrat);
        tvEmail = findViewById(R.id.tvEmailFinish);
        emailGroup = findViewById(R.id.emailGroup);
        tvFinalPhrase.setText(getPhrase(place));

        if(place != 0) {
            tvPlace.setText(String.valueOf(place));
            if(place == 1) showInfoForWinner();
        } else new AsyncLoadPlace().execute();

        btnSendReview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.animate().scaleXBy(1).scaleX(0.9f).scaleYBy(1).scaleY(0.9f).setDuration(30).start();
                        v.animate().alphaBy(1.0f).alpha(0.9f).setDuration(80).start();
                        break;
                    }
                    case MotionEvent.ACTION_UP: {
                        v.animate().scaleXBy(0.9f).scaleX(1).scaleYBy(0.9f).scaleY(1).setDuration(80).start();
                        v.animate().alphaBy(0.9f).alpha(1.0f).setDuration(80).start();
                        String appPackageName = getPackageName();
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (android.content.ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;
                    }
                }
                return true;
            }
        });
        tvEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("", tvEmail.getText().toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(DoneActivity.this, R.string.email_copy, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        int pastLevel = getIntent().getIntExtra("past_level",22);
        Intent intentMain = new Intent();
        intentMain.putExtra("differ_level", Progress.getInstance().getLevel() - pastLevel);
        try {
            setResult(Activity.RESULT_OK, intentMain);
            finish();
        } catch (NullPointerException ex) {
        }
        finish();
    }

    private String getPhrase(int place) {
        switch (place) {
            case 2: return getResources().getString(R.string.place2_congratulations);
            case 3: return getResources().getString(R.string.place3_congratulations);
            case 4: return getResources().getString(R.string.place4_congratulations);
            case 5: return getResources().getString(R.string.place5_congratulations);
            case 6: return getResources().getString(R.string.place6_congratulations);
            case 7: return getResources().getString(R.string.place7_congratulations);
            case 8: return getResources().getString(R.string.place8_congratulations);
            case 9: return getResources().getString(R.string.place9_congratulations);
            case 10: return getResources().getString(R.string.place10_congratulations);
            case 11: return getResources().getString(R.string.place11_congratulations);
            case 12: return getResources().getString(R.string.place12_congratulations);
            case 13: return getResources().getString(R.string.place13_congratulations);
            case 14: return getResources().getString(R.string.place14_congratulations);
            default: return getResources().getString(R.string.place_congratulations_default);
        }
    }

    private void showInfoForWinner() {
        emailGroup.setVisibility(View.VISIBLE);
        new GetEmailTask().execute();
    }

    private class AsyncLoadPlace extends AsyncTask<Void,Void,Integer> {

        private final int ERROR = -1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            GetPlace getPlace = new GetPlace();
            getPlace.setNickname(Player.getInstance().getName());
            getPlace.setToken(Player.getInstance().getToken());
            try {
                ResponseBody responseBody = RequestController.Companion // получем приз
                        .getInstance()
                        .getApiService(GetContextClass.getContext())
                        .getPlace(getPlace)
                        .execute().body();
                if (responseBody == null) return ERROR;
                else return Integer.parseInt(responseBody.string());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer place) {
            super.onPostExecute(place);
            if(place > 0) {
                StoredData.saveData(StoredData.DATA_PLACE,place);
                tvFinalPhrase.setText(getPhrase(place));
                tvPlace.setText(place.toString());
                if(place == 1) showInfoForWinner();
            } else tvPlace.setText("Error");
        }
    }
    private class GetEmailTask extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            while(true) {
                try {
                    GetEmail getEmail = new GetEmail();
                    getEmail.setNickname(Player.getInstance().getName());
                    getEmail.setToken(Player.getInstance().getToken());
                    getEmail.setLocale(Locale.getDefault().getLanguage());
                    ResponseBody response = RequestController
                            .Companion // получем приз
                            .getInstance()
                            .getApiService(GetContextClass.getContext())
                            .getEmail(getEmail)
                            .execute().body();
                    if(response != null) return response.string();
                    else return null;

                } catch (IOException e) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    continue;
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if(s != null) {
                tvEmail.setText(s);
            } else tvEmail.setText("Error");
        }
    }
}
