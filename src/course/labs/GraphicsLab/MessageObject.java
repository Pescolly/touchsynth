package course.labs.GraphicsLab;

public class MessageObject extends Object
{
	public double freq;							// ie. 646.666 Hz
	public float notelength;					//fractions of samplerate/second i.e. .25 = 1/4*SAMPLE_RATE
	public int osc;							//used to determine which oscillator is used
	public short[] waveformData;
	public int amplitude;
	public float mixerWeight;			//flaot between 0.0 and 1.0 out of 1.0 to determine how to mix with other channels
	public int what;
	
	public MessageObject()
	{
		super();
	}
}
