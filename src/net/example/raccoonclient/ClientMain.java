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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ClientMain extends Service {
    
    abstract public class Thingy {
        abstract public String getHeader();
        abstract public int getNumber();
    }
 
    public class Forum extends Thingy {

        int _number;
        String _name = "x";
        int _lastnote;
        String _flags = "";
        int _todo;
        
//        topic:180       name:Stereo And Electronic Technology   lastnote:64104  flags:nosubject,sparse  admin:acct579504-oldisca/Copper Lethe/(hidden)  todo:1
//        topic:0   name:Lobby  lastnote:2379   flags:nosubject,sparse  admin:acct578247-oldisca/(Unknown ISCABBS User)/(hidden)
        Forum(String s) {
            String[] fields = s.split("\t");
            for (String f : fields) {
                String[] k = f.split(":");
                if (k[0].equals("topic"))
                    _number = Integer.parseInt(k[1]);
                if (k[0].equals("name"))
                    _name = k[1];
                if (k[0].equals("lastnote"))
                    _lastnote = Integer.parseInt(k[1]);
                if (k[0].equals("flags"))
                    _flags = k[1];
                if (k[0].equals("todo"))
                    _todo = Integer.parseInt(k[1]);
            }
        }
        public int getNumber() { return _number; }
        public String getHeader() {
            if (_todo == 0)
                return _name;
            else
                return (_name + " (" + _todo + ")"); 
        }
    }

    public class Message extends Thingy {

        int _number;
        String _subject = "y";
        
        // noteno:201642   subject:Fucking hackers! If I could ever find you, I would see that     size:115

        // noteno:78049    formal-author:acct550746-oldisca/Danix/(hidden) date:Tue, 18 Sep 2012 00:42:00 GMT      subject:Feoh> heh, yes you have to back up your zone files. I had       size:192

        Message(String s) {
            String[] fields = s.split("\t");
            for (String f : fields) {
                String[] k = f.split(":");
                if (k[0].equals("noteno"))
                    _number = Integer.parseInt(k[1]);
                if (k[0].equals("subject"))
                    _subject = k[1];
            }
        }

        public int getNumber() { return _number; }
        public String getHeader() { return _subject; }
    }

//    _list = new ArrayAdapter(this, R.layout.item, _main._currentlist);
    abstract class ThingyList extends ArrayList<Thingy> {};
    class ForumList extends ThingyList { };
    class MessageList extends ThingyList { };
    
    //     public ForumListAdapter(Context context, int resource, ArrayList<Forum> objects) {    

    public class ThingyListAdapter extends ArrayAdapter<Thingy> {
        ThingyListAdapter(Context c, int resource, ArrayList<Thingy> objects) {
            super(c, resource, objects);
            _context = c;
            _things = (ArrayList<Thingy>) objects.clone();
        }
        private ArrayList<Thingy> _things;
        private Context _context;
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) _context.getSystemService
                        (Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.item, null);
            }
            try {
                Thingy t = _things.get(position);
                TextView tv = (TextView) v;
                tv.setText(t.getHeader());
            } catch (Exception e) {
                Log.e(TAG, "getview got exception: ", e);
            }
            return v;
        }
    }

//    public class ForumListAdapter extends ThingyListAdapter { }
 //   public class MessageListAdapter extends ThingyListAdapter { }
    
    
    ForumList _forumlist = new ForumList();
    MessageList _messagelist = new MessageList();
    ThingyList _currentlist = _forumlist;
    String[] _post = { };
    MessageList _emptylist = new MessageList();
    String[] _emptypost = { };

    
    public boolean post(String s) {
        String[] lines = writeline("OKAY POST\n", 1);
        Log.e(TAG, "count of lines is " + lines.length);
        for (String line : lines)
            Log.e(TAG, "line is " + line);
        if (lines[0].charAt(0) == '4') {
            Log.e(TAG, "error with posting: " + lines[0]);
            return false;
        }
        writeline("POST\n", 1);
        String[] test = writeline(s + "\n.\n");
        for (String t : test) 
            Log.e(TAG, "post returned " + t);
        return true;
    }

    public boolean back() {
        Log.e(TAG, "state is " + _state + " and messagelist size is " + _messagelist.size());
        if (_state == State.SHOW_POST) {
            _state = State.MESSAGE_LIST;
            _post = _emptypost;
            _currentlist = _messagelist;
            return true;
        }
        if (_state == State.MESSAGE_LIST) {
            _state = State.FORUM_LIST;
            _currentlist = _forumlist;
            return true;
        }
        return true;
    }
    
    public boolean getMessage(int i) {
        assert (_state == State.MESSAGE_LIST);
        _state = State.SHOW_POST;
        String[] lines = writeline("READ " + i + "\n");
        _post = lines;
        _currentlist = _emptylist;
        return true;
    }
    
    public boolean changeToForum(int i) {
        assert (_state == State.FORUM_LIST);
        _state = State.MESSAGE_LIST;
        Log.e(TAG, "switching to forum " + i);
        String[] lines2 = writeline("TOPIC " + i + "\n");
        String[] fields = lines2[0].split("\\t");
        String lastnote = "";
        for (String s: fields) {
            if (s.startsWith("lastnote")) {
                s = s.substring(9);
            }
            if (s.startsWith("name")) {
                _forumname = s.substring(5);
            }
        }
        String firstnote = "";
        if (_forummode == R.id.radio_unread) {
            lines2 = writeline("SHOW rcval\n");
            fields = lines2[0].split("\\t");
            firstnote = (fields.length > 1) ?  fields[1] : "";
        }
        String[] lines = writeline("XHDR subject " + firstnote + "-" + lastnote + "\n");
        Log.e(TAG, "list is length " + lines.length);
        _messagelist.clear();
        for (String line : lines) {
            if (line.charAt(0) == '3')
                continue;
            if (line.charAt(0) == '.')
                break;
            Message m = new Message(line);
            _messagelist.add(m);
        }
        _currentlist = _messagelist;
        return true;
    }
    
	SharedPreferences _preferences;
	public static enum State { INITIAL, LOGGING_IN, FORUM_LIST, MESSAGE_LIST, SHOW_POST };
	private static final String TAG = "Client Main";
	public State _state = State.INITIAL;  // readable by code
	public String _status = "Not connected"; // readable by humans
	public int _forumnumber = 0;
	public String _forumname = "Lobby";
	
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

	public int _forummode = R.id.radio_unread;
	
	// Returns success if we log in
	public boolean login() {
        _state = State.LOGGING_IN;
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
	
	    r = writeline("LOGIN\t" + _username + "\t" + _password + "\n");
	    for (String s : r) {
	        Log.e(TAG, "line is " + s);
	    }
        _state = State.FORUM_LIST;
	    return grab_forums();
	}
	
	public boolean grab_forums() {
	    if (_state != State.FORUM_LIST) 
	        return false;
	    if (_s == null) {
	        Log.e(TAG, "trying to load forums when socket is null");
	        return false;
	    }
	    // XXX send "LOGIN Username    Password\n" here
	    String mode = "";
	    switch (_forummode) {
        case R.id.radio_unread:    mode = "TODO"; break;    
        case R.id.radio_joined:    mode = "JOINED"; break;
        case R.id.radio_all:       mode = "ALL"; break;
	    }
	    String[] lines = writeline("LIST " + mode + "\n");
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
        _state = State.FORUM_LIST;
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
	    _state = State.INITIAL;

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
	private String[] readlines() { return readlines(null); }
	private String[] readlines(Integer mode) {
	    assert (_s != null);
	    int state = 0; // 0 = new, 1 = singleline, 2 = multiline
	    if (mode != null)
	        state = mode;
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

	private String[] writeline(String msg) { return writeline(msg, null); }
    private String[] writeline(String msg, Integer mode) {
        if (_s == null) return _emptypost;
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
	    return readlines(mode);
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
	

	
	public void obsolete_mainLoop() {
		boolean waiting = false;
		if (waiting) return;
	}

	
}
