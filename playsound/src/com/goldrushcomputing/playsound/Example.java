package com.goldrushcomputing.playsound;

import java.io.File;
import java.util.ArrayList;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.fmod.FMODAudioDevice;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Example extends ARActivity 
{
	static String TAG = "Example";
	boolean isPlayerReady = false;
	boolean isPlaying = false;
	int downloadCount = 0;
	
	private ArrayList<String> trackUrls;
	String trackUrlRoot = "https://dl.dropboxusercontent.com/u/46669699/GoogleIO/";
	String soundFile0 = "bass.wav";
	String soundFile1 = "hat.wav";
	String soundFile2 = "snaredrum.wav";
	String soundFile3 = "bosa.wav";
	
    private FMODAudioDevice mFMODAudioDevice = new FMODAudioDevice();
    
	private Handler mUpdateHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{		
			cUpdate();
			
			int position = cGetPosition();
			int length = cGetLength();
			int channels = cGetChannelsPlaying();
			
			((TextView)findViewById(R.id.txtState)).setText(cGetPlaying() ? "Playing" : "Stopped");	
			((TextView)findViewById(R.id.txtPos)).setText(String.format("%02d:%02d:%02d / %02d:%02d:%02d", position / 1000 / 60, position / 1000 % 60, position / 10 % 100, length / 1000 / 60, length / 1000 % 60, length / 10 % 100));
			((TextView)findViewById(R.id.txtChans)).setText(String.format("%d", channels));

			removeMessages(0);
		    sendMessageDelayed(obtainMessage(0), 50);
		}
	};
	
	/**
     * A custom renderer is used to produce a new visual experience.
     */
    private SimpleRenderer simpleRenderer = new SimpleRenderer();

    /**
     * The FrameLayout where the AR view is displayed.
     */
    private FrameLayout mainLayout;	
   
	
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        trackUrls = new ArrayList<String>();
        trackUrls.add(trackUrlRoot + soundFile0);
        trackUrls.add(trackUrlRoot + soundFile1);
        trackUrls.add(trackUrlRoot + soundFile2);
        trackUrls.add(trackUrlRoot + soundFile3);
                      
        
    }
    
    @Override
    public void onStart()
    {
    	super.onStart();   	
    	mFMODAudioDevice.start();
    	//cBegin();
    	//mUpdateHandler.sendMessageDelayed(mUpdateHandler.obtainMessage(0), 0);
    	
    	
    	//this.downloadTrack();
    	
    	
    	final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		    	startPlayer();
		    }
		}, 1000);
		
    	
    	
    	
    	mainLayout = (FrameLayout)this.findViewById(R.id.mainLayout);

		
		
		// When the screen is tapped, inform the renderer and vibrate the phone
    	/*
				mainLayout.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {

		                simpleRenderer.click();

		                Vibrator vib = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		                vib.vibrate(40);
		            }

		        });
		        */
    }
    
    @Override
    public void onStop()
    {
    	mUpdateHandler.removeMessages(0);
    	cEnd();
    	mFMODAudioDevice.stop();
    	super.onStop();
    }
	
    static 
    {
    	System.loadLibrary("fmodex");
        System.loadLibrary("main");
    }
    
	public native void cBegin();
	public native void cBeginWith(String path0, String path1, String path2, String path3);
	public native void cUpdate();
	public native void cEnd();
	public native void cPlaySound(int id);
	public native int cGetLength();
	public native int cGetPosition();
	public native boolean cGetPlaying();
	public native int cGetChannelsPlaying();
	
	

	public void downloadTrack() {
		this.showLoadingPanel();
		
		if (Environment.MEDIA_MOUNTED.equals(Environment
				.getExternalStorageState())) {

			/*String directoryPath = Environment.getExternalStorageDirectory()
					.getPath()
					+ "/Android/data/"
					+ this.getPackageName()
					+ "/track";
					*/
			
			//directoryPath = "/sdcard/fmod";
			
			String directoryPath = getTrackDirectory();

			File directory = new File(directoryPath);

			if (!directory.exists()) {
				directory.mkdirs();
				if (!directory.exists()) {
					Log.e(TAG, "Failed to create directory:" + directoryPath);
				}
			}
			
			boolean needToDownload = false;
			for (int i = 0; i < this.trackUrls.size(); i++) {
				String trackUrl = this.trackUrls.get(i);
				String fileName = trackUrl
						.substring(trackUrl.lastIndexOf("/") + 1);
				
				
				File file = new File(directory, fileName);
				if(!file.exists()){
					needToDownload = true;
				}
				
			}
			
			if(needToDownload == false){
				this.hideLoadingPanel();
				this.startPlayer();
				//startPlaySync();	
			}else{
				for (int i = 0; i < this.trackUrls.size(); i++) {
					String trackUrl = this.trackUrls.get(i);
					String fileName = trackUrl
							.substring(trackUrl.lastIndexOf("/") + 1);
					File file = new File(directory, fileName);

					DownloadFileAsyncTask task = new DownloadFileAsyncTask() {
						@Override
						protected void onPostExecute(Boolean result) {
							if (result == true) {
								
								downloadCount ++;
								
								if(downloadCount == 4){
									hideLoadingPanel();
									// Log.i(TAG, "Finished downloading moview");
									Toast.makeText(Example.this, "Music Loaded", Toast.LENGTH_SHORT)
											.show();
									startPlayer();
									//startPlaySync();
								}							
								
							} else {
								hideLoadingPanel();
								new AlertDialog.Builder(Example.this)
										.setMessage("Failed to load music")
										.setPositiveButton(
												"OK",
												new DialogInterface.OnClickListener() {
													public void onClick(
															DialogInterface dialog,
															int which) {

													}
												}).show();
							}
						}
					};
					String filePath = file.getPath();
					task.execute(trackUrl, file.getPath());
					// Log.i(TAG, "Start downloading movie of " + this.movieUrl);

				}
			}

		}

	}
	
	
	
	
	/* FMOD Player */
	public void startPlayer() {
		isPlayerReady = true;
		mFMODAudioDevice.start();
		
		//String directoryPath = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + this.getPackageName() + "/track";
		
		String directoryPath = getTrackDirectory();
		
		//directoryPath = directoryPath.replace("/0/", "/legacy/");
		
		String path1 = directoryPath + "/" + soundFile0;
		String path2 = directoryPath + "/" + soundFile1;
		String path3 = directoryPath + "/" + soundFile2;
		String path4 = directoryPath + "/" + soundFile3;
		
		//path1 = path1.substring(path1.indexOf("/sdcard"));
		//path2 = path2.substring(path2.indexOf("/sdcard"));
		//path3 = path3.substring(path3.indexOf("/sdcard"));
		//path4 = path4.substring(path4.indexOf("/sdcard"));
		
		Log.d(TAG, path1);
		Log.d(TAG, path2);
		Log.d(TAG, path3);
		Log.d(TAG, path4);
		
		//path1 = "/sdcard/bosa.wav";
		//path2 = "/sdcard/bass.wav";
		//path3 = "/sdcard/hat.wav";
		//path4 = "/sdcard/snaredrum.wav";
				
		
		cBeginWith(path1, path2, path3, path4);
		//cBegin();
		
		mUpdateHandler.sendMessageDelayed(mUpdateHandler.obtainMessage(0), 0);
		
		/*		
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
		        // Do something after 5s = 5000ms
		    	//downloadTrack();
		    	
		    	cUpdate();
				DrumsActivity.this.length = cGetLength();
				//int channels = cGetChannelsPlaying();

				
				//DrumsActivity.this.inspectBufferSize();
				
				DrumsActivity.this.playSound1(null);
				DrumsActivity.this.playSound2(null);
				DrumsActivity.this.playSound3(null);
				DrumsActivity.this.playSound4(null);
		    }
		}, 5000);
		*/
		
		
	}

	public String getTrackDirectory(){
		
		/*
		String directoryPath = Environment.getExternalStorageDirectory()
				.getPath()
				+ "/Android/data/"
				+ this.getPackageName()
				+ "/track";
		return directoryPath;
		*/
		
		//return "/sdcard/fmod";
		
		
		return this.getCacheDir().getAbsolutePath() + "/Music";
		
	}
	public void endPlayer() {
		isPlayerReady = false;
		cEnd();
		mFMODAudioDevice.stop();
	}

	public void playSound1(View view) {
		cPlaySound(0);
	}

	public void playSound2(View view) {
		cPlaySound(1);
	}

	public void playSound3(View view) {
		cPlaySound(2);
	}

	public void playSound4(View view) {
		cPlaySound(3);
	}

	public void playSound(int trackIndex) {

		cPlaySound(trackIndex);
		this.isPlaying = true;
	}

	/*
	public void playSoundFrom(int trackIndex, int position) {
		Log.d(TAG, "PlaySoundFrom " + position);
		cPlaySoundFrom(trackIndex, position);
		this.isPlaying = true;
	}
	*/

	public void seekSoundTo(int position) {
		//cSeekSoundTo(position);
	}

	/*
	public void pauseSound() {
		cPauseSound();
		this.isPlaying = false;
	}
	*/

	/*
	public void stopSound(int trackIndex) {
		cStopSound();
		this.isPlaying = false;
	}
	*/

	/*
	public void inspectBufferSize() {
		int size = cGetDSPBufferSize();
		Log.d(TAG, "DSP Buffer Size is " + size);
	}
	*/
	
	
	
	public void showLoadingPanel(){
		/*
		View view = (View) this.findViewById(R.id.loading_panel);
		view.setVisibility(View.VISIBLE);
		
		ImageView loadingMark = (ImageView) this.findViewById(R.id.loading_mark);
		Animation animationSlideIn = AnimationUtils.loadAnimation(this, R.anim.spinning);
		loadingMark.startAnimation(animationSlideIn);
		*/		
	}
	
	public void hideLoadingPanel(){
		/*
		View view = (View) this.findViewById(R.id.loading_panel);
		view.setVisibility(View.GONE);
		
		ImageView loadingMark = (ImageView) this.findViewById(R.id.loading_mark);
		loadingMark.clearAnimation();
		*/
	}
	
	
	/* ARToolKit */
	/**
	 * Provide our own SimpleRenderer.
	 */
	@Override
	protected ARRenderer supplyRenderer() {
		
		//return new SimpleRenderer();
		return new DrumsRenderer(this);
	}
	
	/**
	 * Use the FrameLayout in this Activity's UI.
	 */
	@Override
	protected FrameLayout supplyFrameLayout() {
		return (FrameLayout)this.findViewById(R.id.mainLayout);    	
	}
	

}