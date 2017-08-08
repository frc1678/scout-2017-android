package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    public abstract String activityName();
    public abstract List<String> getToggleData();
    public abstract List<String> getCounterData();
    public abstract List<String> getShotData();
    public abstract Integer getToggleXML();
    public abstract Integer getCounterXML();
    public abstract Integer getShotXML();
    public abstract Integer getOtherXML();
    public abstract Class getNextActivityClass();
    public abstract Class getPreviousActivityClass();
    public abstract int getActionBarMenu();
    public Boolean shouldSpaceToggles() {return false;}
    public Boolean doTogglesDepend() {return false;}

    public final Activity context = this;

    private Intent intent;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator.UICounterCreator counterCreator;
    private UIComponentCreator.UIGearCreator gearCreator;
    private UIComponentCreator.UIButtonCreator liftOffCreator;
    private UIComponentCreator.UIShotCreator shotCreator;
    private Boolean readyForNextActivity = false;
    private final Object readyForNextActivityLock = new Object();
//    private LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(activityName() == "auto"){
            setContentView(R.layout.activity_auto);
        }else if(activityName() == "tele"){
            setContentView(R.layout.activity_teleop);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        intent = getIntent();
        databaseReference = FirebaseDatabase.getInstance().getReference();
//        setTitle("Scout Team " + intent.getIntExtra("teamNumber", -1));

            Drawable actionBarBackgroundColor;
            actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_GREEN));
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

        updateUI();
    }

    private void updateUI() {
        if(getToggleXML() != null){
            LinearLayout toggleLayout = (LinearLayout)findViewById(getToggleXML());
            List<String> toggleDisplayTitles = new ArrayList<>();
            if(getToggleData() != null) {
                Log.e("toggleSize", getToggleData().size()+"");
                for (int i = 0; i < getToggleData().size(); i++) {
                    toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
                }

                toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
                Log.e("toggleSize", toggleDisplayTitles.size()+"");
                final ToggleButton button1 = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false);
                final ToggleButton button2 = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false);

                DataManager.addZeroTierJsonData(button1.getText().toString(), false);
                DataManager.addZeroTierJsonData(button2.getText().toString(), false);

                liftOffCreator = new UIComponentCreator.UIButtonCreator(this, null);
                toggleLayout.addView(liftOffCreator.addButton(button1, button2));

                for (int i = 0; i < getToggleData().size(); i+=2) {
                    toggleLayout.addView(button1);
                    if (shouldSpaceToggles()) {
                        toggleLayout.addView(getFillerSpace(0.2f));
                    }
                    toggleLayout.addView(button2);
                    if (shouldSpaceToggles() && i+1 != getToggleData().size()-1) {
                        toggleLayout.addView(getFillerSpace(0.2f));
                    }
                    if (doTogglesDepend()) {
                        button1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    button2.setChecked(false);
                                }
                            }
                        });
                        button2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    button1.setChecked(false);
                                }
                            }
                        });
                    }
                }
            }
        }

                    LinearLayout shotLayout = (LinearLayout)findViewById(getShotXML());
                    List<String> shotNames = new ArrayList<>();
                    for (int i = 0; i < getShotData().size(); i++) {
                        shotNames.add(Constants.KEYS_TO_TITLES.get(getShotData().get(i)));
                    }
                    Log.e("shotNames",String.valueOf(shotNames.size()));
                    shotCreator = new UIComponentCreator.UIShotCreator(this, shotNames);
                    for (int i = 0; i < getShotData().size(); i+=2) {
                        Button button1 = null;
                        Button button2 = null;

                        if(activityName() == "auto"){
                            Log.e("first",i+"");
                            button1 = shotCreator.addButton("highShotTimesForBoilerAuto");
                            button2 = shotCreator.addButton("lowShotTimesForBoilerAuto");
                        }else if(activityName() == "tele"){
                            button1 = shotCreator.addButton("highShotTimesForBoilerTele");
                            button2 = shotCreator.addButton("lowShotTimesForBoilerTele");
                        }

                        shotLayout.addView(button1);
                        shotLayout.addView(getFillerSpace(0.3f));
                        shotLayout.addView(button2);
                    }

        LinearLayout gearLayout = (LinearLayout)findViewById(getOtherXML());
        gearCreator = new UIComponentCreator.UIGearCreator(this, null);
        gearLayout.addView(gearCreator.addButton());
        gearLayout.addView(getFillerSpace(1f));

                    LinearLayout counterLayout = (LinearLayout) findViewById(getCounterXML());
                    List<String> counterNames = new ArrayList<>();
                    for (int i = 0; i < getCounterData().size(); i++) {
                        counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
                    }
                    counterCreator = new UIComponentCreator.UICounterCreator(this, counterNames);
                    for (int i = 0; i < getCounterData().size(); i++) {
                        if(i == (getCounterData().size() - 1) && activityName() == "tele"){
                            gearLayout.addView(counterCreator.addCounter());
                        }else{
                            counterLayout.addView(counterCreator.addCounter());
                            if(activityName() == "tele"){
                                Log.e("telecounter", "counterMade"+i);
                            }
                        }
                    }
                    counterLayout.addView(getFillerSpace(1f));
    }



    private LinearLayout getFillerSpace(Float weight) {
        LinearLayout fillerSpace = new LinearLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return fillerSpace;
    }



    private void updateData() {
        if(activityName() == "tele"){
            Log.e("toggledCalled", "called");
            List<View> toggleList = toggleCreator.getComponentViews();
            Log.e("toggledCalled", "called");
            for (int i = 0; i < toggleList.size(); i++) {
                Log.e("toggledCalled", "called");
                ToggleButton toggleButton = (ToggleButton) toggleList.get(i);
                try {
                    DataManager.addZeroTierJsonData(toggleButton.getText().toString(), toggleButton.isChecked());
                } catch (Exception e) {
                    Log.e("Data Error", "Failed to add toggle " + Integer.toString(i) + " to Data");
                    Toast.makeText(this, "Invalid data in counter" + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        Log.e("HOPESIZE", counterCreator.getComponentViews().size()+"");
        List<View> currentTextViews = counterCreator.getComponentViews();
        Log.e("HOPESIZE", currentTextViews.size()+"");
        for (int i = 0; i < currentTextViews.size(); i++) {
            if(currentTextViews.get(i) != null){
                Log.e("MOREHOPE", i+"");
            }
            try {
                Log.e("keyCOUNTER", getCounterData().get(i));
                DataManager.addZeroTierJsonData(getCounterData().get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add counter" + Integer.toString(i) + " num to Data");
                Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                return;
            }
        }

        Log.e("check", "CHECKGEAR");
        List<String> gearLifts = Arrays.asList("allianceWall", "boiler", "hpStation");
        List<Object> gearNums = new ArrayList<>();
        Log.e("check", "CHECKGEAR");
        gearNums.add(gearCreator.getNumGearsLiftOne());
        gearNums.add(gearCreator.getNumGearsLiftTwo());
        gearNums.add(gearCreator.getNumGearsLiftThree());
        Log.e("CHECKPOINT", "CHECKGEAR");
        if(activityName() == "auto"){
            Log.e("gearData", "auto");
            DataManager.addOneTierJsonData(false, "gearsPlacedByLiftAuto", gearLifts, gearNums);
        }else if(activityName() == "tele"){
            Log.e("gearData", "tele");
            DataManager.addOneTierJsonData(false, "gearsPlacedByLiftTele", gearLifts, gearNums);
        }
    }

    private Intent prepareIntent(Class clazz) {
        Log.e("intentCalled", "called");
        updateData();
        Intent intent = new Intent(this, clazz);
        intent.putExtras(this.intent);
        try {
            intent.putExtra("previousData", Utils.serializeClass(DataManager.collectedData));
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

            if(activityName() == "tele"){
                Log.e("collectedData", DataManager.collectedData.toString());
                Utils.SendFirebaseData(databaseReference);
            }

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
