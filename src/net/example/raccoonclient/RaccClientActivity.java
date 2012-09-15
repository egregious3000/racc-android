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
            // Does nothing for now 
            _h.postDelayed(this, 1000);
        }
    }
    // End UI Loop Code

    Button _userpass, _login, _logout;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        doBindService(); // connect to _main
        Log.e(TAG, "starting");
        setContentView(R.layout.main);
        Button b = (Button) findViewById(R.id.userpass);
        b.setOnClickListener(_buttonhandler);
        _userpass = (Button) findViewById(R.id.userpass);
        _login  = (Button) findViewById(R.id.login);
        _logout = (Button) findViewById(R.id.logout);
        _userpass.setOnClickListener(_buttonhandler);
        _login.setOnClickListener(_buttonhandler);
        _logout.setOnClickListener(_buttonhandler);
        _logout.setEnabled(false);
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
                    if (_main.login()) {
                        _login.setEnabled(false);
                        _logout.setEnabled(true);   
                    }
                }   
                break;
	        case R.id.logout:
                if (_main == null) {
                    Log.e(TAG, "main is null :<");
                } else {
                    _main.logout();
                    _login.setEnabled(true);
                    _logout.setEnabled(false);
                }
                break;
	        }
	    }   
	}
 	

	
	public void onDestroy() {
	    _killed = true;
	    doUnbindService();
	    super.onDestroy();
	}




}
