package com.example.evan.scout;

import java.util.ArrayList;
import java.util.List;

public class LeaderBoard {
    public LeaderBoard() {
        rankedScouts = new ArrayList<>();
    }
    List<Scout> rankedScouts;
    class Scout {
        String name;
        Float score;
        Integer numOfMatches;
    }
}
