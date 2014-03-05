package course.labs.GraphicsLab;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class BubbleActivity extends Activity {

	// These variables are for testing purposes, do not modify
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;

	private static final int MENU_STILL = Menu.FIRST;
	private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
	private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;

	private static final String TAG = "Lab-Graphics";

	// Main view
	private RelativeLayout mFrame;

	// Display dimensions
	private int mDisplayWidth, mDisplayHeight;

	// Gesture Detector
	private GestureDetector mGestureDetector;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		// Set up user interface
		mFrame = (RelativeLayout) findViewById(R.id.frame);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Manage bubble popping sound
		// Use AudioManager.STREAM_MUSIC as stream type

		setupGestureDetector();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {

			// Get the size of the display so this view knows where borders are
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();

		}
	}

	// Set up GestureDetector
	private void setupGestureDetector() {

		mGestureDetector = new GestureDetector(this,
				new GestureDetector.SimpleOnGestureListener() {

			// If a fling gesture starts on a touchCircle then change the
			// touchCircle's velocity

			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2,
					float velocityX, float velocityY) {

				// TODO - Implement onFling actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method
				touchCircle bubble;
			
				int childCount = mFrame.getChildCount();
				for (int index = 0; index < childCount; index++){
					bubble = (touchCircle) mFrame.getChildAt(index);
					if(bubble.intersects(event1.getX(), event1.getY())){
						bubble.deflect(velocityX, velocityY);
						return true;
					}
				}				
				return false;
				
			}

			// If a single tap intersects a touchCircle, then pop the touchCircle
			// Otherwise, create a new touchCircle at the tap's location and add
			// it to mFrame. You can get all views from mFrame with ViewGroup.getChildAt()

			@Override
			public boolean onSingleTapConfirmed(MotionEvent event) {

				// - Implement onSingleTapConfirmed actions.
				// You can get all Views in mFrame using the
				// ViewGroup.getChildCount() method
	
				touchCircle bubble;
				
				int childCount = mFrame.getChildCount();
				for (int index = 0; index < childCount; index++){
					bubble = (touchCircle) mFrame.getChildAt(index);
					if(bubble.intersects(event.getX(), event.getY())){
						bubble.stop(true);
						return true;
					}
				}
				bubble = new touchCircle(getApplicationContext(), event.getX(), event.getY());
				bubble.start();
				mFrame.addView(bubble);
				return true;
			
			}
		});
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		// delegate the touch to the gestureDetector
		if(mGestureDetector.onTouchEvent(event)) return true;
		return false;
	
	}

	@Override
	protected void onPause() {
	
		super.onPause();
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// touchCircle is a View that displays a bubble.
	// This class handles animating, drawing, popping amongst other actions.
	// A new touchCircle is created for each bubble on the display

	private class touchCircle extends View {

		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mScaledBitmapWidth;
		private int COLOR_DEPTH = 255;
		private Random r = new Random();
		private ToneGenerator osc = new ToneGenerator();
		private int noteValue;
		private int oldNoteValue;
		MessageObject messageObject = new MessageObject();

		// location, speed and direction of the bubble
		private float mXPos, mYPos, mDx, mDy;

		public touchCircle(Context context, float x, float y) {
			super(context);
		//	log("Creating Bubble at: x:" + x + " y:" + y);

			// Create a new random number generator to
			// randomize size, rotation, speed and direction

			// Creates the bubble bitmap for this touchCircle DONE
			createScaledShape(r);
			
			// Adjust position to center the bubble under user's finger
			mXPos = x - mScaledBitmapWidth / 2;
			mYPos = y - mScaledBitmapWidth / 2;

			// Set the touchCircle's speed and direction
			setSpeedAndDirection(r);

			mPainter.setAntiAlias(true);


		}


		private void setSpeedAndDirection(Random r) {

			// Used by test cases
			switch (speedMode) {

			case SINGLE:

				// Fixed speed
				mDx = 10;
				mDy = 10;
				break;

			case STILL:

				// No speed
				mDx = 0;
				mDy = 0;
				break;

			default:

				// Set movement direction and speed
				// Limit movement speed in the x and y
				// direction to [-3..3].
				
				mDx = r.nextInt(6)-r.nextInt(6);
				mDy = r.nextInt(6)-r.nextInt(6);
		//		Log.i(TAG, mDx+" "+mDy);
			}
		}

		private void createScaledShape(Random r) {

			if (speedMode != RANDOM) {

				mScaledBitmapWidth = BITMAP_SIZE * 3;
			
			} else {
			
				//set scaled bitmap size in range [1..3] * BITMAP_SIZE
				
				mScaledBitmapWidth = ((r.nextInt(3)+1) * BITMAP_SIZE);
			
			}
			
			// create the scaled bitmap using size set above DONE
		}

		// Start moving the touchCircle & updating the display
		private void start() {

			// Creates a WorkerThread
			ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

			// Execute the run() in Worker Thread every REFRESH_RATE milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					//If the touchCircle exits the display, stop the touchCircle's Worker Thread. 
					postInvalidate();
					if(moveWhileOnScreen()){
						stop(false);
					}	
				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}

		private synchronized boolean intersects(float x, float y) {

			// Return true if the touchCircle intersects position (x,y)
			int radius = mScaledBitmapWidth/2;
			double centerX = mXPos + mScaledBitmapWidth/2;
			double centerY = mYPos + mScaledBitmapWidth/2;

			return Math.hypot(centerX - (double) x, centerY - (double) y) < radius;
		}

		// Cancel the Bubble's movement
		// Remove Bubble from mFrame
		// Play pop sound if the touchCircle was popped
		
		private void stop(final boolean popped) {

			if (null != mMoverFuture && mMoverFuture.cancel(true)) {

				// This work will be performed on the UI Thread
				
				mFrame.post(new Runnable() {
					@Override
					public void run() {
						
						if (popped) {
							log("Pop!");
						}
						osc.killAudioTrack();
						mFrame.removeView(touchCircle.this);
	
					}
				});
			}
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) {
			log("velocity X:" + velocityX + " velocity Y:" + velocityY);

			//set mDx and mDy to be the new velocities divided by the REFRESH_RATE
			
			mDx = velocityX/REFRESH_RATE;
			mDy = velocityY/REFRESH_RATE;

		}

		// Draw the Bubble at its current location
		// assign color and tone
		@Override
		protected synchronized void onDraw(Canvas canvas) {
			try{
//				long startTime = System.nanoTime();
				int circleRadius;
				int[] argbValues;
				//TODO: change radius according to length of touch
				circleRadius = 100;
				//  - save the canvas
				canvas.save();
				noteValue = getNoteValue();
				argbValues = getColorArray((float) noteValue);
				messageObject.frequency = noteValue*100;
				//set color and pitch according to location
				//x-axis : tone y-axis pitch
				
				if (oldNoteValue != noteValue){
					osc.doInBackground(messageObject);					
				}
				
				
				mPainter.setARGB(argbValues[0],argbValues[1],argbValues[2],argbValues[3]);
				canvas.drawCircle(mXPos, mYPos, circleRadius, mPainter);
				// - restore the canvas
				oldNoteValue = noteValue;
				canvas.restore();
//				long endTime = System.nanoTime();
	//			System.out.println(endTime-startTime);
			} catch (Exception exception){
				Log.i(TAG, exception.toString());
			}
			
		}

		private synchronized int getNoteValue(){
			//divide screen into 12 half steps
			int STEPS = 12;
			int noteHeight = mDisplayHeight/STEPS;
		
			//assign value to note depending on where it is on the screen.
			for (int step = 0; step < STEPS; step++){
				if (mYPos > (step) && mYPos < (noteHeight*step)){
					return step;
				}
			}
			//return arbitrary value if loop doens't work.
			return mDisplayHeight/2;
		}
		
		private int[] getColorArray(float yPos){
			int RED =0;
			int GREEN=0;
			int BLUE=0;
			int ALPHA=255;
			int colorSteps = 255/12;
			
			//return ARGB array
			RED = (int)yPos*colorSteps; 
			
			int returnArray[] = {ALPHA,RED,GREEN,BLUE};	
			return returnArray;
			
		}
		
		private synchronized boolean moveWhileOnScreen() {

			// Move the touchCircle
			// Returns true if the touchCircle has exited the screen
			if (isOutOfView()){
				return true;
			}
	//		Log.i(TAG, "MOVING: "+mXPos+" "+mYPos);
			mXPos += mDx;
			mYPos += mDy;
			
			return false;

		}

		private boolean isOutOfView() {

			// Return true if the touchCircle has exited the screen
			if (mXPos > mDisplayWidth || mYPos > mDisplayHeight || mXPos < 0 || mYPos < 0){
				return true;
			}

			return false;

		}
	}

	// Do not modify below here
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_STILL, Menu.NONE, "Still Mode");
		menu.add(Menu.NONE, MENU_SINGLE_SPEED, Menu.NONE, "Single Speed Mode");
		menu.add(Menu.NONE, MENU_RANDOM_SPEED, Menu.NONE, "Random Speed Mode");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_STILL:
			speedMode = STILL;
			return true;
		case MENU_SINGLE_SPEED:
			speedMode = SINGLE;
			return true;
		case MENU_RANDOM_SPEED:
			speedMode = RANDOM;
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private static void log (String message) {
		Log.i(TAG,message);
	}
}