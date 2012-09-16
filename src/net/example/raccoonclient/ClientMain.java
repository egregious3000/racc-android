package net.example.raccoonclient;

// Copyright 2012.  You may modify for your personal use.  You may not submit to any appstore.


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class ClientMain extends Service {

    
    ArrayList<Forum> _forumlist = new ArrayList<Forum>();
    
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

	// Returns success if we log in
	public boolean login() {
	    Log.w(TAG, "logging in");
	    try {
            _s = new Socket("bbs.iscabbs.com", 6145);
        } catch (UnknownHostException e) {
            Log.e(TAG, "unknown host", e);
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
        }
	    String[] r = readlines();
	    Log.e(TAG, "login got " + r[0]);
	    
	    // XXX send "LOGIN Username    Password\n" here
	    String[] lines = writeline("LIST TODO\n");
	    Log.e(TAG, "list is length " + lines.length);
        _forumlist.clear();
	    for (String line : lines) {
	        if (line.charAt(0) == '3')
	            continue;
	        if (line.charAt(0) == '.')
	            break;
	        Forum f = new Forum(line);
	        _forumlist.add(f);
	    }
	    return true;
	}

	public void logout() {
	    Log.w(TAG, "logging out");
	    if (_s == null)
	        return;
	    try {
	        writeline("QUIT\n");
	        _s.close();
        } catch (IOException e) {
            Log.e(TAG, "IO Exception on close", e);
        }
	    _s = null;
	}

	// returns index of \n or -1
	private int findCR(byte[] buffer, int start, int len) {
	    byte b = (byte) '\n';
	    for (int i = start; i < len; i++)
	        if (buffer[i] == b)
	            return i;
	    return -1;
	}

	
	
	// argh why is this so hard?
	private void bufferIntoLines(ArrayList<StringBuilder> lines, byte[] buffer, int len) {
	    int start = 0;
	    // i is just a limit in case we go nuts
	    for (int i = 0; i < 100; i++) {
	        int index = findCR(buffer, start, len);
    	    Log.e(TAG, "" + i + ":found CR starting at " + start + " at " + index + ", limit should be " + len);
    	    if (index == -1) {
    	        lines.get(lines.size() - 1).append(new String(buffer, start, len));
    	        return;
    	    }
            Log.e(TAG, "before.  Buffer length: " + buffer.length + ". Start is " + start + ".  Index is " + index + ".");
            String s = new String(buffer, start, index);
            Log.e(TAG, "Made String");
            Log.e(TAG, "Made String of " + s);
            lines.get(lines.size() - 1).append(s);
            Log.e(TAG, "after");
    	    start = index + 1;
            if (start >= len)
                return;
    	    StringBuilder sb = new StringBuilder("");
    	    lines.add(sb);
	    }
	}

	
	// If return starts with "3", returns multiple Strings.
	// Otherwise returns 1 String.
	// Each String is a line.
	// Promises to return at least 1 String.
	private String[] readlines() { 	    
	    assert (_s != null);
	    int state = 0; // 0 = new, 1 = singleline, 2 = multiline
	    ArrayList<StringBuilder> ret = new ArrayList<StringBuilder>();
	    ret.add(new StringBuilder(""));
	    try {
	        InputStream ins = _s.getInputStream();
	        byte[] buffer = new byte[500]; 
	        StringBuilder huge = new StringBuilder("");
	        while (true) {
	            int readlen = ins.read(buffer);
	            if (readlen == -1)
	                break;
	            
	            huge.append(new String(buffer, 0, readlen));
	            char last = huge.charAt(huge.length() - 1);
	            if (last == '\n') {
	                if (state == 0) {
	                    state = (huge.charAt(0) == '3' ? 2 : 1);
	                    Log.i(TAG, "set state to " + state);
	                }
	                if (state == 1) {
	                    // single line mode!
	                    Log.i(TAG, "single line done.");
	                    break;
	                }
	                if (huge.charAt(huge.length() - 2) == '.' && huge.charAt(huge.length() - 3) == '\n') {
	                    Log.i(TAG, "multiline done.");
	                    break;
	                }
	                    // xxx not if multiline mode
	            }
	            /*
	            Log.w(TAG, "read in " + readlen + " thingies.");
	            if (readlen == -1) {
	                if (state == 1)
	                    break;
	                Thread.sleep(100);
	                continue;
	            }
	            Log.w(TAG, "XXX:" + new String(buffer, 0, readlen, "UTF-8") + ":XXX");
	            // break into lines
                bufferIntoLines(ret, buffer, readlen);

                Log.e(TAG, "ALPHA length is " + ret.size());
                for (StringBuilder sb : ret) {
                    Log.e(TAG, "ALPHA line: " + sb.toString());
                }

                if (state == 0) {
                    Log.e(TAG, "length of ret is " + ret.size());
                    state = (ret.get(0).charAt(0) == '3') ? 2 : 1;
                }
                break; // ugh
*/
	        }
	        return huge.toString().split("\n");
        } catch (IOException e) {
            Log.e(TAG, "readline ioexception", e);
        } catch (Exception e) {
            Log.e(TAG, "Other exception", e);
        }
	    
	    String[] temp2 = { "" };
	    return temp2;
	    
	}

	public String[] writeline(String msg) {
        assert (_s != null);
	    OutputStream outs;
	    try {
            outs = _s.getOutputStream();
            byte[] send = msg.getBytes("UTF-8");
            outs.write(send);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 doesn't work, this is weird", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
        } catch (Exception e) {
            Log.e(TAG, "Other error", e);
        }
	    return readlines();
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
        _preferences = PreferenceManager.getDefaultSharedPreferences(ClientMain.this);
        _username = _preferences.getString("username", "Guest");
        _password = _preferences.getString("password", "");
        
        Log.e(TAG, "onCreate");		
		
	}
	
	public void mainLoop() {
		boolean waiting = false;
		if (waiting) return;
	}

	
}
