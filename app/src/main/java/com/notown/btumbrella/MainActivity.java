package com.notown.btumbrella;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {


    // Debugging
    private static final String TAG = "BluetoothChat";

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    // Layout Views
    private TextView status;
    private Button goToGame;
    private GifView gif[] = new GifView[8];
    private GifView gifUmbrella;
    private RelativeLayout layout;

    private int ledDrawable[] = {R.drawable.led1, R.drawable.led2, R.drawable.led3, R.drawable.led4,
            R.drawable.led5, R.drawable.led6, R.drawable.led7, R.drawable.led8};
    private int gifID[]={R.id.gif0,R.id.gif1,R.id.gif2,R.id.gif3,R.id.gif4,R.id.gif5,R.id.gif6,R.id.gif7};
    private boolean ledsetornot[] = {false, false, false, false, false, false, false, false};

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    static BluetoothSerialService mChatService = null;
    //our umbrella device name
    private final String DEVICENAME = "HC-05";
    //umbrella device address
    private String deviceAddress;

    private int layoutWidth, layoutHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        setUpViewComponent();

        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bt_not_available,
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        //開一個new thread，用以做gif position 的隨機選取
        layout.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                layoutWidth = layout.getWidth() - 250;//避免圖像太靠邊邊
                layoutHeight = layout.getHeight() - 250;
                //set gif position
                float x;
                float y;
                for (int i = 0; i < 8; i++) {
                    x = 50 + (float) (Math.random() * layoutWidth);
                    y = 50 + (float) (Math.random() * layoutHeight);
                    gif[i].setX(x);
                    gif[i].setY(y);
                }
            }
        });

    }

    private void setUpViewComponent() {
        status = (TextView) findViewById(R.id.status);
        goToGame = (Button) findViewById(R.id.gogame);
        goToGame.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });
        //set gifview
        for(int i=0;i<8;i++)
            gif[i] = (GifView) findViewById(gifID[i]);

        for (int i = 0; i < 8; i++) {
            int tmp;
            do {
                tmp = (int) (Math.random() * 8);
                Log.d("123", "tmp= " + tmp);
            } while (ledsetornot[tmp] == true);
            gif[i].setMovieResource(ledDrawable[tmp]);
            ledsetornot[tmp] = true;
        }
        //set layout
        layout = (RelativeLayout) findViewById(R.id.container);

        //set gifumbrella
        gifUmbrella = (GifView) findViewById(R.id.gifumbrella);
        gifUmbrella.setMovieResource(R.drawable.umbrella);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mBluetoothAdapter.isEnabled() && mChatService == null)
            setupChat();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't
            // started already
            if (mChatService.getState() == BluetoothSerialService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void setupChat() {
        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothSerialService(this, mHandler);
    }

    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothSerialService.STATE_CONNECTED:
                            status.setText(R.string.title_connected_to);
                            status.append(mConnectedDeviceName);
                            GlobalVariables.connectOrNot = true;
                            break;
                        case BluetoothSerialService.STATE_CONNECTING:
                            status.setText(R.string.title_connecting);
                            break;
                        case BluetoothSerialService.STATE_LISTEN:
                        case BluetoothSerialService.STATE_NONE:
                            status.setText(R.string.title_not_connected);
                            GlobalVariables.connectOrNot = false;
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            R.string.connect_to + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.about_us:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.manager) + "\n" +
                                getString(R.string.music_team) + "\n" +
                                getString(R.string.bt_team) + "\n" +
                                getString(R.string.coaster_team) + "\n" +
                                getString(R.string.led_team) + "\n" +
                                getString(R.string.app_team)).setTitle(R.string.staff)
                        .setCancelable(false)
                        .setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        }).show();
                return true;
            // open bluetooth
            case R.id.openbluetooth:
                mBluetoothAdapter.enable();
                linkToHC05();
                Toast.makeText(this, R.string.btopen, Toast.LENGTH_SHORT).show();
                return true;
            //close bluetooth
            case R.id.quitbluetooth:
                // Ensure this device is discoverable by others
                mBluetoothAdapter.disable();
                Toast.makeText(this, R.string.btquit, Toast.LENGTH_SHORT).show();
                return true;
            //help
            case R.id.help:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage(getString(R.string.instruction)).setTitle(R.string.help)
                        .setCancelable(false).setNeutralButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                return true;
            //評分
            case R.id.rating:
                return true;
        }
        return false;
    }

    private void linkToHC05() {
        setProgressBarIndeterminateVisibility(true);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        // If we're already discovering, stop it
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getName() == DEVICENAME) {
                    mBluetoothAdapter.cancelDiscovery();
                    String addressStr = device.getAddress();
                    deviceAddress = addressStr.toString().substring(addressStr.length() - 17);
                    // Attempt to connect to the device
                    mChatService.connect(mBluetoothAdapter.getRemoteDevice(deviceAddress));
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getApplication(), "doesn't find", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
