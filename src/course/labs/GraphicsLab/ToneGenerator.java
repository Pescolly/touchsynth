package course.labs.GraphicsLab;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;

public class ToneGenerator extends AsyncTask {
	private int amp = 10000;
	private double twopi = 8.*Math.atan(1.);
	private double fr = 440.f;
	private double ph = 0.0;
	private int sr = 44100;
	private int BUFF_SIZE;
	private AudioTrack audioTrack;
	
	public ToneGenerator(){
		BUFF_SIZE = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*5;
		
		//create an audiotrack object
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sr, 
        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFF_SIZE, AudioTrack.MODE_STREAM);
		
	}
	
	public void oscillator(double freq){
		//assign incoming freq value to 'fr' and use to generate a sin wave
		fr =  freq;		
		short samples[] = new short[BUFF_SIZE];
		audioTrack.play();
		for(int i=0; i < BUFF_SIZE; i++){
			samples[i] = (short) (amp*Math.sin(ph));
			ph += twopi*fr/sr;
		}
		//System.out.println(BUFF_SIZE);
		audioTrack.write(samples, 0, BUFF_SIZE);
		audioTrack.stop();
	}

	public void killAudioTrack(){
		audioTrack.release();
	}
	
	@Override
	protected Object doInBackground(Object... params) {
		// get message object and pull out frequency from frequency parameter.
		//send frequency to oscillator method.
		MessageObject incomingMessage;
		incomingMessage = (MessageObject) params[0];
		oscillator(incomingMessage.frequency);
		
		return null;
	}
}