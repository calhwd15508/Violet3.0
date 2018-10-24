package com.example.IO;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.util.UUID;

/**
 * Created by howardzhang on 11/19/17.
 */

/*
bluetooth class to control MiBand2 watch
controls various watch functions
works with detection for watch button press
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothHelper implements BLEAction {

    //button touch variables
    private final UUID UUID_SERVICE_MIBAND2_SERVICE = UUID.fromString("0000fee0-0000-1000-8000-00805f9b34fb");
    private final UUID UUID_BUTTON_TOUCH = UUID.fromString("00000010-0000-3512-2118-0009af100700");

    //vibration variables
    public final UUID UUID_ALERT_SERVICE = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
    public final UUID UUID_ALERT = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");

    //heart rate scan varibales
    public final UUID UUID_HR_SERVICE = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb");
    public final UUID UUID_HR_MEASURE = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb");
    public final UUID UUID_HR_DESC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public final UUID UUID_HR_CONTROL = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb");

    //reference to IOManager
    private IOManager managerIO;
    //handler object to execute processes on main thread
    private Handler handler;
    //listener to detect bluetooth signals
    private BLEAction listener;
    //reference to bluetooth device
    private BluetoothDevice bluetoothDevice = null;
    //boolean for whether or not watch is connected
    private boolean isConnectedGatt = false;
    //gatt reference for bluetooth connection
    private BluetoothGatt gatt = null;

    //constructor
    public BluetoothHelper(IOManager managerIO) {
        //setup
        this.managerIO = managerIO;
        handler = new Handler(Looper.getMainLooper());
        listener = this;
        findBluetoothDevice(BluetoothAdapter.getDefaultAdapter());
        //establish connection
        connectToGatt();
    }

    //attempt to connect to bluetooth
    public void establishConnection(){
        findBluetoothDevice(BluetoothAdapter.getDefaultAdapter());
        connectToGatt();
    }

    //returns whether or not watch is connected
    public boolean isConnected() {
        return isConnectedGatt;
    }

    //finds miband2
    private void findBluetoothDevice(BluetoothAdapter adapter) {
        if (adapter.isEnabled()) {
            for (BluetoothDevice pairedDevice : adapter.getBondedDevices()) {
                if (pairedDevice.getName().contains("MI")) {
                    bluetoothDevice = pairedDevice;
                    break;
                }
            }
        }
    }


    //connects to gatt
    private void connectToGatt() {
        if (bluetoothDevice != null) {
            gatt = bluetoothDevice.connectGatt(managerIO.getMain(), true, gattCallback);
        }
    }

    //disconnects from gatt using handler
    private void disconnectGatt() {
        if (gatt != null && isConnectedGatt) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    gatt.disconnect();
                    gatt.close();
                    gatt = null;
                    isConnectedGatt = false;
                }
            });
        }
    }

    //set up bluetooth callbacks
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        /*
        called when a service is discovered
        used for heart rate
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            getHeartRate();
        }

        /*
        called when connected or disconnected
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices();
                isConnectedGatt = true;
                raiseOnConnect();
            } else {
                isConnectedGatt = false;
                raiseOnDisconnect();
            }
        }

        /*
        called when characteristic is changed
        used for heart rate
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            raiseOnNotification(gatt, characteristic);
            super.onCharacteristicChanged(gatt, characteristic);
            if(characteristic.getUuid().equals(UUID_HR_MEASURE)) {
                final byte[] data = characteristic.getValue();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        managerIO.heartRate(data[1]);
                    }
                });
            }
        }

        /*
        called when characteristic is read
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
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
                boolean status = gatt.setCharacteristicNotification(myGatChar, state);
            }
        }
    }

    //enables touch notifications
    public void enableTouchNotifications() {
        getNotifications(UUID_SERVICE_MIBAND2_SERVICE, UUID_BUTTON_TOUCH, true);
    }

    //disables touch notifications
    public void disableTouchNotifications() {
        getNotifications(UUID_SERVICE_MIBAND2_SERVICE, UUID_BUTTON_TOUCH, false);
    }

    //disconnect from Gatt
    public void destroy() {
        disconnectGatt();
    }

    //BLEAction listener Implementation
    @Override
    public void onDisconnect() {
    }

    @Override
    public void onConnect() {
        if(managerIO.bluetoothReconnect) {
            managerIO.bluetoothReconnect = false;
            managerIO.changeDetection(Detection.WATCH_DETECT);
            managerIO.addNode(IOManager.DET_REQUEST, null);
        }
    }

    //on a button touch notification, call detected method of managerIO
    @Override
    public void onNotification(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        UUID alertUUID = characteristic.getUuid();
        if (alertUUID.equals(UUID_BUTTON_TOUCH)) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    managerIO.detected();

                }
            });
        }
    }

    //start watch vibration characteristic
    public void vibrateStart(){
        BluetoothGattCharacteristic gattChar =
                gatt.getService(UUID_ALERT_SERVICE).getCharacteristic(UUID_ALERT);
        gattChar.setValue(new byte[]{2});
        gatt.writeCharacteristic(gattChar);
    }

    //stop watch vibration characteristic
    public void vibrateStop(){
        BluetoothGattCharacteristic gattChar =
                gatt.getService(UUID_ALERT_SERVICE).getCharacteristic(UUID_ALERT);
        gattChar.setValue(new byte[]{0});
        gatt.writeCharacteristic(gattChar);
    }

    //start heart rate scan characteristic
    public void startHRScan(){
        BluetoothGattCharacteristic gattChar = gatt.getService(UUID_HR_SERVICE)
                .getCharacteristic(UUID_HR_CONTROL);
        gattChar.setValue(new byte[]{21, 2, 1});
        gatt.writeCharacteristic(gattChar);
    }

    //read heart rate measurement characteristic
    private void getHeartRate(){
        BluetoothGattCharacteristic gattChar = gatt.getService(UUID_HR_SERVICE)
                .getCharacteristic(UUID_HR_MEASURE);
        gatt.setCharacteristicNotification(gattChar, true);
        BluetoothGattDescriptor desc = gattChar.getDescriptor(UUID_HR_DESC);
        desc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        gatt.writeDescriptor(desc);
    }
}
