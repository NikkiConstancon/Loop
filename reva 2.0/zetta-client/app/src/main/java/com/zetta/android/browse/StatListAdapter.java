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
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.zetta.android.ImageLoader;
import com.zetta.android.R;
import com.zetta.android.StatItem;
import com.zetta.android.settings.SettingsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gregory Austin 22/08/2017
 */
public class StatListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public MyAdapterListener onClickListener;

    public interface MyAdapterListener {
        void moreInfoOnClick(View v, int position);
    }

    private static final String TAG = StatListAdapter.class.getSimpleName();

    private List<StatItem> cards;
    /**
     * Constructor for StatListAdapter that accepts a number of items to display and the specification
     * for the ListItemClickListener.
     *
     * @param cards, the cards to display in a list.
     */
    public StatListAdapter(List<StatItem> cards, MyAdapterListener listener ) {
        this.cards = cards;
        this.onClickListener = listener;
    }

    public List<StatItem> getCards() {
        return cards;
    }

    public void updateList (List<StatItem> items) {

        if (cards != null) {
            cards.clear();
            cards.addAll(items);
        }
        else {
            cards = items;
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return cards.get(position).getType();
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
            case StatItem.TYPE_LINE_GRAPH:
                layoutIdForListItem = R.layout.stat_item_graph;

                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                return new GraphViewHolder(view);
            case StatItem.TYPE_SIMPLE_STAT:
                layoutIdForListItem = R.layout.stat_item_simple;

                view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
                return new NumberViewHolder(view);
            default:
                throw new IllegalStateException("Attempted to create view holder for a type you haven't coded for: " + viewType);
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
            case StatItem.TYPE_LINE_GRAPH:
                ((GraphViewHolder) holder).bind((GraphStatItem) cards.get(position));
                break;
            case StatItem.TYPE_SIMPLE_STAT:
                ((NumberViewHolder) holder).bind((SimpleStatItem) cards.get(position));
                break;
            default:
                throw new IllegalStateException("Attempted to bind a type you haven't coded for: " + holder.getItemViewType());
        }
    }
    ImageLoader imageLoader = new ImageLoader();
    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our forecast
     */
    @Override
    public int getItemCount() {
        return cards.size();
    }

    /**
     * Cache of the children views for a list item.
     */
    class NumberViewHolder extends RecyclerView.ViewHolder {

        //private final

        @NonNull private final TextView stat_simple;
        @NonNull private final TextView units_simple_stat;
        @NonNull private final TextView stat_title;
        @NonNull private final TextView stat_subtitle;
        @NonNull private final ImageView stateImageWidget;

        /**
         * Constructor for our ViewHolder. Within this constructor, we get a reference to our
         * TextViews and set an onClickListener to listen for clicks. Those will be handled in the
         * onClick method below.
         * @param itemView The View that you inflated in
         *                 {@link StatListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public NumberViewHolder(View itemView) {
            super(itemView);

            stat_simple = (TextView) itemView.findViewById(R.id.stat_simple);
            units_simple_stat = (TextView) itemView.findViewById(R.id.units_simple_stat);
            stat_title = (TextView) itemView.findViewById(R.id.stat_title);
            stat_subtitle = (TextView) itemView.findViewById(R.id.stat_subtitle);
            stateImageWidget = (ImageView) itemView.findViewById(R.id.list_item_device_state_image);
        }

        /**
         * A method we wrote for convenience. This method will take an integer as input and
         * use that integer to display the appropriate text within a list item.
         * @param item The simplestatitem that needs to be displayed
         */
        void bind(SimpleStatItem item) {
            stat_simple.setText("" + item.getStatistic());
            units_simple_stat.setText("" + item.getUnits());
            stat_title.setText("" + item.deviceName + " " + item.getStatName());
            stat_subtitle.setText("Start: " + item.getStart() +"\nEnd:   " + item.getEnd());
            if (item.getImgURL() != "")
                imageLoader.load(Uri.parse(item.getImgURL()), stateImageWidget);
        }
    }

    /**
     * Cache of the children views for the graphs
     */
    class GraphViewHolder extends RecyclerView.ViewHolder {
        @NonNull private final TextView stat_title;
        @NonNull private final TextView stat_subtitle;
        @NonNull private final ImageView stateImageWidget;

        LineChart chart;

        /**
         * We get references to the views in the graph card
         * @param itemView The view that was inflated in
         *              {@Link StatListAdapter#onCreateViewHolder(ViewGroup, int)}
         */
        public GraphViewHolder(View itemView) {
            super(itemView);
            chart = (LineChart) itemView.findViewById(R.id.line_chart);
            stat_subtitle = (TextView) itemView.findViewById(R.id.stat_subtitle);
            stat_title = (TextView) itemView.findViewById(R.id.stat_title);
            stateImageWidget = (ImageView) itemView.findViewById(R.id.list_item_device_state_image);

            Button moreInfo = (Button) itemView.findViewById(R.id.more_stats);

            moreInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.moreInfoOnClick(v, getAdapterPosition());
                }
            });
        }

        void bind(GraphStatItem item) {

            stat_title.setText(item.getDeviceName() + " " + item.getStatName());
            stat_subtitle.setText("Start: " + item.getStart() +"\nEnd:   " + item.getEnd());
            if (item.getImgURL() != "")
                imageLoader.load(Uri.parse(item.getImgURL()), stateImageWidget);

            List<Entry> entries = new ArrayList<Entry>();
            for (int i = 0; i < item.getEntries().size(); i++) {
                entries.add(new Entry(item.getEntries().get(i).getX(), item.getEntries().get(i).getY()));
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
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setTextColor(R.color.colorSecondaryText);
            xAxis.setDrawAxisLine(false);

            YAxis leftAxis = chart.getAxisLeft();
            leftAxis.setTextColor(R.color.colorSecondaryText);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setGranularityEnabled(true);
            //leftAxis.setGranularity(5f); //TODO set this properly

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
