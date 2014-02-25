package com.controller.app;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class Status extends ActionBarActivity {




    BF_ComUtil Bluetooth=null;
    BFDeviceData Bluefin=null;
    Button btnRequest;
    TextView battery, name, imgStatus;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }


        btnRequest = (Button) findViewById(R.id.button);
        btnRequest.setOnClickListener(new View.OnClickListener() {
           // @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onClick(View v) {
                battery= (TextView) findViewById(R.id.battery);
                imgStatus= (TextView) findViewById(R.id.imgStatus);

                name= (TextView) findViewById(R.id.name);


                Bluetooth=BluetoothSingleton.getBluetoothManager().getBluetooth();
                Bluefin=BluetoothSingleton.getBluetoothManager().getBluefin();


                Bluetooth.BF_GetFPStatus (Bluefin);
                Bluetooth.BF_GetInfo(Bluefin);


                battery.setText("Battery: "+Bluefin.getBattStatus());
                name.setText("Device name: " + Bluefin.getDeviceName());
                imgStatus.setText("Fingerprint Image Status: "+Bluefin.getfingerImgStatus());



            }
        });






    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.status, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_echo, container, false);
            return rootView;
        }
    }

}
