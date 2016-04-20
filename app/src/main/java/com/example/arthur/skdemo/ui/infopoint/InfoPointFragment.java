package com.example.arthur.skdemo.ui.infopoint;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.data.model.EventObject;
import com.example.arthur.skdemo.data.model.InfoPoint;

import de.greenrobot.event.EventBus;

/**
 * Created by Arthur on 4/19/2016.
 *
 */
public class InfoPointFragment extends Fragment {

    private static final String TAG = InfoPointFragment.class.getSimpleName();

    public static final String KEY_ACTION_TYPE = "KEY_ACTION_TYPE";
    public static final String KEY_ANNOTATION_ID = "KEY_ANNOTATION_ID";
    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_ADDRESS = "KEY_ADDRESS";
    public static final String KEY_DESCRIPTION = "KEY_DESCRIPTION";
    public static final String KEY_LONGITUDE = "KEY_LONGITUDE";
    public static final String KEY_LATITUDE = "KEY_LATITUDE";
    public static final String KEY_FAVOURITE = "KEY_FAVOURITE";

    public static final int ACTION_CREATE = 0;
    public static final int ACTION_EDIT = 1;

    private int mActionType;
    private int mAnnotationId;
    private String mTitle;
    private String mAddress;
    private String mDescription;
    private double mLongitude;
    private double mLatitude;
    private int mFavourite;

    private EditText mTitleInput;
    private EditText mAddressInput;
    private EditText mDescriptionInput;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = this.getArguments();
        mActionType = bundle.getInt(KEY_ACTION_TYPE, 0);
        mAnnotationId = bundle.getInt(KEY_ANNOTATION_ID, -1);
        if (mActionType == ACTION_EDIT) {
            mTitle = bundle.getString(KEY_TITLE);
            mAddress = bundle.getString(KEY_ADDRESS);
            mDescription = bundle.getString(KEY_DESCRIPTION);
        }
        mLongitude = bundle.getDouble(KEY_LONGITUDE, 0);
        mLatitude = bundle.getDouble(KEY_LATITUDE, 0);
        mFavourite = bundle.getInt(KEY_FAVOURITE, InfoPoint.NOT_FAVOURITE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_info_point, container, false);

        mTitleInput = (EditText) view.findViewById(R.id.info_point_title_input);
        mAddressInput = (EditText) view.findViewById(R.id.info_point_address_input);
        mDescriptionInput = (EditText) view.findViewById(R.id.info_point_description_input);

        if (mActionType == ACTION_EDIT) {
            mTitleInput.setText(mTitle);
            mAddressInput.setText(mAddress);
            mDescriptionInput.setText(mDescription);
        }

        view.findViewById(R.id.info_point_cancel_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventObject event = new EventObject(EventObject.INFO_POINT_CANCEL);
                event.annotationId = mAnnotationId;
                EventBus.getDefault().post(event);
            }
        });

        Button submitButton = (Button) view.findViewById(R.id.info_point_submit_button);
        final EventObject event;
        if (mActionType == ACTION_EDIT) {
            submitButton.setText(getContext().getString(R.string.done));
            event = new EventObject(EventObject.INFO_POINT_EDIT);
        } else {
            submitButton.setText(getContext().getString(R.string.create));
            event = new EventObject(EventObject.INFO_POINT_CREATE);
        }
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mTitleInput.getText().toString().trim())) {
                    Snackbar.make(view, getContext().getString(R.string.error_title_empty), Snackbar.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(mAddressInput.getText().toString().trim())) {
                    Snackbar.make(view, getContext().getString(R.string.error_address_empty), Snackbar.LENGTH_SHORT).show();
                } else {
                    event.annotationId = mAnnotationId;
                    event.title = mTitleInput.getText().toString().trim();
                    event.address = mAddressInput.getText().toString().trim();
                    event.description = mDescriptionInput.getText().toString().trim();
                    event.longitude = mLongitude;
                    event.latitude = mLatitude;
                    event.favourite = mFavourite;
                    EventBus.getDefault().post(event);
                }
            }
        });

        return view;
    }

    public int getActionType() {
        return mActionType;
    }
}