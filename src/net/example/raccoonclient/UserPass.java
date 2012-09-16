package net.example.raccoonclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class UserPass extends Activity {

	private final String TAG = "USERPASS";
	private EditText _username;
	private EditText _password;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userpass);        
        try {
        	_username = (EditText) findViewById(R.id.username);
        	_password = (EditText) findViewById(R.id.password);
        	Intent i = this.getIntent();
        	Bundle b = i.getExtras();
//        	_username.setText(b.getString("username"));
//        	_password.setText(b.getString("password"));
        } catch (Exception e) {
        	Log.e(TAG, "Exception: " + e);
        }
        Button b = (Button) findViewById(R.id.done);
        b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("username", _username.getText()
                                .toString());
                        resultIntent.putExtra("password", _password.getText()
                                .toString());
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
    }
    
}
