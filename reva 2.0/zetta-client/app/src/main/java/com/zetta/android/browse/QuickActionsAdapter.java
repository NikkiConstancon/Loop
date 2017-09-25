package com.zetta.android.browse;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zetta.android.ListItem;
import com.zetta.android.R;
import com.zetta.android.device.actions.ActionMultipleInputListItem;
import com.zetta.android.device.actions.ActionMultipleViewHolder;
import com.zetta.android.device.actions.ActionSingleInputListItem;
import com.zetta.android.device.actions.ActionSingleViewHolder;
import com.zetta.android.device.actions.ActionToggleListItem;
import com.zetta.android.device.actions.ActionToggleViewHolder;
import com.zetta.android.device.actions.OnActionClickListener;

import java.util.ArrayList;
import java.util.List;

class QuickActionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    @NonNull private final List<ListItem> items = new ArrayList<>();

    @NonNull private final OnActionClickListener onActionClickListener;

    public QuickActionsAdapter(@NonNull OnActionClickListener onActionClickListener) {
        this.onActionClickListener = onActionClickListener;
    }

    public void updateAll(@NonNull List<ListItem> listItems) {
        this.items.clear();
        this.items.addAll(listItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ListItem.TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_quick_action_header, parent, false);
            return new HeaderViewHolder(v);
        } else if (viewType == ListItem.TYPE_ACTION_TOGGLE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action_toggle, parent, false);
            return new ActionToggleViewHolder(v);
        } else if (viewType == ListItem.TYPE_ACTION_SINGLE_INPUT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action_input_single, parent, false);
            return new ActionSingleViewHolder(v);
        } else if (viewType == ListItem.TYPE_ACTION_MULTIPLE_INPUT) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_action_input_multiple, parent, false);
            return new ActionMultipleViewHolder(v);
        } else if (viewType == ListItem.TYPE_LOADING) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
            return new LoadingViewHolder(v);
        } else if (viewType == ListItem.TYPE_EMPTY) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_empty, parent, false);
            return new EmptyViewHolder(v);
        }
        throw new IllegalStateException("Attempted to create view holder for a type you haven't coded for: " + viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        if (type == ListItem.TYPE_HEADER) {
            ListItem.HeaderListItem headerListItem = (ListItem.HeaderListItem) items.get(position);
            ((HeaderViewHolder) holder).bind(headerListItem);
            return;
        } else if (type == ListItem.TYPE_ACTION_TOGGLE) {
            ActionToggleListItem actionToggleListItem = (ActionToggleListItem) items.get(position);
            ((ActionToggleViewHolder) holder).bind(actionToggleListItem, onActionClickListener);
            return;
        } else if (type == ListItem.TYPE_ACTION_SINGLE_INPUT) {
            ActionSingleInputListItem actionSingleListItem = (ActionSingleInputListItem) items.get(position);
            ((ActionSingleViewHolder) holder).bind(actionSingleListItem, onActionClickListener);
            return;
        } else if (type == ListItem.TYPE_ACTION_MULTIPLE_INPUT) {
            ActionMultipleInputListItem actionMultipleListItem = (ActionMultipleInputListItem) items.get(position);
            ((ActionMultipleViewHolder) holder).bind(actionMultipleListItem, onActionClickListener);
            return;
        } else if (type == ListItem.TYPE_LOADING) {
            ((LoadingViewHolder) holder).bind();
            return;
        } else if (type == ListItem.TYPE_EMPTY) {
            ListItem.EmptyListItem emptyListItem = (ListItem.EmptyListItem) items.get(position);
            ((EmptyViewHolder) holder).bind(emptyListItem);
            return;
        }
        throw new IllegalStateException("Attempted to bind a type you haven't coded for: " + type);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView headerTitleWidget;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitleWidget = (TextView) itemView.findViewById(R.id.list_item_action_header_label);
        }

        public void bind(@NonNull ListItem.HeaderListItem item) {
            headerTitleWidget.setText(item.getTitle());
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public void bind() {
            // nothing to bind
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageWidget;

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
