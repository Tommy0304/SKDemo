package com.example.arthur.skdemo.ui.infopoint;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.data.model.InfoPoint;
import com.example.arthur.skdemo.networking.DatabaseManager;

import java.util.List;
import java.util.Locale;

/**
 * Created by Arthur on 4/20/2016.
 *
 */
public class InfoPointListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<InfoPoint> mInfoPointList;

    public InfoPointListAdapter(List<InfoPoint> infoPointList) {

        mInfoPointList = infoPointList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InfoPointHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_info_point, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((InfoPointHolder) holder).setup(mInfoPointList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mInfoPointList.size();
    }

    public static class InfoPointHolder extends RecyclerView.ViewHolder {

        public InfoPointHolder(View itemView) {
            super(itemView);
        }

        public void setup(final InfoPoint infoPoint, int position) {

            int bgResourceId = position % 2 == 0 ? R.drawable.bg_infopoint_list_item  : R.drawable.bg_infopoint_list_item_reversed;
            itemView.setBackgroundResource(bgResourceId);
            ((TextView) itemView.findViewById(R.id.item_title)).append(infoPoint.title);
            ((TextView) itemView.findViewById(R.id.item_address)).append(infoPoint.address);
            ((TextView) itemView.findViewById(R.id.item_description)).append(infoPoint.description);
            ((TextView) itemView.findViewById(R.id.item_longitude)).append(String.format(Locale.getDefault(), "%f", infoPoint.longitude));
            ((TextView) itemView.findViewById(R.id.item_latitude)).append(String.format(Locale.getDefault(), "%f", infoPoint.latitude));
            final ImageButton starButton = (ImageButton) itemView.findViewById(R.id.item_favourite_toggle);
            int starResourceId = infoPoint.favourite == InfoPoint.NOT_FAVOURITE ? R.drawable.ic_star_empty : R.drawable.ic_star_full;
            starButton.setImageResource(starResourceId);
            starButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (infoPoint.favourite == InfoPoint.NOT_FAVOURITE) {
                        infoPoint.favourite = InfoPoint.FAVOURITE;
                        starButton.setImageResource(R.drawable.ic_star_full);
                    } else {
                        infoPoint.favourite = InfoPoint.NOT_FAVOURITE;
                        starButton.setImageResource(R.drawable.ic_star_empty);
                    }
                    final Context context = itemView.getContext();
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            DatabaseManager.getInstance(context).updateInfoPoint(infoPoint);
                            return null;
                        }
                    }.execute();
                }
            });
        }
    }
}
