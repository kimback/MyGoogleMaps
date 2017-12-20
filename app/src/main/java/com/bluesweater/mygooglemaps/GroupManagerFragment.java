package com.bluesweater.mygooglemaps;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class GroupManagerFragment extends Fragment {

    private DashboardActivity context;
    private Button createBtn;
    private EditText groupText;

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

        createBtn = (Button) view.findViewById(R.id.btn_group);
        groupText = (EditText) view.findViewById(R.id.text_group);

        return view;
    }



    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = (DashboardActivity) context;
    }

}
