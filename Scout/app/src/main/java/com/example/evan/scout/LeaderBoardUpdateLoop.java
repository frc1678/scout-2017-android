package com.example.evan.scout;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.JsonParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

public class LeaderBoardUpdateLoop extends Thread {
    private Application application;
    private static LeaderBoard currentLeaderBoard = null;
    public LeaderBoardUpdateLoop(Application application) {
        this.application = application;
    }
    public static LeaderBoard getCurrentLeaderBoard() {return currentLeaderBoard;}
    private static final String uuid = "3dea0562-f9f8-11e5-86aa-5e5517507c66";
    public void run() {
        BluetoothServerSocket serverSocket;
        try {
            serverSocket = setupServer();
        } catch (IOException ioe) {
            writeError("Server setup failed, Exiting");
            return;
        }
        while (true) {
            BluetoothSocket socket;
            try {
                socket = serverSocket.accept();
            } catch (IOException ioe) {
                writeError("Failed to open socket from serverSocket");
                return;
            }
            BufferedReader in;
            PrintWriter out;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe) {
                writeError("Failed to open streams from socket");
                return;
            }
            int byteSize;
            try {
                byteSize = Integer.parseInt(in.readLine());
            } catch (IOException|NumberFormatException ioe) {
                writeError("Failed to read byte size from input stream");
                return;
            }
            String data;
            try {
                data = in.readLine();
                if (!in.readLine().equals("\0")) {
                    throw new IOException();
                }
            } catch (IOException ioe) {
                writeError("Failed to read data from imput stream");
                return;
            }
            if (data.length() != byteSize) {
                writeError("Corrupted data was sent from super");
                out.println("1");
                out.flush();
                if (out.checkError()) {
                    writeError("Failed to reply with error code 1");
                }
                return;
            }
            LeaderBoard leaderBoard;
            try {
                leaderBoard = (LeaderBoard) Utils.deserializeClass(data, LeaderBoard.class);
            } catch (Exception e) {
                writeError("Data sent from super was not in leaderboard format");
                out.println("2");
                out.flush();
                if (out.checkError()) {
                    writeError("Failed to reply with error code 2");
                }
                return;
            }
            out.println("0");
            out.flush();
            if (out.checkError()) {
                Log.i("Bluetooth Info", "Failed to reply with success code. Unimportant");
            }
            currentLeaderBoard = leaderBoard;
        }
    }
    private BluetoothServerSocket setupServer() throws IOException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.wtf("Bluetooth Error", "Device Not Configured With Bluetooth");
            throw new IOException();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Log.e("Bluetooth Error", "Bluetooth Not Enabled");
            throw new IOException();
        }
        try {
            return bluetoothAdapter.listenUsingRfcommWithServiceRecord("Test_Connection", UUID.fromString(uuid));
        } catch (IOException ioe) {
            Log.e("Bluetooth Error", "Failed to set up server");
            throw new IOException();
        }
    }
    private void writeError(String logMessage) {
        Log.e("Bluetooth Error", logMessage);
        toastText("Error in Leader Board updates");
    }
    private void toastText(final String text) {
        final Activity activity = ((ScoutApplication) application).getCurrentActivity();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_LONG).show();
            }
        });
    }
 }
