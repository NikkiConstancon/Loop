package com.reva.loop301.reva;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hristian Vitrychenko on 07/07/2017.
 */

/**
 * Adapter for real time data to be displayed as cards
 */
public class RealTimeAdapter extends RecyclerView.Adapter<RealTimeAdapter.RealTimeViewHolder> {

    ArrayList<RealTime> realTimeData = new ArrayList<RealTime>();

    /**
     * Constructor for the real time data adapter
     * @param realTime holds array list of real time data
     */
    public RealTimeAdapter(ArrayList<RealTime> realTime)
    {
        realTimeData = realTime;
    }


    /**
     * Overridden onCreateViewHolder adapted for real time data displaying
     * @param parent holds the view group that is the parent of the view holder
     * @param viewType holds the type of view the view holder will use
     * @return the real time view holder
     */
    @Override
    public RealTimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        RealTimeViewHolder realTimeViewHolder = new RealTimeViewHolder(view);
        return realTimeViewHolder;
    }

    /**
     * Overridden onBindViewHolder class adapted for real time view hodlers
     * @param holder holds the real time view holder
     * @param position holds the position of the real time view holder
     */
    @Override
    public void onBindViewHolder(RealTimeViewHolder holder, int position) {

        RealTime data = realTimeData.get(position);
        holder.icon_img.setImageResource(data.getImg_id());
        holder.aboveText.setText(data.getAboveText());
        holder.value.setText(data.getValue());
    }

    /**
     * Overridden getItemCount, adapted for returning number of real time data
     * @return number of real time data cases
     */
    @Override
    public int getItemCount() {
        return realTimeData.size();
    }

    /**
     * RealTimeViewHolder class used for holding and displaying real time data
     */
    public static class RealTimeViewHolder extends RecyclerView.ViewHolder{

        ImageView icon_img;
        TextView aboveText, value, extValue;

        /**
         * Constructor for the real time view holder
         * @param itemView holds the view of the item about to be displayed
         */
        public RealTimeViewHolder(View itemView) {
            super(itemView);
            icon_img = itemView.findViewById(R.id.img_heart_rate_icon);
            aboveText = itemView.findViewById(R.id.txt_heartRate);
            value = itemView.findViewById(R.id.txt_heart_rate_number);
        }
    }
}
