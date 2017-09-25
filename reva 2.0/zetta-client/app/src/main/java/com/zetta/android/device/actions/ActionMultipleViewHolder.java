package com.zetta.android.device.actions;

import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.novoda.notils.meta.AndroidUtils;
import com.zetta.android.ImageLoader;
import com.zetta.android.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionMultipleViewHolder extends RecyclerView.ViewHolder {

    private static final int[] rowIds = {R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4, R.id.row_5, R.id.row_6};

    @NonNull private final List<EditText> boundInputRows = new ArrayList<>();

    @NonNull private final AppCompatButton actionButton;

    public ActionMultipleViewHolder(@NonNull View itemView) {
        super(itemView);
        actionButton = (AppCompatButton) itemView.findViewById(R.id.list_item_action_button);
    }

    public void bind(@NonNull final ActionMultipleInputListItem item,
                     @NonNull final OnActionClickListener onActionClickListener) {
        if (item.getLabels().size() > 6) {
            itemView.setVisibility(View.GONE);
            Log.e("Zetta", "Sorry demo only supports upto 6 custom input actions. Cannot display " + item.getLabels());
            return;
        }
        final List<String> labels = item.getLabels();
        for (int i = 0; i < labels.size(); i++) {
            TextInputLayout rowWidget = (TextInputLayout) itemView.findViewById(rowIds[i]);
            rowWidget.setVisibility(View.VISIBLE);
            rowWidget.setHint(labels.get(i));
            ImageLoader.Drawables.setInputTextLayoutColor(rowWidget, item.getActionInputTextColor());

            EditText actionInputWidget = (EditText) rowWidget.findViewById(R.id.list_item_action_input);
            actionInputWidget.setTextColor(item.getActionInputTextColor());
            boundInputRows.add(actionInputWidget);
        }

        actionButton.setText(item.getAction());
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> labelledInput = new HashMap<>();
                for (int i = 0; i < labels.size(); i++) {
                    String label = labels.get(i);
                    EditText actionInputWidget = boundInputRows.get(i);
                    String inputForLabel = actionInputWidget.getText().toString();
                    actionInputWidget.getText().clear();
                    actionInputWidget.clearFocus();
                    AndroidUtils.requestHideKeyboard(actionInputWidget.getContext(), actionInputWidget);
                    labelledInput.put(label, inputForLabel);
                }
                onActionClickListener.onActionClick(item.getDeviceId(), item.getAction(), labelledInput);
            }
        });
        actionButton.setTextColor(item.getActionTextColor());
        actionButton.setBackground(item.createActionBackground());
        itemView.setBackground(item.createBackground());
    }

}
