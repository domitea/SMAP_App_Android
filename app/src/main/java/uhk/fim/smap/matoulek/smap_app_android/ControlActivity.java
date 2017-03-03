package uhk.fim.smap.matoulek.smap_app_android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public class ControlActivity extends AppCompatActivity {

    BluetoothService BTService = BluetoothService.getInstance();
    TextView text;
    TextView textSeek;
    SeekBar seekBar;
    ToggleButton gyroToggleButton;

    ProgressBar progressBarOrderedSteps;
    ProgressBar progressBarRealSteps;

    final int STEPS_PER_REV = 200;

    int aimedSteps;
    boolean countPlus;
    int realSteps;
    int settedValueFromProgress;
    int minus = 1;
    boolean absolute_checked = false;
    boolean gyroOn = false;

    double lastAzimuth = 0;
    double azimuth = 0;

    SensorManager sensorManager;
    SensorEventListener sensorEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textSeek = (TextView) findViewById(R.id.textViewSeek);
        gyroToggleButton = (ToggleButton) findViewById(R.id.gyroToggle);

        progressBarOrderedSteps = (ProgressBar) findViewById(R.id.progressPlanned);
        progressBarRealSteps = (ProgressBar) findViewById(R.id.progressReal);

        sensorManager = (SensorManager) getBaseContext().getSystemService(Context.SENSOR_SERVICE);

        initListeners();

        if(!BTService.createSocket(handler)) {
            finish();
        }

        sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_NORMAL);

    }

    private void initListeners() {

        gyroToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    gyroOn = true;
                } else {
                    gyroOn = false;
                }
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                settedValueFromProgress = minus * i;
                textSeek.setText("Nastaveno: " + settedValueFromProgress + " stupnu");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        sensorEventListener = new SensorEventListener() {

            float[] orientation = new float[3];
            float[] rotationMatrix = new float[9];

            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                Sensor sensor = sensorEvent.sensor;
                if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR && gyroOn)
                {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
                    lastAzimuth = azimuth;

                    azimuth = ( Math.toDegrees(SensorManager.getOrientation(rotationMatrix, orientation)[0]) + 360 ) % 360;

                    int deltaAzimuth = (int) (azimuth - lastAzimuth);

                    if (Math.abs(deltaAzimuth) > 0 && Math.abs(deltaAzimuth) < 50)
                    {
                         BTService.send(getCommand(CommandType.MOTOR_RELATIVE, deltaAzimuth));
                    }

                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    //text.setText(msg.obj.toString());
                    byte[] message = (byte[]) msg.obj;
                    parseMessage(message);
            }
        }
    };

    private void parseMessage(byte[] message) {
        if (message.length > 0) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < message.length; i++) {
                if ((message[i] != (byte) 13) || (message[i] != (byte) 10)) {
                    builder.append((char) message[i]);
                }
            }
            String command = builder.toString();
            Log.d("SMAP", "parseMessage: " + command);
            if (command.equals("DISC")) {
                Toast.makeText(this, "Disconnecting", Toast.LENGTH_SHORT);
                finish();
            }
            if (command.equals("GP")) {
                aimedSteps++;
                countPlus = true;
                //Log.d("SMAP", "parseMessage: " + aimedSteps);
                progressBarOrderedSteps.setProgress(Math.abs(aimedSteps) % STEPS_PER_REV);
            }
            if (command.equals("GM")) {
                aimedSteps--;
                countPlus = false;
                //Log.d("SMAP", "parseMessage: " + aimedSteps);
                progressBarOrderedSteps.setProgress(Math.abs(aimedSteps) % STEPS_PER_REV);
            }
            if (command.equals("STEP")) {
                if (countPlus) {
                    realSteps++;
                } else {
                    realSteps--;
                }

                progressBarRealSteps.setProgress(Math.abs(realSteps) % STEPS_PER_REV);

                Log.d("SMAP", "parseMessage real: " + realSteps);
            }
        }
    }

    public void click(View view) {
        //BTService.send(new byte[] {(byte) 'm', (byte) 'r', (byte) '1', (byte) '8', (byte) '0', (byte) '!'});
        if (!absolute_checked) {
                BTService.send(getCommand(CommandType.MOTOR_RELATIVE, settedValueFromProgress));
                progressBarOrderedSteps.setProgress(0);
                progressBarRealSteps.setProgress(0);
                realSteps = 0;
            } else{
                BTService.send(getCommand(CommandType.MOTOR_ABSOLUTE, settedValueFromProgress));
                progressBarOrderedSteps.setProgress(0);
                progressBarRealSteps.setProgress(0);
                realSteps = 0;
            }


    }

    public void onCheckClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        if (view.getId() == R.id.negative_check) {
            if (checked) {
                settedValueFromProgress = -settedValueFromProgress;
                minus = -1;
            } else {
                settedValueFromProgress = -settedValueFromProgress;
                minus = 1;
            }

            textSeek.setText("Nastaveno: " + settedValueFromProgress + " stupňů");
        }
        if (view.getId() == R.id.absolute_check) {
            if (checked) {
                absolute_checked = true;
            } else {
                absolute_checked = false;
            }
        }

    }


    private byte[] getCommand(int command, Object... args) {
        aimedSteps = 0;
        switch (command) {
            case CommandType.MOTOR_RELATIVE: {
                int angle = (int) args[0];
                char[] charAngle = String.valueOf(angle).toCharArray();
                byte[] message = new byte[charAngle.length + 3];
                message[0] = (byte) 'm';
                message[1] = (byte) 'r';
                for (int i = 0; i <= charAngle.length; i++)
                {
                    if (i == charAngle.length) {
                        message[i + 2] = (byte) '!';
                    } else {
                        message[i + 2] = (byte) charAngle[i];
                    }
                }
                return message;
            }
            case CommandType.MOTOR_ABSOLUTE: {
                int angle = (int) args[0];
                char[] charAngle = String.valueOf(angle).toCharArray();
                byte[] message = new byte[charAngle.length + 3];
                message[0] = (byte) 'm';
                message[1] = (byte) 'a';
                for (int i = 0; i <= charAngle.length; i++)
                {
                    if (i == charAngle.length) {
                        message[i + 2] = (byte) '!';
                    } else {
                        message[i + 2] = (byte) charAngle[i];
                    }
                }
                return message;
            }
            case CommandType.HOME_SET:
                return new byte[] {(byte) 'h', (byte) 's', (byte) '!'};
            case CommandType.HOME_HOME:
                return new byte[] {(byte) 'h', (byte) 'h', (byte) '!'};
            case CommandType.HOME_REMOVE:
                return new byte[] {(byte) 'h', (byte) 'r', (byte) '!'};
            default: return null;
        }
    }

    public void onSetHomeClick(View view) {
        Button goHome = (Button) findViewById(R.id.btn_goHome);
        goHome.setEnabled(true);
        goHome.setClickable(true);

        Button remove = (Button) findViewById(R.id.btn_remHome);
        remove.setEnabled(true);
        remove.setClickable(true);

        BTService.send(getCommand(CommandType.HOME_SET, 0));
    }

    public void onGoHomeClick(View view) {
        BTService.send(getCommand(CommandType.HOME_HOME, 0));
    }

    public void onRemoveHome(View view) {
        Button goHome = (Button) findViewById(R.id.btn_goHome);
        goHome.setEnabled(false);
        goHome.setClickable(false);

        Button remove = (Button) findViewById(R.id.btn_remHome);
        remove.setEnabled(false);
        remove.setClickable(false);

        BTService.send(getCommand(CommandType.HOME_REMOVE, 0));
    }

    @Override
    protected void onDestroy() {
        BTService.stop();
        gyroOn = false;
        sensorManager.unregisterListener(sensorEventListener);
        gyroToggleButton.setChecked(false);
        super.onDestroy();
    }

    public static class CommandType {
        public static final int HOME_SET = 1;
        public static final int HOME_REMOVE = 2;
        public static final int HOME_HOME = 3;

        public static final int DEBUG_LED = 4;
        public static final int DEBUG_RESET = 5;
        public static final int DEBUG_STEPPER = 6;

        public static final int MOTOR_RELATIVE = 7;
        public static final int MOTOR_ABSOLUTE = 8;
    }
}