package uhk.fim.smap.matoulek.smap_app_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by domitea on 1/31/2017.
 */
public class BluetoothService {
    private static BluetoothService ourInstance = new BluetoothService();

    private BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    private BluetoothDevice selectedDevice;

    private final UUID UUID_Serial = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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

    public String getSelectedDevice() {
        return "Device:" + selectedDevice.getName() + " " + selectedDevice.getAddress();
    }

    public ArrayList<String> getListOfDevices() {
        ArrayList<String> devices = new ArrayList<>();
        for (BluetoothDevice b: bluetoothAdapter.getBondedDevices()) {
            devices.add(b.getName());
        }
        return devices;
    }

    public void selectDevice(int index) {
        selectedDevice = (BluetoothDevice) bluetoothAdapter.getBondedDevices().toArray()[index];
    }



    public void setHandler(Handler handler){
        this.handler = handler;
    }

    private class CommunicationThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;
        private final Handler handler;

        public CommunicationThread(BluetoothSocket socket, Handler handler) {
            this.socket = socket;
            this.handler = handler;

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            inputStream = tmpIn;
            outputStream = tmpOut;

        }

        public void run() {
            byte[] buffer;
            ArrayList<Integer> buffer_data = new ArrayList<>();

            while (true)
            {
                try {
                    int data = inputStream.read();
                    if (data == (int) '!')
                    {
                        buffer = new byte[buffer_data.size()];
                        for(int i = 0; i < buffer_data.size(); i++) {
                            buffer[i] = buffer_data.get(i).byteValue();
                        }
                        Log.d("SMAP_APP", "run: recieved " + buffer_data.toString());
                        handler.obtainMessage(1,buffer.length, -1, buffer).sendToTarget();
                        buffer_data = new ArrayList<>();
                    }
                    else
                    {
                        buffer_data.add(data);
                        Log.d("SMAP APP", "run: added " + data);
                    }
                } catch (IOException e)
                {
                    cancel();
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                Log.d("SMAP_APP", "write: sended " + buffer);
                outputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                socket.close();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
}
