package uhk.fim.smap.matoulek.smap_app_android;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

        BTService.createSocket(handler);

    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    text.setText(msg.obj.toString());
            }
        }
    };

    public void click(View view) {
        BTService.send(new byte[] {(byte) 'X', (byte) 'B', (byte) '!'});
    }
}
