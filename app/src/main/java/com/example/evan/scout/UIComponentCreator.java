package com.example.evan.scout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.SystemUpdatePolicy;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//class that creates all the ui components I need like togglebuttons, etc.  Also stores all buttons in list to be accessed later
public class UIComponentCreator {
    //list of views that hold data that I need to access later
    private List<View> componentViews;
    //list of names of buttons, textviews, etc
    private List<String> componentNames;
    private Activity context;
    //counter for list
    private int currentComponent;

    public UIComponentCreator(Activity context, List<String> componentNames) {
        componentViews = new ArrayList<>();
        this.componentNames = componentNames;
        this.context = context;
        currentComponent = 0;
    }

    public Button getBasicButton (int width, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if(componentNames != null){            button.setText(componentNames.get(currentComponent)); }
        button.setTextSize(button.getTextSize() * textScale);
        return button;
    }

    public ToggleButton getToggleButton (int width, boolean value) {
        ToggleButton toggleButton = new ToggleButton(context);
        toggleButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f));
        toggleButton.setText(componentNames.get(currentComponent));
        toggleButton.setTextOn(componentNames.get(currentComponent));
        toggleButton.setTextOff(componentNames.get(currentComponent));
        toggleButton.setTextSize(toggleButton.getTextSize() * 0.4f);
        toggleButton.setChecked(value);
        currentComponent++;
        componentViews.add(toggleButton);
        return toggleButton;
    }

//    public void getCounters(int counters, int value, LinearLayout parent){
//        for (int j = 0; j < counters; j++) {
//            final TextView currentTitle = new TextView(context);
//            currentTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
//            currentTitle.setText(componentNames.get(j));
//            currentTitle.setGravity(Gravity.CENTER);
//            componentViews.add(currentTitle);
//
//            final TextView currentCount = new TextView(context);
//            currentCount.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
//            currentCount.setText(Integer.toString(value));
//            currentCount.setGravity(Gravity.CENTER);
//            componentViews.add(currentCount);
//
//            Button minus = new Button(context);
//            minus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
//            minus.setText("-");
//            minus.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int prevNum = Integer.parseInt(currentCount.getText().toString());
//                    if (prevNum > 0) {
//                        prevNum--;
//                    }
//                    currentCount.setText(Integer.toString(prevNum));
//                }
//            });
//
//            final Button plus = new Button(context);
//            plus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
//            plus.setText("+");
//            plus.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    int prevNum = Integer.parseInt(currentCount.getText().toString());
//                    prevNum++;
//                    currentCount.setText(Integer.toString(prevNum));
//                }
//            });
//
//            parent.addView(currentTitle);
//            parent.addView(minus);
//            parent.addView(currentCount);
//            parent.addView(plus);
//        }
//    }

    public List<View> getComponentViews() {
        return componentViews;
    }

    public static class UICounterCreator extends UIComponentCreator {
        private int value;
        private String name;
        private Activity context;
        private int currentCounterComponent;

        public UICounterCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentCounterComponent = 0;
            value = 0;
            this.context = context;
            name = "";
        }

        public RelativeLayout addCounter() {
            name = UICounterCreator.super.componentNames.get(currentCounterComponent);
            RelativeLayout counterLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.counter, null);

            TextView titleTV = (TextView) counterLayout.findViewById(R.id.counterTitle);
            titleTV.setText(name);
            Log.e("counterTitle", titleTV.getText().toString());

            final TextView valueTV = (TextView) counterLayout.findViewById(R.id.value);
            valueTV.setText(String.valueOf(value));
            Log.e("counterValue", valueTV.getText().toString());

            Button minus = (Button) counterLayout.findViewById(R.id.minus);
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(value > 0){
                        value --;
                    }
                    valueTV.setText(String.valueOf(value));
                }
            });
            Log.e("counterB", minus.getText().toString());

            Button plus = (Button) counterLayout.findViewById(R.id.plus);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    value ++;
                    valueTV.setText(String.valueOf(value));
                }
            });
            Log.e("counterB", plus.getText().toString());

            currentCounterComponent++;
            super.componentViews.add(valueTV);
            return counterLayout;
        }
    }

    //sub class specifically for creating defense buttons
    public static class UIButtonCreator extends UIComponentCreator {
        private long startTime;
        private long endTime;
        private boolean didLiftOff;
        private Activity context;

        public UIButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
        }

        public Button addButton(final ToggleButton button1, final ToggleButton button2) {
            resetLiftOff();
            //add button to row
            final Button liftOffButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 0.4f);
            liftOffButton.setText("Ready For LiftOff");
            liftOffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    button1.setChecked(false);
                    button2.setChecked(false);

                    //next get the time in milliseconds
                    startTime = System.currentTimeMillis();

                    //display custom dialog with big buttons
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Lift Off");

                    final TextView liftoffTimeView = (TextView) dialogLayout.findViewById(R.id.liftoffTime);
                    final CountDownTimer cdt = new CountDownTimer(135000, 100) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            double currentSeconds = (135000.0 - millisUntilFinished) / 1000;
                            liftoffTimeView.setText(String.valueOf(currentSeconds));
                        }

                        @Override
                        public void onFinish() {

                        }
                    };

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cdt.cancel();
                            endTime = System.currentTimeMillis();
                            float totalTime = (endTime - startTime)/1000;
                            DataManager.addZeroTierJsonData("didLiftoff", true);
                            DataManager.addZeroTierJsonData("liftoffTime", totalTime);
                            //add to sd card
                            dialog.dismiss();
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cdt.cancel();
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(dialogLayout);
                    dialog.show();
                    cdt.start();
                }
            });

            return liftOffButton;
        }
        public void resetLiftOff(){
            DataManager.addZeroTierJsonData("didLiftoff", false);
            DataManager.addZeroTierJsonData("liftoffTime", 0);
        }
    }

    public static class UIGearCreator extends UIComponentCreator {
        private Activity context;
        private int numGearsLiftOne;
        private int numGearsLiftTwo;
        private int numGearsLiftThree;

        public UIGearCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            numGearsLiftOne = 0;
            numGearsLiftTwo = 0;
            numGearsLiftThree = 0;
            this.context = context;
        }

        public Button addButton(){
            final Button gearButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 0.7f);
            gearButton.setText("Gear Placed");
            gearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.gear_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText("Which Lift?");

                    Button liftOneButton = (Button) dialogLayout.findViewById(R.id.liftOneButton);
                    liftOneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftOne += 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftTwoButton = (Button) dialogLayout.findViewById(R.id.liftTwoButton);
                    liftTwoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftTwo += 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftThreeButton = (Button) dialogLayout.findViewById(R.id.liftThreeButton);
                    liftThreeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftThree += 1;
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });

            gearButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.gear_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText("Which Lift?");

                    Button liftOneButton = (Button) dialogLayout.findViewById(R.id.liftOneButton);
                    liftOneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftOne -= 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftTwoButton = (Button) dialogLayout.findViewById(R.id.liftTwoButton);
                    liftTwoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftTwo -= 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftThreeButton = (Button) dialogLayout.findViewById(R.id.liftThreeButton);
                    liftThreeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftThree -= 1;
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();

                    return true;
                }
            });

            return gearButton;
        }

        public int getNumGearsLiftOne() {   return numGearsLiftOne;}
        public int getNumGearsLiftTwo() {   return numGearsLiftTwo;}
        public int getNumGearsLiftThree() {   return numGearsLiftThree;}
    }

    public static class UIShotCreator extends UIComponentCreator {
        private int shotsMade;
        private String position;
        private long startTime;
        private long endTime;
        private float totalTime;
        private String name;
        private Activity context;
        private int currentShotComponent;

        public UIShotCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentShotComponent = 0;
            shotsMade = 0;
            this.context = context;
            name = "LOL";
        }
        public Button addButton(final String shotFBname) {
            name = UIShotCreator.super.componentNames.get(currentShotComponent);
            final Button shotButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 0.7f);
            Log.e("CurrentComponent",String.valueOf(currentShotComponent));
            shotButton.setText(super.componentNames.get(currentShotComponent));
            shotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTime = System.currentTimeMillis();
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.shot_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    final TextView numberView = (TextView) dialogLayout.findViewById(R.id.numberView);

                    Button minusTenButton = (Button) dialogLayout.findViewById(R.id.minusTenButton);
                    minusTenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade -= 10;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button minusButton = (Button) dialogLayout.findViewById(R.id.minusButton);
                    minusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade -= 1;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button plusTenButton = (Button) dialogLayout.findViewById(R.id.plusTenButton);
                    plusTenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade += 10;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button plusButton = (Button) dialogLayout.findViewById(R.id.plusButton);
                    plusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade += 1;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    //TODO make them not multi-selectable
                    RadioButton keyRadioButton = (RadioButton) dialogLayout.findViewById(R.id.keyRadio);
                    keyRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Key";
                        }
                    });

                    RadioButton hopperRadioButton = (RadioButton) dialogLayout.findViewById(R.id.hopperRadio);
                    hopperRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Hopper";
                        }
                    });

                    RadioButton allianceWallRadioButton = (RadioButton) dialogLayout.findViewById(R.id.allianceWallRadio);
                    allianceWallRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Alliance Wall";
                        }
                    });

                    RadioButton otherRadioButton = (RadioButton) dialogLayout.findViewById(R.id.otherRadio);
                    otherRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Other";
                        }
                    });

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(position != null){
                                endTime = System.currentTimeMillis();
                                totalTime = (startTime - endTime)/1000;

                                int i = 0;
                                List<String> shotKeys = Arrays.asList("numShots", "position", "time");
                                List<Object> shotValues = new ArrayList<>();
                                shotValues.clear();
                                shotValues.add(shotsMade);
                                shotValues.add(position);
                                shotValues.add(totalTime);
                                switch(name) {
                                    case "highShotAuto" : i = Constants.highShotAuto; break;
                                    case "lowShotAuto" : i = Constants.lowShotAuto; break;
                                    case "highShotTele" : i = Constants.highShotTele; break;
                                    case "lowShotTele" : i = Constants.lowShotTele; break;
                                }
                                DataManager.addOneTierJsonData(true, i+"", shotKeys, shotValues);
                                DataManager.addZeroTierJsonData(shotFBname,DataManager.sideData);
                                dialog.dismiss();
                            }else{
                                //TODO make a toast
                            }
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                           //Firebase stuff
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
            currentShotComponent++;
            super.componentViews.add(shotButton);
            return shotButton;
        }
    }
}
