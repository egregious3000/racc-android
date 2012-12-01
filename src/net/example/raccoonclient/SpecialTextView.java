package net.example.raccoonclient;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

// Ignore this file for now.  It was an attempt to get flinging to work.

public class SpecialTextView extends LinearLayout {

    private final String TAG = "SPECIALVIEW";
    
    public SpecialTextView(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    
    public SpecialTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // disabled for 2.2.2
  //  public SpecialTextView(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
   // }

    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);
    }

    GestureDetector mGestureDetector;
    
    public class OnSwipeTouchListener implements OnTouchListener {
        
//        private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());
  
            private final GestureDetector gestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {

              @Override
              public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                          float distanceX, float distanceY) {
                  // beware, it can scroll to infinity
                  scrollBy((int)distanceX, (int)distanceY);
                  return true;
              }

              @Override
              public boolean onFling(MotionEvent e1, MotionEvent e2, float vX, float vY) {
                  //mScroller.fling(getScrollX(), getScrollY(),
                     //     -(int)vX, -(int)vY, 0, (int)mMaxScrollX, 0, (int)mMaxScrollY);
          //        invalidate(); // don't remember if it's needed
            //      return true;
                  Log.e(TAG, "that was a fling");
                  return false;
                  // return true;
              }

              @Override
              public boolean onDown(MotionEvent e) {
//                  if(!mScroller.isFinished() ) { // is flinging
  //                    mScroller.forceFinished(true); // to stop flinging on touch
     //             }
                  Log.e(TAG, "ondown");
                  return false;
//                  return true; // else won't work
              }

          });
          public boolean onTouch(final View v, final MotionEvent event) {
              return gestureDetector.onTouchEvent(event);
          }

          private final class GestureListener extends SimpleOnGestureListener {

              private static final int SWIPE_THRESHOLD = 100;
              private static final int SWIPE_VELOCITY_THRESHOLD = 100;

              @Override
              public boolean onDown(MotionEvent e) {
                  return true;
              }


  /*
              @Override
              public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                          float distanceX, float distanceY) {
                  // beware, it can scroll to infinity
                  scrollBy((int)distanceX, (int)distanceY);
                  return true;
              }
  */
              
              @Override
              public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                  boolean result = false;
                  try {
//                    float diffY = e2.getY() - e1.getY();
                      float diffX = e2.getX() - e1.getX();
                      // if (Math.abs(diffX) > Math.abs(diffY)) 
                      {
                          if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                              if (diffX > 0) {
                                  onSwipeRight();
                                  result = true;
                              } else {
                                  onSwipeLeft();
                                  result = true;
                              }
                          }
                      } /* else {
                          if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                              if (diffY > 0) {
                                  onSwipeBottom();
                              } else {
                                  onSwipeTop();
                              }
                          }
                      } */
                  } catch (Exception exception) {
                      exception.printStackTrace();
                  }
                  Log.e(TAG, "returning " + result);
                  return result;
              }
          
      
              public void onSwipeRight() {
                  Log.e(TAG, "RIGHT");
              }
      
              public void onSwipeLeft() {
                  Log.e(TAG, "LEFT");
              }
      
              public void onSwipeTop() {
                  Log.e(TAG, "TOP");
              }
      
              public void onSwipeBottom() {
                  Log.e(TAG, "BOTTOM");
              }
          }
      }


    OnSwipeTouchListener larry = new OnSwipeTouchListener();
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
  //      Log.e(TAG, "in here");
        //Call super first because it does some hidden motion event handling
        boolean result = super.onInterceptTouchEvent(ev);
        if (larry.gestureDetector.onTouchEvent(ev)) return true;
        return result;
    }
    
}
