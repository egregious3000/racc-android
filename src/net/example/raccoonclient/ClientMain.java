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
import android.graphics.Typeface;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ClientMain extends Service {
    
    abstract public class Listable {
        abstract public SpannableString getHeader();
        abstract public int getNumber();
    }
 
    public class Forum extends Listable {

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
        public SpannableString getHeader() {
            if (_todo == 0)
                return new SpannableString(_name);
            SpannableString ss = new SpannableString(_name + " (" + _todo + ")");
            ss.setSpan(new ForegroundColorSpan(0xFF00FF00), _name.length(), ss.length(), 0);   
            return ss;
        }
    }
    
    public class Message extends Listable {

        int _number;
        String _subject = "y";
        String _author = "";
        
        // noteno:201642   subject:Fucking hackers! If I could ever find you, I would see that     size:115
        // noteno:78049    formal-author:acct550746-oldisca/Danix/(hidden) date:Tue, 18 Sep 2012 00:42:00 GMT      subject:Feoh> heh, yes you have to back up your zone files. I had       size:192

        Message(String s) {
            String[] fields = s.split("\t");
            for (String f : fields) {
                if (f.startsWith("noteno"))
                    _number = Integer.parseInt(f.substring(7));
                if (f.startsWith("subject"))
                    _subject = f.substring(8);
                if (f.startsWith("formal-author")) {
                    _author = getUserName(f.substring(14));
                }
            }
        }
        public int getNumber() { return _number; }
        public SpannableString getHeader() { 
            if (_author.length() == 0)
                return new SpannableString(_subject);
            SpannableString ss = new SpannableString("(" + _author + ") " + _subject);
            ss.setSpan(new ForegroundColorSpan(0xFF00FFFF), 0, _author.length() + 2, 0);   
            return ss;
        }
    }

    // Find username, or user # if no name.
    // Todo: use database of old users for expired users.
    private String getUserName(String s) {
        String authordata[] = s.split("/");
        assert (authordata.length == 3);
        if (authordata[1].equals("(Unknown ISCABBS User)"))
            return authordata[0];
        return authordata[1];
    }

    SpannableStringBuilder formatMessage(String[] lines) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        boolean inheader = true;
        String author = "";
        String date = "";
        String to = "";
        
        for (String line : lines) {
            if (inheader) {
                if (line.length() == 0) {
                    inheader = false;
                    if (author.length() == 0) {
                        ssb.append(" -anonymous- ");
                        ssb.setSpan(new ForegroundColorSpan(0xFFFFFF00), 1, 12, 0);
                        ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                    } else {
                        ssb.append(date);
                        ssb.setSpan(new ForegroundColorSpan(0xFFFF00FF), 0, date.length(), 0);
                        ssb.append(" from ");
                        ssb.append(author);
                        ssb.setSpan(new ForegroundColorSpan(0xFF00FFFF), date.length() + 6, date.length() + author.length() + 6, 0);
                        if (to.length() != 0) {
                            ssb.append(" to ");
                            int len = ssb.length();
                            ssb.append(to);
                            ssb.setSpan(new ForegroundColorSpan(0xFF00FFFF), len, len + to.length(), 0);
                        }
                    }
                    ssb.setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(), 0);
                    ssb.append("\n\n");
                    continue;
                }
                if (line.startsWith("Formal-Name")) {
                    author = getUserName(line.substring(13));
                } else if (line.startsWith("Date")) {
                    // Date: Thu, 06 Nov 2008 21:13:00 GMT
                    date = line.substring(11,27) + line.substring(30);
                } else if (line.startsWith("Formal-To")) {
                    to = getUserName(line.substring(11));
                }
                continue;
            }
            if (! line.equals(".")) {
                ssb.append(line);
                ssb.append("\n");
            }   
        }   
        return ssb;
    }


    
    abstract class ListableList extends ArrayList<Listable> {};
    class ForumList extends ListableList { };
    class MessageList extends ListableList { };
    
    public class ThingyListAdapter extends ArrayAdapter<Listable> {
        @SuppressWarnings("unchecked")
        ThingyListAdapter(Context c, int resource, ArrayList<Listable> objects) {
            super(c, resource, objects);
            _context = c;
            _things = (ArrayList<Listable>) objects.clone();
        }
        private ArrayList<Listable> _things;
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
                Listable t = _things.get(position);
                TextView tv = (TextView) v;
                tv.setText(t.getHeader());
            } catch (Exception e) {
                Log.e(TAG, "getview got exception: ", e);
            }
            return v;
        }
    }
    
    ForumList _forumlist = new ForumList();
    MessageList _messagelist = new MessageList();
    ListableList _currentlist = _forumlist;
    String[] _post = { };
    SpannableStringBuilder _formattedpost = null;
    MessageList _emptylist = new MessageList();
    String[] _emptypost = { };

    public boolean post(String s, Integer mode) {
        Log.d(TAG, "posting something");
        Log.v(TAG, "posting " + s);
        String[] lines = writeline("OKAY POST\n", 1);
        if (lines.length == 0 || lines[0].charAt(0) == '4') {
            Log.e(TAG, "error with posting: " + lines[0]);
            return false;
        }
        if (lines[0].charAt(0) != '2') {
            Log.e(TAG, "PROTOCOL ERROR.  We are mis-aligned. " +  lines[0]);
        }
        String flags = (mode == R.id.postanonymous) ? " flags:anonymous" : "";
        writeline("POST" + flags + "\n", 1);
        String[] test = writeline(s + "\n.\n");
        for (String t : test) 
            Log.e(TAG, "post returned " + t);
        return true;
    }

    public boolean back() {
        Log.d(TAG, "state is " + _state + " and messagelist size is " + _messagelist.size());
        if (_state == State.SHOW_POST) {
            _state = State.MESSAGE_LIST;
            _post = _emptypost;
            _formattedpost = null;
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
        _formattedpost = formatMessage(lines);
        _currentlist = _emptylist;
        return true;
    }
    
    public boolean changeToForum(int i) {
        assert (_state == State.FORUM_LIST);
        _state = State.MESSAGE_LIST;
        Log.d(TAG, "switching to forum " + i);
        String[] lines2 = writeline("TOPIC " + i + "\n");
        if (lines2.length == 0) return false;
        String[] fields = lines2[0].split("\\t");
        String lastnote = "";
        for (String s: fields) {
            if (s.startsWith("lastnote")) {
                lastnote = s.substring(9);
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
            return false;
        } catch (IOException e) {
            Log.e(TAG, "io exception", e);
            return false;
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
        _state = State.INITIAL;
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
                bufferIntoLines(ret, buffer, readlen);
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
	    return _emptypost;
	}

	private String[] writeline(String msg) { return writeline(msg, null); }
    private String[] writeline(String msg, Integer mode) {
        if (_s == null) return _emptypost;
        OutputStream outs;
	    try {
            outs = _s.getOutputStream();
            byte[] send = msg.getBytes("UTF-8");
            outs.write(send);
            Log.w(TAG, "SENT: " + msg);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UTF-8 doesn't work, this is weird", e);
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
            _s = null;
            // I need convenience functions for switching to a given state
            _state = State.INITIAL;
            _currentlist = _forumlist;
            return _emptypost;
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
