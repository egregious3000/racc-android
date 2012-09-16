package net.example.raccoonclient;

import java.util.ArrayList;


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
import android.widget.ListView;
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
            onResume();
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
    ForumListAdapter _list;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            doBindService(); // connect to _main
            Log.e(TAG, "starting");
            setContentView(R.layout.main);
            _userpass = (Button) findViewById(R.id.change);
            _login  = (Button) findViewById(R.id.login);
            _logout = (Button) findViewById(R.id.logout);
            _userpass.setOnClickListener(_buttonhandler);
            _login.setOnClickListener(_buttonhandler);
            _logout.setOnClickListener(_buttonhandler);
            _logout.setEnabled(false);
            _list = new ForumListAdapter(this, R.layout.item, new ArrayList<Forum>());
    
            _h.post(_looper);
        } catch (Exception e) {
            Log.e(TAG, "Creation Exception", e);
        }
        
    }

	private ButtonHandler _buttonhandler = new ButtonHandler();
	private class ButtonHandler implements OnClickListener {
	    public void onClick(View src) {
	        switch (src.getId()) {
	        case R.id.change:
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
 	
	public void onResume() {
	    super.onResume();
	    Log.e(TAG, "Resuming");
	    try { 
    	    if (_main != null) {
    	        _list = new ForumListAdapter(this, R.layout.item, (ArrayList<Forum>) _main._forumlist.clone());
    	        Log.e(TAG, "remade forum list adapater");
    	        Log.e(TAG, "list size is " + _main._forumlist.size());
    	    }
    	    _list.notifyDataSetChanged();
    	    ListView lv = (ListView)findViewById(R.id.forumlist);
    	    lv.setTextFilterEnabled(true);
    	    lv.setAdapter(_list);
	    } catch (Exception e) {
	        Log.e(TAG, "resuming exception", e);
	    }
	}
	
	public void onDestroy() {
	    _killed = true;
	    doUnbindService();
	    super.onDestroy();
	}




}
