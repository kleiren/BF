

package com.controller.app;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class MainActivity extends Activity {
    private static final String TAG = "bluetooth2";

    Button btnCreateSock, btnCheckSock, btnEcho, btnCheckCon, getImage, btnOFF, btnSec, btnStat;


    private BluetoothAdapter btAdapter = null;
    protected BluetoothSocket btSocket = null;


    BF_ComUtil Bluetooth=null;
    BFDeviceData Bluefin=null;




    private StringBuilder sb = new StringBuilder();

    //private ConnectedThread mConnectedThread;

    // SPP UUID service
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // MAC-address of Bluetooth module (you must edit this line)
    private static String address = "00:15:71:17:9A:B9";
    // private static String address = "00:15:FF:F2:19:5F";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        btnCreateSock = (Button) findViewById(R.id.btnCreateSock);
        btnCheckSock = (Button) findViewById(R.id.btnCheckSock);
        btnCheckCon = (Button) findViewById(R.id.btnCheckCon);
        getImage = (Button) findViewById(R.id.getImage);
        btnEcho = (Button) findViewById(R.id.btnEcho);
        btnSec = (Button) findViewById(R.id.btnSec);
        btnStat = (Button) findViewById(R.id.btnStat);
        btnOFF = (Button) findViewById((R.id.btnOFF));






        btAdapter = BluetoothAdapter.getDefaultAdapter();		// get Bluetooth adapter
        checkBTState();


        btnCreateSock.setOnClickListener(new OnClickListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onClick(View v) {

                // Set up a pointer to the remote node using it's address.
                BluetoothDevice device = btAdapter.getRemoteDevice(address);

                // Two things are needed to make a connection:
                //   A MAC address, which we got above.
                //   A Service ID or UUID.  In this case we are using the
                //     UUID for SPP.

                try {
                    btSocket = createBluetoothSocket(device);
                } catch (IOException e) {
                    errorExit("Fatal Error", "In onResume() and socket create failed: " + e.getMessage() + ".");
                }


                // Discovery is resource intensive.  Make sure it isn't going on
                // when you attempt to connect and pass your message.
                btAdapter.cancelDiscovery();

                // Establish the connection.  This will block until it connects.
                Log.d(TAG, "...Connecting...");
                try {
                    btSocket.connect();
                    Log.d(TAG, "....Connection ok...");

                    Bluetooth=BluetoothSingleton.getBluetoothManager(btSocket).getBluetooth();
                    Bluefin=BluetoothSingleton.getBluetoothManager(btSocket).getBluefin();
                } catch (IOException e) {
                    try {
                        btSocket.close();
                    } catch (IOException e2) {
                        errorExit("Fatal Error", "In onResume() and unable to close socket during connecture" + e2.getMessage() + ".");
                    }
                }

                // Create a data stream so we can talk to server.
                Log.d(TAG, "...Create Socket...");




            }
        });
        



        btnCheckSock.setOnClickListener(new OnClickListener() { //Check if the socket is connected
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onClick(View v) {

                String a = "Socket: " + btSocket.isConnected();
                Toast.makeText(getBaseContext(), a, Toast.LENGTH_SHORT).show();
            }
        });

        btnCheckCon.setOnClickListener(new OnClickListener() { //Check if the device is initialized
            public void onClick(View v) {



                String a = "Connection: " + Bluetooth.isIsInit();
                Toast.makeText(getBaseContext(), a, Toast.LENGTH_SHORT).show();


            }
        });

        btnEcho.setOnClickListener(new OnClickListener() { //Starts echo activity
            public void onClick(View v) {



                Intent ac = new Intent(MainActivity.this, Echo.class);

                startActivityForResult(ac, 0);

            }
        });


        btnSec.setOnClickListener(new OnClickListener() { //Creates a secure connection with the device (getting mKey directly from BF_comutil, thats wrong)

            public void onClick(View v) {

                Bluetooth.BF_SetSessionKey(Bluetooth.mKey, Bluefin);

                Toast.makeText(getBaseContext(), "Secure Connection: "+ Bluetooth.isSecure(), Toast.LENGTH_SHORT).show();



            }
        });

        btnStat.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {



                Intent ac = new Intent(MainActivity.this, Status.class);

                startActivityForResult(ac, 0);

            }
        });

        getImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {



                Intent ac = new Intent(MainActivity.this, GetImage.class);

                startActivityForResult(ac, 0);

            }
        });

        btnOFF.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                Bluetooth.BF_PowerOff(Bluefin);


            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        if(Build.VERSION.SDK_INT >= 10){
            try {
                final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
                return (BluetoothSocket) m.invoke(device, MY_UUID);
            } catch (Exception e) {
                Log.e(TAG, "Could not create Insecure RFComm Connection",e);
            }
        }
        return  device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "...onResume - try connect...");



    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "...In onPause()...");

        try     {
            btSocket.close();
        } catch (IOException e2) {
            errorExit("Fatal Error", "In onPause() and failed to close socket." + e2.getMessage() + ".");
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }



}