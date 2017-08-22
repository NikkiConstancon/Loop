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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hristian Vitrychenko on 22/08/2017.
 */

public class Registration_Cont extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_continue);

        Button btnRegPat = (Button) findViewById(R.id.btn_patRegCont);

        EditText text = (EditText) findViewById(R.id.txt_addr);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_userName);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_subpass);
        text.setEnabled(false);

        text = (EditText) findViewById(R.id.txt_confirmSubPass);
        text.setEnabled(false);

        btnRegPat.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                contPatReg();
            }
        });
    }

    public void contPatReg()
    {
        EditText text = (EditText) findViewById((R.id.input_addr));
        String address = text.getText().toString();
        text = (EditText) findViewById(R.id.input_userName);
        String username = text.getText().toString();
        text = (EditText) findViewById((R.id.input_subsPass));
        String subPass = text.getText().toString();
        text = (EditText) findViewById(R.id.input_confSubPass);
        String confSubPass = text.getText().toString();

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
        else if(!m2.find())
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
            ///////////////////////////////////////////////////////// set intent to Register_Patient
            String regEmail = getIntent().getStringExtra("regEmail");
            String regPass = getIntent().getStringExtra("regPass");
            Intent toRegPat = new Intent(context, Registration_Patient.class);
            toRegPat.putExtra("regEmail", regEmail);
            toRegPat.putExtra("regPass", regPass);
            toRegPat.putExtra("address", address);
            toRegPat.putExtra("username", username);
            toRegPat.putExtra("subPass", subPass);
            startActivityForResult(toRegPat, 0);
        }
    }

}
