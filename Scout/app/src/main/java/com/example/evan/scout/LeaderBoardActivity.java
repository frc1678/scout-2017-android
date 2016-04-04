package com.example.evan.scout;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LeaderBoardActivity extends AppCompatActivity {
    LeaderBoardAdapter adapter;
    LeaderBoard leaderBoard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        leaderBoard = LeaderBoardUpdateLoop.getCurrentLeaderBoard();
        if (leaderBoard == null) {
            leaderBoard = new LeaderBoard();
            Toast.makeText(this, "No Leader Board Published", Toast.LENGTH_LONG).show();
        }
        adapter = new LeaderBoardAdapter(this, leaderBoard);
        ((ListView)findViewById(R.id.leaderBoard)).setAdapter(adapter);
    }
    private class LeaderBoardAdapter extends ArrayAdapter<LeaderBoard.Scout> {
        Activity context;
        public LeaderBoardAdapter(Activity context, LeaderBoard leaderBoard) {
            super(context, R.layout.leader_board_row, leaderBoard.rankedScouts);
            this.context = context;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.leader_board_row, parent);
            }
            LeaderBoard.Scout item = getItem(position);
            TextView scoutNameText = (TextView)convertView.findViewById(R.id.leaderBoardScoutName);
            scoutNameText.setText(item.name);
            TextView scoutMatchesText = (TextView)convertView.findViewById(R.id.leaderBoardMatchesScouted);
            scoutMatchesText.setText(item.numOfMatches);
            TextView scoutScoreText = (TextView)convertView.findViewById(R.id.leaderBoardScoutScore);
            scoutScoreText.setText(item.score.toString());
            return convertView;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.leader_board_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.leaderBoardRefreshButton) {
            leaderBoard = LeaderBoardUpdateLoop.getCurrentLeaderBoard();
            adapter.notifyDataSetChanged();
        }
        return true;
    }
}
