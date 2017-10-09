package com.zetta.android;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.zetta.android.R;
import com.zetta.android.browse.GraphStatItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import static com.zetta.android.R.id.stat_subtitle;
import static com.zetta.android.R.id.stat_title;

public class MoreGraph extends AppCompatActivity {
    private float reference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.more_graph);

        // Setting the toolbar up
        Toolbar toolbar = (Toolbar) findViewById(R.id.graph_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        String title = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(title);



        LineChart chart = (LineChart) findViewById(R.id.more_line_chart);
        ArrayList<GraphEntry> ontray = (ArrayList<GraphEntry>) getIntent().getSerializableExtra("entries");
        reference = getIntent().getFloatExtra("reference", 0f);
        Log.d("REF", reference + "");

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < ontray.size(); i++) {
            entries.add(new Entry((ontray.get(i).getX()), ontray.get(i).getY()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "First Graph");
        LineData lineData = new LineData(dataSet);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setValueFormatter(createDateFormatter());
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setData(lineData);

        chart.invalidate();
    }
    IAxisValueFormatter createDateFormatter() {
        IAxisValueFormatter formatter = new IAxisValueFormatter() {


            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                Date date = new Date((long) (value + reference)); //TODO: change the label mode so that it suits the time period selected

                SimpleDateFormat fmt;


//                switch (labelModeSelected) {
//                    case HOURS_FORMAT:
//                        fmt = new SimpleDateFormat("h:mm a");
//                        break;
//
//                    case DAYS_FORMAT:
//                        fmt = new SimpleDateFormat("E d");
//                        break;
//
//                    case WEEKS_FORMAT:
//                        fmt = new SimpleDateFormat("d MMM");
//                        break;
//
//                    case MONTHS_FORMAT:
//                        fmt = new SimpleDateFormat("MMM yyyy");
//                        break;
//
//                    case YEARS_FORMAT:
//                        fmt = new SimpleDateFormat("yyyy");
//
//                        break;
//
//                    default:
//                        fmt = new SimpleDateFormat("E d MMM");
//                        break;
//                }


                fmt = new SimpleDateFormat("H:mm");
                fmt.setTimeZone(TimeZone.getDefault());


                String s = fmt.format(date);


                return s;
            }

            // we don't draw numbers, so no decimal digits needed
            public int getDecimalDigits() {
                return 0;
            }


        };

        return formatter;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
