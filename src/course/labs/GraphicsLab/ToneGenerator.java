package course.labs.GraphicsLab;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

public class ToneGenerator extends AsyncTask<MessageObject, MessageObject, Void>
{
	private int amplitude;
	private double TWO_PI = 8.*Math.atan(1.);
	private double phase = 0.0;
	private int SAMPLE_RATE;
	private int BUFF_SIZE;
	private AudioTrack audioTrack;
	
	private double freq;
	private float notelength;
	/*
	 * 
	 * 
	 * 
	 * Possibly switch to wavetable synthesis????????
	 * 
	 * 
	 * 
	 */
	public ToneGenerator()
	{
		//get sample rate and buffer size.
		SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		BUFF_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
		
		//create an audiotrack object		
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, 
        AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, BUFF_SIZE, AudioTrack.MODE_STREAM);
		
	}

	@Override
	protected Void doInBackground(MessageObject... incomingNoteArray) 
	{
		// TODO select first message in incoming array and pull attributes to pass to oscillator
		MessageObject incomingNote = incomingNoteArray[0];
		freq = incomingNote.freq;
		notelength = incomingNote.notelength;
		amplitude = incomingNote.amplitude;
		triangleWave();
		return null;
	}
	
	@Override
	protected void onProgressUpdate(MessageObject... progress)
	{
		MessageObject incomingNote = progress[0];
		freq = incomingNote.freq;
		notelength = incomingNote.notelength;
	}
	
	private void sinWave(float notelength)	//method to generate sinwaves
	{
		short samples[] = new short[BUFF_SIZE];
		int playedSamples = 0;
		notelength = SAMPLE_RATE*notelength;
		
		audioTrack.play();
		while (playedSamples < notelength)
		{
			for(int i=0; i < BUFF_SIZE; i++)
			{
				samples[i] = (short) (amplitude*Math.sin(phase));
				phase += TWO_PI*freq/SAMPLE_RATE;
			}
			audioTrack.write(samples, 0, BUFF_SIZE);
			playedSamples += BUFF_SIZE;
		}
		killAudioTrack();
	}
	
	private void squareWave(double freq, float notelength)
	{
		short samples[] = new short[BUFF_SIZE];
		int playedSamples = 0;
		notelength = SAMPLE_RATE*notelength;					//change play rate to fraction of sample rate

		audioTrack.play();
		while(playedSamples < notelength)
		{
		    for( int i = 0; i < samples.length; i++ )
		    {
		        samples[i] = (short)(amplitude*Math.signum(Math.sin(phase)));
		        phase += freq*TWO_PI/SAMPLE_RATE;
			}
			audioTrack.write(samples, 0, BUFF_SIZE);
			playedSamples += BUFF_SIZE;
		}
		//killAudioTrack();
	}
	
	
	private void triangleWave()
	{
		short samples[] = new short[BUFF_SIZE];
		int playedSamples = 0;
		
		notelength = SAMPLE_RATE*notelength;
		audioTrack.play();
		while(playedSamples < notelength)
		{
		    for( int i = 0; i < samples.length; i++ )
		    {
		 	   samples[i] = (short) (amplitude*Math.asin(Math.sin(phase)));
		 	   phase += freq*TWO_PI/SAMPLE_RATE;
		    }
			audioTrack.write(samples, 0, BUFF_SIZE);
			playedSamples += BUFF_SIZE;
		}
		killAudioTrack();
	}
	
	
	private void killAudioTrack()
	{
		audioTrack.stop();
		audioTrack.release();
	}
}