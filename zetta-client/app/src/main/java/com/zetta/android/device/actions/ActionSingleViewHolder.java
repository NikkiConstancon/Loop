package com.zetta.android.device.actions;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.novoda.notils.meta.AndroidUtils;
import com.zetta.android.ImageLoader;
import com.zetta.android.R;

import java.util.Collections;
import java.util.Map;

public class ActionSingleViewHolder extends RecyclerView.ViewHolder {

    @NonNull private final TextInputLayout actionHintWidget;
    @NonNull private final EditText actionInputWidget;
    @NonNull private final AppCompatButton actionButton;

    public ActionSingleViewHolder(@NonNull View itemView) {
        super(itemView);
        actionHintWidget = (TextInputLayout) itemView.findViewById(R.id.list_item_action_input_layout);
        actionInputWidget = (EditText) itemView.findViewById(R.id.list_item_action_input);
        actionButton = (AppCompatButton) itemView.findViewById(R.id.list_item_action_button);
    }

    public void bind(@NonNull final ActionSingleInputListItem item,
                     @NonNull final OnActionClickListener onActionClickListener) {
        actionHintWidget.setHint(item.getLabel());
        ImageLoader.Drawables.setInputTextLayoutColor(actionHintWidget, item.getActionInputTextColor());
        actionInputWidget.setTextColor(item.getActionInputTextColor());
        actionButton.setText(item.getAction());
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = actionInputWidget.getText().toString();
                String label = item.getLabel();
                String action = item.getAction();
                Map<String, Object> labelledInput = Collections.<String, Object>singletonMap(label, input);
                onActionClickListener.onActionClick(item.getDeviceId(), action, labelledInput);
                actionInputWidget.getText().clear();
                actionInputWidget.clearFocus();
                AndroidUtils.requestHideKeyboard(actionInputWidget.getContext(), actionInputWidget);
            }
        });
        actionButton.setTextColor(item.getActionTextColor());
        actionButton.setBackground(item.getActionBackground());
        itemView.setBackground(item.getBackground());
    }
}
