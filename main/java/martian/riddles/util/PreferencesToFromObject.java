package martian.riddles.util;

import java.util.ArrayList;
import java.util.List;

import martian.riddles.dto.Leaders;

// TODO("remove")
public class PreferencesToFromObject {

    public static List<Leaders> toLeadersList(String leadersString) {

        List<Leaders> result = new ArrayList<>(4);
        String[] oneLevelLeaders = leadersString.split(";");
        for (String oneLevelLeader : oneLevelLeaders) {
            if(oneLevelLeader.equals("0-0-...")) continue;

            Leaders leadersOnLevel = new Leaders();
            /*leadersOnLevel.setRiddle(Integer.parseInt(oneLevelLeader.split("-")[0]));
            leadersOnLevel.setNickname(oneLevelLeader.split("-")[2]);
            leadersOnLevel.setCountUsersOnThisRiddle(Integer.parseInt(oneLevelLeader.split("-")[1]));*/
            result.add(leadersOnLevel);
        }

        return result;
    }

    public static String toLeadersString(List<Leaders> leadersList) {
        String result = "";

        int size = leadersList.size();
        for(int i = 0; i < 4; i++) {

            if(i < size) {
                Leaders leaders = leadersList.get(i);
                result = result.concat(leaders.getRiddle()
                        +"-"
                        +leaders.getCountUsersOnThisRiddle()
                        +"-"
                        +leaders.getNickname()
                        +";");
            } else result = result.concat("0-0-...;");
        }

        return result;
    }
}
