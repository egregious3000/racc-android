package net.example.raccoonclient;

// Copyright 2012.  You may modify for your personal use.  You may not submit to any appstore.


import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;


public class ClientMain extends Service {

	SharedPreferences _preferences;
	public static enum State { INITIAL, LOGGING_IN, CONNECTED };
	private static final String TAG = "Client Main";

	public String _username = "default1";
	public String _password = "default2";
	
	public void setNewProfile(String username, String password) {
		Log.e(TAG, "setting new profile now");
		_username = username;
		_password = password;
	
		SharedPreferences.Editor editor = _preferences.edit();
		editor.putString("username", username);
		editor.putString("password", password);
		editor.commit();
	}
	
	public void login() {
	    // magic goes here
	}
	
    public class LocalBinder extends Binder {
        ClientMain getService() {
            Log.e(TAG, "getting service");
            return ClientMain.this;
        }
    }
	LocalBinder _binder = new LocalBinder();
    
	@Override
	public IBinder onBind(Intent arg0) {
		Log.w(TAG, "onBind");
		return _binder;
	}

    @Override
    public void onStart(Intent intent, int startid) {
        Log.e(TAG, "onStart: " + startid);
    }
	
	@Override
	public void onCreate() {
	    _username = "default3";
	    _password = "default4";
		Log.e(TAG, "onCreate");		
		
	}
	
	public void mainLoop() {
		boolean waiting = false;
		if (waiting) return;
	}

	
}
