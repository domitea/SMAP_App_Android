package uhk.fim.smap.matoulek.smap_app_android;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class ControlActivity extends AppCompatActivity {

    BluetoothService BTService = BluetoothService.getInstance();
    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

         text = (TextView) findViewById(R.id.textView);

        text.setText(BTService.getSelectedDevice());

        if(!BTService.createSocket(handler)) {
            finish();
        }

    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    text.setText(msg.obj.toString());
                    byte[] message = msg.getData().getByteArray(null);
                    parseMessage(message);
            }
        }
    };

    private void parseMessage(byte[] message) {
        
    }

    public void click(View view) {
        BTService.send(new byte[] {(byte) 'm', (byte) 'r', (byte) '1', (byte) '8', (byte) '0', (byte) '!'});
    }

    @Override
    protected void onStop() {
        BTService.stop();
    }
}
