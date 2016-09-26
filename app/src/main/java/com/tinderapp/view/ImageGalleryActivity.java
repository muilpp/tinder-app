package com.tinderapp.view;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

import com.squareup.picasso.Picasso;
import com.tinderapp.BuildConfig;
import com.tinderapp.R;

import java.util.ArrayList;

public class ImageGalleryActivity extends Activity {
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private ViewFlipper mViewFlipper;
    private GestureDetector detector;
    private final static String TAG = ImageGalleryActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        mViewFlipper = (ViewFlipper) this.findViewById(R.id.view_flipper);
        mViewFlipper.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                detector.onTouchEvent(event);
                return true;
            }
        });
        detector = new GestureDetector(getApplicationContext(), new SwipeGestureDetector());

        ArrayList<String> imagesList = getIntent().getStringArrayListExtra(BuildConfig.IMAGES_ARRAY);

        for (String imageURL : imagesList) {
            RelativeLayout relativeLayout = new RelativeLayout(this);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
            relativeLayout.setLayoutParams(rlp);

            ImageView imageView = new ImageView(this);
            imageView.setAdjustViewBounds(true);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setLayoutParams(rlp);

            relativeLayout.addView(imageView);
            mViewFlipper.addView(relativeLayout);

            Picasso.with(this)
                    .load(imageURL)
                    .into(imageView);
        }
    }

    class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_right));
//                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_to_right));
                    mViewFlipper.showNext();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_in_from_left));
//                    mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(mContext, R.anim.slide_out_to_left));
                    mViewFlipper.showPrevious();
                    return true;
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }

            return false;
        }
    }
}