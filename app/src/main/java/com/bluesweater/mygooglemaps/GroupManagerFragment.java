package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GroupManagerFragment extends Fragment {

    private DashboardActivity context;

    public GroupManagerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_group_manager, container, false);

        String title = getArguments().getString("title");
        context.changeTitleView(title);

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (DashboardActivity) context;
    }

}
