package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class DataActivity extends AppCompatActivity {
    public abstract List<String> getToggleData();
    public abstract List<String> getCounterData();
    public abstract List<String> getShotData();
    public abstract List<String> getDefenseData();
    public abstract Class getNextActivityClass();
    public abstract Class getPreviousActivityClass();
    public abstract int getActionBarMenu();
    public String getLongToggleData() {return null;}
    public Boolean shouldSpaceToggles() {return false;}
    public Boolean doTogglesDepend() {return false;}

    public final Activity context = this;

    private Intent intent;
    private LocalTeamInMatchData collectedData;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator counterCreator;
    private UIComponentCreator.UIShotCreator shotCreator;
    private UIComponentCreator.UIButtonCreator defenseCreator;
    private ToggleButton longToggleButton;
    private Boolean readyForNextActivity = false;
    private final Object readyForNextActivityLock = new Object();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        intent = getIntent();
        setTitle("Scout Team " + intent.getIntExtra("teamNumber", -1));
        Drawable actionBarBackgroundColor;
        if (intent.getIntExtra("scoutNumber", -1) < 4) {
            actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_RED));
        } else {
            actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_BLUE));
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(actionBarBackgroundColor);
        }
        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (readyForNextActivityLock) {
                    readyForNextActivity = true;
                }
            }
        }, 1, TimeUnit.SECONDS);
        parseJson(intent.getStringExtra("previousData"));
        updateUI();
    }



    private void parseJson(String json) {
        try {
            if (json == null) {throw new NullPointerException();}
            collectedData = (LocalTeamInMatchData)Utils.deserializeClass(json, LocalTeamInMatchData.class);
        } catch (JsonParseException|NullPointerException e) {
            collectedData = new LocalTeamInMatchData();
        }
        Log.i("Data in data activity", Utils.serializeClass(collectedData));
    }



    private void updateUI() {
        LinearLayout toggleLayout = (LinearLayout)findViewById(R.id.dataActivityToggleLayout);
        List<String> toggleDisplayTitles = new ArrayList<>();
        for (int i = 0; i < getToggleData().size(); i++) {
            toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
        }
        toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
        for (int i = 0; i < getToggleData().size(); i+=2) {
            Boolean value1;
            Boolean value2;
            try {
                value1 = (Boolean) Utils.getField(collectedData, getToggleData().get(i));
                if (value1 == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value1 = false;
            }
            try {
                value2 = (Boolean) Utils.getField(collectedData, getToggleData().get(i+1));
                if (value2 == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value2 = false;
            }
            final ToggleButton button1 = toggleCreator.getNextToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, value1);
            toggleLayout.addView(button1);
            if (shouldSpaceToggles()) {
                toggleLayout.addView(getFillerSpace(0.5f));
            }
            final ToggleButton button2 = toggleCreator.getNextToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, value2);
            toggleLayout.addView(button2);
            if (shouldSpaceToggles() && i+1 != getToggleData().size()-1) {
                toggleLayout.addView(getFillerSpace(0.5f));
            }
            if (doTogglesDepend()) {
                button1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {button2.setChecked(false);}
                    }
                });
                button2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {button1.setChecked(false);}
                    }
                });
            }
        }



        LinearLayout counterLayout = (LinearLayout)findViewById(R.id.dataActivityCounterLayout);
        counterLayout.addView(getFillerSpace(1f));
        List<String> counterNames = new ArrayList<>();
        for (int i = 0; i < getCounterData().size(); i++) {
            counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
        }
        counterCreator = new UIComponentCreator(this, counterNames);
        for (int i = 0; i < getCounterData().size(); i++) {
            Integer value;
            try {
                value = (Integer)Utils.getField(collectedData, getCounterData().get(i));
                if (value == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value = 0;
            }
            LinearLayout enclosingLayout = new LinearLayout(this);
            enclosingLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            enclosingLayout.setOrientation(LinearLayout.VERTICAL);
            enclosingLayout.addView(counterCreator.getNextTitleRow(1));
            enclosingLayout.addView(counterCreator.getNextCounterRow(1, value));
            counterLayout.addView(enclosingLayout);
        }
        counterLayout.addView(getFillerSpace(1f));



        List<String> shotNames = new ArrayList<>();
        for (int i = 0; i < getShotData().size(); i+=2) {
            shotNames.add(Constants.KEYS_TO_TITLES.get(getShotData().get(i)));
        }
        shotCreator = new UIComponentCreator.UIShotCreator(this, shotNames);
        for (int i = 0; i < getShotData().size(); i+=2) {
            Integer value1;
            Integer value2;
            try {
                value1 = (Integer)Utils.getField(collectedData, getShotData().get(i));
                if (value1 == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value1 = 0;
            }
            try {
                value2 = (Integer)Utils.getField(collectedData, getShotData().get(i+1));
                if (value2 == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value2 = 0;
            }
            shotCreator.addButtonRow(counterLayout, value1,
                    value2, i/2);
            counterLayout.addView(getFillerSpace(1f));
        }



        LinearLayout defenseLayout = (LinearLayout)findViewById(R.id.dataActivityDefenseLayout);
        List<String> defenseNames = new ArrayList<>();
        for (int i = 0; i < getDefenseData().size(); i++) {
            defenseNames.add(Constants.KEYS_TO_TITLES.get(getDefenseData().get(i)));
        }
        defenseCreator = new UIComponentCreator.UIButtonCreator(this, defenseNames);
        for (int i = 0; i < getDefenseData().size(); i++) {
            try {
                defenseCreator.addButtonRow(defenseLayout,
                        (List<Utils.TwoValueStruct<Float, Boolean>>)Utils.getField(collectedData, getDefenseData().get(i)), i);
            } catch (Exception e) {
                //data not passed from previous activity.  Use default empty struct
            }
        }



        if (getLongToggleData() != null) {
            RelativeLayout longToggleLayout = (RelativeLayout)findViewById(R.id.dataActivityLongToggleLayout);
            longToggleButton = new ToggleButton(this);
            longToggleButton.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            longToggleButton.setTextOn(Constants.KEYS_TO_TITLES.get(getLongToggleData()));
            longToggleButton.setTextOff(Constants.KEYS_TO_TITLES.get(getLongToggleData()));
            Boolean value;
            try {
                value = (Boolean)Utils.getField(collectedData, getLongToggleData());
                if (value == null) {throw new NullPointerException();}
            } catch (Exception e) {
                value = false;
            }
            longToggleButton.setChecked(value);
            longToggleLayout.addView(longToggleButton);
        }
    }



    private LinearLayout getFillerSpace(Float weight) {
        LinearLayout fillerSpace = new LinearLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return fillerSpace;
    }



    private void updateData() {
        collectedData.scoutName = getIntent().getStringExtra("scoutName");

        List<View> toggleList = toggleCreator.getComponentViews();
        for (int i = 0; i < toggleList.size(); i++) {
            ToggleButton toggleButton = (ToggleButton) toggleList.get(i);
            try {
                Utils.setField(collectedData, getToggleData().get(i), toggleButton.isChecked());
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add toggle " + Integer.toString(i) + " to Data");
                Toast.makeText(this, "Invalid data in counter" + Integer.toString(i), Toast.LENGTH_LONG).show();
                return;
            }
        }


        List<View> currentTextViews = counterCreator.getComponentViews();
        for (int i = 0; i < currentTextViews.size(); i++) {
            try {
                Utils.setField(collectedData, getCounterData().get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add counter" + Integer.toString(i) + " num to Data");
                Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                return;
            }
        }


        for (int i = 0; i < getShotData().size(); i+=2) {
            try {
                Utils.setField(collectedData, getShotData().get(i), shotCreator.getShotsMade().get(i / 2));
                Utils.setField(collectedData, getShotData().get(i + 1), shotCreator.getShotsMissed().get(i / 2));
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add shot " + Integer.toString(i) + " num to Data");
                Toast.makeText(this, "Error in Shot Data", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (getLongToggleData() != null) {
            try {
                Utils.setField(collectedData, getLongToggleData(), longToggleButton.isChecked());
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add long toggle to Data");
                Toast.makeText(this, "Error in long toggle", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (this.intent.getIntExtra("scoutNumber", -1) < 4) {
            collectedData.alliance = "red";
        } else {
            collectedData.alliance = "blue";
        }

        collectedData.matchNumber = this.intent.getIntExtra("matchNumber", -1);
        collectedData.teamNumber = this.intent.getIntExtra("teamNumber", -1);
    }



    private Intent prepareIntent(Class clazz) {
        updateData();
        Intent intent = new Intent(this, clazz);
        intent.putExtras(this.intent);
        try {
            intent.putExtra("previousData", Utils.serializeClass(collectedData));
        } catch (JsonParseException jpe) {
            intent.putExtra("previousData", (String)null);
        }
        intent.putExtra("matchName", "Q" + Integer.toString(this.intent.getIntExtra("matchNumber", -1))
                + "_" + Integer.toString(this.intent.getIntExtra("teamNumber", -1)));
        return intent;
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(getActionBarMenu(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonNext) {
            synchronized (readyForNextActivityLock) {
                if (!readyForNextActivity) {
                    Log.i("Scout Error", "Tried to move on too quickly!");
                    return true;
                }
            }
            Long startTime = Calendar.getInstance().getTimeInMillis();
            Intent intent = prepareIntent(getNextActivityClass());
            Long stopTime = Calendar.getInstance().getTimeInMillis();
            Log.i("Starting next Activity!", "Time to update and serialize data: " + Long.toString(stopTime - startTime) + "ms");
            startActivity(intent);
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Stop Scouting")
                .setMessage("Are you sure you want to go back now?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = prepareIntent(getPreviousActivityClass());
                        if (getPreviousActivityClass() == MainActivity.class) {
                            intent.putExtra("previousData", (String) null);
                        }
                        startActivity(intent);
                    }
                })
                .show();
    }
}
