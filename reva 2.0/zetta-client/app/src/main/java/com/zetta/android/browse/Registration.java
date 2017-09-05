package com.zetta.android.browse;

/**
 * Created by Hristian Vitrychenko on 27/07/2017.
 */

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Method that handles all registration procedures for both patients and subscribers
 */
public class Registration extends AppCompatActivity {
    /**
     * Overridden on create method used to load register activity
     *
     * @param savedInstanceState used to save instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnCont = (Button) findViewById(R.id.btn_register);

        btnCont.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                contReg();
            }
        });

        EditText text = (EditText) findViewById(R.id.txt_patientCheck);
        text.setEnabled(false);


        checkedRad = false;

        Context context = this;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please double check your details");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    /**
                     * On click method for warning dialogue box
                     * @param dialog the dialog to be displayed
                     * @param id the id of the message
                     */
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupReg1);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            /**
             * Overridden on checked changed method that keeps track of radio group changes
             * @param group the radio group
             * @param checkedId the view id that was checked
             */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                checkedRad = false;
                // find which radio button is selected
                if (checkedId == R.id.rad_patYes) {
                    checkedRad = true;
                    patient = true;

                } else if (checkedId == R.id.rad_patientNo) {
                    checkedRad = true;
                    patient = false;
                }
            }

        });

        userManagerEndpoint.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        userManagerEndpoint.unbind(this);
    }

    boolean checkedRad = false;
    boolean patient = false;
    String regEmail = "", regPass = "", confPass = "", address = "", username = "", subPass = "", confSubPass = "";
    int ageVal = 0;
    double weight = 0, height = 0;


    /**
     * Method that continues registration for patients (not for subscribers, validation and intent change)
     */
    public void contReg() {
        EditText text = (EditText) findViewById(R.id.input_emailReg);
        regEmail = text.getText().toString();
        text = (EditText) findViewById(R.id.input_passwordReg);
        regPass = text.getText().toString();
        text = (EditText) findViewById(R.id.input_confirmPassReg);
        confPass = text.getText().toString();

        Context context = this;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please double check kiss your details");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    /**
                     * On click method for warning dialogue box
                     * @param dialog the dialog to be displayed
                     * @param id the id of the message
                     */
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,63}$");
        Matcher m = p.matcher(regEmail);

        Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
        Matcher m2 = p2.matcher(regPass);

        if (regEmail.length() < 1 || regPass.length() < 1 || confPass.length() < 1 || !checkedRad) {
            builder1.setMessage("Please fill in all details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (!m.find()) {
            builder1.setMessage("Email incorrect. Please double check your email.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (!m2.find()) {
            builder1.setMessage("Password incorrect. Passwords must be at least 6 characters long, with at least one capital letter and number.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (!regPass.equals(confPass)) {
            builder1.setMessage("Passwords do not match. Please double check your password confirmation.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else {
            //NOTE! Moved the startActivityForResult execute after confirmation of available email
            //TODO what if no connection? -> override onConnect/Disconnect etc.
            userManagerEndpoint.getPublisher().send(KEY_EMAIL_AVAILABLE, regEmail);
        }
    }

    private static final String KEY_EMAIL_AVAILABLE = "TEST_EMAIL_AVAILABLE";
    UserManagerEndpoint userManagerEndpoint = new UserManagerEndpoint();

    class UserManagerEndpoint extends RevaWebsocketEndpoint {
        private final String TAG = this.getClass().getName();

        @Override
        public void onMessage(final LinkedTreeMap obj) {
            Log.d(TAG, obj.toString());
            GsonBuilder builder = new GsonBuilder();

            if (obj.containsKey(KEY_EMAIL_AVAILABLE)) {
                if ((Boolean)obj.get(KEY_EMAIL_AVAILABLE)) {
                    if (patient) {
                        /////////////////////////////////////////change intent to Registration_cont
                        Intent toReg = new Intent(Registration.this, Registration_Cont.class);
                        toReg.putExtra("regEmail", regEmail);
                        toReg.putExtra("regPass", regPass);
                        startActivityForResult(toReg, 0);
                    } else {
                        ////////////////////////////////////////change intent to Registration_Sub
                        Intent toSub = new Intent(Registration.this, Registration_Sub.class);
                        toSub.putExtra("regEmail", regEmail);
                        toSub.putExtra("regPass", regPass);
                        startActivityForResult(toSub, 0);
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            EditText text = (EditText) findViewById(R.id.input_emailReg);
                            text.setError("This email is not available");
                        }
                    });
                }
            }
        }

        @Override
        public String key() {
            return "UserManager";
        }

        @Override
        public void onServiceConnect(RevaWebSocketService service) {
            if(service.getAuthForHeader() == null) {
                service.setLogin("--ANONYMOUS--", "");
            }
        }
    }
}
