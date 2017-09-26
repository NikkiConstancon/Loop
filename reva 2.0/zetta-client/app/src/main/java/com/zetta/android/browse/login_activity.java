package com.zetta.android.browse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;
import com.zetta.android.lib.NotifyCloudAwait;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import android.os.Handler;

import org.json.JSONException;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hristian Vitrychenko on 06/06/2017.
 * Edited by Gregory Austin 10/08/2017.
 */

/**
 * login activity class for starting the login process (possible first page)
 */


public class login_activity extends AppCompatActivity {

    private Button loginButton;
    private Button registerButton;
    private boolean exit = false;

    /**
     * Overridden back button press to allow for accidental back presses
     */
    @Override
    public void onBackPressed() {
        if (exit) {
            finish();
            userManagerEndpoint.unbind(this);
            this.finishAffinity();
        } else {
            Toast.makeText(this, "Press back again to exit ReVA", Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);
        }
    }

    /**
     * Overridden onCreate for starting with the login_activity class
     *
     * @param savedInstanceState used to start a new instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        registerButton = (Button) findViewById(R.id.btn_register);
        loginButton = (Button) findViewById(R.id.btn_login);

        final EditText user = (EditText) findViewById(R.id.input_emailLogin);
        final EditText passw = (EditText) findViewById(R.id.input_passwordLogin);

        user.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    user.setHint("johndoe@example.com");
                } else {
                    user.setHint("");
                }
            }
        });


        /* Setting an OnClickListener allows us to do something when this button is clicked. */
        loginButton.setOnClickListener(new View.OnClickListener() {

            /**
             * The onClick method is triggered when this button (mDoSomethingCoolButton) is clicked.
             *
             * @param v The view that is clicked. In this case, it's mDoSomethingCoolButton.
             */
            @Override
            public void onClick(View v) {
                userManagerEndpoint.getService().setLogin(user.getText().toString(), passw.getText().toString());
                buildLoginAwaitObject();
            }
        });


        registerButton.setOnClickListener(new View.OnClickListener() {

            /**
             * The onClick method is triggered when this button (mDoSomethingCoolButton) is clicked.
             *
             * @param v The view that is clicked. In this case, it's mDoSomethingCoolButton.
             */
            @Override
            public void onClick(View v) {
                /*
                 * Storing the Context in a variable in this case is redundant since we could have
                 * just used "this" or "MainActivity.this" in the method call below. However, we
                 * wanted to demonstrate what parameter we were using "MainActivity.this" for as
                 * clear as possible.
                 */
                Context context = login_activity.this;
                Class destinationActivity = Registration.class;

                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);
                String message = "Register clicked!";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        });
        userManagerEndpoint.bind(this);
    }

    void buildLoginAwaitObject(){
        if (notifyWait == null) {
            notifyWait = new NotifyCloudAwait(login_activity.this, false,
                    750, " ... validating your input ... ", 5000) {
                @Override
                public void end(NotifyCloudAwait.DISMISS_TYPE type) {
                    if (type == DISMISS_TYPE.TIMEOUT) {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(login_activity.this);
                        builder1.setTitle("Could not connect to the cloud");
                        builder1.setMessage("try again ?");
                        builder1.setCancelable(true);
                        builder1.setPositiveButton("YES",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        notifyWait = null;
                                        buildLoginAwaitObject();
                                        dialog.dismiss();
                                    }
                                });
                        builder1.setNegativeButton("NO",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        notifyWait = null;
                                        dialog.dismiss();
                                    }
                                });
                        builder1.show();
                    }
                }
            };
            ((EditText) findViewById(R.id.input_emailLogin)).setError(null);
            ((EditText) findViewById(R.id.input_passwordLogin)).setError(null);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        userManagerEndpoint.unbind(this);
    }

    /**
     * Validation of login details and attempt to continue to main
     */
    public Boolean attemptLogin() {
        Context context = this;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please fill in all details.");
        builder1.setCancelable(true);


        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        String pass = findViewById(R.id.input_passwordLogin).toString();
        String email = findViewById(R.id.input_emailLogin).toString();

        if (email.length() < 1 || pass.length() < 1) {
            TextInputLayout til = (TextInputLayout) findViewById(R.id.login_email_label);
            til.setErrorEnabled(true);
            til.setError("You need to enter a name");
        } else {
            Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,63}$");
            Matcher m = p.matcher(email);

            Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
            Matcher m2 = p2.matcher(pass);

            TextInputLayout til;
            /*if (!m.find())
            {
                til = (TextInputLayout)    findViewById(R.id.login_email_label);
                til.setErrorEnabled(true);
                til.setError("Type in your email address");
            }*/
            if (!m2.find()) {
                til = (TextInputLayout) findViewById(R.id.login_pass_label);
                til.setErrorEnabled(true);
                til.setError("Type in your password");
            }
            if (m2.find())//&& m.find() hotfix
            {
                //validation happens here
                return true;

            }
        }
        return false;
    }


    NotifyCloudAwait notifyWait = null;
    UserManagerEndpoint userManagerEndpoint = new UserManagerEndpoint();

    class UserManagerEndpoint extends RevaWebsocketEndpoint {
        @Override
        public String key() {
            return "UserManager";
        }

        public void onMessage(LinkedTreeMap obj) {
            //TODO move this to
            final String USER_MANAGER_KEY_CONNECTED = "CONNECTED";
            if (obj.containsKey(USER_MANAGER_KEY_CONNECTED)) {
                notifyWait.dismiss();
                notifyWait = null;
                Map<String, Object> info = (Map<String, Object>) obj.get(USER_MANAGER_KEY_CONNECTED);
                String userUid = (String) info.get("USER_UID");
                if (userUid.compareTo(RevaWebSocketService.SPECIAL_USER_ANONYMOUS) != 0) {
                    Intent intent = new Intent(login_activity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("Username", userUid.toString());
                    startActivity(intent);
                } else {
                    final Map<String, String> errorMap = (Map<String, String>) info.get("ERROR");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errorMap != null) {
                                String error = errorMap.get("text");
                                EditText text = null;
                                if (errorMap.get("field").compareTo("password") == 0) {
                                    text = findViewById(R.id.input_passwordLogin);
                                } else {
                                    text = findViewById(R.id.input_emailLogin);
                                }
                                text.setError(error);
                            } else {
                                ((EditText) findViewById(R.id.input_emailLogin)).setError("You must specify a username");
                            }
                        }
                    });
                }
            }
        }
    }
}
