package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

//class to send data over bluetooth and save it to disk
//we treat sending and saving differently; in some cases you may want to send but not save
public class ConnectThread extends Thread {
    protected static BluetoothDevice device = null;
    protected static final Object deviceLock = new Object();
    protected MainActivity context;
    protected String superName;
    protected String uuid;
    private List<File> files = new ArrayList<>();
    private File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData");
    private ConnectThreadData data;



    public ConnectThread(MainActivity context, String superName, String uuid, ConnectThreadData data) {
        this.context = context;
        this.superName = superName;
        this.uuid = uuid;
        this.data = data;
    }



    public static class ConnectThreadData {
        private List<String> fileNames;
        private List<String> dataToSave;
        private List<String> dataToSend;
        private int size;
        public ConnectThreadData(String fileName, String data) {
            this();
            size = 1;
            fileNames.add(fileName.replaceFirst("UNSENT_", ""));
            this.dataToSave.add(data);
            this.dataToSend.add(data);
        }
        public ConnectThreadData(String fileName, String dataToSave, String dataToSend) {
            this();
            size = 1;
            fileNames.add(fileName.replaceFirst("UNSENT_", ""));
            this.dataToSave.add(dataToSave);
            this.dataToSend.add(dataToSend);
        }
        public ConnectThreadData(List<String> fileNames, List<String> data) throws IllegalArgumentException {
            this();
            if (fileNames.size() != data.size()) {
                throw new IllegalArgumentException();
            }
            size = fileNames.size();
            for (int i = 0; i < size; i++) {
                this.fileNames.add(fileNames.get(i).replaceFirst("UNSENT_", ""));
            }
            this.dataToSend.addAll(data);
            this.dataToSave.addAll(data);
        }
        public ConnectThreadData(List<String> fileNames, List<String> dataToSave, List<String> dataToSend) throws IllegalArgumentException {
            this();
            if ((fileNames.size() != dataToSend.size()) || (fileNames.size() != dataToSave.size())) {
                throw new IllegalArgumentException();
            }
            size = fileNames.size();
            for (int i = 0; i < size; i++) {
                this.fileNames.add(fileNames.get(i).replaceFirst("UNSENT_", ""));
            }
            this.dataToSend.addAll(dataToSend);
            this.dataToSave.addAll(dataToSave);
        }
        private ConnectThreadData() {
            fileNames = new ArrayList<>();
            dataToSave = new ArrayList<>();
            dataToSend = new ArrayList<>();
        }
        public int size() {
            return size;
        }
        public List<String> getFileNames() {
            return fileNames;
        }
        public List<String> getDataToSave() {
            return dataToSave;
        }
        public List<String> getDataToSend() {
            return dataToSend;
        }
    }




    public static boolean initBluetooth(final Activity context, String superName) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.wtf("Bluetooth Error", "Device Not Configured With Bluetooth");
            Utils.toastText("Device Not Configured With Bluetooth", Toast.LENGTH_LONG, context);
            return false;
        }
        if (!adapter.isEnabled()) {
            Log.e("Bluetooth Error", "Bluetooth Not Enabled");
            Utils.toastText("Bluetooth Not Enabled", Toast.LENGTH_LONG, context);
            return false;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices.size() < 1) {
            Log.e("Bluetooth Error", "No Paired Devices");
            Utils.toastText("No Paired Devices", Toast.LENGTH_LONG, context);
            return false;
        }
        adapter.cancelDiscovery();
        for (BluetoothDevice tmpDevice : devices) {
            if (tmpDevice.getName().equals(superName)) {
                synchronized (deviceLock) {
                    device = tmpDevice;
                }
                return true;
            }
        }
        Log.e("Bluetooth Error", "No Paired Device With Name: \"" + superName + "\"");
        Utils.toastText("No Paired Device With Name: \"" + superName + "\"", Toast.LENGTH_LONG, context);
        return false;
    }



    private boolean writeToDisk() {
        //first we save to a file so if something goes wrong we have backups.  We use external storage so it is not deleted when app is reinstalled.
        //storage path: /sdcard/Android/MatchData
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("File Error", "External Storage not Mounted");
            Utils.toastText("External Storage Not Mounted", Toast.LENGTH_LONG, context);
            return false;
        }
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory.  Unimportant");
        }
        //we loop through all the data points, write them to files, and save their files to be renamed later
        for (int i = 0; i < data.size(); i++) {
            File file;
            PrintWriter fileWriter;
            try {
                //we first name the file with the prefix "UNSENT_".  If all goes well, it is renamed without the prefix, but if something fails it will still have it.
                file = new File(dir, "UNSENT_" + data.getFileNames().get(i));
                fileWriter = new PrintWriter(file);
            } catch (IOException ioe) {
                Log.e("File Error", "Failed to open file");
                Utils.toastText("Failed To Open File", Toast.LENGTH_LONG, context);
                return false;
            }


            fileWriter.print(data.getDataToSave().get(i));
            fileWriter.close();
            if (fileWriter.checkError()) {
                Log.e("File Error", "Failed to Write to File");
                Utils.toastText("Failed To Save Match Data To File", Toast.LENGTH_LONG, context);
                return false;
            }
            files.add(file);
        }
        return true;
    }



    @Override
    public void run() {
        if (!writeToDisk()) {
            return;
        }


        if(!initBluetooth(context, superName)) {
            return;
        }


        if (data.getDataToSend().get(0) == null) {
            return;
        }

        //convert data to sendable string
        String data = "";
        for (int i = 0; i < this.data.size(); i++) {
            data = data.concat(this.data.getDataToSend().get(i) + "\n");
            Log.i("JSON during send", this.data.getDataToSend().get(i));
        }


        //we try the entire process of sending three times.  If it repeatedly fails, we exit
        int counter = 0;
        while (true) {
            PrintWriter out = null;
            BufferedReader in;
            BluetoothSocket socket = null;
            try {
                //first open connection
                synchronized (deviceLock) {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
                }
                Log.i("Socket Info", "Attempting To Start Connection...");
                socket.connect();
                Log.i("Socket info", "Connection Successful!  Getting Ready To Send Data...");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException ioe) {
                //if it fails, close stuff and start over
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (socket != null) {
                        socket.close();
                    }
                } catch (IOException ioe2) {
                    Log.e("Socket Error", "Failed To End Socket");
                    Utils.toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
                    return;
                }
                if (counter == 2) { //TODO
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
                counter++;
                continue;
            }

            Log.i("Communications Info", "Starting To Communicate");

            try {
                //we print the length of the data before we print the data so the super can identify corrupted data
                out.println(data.length());
                out.print(data);
                //we print '\0' at end of data to signify the end
                out.println("\0");
                out.flush();
                if (out.checkError()) {
                    throw new IOException();
                }
                int ackCode = Integer.parseInt(in.readLine());
                //super will send 0 if the data sizes match up, 1 if they don't
                if (ackCode == 1) {
                    throw new IOException();
                } else if (ackCode == 2) {
                    //data is invalid JSON, notify user and return
                    Log.e("Communications Error", "Data not in valid format");
                    Utils.toastText("Data not in valid format", Toast.LENGTH_LONG, context);
                    return;
                }
            } catch (IOException ioe) {
                try {
                    out.close();
                    in.close();
                    socket.close();
                } catch (IOException ioe2) {
                    Log.e("Socket Error", "Failed To End Socket");
                    Utils.toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
                    return;
                }
                if (counter == 2) { //TODO
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
                counter++;
                continue;
            }

            //we succeeded, close stuff and leave
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ioe) {
                Log.e("Socket Error", "Failed To End Socket");
                Utils.toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
            }
            break;
        }




        Log.i("Communications Info", "Done");
        Utils.toastText("Data Send Success", Toast.LENGTH_LONG, context);
        //rename files
        for (int i = 0; i < this.data.size(); i++) {
            if (!files.get(i).renameTo(new File(dir, this.data.getFileNames().get(i)))) {
                Log.e("File Error", "Failed to Rename File");
            }
        }
    }


}
