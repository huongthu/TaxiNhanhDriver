package com.example.thu.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.thu.taxinhanhdriver.R;
import com.example.thu.utils.BookHistory;

/**
 * Created by thu on 6/12/2017.
 */

public class HistoryFragment extends Fragment {
    View root = null;
    public static android.support.v4.app.Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = (ViewGroup) inflater.inflate(R.layout.activity_history, null);

        BookHistory history = new BookHistory("This is a pick up location bla bla bla",
                "This is a drop off location bla bla bla",
                "12/06/2017", "30 mins", "Nguyễn Văn Tài Xế", "http://");

        for (int i = 0; i < 10; i++) {
            addHistoryRow(history);
        }

        return root;
    }

    public void addHistoryRow(BookHistory history) {
        if (null == history) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater)  root.getContext().getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View viewHistory = inflater.inflate(R.layout.layout_history_row, null);

        //get TextViews
        TextView tvPickUp = (TextView)viewHistory.findViewById(R.id.tvPickUp);
        TextView tvDropOff = (TextView)viewHistory.findViewById(R.id.tvDropOff);
        TextView tvHistoryDay = (TextView)viewHistory.findViewById(R.id.tvHistoryDay);
        TextView tvHistoryDuring = (TextView)viewHistory.findViewById(R.id.tvHistoryDuring);
        TextView tvHistoryDriver = (TextView)viewHistory.findViewById(R.id.tvHistoryDriver);

        //set textviews' value
        tvPickUp.setText(history.getPickUp());
        tvDropOff.setText(history.getDropOff());
        tvHistoryDay.setText(history.getDate());
        tvHistoryDuring.setText(history.getDuringTime());
        tvHistoryDriver.setText(history.getTaxiDriver());

        //focus textview to scroll horizontally
        tvPickUp.setSelected(true);
        tvDropOff.setSelected(true);
        tvHistoryDay.setSelected(true);
        tvHistoryDuring.setSelected(true);
        tvHistoryDriver.setSelected(true);

        //find linearLayout and put history row to this
        LinearLayout llChatContent = (LinearLayout)root.findViewById(R.id.llHistoryContent);
        llChatContent.addView(viewHistory);

        //scroll to end - NOT WORKING
        ScrollView svChatContent = (ScrollView)root.findViewById(R.id.svHistoryContent);
        svChatContent.fullScroll(View.FOCUS_DOWN);
    }
}
