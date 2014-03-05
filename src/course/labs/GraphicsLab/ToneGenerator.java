package course.labs.GraphicsLab;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ToneGenerator extends AsyncTask{
	private int amp = 10000;
	private double twopi = 8.*Math.atan(1.);
	private double fr = 440.f;
	private double ph = 0.0;
	private int SAMPLE_RATE;
	private int BUFF_SIZE;
	private AudioTrack audioTrack;
	
	public ToneGenerator(){
		//get buffer size.
		SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		BUFF_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		//create an audiotrack object		
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, 
        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFF_SIZE, AudioTrack.MODE_STREAM);
		
	}
	
	public void oscillator(double freq, int playLength){
		//assign incoming freq value to 'fr' and use to generate a sin wave
		fr =  freq;		
		short samples[] = new short[BUFF_SIZE];
		int playedSamples = 0;
		System.out.println(playLength);
		
		audioTrack.play();
		while (playedSamples < playLength){
			for(int i=0; i < BUFF_SIZE; i++){
				samples[i] = (short) (amp*Math.sin(ph));
				ph += twopi*fr/SAMPLE_RATE;
			}
			audioTrack.write(samples, 0, BUFF_SIZE);
			playedSamples += BUFF_SIZE;
//			System.out.println(playedSamples);
		}
		//System.out.println(BUFF_SIZE);
		audioTrack.stop();
	}

	public void killAudioTrack(){
		audioTrack.release();
	}

	@Override
	protected Object doInBackground(Object... params) {
		// TODO Auto-generated method stub
		oscillator(500, 44100);
		return null;
	}
}