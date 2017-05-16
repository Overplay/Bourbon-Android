package tv.ourglass.alyssa.bourbon_android.Scenes.Registration;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;
import tv.ourglass.alyssa.bourbon_android.R;

public class EnterNameActivity extends RegistrationBaseActivity {

    private EditText mFirstName;
    private EditText mLastName;

    private ImageButton mNextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_name);

        mFirstName = (EditText)findViewById(R.id.firstName);
        mLastName = (EditText)findViewById(R.id.lastName);

        mNextButton = (ImageButton)findViewById(R.id.nextButton);

        // Add text change listeners
        mFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkFields();
            }
        });

        mLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkFields();
            }
        });
    }

    private void checkFields() {
        if ((mFirstName.getText().toString().isEmpty() && mLastName.getText().toString().isEmpty())) {
            mNextButton.animate().alpha(0f).setDuration(OGConstants.fadeOutTime).start();
        } else {
            mNextButton.animate().alpha(1f).setDuration(OGConstants.fadeInTime).start();
        }
    }

    public void next(View view) {
        Intent intent = new Intent(this, EnterEmailActivity.class)
                .putExtra(OGConstants.firstNameExtra, mFirstName.getText().toString())
                .putExtra(OGConstants.lastNameExtra, mLastName.getText().toString());
        startActivity(intent);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
    }
}
