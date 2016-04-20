package com.example.arthur.skdemo.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.arthur.skdemo.R;
import com.example.arthur.skdemo.data.SKApplication;
import com.example.arthur.skdemo.data.model.EventObject;
import com.example.arthur.skdemo.data.model.InfoPoint;
import com.example.arthur.skdemo.networking.DatabaseManager;
import com.example.arthur.skdemo.ui.infopoint.InfoPointFragment;
import com.example.arthur.skdemo.ui.infopoint.InfoPointListFragment;
import com.example.arthur.skdemo.ui.mapstyle.MapStyleFragment;
import com.skobbler.ngx.SKCoordinate;
import com.skobbler.ngx.SKMaps;
import com.skobbler.ngx.map.SKAnimationSettings;
import com.skobbler.ngx.map.SKAnnotation;
import com.skobbler.ngx.map.SKCalloutView;
import com.skobbler.ngx.map.SKCoordinateRegion;
import com.skobbler.ngx.map.SKMapCustomPOI;
import com.skobbler.ngx.map.SKMapPOI;
import com.skobbler.ngx.map.SKMapScaleView;
import com.skobbler.ngx.map.SKMapSurfaceListener;
import com.skobbler.ngx.map.SKMapSurfaceView;
import com.skobbler.ngx.map.SKMapViewHolder;
import com.skobbler.ngx.map.SKMapViewStyle;
import com.skobbler.ngx.map.SKPOICluster;
import com.skobbler.ngx.map.SKScreenPoint;
import com.skobbler.ngx.positioner.SKCurrentPositionListener;
import com.skobbler.ngx.positioner.SKCurrentPositionProvider;
import com.skobbler.ngx.positioner.SKPosition;

import java.util.List;
import java.util.Random;

import de.greenrobot.event.EventBus;

/**
 * Created by Arthur on 4/19/2016.
 *
 */
public class MapActivity extends AppCompatActivity implements SKMapSurfaceListener, SKCurrentPositionListener {

    private static final int ACCESS_FINE_LOCATION = 0;

    //region MEMBERS
    private SKMapSurfaceView mMapView;

    private SKMapViewHolder mMapHolder;
    private View mInfoPointFormHolder;
    private View mMapStyleHolder;
    private View mInfoPointListHolder;

    private SKCurrentPositionProvider mSkCurrentPositionProvider;

    private DatabaseManager mDataBase;

    private InfoPointFragment mInfoPointFragment = null;
    private MapStyleFragment mMapStyleFragment = null;
    private InfoPointListFragment mInfoPointListFragment = null;

    private SKAnnotation mAnnotation = null;

    private List<InfoPoint> mInfoPointList;

    private int mCurrentMapStyle;
    //endregion

    //region LIFECYCLE CALLBACKS
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mDataBase = ((SKApplication) getApplication()).getDataBase();

        mInfoPointFormHolder = findViewById(R.id.map_info_point_root);
        mMapStyleHolder = findViewById(R.id.map_style_settings_root);
        mInfoPointListHolder = findViewById(R.id.map_info_point_list_root);

        mMapHolder = (SKMapViewHolder) findViewById(R.id.map_surface_holder);
        if (mMapHolder != null) {
            mMapHolder.setMapSurfaceListener(this);
        }

        mCurrentMapStyle = ((SKApplication) getApplicationContext()).getSharedPreferences().getIntPreference(MapStyleFragment.KEY_MAP_STYLE, MapStyleFragment.MAP_STYLE_DAY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapHolder.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapHolder.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onBackPressed() {
        if (mInfoPointFragment != null) {
            if (mInfoPointFragment.getActionType() == InfoPointFragment.ACTION_CREATE) {
                mMapView.deleteAnnotation(mAnnotation.getUniqueID());
            }
            removeInfoPointCreatorFragment();
        } else if (mMapStyleFragment != null) {
            removeMapStyleSelectorFragment();
        } else if (mInfoPointListFragment != null) {
            removeInfoPointListFragment();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mSkCurrentPositionProvider.requestLocationUpdates(false, true, false);
                    mSkCurrentPositionProvider.requestUpdateFromLastPosition();

                }
            }
        }
    }
    //endregion

    //region MENU
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                removeInfoPointCreatorFragment();
                removeInfoPointListFragment();
                mMapHolder.setVisibility(View.GONE);
                mInfoPointFormHolder.setVisibility(View.GONE);
                mInfoPointListHolder.setVisibility(View.GONE);
                mMapStyleHolder.setVisibility(View.VISIBLE);
                mMapStyleFragment = new MapStyleFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(MapStyleFragment.KEY_MAP_STYLE, mCurrentMapStyle);
                mMapStyleFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.map_style_settings_root, mMapStyleFragment).commit();
                getSupportFragmentManager().executePendingTransactions();
                return true;
            case R.id.list:
                removeInfoPointCreatorFragment();
                removeMapStyleSelectorFragment();
                mMapHolder.setVisibility(View.GONE);
                mInfoPointFormHolder.setVisibility(View.GONE);
                mMapStyleHolder.setVisibility(View.GONE);
                mInfoPointListHolder.setVisibility(View.VISIBLE);
                mInfoPointListFragment = new InfoPointListFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.map_info_point_list_root, mInfoPointListFragment).commit();
                getSupportFragmentManager().executePendingTransactions();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion

    //region SKCurrentPositionListener CALLBACKS
    @Override
    public void onCurrentPositionUpdate(SKPosition skPosition) {
        mMapView.centerMapOnPositionSmooth(skPosition.getCoordinate(), 3000);
        mMapView.setPositionAsCurrent(skPosition.getCoordinate(), 0, true);
        mSkCurrentPositionProvider.stopLocationUpdates();
    }
    //endregion

    //region SKMapSurfaceListener CALLBACKS
    @Override
    public void onSurfaceCreated(SKMapViewHolder skMapViewHolder) {
        mMapView = mMapHolder.getMapSurfaceView();
        setMapStyle();
        mSkCurrentPositionProvider = new SKCurrentPositionProvider(this);
        mSkCurrentPositionProvider.setCurrentPositionListener(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mSkCurrentPositionProvider.requestLocationUpdates(false, true, false);
            mSkCurrentPositionProvider.requestUpdateFromLastPosition();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
        }

        drawCompass();
        drawScale();
        drawInfoPoints();

    }

    @Override
    public void onActionPan() {

    }

    @Override
    public void onActionZoom() {

    }

    @Override
    public void onMapRegionChanged(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onMapRegionChangeStarted(SKCoordinateRegion skCoordinateRegion) {
    }

    @Override
    public void onMapRegionChangeEnded(SKCoordinateRegion skCoordinateRegion) {

    }

    @Override
    public void onDoubleTap(SKScreenPoint skScreenPoint) {
    }

    @Override
    public void onSingleTap(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onRotateMap() {

    }

    @Override
    public void onLongPress(SKScreenPoint skScreenPoint) {
        final SKCoordinate skCoordinate = mMapView.pointToCoordinate(skScreenPoint);

        // Generate random annotation ID
        int annotationId = new Random().nextInt();
        while (!isIdAvailable(annotationId)) {
            annotationId = new Random().nextInt();
        }
        mAnnotation = new SKAnnotation(annotationId);
        mAnnotation.setLocation(skCoordinate);
        mAnnotation.setMininumZoomLevel(5);
        mAnnotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
        mMapView.addAnnotation(mAnnotation, SKAnimationSettings.ANIMATION_PIN_DROP);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mMapHolder.setVisibility(View.GONE);
                mInfoPointFormHolder.setVisibility(View.VISIBLE);
                mInfoPointFragment = new InfoPointFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(InfoPointFragment.KEY_ACTION_TYPE, InfoPointFragment.ACTION_CREATE);
                bundle.putInt(InfoPointFragment.KEY_ANNOTATION_ID, mAnnotation.getUniqueID());
                bundle.putDouble(InfoPointFragment.KEY_LONGITUDE, skCoordinate.getLongitude());
                bundle.putDouble(InfoPointFragment.KEY_LATITUDE, skCoordinate.getLatitude());
                bundle.putInt(InfoPointFragment.KEY_FAVOURITE, InfoPoint.NOT_FAVOURITE);
                mInfoPointFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().add(R.id.map_info_point_root, mInfoPointFragment).commit();
                getSupportFragmentManager().executePendingTransactions();
            }
        }, 1000);

    }

    @Override
    public void onInternetConnectionNeeded() {

    }

    @Override
    public void onMapActionDown(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onMapActionUp(SKScreenPoint skScreenPoint) {

    }

    @Override
    public void onPOIClusterSelected(SKPOICluster skpoiCluster) {

    }

    @Override
    public void onMapPOISelected(SKMapPOI skMapPOI) {

    }

    @Override
    public void onAnnotationSelected(SKAnnotation skAnnotation) {
        final SKCalloutView mapPopup = mMapHolder.getCalloutView();
        View view = LayoutInflater.from(this).inflate(R.layout.popup_info_point, null);
        mapPopup.setCustomView(view);
        final InfoPoint infoPoint = getInfoPointById(skAnnotation.getUniqueID());
        if (infoPoint != null) {
            mapPopup.setVisibility(View.VISIBLE);

            ((TextView) view.findViewById(R.id.popup_title)).setText(infoPoint.title);

            view.findViewById(R.id.popup_view_details_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapPopup.hide();
                    mMapHolder.setVisibility(View.GONE);
                    mInfoPointFormHolder.setVisibility(View.VISIBLE);
                    mInfoPointFragment = new InfoPointFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(InfoPointFragment.KEY_ACTION_TYPE, InfoPointFragment.ACTION_EDIT);
                    bundle.putInt(InfoPointFragment.KEY_ANNOTATION_ID, infoPoint.annotationId);
                    bundle.putString(InfoPointFragment.KEY_TITLE, infoPoint.title);
                    bundle.putString(InfoPointFragment.KEY_ADDRESS, infoPoint.address);
                    bundle.putString(InfoPointFragment.KEY_DESCRIPTION, infoPoint.description);
                    bundle.putDouble(InfoPointFragment.KEY_LONGITUDE, infoPoint.longitude);
                    bundle.putDouble(InfoPointFragment.KEY_LATITUDE, infoPoint.latitude);
                    bundle.putInt(InfoPointFragment.KEY_FAVOURITE, infoPoint.favourite);
                    mInfoPointFragment.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction().add(R.id.map_info_point_root, mInfoPointFragment).commit();
                    getSupportFragmentManager().executePendingTransactions();
                }
            });
            view.findViewById(R.id.popup_remove_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapPopup.hide();
                    mMapView.deleteAnnotation(infoPoint.annotationId);
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            mDataBase.deleteInfoPoint(infoPoint.annotationId);
                            return null;
                        }
                    }.execute();
                }
            });
            view.findViewById(R.id.popup_cancel_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapPopup.hide();
                }
            });
        }

    }

    @Override
    public void onCustomPOISelected(SKMapCustomPOI skMapCustomPOI) {

    }

    @Override
    public void onCompassSelected() {

    }

    @Override
    public void onCurrentPositionSelected() {

    }

    @Override
    public void onObjectSelected(int i) {

    }

    @Override
    public void onInternationalisationCalled(int i) {

    }

    @Override
    public void onBoundingBoxImageRendered(int i) {

    }

    @Override
    public void onGLInitializationError(String s) {

    }

    @Override
    public void onScreenshotReady(Bitmap bitmap) {

    }
    //endregion

    //region UI ELEMENTS
    private void drawCompass() {

        mMapView.getMapSettings().setCompassPosition(new SKScreenPoint(-70, -50));  // right top corner
        mMapView.getMapSettings().setCompassShown(true);
    }

    private void drawScale() {

        mMapHolder.setScaleViewEnabled(true);
        mMapHolder.setScaleViewPosition(0, 80, RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.ALIGN_PARENT_BOTTOM);
        // get the map scale object from the map holder object that contains it
        SKMapScaleView scaleView = mMapHolder.getScaleView();
        scaleView.setLighterColor(Color.argb(255, 255, 200, 200));
        // disable fade out animation on the scale view
        scaleView.setFadeOutEnabled(false);
        scaleView.setDistanceUnit(SKMaps.SKDistanceUnitType.DISTANCE_UNIT_KILOMETER_METERS);
    }

    private void drawInfoPoints() {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... params) {
                mInfoPointList = mDataBase.getAllInfoPoints();
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                for (InfoPoint infoPoint : mInfoPointList) {
                    placeAnnotation(infoPoint);
                }
            }
        }.execute();

    }
    //endregion

    //region EVENT BUS
    public void onEvent(EventObject event) {
        if (event.tag == EventObject.INFO_POINT_CANCEL) {

            if (mInfoPointFragment.getActionType() == InfoPointFragment.ACTION_CREATE) {
                mMapView.deleteAnnotation(event.annotationId);
            }
            removeInfoPointCreatorFragment();
        } else if (event.tag == EventObject.INFO_POINT_CREATE) {

            removeInfoPointCreatorFragment();
            final InfoPoint infoPoint = new InfoPoint(event.annotationId, event.title, event.address, event.description, event.longitude, event.latitude, event.favourite);
            mInfoPointList.add(infoPoint);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    mDataBase.insertInfoPoint(infoPoint);
                    return null;
                }
            }.execute();
            Snackbar.make(mInfoPointFormHolder, getString(R.string.infopoint_created), Snackbar.LENGTH_LONG).show();
        } else if (event.tag == EventObject.INFO_POINT_EDIT) {

            removeInfoPointCreatorFragment();
            final InfoPoint infoPoint = getInfoPointById(event.annotationId);
            if (infoPoint != null) {
                infoPoint.title = event.title;
                infoPoint.address = event.address;
                infoPoint.description = event.description;
                infoPoint.longitude = event.longitude;
                infoPoint.latitude = event.latitude;
                infoPoint.favourite = event.favourite;
            }
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... params) {
                    mDataBase.updateInfoPoint(infoPoint);
                    return null;
                }
            }.execute();
        } else if (event.tag == EventObject.MAP_STYLE_CANCEL) {

            removeMapStyleSelectorFragment();
        } else if (event.tag == EventObject.MAP_STYLE_SUBMIT) {

            removeMapStyleSelectorFragment();
            if (mCurrentMapStyle != event.mapStyle) {
                mCurrentMapStyle = event.mapStyle;
                ((SKApplication) getApplicationContext()).getSharedPreferences().saveIntPreference(MapStyleFragment.KEY_MAP_STYLE, mCurrentMapStyle);
                setMapStyle();
            }
        }
    }
    //endregion

    //region OTHER UTILITIES
    private void removeInfoPointCreatorFragment() {
        if (mInfoPointFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mInfoPointFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            mInfoPointFragment = null;
            mMapHolder.setVisibility(View.VISIBLE);
        }
    }

    private void removeMapStyleSelectorFragment() {
        if (mMapStyleFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mMapStyleFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            mMapStyleFragment = null;
            mMapHolder.setVisibility(View.VISIBLE);
        }
    }

    private void removeInfoPointListFragment() {
        if (mInfoPointListFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(mInfoPointListFragment).commit();
            getSupportFragmentManager().executePendingTransactions();
            mInfoPointListFragment = null;
            mMapHolder.setVisibility(View.VISIBLE);
        }
    }

    private void setMapStyle() {
        switch (mCurrentMapStyle) {
            case MapStyleFragment.MAP_STYLE_NIGHT:
                mMapView.getMapSettings().setMapStyle(new SKMapViewStyle(((SKApplication) getApplicationContext()).getMapResourcesDirPath() + getString(R.string.night_dir),  getString(R.string.night_file)));
                break;
            case MapStyleFragment.MAP_STYLE_GRAYSCALE:
                mMapView.getMapSettings().setMapStyle(new SKMapViewStyle(((SKApplication) getApplicationContext()).getMapResourcesDirPath() + getString(R.string.grayscale_dir), getString(R.string.grayscale_file)));
                break;
            case MapStyleFragment.MAP_STYLE_OUTDOOR:
                mMapView.getMapSettings().setMapStyle(new SKMapViewStyle(((SKApplication) getApplicationContext()).getMapResourcesDirPath() + getString(R.string.outdoor_dir), getString(R.string.outdoor_file)));
                break;
            default:
                mMapView.getMapSettings().setMapStyle(new SKMapViewStyle(((SKApplication) getApplicationContext()).getMapResourcesDirPath() + getString(R.string.day_dir), getString(R.string.day_file)));
                break;
        }
    }

    private InfoPoint getInfoPointById(int id) {
        for (InfoPoint infoPoint : mInfoPointList) {
            if (infoPoint.annotationId == id) {
                return infoPoint;
            }
        }
        return null;
    }

    private boolean isIdAvailable(int id) {
        boolean exists = false;
        for (InfoPoint infoPoint : mInfoPointList) {
            if (infoPoint.annotationId == id) {
                exists = true;
            }
        }
        return !exists;
    }

    private void placeAnnotation(InfoPoint infoPoint) {
        SKAnnotation annotation = new SKAnnotation(infoPoint.annotationId);
        annotation.setLocation(new SKCoordinate(infoPoint.longitude, infoPoint.latitude));
        annotation.setMininumZoomLevel(5);
        annotation.setAnnotationType(SKAnnotation.SK_ANNOTATION_TYPE_MARKER);
        mMapView.addAnnotation(annotation, SKAnimationSettings.ANIMATION_PIN_DROP);
    }

    public List<InfoPoint> getInfoPointList() {
        return mInfoPointList;
    }
    //endregion
}
