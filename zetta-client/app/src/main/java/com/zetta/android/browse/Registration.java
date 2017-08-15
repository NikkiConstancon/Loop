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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.zetta.android.R;

import org.w3c.dom.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Method that handles all registration procedures for both patients and subscribers
 */
public class Registration extends AppCompatActivity
{
    /**
     * Overridden on create method used to load register activity
     * @param savedInstanceState used to save instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnCont = (Button) findViewById(R.id.btn_register);
        /*
        Button btnRegPat = (Button) findViewById(R.id.btn_patRegDone);
        Button btnRegSub = (Button) findViewById(R.id.btn_subRegDone);*/

        btnCont.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                contReg();
            }
        });

        //TextView txtReg = (TextView) findViewById(R.id.txt_register);
        //txtReg.setEnabled(false);

        EditText text = (EditText) findViewById(R.id.txt_userMailReg);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_PassReg);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_confPass);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_patientCheck);
        text.setEnabled(false);

        btnCont.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)  {
                contReg();
            }
        });


        /*

        btnRegPat.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                registerPatient();
            }
        });

        btnRegSub.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                registerSub();
            }
        });*/

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

                Context context = Registration.this;
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

                checkedRad = false;
                // find which radio button is selected
                if(checkedId == R.id.rad_patYes) {
                    builder1.setMessage("Patient Yes");
                    AlertDialog alertWarning = builder1.create();
                    alertWarning.show();
                    checkedRad = true;
                    patient = true;

                } else if(checkedId == R.id.rad_patientNo) {
                    builder1.setMessage("Patient No");
                    AlertDialog alertWarning = builder1.create();
                    alertWarning.show();
                    checkedRad = true;
                    patient = false;
                }
            }

        });
    }

    boolean checkedRad = false;
    boolean patient = false;
    String regEmail = "", regPass = "", confPass = "", address = "", username = "", subPass = "", confSubPass = "";
    int ageVal = 0;
    double weight = 0, height = 0;
    boolean age = false, illness = false, accident = false, disability = false;


    /**
     * Method that continues registration for patients (not for subscribers)
     */
    public void contReg()
    {
        EditText text = (EditText) findViewById(R.id.input_emailReg);
        regEmail = text.getText().toString();
        text = (EditText) findViewById(R.id.input_passwordReg);
        regPass = text.getText().toString();
        text = (EditText) findViewById(R.id.input_confirmPassReg);
        confPass = text.getText().toString();

        /*Button btnContPat1 = (Button) findViewById(R.id.btn_patRegCont);

        btnContPat1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                contPatReg();
            }
        });*/

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

        Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,63}$");
        Matcher m = p.matcher(regEmail);

        Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
        Matcher m2 = p2.matcher(regPass);

        if(regEmail.length() < 1 || regPass.length() < 1 || confPass.length() < 1 || !checkedRad)
        {
            builder1.setMessage("Please fill in all details." + regEmail.length() + regPass.length() + confPass.length() + checkedRad);
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(!m.find())
        {
            builder1.setMessage("Email incorrect. Please double check your email.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(!m2.find())
        {
            builder1.setMessage("Password incorrect. Passwords must be at least 6 characters long, with at least one capital letter and number.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(!regPass.equals(confPass))
        {
            builder1.setMessage("Passwords do not match. Please double check your password confirmation.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(patient)
        {
            builder1.setMessage("Continuing registration.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
            setContentView(R.layout.activity_register_continue);
        }
        else
        {
            //setContentView(R.layout.activity_register_continue_subscriber);
        }
    }

    /**
     * Method that continues patient registration by gathering additional patient data (not for subscribers)
     */
    public void contPatReg()
    {
        EditText text = (EditText) findViewById((R.id.input_addr));
        address = text.getText().toString();
        text = (EditText) findViewById(R.id.input_userName);
        username = text.getText().toString();
        text = (EditText) findViewById((R.id.input_subsPass));
        subPass = text.getText().toString();
        text = (EditText) findViewById(R.id.input_confSubPass);
        confSubPass = text.getText().toString();

        text = (EditText) findViewById(R.id.txt_addr);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_userName);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_subpass);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_confirmSubPass);
        text.setEnabled(false);

        Context context = this;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
        builder1.setMessage("Please fill in all details.");
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

        Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
        Matcher m2 = p2.matcher(subPass);

        if(address.length() < 1 || username.length() < 1 || subPass.length() < 1 || confSubPass.length() < 1)
        {
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(m2.find())
        {
            builder1.setMessage("Password incorrect. Passwords must be at least 6 characters long, with at least one capital letter and number.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(!subPass.equals(confSubPass))
        {
            builder1.setMessage("Passwords do not match. Please double check your password confirmation.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else
        {
            //setContentView(R.layout.activity_register_continue_patient);
        }
    }

    RadioGroup rg2;
    RadioGroup rg3;

    /**
     * Private radio group listener that simulates larger radio group
     */
    //private RadioGroup.OnCheckedChangeListener listener1 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        /*@Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null);
                rg2.clearCheck();
                rg2.setOnCheckedChangeListener(listener2);

                if(checkedId == R.id.rad_age) {
                    checkedRad = true;
                    age = true;
                    illness = false;
                    accident = false;
                    disability = false;

                } else if(checkedId == R.id.rad_illness) {
                    checkedRad = true;
                    illness = true;
                    age = false;
                    accident = false;
                    disability = false;
                }
            }
        }
    };*/


    /**
     * Private radio group listener that simulates larger radio group
     */
    //private RadioGroup.OnCheckedChangeListener listener2 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
       /* @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg3.setOnCheckedChangeListener(null);
                rg3.clearCheck();
                rg3.setOnCheckedChangeListener(listener1);

                if(checkedId == R.id.rad_accident) {
                    checkedRad = true;
                    age = false;
                    illness = false;
                    accident = true;
                    disability = false;

                } else if(checkedId == R.id.rad_disability) {
                    checkedRad = true;
                    illness = false;
                    age = false;
                    accident = false;
                    disability = true;
                }
            }
        }
    };*/


    /**
     * Final method for registering patient (actual registration occurs here)
     */
   // public void registerPatient()
    //{
        /*
        int ageVal = 0;
        double weight = 0, height = 0;
        boolean age, illness, accident, disability; */

       /* Context context = this;
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

        EditText text;

        try {
            text = (EditText) findViewById(R.id.input_age);
            ageVal = Integer.parseInt(text.getText().toString());
            text = (EditText) findViewById(R.id.input_weight);
            weight = Double.parseDouble(text.getText().toString());
            text = (EditText) findViewById(R.id.input_height);
            height = Double.parseDouble(text.getText().toString());
        }
        catch(Exception ex)
        {
            builder1.setMessage("Please make sure you provided an appropriate number for the appropriate details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
            return;
        }

        checkedRad = false;

        rg2 = (RadioGroup) findViewById(R.id.radioGroupReg2);
        rg3 = (RadioGroup) findViewById(R.id.radioGroupReg3);
        rg2.clearCheck();
        rg3.clearCheck();
        rg2.setOnCheckedChangeListener(listener1);
        rg3.setOnCheckedChangeListener(listener2);


        if(ageVal == 0 || weight == 0 || height == 0 || checkedRad == false)
        {
            builder1.setMessage("Please fill in all details.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(ageVal < 0 || ageVal > 130)
        {
            builder1.setMessage("Age is out of bounds, please input an appropriate age.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(weight < 0 || weight > 600)
        {
            builder1.setMessage("Weight is out of bounds, please input an appropriate weight.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else if(height < 0 || height > 3)
        {
            builder1.setMessage("Height is out of bounds, please input an appropriate height.");
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else
        {
            //register patient in database
            Intent toReg = new Intent(this, Login.class);
            startActivityForResult(toReg, 0);
        }
    }

    RadioGroup rg4;
    RadioGroup rg5;*/

    /**
     * Private radio group listener that simulates larger radio group (for subscribers)
     */
   // private RadioGroup.OnCheckedChangeListener listener3 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
       /* @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg2.setOnCheckedChangeListener(null);
                rg2.clearCheck();
                rg2.setOnCheckedChangeListener(listener4);

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
    };*/

    /**
     * Private radio group listener that simulates larger radio group (for subscribers)
     */
    //private RadioGroup.OnCheckedChangeListener listener4 = new RadioGroup.OnCheckedChangeListener() {

        /**
         * Overridden on checked changed method that keeps track of radio group changes and changes other radio group to not allow multiple radio inputs
         * @param group the radio group
         * @param checkedId the view id that was checked
         */
        /*@Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId != -1) {
                rg3.setOnCheckedChangeListener(null);
                rg3.clearCheck();
                rg3.setOnCheckedChangeListener(listener3);

                if(checkedId == R.id.rad_researcher) {
                    family = false;
                    caretaker = false;
                    researcher = true;
                    doctor = false;

                } else if(checkedId == R.id.rad_doctor) {
                    family = false;
                    caretaker = false;
                    researcher = false;
                    doctor = true;
                }
            }
        }*/
    //};

    boolean family = false, caretaker = false, researcher = false, doctor = false;
    String toWho = "", toWhoPass = "";

    /**
     * Method for subscriber registration (actual subscriber registration occurs here)
     */
   /* public void registerSub()
    {
        EditText text = (EditText) findViewById(R.id.input_whoSub);
        toWho = text.getText().toString();
        text = (EditText) findViewById(R.id.input_subToPatientPass);
        toWhoPass = text.getText().toString();

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

        checkedRad = false;

        rg4 = (RadioGroup) findViewById(R.id.radioGroupReg4);
        rg5 = (RadioGroup) findViewById(R.id.radioGroupReg5);
        rg4.clearCheck();
        rg5.clearCheck();
        rg4.setOnCheckedChangeListener(listener1);
        rg5.setOnCheckedChangeListener(listener2);

        if(toWho.length() < 1 || toWhoPass.length() < 1 || checkedRad == false)
        {
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else
        {
            //register subscriber in database
            Intent toReg = new Intent(this, Login.class);
            startActivityForResult(toReg, 0);
        }
        //Database validation needed for this section in terms of existing users and subscriber passwords
    }*/
}
