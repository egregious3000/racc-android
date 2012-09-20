package net.example.raccoonclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class WritePost extends Activity {

    private final String TAG = "WRITEPOST";
    private EditText _thepost;
    private TextView _header;
    private Button _post;
    
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
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e);
        }
        _post = (Button) findViewById(R.id.post);
        _post.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("thepost", _thepost.getText().toString());
                        setResult(Activity.RESULT_OK, resultIntent);
                        finish();
                    }
                });
    }
}
