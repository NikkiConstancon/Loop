package com.zetta.android.browse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.zetta.android.R;

/**
 * Created by Hristian Vitrychenko on 22/08/2017.
 */

public class Registration_Sub extends AppCompatActivity {

    /**
     * Overridden on create view to load activity_register_continue_subscriber view
     * @param savedInstanceState saved instance of view state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_continue_subscriber);

        EditText textHold = (EditText) findViewById(R.id.txt_Title);
        textHold.setEnabled(false);
        textHold = (EditText) findViewById(R.id.txt_whoSub);
        textHold.setEnabled(false);
        textHold = (EditText) findViewById(R.id.txt_subscribePass);
        textHold.setEnabled(false);

        checkedRad = false;

        rg5 = (RadioGroup) findViewById(R.id.radioGroupReg4);
        rg4 = (RadioGroup) findViewById(R.id.radioGroupReg5);
        rg4.clearCheck();
        rg5.clearCheck();
        rg4.setOnCheckedChangeListener(listener3);
        rg5.setOnCheckedChangeListener(listener4);


        Button btnRegSub = (Button) findViewById(R.id.btn_subRegDone);
        btnRegSub.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                registerSub();
            }
        });
    }

    RadioGroup rg4;
    RadioGroup rg5;
    boolean checkedRad = false;
    boolean family = false, caretaker = false, researcher = false, doctor = false;
    String toWho = "", toWhoPass = "";

    /**
     * Private radio group listener that simulates larger radio group (for subscribers)
     */
    private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg5.setOnCheckedChangeListener(null);
                rg5.clearCheck();
                rg5.setOnCheckedChangeListener(listener4);

                if(checkedId == R.id.rad_family) {
                    checkedRad = true;
                    family = true;
                    caretaker = false;
                    researcher = false;
                    doctor = false;

                } else if(checkedId == R.id.rad_caretaker) {
                    checkedRad = true;
                    family = false;
                    caretaker = true;
                    researcher = false;
                    doctor = false;
                }
            }
        }
    };

    /**
     * Private radio group listener that simulates larger radio group (for subscribers)
     */
    private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg4.setOnCheckedChangeListener(null);
                rg4.clearCheck();
                rg4.setOnCheckedChangeListener(listener3);

                if(checkedId == R.id.rad_researcher) {
                    checkedRad = true;
                    family = false;
                    caretaker = false;
                    researcher = true;
                    doctor = false;

                } else if(checkedId == R.id.rad_doctor) {
                    checkedRad = true;
                    family = false;
                    caretaker = false;
                    researcher = false;
                    doctor = true;
                }
            }
        }
    };

    /**
     * Method that completes the subscriber registration process (Validation and intent change)
     */
    public void registerSub()
    {
        EditText text = (EditText) findViewById(R.id.input_whoSub);
        toWho = text.getText().toString();
        text = (EditText) findViewById(R.id.input_subToPatientPass);
        toWhoPass = text.getText().toString();

        final Context context = this;
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

        //Need some database validation here!

        if(toWho.length() < 1 || toWhoPass.length() < 1 || checkedRad == false)
        {
            builder1.setMessage("Please fill in all details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else
        {
            String regEmail = getIntent().getStringExtra("regEmail");
            String regPass = getIntent().getStringExtra("regPass");

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent toLogin = new Intent(context, login_activity.class);
                            toLogin.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            toLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            toLogin.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivityForResult(toLogin, 0);
                        }
                    });
            builder1.setMessage("You have been registered!");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
            //register subscriber in database

        }
        //Database validation needed for this section in terms of existing users and subscriber passwords
    }
}
