package uhk.fim.smap.matoulek.smap_app_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ControlActivity extends AppCompatActivity {

    BluetoothService BTService = BluetoothService.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        TextView text = (TextView) findViewById(R.id.textView);

        text.setText(BTService.getSelectedDevice());

    }
}
