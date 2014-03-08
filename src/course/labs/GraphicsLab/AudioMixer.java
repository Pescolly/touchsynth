package course.labs.GraphicsLab;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;

public class AudioMixer extends Handler
{
	private final int BUFFER_SIZE;
	private final int SAMPLE_RATE;
	private final int AUDIO_OUTPUT_CHANNELS;
	private AudioTrack outputToHardware;
	private short[] mixedAudioData;
	
	public AudioMixer()
	{
		super();
		AUDIO_OUTPUT_CHANNELS = AudioFormat.CHANNEL_OUT_STEREO;
		SAMPLE_RATE = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);
		BUFFER_SIZE = AudioTrack.getMinBufferSize(SAMPLE_RATE, AUDIO_OUTPUT_CHANNELS, AudioFormat.ENCODING_PCM_16BIT);
		outputToHardware = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AUDIO_OUTPUT_CHANNELS, AudioFormat.ENCODING_PCM_16BIT, 
				BUFFER_SIZE, AudioManager.STREAM_MUSIC);
	}

	public void mainLoop()
	{
		if (true)
		{
			//do stuff with messages
		}
	}

	private void mixdown(MessageObject[] incomingArrays){			//audio audio data from arrays to create one array
		int numberOfIncomingChannels = incomingArrays.length;
		
		for (int i = 0; i < numberOfIncomingChannels; i++)
		{
			
		}
	}
	
	private void writeToHardware()
	{
		outputToHardware.play();
		outputToHardware.write(mixedAudioData, 0, BUFFER_SIZE);
	}
	
	private void killAudio()
	{
		outputToHardware.stop();
		outputToHardware.release();
	}

}
