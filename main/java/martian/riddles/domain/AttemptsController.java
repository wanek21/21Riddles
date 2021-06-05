package martian.riddles.domain;

import martian.riddles.data.local.StoredData;

import static martian.riddles.data.local.StoredData.DATA_COUNT_ATTEMPTS;

public class AttemptsController {

    private String DATA_WRONG_ANSWERS = "count_wrong_answers";
    private int countWrongAnswers;
    private StatisticsController statisticsController;
    private boolean endlessAttempts = false;

    public AttemptsController(StatisticsController statisticsController) {
        this.statisticsController = statisticsController;
        countWrongAnswers = StoredData.getDataInt(DATA_WRONG_ANSWERS,0);
    }
    public AttemptsController() {
        countWrongAnswers = StoredData.getDataInt(DATA_WRONG_ANSWERS,0);
    }

    public boolean isEndlessAttempts() {
        return endlessAttempts;
    }

    public void setEndlessAttempts(boolean endlessAttempts) {
        this.endlessAttempts = endlessAttempts;
    }

    public int getCountAttempts() {
        return StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
    }
    public void resetCountAttempts() {
        StoredData.saveData(DATA_COUNT_ATTEMPTS, 3);
    }

    public void decrementCountAtempts() { // уменьшает кол-во попыток на 1 и сохраняет в памяти
        int countAttempts = StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
        if (countAttempts > 0) StoredData.saveData(DATA_COUNT_ATTEMPTS, countAttempts - 1);
    }

    public void incrementCountAtempts() { // увеличивает кол-во попыток на 1 и сохраняет в памяти
        int countAttempts = StoredData.getDataInt(DATA_COUNT_ATTEMPTS, 3);
        if (countAttempts < 9) StoredData.saveData(DATA_COUNT_ATTEMPTS, countAttempts + 1);
        statisticsController.earnAttempt(1);
    }


    public int getCountWrongAnswers() { return countWrongAnswers; }

    public void increaseCountWrongAnswers() {
        StoredData.saveData(DATA_WRONG_ANSWERS,++countWrongAnswers);
        statisticsController.sendAttempt(isEndlessAttempts());
    }

    public void resetCountWrongAnswers() {
        StoredData.saveData(DATA_WRONG_ANSWERS,0);
        countWrongAnswers = 0;
    }
}