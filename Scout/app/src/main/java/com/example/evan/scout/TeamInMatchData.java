package com.example.evan.scout;

import java.util.List;

public class TeamInMatchData {
    public Boolean didReachAuto;
    public Boolean didChallengeTele;
    public Boolean didScaleTele;
    public Boolean didGetDisabled;
    public Boolean didGetIncapacitated;

    public Integer matchNumber;
    public Integer teamNumber;
    public String scoutName;
    public String alliance;

    public List<Integer> ballsIntakedAuto;

    public Integer numBallsKnockedOffMidlineAuto;
    public Integer numHighShotsMadeAuto;
    public Integer numHighShotsMissedAuto;
    public Integer numLowShotsMadeAuto;
    public Integer numLowShotsMissedAuto;

    public Integer numGroundIntakesTele;
    public Integer numShotsBlockedTele;
    public Integer numHighShotsMadeTele;
    public Integer numHighShotsMissedTele;
    public Integer numLowShotsMadeTele;
    public Integer numLowShotsMissedTele;

    public List<List<Float>> successfulDefenseCrossTimesAuto;
    public List<List<Float>> failedDefenseCrossTimesAuto;
    public List<List<Float>> successfulDefenseCrossTimesTele;
    public List<List<Float>> failedDefenseCrossTimesTele;
}
