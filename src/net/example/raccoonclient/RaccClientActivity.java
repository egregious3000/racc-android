package net.example.raccoonclient;

import java.util.concurrent.ArrayBlockingQueue;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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
    ArrayBlockingQueue<String> _posts = new ArrayBlockingQueue<String>(10);
    
    private class MainUILoop implements Runnable {
        @Override
        public void run() {
            if (_killed)
                return;
            String _s;
            // make a post if it's in our queue; I hope it's the right forum!
            if (_main != null) {
                if ((_s = _posts.poll()) != null) {
                    _main.post(_s);
                }
            }
            _h.postDelayed(this, 1000);
        }
    }
    // End UI Loop Code

    TextView _usernametextfield, _message;
    Button _userpass, _login, _logout, _back, _post;
//    ForumListAdapter _list;
//    ArrayAdapter<ClientMain.Thingy> _list;
    ClientMain.ThingyListAdapter _list;
    String _username, _password;
    
	private ButtonHandler _buttonhandler = new ButtonHandler();
	private class ButtonHandler implements OnClickListener {
	    public void onClick(View src) {
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
                    if (_main.login()) {
                        _login.setEnabled(false);
                        _logout.setEnabled(true);   
                        onResume();
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
            case R.id.back:
                _main.back();
                onResume();
                break;
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
                    Log.e(TAG, "the post is " + ( (thepost == null) ? "NULL" : thepost));
                    Log.e(TAG, "the main is " + ( (_main == null) ? "NULL" : "not null"));
                    _posts.add(thepost);
//                    _main.post("dead code");
                    Log.e(TAG, "sample");
                } catch (Exception e) {
                    Log.e(TAG, "exception receiving ", e);
                }
            }
        }
	}
	
    // Start lifecycle code
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
            _userpass.setOnClickListener(_buttonhandler);
            _login.setOnClickListener(_buttonhandler);
            _logout.setOnClickListener(_buttonhandler);
            _back.setOnClickListener(_buttonhandler);
            _post.setOnClickListener(_buttonhandler);
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
	    Log.w(TAG, "Resuming");
	    try { 
    	    if (_main != null) {
    	        switch (_main._state) {
    	        case INITIAL:
                    _login.setEnabled(true);
                    _logout.setEnabled(false);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    break;
    	        case LOGGING_IN:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    break;
    	        case FORUM_LIST:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(false);
                    _post.setEnabled(false);
                    break;
    	        case MESSAGE_LIST:
    	        case SHOW_POST:
                    _login.setEnabled(false);
                    _logout.setEnabled(true);
                    _back.setEnabled(true);
                    _post.setEnabled(true);
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
                    ClientMain.Thingy t = _list.getItem(position);
//                    Log.e(TAG, "checking if " + _main._state + " equals " + ClientMain.State.FORUM_LIST);
                    if (_main._state == ClientMain.State.FORUM_LIST) {
                        _main.changeToForum(t.getNumber());
                        onResume();
                    } else if (_main._state == ClientMain.State.MESSAGE_LIST) {
                        _main.getMessage(t.getNumber());
                        onResume();
                    }
                    //                    Intent i = new Intent(RaccClientActivity.this, ForumActivity.class);
//                    i.putExtra("forumnumber", f._number);
//                    startActivity(i);       
                }
              });

	    } catch (Exception e) {
	        Log.e(TAG, "resuming exception", e);
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
            _main.logout();
            onResume();
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
