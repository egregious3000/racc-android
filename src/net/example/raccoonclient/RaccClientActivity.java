package net.example.raccoonclient;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RaccClientActivity extends Activity {
    

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button b = (Button) findViewById(R.id.userpass);
    	b.setOnClickListener(
    			new View.OnClickListener() {
    				@Override
					public void onClick(View v) {
						Intent i = new Intent(RaccClientActivity.this, UserPass.class);
			    		startActivity(i);
					}
        		}
    			);
	}
}
