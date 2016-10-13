package com.example.evan.scout;

import java.util.ArrayList;
import java.util.List;

public class LocalTeamInMatchData extends TeamInMatchData {
    public List<List<Utils.TwoValueStruct<Float, Boolean>>> defenseTimesAuto;
    public List<List<Utils.TwoValueStruct<Float, Boolean>>> defenseTimesTele;
    public List<Boolean> isBallIntaked;





    public LocalTeamInMatchData() {
        defenseTimesAuto = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            defenseTimesAuto.add(i, new ArrayList<Utils.TwoValueStruct<Float, Boolean>>());
        }
        defenseTimesTele = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            defenseTimesTele.add(i, new ArrayList<Utils.TwoValueStruct<Float, Boolean>>());
        }
        isBallIntaked = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            isBallIntaked.add(false);
        }
    }



    public TeamInMatchData getFirebaseData() {
        successfulDefenseCrossTimesAuto = localDefenseToFireBaseDefense(defenseTimesAuto, true);
        failedDefenseCrossTimesAuto = localDefenseToFireBaseDefense(defenseTimesAuto, false);
        successfulDefenseCrossTimesTele = localDefenseToFireBaseDefense(defenseTimesTele, true);
        failedDefenseCrossTimesTele = localDefenseToFireBaseDefense(defenseTimesTele, false);
        ballsIntakedAuto = new ArrayList<>();
        for (int i = 0; i < isBallIntaked.size(); i++) {
            if (isBallIntaked.get(i)) {
                ballsIntakedAuto.add(i);
            }
        }
        defenseTimesAuto = null;
        defenseTimesTele = null;
        isBallIntaked = null;
        return this;
    }

    private List<List<Float>> localDefenseToFireBaseDefense(List<List<Utils.TwoValueStruct<Float, Boolean>>> defenseTimes, boolean isSuccess) {
        try {
            List<List<Float>> splitData = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                splitData.add(i, new ArrayList<Float>());
            }
            for (int i = 0; i < defenseTimes.size(); i++) {
                for (int j = 0; j < defenseTimes.get(i).size(); j++) {
                    Utils.TwoValueStruct<Float, Boolean> firstEntry = defenseTimes.get(i).get(j);
                    if (firstEntry.value2 == isSuccess) {
                        splitData.get(i).add(firstEntry.value1);
                    }
                }
            }
            return splitData;
        } catch (NullPointerException npe) {
            return null;
        }
    }
}
