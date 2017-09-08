package com.zetta.android.browse;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.zetta.android.R;

import java.util.Date;

import static android.hardware.camera2.params.RggbChannelVector.BLUE;

/**
 * Created by Hristian Vitrychenko on 07/09/2017.
 */

public class AddSimpleStat extends AppCompatActivity {

    boolean checkedRad = false;
    boolean graph = false;
    TableLayout tl;
    TextView newText;
    ScrollView scroll;
    Spinner spin;

    Long startDate, endDate;
    String vital, statistic, interval;

    //The input values


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_simple_stat);

        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(15);
        shape.setColor(BLUE);
        shape.setStroke(2, Color.parseColor("#38ACEC"));

        spin = (Spinner) findViewById(R.id.spin_choose_vital);
        spin.setBackground(shape);
        spin = (Spinner) findViewById(R.id.spin_choose_simp_stat);
        spin.setBackground(shape);
        spin = (Spinner) findViewById(R.id.spin_choose_interval);
        spin.setBackground(shape);

        scroll = (ScrollView) findViewById(R.id.stat_scroll);

        final Button btnAddStat = (Button) findViewById(R.id.btn_add_simple_stat);
        final Context myCont = this;

        tl = (TableLayout) findViewById(R.id.stat_table_layout);
        tl.removeView(spin);

        newText = new TextView(this);
        newText.setTextSize(20);
        newText.setGravity(Gravity.CENTER);
        newText.setPadding(0,0,0,15);
        newText.setText("Please choose an interval for the graph");
        newText.setLines(2);

        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroupStat);
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
                if(checkedId == R.id.rad_statYes) {

                    tl.removeView(btnAddStat);
                    tl.addView(newText);
                    tl.addView(spin);
                    tl.addView(btnAddStat);

                    scroll.post(new Runnable() {
                        @Override
                        public void run() {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });

                    checkedRad = true;
                    graph = true;

                } else if(checkedId == R.id.rad_statNo) {

                    if(graph == true)
                    {
                        tl.removeView(newText);
                        tl.removeView(spin);
                    }
                    checkedRad = true;
                    graph = false;
                }
            }

        });



        //On click for adding the statistic
        btnAddStat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                int day, month, year, hour, minute;

                Spinner checker = (Spinner) findViewById(R.id.spin_choose_vital);
                vital = checker.getSelectedItem().toString();
                checker = (Spinner) findViewById(R.id.spin_choose_simp_stat);
                statistic = checker.getSelectedItem().toString();

                if(graph == true)
                {
                    checker = (Spinner) findViewById(R.id.spin_choose_interval);
                    interval = checker.getSelectedItem().toString();
                }

                DatePicker datePicker = (DatePicker) findViewById(R.id.simp_datePicker);
                TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);


                day = datePicker.getDayOfMonth();
                month = datePicker.getMonth();
                year =  datePicker.getYear();

                Date date = new Date(day, month, year, timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                startDate = date.getTime();

                datePicker = (DatePicker) findViewById(R.id.simp_datePicker2);
                timePicker = (TimePicker) findViewById(R.id.timePicker2);

                day = datePicker.getDayOfMonth();
                month = datePicker.getMonth();
                year =  datePicker.getYear();

                date = new Date(day, month, year, timePicker.getCurrentHour(), timePicker.getCurrentMinute());

                endDate = date.getTime();

                Toast.makeText(myCont, vital + " " + statistic + " " + interval + startDate.toString() + " " + endDate.toString(),Toast.LENGTH_SHORT).show();

                //Do whatever you want with the data
            }
        });
    }
}
