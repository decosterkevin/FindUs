package decoster.findus.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import decoster.findus.R;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {


    private EditText mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserId = (EditText) findViewById(R.id.userIDEdit);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = mUserId.getText().toString();
                if (input.matches("")) {
                    Toast.makeText(LoginActivity.this, "Please enter a user ID", Toast.LENGTH_SHORT).show();
                } else {
                    editor.putString("userID", input);
                    editor.commit();
                    Intent myIntent = new Intent(LoginActivity.this, MapsActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                }

            }
        });

    }


}

