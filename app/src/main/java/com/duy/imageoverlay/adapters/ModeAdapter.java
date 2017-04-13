package com.duy.imageoverlay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.duy.imageoverlay.views.GirdFrameLayout.MODE_BOTTOM_RIGHT;
import static com.duy.imageoverlay.views.GirdFrameLayout.MODE_LEFT_RIGHT;
import static com.duy.imageoverlay.views.GirdFrameLayout.MODE_TOP_BOTTOM;
import static com.duy.imageoverlay.views.GirdFrameLayout.MODE_TOP_LEFT;

public class ModeAdapter extends BindableAdapter<Integer> {
    private static final int[] VALUES = {
           MODE_TOP_BOTTOM, MODE_TOP_LEFT,  MODE_LEFT_RIGHT, MODE_BOTTOM_RIGHT
    };

    private static final String[] TITLES = {
            "Top - Bottom", "Top - Left", "Left - Right", "Bottom - Right"
    };

    public ModeAdapter(Context context) {
        super(context);
    }

    @Override
    public int getCount() {
        return VALUES.length;
    }

    @Override
    public Integer getItem(int position) {
        return VALUES[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false);
    }

    @Override
    public void bindView(Integer item, int position, View view) {
        TextView tv = (TextView) view.findViewById(android.R.id.text1);
        tv.setText(TITLES[position]);
    }

    @Override
    public View newDropDownView(LayoutInflater inflater, int position, ViewGroup container) {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false);
    }
}
