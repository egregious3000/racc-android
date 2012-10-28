package net.example.raccoonclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WritePost extends Activity {

    private final String TAG = "WRITEPOST";
    private EditText _thepost;
    private TextView _header;
    private Button _post, _postanonymous;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.writepost);
        try {
            _thepost = (EditText) findViewById(R.id.postText);
            _header = (TextView) findViewById(R.id.header);
            Intent i = this.getIntent();
            Bundle b = i.getExtras();
            _header.setText("Posting to " + b.getString("forumname"));
            _post = (Button) findViewById(R.id.post);
            _postanonymous = (Button) findViewById(R.id.postanonymous);
            _post.setOnClickListener(_buttonhandler);
            if (false) // XXX find if we can post anon.  Or have to.
                _postanonymous.setVisibility(View.INVISIBLE);
            _postanonymous.setOnClickListener(_buttonhandler);
        } catch (Exception e) {
            Log.e(TAG, "Exception: ", e);
        }   

    }   

    private ButtonHandler _buttonhandler = new ButtonHandler();
    private class ButtonHandler implements OnClickListener {
        public void onClick(View v) {
            try {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("thepost", _thepost.getText().toString());
                resultIntent.putExtra("anon", v.getId());
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } catch (Exception e) {
                Log.e(TAG, "exception sending:", e);
            }
        }
    }
}