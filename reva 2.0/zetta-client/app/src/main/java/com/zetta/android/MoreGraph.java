package com.zetta.android;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zetta.android.R;
import com.zetta.android.browse.GraphStatItem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.zetta.android.R.id.stat_subtitle;
import static com.zetta.android.R.id.stat_title;

public class MoreGraph extends AppCompatActivity {

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

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i < ontray.size(); i++) {
            entries.add(new Entry(ontray.get(i).x(), ontray.get(i).y()));
        }

        LineDataSet dataSet = new LineDataSet(entries, "First Graph");
        LineData lineData = new LineData(dataSet);

        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        Description description = new Description();
        description.setText("");
        chart.setDescription(description);

        chart.getLegend().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.setData(lineData);

        chart.invalidate();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
