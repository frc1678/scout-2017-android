package com.example.evan.scout;

import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

//class to manage schedule
public class ScheduleHandler {
    private JSONObject schedule;
    private MainActivity context;
    private class ScheduleReceiver extends ConnectThread {
        public ScheduleReceiver(MainActivity context, String superName, String uuid) {
            super(context, superName, uuid, null);
        }



        @Override
        public void run() {
            if(!initBluetooth(context, superName)) {
                return;
            }
            //try the whole process three times before quitting
            for (int i = 0; i < 3; i++) {
                PrintWriter out;
                BufferedReader in;
                BluetoothSocket socket;
                int counter = 0;
                //we loop until a connection is made
                while (true) {
                    try {
                        synchronized (deviceLock) {
                            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid)); // make this a constant somewhere nice
                        }
                        Log.i("Socket Info", "Attempting To Start Connection...");
                        socket.connect();
                        Log.i("Socket info", "Connection Successful!  Getting Ready To Send Data...");
                        out = new PrintWriter(socket.getOutputStream(), true);
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        break;
                    } catch (IOException ioe) {
                        Log.e("Socket Error", "Failed To Open Socket");
                        Utils.toastText("Failed To Connect To Super", Toast.LENGTH_SHORT, context);
                        counter++;
                        //if this is the third time, give up
                        if (counter == 3) {
                            Log.e("Socket Error", "Repeated Socket Open Failure");
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Repeated Connection Failure")
                                            .setMessage("Please resend this data when successful data transfer is made.")
                                            .setNeutralButton("Dismiss", null)
                                            .show();
                                }
                            });
                            return;
                        }
                    }
                }


                Log.i("Communications Info", "Starting To Communicate");
                counter = 0;
                //we loop until the data is sent without io error or error code from super
                while (true) {
                    try {
                        //we print -1 to request schedule
                        out.println("-1");
                        out.flush();
                        if (out.checkError()) {
                            throw new IOException();
                        }
                        break;
                    } catch (IOException ioe) {
                        Log.e("Communications Error", "Failed To Send Data");
                        Utils.toastText("Failed To Send Match Data To Super", Toast.LENGTH_SHORT, context);
                        counter++;
                        //after the third failure, we terminate thread and notify user
                        if (counter == 3) {
                            Log.e("Communications Error", "Repeated Data Send Failure");
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new AlertDialog.Builder(context)
                                            .setTitle("Repeated Data Send Failure")
                                            .setMessage("Please resend this data when successful data transfer is made.")
                                            .setNeutralButton("Dismiss", null)
                                            .show();
                                }
                            });
                            return;
                        }
                    }
                }


                int ackCode = -1;
                String data;
                try {
                    //first line will be length of schedule
                    ackCode = Integer.parseInt(in.readLine());
                    data = "";
                    String tmp;
                    while (true) {
                        tmp = in.readLine();
                        //'\0' signifies end
                        if (tmp.equals("\0")) {
                            break;
                        }
                        data = data.concat(tmp);
                    }
                } catch (IOException ioe) {
                    Log.e("Bluetooth Error", "Failed to receive schedule from super");
                    Utils.toastText("Failed to receive schedule from super", Toast.LENGTH_SHORT, context);
                    data = null;
                }


                if (data != null) {
                    //validate ack code
                    if (ackCode == data.length()) {
                        JSONObject schedule;
                        //validate json
                        try {
                            schedule = new JSONObject(data);
                        } catch (JSONException jsone) {
                            Log.e("JSON Error", "Super sent bad schedule");
                            Utils.toastText("Bad Schedule", Toast.LENGTH_LONG, context);
                            return;
                        }
                        Log.i("Bluetooth Info", "Schedule receive success");
                        Utils.toastText("Schedule Receive Success", Toast.LENGTH_LONG, context);
                        onReceive(schedule);
                        //close sockets and stuff
                        try {
                            in.close();
                            out.close();
                            socket.close();
                        } catch (IOException ioe) {
                            Log.e("Socket Error", "Failed To End Socket");
                            Utils.toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
                            return;
                        }
                        return;
                    }
                }


            }
            //after third time trying, we give up
            Log.e("Bluetooth Error", "Repeated Schedule Receive Error");
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(context)
                            .setTitle("Repeated Schedule Receive Failure")
                            .setMessage("Please resend this data when successful data transfer is made.")
                            .setNeutralButton("Dismiss", null)
                            .show();
                }
            });
        }



        //override this method to handle JSONObject
        public void onReceive (JSONObject schedule) {

        }
    }

    public ScheduleHandler(MainActivity context) {
        this.context = context;
    }


    public void getScheduleFromDisk() {
        //first open up file
        boolean scheduleAvailable = true;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Schedule");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        File scheduleFile = new File(dir, "Schedule.txt");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(scheduleFile)));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed to open schedule file");
            Toast.makeText(context, "Schedule not avaialble", Toast.LENGTH_LONG).show();
            scheduleAvailable = false;
        }

        //next read from it
        String scheduleString = "";
        if (scheduleAvailable) {
            try {
                String tmp;
                while ((tmp = in.readLine()) != null) {
                    scheduleString = scheduleString.concat(tmp);
                }
            } catch (IOException ioe) {
                Log.e("File Error", "Failed to read from schedule file");
                Toast.makeText(context, "Schedule not avaialble", Toast.LENGTH_LONG).show();
                scheduleAvailable = false;
            }
        }

        //finally parse it to json format
        if (scheduleAvailable) {
            try {
                schedule = new JSONObject(scheduleString);
            } catch (JSONException jsone) {
                Log.e("File Error", "Failed to parse JSON from schedule file");
                Toast.makeText(context, "Schedule not avaialble", Toast.LENGTH_LONG).show();
                schedule = null;
            }
        }
    }


    public void getScheduleFromSuper(String superName, String uuid) {
        new ScheduleReceiver(context, superName, uuid) {
            @Override
            public void onReceive(JSONObject receivedSchedule) {
                //handle the JSONObject received from super:
                //open file
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Schedule");
                if (!dir.mkdir()) {
                    Log.i("File Info", "Failed to make Directory. Unimportant");
                }
                File scheduleFile = new File(dir, "Schedule.txt");
                PrintWriter out;
                try {
                    out = new PrintWriter(scheduleFile);
                } catch (IOException ioe) {
                    Log.e("File Error", "Failed to save schedule data to file");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Failed to save schedule to file", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                //write to file
                try {
                    out.println(receivedSchedule.toString());
                    if (out.checkError()) {
                        throw new IOException();
                    }
                } catch (IOException ioe) {
                    Log.e("File Error", "Failed to save schedule data to file");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Failed to save schedule to file", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                //save reference to schedule
                schedule = receivedSchedule;
                Log.i("Schedule at onReceive", schedule.toString());
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.updateTeamNumbers();
                    }
                });
            }
        }.start();
    }

    public JSONObject getSchedule() {
        return schedule;
    }
    public boolean hasSchedule() {
        return (schedule != null);
    }
}
