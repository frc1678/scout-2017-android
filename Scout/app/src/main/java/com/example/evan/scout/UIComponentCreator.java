package com.example.evan.scout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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


    public View getNextTitleRow(int titles) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int j = 0; j < titles; j++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            textView.setGravity(Gravity.CENTER);
            textView.setText(componentNames.get(currentComponent));
            currentComponent++;
            row.addView(textView);
        }
        return row;
    }


    public View getNextCounterRow(int counters, int value) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int j = 0; j < counters; j++) {
            final TextView currentCount = new TextView(context);
            currentCount.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            currentCount.setText(Integer.toString(value));
            currentCount.setGravity(Gravity.CENTER);
            componentViews.add(currentCount);


            Button minus = new Button(context);
            minus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            minus.setText("-");
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int prevNum = Integer.parseInt(currentCount.getText().toString());
                    if (prevNum > 0) {
                        prevNum--;
                    }
                    currentCount.setText(Integer.toString(prevNum));
                }
            });



            final Button plus = new Button(context);
            plus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            plus.setText("+");
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int prevNum = Integer.parseInt(currentCount.getText().toString());
                    prevNum++;
                    currentCount.setText(Integer.toString(prevNum));
                }
            });
            row.addView(minus);
            row.addView(currentCount);
            row.addView(plus);
        }
        return row;
    }


    public List<View> getComponentViews() {
        return componentViews;
    }


    public ToggleButton getNextToggleButton (int width, boolean value) {
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

    public Button getNextDefenseButton(int width, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        button.setText(componentNames.get(currentComponent));
        button.setTextSize(button.getTextSize() * textScale);
        currentComponent++;
        return button;
    }




    //sub class specifically for creating defense buttons
    public static class UIButtonCreator extends UIComponentCreator {
        private List<TextView> successTexts;
        private List<TextView> failTexts;
        private Activity context;
        public UIButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
            successTexts = new ArrayList<>();
            failTexts = new ArrayList<>();
        }



        public void addButtonRow(LinearLayout column, final List<Utils.TwoValueStruct<Float, Boolean>> defenseTimes, final Integer index) {
            //add textview counters to layout
            LinearLayout textViewLayout = new LinearLayout(context);
            textViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);

            final TextView successText = new TextView(context);
            successText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            successText.setGravity(Gravity.CENTER);
            successText.setText("S: " + Integer.toString(numOfCrosses(defenseTimes, true)));
            successTexts.add(successText);

            final TextView failText = new TextView(context);
            failText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            failText.setGravity(Gravity.CENTER);
            failText.setText("F: " + Integer.toString(numOfCrosses(defenseTimes, false)));
            failTexts.add(failText);

            textViewLayout.addView(successText);
            textViewLayout.addView(failText);


            //add button to row
            final Button defenseButton = getNextDefenseButton(LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f);
            defenseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //first find out what button was clicked, based on the name of the button
                    final int buttonNum = Integer.parseInt(((Button) v).getText().toString().replaceAll("Defense ", "")) - 1;
                    //next get the time in milliseconds
                    final Long startTime = Calendar.getInstance().getTimeInMillis();


                    //display custom dialog with big buttons
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Attempt Defense " + Integer.toString(buttonNum + 1));

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //add time
                            Utils.TwoValueStruct<Float, Boolean> value =
                                    new Utils.TwoValueStruct<>(
                                            (float)(Calendar.getInstance().getTimeInMillis() - startTime)/(float)1000, true);
                            defenseTimes.add(value);
                            //increment counter
                            successText.setText("S: " + Integer.toString(numOfCrosses(defenseTimes, true)));
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //add time
                            Utils.TwoValueStruct<Float, Boolean> value =
                                    new Utils.TwoValueStruct<>(
                                            (float)(Calendar.getInstance().getTimeInMillis() - startTime)/(float)1000, false);
                            defenseTimes.add(value);
                            //increment counter
                            failText.setText("F: " + Integer.toString(numOfCrosses(defenseTimes, false)));
                            //dismiss dialog
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



            //when they long click, give them list of crosses
            defenseButton.setOnLongClickListener(new View.OnLongClickListener() {
                final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1);
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.defense_edit_dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Edit Defense " + Integer.toString(index + 1) + " Crossings");

                    ListView listView = (ListView) dialogLayout.findViewById(R.id.defenseEditList);
                    listView.setAdapter(adapter);
                    updateUI();

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            deleteCrossing(position);
                        }
                    });
                    /*listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                            final Dialog dialog = new Dialog(context);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            final RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.defense_options_dialog, null);
                            TextView title = (TextView) dialogLayout.findViewById(R.id.defenseOptionsTitle);
                            title.setText(((TextView)view).getText());

                            /*Button moveButton = (Button) dialogLayout.findViewById(R.id.defenseOptionsMoveButton);
                            moveButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EditText editText = (EditText) dialogLayout.findViewById(R.id.defenseOptionMoveEdit);
                                    int defenseTarget;
                                    try {
                                        defenseTarget = Integer.parseInt(editText.getText().toString());
                                        if ((defenseTarget < 0) || (defenseTarget > 5)) {
                                            throw new NumberFormatException();
                                        }
                                    } catch (NumberFormatException nfe) {
                                        Log.e("Scout Error", "Tried to move to a non real defense");
                                        Toast.makeText(context, "Please enter a valid defense", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    Map<Long, Boolean> crossing = defenseTimes.remove(position);
                                    updateUI();
                                    defenseTimes.add(crossing);
                                    dialog.dismiss();
                                }
                            });

                            Button deleteButton = (Button) dialogLayout.findViewById(R.id.defenseOptionsDeleteButton);
                            deleteButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    deleteCrossing(position);
                                }
                            });

                            Button cancelButton = (Button) dialogLayout.findViewById(R.id.defenseOptionsCancelButton);
                            cancelButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });

                            dialog.setContentView(dialogLayout);
                            dialog.show();
                            return true;
                        }
                    });*/

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    //when they dismiss, update ui
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            successTexts.get(index).setText("S: " + Integer.toString(numOfCrosses(defenseTimes, true)));
                            failTexts.get(index).setText("F: " + Integer.toString(numOfCrosses(defenseTimes, false)));
                        }
                    });
                    dialog.show();
                    return true;
                }


                private void deleteCrossing(int position) {
                    //remove from list
                    defenseTimes.remove(position);
                    //update ui
                    updateUI();
                }
                private void updateUI() {
                    adapter.clear();
                    for (int i = 0; i < defenseTimes.size(); i++) {
                        Utils.TwoValueStruct<Float, Boolean> value = defenseTimes.get(i);
                        if (value.value2) {
                            adapter.add("Defense Succeeded (" + Float.toString(value.value1) + "s)");
                        } else {
                            adapter.add("Defense Failed (" + Float.toString(value.value1) + "s)");
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });



            column.addView(defenseButton);
            column.addView(textViewLayout);
        }

        //get number of successes or fails in list
        private int numOfCrosses(List<Utils.TwoValueStruct<Float, Boolean>> defenseTimes, boolean success) {
            int counter = 0;
            for (int i = 0; i < defenseTimes.size(); i++) {
                Utils.TwoValueStruct<Float, Boolean> entry = defenseTimes.get(i);
                if (entry.value2 == success) {
                    counter++;
                }
            }
            return counter;
        }
    }




    public static class UIShotCreator extends UIComponentCreator {
        private List<Integer> shotsMade;
        private List<Integer> shotsMissed;
        private Activity context;
        public UIShotCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            shotsMade = new ArrayList<>();
            shotsMissed = new ArrayList<>();
            this.context = context;
        }
        public void addButtonRow(LinearLayout column, final Integer startShotsMade, Integer startShotsMissed, final int index) {
            LinearLayout textViewLayout = new LinearLayout(context);
            textViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);

            final TextView successText = new TextView(context);
            successText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            successText.setGravity(Gravity.CENTER);
            successText.setText("Made: " + Integer.toString(startShotsMade));
            shotsMade.add(index, startShotsMade);

            final TextView failText = new TextView(context);
            failText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            failText.setGravity(Gravity.CENTER);
            failText.setText("Missed: " + Integer.toString(startShotsMissed));
            shotsMissed.add(index, startShotsMissed);

            textViewLayout.addView(successText);
            textViewLayout.addView(failText);

            final String titleString = super.componentNames.get(super.currentComponent);
            final Button defenseButton = getNextDefenseButton(LinearLayout.LayoutParams.MATCH_PARENT, 0.4f);
            defenseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText(titleString);

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                    success.setText("Made");
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade.set(index, shotsMade.get(index) + 1);
                            successText.setText("Made: " + shotsMade.get(index));
                            dialog.dismiss();
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
                    failure.setText("Missed");
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMissed.set(index, shotsMissed.get(index) + 1);
                            failText.setText("Missed: " + shotsMissed.get(index));
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
            defenseButton.setOnLongClickListener(new View.OnLongClickListener() {
                Boolean wasCancelled;
                @Override
                public boolean onLongClick(View v) {
                    wasCancelled = false;
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.counters_dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Edit " + titleString + "s");

                    LinearLayout column = (LinearLayout) dialogLayout.findViewById(R.id.contentLayout);
                    LinearLayout fillerSpace = new LinearLayout(context);
                    fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    column.addView(fillerSpace);

                    final UIComponentCreator creator = new UIButtonCreator(context,
                            Arrays.asList(titleString + "s Made", titleString + "s Missed"));
                    LinearLayout counterLayout = new LinearLayout(context);
                    counterLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    counterLayout.setOrientation(LinearLayout.VERTICAL);
                    counterLayout.addView(creator.getNextTitleRow(1));
                    counterLayout.addView(creator.getNextCounterRow(1, shotsMade.get(index)));
                    column.addView(counterLayout);

                    fillerSpace = new LinearLayout(context);
                    fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    column.addView(fillerSpace);

                    counterLayout = new LinearLayout(context);
                    counterLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    counterLayout.setOrientation(LinearLayout.VERTICAL);
                    counterLayout.addView(creator.getNextTitleRow(1));
                    counterLayout.addView(creator.getNextCounterRow(1, shotsMissed.get(index)));
                    column.addView(counterLayout);

                    fillerSpace = new LinearLayout(context);
                    fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    column.addView(fillerSpace);

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wasCancelled = true;
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(dialogLayout);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            List<View> views = creator.getComponentViews();
                            if (!wasCancelled) {
                                shotsMade.set(index, Integer.parseInt(((TextView) views.get(0)).getText().toString()));
                                shotsMissed.set(index, Integer.parseInt(((TextView) views.get(1)).getText().toString()));
                            }
                            successText.setText("Made: " + shotsMade.get(index));
                            failText.setText("Missed: " + shotsMissed.get(index));
                        }
                    });
                    dialog.show();
                    return true;
                }
            });
            column.addView(defenseButton);
            column.addView(textViewLayout);
        }
        public List<Integer> getShotsMade() {return shotsMade;}
        public List<Integer> getShotsMissed() {return shotsMissed;}
    }
}
