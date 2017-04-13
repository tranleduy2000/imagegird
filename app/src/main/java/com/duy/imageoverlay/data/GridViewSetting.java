package com.duy.imageoverlay.data;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.alexvasilkov.gestures.Settings;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.duy.imageoverlay.R;
import com.duy.imageoverlay.adapters.IntervalAdapter;
import com.duy.imageoverlay.adapters.ModeAdapter;
import com.duy.imageoverlay.views.GirdFrameLayout;
import com.duy.imageoverlay.views.ImageUtils;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.OnColorSelectedListener;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.IOException;

/**
 * Created by Duy on 17-Feb-17.
 */

public class GridViewSetting implements View.OnClickListener {
    private static final String TAG = GridViewSetting.class.getSimpleName();
    private static final float OVERSCROLL = 32f;
    private static final long SLOW_ANIMATIONS = 1000L;

    private final Database mDatabase;
    private final Activity mActivity;
    private final ViewGroup mContainerSetting;
    private final SubsamplingScaleImageView photoView;
    private OnImageListener imageListener;
    private GirdFrameLayout mGridLayout;
    private Spinner spinnerColor, spinnerInterval, spinnerMode;
    private SwitchCompat showVerticalSwitch;
    private SwitchCompat showHorizontalSwitch;
    private SwitchCompat enabledSwitch;
    private Button btnChooseColor;
    private View mColorPreview;
    private Spinner spinnerSizePaint;
    private View mBackgroundReview;
    private Uri uriOriginalImage;
    private boolean isPanEnabled = true;
    private boolean isZoomEnabled = true;
    private boolean isRotationEnabled = false;
    private boolean isRestrictRotation = false;
    private boolean isOverscrollXEnabled = false;
    private boolean isOverscrollYEnabled = false;

    private boolean isOverzoomEnabled = true;

    private boolean isExitEnabled = false;

    private boolean isFillViewport = true;

    private Settings.Fit fitMethod = Settings.Fit.INSIDE;

    private int gravity = Gravity.CENTER;

    private boolean isSlow = false;

    private TypeFilter typeFilter = TypeFilter.NORMAL;
    private Spinner spinnerFilters;

    public GridViewSetting(Activity mActivity,
                           GirdFrameLayout girdFrameLayout,
                           ViewGroup mContainerView1,
                           SubsamplingScaleImageView photoView) {
        this.mActivity = mActivity;
        this.mContainerSetting = mContainerView1;
        this.photoView = photoView;
        this.mGridLayout = girdFrameLayout;
        this.mDatabase = new Database(mActivity);
    }

    public void setup() {
        setupModesSpinner();
        setupIntervalSpinner();
        setupColorLines();
        setupShowVerticalSwitch();
        setupShowHorizontalSwitch();
        setupEnabledSwitch();
        setupSizePaint();
        setupBackground();
        setupFilter();
    }

    private void setupFilter() {
        spinnerFilters = (Spinner) findViewById(R.id.spinner_filter);
        spinnerFilters.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    typeFilter = TypeFilter.NORMAL;
                else
                    typeFilter = TypeFilter.BW;
                if (uriOriginalImage != null) {
                    new LoadImageTask().execute(uriOriginalImage);
                } else {
                    Toast.makeText(mActivity, R.string.msg_select_img, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerFilters.setSelection(mDatabase.getInt(Database.FILTER_TYPE, 0));
    }


    private void addSwitch(boolean checked, int titleId) {
        float pixels = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, mActivity.getResources().getDisplayMetrics());

        SwitchCompat item = new SwitchCompat(mActivity);
        item.setChecked(checked);
        item.setText(titleId);
        item.setId(titleId);
        item.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        item.setOnClickListener(this);
        item.setMinHeight((int) pixels);
        mContainerSetting.addView(item);
    }

    @Override
    public void onClick(View view) {
        Log.d(TAG, "onClick: " + view.toString());
        switch (view.getId()) {
            case R.string.menu_enable_pan:
                isPanEnabled = !isPanEnabled;
                break;
            case R.string.menu_enable_zoom:
                isZoomEnabled = !isZoomEnabled;
                break;
            case R.string.menu_enable_rotation:
                isRotationEnabled = !isRotationEnabled;
                break;
            case R.string.menu_restrict_rotation:
                isRestrictRotation = !isRestrictRotation;
                break;
            case R.string.menu_enable_overscroll_x:
                isOverscrollXEnabled = !isOverscrollXEnabled;
                break;
            case R.string.menu_enable_overscroll_y:
                isOverscrollYEnabled = !isOverscrollYEnabled;
                break;
            case R.string.menu_enable_overzoom:
                isOverzoomEnabled = !isOverzoomEnabled;
                break;
            case R.string.menu_enable_exit:
                isExitEnabled = !isExitEnabled;
                break;
            case R.string.menu_fill_viewport:
                isFillViewport = !isFillViewport;
                break;
            case R.string.menu_enable_slow:
                isSlow = !isSlow;
                break;
            default:
                break;
        }
        onSetupGestureView(photoView);
    }

    public void onSetupGestureView(SubsamplingScaleImageView view) {
    }

    /**
     * set event for set background
     */
    private void setupBackground() {
        mBackgroundReview = findViewById(R.id.color_bg);
        int color = mDatabase.getInt(Database.COLOR_BACKGROUND, Color.WHITE);
        mBackgroundReview.setBackgroundColor(color);
        mGridLayout.setBackgroundColor(color);

        findViewById(R.id.btn_color_bg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ColorPickerDialogBuilder
                        .with(mActivity)
                        .setTitle("Choose color")
                        .initialColor(mDatabase.getInt(Database.COLOR_BACKGROUND, Color.WHITE))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {

                            @Override
                            public void onColorSelected(int selectedColor) {
                                Log.d(TAG, "onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("OK", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mGridLayout.setBackgroundColor(selectedColor);
                                mDatabase.putInt(Database.COLOR_BACKGROUND, selectedColor);
                                mBackgroundReview.setBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    /**
     * set data for spinner mode
     */
    private void setupModesSpinner() {
        spinnerMode = (Spinner) findViewById(R.id.debug_mode);
        final ModeAdapter adapter = new ModeAdapter(mActivity);
        spinnerMode.setAdapter(adapter);
        spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mGridLayout.setMode(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //top-left
        spinnerMode.setSelection(mDatabase.getInt(Database.MODE_POSITION, 1));
    }

    private View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    private void setupIntervalSpinner() {
        spinnerInterval = (Spinner) findViewById(R.id.debug_interval);
        final IntervalAdapter adapter = new IntervalAdapter(mActivity);
        spinnerInterval.setAdapter(adapter);
        spinnerInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mGridLayout.setIntervalDp(adapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinnerInterval.setSelection(mDatabase.getInt(Database.INTERVAL_POSITION, 2));

    }

    private void setupColorLines() {
        mColorPreview = findViewById(R.id.color_preview);
        int color = mDatabase.getInt(Database.COLOR_LINE, Color.BLACK);
        mColorPreview.setBackgroundColor(color);
        mGridLayout.setColor(color);
        btnChooseColor = (Button) findViewById(R.id.btn_choose_color);
        btnChooseColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                ColorPickerDialogBuilder
                        .with(mActivity)
                        .setTitle("Choose color")
                        .initialColor(mDatabase.getInt(Database.COLOR_LINE, Color.WHITE))
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(12)
                        .setOnColorSelectedListener(new OnColorSelectedListener() {

                            @Override
                            public void onColorSelected(int selectedColor) {
                                Log.d(TAG, "onColorSelected: 0x" + Integer.toHexString(selectedColor));
                            }
                        })
                        .setPositiveButton("OK", new ColorPickerClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                                mGridLayout.setColor(selectedColor);
                                mDatabase.putInt(Database.COLOR_LINE, selectedColor);
                                mColorPreview.setBackgroundColor(selectedColor);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .build()
                        .show();
            }
        });
    }

    private void setupShowVerticalSwitch() {
        showVerticalSwitch = (SwitchCompat) findViewById(R.id.debug_show_vertical);
        showVerticalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGridLayout.setDrawVerticalLines(isChecked);
            }
        });
        showVerticalSwitch.setChecked(mDatabase.get(Database.DRAW_VERTICAL_LINE, true));
    }

    private void setupShowHorizontalSwitch() {
        showHorizontalSwitch = (SwitchCompat) findViewById(R.id.debug_show_horizontal);
        showHorizontalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGridLayout.setDrawHorizontalLines(isChecked);
            }
        });
        showHorizontalSwitch.setChecked(mDatabase.get(Database.DRAW_HORIZONTAL_LINE, true));

    }

    private void setupEnabledSwitch() {
        enabledSwitch = (SwitchCompat) findViewById(R.id.debug_enabled);
        enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mGridLayout.setEnabled(isChecked);
            }
        });
    }

    /**
     * save data to storage
     */
    public void onPause() {
        /**
         * recycle bitmap
         */
//        originalBitmap = null;
        mDatabase.putInt(Database.MODE_POSITION, spinnerMode.getSelectedItemPosition());
        mDatabase.putInt(Database.INTERVAL_POSITION, spinnerInterval.getSelectedItemPosition());
        mDatabase.putBool(Database.DRAW_VERTICAL_LINE, showVerticalSwitch.isChecked());
        mDatabase.putBool(Database.DRAW_HORIZONTAL_LINE, showHorizontalSwitch.isChecked());
        mDatabase.putString(Database.PAINT_SIZE_POSITION, spinnerSizePaint.getSelectedItem().toString());
        if (uriOriginalImage != null)
            mDatabase.putString(Database.LASTEST_FILE, uriOriginalImage.toString());
    }

    private void setupSizePaint() {
        spinnerSizePaint = (Spinner) findViewById(R.id.spinner_size_paint);
        final String[] arr = mActivity.getResources().getStringArray(R.array.size_paint);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(mActivity, android.R.layout.simple_list_item_1, arr);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerSizePaint.setAdapter(arrayAdapter);
        spinnerSizePaint.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemSelected: " + arr[position]);
                mGridLayout.setPaintWidth(Integer.parseInt(arr[position]));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
//        int position = mDatabase.getInt(Database.PAINT_SIZE_POSITION, 0);
//        Log.d(TAG, "setupSizePaint: " + position);
//        spinnerSizePaint.setSelection(position);
    }

    public OnImageListener getImageListener() {
        return imageListener;
    }

    public void setImageListener(OnImageListener imageListener) {
        this.imageListener = imageListener;
    }

    /**
     * reload image
     *
     * @param uri
     */
    public void onImageChange(Uri uri) {
        Log.d(TAG, "onImageChange: uri" + uri.toString());
        this.uriOriginalImage = uri;
        new LoadImageTask().execute(uri);
//        photoView.setImage(ImageSource.uri(uri));
    }

    private Bitmap createImageWithFilter(Bitmap bitmap) {
        if (typeFilter == TypeFilter.NORMAL) {
            return bitmap;
        } else {
//            Filter myFilter = new Filter();
//            myFilter.addSubFilter(new BrightnessSubfilter(30));
//            myFilter.addSubFilter(new ContrastSubfilter(1.1f));
//            Bitmap outputImage = myFilter.processFilter(bitmap);
//            return outputImage;
            return bitmap;
        }
    }

    public void onImageChange(String path) {
        try {
            File file = new File(path);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            bitmap = createImageWithFilter(bitmap);
//            this.originalBitmap = bitmap;
            photoView.setImage(ImageSource.bitmap(bitmap));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * this method used to load image when resume activity or restart app
     */
    public void onResume() {
//        if (!mDatabase.getString(Database.LASTEST_FILE).isEmpty()) {
//            try {
//                uriOriginalImage = Uri.parse(mDatabase.getString(Database.LASTEST_FILE));
//                new LoadImageTask().execute(uriOriginalImage);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
    }


    private enum TypeFilter {
        NORMAL, BW
    }


    /**
     * class load image
     */
    public class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mActivity);
            dialog.setMessage("Loading");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(Uri... params) {
            Log.d(TAG, "doInBackground: ");
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), params[0]);
                if (typeFilter == TypeFilter.NORMAL) {
                    return bitmap;
                } else {
                    return ImageUtils.createBlackAndWhite(bitmap);
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                photoView.setImage(ImageSource.bitmap(bitmap));
            } else {
                Toast.makeText(mActivity, "Failed", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            dialog.dismiss();
        }

        @Override
        protected void onCancelled(Bitmap bitmap) {
            super.onCancelled(bitmap);
            dialog.dismiss();
        }
    }


}
