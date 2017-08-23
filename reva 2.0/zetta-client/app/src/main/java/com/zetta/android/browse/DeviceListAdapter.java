package com.zetta.android.browse;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.novoda.notils.logger.simple.Log;
import com.zetta.android.ImageLoader;
import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.ZettaDeviceId;

import java.util.ArrayList;
import java.util.List;

class DeviceListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull private final List<ListItem> listItems = new ArrayList<>();

    @NonNull private final ImageLoader imageLoader;
    @NonNull private final OnDeviceClickListener onDeviceClickListener;

    private int touchingPosition;

    DeviceListAdapter(@NonNull ImageLoader imageLoader, @NonNull OnDeviceClickListener onDeviceClickListener) {
        this.imageLoader = imageLoader;
        this.onDeviceClickListener = onDeviceClickListener;
    }

    public void replaceAll(@NonNull List<ListItem> listItems) {
        this.listItems.clear();
        this.listItems.addAll(listItems);
        notifyDataSetChanged();
    }

    public void update(@NonNull List<ListItem> listItems) {
        for (ListItem listItem : listItems) {
            int i = this.listItems.indexOf(listItem);
            if (i == -1) {
                Log.v("Not found in list " + listItem.getType());
            } else if (i == touchingPosition) {
                Log.v("currently being touched not updating");
            } else {
                // USING THIS ALLOWS A SINGLE ITEM IN A LIST TO BE UPDATED AND THE REST LEFT AS IS
                this.listItems.remove(i);
                this.listItems.add(i, listItem);
                notifyItemChanged(i);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    touchingPosition = RecyclerView.NO_POSITION;
                } else {
                    View childView = rv.findChildViewUnder(e.getX(), e.getY());
                    touchingPosition = rv.getChildAdapterPosition(childView);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return listItems.get(position).getType();
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_SERVER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_server, parent, false);
            return new ServerViewHolder(v);
        } else if (viewType == ListItem.TYPE_DEVICE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_device, parent, false);
            return new DeviceViewHolder(v, imageLoader);
        } else if (viewType == ListItem.TYPE_EMPTY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty, parent, false);
            return new EmptyViewHolder(v);
        }
        throw new IllegalStateException("Attempted to create view holder for a type you haven't coded for: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_SERVER) {
            ServerListItem serverListItem = (ServerListItem) listItems.get(position);
            ((ServerViewHolder) holder).bind(serverListItem);
            return;
        } else if (type == ListItem.TYPE_DEVICE) {
            DeviceListItem deviceListItem = (DeviceListItem) listItems.get(position);
            ((DeviceViewHolder) holder).bind(deviceListItem, onDeviceClickListener);
            return;
        } else if (type == ListItem.TYPE_EMPTY) {
            ListItem.EmptyListItem emptyListItem = (ListItem.EmptyListItem) listItems.get(position);
            ((EmptyViewHolder) holder).bind(emptyListItem);
            return;
        }
        throw new IllegalStateException("Attempted to bind a type you haven't coded for: " + type);
    }

    public interface OnDeviceClickListener {

        void onDeviceClick(@NonNull ZettaDeviceId deviceId);

        void onDeviceLongClick(@NonNull ZettaDeviceId deviceId);

    }

    public static class DeviceViewHolder extends RecyclerView.ViewHolder {

        @NonNull private final ImageLoader imageLoader;
        @NonNull private final TextView nameLabelWidget;
        @NonNull private final TextView unitsOfMeasure;
        @NonNull private final TextView stateLabelWidget;
        @NonNull private final ImageView stateImageWidget;

        public DeviceViewHolder(@NonNull View itemView, @NonNull ImageLoader imageLoader) {
            super(itemView);
            this.imageLoader = imageLoader;

            nameLabelWidget = (TextView) itemView.findViewById(R.id.list_item_device_name);
            stateLabelWidget = (TextView) itemView.findViewById(R.id.list_item_device_state);
            unitsOfMeasure = (TextView) itemView.findViewById(R.id.units_of_measure);
            stateImageWidget = (ImageView) itemView.findViewById(R.id.list_item_device_state_image);
        }

        public void bind(@NonNull final DeviceListItem deviceListItem,
                         @NonNull final OnDeviceClickListener onDeviceClickListener) {
            itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onDeviceClickListener.onDeviceClick(deviceListItem.getDeviceId());
                    }
                });
            itemView.setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        onDeviceClickListener.onDeviceLongClick(deviceListItem.getDeviceId());
                        return true;
                    }
                });

            if (deviceListItem.getDeviceId().equals(itemView.getTag())) {
                // Performance Optimisation which makes an assumption
                // when an item is updated via a stream only the state label & image is wanting to be updated
                stateLabelWidget.setText(deviceListItem.getState());
            } else {
                itemView.setTag(deviceListItem.getDeviceId());
                itemView.setBackground(deviceListItem.createBackground());
                nameLabelWidget.setText(deviceListItem.getName());
                nameLabelWidget.setTextColor(deviceListItem.getTextColor());
                stateLabelWidget.setText(deviceListItem.getState());
                stateLabelWidget.setTextColor(deviceListItem.getTextColor());
                unitsOfMeasure.setText(deviceListItem.getUnitOfMeasure());
                imageLoader.load(deviceListItem.getStateImageUri(), stateImageWidget);
                stateImageWidget.setColorFilter(deviceListItem.getImageColorFilter());
            }
        }

    }

    public static class ServerViewHolder extends RecyclerView.ViewHolder {

        @NonNull private final View swatchColorWidget;
        @NonNull private final TextView nameLabelWidget;

        public ServerViewHolder(@NonNull View itemView) {
            super(itemView);
            swatchColorWidget = itemView.findViewById(R.id.list_item_server_swatch);
            nameLabelWidget = (TextView) itemView.findViewById(R.id.list_item_server_name);
        }

        public void bind(@NonNull ServerListItem serverListItem) {
            swatchColorWidget.setBackgroundColor(serverListItem.getSwatchColor());
            nameLabelWidget.setText(serverListItem.getName());
            nameLabelWidget.setTextColor(serverListItem.getTextColor());
            itemView.setBackground(serverListItem.createBackground());
        }

    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        @NonNull private final TextView messageWidget;

        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
            messageWidget = (TextView) itemView.findViewById(R.id.list_item_empty_message);
        }

        public void bind(@NonNull ListItem.EmptyListItem listItem) {
            messageWidget.setText(listItem.getMessage());
            messageWidget.setTextColor(listItem.getTextColor());
            itemView.setBackground(listItem.createBackground());
        }

    }

}
