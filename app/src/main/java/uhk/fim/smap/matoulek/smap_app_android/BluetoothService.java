package uhk.fim.smap.matoulek.smap_app_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by domitea on 1/31/2017.
 */
public class BluetoothService {
    private static BluetoothService ourInstance = new BluetoothService();

    private BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;

    Handler handler;

    public static BluetoothService getInstance() {
        return ourInstance;
    }
    private BluetoothService() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Log.e("SMAP_APP", "No Bluetooth");
        }
    }

    public void printPairedDevices() {
        pairedDevices = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice b : pairedDevices) {
            Log.d("SMAP_APP", "printPairedDevices: " + b.toString());
        }
    }

    public ArrayList<String> getListOfDevices() {
        ArrayList<String> devices = new ArrayList<>();
        for (BluetoothDevice b: bluetoothAdapter.getBondedDevices()) {
            devices.add(b.getName());
        }
        return devices;
    }

    public void setHandler(Handler handler){
        this.handler = handler;
    }
}
