package martian.riddles.controllers;


import java.io.IOException;
import java.util.Locale;

import martian.riddles.R;
import martian.riddles.dto.CheckAnswer;
import martian.riddles.dto.GetRiddle;
import martian.riddles.dto.Player;
import martian.riddles.exceptions.ErrorOnServerException;
import martian.riddles.exceptions.NoInternetException;
import martian.riddles.dto.Riddle;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiddlesController {

    public static final String DATA_CURRENT_RIDDLE = "current_riddle";
    public static final String DATA_NEXT_RIDDLE = "next_riddle";
    public static final String EMPTY_RIDDLE = "empty_riddle";
    public static final String ERROR_LOAD_RIDDLE = "error_riddle";

    private final int START_REMOTE_RIDDLES_LEVEL = 10; // с этого уровня загадки загружаются с сервера
    private String locale;

    public RiddlesController() {
        locale = Locale.getDefault().getLanguage();
    }

    public boolean checkAnswer(String answer) throws NoInternetException, ErrorOnServerException, IOException {
        answer = answer.trim().toLowerCase();

        int currentLevel = Player.getInstance().getLevel();
        RequestController.Companion.getInstance();
        if (RequestController.hasConnection(GetContextClass.getContext())) {
            boolean isRight;
            CheckAnswer checkAnswer = new CheckAnswer();
            checkAnswer.setNickname(Player.getInstance().getName());
            checkAnswer.setToken(Player.getInstance().getToken());
            checkAnswer.setAnswer(answer);

            ResponseBody response = RequestController.Companion
                    .getInstance()
                    .getApiService(GetContextClass.getContext())
                    .checkAnswer(checkAnswer)
                    .execute().body();

            if (response == null) throw new ErrorOnServerException();
            isRight = (response.string().equals("1"));
            if (isRight) {
                if (currentLevel < 21) {
                    // меняем местами текущую загадку и следующую
                    StoredData.saveData(DATA_CURRENT_RIDDLE, StoredData.getDataString(DATA_NEXT_RIDDLE, EMPTY_RIDDLE));
                    StoredData.saveData(DATA_NEXT_RIDDLE, EMPTY_RIDDLE);
                }
            }
            return isRight;
        } else throw new NoInternetException();
    }

    public String getRiddle() {

        int level = Player.getInstance().getLevel();
        String riddle = "Riddle";
        switch (level) {
            case 1:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst1);
                break;
            case 2:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst2);
                break;
            case 3:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst3);
                break;
            case 4:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst4);
                break;
            case 5:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst5);
                break;
            case 6:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst6);
                break;
            case 7:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst7);
                break;
            case 8:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst8);
                break;
            case 9:
                riddle = GetContextClass.getContext().getResources().getString(R.string.qst9);
                break;
            default: {
                riddle = StoredData.getDataString(DATA_CURRENT_RIDDLE, EMPTY_RIDDLE);
                if (riddle.equals(EMPTY_RIDDLE) || riddle.equals(ERROR_LOAD_RIDDLE)) {
                    riddle = GetContextClass.getContext().getString(R.string.load_riddle_error);
                    try {
                        loadRiddle();
                    } catch (NoInternetException ex) {
                    }
                }
                break;
            }
        }

        // подгружаем следующую загадку
        if (level >= (START_REMOTE_RIDDLES_LEVEL-1) && level != 21) {
            if (StoredData.getDataString(DATA_NEXT_RIDDLE, EMPTY_RIDDLE).equals(EMPTY_RIDDLE)) { // если следующая загадка не загружена
                try {
                    loadNextRiddle();
                } catch (NoInternetException ex) {

                }
            }
        }

        return riddle;
    }

    public void loadRiddle() throws NoInternetException {
        downloadRiddle(false);
    }

    public void loadNextRiddle() throws NoInternetException {
        // загружаем следующуй загадку заранее
        downloadRiddle(true);
    }

    private void downloadRiddle(final boolean nextRiddle) throws NoInternetException {
        GetRiddle getRiddle = new GetRiddle();
        getRiddle.setToken(Player.getInstance().getToken());
        getRiddle.setNickname(Player.getInstance().getName());
        getRiddle.setLocale(locale);
        getRiddle.setNext(nextRiddle);
        if (RequestController.hasConnection(GetContextClass.getContext())) {
            RequestController.Companion // получем приз
                    .getInstance()
                    .getApiService(GetContextClass.getContext())
                    .getRiddle(getRiddle)
                    .enqueue(new Callback<Riddle>() {
                        @Override
                        public void onResponse(Call<Riddle> call, Response<Riddle> response) {
                            String riddle = response.body().getRiddle();
                            if (nextRiddle) StoredData.saveData(DATA_NEXT_RIDDLE, riddle);
                            else StoredData.saveData(DATA_CURRENT_RIDDLE, riddle);
                        }

                        @Override
                        public void onFailure(Call<Riddle> call, Throwable t) {
                            if (nextRiddle)
                                StoredData.saveData(DATA_NEXT_RIDDLE, ERROR_LOAD_RIDDLE);
                            else StoredData.saveData(DATA_CURRENT_RIDDLE, ERROR_LOAD_RIDDLE);
                        }
                    });
        } else throw new NoInternetException();
    }

}
