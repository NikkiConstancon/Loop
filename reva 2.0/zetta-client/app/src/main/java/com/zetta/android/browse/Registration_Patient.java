package com.zetta.android.browse;

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
import android.widget.Toast;

import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.zetta.android.R;
import com.zetta.android.revaServices.UserManager;
import com.zetta.android.revawebsocketservice.ChannelPublisher;
import com.zetta.android.revawebsocketservice.CloudAwaitObject;
import com.zetta.android.revawebsocketservice.RevaWebSocketService;
import com.zetta.android.revawebsocketservice.RevaWebsocketEndpoint;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Hristian Vitrychenko on 22/08/2017.
 */

public class Registration_Patient extends AppCompatActivity {

    RadioGroup rg2;
    RadioGroup rg3;
    boolean checkedRad = false;
    boolean age = false, illness = false, accident = false, disability = false;

    /**
     * Overridden on create method to load activity_register_continue_patient view
     *
     * @param savedInstanceState saved instance of view state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_continue_patient);

        EditText textHold = (EditText) findViewById(R.id.txt_age);
        textHold.setEnabled(false);
        textHold = (EditText) findViewById(R.id.txt_height);
        textHold.setEnabled(false);
        textHold = (EditText) findViewById(R.id.txt_weight);
        textHold.setEnabled(false);
        textHold = (EditText) findViewById(R.id.txt_reason);
        textHold.setEnabled(false);

        checkedRad = false;

        rg2 = (RadioGroup) findViewById(R.id.radioGroupReg2);
        rg3 = (RadioGroup) findViewById(R.id.radioGroupReg3);
        rg2.clearCheck();
        rg3.clearCheck();
        rg2.setOnCheckedChangeListener(listener1);
        rg3.setOnCheckedChangeListener(listener2);

        Button btnContPat2 = (Button) findViewById(R.id.btn_patRegDone);
        btnContPat2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registerPatient();
            }
        });

        registerPatientEndpoint.bind(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerPatientEndpoint.unbind(this);
    }

    /**
     * Private radio group listener that simulates larger radio group
     */
    private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {
        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg3.setOnCheckedChangeListener(null);
                rg3.clearCheck();
                rg3.setOnCheckedChangeListener(listener2);

                if (checkedId == R.id.rad_age) {
                    checkedRad = true;
                    age = true;
                    illness = false;
                    accident = false;
                    disability = false;

                } else if (checkedId == R.id.rad_illness) {
                    checkedRad = true;
                    illness = true;
                    age = false;
                    accident = false;
                    disability = false;
                }
            }
        }
    };


    /**
     * Private radio group listener that simulates larger radio group
     */
    private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null);
                rg2.clearCheck();
                rg2.setOnCheckedChangeListener(listener1);

                if (checkedId == R.id.rad_accident) {
                    checkedRad = true;
                    age = false;
                    illness = false;
                    accident = true;
                    disability = false;

                } else if (checkedId == R.id.rad_disability) {
                    checkedRad = true;
                    illness = false;
                    age = false;
                    accident = false;
                    disability = true;
                }
            }
        }
    };

    /**
     * Method that completes the patient registration process (Validation and intent change)
     */
    AlertDialog.Builder builder1;

    public void registerPatient() {

        int ageVal = 0;//Values for user data
        double weight = 0, height = 0;

        final Context context = this;
       /*AlertDialog.Builder*/
        builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please fill in all details.");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        EditText text;

        try {
            text = (EditText) findViewById(R.id.input_age);
            ageVal = Integer.parseInt(text.getText().toString());
            text = (EditText) findViewById(R.id.input_weight);
            weight = Double.parseDouble(text.getText().toString());
            text = (EditText) findViewById(R.id.input_height);
            height = Double.parseDouble(text.getText().toString());
        } catch (Exception ex) {
            builder1.setMessage("Please make sure you provided an appropriate number for the appropriate details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
            return;
        }

        if (ageVal == 0 || weight == 0 || height == 0 || checkedRad == false) {
            builder1.setMessage("Please fill in all details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (ageVal < 1 || ageVal > 130) {
            builder1.setMessage("Age is out of bounds, please input an appropriate age.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (weight < 1 || weight > 600) {
            builder1.setMessage("Weight is out of bounds, please input an appropriate weight.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else if (height < 1 || height > 3) {
            builder1.setMessage("Height is out of bounds, please input an appropriate height.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        } else {
            registerPatientEndpoint.sendRequest(
                    builder1,
                    getIntent().getStringExtra("regEmail"),
                    getIntent().getStringExtra("regPass"),
                    getIntent().getStringExtra("address"),
                    getIntent().getStringExtra("username"),
                    getIntent().getStringExtra("subPass")
            );
        }
    }

    UserManager.RegisterPatientEndpoint registerPatientEndpoint = new UserManager.RegisterPatientEndpoint(this);
}
