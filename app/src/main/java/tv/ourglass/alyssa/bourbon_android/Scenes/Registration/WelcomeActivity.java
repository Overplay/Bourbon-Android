package tv.ourglass.alyssa.bourbon_android.Scenes.Registration;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import tv.ourglass.alyssa.bourbon_android.R;

public class WelcomeActivity extends RegistrationBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Welcome Screen")
                .putContentType("Activity"));

        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        findViewById(R.id.signupButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signup();
            }
        });

        findViewById(R.id.textViewForgot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passwordReset();
            }
        });
    }

    @Override
    public void onBackPressed() {
        // make Android back button do nothing
    }

    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    public void signup() {
        Intent intent = new Intent(this, EnterNameActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }

    private void passwordReset(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cloud.ourglass.tv/login"));
        startActivity(browserIntent);
    }
}
