package com.example.evan.scout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeleopActivity extends DataActivity {
    public List<String> getDefenseData() {
        return formatList(Arrays.asList("defenseTimesTele._NUMBER/5"));
    }
    public List<String> getShotData() {
        return formatList(Arrays.asList("numHighShotsMadeTele", "numHighShotsMissedTele",
                "numLowShotsMadeTele", "numLowShotsMissedTele"));
    }
    public List<String> getToggleData() {
        return formatList(Arrays.asList("didChallengeTele", "didScaleTele",
                "didGetDisabled", "didGetIncapacitated"));
    }
    public List<String> getCounterData() {
        return formatList(Arrays.asList("numGroundIntakesTele", "numShotsBlockedTele"));
    }
    @Override
    public Boolean shouldSpaceToggles() {return true;}
    @Override
    public Boolean doTogglesDepend() {return true;}
    public Class getNextActivityClass() {return MainActivity.class;}
    public Class getPreviousActivityClass() {return AutoActivity.class;}
    public int getActionBarMenu() {return R.menu.teleop_menu;}
    private List<String> formatList(List<String> startList) {
        List<String> returnList = new ArrayList<>();
        for (String dataPoint : startList) {
            if (dataPoint.contains("_NUMBER")) {
                Integer limit = Integer.parseInt(dataPoint.split("/")[1].substring(0, 1));
                String modKey = dataPoint.substring(0, dataPoint.indexOf("/")) + dataPoint.substring(dataPoint.indexOf("/") + 2);
                for (int i = 0; i < limit; i++) {
                    returnList.add(modKey.replaceAll("_NUMBER", Integer.toString(i)));
                }
            } else {
                returnList.add(dataPoint);
            }
        }
        return returnList;
    }
}
