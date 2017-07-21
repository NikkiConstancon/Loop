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

public class RealTimeAdapter extends RecyclerView.Adapter<RealTimeAdapter.RealTimeViewHolder> {

    ArrayList<RealTime> realTimeData = new ArrayList<RealTime>();

    public RealTimeAdapter(ArrayList<RealTime> realTime)
    {
        realTimeData = realTime;
    }


    @Override
    public RealTimeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        RealTimeViewHolder realTimeViewHolder = new RealTimeViewHolder(view);
        return realTimeViewHolder;
    }

    @Override
    public void onBindViewHolder(RealTimeViewHolder holder, int position) {

        RealTime data = realTimeData.get(position);
        holder.icon_img.setImageResource(data.getImg_id());
        holder.aboveText.setText(data.getAboveText());
        holder.value.setText(data.getValue());
    }

    @Override
    public int getItemCount() {
        return realTimeData.size();
    }

    public static class RealTimeViewHolder extends RecyclerView.ViewHolder{

        ImageView icon_img;
        TextView aboveText, value, extValue;

        public RealTimeViewHolder(View itemView) {
            super(itemView);
            icon_img = itemView.findViewById(R.id.img_heart_rate_icon);
            aboveText = itemView.findViewById(R.id.txt_heartRate);
            value = itemView.findViewById(R.id.txt_heart_rate_number);
        }
    }
}
