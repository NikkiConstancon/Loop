/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zetta.android.browse;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zetta.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregory Austin 22/08/2017
 */
public class StatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = StatListAdapter.class.getSimpleName();

    private int mNumberItems;
    /**
     * Constructor for StatListAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param numberOfItems Number of items to display in list
     */
    public StatListAdapter(int numberOfItems) {
        mNumberItems = numberOfItems;
    }

    @Override
    public int getItemViewType(int position) {
        return position % 2 * 2;
    }

    /**
     *
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new NumberViewHolder that holds the View for each list item
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem;
        View view;
        boolean shouldAttachToParentImmediately = false;
        LayoutInflater inflater = LayoutInflater.from(context);
        switch(viewType) {
            case 0:
                layoutIdForListItem = R.layout.stat_item_graph;

                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                return new GraphViewHolder(view);
            default:
                layoutIdForListItem = R.layout.stat_item_simple;

                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                return new NumberViewHolder(view);
        }
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the correct
     * indices in the list for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "#" + position);
        switch(holder.getItemViewType()) {
            case 0:
                ((GraphViewHolder) holder).bind(position);
                break;
            default:
                ((NumberViewHolder) holder).bind(position);
                break;
        }

    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return mNumberItems;
    }

    /**
     * Cache of the children views for a list item.
     */
    class NumberViewHolder extends RecyclerView.ViewHolder {

        // Will display the position in the list, ie 0 through getItemCount() - 1
        TextView listItemNumberView;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link StatListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public NumberViewHolder(View itemView) {
            super(itemView);

            listItemNumberView = (TextView) itemView.findViewById(R.id.stat_simple);
        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         * @param listIndex Position of the item in the list
         */
        void bind(int listIndex) {
            listItemNumberView.setText(String.valueOf(listIndex));
        }
    }

    /**
     * Cache of the children views for the graphs
     */
    class GraphViewHolder extends RecyclerView.ViewHolder {

        LineChart chart;

        /**
         * We get references to the views in the graph card
         * @param itemView The view that was inflated in
         *              {@Link StatListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public GraphViewHolder(View itemView) {
            super(itemView);
            chart = (LineChart) itemView.findViewById(R.id.line_chart);
        }

        void bind(int listIndex) {
            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < 20; i++) {
                entries.add(new Entry(i ,i));
            }


            // ALL THAT FOLLOWS IS STYLING FOR THE GRAPH
            LineDataSet dataSet = new LineDataSet(entries, "First Graph");

            dataSet.setFillColor(R.color.colorPrimary);
            dataSet.setDrawCircles(false);
            dataSet.setDrawValues(false);
            dataSet.setLineWidth(2.0f);


            LineData lineData = new LineData(dataSet);
            XAxis xAxis = chart.getXAxis();
            xAxis.setDrawGridLines(false);
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM_INSIDE);
            xAxis.setTextColor(R.color.colorSecondaryText);
            xAxis.setDrawAxisLine(false);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setTextColor(R.color.colorSecondaryText);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setGranularityEnabled(true);
            leftAxis.setGranularity(5f); //TODO set this properly

            //Removing thedescription
            Description description = new Description();
            description.setText("");
            chart.setDescription(description);

            chart.getLegend().setEnabled(false);
            chart.getAxisRight().setEnabled(false);

            chart.setTouchEnabled(false);
            chart.setDrawBorders(false);
            chart.setData(lineData);

            chart.invalidate();
        }
    }
}
