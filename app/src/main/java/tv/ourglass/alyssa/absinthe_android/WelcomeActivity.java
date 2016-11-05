package tv.ourglass.alyssa.absinthe_android;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        TextView text = (TextView)findViewById(R.id.welcomeTextView);
        Typeface font = Typeface.createFromAsset(getAssets(), "Poppins-Bold.ttf");
        if (text != null) {
            text.setTypeface(font);
        }

        text = (TextView)findViewById(R.id.textView1);
        font = Typeface.createFromAsset(getAssets(), "Poppins-Regular.ttf");
        if (text != null) {
            text.setTypeface(font);
        }

        text = (TextView)findViewById(R.id.textView2);
        if (text != null) {
            text.setTypeface(font);
        }

        Button btn = (Button)findViewById(R.id.loginButton);
        if (btn != null) {
            btn.setTypeface(font);
        }

        btn = (Button)findViewById(R.id.signupButton);
        if (btn != null) {
            btn.setTypeface(font);
        }
    }

    public void login(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void signup(View view) {
        Intent intent = new Intent(this, EnterNameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }
}
