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

public class StatisticalAdapter extends RecyclerView.Adapter<StatisticalAdapter.StatisticalViewHolder> {

    ArrayList<Statistical> statisticalArrayList = new ArrayList<Statistical>();

    public StatisticalAdapter(ArrayList<Statistical> statistical)
    {
        statisticalArrayList = statistical;
    }

    @Override
    public StatisticalAdapter.StatisticalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_layout, parent, false);
        StatisticalAdapter.StatisticalViewHolder statisticalViewHolder = new StatisticalAdapter.StatisticalViewHolder(view);
        return statisticalViewHolder;
    }

    @Override
    public void onBindViewHolder(StatisticalViewHolder holder, int position) {

        Statistical data = statisticalArrayList.get(position);
        holder.icon_img.setImageResource(data.getImg_id());
        holder.aboveText.setText(data.getAboveText());
        holder.min.setText(data.getMinimum());
        holder.max.setText(data.getMaximum());
        holder.avg.setText(data.getAverage());
    }

    @Override
    public int getItemCount() {
        return statisticalArrayList.size();
    }

    public static class StatisticalViewHolder extends RecyclerView.ViewHolder{

        ImageView icon_img;
        TextView aboveText, min, max, avg;

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
