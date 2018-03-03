package tv.ourglass.alyssa.bourbon_android.Scenes.Registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.Input.InputType;
import tv.ourglass.alyssa.bourbon_android.Model.Input.TextFocusChangeListener;
import tv.ourglass.alyssa.bourbon_android.Model.Input.TextValidator;
import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;
import tv.ourglass.alyssa.bourbon_android.Networking.HttpCallback;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud2;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

public class EnterPasswordActivity extends RegistrationBaseActivity {

    public String TAG = "EnterPasswordActivity";

    EditText mPassword;

    ImageButton mNextButton;

    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_password);

        mNextButton = (ImageButton) findViewById(R.id.nextButton);

        mPassword = (EditText)findViewById(R.id.password);
        mPassword.setOnFocusChangeListener(TextFocusChangeListener.newInstance(mPassword, InputType.PASSWORD));

        // Add text change listeners
        mPassword.addTextChangedListener(new TextValidator(mPassword) {
            @Override
            public void validate(TextView textView, String text) {
                if (isValidPassword(text)) {
                    mNextButton.animate().alpha(1f).setDuration(OGConstants.fadeInTime).start();
                } else {
                    mNextButton.animate().alpha(0f).setDuration(OGConstants.fadeOutTime).start();

                }
            }
        });

        mPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    if (!isValidPassword(mPassword.getText().toString())) {
                        mPassword.setError(getString(R.string.pwd_not_valid));
                    }
                    return true;
                }
                return false;
            }
        });
    }

    public void next(View view) {
        Intent prev = getIntent();

        progress = ProgressDialog.show(this, "Signing up...", "", true);

        String email = prev.getStringExtra(OGConstants.emailExtra);
        String firstName = prev.getStringExtra(OGConstants.firstNameExtra);
        String lastName = prev.getStringExtra(OGConstants.lastNameExtra);
        String password = mPassword.getText().toString();

        if (!isValidPassword(password)) {
            progress.dismiss();
            mPassword.setError(getString(R.string.pwd_not_valid));
            mPassword.requestFocus();
            return;
        }

        OGCloud2.registerAndLogin(this, email, password, firstName, lastName,
                new HttpCallback() {


                    @Override
                    public void onSuccess(Response response) {

                        EnterPasswordActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Intent intent = new Intent(EnterPasswordActivity.this, MainTabsActivity.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
                            }
                        });

                        response.body().close();
                    }

                    @Override
                    public void onFailure(Call call, final IOException e, OGCloud2.OGCloudError error) {
                        EnterPasswordActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progress.dismiss();
                                if (e != null)
                                    Log.d(TAG, e.getLocalizedMessage());
                                showAlert(getString(R.string.uhoh), "There was a problem signing you up. Do you already have an account with that email?");
                            }
                        });
                    }
                });
    }
}
