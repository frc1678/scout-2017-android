package com.example.evan.scout;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

public class LeaderBoardUpdateLoop extends Thread {
    private String uuid = "3dea0562-f9f8-11e5-86aa-5e5517507c66";
    public void run() {
        BluetoothServerSocket serverSocket;
        try {
            serverSocket = setupServer();
        } catch (IOException ioe) {
            Log.e("Bluetooth Error", "Failed to set up server");
            return;
        }
        while (true) {
            try {
                BluetoothSocket socket = serverSocket.accept();
            } catch (IOException ioe) {

            }
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
        return bluetoothAdapter.listenUsingRfcommWithServiceRecord("Test_Connection", UUID.fromString(uuid));
    }
 }
