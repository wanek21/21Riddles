package martian.riddles.domain;


import martian.riddles.data.local.StoredData;

import static martian.riddles.data.local.StoredData.DATA_COUNT_LAUNCH_APP;
import static martian.riddles.data.local.StoredData.DATA_LEVEL;

public class Progress { // класс синглтон для управления уровнем(прогрессом)
    private int level;
    private boolean isDone = false;
    public static final int DEFAULT_LEVEL = 1;

    private static final Progress instanceProgress = new Progress();

    public static Progress getInstance() {
        return instanceProgress;
    }

    private Progress() { this.level = getLevelFromStorage(); }
    private static int getLevelFromStorage() { // получение уровня игрока из какой-нибудь базы данных
        /*int countLaunches = StoredData.getDataInt(DATA_COUNT_LAUNCH_APP,0);
        int level = SecurityController.decodeLevel(StoredData.getDataInt(DATA_LEVEL,DEFAULT_LEVEL));
        if(level != -1) {
            if(countLaunches == 1 && level > 1) level = 1; // для защиты от взлома
        } else level = 1;
        return level;*/
        return 1;
    }
    public void levelUp() {
        level++;
        if(level == 22) isDone = true;
        if(level <= 22) {
            incrementSaveLevel();
        }
    }
    public int getLevel() {
        return level;
    }

    private void incrementSaveLevel() { // увеличивает записанный в памяти уровень на 1
        int currentLevel = SecurityController.decodeLevel(StoredData.getDataInt(DATA_LEVEL,DEFAULT_LEVEL));
        int incLevel = currentLevel+1;
        StoredData.saveData(DATA_LEVEL,SecurityController.encodeLevel(incLevel));
    }

    public boolean isDone() {
        return isDone;
    }
    public void done(boolean done) {
        isDone = done;
    }
}
