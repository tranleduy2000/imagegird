package com.duy.imageoverlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.duy.imageoverlay.data.Database;
import com.duy.imageoverlay.data.GridViewSetting;
import com.duy.imageoverlay.views.GirdFrameLayout;
import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;

public class MainActivity extends AbstractAppCompatActivity {

    private static final int REQ_CHOOSE_IMAGE = 1222;
    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewHolder views;
    //    private GestureSettingsMenu settingsMenu;
    private GridViewSetting gridViewSetting;
    private Database database;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new Database(this);
        setContentView(R.layout.activity_main);

        views = new ViewHolder(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        gridViewSetting = new GridViewSetting(this,
                views.girdFrameLayout, views.mContainerControl, views.fullImage);

        gridViewSetting.setup();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        gridViewSetting.onResume();
        showHelpIfNeeded();
    }

    private void showHelpIfNeeded() {
        boolean b;
        b = database.get(Database.STARTED, false);
        if (b) return;
        ;
//        TapTarget tapTarget = TapTarget.forView(findViewById(R.id.fab_open), getString(com.duy.imageoverlay.R.string.open_file),
//                getString(com.duy.imageoverlay.R.string.open_file_desc))
//                .targetCircleColor(R.color.colorAccent)
//                .outerCircleColor(R.color.color_blue)
//                .drawShadow(true)
//                .cancelable(true)
//                .textColor(R.color.colorAccent)
//                .transparentTarget(true);
        TapTarget tapTarget1 = TapTarget.forToolbarNavigationIcon(
                (Toolbar) findViewById(R.id.toolbar), getString(R.string.option_title),
                getString(R.string.options_desc))
                .targetCircleColor(R.color.colorAccent)
                .outerCircleColor(R.color.color_blue)
                .drawShadow(true)
                .cancelable(true)
                .textColor(R.color.colorAccent)
                .transparentTarget(true);
        TapTargetSequence sequence = new TapTargetSequence(this);
        sequence.targets(tapTarget1)
                .listener(new TapTargetSequence.Listener() {
                    @Override
                    public void onSequenceFinish() {
                        database.putBool(Database.STARTED, true);
                    }

                    @Override
                    public void onSequenceCanceled(TapTarget lastTarget) {
                        database.putBool(Database.STARTED, true);

                    }
                }).start();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.open:
                pickImage(null);
                return true;
            case R.id.action_rate:
                rateApp();
                return true;
            case R.id.action_more_app:
                moreApp(null);
            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            //get uri from intent
            Uri selectedImage = data.getData();
            database.putString(Database.LASTEST_FILE, selectedImage.toString());
            gridViewSetting.onImageChange(selectedImage);
        }
    }


    /**
     * choose image path
     */
    public void pickImage(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_CHOOSE_IMAGE);
    }

    /**
     * save grid setting
     */
    @Override
    protected void onStop() {
        super.onStop();
        gridViewSetting.onPause();
    }


    public class ViewHolder {
        final SubsamplingScaleImageView fullImage;
        final GirdFrameLayout girdFrameLayout;
        final LinearLayout mContainerControl;

        ViewHolder(Activity activity) {
            fullImage = (SubsamplingScaleImageView) activity.findViewById(R.id.image_view);
            girdFrameLayout = (GirdFrameLayout) activity.findViewById(R.id.gird_view);
            mContainerControl = (LinearLayout) activity.findViewById(R.id.container_control);
        }
    }
}
