package martian.riddles.view;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.Random;

import martian.riddles.R;
import martian.riddles.controllers.GetContextClass;
import martian.riddles.controllers.RequestController;
import martian.riddles.controllers.StatisticsController;
import martian.riddles.controllers.StoredData;
import martian.riddles.model.DataOfUser;
import martian.riddles.model.Player;
import martian.riddles.model.ResponseFromServer;

public class LogupActivity extends AppCompatActivity {

    private TextView tvInfo;
    private ImageView imgAnimation;
    private EditText etLogin;
    private Button btnLogup;
    private TextView tvError;

    private ObjectAnimator errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f, 0.0f);
    private ObjectAnimator errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f, 1.0f);

    private final int MANY_LOGUP_IP = 4;
    private final int LOGIN_IS_TAKEN = 2;
    private final int LOGIN_NOT_EXIST = 1;
    private final int BAD_WORDS = 10;
    private final int WRONG_SYMBOLS = 3;
    private final int SHORT_LOGIN = -2;
    private final int LONG_LOGIN = 5;
    private final int MANY_SPACES = 6;
    private final int LOGIN_IS_ACCESS = 7;
    private final int SERVER_DOES_NOT_WORKING = 8;
    private final int UNKNOWN_ERROR = 0;
    private final int NO_INTERNET = 11;
    private final int SERVER_ERROR = 12;

    StatisticsController statisticsController;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logup_activity);

        tvInfo = findViewById(R.id.tvTopWord);
        imgAnimation = findViewById(R.id.imgLogupAnimation);
        etLogin = findViewById(R.id.etLogin);
        btnLogup = findViewById(R.id.btnLogup);
        tvError = findViewById(R.id.tvInvalidName);

        btnLogup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!etLogin.getText().toString().equals("")) {
                    LogupTask logupTask = new LogupTask();
                    logupTask.execute(etLogin.getText().toString());
                }
            }
        });
        etLogin.setOnFocusChangeListener(onFocusChangeListener);
        statisticsController = new StatisticsController(this);
    }

    private void animateError(int errorString, int duration) {
        if (errorAnimatorHide.isStarted()) {
            errorAnimatorShow.cancel();
            errorAnimatorHide.cancel();

            tvError.setText(errorString);
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f, 0.0f);
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f, 1.0f);
            errorAnimatorHide.setDuration(800);
            errorAnimatorHide.setStartDelay(duration);
            errorAnimatorShow.setDuration(800);
            errorAnimatorShow.start();
            errorAnimatorHide.start();
            return;
        } else {
            tvError.setText(errorString);
            errorAnimatorHide = ObjectAnimator.ofFloat(tvError, "alpha", 1.0f, 0.0f);
            errorAnimatorShow = ObjectAnimator.ofFloat(tvError, "alpha", 0.0f, 1.0f);
            errorAnimatorHide.setDuration(800);
            errorAnimatorHide.setStartDelay(duration);
            errorAnimatorShow.setDuration(800);
            errorAnimatorShow.start();
            errorAnimatorHide.start();
            return;
        }
    }

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {

        private boolean wasShow = false;

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                if (!wasShow) { // если warning еще не был показан
                    animateError(R.string.logup_warning, 7000);
                }
                TransitionDrawable transitionDrawable = (TransitionDrawable) imgAnimation.getDrawable();
                transitionDrawable.startTransition(1200);
            }
        }
    };

    private class LogupTask extends AsyncTask<String, Void, ResponseFromServer> {

        private String resultLogin;
        private int validateLoginStatus;
        private String usersToken;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnLogup.setClickable(false);
        }

        @Override
        protected ResponseFromServer doInBackground(String... voids) {
            String login = voids[0].trim();
            validateLoginStatus = isValidLogin(login);

            if (validateLoginStatus != LOGIN_IS_ACCESS) return null;
            else {
                resultLogin = login;
                DataOfUser dataOfUser = new DataOfUser();
                dataOfUser.setNickname(login.concat(StoredData.DATA_WINS));
                usersToken = generateRandomHexString(42);
                dataOfUser.setToken(usersToken);
                if (Build.VERSION.SDK_INT <= 28) {
                    dataOfUser.setUniqueString(Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID));
                }
                try {
                    if(!RequestController.hasConnection(LogupActivity.this)) { // проверка наличия интернета
                        ResponseFromServer response = new ResponseFromServer();
                        response.setResultCode(NO_INTERNET);
                        return response;
                    }
                    ResponseFromServer response = RequestController.Companion // получем приз
                            .getInstance()
                            .getApiService(GetContextClass.getContext())
                            .logup(dataOfUser)
                            .execute().body();
                    if (response == null) {
                        response = new ResponseFromServer();
                        response.setResultCode(SERVER_ERROR);
                    }
                    return response;
                } catch (IOException e) {
                    ResponseFromServer response = new ResponseFromServer();
                    response.setResultCode(UNKNOWN_ERROR);
                    return response;
                }
            }
        }

        public String generateRandomHexString(int length){
            Random r = new Random();
            StringBuffer sb = new StringBuffer();
            while(sb.length() < length){
                sb.append(Integer.toHexString(r.nextInt()));
            }
            return sb.toString().substring(0, length);
        }

        @Override
        protected void onPostExecute(ResponseFromServer res) {
            super.onPostExecute(res);

            if (res == null) { // если проблема с валидацией логина
                if (validateLoginStatus == WRONG_SYMBOLS) {
                    animateError(R.string.wrong_symbols, 5000);
                } else if (validateLoginStatus == BAD_WORDS) {
                    animateError(R.string.bad_words, 6000);
                } else if (validateLoginStatus == SHORT_LOGIN) {
                    animateError(R.string.invalid_long_name, 5000);
                } else if (validateLoginStatus == LONG_LOGIN) {
                    animateError(R.string.invalid_long_name, 5000);
                } else if (validateLoginStatus == MANY_SPACES) {
                    animateError(R.string.many_scapes, 5000);
                }
            } else {
                if (res.getResultCode() == LOGIN_IS_TAKEN) {
                    animateError(R.string.login_exist, 6000);
                } else if (res.getResultCode() == LOGIN_NOT_EXIST) {
                    Player.getInstance().setToken(usersToken);
                    Player.getInstance().setName(resultLogin);
                    statisticsController.signUp();
                    startActivity(new Intent(LogupActivity.this, MainActivity.class));
                    finish();
                } else if (res.getResultCode() == MANY_LOGUP_IP) {
                    animateError(R.string.many_ip_logup, 4000);
                } else if (res.getResultCode() == SERVER_DOES_NOT_WORKING) {
                    animateError(R.string.server_error_logup, 4000);
                } else if(res.getResultCode() == NO_INTERNET) {
                    animateError(R.string.no_internet,4000);
                } else if(res.getResultCode() == SERVER_ERROR) {
                    animateError(R.string.server_error_logup,4000);
                } else {
                    animateError(R.string.unknown_error, 4000);
                }
            }
            btnLogup.setClickable(true);
        }

        private int isValidLogin(String login) { // проверка логина на валидность
            if (login.length() < 4) return SHORT_LOGIN;
            if (login.length() > 15) return LONG_LOGIN;
            if (login.toLowerCase().contains("хуй") ||
                    login.toLowerCase().contains("пизда") ||
                    login.toLowerCase().contains("fuck") ||
                    login.toLowerCase().contains("член") ||
                    login.toLowerCase().contains("пидор") ||
                    login.toLowerCase().contains("пидр") ||
                    login.toLowerCase().contains("pidor") ||
                    login.toLowerCase().equals("соси") ||
                    login.toLowerCase().equals("sosi") ||
                    login.toLowerCase().contains("pizda") ||
                    login.toLowerCase().contains("hui") ||
                    login.toLowerCase().contains("pizdec") ||
                    login.toLowerCase().contains("pidr")) {
                return BAD_WORDS;
            }
            if (login.indexOf(' ') != login.lastIndexOf(' '))
                return MANY_SPACES; // если больше одного пробела
            if (login.matches("[A-Za-z0-9а-яА-Я\\s]+")) return LOGIN_IS_ACCESS;
            else return WRONG_SYMBOLS;
        }
    }
}
