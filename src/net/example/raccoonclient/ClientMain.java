package net.example.raccoonclient;

// Copyright 2012.  You may modify for your personal use.  You may not submit to any appstore.


import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ClientMain extends Service {

	SharedPreferences _preferences;
	public static enum State { INITIAL, LOGGING_IN, CONNECTED };
	private static final String TAG = "Client Main";
	public State _state = State.INITIAL;  // readable by code
	public String _status = "Not connected"; // readable by humans
	
	
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
	
	private Socket _s = null;

	
	public void login() {
	    Log.w(TAG, "logging in");
	    try {
            _s = new Socket("bbs.iscabbs.com", 6145);
        } catch (UnknownHostException e) {
            Log.e(TAG, "unknown host", e);
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
        }
	    String r = readline();
	    Log.e(TAG, "login got " + r);
	}

	private String readline() { 
	    try {
    	    InputStream ins = _s.getInputStream();
    	    byte[] buffer = new byte[75000]; 
    	    int readlen = ins.read(buffer);
            Log.w(TAG, "read in " + readlen + " thingies.");
//            return buffer.toString();
            return new String(buffer, 0, readlen, "UTF-8");
	    } catch (IOException e) {
            Log.e(TAG, "readline ioexception", e);
        }
	    return "";
	}

	public String writeline(String msg) {
	    
	    OutputStream outs;
	    try {
            outs = _s.getOutputStream();
            byte[] send = msg.getBytes("UTF-8");
            outs.write(send);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
	    return readline();
	}

	
	// Begin Binder code
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
	// End Binder code
	
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
