package com.zetta.android.browse;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.zetta.android.R;

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

    /**
     * Overridden onCreate for starting with the login_activity class
     * @param savedInstanceState used to start a new instance
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);



        loginButton = (Button) findViewById(R.id.btn_login);
        registerButton = (Button) findViewById(R.id.btn_register);



        /* Setting an OnClickListener allows us to do something when this button is clicked. */
        loginButton.setOnClickListener(new View.OnClickListener() {

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
                Class destinationActivity = DeviceListActivity.class;

                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);
                String message = "Login clicked!";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();

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
                Class destinationActivity = DeviceListActivity.class;

                Intent intent = new Intent(context, destinationActivity);
                startActivity(intent);
                String message = "Register clicked!";
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();

            }
        });
    }

    /**
     * Validation of login details and attempt to continue to main
     * @param s not needed and not used
     */
    public void attemptLogin(Editable s)
    {
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

        if(email.length() < 1 || pass.length() < 1)
        {
            AlertDialog alertWarning = builder1.create();
            alertWarning.show();
        }
        else
        {
            Pattern p = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z.]{2,63}$");
            Matcher m = p.matcher(email);

            Pattern p2 = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{6,}$");
            Matcher m2 = p2.matcher(pass);

            if (m.find())
            {
                builder1.setMessage("Email incorrect. Please double check your email.");
                AlertDialog alertWarning = builder1.create();
                alertWarning.show();
            }
            else if(m2.find())
            {
                builder1.setMessage("Password incorrect. Passwords must be at least 6 characters long, with at least one capital letter and number.");
                AlertDialog alertWarning = builder1.create();
                alertWarning.show();
            }
            else
            {
                //validation happens here
                Intent toMain = new Intent(this, DeviceList.class);
                startActivityForResult(toMain,0);
            }
        }
    }

    /**
     * on click method for first time registration
     * @param s not needed and not used
     */
    public void toRegistration(Editable s)
    {
//        Intent toReg = new Intent(this, Registration.class);
//        startActivityForResult(toReg, 0);
    }
}
