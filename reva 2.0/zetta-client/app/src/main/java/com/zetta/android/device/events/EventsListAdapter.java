package com.zetta.android.device.events;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zetta.android.ListItem;
import com.zetta.android.R;

import java.util.ArrayList;
import java.util.List;

class EventsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull private final List<ListItem> listItems = new ArrayList<>();

    public void update(@NonNull ListItem listItem) {
        this.listItems.add(0, listItem);
        notifyDataSetChanged();
    }

    public void updateAll(@NonNull List<ListItem> listItems) {
        this.listItems.clear();
        this.listItems.addAll(listItems);
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return listItems.isEmpty();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        EventListItem serverListItem = (EventListItem) listItems.get(position);
        ((EventViewHolder) holder).bind(serverListItem);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {

        private final TextView transitionLabelWidget;
        private final TextView timeStampLabelWidget;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            transitionLabelWidget = (TextView) itemView.findViewById(R.id.list_item_event_transition);
            timeStampLabelWidget = (TextView) itemView.findViewById(R.id.list_item_event_timestamp);
        }

        public void bind(@NonNull EventListItem eventListItem) {
            transitionLabelWidget.setText(eventListItem.getTransition());
            transitionLabelWidget.setTextColor(eventListItem.getForegroundColor());
            timeStampLabelWidget.setText(eventListItem.getTimeStamp());
            timeStampLabelWidget.setTextColor(eventListItem.getForegroundColor());
            itemView.setBackgroundColor(eventListItem.getBackgroundColor());
        }
    }

}
