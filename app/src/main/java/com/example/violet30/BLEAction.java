package com.example.violet30;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * Created by howardzhang on 12/22/17.
 */

//Listener interface for Bluetooth
public interface BLEAction {
    void onDisconnect();
    void onConnect();
    void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
}
