package martian.riddles.dto;

import martian.riddles.domain.Progress;
import martian.riddles.data.local.StoredData;

public class Player {

    private String name;
    private int level;
    public static final String DATA_NAME_PLAYER = "name_player";
    public static final String DATA_TOKEN = "users_token";

    private static final Player instancePlayer = new Player();

    public static Player getInstance() {
        return instancePlayer;
    }

    private Player() {
        name = StoredData.getDataString(DATA_NAME_PLAYER,"");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        StoredData.saveData(DATA_NAME_PLAYER,name);
        this.name = name;
    }
    public void setToken(String token) {
        StoredData.saveData(DATA_TOKEN,token);
    }
    public String getToken() {
        return StoredData.getDataString(DATA_TOKEN,"");
    }

    public int getLevel() {
        return Progress.getInstance().getLevel();
    }

}
