package net.example.raccoonclient;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class RaccClientActivity extends Activity {
    
    final private String TAG = "Main UI";

    // Start Interface Code
    private ClientMain _main;
    private boolean _isbound = false;

    private ServiceConnection _connection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.e(TAG, "onservice connected");
            _main = ((ClientMain.LocalBinder)service).getService();
        }
        public void onServiceDisconnected(ComponentName className) {
            _main = null;
        }
    };
    void doBindService() {
        Log.i(TAG, "bind");
        Intent intent = new Intent(this, ClientMain.class);
        startService(intent);
        bindService(intent, _connection, 0); // Context.BIND_AUTO_CREATE);
        _isbound = true;
    }
    void doUnbindService() {
        if (_isbound) {
            Log.i(TAG, "unbind");
            unbindService(_connection);
            _isbound = false;
        }
    }
    // End Interface Code

    // Start UI Loop Code
    private Handler _h = new Handler();
    MainUILoop _looper = new MainUILoop();
    boolean _killed = false;
    
    private class MainUILoop implements Runnable {
        @Override
        public void run() {
            if (_killed)
                return;
            ( (TextView) findViewById(R.id.login) ).setText(Long.toString(System.currentTimeMillis()));
            if (_main == null) {
                Log.e(TAG, "err: main is null");
            } else {
                ( (TextView) findViewById(R.id.logout) ).setText(_main._username);                
            }
            _h.postDelayed(this, 500);
        }
    }
    // End UI Loop Code
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService(); // connect to _main
        Log.e(TAG, "starting");
        setContentView(R.layout.main);
        Button b = (Button) findViewById(R.id.userpass);
        b.setOnClickListener(_buttonhandler);
        ( (Button) findViewById(R.id.userpass)).setOnClickListener(_buttonhandler);
        ( (Button) findViewById(R.id.login)).setOnClickListener(_buttonhandler);
        ( (Button) findViewById(R.id.logout)).setOnClickListener(_buttonhandler);
    	_h.post(_looper);
	}

	private ButtonHandler _buttonhandler = new ButtonHandler();
	private class ButtonHandler implements OnClickListener {
	    public void onClick(View src) {
	        switch (src.getId()) {
	        case R.id.userpass:
                Intent i = new Intent(RaccClientActivity.this, UserPass.class);
                startActivity(i);
                break;
	        case R.id.login:
	            if (_main == null) {
	                Log.e(TAG, "main is null :<");
	            } else {
	                _main.login();
	            }
	            break;
	        case R.id.logout:
	        }
	    }   
	}
 	

	
	public void onDestroy() {
	    _killed = true;
	    doUnbindService();
	    super.onDestroy();
	}




}
