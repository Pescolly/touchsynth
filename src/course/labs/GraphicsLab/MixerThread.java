package course.labs.GraphicsLab;

public class MixerThread extends Thread
{
	private AudioMixer mixer;
	public MixerThread()
	{
		super();
		mixer = new AudioMixer();
	}

	@Override
	public void run()
	{
		super.run();
		mixer.mainLoop();
		
	}

}
