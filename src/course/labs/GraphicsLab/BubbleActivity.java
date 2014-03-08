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

public class BubbleActivity extends Activity 
{
	private final static int RANDOM = 0;
	private final static int SINGLE = 1;
	private final static int STILL = 2;
	private static int speedMode = RANDOM;
	private static final int MENU_STILL = Menu.FIRST;
	private static final int MENU_SINGLE_SPEED = Menu.FIRST + 1;
	private static final int MENU_RANDOM_SPEED = Menu.FIRST + 2;
	private static final String TAG = "Lab-Graphics";
	private RelativeLayout mFrame;									// Main view
	private int mDisplayWidth, mDisplayHeight;						// Display dimensions
	private GestureDetector mGestureDetector;					// Gesture Detector

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mFrame = (RelativeLayout) findViewById(R.id.frame);			// Set up user interface		
	}

	@Override
	protected void onResume() 
	{
		super.onResume();
		setupGestureDetector();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) 			// Get the size of the display
	{
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) 
		{
			mDisplayWidth = mFrame.getWidth();
			mDisplayHeight = mFrame.getHeight();
		}
	}

	private void setupGestureDetector() 
	{
		mGestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener() 
		{
			// If a fling gesture starts on a touchCircle then change the touchCircle's velocity
			@Override
			public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) 
			{
				touchCircle bubble;		
				int childCount = mFrame.getChildCount();
				for (int index = 0; index < childCount; index++)
				{
					bubble = (touchCircle) mFrame.getChildAt(index);
					if(bubble.intersects(event1.getX(), event1.getY()))
					{
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
			public boolean onSingleTapConfirmed(MotionEvent event) 
			{
				touchCircle bubble;				
				int childCount = mFrame.getChildCount();
				for (int index = 0; index < childCount; index++)
				{
					bubble = (touchCircle) mFrame.getChildAt(index);
					if(bubble.intersects(event.getX(), event.getY()))
					{
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
	public boolean onTouchEvent(MotionEvent event) 
	{
		if (mGestureDetector.onTouchEvent(event)) return true; 		// delegate the touch to the gestureDetector
		else return false;
	
	}

	@Override
	protected void onPause() {
		super.onPause();
	}


	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	private class touchCircle extends View 
	{
		private static final int BITMAP_SIZE = 64;
		private static final int REFRESH_RATE = 40;
		private final Paint mPainter = new Paint();
		private ScheduledFuture<?> mMoverFuture;
		private int mCircleSize;
		private int COLOR_DEPTH = 255;
		private Random r = new Random();
		private int oldNoteValue;	//last played note to be compared with new note
		private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);// Creates a WorkerThread
		private int STEPS = 12;
		
		private double[] armenianScale = {261.626, 293.665, 329.628, 349.228, 391.995, 		//armenian tetrachord scale
				440.000, 466.164, 523.251, 587.330, 622.254, 698.456, 830.609};
		
		private float mXPos, mYPos, mDx, mDy;			// location, speed and direction of the circle

		public touchCircle(Context context, float x, float y) 
		{
			super(context);
			createScaledShape();			// Creates the bubble bitmap for this touchCircle
			mXPos = x - mCircleSize / 2;				// Adjust position to center the bubble under user's finger
			mYPos = y - mCircleSize / 2;
			setSpeedAndDirection();			// Set the touchCircle's speed and direction
			mPainter.setAntiAlias(true);
		}


		private void setSpeedAndDirection() 
		{
			mDx = r.nextInt(6)-r.nextInt(6);
			mDy = r.nextInt(6)-r.nextInt(6);
		}

		private void createScaledShape() 
		{
			mCircleSize = ((r.nextInt(7)+1) * BITMAP_SIZE);
		}

		
		private void start() 				// Start moving the touchCircle & updating the display
		{							
			// Execute the run() in Worker Thread every REFRESH_RATE milliseconds
			// Save reference to this job in mMoverFuture
			mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() 
			{
				@Override
				public void run() 
				{
					postInvalidate();					//call to update screen
					if(moveWhileOnScreen())
					{									//If the touchCircle exits the display.... 
						stop(false);					//...stop the touchCircle's Worker Thread
					}	
				}
			}, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
		}
		
		private void stop(final boolean popped) 				// This work will be performed on the UI Thread				
		{
			if (mMoverFuture != null && mMoverFuture.cancel(true)) 
			{
				mFrame.post(new Runnable() 
				{
					@Override
					public void run() 
					{	
						if (popped) 
						{
							log("Pop!");
						}
						mFrame.removeView(touchCircle.this);			//remove view
						executor.shutdown();							//shut down executor thread
					}
				});
			}
		}


		private synchronized boolean intersects(float x, float y) 
		{														// Return true if the touchCircle intersects position (x,y)
			int radius = mCircleSize/2;
			double centerX = mXPos + mCircleSize/2;
			double centerY = mYPos + mCircleSize/2;
			return Math.hypot(centerX - (double) x, centerY - (double) y) < radius;
		}

		// Change the Bubble's speed and direction
		private synchronized void deflect(float velocityX, float velocityY) 
		{
			mDx = velocityX/REFRESH_RATE;			//set mDx and mDy to be the new velocities divided by the REFRESH_RATE
			mDy = velocityY/REFRESH_RATE;
		}

		// Draw the Bubble at its current location
		// assign color and tone
		@Override
		protected synchronized void onDraw(Canvas canvas) 
		{
			try
			{
				int[] argbValues;
				int noteValue;
				
				canvas.save();				//  - save the canvas
				
				
				//TODO: change radius according to length of touch
				noteValue = getNoteValue();
				argbValues = getColorArray((float) noteValue);
		
				//set color and pitch according to location
				//x-axis : tone y-axis pitch
				
				if (oldNoteValue != noteValue)				//send screen division to playnote function to send to oscillator
				{
					playNote(noteValue);
				}
				
				mPainter.setARGB(argbValues[0],argbValues[1],argbValues[2],argbValues[3]);	//set color of circle
				canvas.drawCircle(mXPos, mYPos, mCircleSize, mPainter);			//update circle position
				oldNoteValue = noteValue;		//preserve old note value to avoid repetition
				
				canvas.restore();				// - restore the canvas
			} catch (Exception exception) {
				Log.i(TAG, exception.toString());
			}
		}

		private void playNote(int noteValue)
		{
			ToneGenerator osc = new ToneGenerator();			//create oscillator object and send note to it
			MessageObject noteInfo = new MessageObject();		//create MessageObject and assign values
			noteInfo.freq = 440;
			noteInfo.notelength = 0.25f;
			if(osc.getStatus() != null){
				
			}
			osc.execute(noteInfo);								//execute oscillator async task
		}
		
		private synchronized int getNoteValue()
		{
			int noteHeight = mDisplayHeight/STEPS;				//divide screen into 12 steps
		
			for (int step = 0; step < STEPS; step++)
			{
				if (mYPos > (step) && mYPos < (noteHeight*step))
				{
					return step;			//assign value to note depending on where it is on the screen.
				}
			}
			return mDisplayHeight/2;					//return arbitrary value if something goes wrong
		}
		
		private int[] getColorArray(float yPos)
		{
			int RED =0;
			int GREEN=0;
			int BLUE=0;
			int ALPHA=255;
			int colorSteps = COLOR_DEPTH/STEPS;
			
			RED = (int)yPos*colorSteps; 
			int returnArray[] = {ALPHA,RED,GREEN,BLUE};				//return ARGB array
			return returnArray;	
		}
		
		private synchronized boolean moveWhileOnScreen() 
		{
			if (isOutOfView())
			{				
				return true;					// Returns true if the touchCircle has exited the screen
			} else {							//move circle by mDelta x/y
				mXPos += mDx;
				mYPos += mDy;
				return false;
			}
		}

		private boolean isOutOfView() 
		{														// Return true if the touchCircle has exited the screen
			if (mXPos > mDisplayWidth || mYPos > mDisplayHeight || mXPos < 0 || mYPos < 0)
			{
				return true;
			} else {
				return false;
			}
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