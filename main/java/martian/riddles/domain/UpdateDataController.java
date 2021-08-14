package martian.riddles.domain;

import martian.riddles.data.local.StoredData;
import martian.riddles.data.remote.RequestController;
import martian.riddles.util.GetContextClass;

import static martian.riddles.domain.RiddlesController.DATA_CURRENT_RIDDLE;
import static martian.riddles.domain.RiddlesController.DATA_NEXT_RIDDLE;
import static martian.riddles.domain.RiddlesController.EMPTY_RIDDLE;
import static martian.riddles.domain.RiddlesController.ERROR_LOAD_RIDDLE;

//TODO("remove")
public class UpdateDataController {
    private static final UpdateDataController ourInstance = new UpdateDataController();

    private boolean isConnection = false;

    public static UpdateDataController getInstance() {
        return ourInstance;
    }

    private UpdateDataController() {
        isConnection = checkConnection();
    }

    private boolean checkConnection() {
        boolean isConnection = false;
        isConnection = RequestController
                .hasConnection(GetContextClass.getContext()); // check connection
        if(!isConnection) isConnection = RequestController
                .hasConnection(GetContextClass.getContext()); // if there is no connection, try again
        return isConnection;
    }
    public boolean nextRiddleIsLoaded() {
        String riddle = StoredData.getDataString(DATA_NEXT_RIDDLE,ERROR_LOAD_RIDDLE);
        if(riddle.equals(ERROR_LOAD_RIDDLE) || riddle.equals(EMPTY_RIDDLE)) {
            return false;
        } else return true;
    }
    public boolean riddleIsLoaded() {
        String riddle = StoredData.getDataString(DATA_CURRENT_RIDDLE,ERROR_LOAD_RIDDLE);
        if(riddle.equals(ERROR_LOAD_RIDDLE) || riddle.equals(EMPTY_RIDDLE)) {
            return false;
        } else return true;
    }
}
