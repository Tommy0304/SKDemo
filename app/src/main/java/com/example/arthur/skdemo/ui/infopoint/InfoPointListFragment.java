package com.example.arthur.skdemo.ui.infopoint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.ui.MapActivity;

/**
 * Created by Arthur on 4/20/2016.
 *
 */
public class InfoPointListFragment extends Fragment {

    private static final String TAG = InfoPointListFragment.class.getSimpleName();

    private InfoPointListAdapter mInfoPointListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInfoPointListAdapter = new InfoPointListAdapter(((MapActivity) getActivity()).getInfoPointList());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_infopoint_list, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.info_point_recycler_view);
        recyclerView.setAdapter(mInfoPointListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

}
