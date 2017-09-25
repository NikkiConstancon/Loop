package com.zetta.android.device;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zetta.android.R;

public class EmptyLoadingView extends LinearLayout {

    private TextView emptyLoadingLabel;
    private ProgressBar loadingIndicatorWidget;

    public EmptyLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.device_list_empty_loading_view, this, true);
        emptyLoadingLabel = (TextView) findViewById(R.id.empty_loading_text_label);
        loadingIndicatorWidget = (ProgressBar) findViewById(R.id.empty_loading_loading_view);
    }

    public void setStateLoading() {
        loadingIndicatorWidget.setVisibility(View.VISIBLE);
        String loadingLabel = getResources().getString(R.string.querying_device);
        emptyLoadingLabel.setText(loadingLabel);
    }

}
