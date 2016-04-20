package com.example.arthur.skdemo.ui.mapstyle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.data.model.EventObject;
import com.example.arthur.skdemo.ui.infopoint.InfoPointFragment;

import de.greenrobot.event.EventBus;

/**
 * Created by Arthur on 4/19/2016.
 *
 */
public class MapStyleFragment extends Fragment {

    private static final String TAG = InfoPointFragment.class.getSimpleName();

    public static final String KEY_MAP_STYLE = "KEY_MAP_STYLE";

    public static final int MAP_STYLE_DAY = 0;
    public static final int MAP_STYLE_NIGHT = 1;
    public static final int MAP_STYLE_GRAYSCALE = 2;
    public static final int MAP_STYLE_OUTDOOR = 3;

    private int mOldStyle;

    private RadioButton mDayRadioButton;
    private RadioButton mNightRadioButton;
    private RadioButton mGrayscaleRadioButton;
    private RadioButton mOutdoorRadioButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        mOldStyle = bundle.getInt(KEY_MAP_STYLE, 0);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_map_style, container, false);

        mDayRadioButton = (RadioButton) view.findViewById(R.id.map_style_day_radio_button);
        mNightRadioButton = (RadioButton) view.findViewById(R.id.map_style_night_radio_button);
        mGrayscaleRadioButton = (RadioButton) view.findViewById(R.id.map_style_grayscale_radio_button);
        mOutdoorRadioButton = (RadioButton) view.findViewById(R.id.map_style_outdoor_radio_button);

        switch (mOldStyle) {
            case MAP_STYLE_NIGHT:
                mNightRadioButton.setChecked(true);
                break;
            case MAP_STYLE_GRAYSCALE:
                mGrayscaleRadioButton.setChecked(true);
                break;
            case MAP_STYLE_OUTDOOR:
                mOutdoorRadioButton.setChecked(true);
                break;
            default:
                mDayRadioButton.setChecked(true);
                break;
        }

        view.findViewById(R.id.map_style_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventObject event = new EventObject(EventObject.MAP_STYLE_CANCEL);
                EventBus.getDefault().post(event);
            }
        });

        view.findViewById(R.id.map_style_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventObject event = new EventObject(EventObject.MAP_STYLE_SUBMIT);
                event.mapStyle = getSelectedStyle();
                EventBus.getDefault().post(event);
            }
        });

        return view;
    }

    private int getSelectedStyle() {
        if (mNightRadioButton.isChecked()) {
            return MAP_STYLE_NIGHT;
        } else if (mGrayscaleRadioButton.isChecked()) {
            return MAP_STYLE_GRAYSCALE;
        } else if (mOutdoorRadioButton.isChecked()) {
            return MAP_STYLE_OUTDOOR;
        } else {
            return  MAP_STYLE_DAY;
        }
    }
}