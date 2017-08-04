package com.reva.loop301.reva;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hristian Vitrychenko on 08/07/2017.
 */

/**
 * Adapter class for displaying statistical data in card view form
 */
public class StatisticalAdapter extends RecyclerView.Adapter<StatisticalAdapter.StatisticalViewHolder> {

    ArrayList<Statistical> statisticalArrayList = new ArrayList<Statistical>();

    /**
     * Constructor for StatisticalAdapter
     * @param statistical holds array list of statistical data objects
     */
    public StatisticalAdapter(ArrayList<Statistical> statistical)
    {
        statisticalArrayList = statistical;
    }

    /**
     * Overridden onCreateViewHolder, adapted for displaying statistical data
     * @param parent holds the view group that is the parent of the view holder
     * @param viewType holds the type of view the view holder will use
     * @return the statistical view holder
     */
    @Override
    public StatisticalAdapter.StatisticalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        StatisticalAdapter.StatisticalViewHolder statisticalViewHolder = new StatisticalAdapter.StatisticalViewHolder(view);
        return statisticalViewHolder;
    }

    /**
     * Overridden onBindViewHolder, adapted for statistical view holders
     * @param holder holds the statistical view holder
     * @param position holds the position of the view
     */
    @Override
    public void onBindViewHolder(StatisticalViewHolder holder, int position) {

        Statistical data = statisticalArrayList.get(position);
        holder.icon_img.setImageResource(data.getImg_id());
        holder.aboveText.setText(data.getAboveText());
        holder.min.setText(data.getMinimum());
        holder.max.setText(data.getMaximum());
        holder.avg.setText(data.getAverage());
    }

    /**
     * Overridden getItemCount, adapted for statistical objects
     * @return the size of the statisticalArrayList (i.e. the number of statistical data objects)
     */
    @Override
    public int getItemCount() {
        return statisticalArrayList.size();
    }

    /**
     * Statistical view holder class, created for displaying statistical data
     */
    public static class StatisticalViewHolder extends RecyclerView.ViewHolder{

        ImageView icon_img;
        TextView aboveText, min, max, avg;

        /**
         * Constructor for StatisticalViewHolder
         * @param itemView holds the statistical data to be viewed
         */
        public StatisticalViewHolder(View itemView) {
            super(itemView);
            icon_img = itemView.findViewById(R.id.img_heart_rate_icon);
            aboveText = itemView.findViewById(R.id.txt_heartRateStat);
            min = itemView.findViewById(R.id.txt_heart_min_val);
            max = itemView.findViewById(R.id.txt_heart_max_val);
            avg = itemView.findViewById(R.id.txt_heart_avg_val);
        }
    }
}
