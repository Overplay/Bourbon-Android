package tv.ourglass.alyssa.bourbon_android.Scenes.Registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.Input.InputType;
import tv.ourglass.alyssa.bourbon_android.Model.Input.TextFocusChangeListener;
import tv.ourglass.alyssa.bourbon_android.Networking.HttpCallback;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud2;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

public class LoginActivity extends RegistrationBaseActivity {

    private final String TAG = "LoginActivity";

    private EditText mEmail;
    private EditText mPassword;

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Answers.getInstance().logContentView(new ContentViewEvent()
                .putContentName("Login Screen")
                .putContentType("Activity"));

        mEmail = (EditText) findViewById(R.id.email);
        mEmail.setOnFocusChangeListener(TextFocusChangeListener.newInstance(mEmail, InputType.EMAIL));

        mPassword = (EditText) findViewById(R.id.password);
        mPassword.setOnFocusChangeListener(TextFocusChangeListener.newInstance(mPassword, InputType.PASSWORD));
    }

    public void login(View view) {
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();
        progress = ProgressDialog.show(this, getString(R.string.loggingIn), "", true);

        boolean emailValid = true, pwdValid = true;

        if (!isValidEmail(email)) {
            emailValid = false;
            mEmail.setError(getString(R.string.email_not_valid));
        }

        if (!isValidPassword(password)) {
            pwdValid = false;
            mPassword.setError(getString(R.string.pwd_not_valid));
        }

        if (emailValid && pwdValid) {
            OGCloud2.login(this, email, password,
                    new HttpCallback() {

                        @Override
                        public void onSuccess(Response response) {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(LoginActivity.this, MainTabsActivity.class);
                                    startActivity(intent);
                                    overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                                    Log.d(TAG, "login success");
                                }
                            });
                            response.body().close();
                        }

                        @Override
                        public void onFailure(Call call, final IOException e, OGCloud2.OGCloudError error) {
                            LoginActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progress.dismiss();
                                    showAlert(getString(R.string.uhoh), getString(R.string.loginErrorAlertMsg));
                                    if (e != null) {
                                        Log.d(TAG, e.getLocalizedMessage());
                                    }
                                }
                            });
                        }
                    });
        } else {
            if (emailValid && !pwdValid) {
                mPassword.requestFocus();
            } else {
                mEmail.requestFocus();
            }
            progress.dismiss();
        }
    }
}
