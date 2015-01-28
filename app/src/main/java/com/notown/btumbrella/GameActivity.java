package com.notown.btumbrella;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class GameActivity extends ActionBarActivity {

    private static final String TAG = "BluetoothChat";

    private TextView txt[] = new TextView[6];
    private LinearLayout layout;
    private int layoutWidth;
    private float areaWidth;
    private int txtID[] = {R.id.textView1, R.id.textView2, R.id.textView3, R.id.textView4, R.id.textView5, R.id.textView6};
    //TextView background color (not pressed)
    private int txtColor[] = {0xFFFF0000, 0xFFFF8000, 0xFFFFFF00, 0xFF00FF00, 0xFF0000FF, 0xFF8000FF};
    //TextView background color (pressed)
    private int txtDarkColor[] = {0xFFCE0000, 0xFFCE6700, 0xFFCECE00, 0xFF00CE00, 0xFF0000B4, 0xFF5A00B4};
    //TextView pressed or not
    private boolean wchAreaSlt[] = {false, false, false, false, false, false};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_game);
        setUpViewComponent();
        layout.post(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                layoutWidth = layout.getWidth();
                areaWidth = ((float) layoutWidth) / 6;
            }
        });
    }

    private void setUpViewComponent() {
        // TODO Auto-generated method stub
        for (int i = 0; i < 6; i++)
            txt[i] = (TextView) findViewById(txtID[i]);
        layout = (LinearLayout) findViewById(R.id.container);
        layout.setOnTouchListener(layoutTouchListener);

    }

    OnTouchListener layoutTouchListener = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            if (GlobalVariables.connectOrNot == false) {
                Toast.makeText(GameActivity.this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
                return true;
            }
            int pointCount = event.getPointerCount();
            int wchArea;
            //reset txt[] and wchAreaSlt[]
            for (int i = 0; i < 6; i++)
                txt[i].setBackgroundColor(txtColor[i]);
            for (int i = 0; i < 6; i++)
                wchAreaSlt[i] = false;
            //find which area was pressed
            for (int i = 0; i < pointCount; i++) {
                wchArea = (int) (event.getX(i) / areaWidth);
                txt[wchArea].setBackgroundColor(txtDarkColor[wchArea]);
                wchAreaSlt[wchArea] = true;
            }
            for (int i = 0; i < 6; i++)
                if (wchAreaSlt[i])
                    MainActivity.mChatService.write((i + "").getBytes());
            if (event.getAction() == MotionEvent.ACTION_UP)
                for (int i = 0; i < 6; i++)
                    txt[i].setBackgroundColor(txtColor[i]);
            return true;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.quit:
                finish();
                return true;
        }
        return false;
    }

}
