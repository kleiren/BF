package com.controller.app;

import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

/**
 * Created by carlos on 2/24/14.
 */
public class BluetoothSingleton {
    private static BluetoothSingleton manager;
    private BF_ComUtil bluetooth;
    private BFDeviceData bluefin;

    private BluetoothSingleton(BluetoothSocket socket) {
        setBluetooth(new BF_ComUtil(socket));
        setBluefin(new BFDeviceData());
    }

    public static BluetoothSingleton getBluetoothManager(BluetoothSocket socket) {
        if (manager == null) {
            manager = new BluetoothSingleton(socket);
        }
        return manager;
    }

    public static BluetoothSingleton getBluetoothManager() {
        if (manager == null) {

        }
        return manager;
    }

    public BF_ComUtil getBluetooth() {
        return bluetooth;
    }

    public void setBluetooth(BF_ComUtil bluetooth) {
        this.bluetooth = bluetooth;
    }

    public BFDeviceData getBluefin() {
        return bluefin;
    }

    public void setBluefin(BFDeviceData bluefin) {
        this.bluefin = bluefin;
    }
}
