package net.example.raccoonclient;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
            Log.e(TAG, "onservice disconnected");
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
    private class Post {
        String post;
        Integer mode;
        Post(String p, Integer m) { post = p; mode = m; } 
    }
    ArrayBlockingQueue<Post> _posts = new ArrayBlockingQueue<Post>(10);
    
    private class MainUILoop implements Runnable {
        @Override
        public void run() {
            if (_killed)
                return;
            Post post;
            // make a post if it's in our queue; I hope it's the right forum!
            if (_main != null) {
                if ((post = _posts.poll()) != null) {
                    _main.post(post.post, post.mode);
                }
            }
            _h.postDelayed(this, 1000);
        }
    }
    // End UI Loop Code

    TextView _usernametextfield, _message;
    int _messagesize = 14;
    Button _userpass, _login, _logout, _back, _post, _next;
//    ForumListAdapter _list;
//    ArrayAdapter<ClientMain.Thingy> _list;
    ClientMain.ThingyListAdapter _list;
    String _username, _password;

    // for logging in and out
    private class Dialer extends AsyncTask<String, String, Boolean> {
        @Override
        protected void onPostExecute(Boolean status)
        {
            super.onPostExecute(status);
            _login.setEnabled(!status);
            _logout.setEnabled(status);
            onResume();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params[0].equals("login")) {
                return _main.login();
            }
            if (params[0].equals("logout")) {
                _main.logout();
                return false;
            }
            return false;
        }
    };

    // for retrieving forums and messages
    private class Reader extends AsyncTask<Integer, String, Void> {
        String _mode;
        public Reader(String string) {
            _mode = string;
        }

        @Override
        protected void onPostExecute(Void _)
        {
            super.onPostExecute(_);
//            if (_mode.equals("message")) {
            _message.scrollTo(0, 0);
  //              Log.e(TAG, "SCROLLED");
  //          }
            onResume();
        }

        @Override
        protected Void doInBackground(Integer... numbers) {
            if (_mode.equals("message")) {
                _main.getMessage(numbers[0]);
                return null;
            }
            if (_mode.equals("forum")) {
                _main.changeToForum(numbers[0]);
                return null;
            }
            if (_mode.equals("next")) {
                _main.getNextMessage();
                return null;
            }
            return null;
        }
    };

    
    private Dialer _dialer;

    private ButtonHandler _buttonhandler = new ButtonHandler();
    private class ButtonHandler implements OnClickListener {
	    public void onClick(View src) {
	        Log.e(TAG, "view is " + src.getId());
	        switch (src.getId()) {
            case R.id.change:
                assert(_main != null);
                Intent i = new Intent(RaccClientActivity.this, UserPass.class);
                i.putExtra("username", _main._username);
                i.putExtra("password", _main._password);
                startActivityForResult(i, 127);
                break;
            case R.id.post:
                assert(_main != null);
                Intent i2 = new Intent(RaccClientActivity.this, WritePost.class);
                i2.putExtra("forumname", _main._forumname);
                startActivityForResult(i2, 129);
                break;
            case R.id.login:
                if (_main == null) {
                    Log.e(TAG, "main is null :<");
                } else {
                    _dialer = new Dialer();
                    _dialer.execute("login");
                }   
                break;
            case R.id.logout:
                if (_main == null) {
                    Log.e(TAG, "main is null :<");
                } else {
                    try {
                        _dialer = new Dialer();
                        _dialer.execute("logout");
                    } catch (Exception e) {
                        Log.e(TAG, "LOGOUT EXCEPTION", e);
                    }
                }
                break;
            case R.id.back:
                _main.back();
                onResume();
                break;
            case R.id.next:
                new Reader("next").execute();
                break;
            default:
                Log.e(TAG, "DEFAULT NOT FOUND");
	        }
	    }   
	}

	public void onRadioButtonClicked(View v) {
	    _main._forummode = v.getId();
	    _main.grab_forums();
	    onResume();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    Log.e(TAG, "result is here, " + requestCode + " with " + resultCode);
        if (requestCode == 127) {
            if (resultCode == Activity.RESULT_OK) {
                assert(_main != null);
                Bundle b = data.getExtras();
                _username = b.getString("username");
                _password = b.getString("password");
                _usernametextfield.setText(_username);
                _main.setNewProfile(_username, _password);
            }
        }
        if (requestCode == 129) {
            if (resultCode == Activity.RESULT_OK) {
                try { 
                    assert(_main != null);
                    Bundle b = data.getExtras();
                    String thepost = b.getString("thepost");
                    Integer id = b.getInt("anon");
                    Post post = new Post(thepost, id);
                    _posts.add(post);
                    Log.e(TAG, "sample");
                } catch (Exception e) {
                    Log.e(TAG, "exception receiving ", e);
                }
            }
        }
	}
	
    // Start lifecycle code
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	  super.onSaveInstanceState(savedInstanceState);
      savedInstanceState.putInt("messagesize",  _messagesize);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  _messagesize = savedInstanceState.getInt("messagesize");
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
	    // this lets us do network IO from the UI thread.  This is not a good overall design.
        StrictMode.enableDefaults();
	    super.onCreate(savedInstanceState);
        try {
            doBindService();
            Log.e(TAG, "starting");
            setContentView(R.layout.main);
            _usernametextfield = (TextView) findViewById(R.id.username);
            _userpass = (Button) findViewById(R.id.change);
            _login  = (Button) findViewById(R.id.login);
            _logout = (Button) findViewById(R.id.logout);
            _back = (Button) findViewById(R.id.back);
            _post = (Button) findViewById(R.id.post);
            _next = (Button) findViewById(R.id.next);
            _userpass.setOnClickListener(_buttonhandler);
            _login.setOnClickListener(_buttonhandler);
            _logout.setOnClickListener(_buttonhandler);
            _back.setOnClickListener(_buttonhandler);
            _post.setOnClickListener(_buttonhandler);
            _next.setOnClickListener(_buttonhandler);
            _logout.setEnabled(false);
            _message = (TextView) findViewById(R.id.message);
            _message.setMovementMethod(new ScrollingMovementMethod());
            //            _list = new ForumListAdapter(this, R.layout.item, new ArrayList<Forum>());
            _h.post(_looper);
        } catch (Exception e) {
            Log.e(TAG, "Creation Exception", e);
        }
    }
 	
	@SuppressWarnings("unchecked")
    public void onResume() {
	    super.onResume();
	    _message.setTextSize(_messagesize);
	    Log.w(TAG, "Resuming");
	    try { 
    	    if (_main != null) {
    	        switch (_main._state) {
    	        case INITIAL:
                    _login.setEnabled(true);
                    _logout.setEnabled(false);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    _next.setEnabled(false);
                    break;
    	        case LOGGING_IN:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    _next.setEnabled(false);
                    break;
    	        case FORUM_LIST:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    _next.setEnabled(false);
                    break;
    	        case MESSAGE_LIST:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(true);
                    _post.setEnabled(true);
                    _next.setEnabled(false); // <-- set this to start reading immediately
    	        case SHOW_POST:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(true);
                    _post.setEnabled(true);
                    _next.setEnabled(_main._cannext);
                    break;
    	        }
///    	        _list = new ForumListAdapter(this, R.layout.item, (ArrayList<Forum>) _main._forumlist.clone());
                _message.setText(_main._formattedpost);
                _list = _main.new ThingyListAdapter(this, R.layout.item, _main._currentlist);
    	        _usernametextfield.setText(_main._username);
    	        _list.notifyDataSetChanged();
            } else {
                Log.e(TAG, "main is null in onResume()");
            }
    	    ListView lv = (ListView)findViewById(R.id.forumlist);
    	    lv.setTextFilterEnabled(true);
    	    lv.setAdapter(_list);
            lv.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
  //                  Log.e(TAG, "position is " + position + ", id is " + id);
                    ClientMain.Listable t = _list.getItem(position);
//                    Log.e(TAG, "checking if " + _main._state + " equals " + ClientMain.State.FORUM_LIST);
                    if (_main._state == ClientMain.State.FORUM_LIST) {
                        new Reader("forum").execute(t.getNumber());
                    } else if (_main._state == ClientMain.State.MESSAGE_LIST) {
                        new Reader("message").execute(t.getNumber());
                    }
                }
              });

	    } catch (Exception e) {
	        Log.e(TAG, "resuming exception", e);
	    }
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) { 
            _messagesize--;
            onResume();
            return true;
	    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
	        _messagesize++;
            onResume();
            return true;
	    } else {
	        return super.onKeyDown(keyCode, event); 
	    }
	}
	
	// go back, unless we are at the top
	@Override
	public void onBackPressed() {
	    if (_main == null)
	        return;
	    switch (_main._state) {
	    case INITIAL:
	    case LOGGING_IN:
	        super.onBackPressed();
	        return;
        case FORUM_LIST:
            _dialer = new Dialer();
            _dialer.execute("logout");
            return;
	    }
	    _main.back();
	    onResume();
	}

	
	public void onDestroy() {
	    Log.e(TAG, "destroying!");
	    _killed = true;
	    doUnbindService();
	    super.onDestroy();
	}
    // End lifecycle code
}
