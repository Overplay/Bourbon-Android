package tv.ourglass.alyssa.bourbon_android.Scenes.Settings.EditAccount;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;
import tv.ourglass.alyssa.bourbon_android.Model.Input.TextValidator;
import tv.ourglass.alyssa.bourbon_android.Model.OGConstants;
import tv.ourglass.alyssa.bourbon_android.Model.SharedPrefsManager;
import tv.ourglass.alyssa.bourbon_android.Networking.OGCloud;
import tv.ourglass.alyssa.bourbon_android.R;
import tv.ourglass.alyssa.bourbon_android.Scenes.Tabs.MainTabsActivity;

public class EditAccountFragment extends Fragment {
    final String TAG = "EditAccountFragment";

    EditText mFirstName;

    EditText mLastName;

    //EditText mEmail;

    Button mSave;

    ProgressDialog progress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_account, container, false);

        progress = new ProgressDialog(getActivity());

        // set up top toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.tb_edit_account));

//        mEmail = (EditText) view.findViewById(R.id.email);
//        mEmail.setEnabled(false);

        mSave = (Button) view.findViewById(R.id.save);
        mFirstName = (EditText) view.findViewById(R.id.firstName);
        mFirstName.requestFocus(); // take away focus from email which is not enabled
        mLastName = (EditText) view.findViewById(R.id.lastName);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        (view.findViewById(R.id.changePasswordButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainTabsActivity) getActivity()).openNewFragment(new ChangePasswordFragment());
            }
        });

        // Add text change listeners
        mFirstName.addTextChangedListener(new TextValidator(mFirstName) {
            @Override
            public void validate(TextView textView, String text) {
                checkFields();
            }
        });

        mLastName.addTextChangedListener(new TextValidator(mLastName) {
            @Override
            public void validate(TextView textView, String text) {
                checkFields();
            }
        });

        mFirstName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    if (mFirstName.getText().toString().trim().isEmpty()) {
                        mFirstName.setError(getString(R.string.req_field));
                    }
                    return true;
                }
                return false;
            }
        });

        mLastName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)) {
                    if (mLastName.getText().toString().trim().isEmpty()) {
                        mLastName.setError(getString(R.string.req_field));
                    }
                    return true;
                }
                return false;
            }
        });

        displayUserInfo();

        return view;
    }

    public void checkFields() {
        if (mFirstName.getText().toString().trim().isEmpty() || mLastName.getText().toString().trim().isEmpty()) {
            mSave.animate().alpha(0f).setDuration(OGConstants.fadeOutTime).start();
        } else {
            mSave.animate().alpha(1f).setDuration(OGConstants.fadeInTime).start();
        }
    }

    private void displayUserInfo() {
        //progress.show();
        OGCloud.getInstance().checkJWT(getActivity(),
                new OGCloud.HttpCallback() {
                    @Override
                    public void onFailure(Call call, IOException ex, OGCloud.OGCloudError error) {
                        Log.e(TAG, "bad JWT");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                builder
                                        .setTitle(getString(R.string.uhoh))
                                        .setMessage("It looks like your session has expired. Please log back in.")
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                OGCloud.getInstance().logout(getActivity());
                                            }
                                        });
                                progress.dismiss();
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Response response) {
                        Log.d(TAG, "good JWT");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mFirstName.setText(SharedPrefsManager.getUserFirstName(getActivity()));
                                mLastName.setText(SharedPrefsManager.getUserLastName(getActivity()));
                                //mEmail.setText(SharedPrefsManager.getUserEmail(getActivity()));
                                progress.dismiss();
                            }
                        });
                        response.body().close();

                    }
                });
    }

    public void save() {
        Boolean firstNameValid = true, lastNameValid = true;

        if (mFirstName.getText().toString().trim().isEmpty()) {
            firstNameValid = false;
            mFirstName.setError(getString(R.string.req_field));
            mFirstName.requestFocus();
        }
        if (mLastName.getText().toString().trim().isEmpty()) {
            lastNameValid = false;
            mLastName.setError(getString(R.string.req_field));
            mLastName.requestFocus();
        }

        if (firstNameValid && lastNameValid) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder
                    .setTitle("Save changes")
                    .setMessage("Are you sure you want to save changes to your account information?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveAccountInfo();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void saveAccountInfo() {
        progress.show();

        final String firstName = mFirstName.getText().toString();
        final String lastName = mLastName.getText().toString();
        //final String email = SharedPrefsManager.getUserEmail(getActivity());

        // temp until we allow email change, if ever
        String email = null;

        OGCloud.getInstance().changeAccountInfo(getActivity(), firstName, lastName, email,
                SharedPrefsManager.getUserId(getActivity()),
                new OGCloud.HttpCallback() {

                    @Override
                    public void onFailure(Call call, IOException e, OGCloud.OGCloudError error) {
                        Log.e(TAG, "save info failed");
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                                progress.dismiss();

                                builder
                                        .setTitle(getString(R.string.uhoh))
                                        .setMessage("There was a problem saving your new account information.")
                                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                builder.create().show();
                            }
                        });
                    }

                    @Override
                    public void onSuccess(Response response) {
                        response.body().close();

                        SharedPrefsManager.setUserFirstName(getActivity(), firstName);
                        SharedPrefsManager.setUserLastName(getActivity(), lastName);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mFirstName.setText(firstName);
                                mLastName.setText(lastName);
                                progress.dismiss();
                            }
                        });
                    }
                });
    }
}
