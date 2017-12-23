package com.example.violet30;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.UUID;

/**
 * Created by howardzhang on 11/19/17.
 */
@TargetApi(18)
public class BluetoothHelper implements BLEAction{

    //class variables
    public static final UUID UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    public static final UUID UUID_BUTTON_TOUCH = UUID.fromString("00000010-0000-3512-2118-0009af100700");

    //object variables
    private MainActivity main;
    private Handler handler;
    private BLEAction listener;
    private BluetoothDevice bluetoothDevice = null;
    private boolean isConnectedGatt = false;
    private BluetoothGatt gatt = null;

    public BluetoothHelper(MainActivity main){
        Log.d("Order", "Setting up bluetooth");
        this.main = main;
        handler = new Handler(Looper.getMainLooper());
        listener = this;
        findBluetoothDevice(BluetoothAdapter.getDefaultAdapter());
        connectToGatt();
    }

    public boolean isConnected(){
        return isConnectedGatt;
    }

    //FINDS MI BAND 2
    private void findBluetoothDevice(BluetoothAdapter adapter){
        if(adapter.isEnabled()) {
            for (BluetoothDevice pairedDevice : adapter.getBondedDevices()) {
                if (pairedDevice.getName().contains("MI")) {
                    bluetoothDevice =  pairedDevice;
                    break;
                }
            }
        }else{
            main.error("Bluetooth services not enabled.");
        }
        if(bluetoothDevice==null){
            main.error("Could not find bluetooth device.");
        }
    }


    //CONNECTS TO GATT
    private void connectToGatt(){
        if(bluetoothDevice != null){
            gatt = bluetoothDevice.connectGatt(main, true, gattCallback);
        }
    }

    //DISCONNECTS FROM GATT
    private void disconnectGatt(){
        if(gatt != null && isConnectedGatt)
        {
            handler.post(new Runnable()
            {
                @Override public void run()
                {
                    gatt.disconnect();
                    gatt.close();
                    gatt = null;
                    isConnectedGatt = false;
                }
            });
        }
    }

    //SET UP GATT CALLBACKS
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback(){
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {}

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            if(newState==BluetoothProfile.STATE_CONNECTED){
                gatt.discoverServices();
                isConnectedGatt = true;
                raiseOnConnect();
            }else{
                isConnectedGatt = false;
                raiseOnDisconnect();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            raiseOnNotification(gatt, characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
        }

    };

    //Called from Gatt Callback: updates listeners
    public void raiseOnNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // Notify everybody that may be interested.
        listener.onNotification(gatt, characteristic);
    }
    public void raiseOnDisconnect() {
        // Notify everybody that may be interested.
        listener.onDisconnect();
    }
    public void raiseOnConnect() {
        Log.d("Order", "Bluetooth connected");
        // Notify everybody that may be interested.
        listener.onConnect();
    }

    //handles notifications
    public void getNotifications(UUID service, UUID Characteristics, boolean state) {
        if (!isConnectedGatt || gatt == null) {
            return;
        }
        BluetoothGattService myGatService = gatt.getService(service/*Consts.UUID_SERVICE_MIBAND_SERVICE*/);
        if (myGatService != null) {
            BluetoothGattCharacteristic myGatChar = myGatService.getCharacteristic(Characteristics/*Consts.UUID_BUTTON_TOUCH*/);
            if (myGatChar != null) {
                // second parameter is for starting\stopping the listener.
                boolean status =  gatt.setCharacteristicNotification(myGatChar, state);
            }
        }
    }

    //enables touch notifications
    public void enableTouchNotifications() {
        getNotifications(UUID_SERVICE_MIBAND2_SERVICE, UUID_BUTTON_TOUCH, true);
    }
    //disables touch notifications
    public void disableTouchNotifications(){
        getNotifications(UUID_SERVICE_MIBAND2_SERVICE, UUID_BUTTON_TOUCH, false);
    }

    //disconnect from Gatt
    public void destroy(){
        disconnectGatt();
    }

    //BLEAction Implementation
    @Override
    public void onDisconnect() {
    }
    @Override
    public void onConnect() {
    }
    @Override
    public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID alertUUID = characteristic.getUuid();
        if (alertUUID.equals(UUID_BUTTON_TOUCH)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    main.detected();
                }
            });
        }
    }
}
