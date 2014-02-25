package com.controller.app;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class GetImage extends ActionBarActivity {



    BF_ComUtil Bluetooth=null;
    BFDeviceData Bluefin=null;
    Button btnGetImage;
    ImageView imagen;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_image);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        Bluetooth=BluetoothSingleton.getBluetoothManager().getBluetooth();
        Bluefin=BluetoothSingleton.getBluetoothManager().getBluefin();



        btnGetImage = (Button) findViewById(R.id.button);
        imagen = (ImageView) findViewById(R.id.imageView);



        btnGetImage.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onClick(View v) {



                Bluetooth.BF_SetScanStatus(false,Bluefin);

                Bluetooth.BF_SetScanStatus(true,Bluefin);



                MediaStore.Images img;
                byte[] byteArray = null ;
                byteArray=Bluetooth.BF_GetFPImg(Bluefin);
                int[] raw = new int[36864];



                Bitmap bmpImage = Bitmap.createBitmap( 240, 360, Bitmap.Config.ARGB_8888);
                bmpImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imagen.setImageBitmap(bmpImage);
            }
        });






    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.get_image, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_get_image, container, false);
            return rootView;
        }
    }

}
