package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Calvin on 8/9/17.
 */

public class ShotListAdapter extends BaseAdapter {

    public static LayoutInflater layoutInflater = null;
    Dialog listDialog;
    Context activityContext;
    String title;
    ListModificationListener listInterface;
    ArrayList<HashMap<String, Object>> dataList;

    public ShotListAdapter(Activity currentActivity, ArrayList<HashMap<String, Object>> dataList, Dialog listDialog, String title, ListModificationListener lml){
        activityContext = currentActivity;
        this.dataList = dataList;
        this.listDialog = listDialog;
        this.title = title;
        listInterface = lml;
        layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged(){
        if(dataList.size() > 0){
            super.notifyDataSetChanged();
        } else {
            listDialog.cancel();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int inversePosition = dataList.size() - position - 1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.shots_list_cv, null);
        }

        ((TextView) convertView.findViewById(R.id.cvPosition)).setText((String) dataList.get(inversePosition).get("position"));
        ((TextView) convertView.findViewById(R.id.cvNumShots)).setText(String.valueOf(dataList.get(inversePosition).get("numShots")));
        ((TextView) convertView.findViewById(R.id.cvTime)).setText(String.valueOf(dataList.get(inversePosition).get("time")) + " sec");
        ((ImageButton) convertView.findViewById(R.id.cvDelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(inversePosition);
                listInterface.onListChanged(dataList);
                notifyDataSetChanged();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View subDialogView = layoutInflater.inflate(R.layout.shot_dialog, null);
                final RadioGroup shotPositionGoup = (RadioGroup) subDialogView.findViewById(R.id.radioGroup);
                try {
                    shotPositionGoup.check(findRadioId((String) dataList.get(inversePosition).get("position")));
                } catch (NullPointerException npe){
                    Toast.makeText(activityContext, "No Radiobutton Was Checked",
                            Toast.LENGTH_SHORT).show();
                }
                ((TextView) subDialogView.findViewById(R.id.title)).setText(title);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                dialogBuilder.setView(subDialogView);
                dialogBuilder.setCancelable(false);
                final AlertDialog dialog = dialogBuilder.create();

                final TextView numberView = (TextView) subDialogView.findViewById(R.id.numberView);
                numberView.setText(String.valueOf(dataList.get(inversePosition).get("numShots")));
                ((Button) subDialogView.findViewById(R.id.plusTenButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer previousNumber = Integer.parseInt(numberView.getText().toString());
                        previousNumber += 10;
                        numberView.setText(String.valueOf(previousNumber));
                    }
                });
                ((Button) subDialogView.findViewById(R.id.plusButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer previousNumber = Integer.parseInt(numberView.getText().toString());
                        previousNumber++;
                        numberView.setText(String.valueOf(previousNumber));
                    }
                });
                ((Button) subDialogView.findViewById(R.id.minusButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer previousNumber = Integer.parseInt(numberView.getText().toString());
                        previousNumber--;
                        numberView.setText(String.valueOf(previousNumber));
                    }
                });
                ((Button) subDialogView.findViewById(R.id.minusTenButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer previousNumber = Integer.parseInt(numberView.getText().toString());
                        previousNumber -= 10;
                        numberView.setText(String.valueOf(previousNumber));
                    }
                });

                ((Button) subDialogView.findViewById(R.id.successButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Integer finalShots = Integer.parseInt(numberView.getText().toString());
                        RadioButton shotPositionButton = (RadioButton) subDialogView.findViewById(shotPositionGoup.getCheckedRadioButtonId());
                        if (shotPositionButton != null) {
                            String finalPosition = shotPositionButton.getText().toString();
                            dataList.get(inversePosition).put("position", finalPosition);
                            dataList.get(inversePosition).put("numShots", finalShots);
                            listInterface.onListChanged(dataList);
                            dialog.cancel();
                            listDialog.cancel();
                        } else {
                            Toast.makeText(activityContext, "Please Indicate a Valid Location.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                ((Button) subDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                    }
                });
                dialog.show();
            }
        });

        return convertView;
    }

    public Integer findRadioId(String radioText){
        if(radioText.equals(("Other"))){
            return R.id.otherRadio;
        } else if(radioText.equals("Alliance Wall")){
            return R.id.allianceWallRadio;
        } else if(radioText.equals("Hopper")){
            return R.id.hopperRadio;
        } else if(radioText.equals("Key")){
            return R.id.keyRadio;
        } else {
            return null;
        }
    }

    public interface ListModificationListener{
        void onListChanged(ArrayList<HashMap<String, Object>> returnList);
    }
}

