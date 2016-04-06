package com.zbar.lib;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.zbar.lib.camera.CameraManager;
import com.zbar.lib.decode.CaptureActivityHandler;
import com.zbar.lib.decode.DecoderFile;
import com.zbar.lib.decode.InactivityTimer;

import org.zsh.permission.Permission;
import org.zsh.permission.callback.IHandleCallback;
import org.zsh.permission.callback.IRationale;

import java.io.IOException;

/**
 * 相机扫描界面，继承该类并实现decode两个方法即可完成解析回调操作
 *
 * @author zsh
 * @version 2.0.2
 */
public abstract class CaptureActivity extends Activity implements Callback,
		View.OnClickListener {

	private CaptureActivityHandler handler;
	private boolean hasSurface;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.50f;
	private boolean vibrate;
	private int x = 0;
	private int y = 0;
	private int cropWidth = 0;
	private int cropHeight = 0;
	private RelativeLayout mContainer = null;
	private RelativeLayout mCropLayout = null;
	private boolean isNeedCapture = false;

	private Button btnOpenLight;
	private Button btnChooseImg;

	private boolean isOpenLight = false;

	public boolean isNeedCapture() {
		return isNeedCapture;
	}

	public void setNeedCapture(boolean isNeedCapture) {
		this.isNeedCapture = isNeedCapture;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getCropWidth() {
		return cropWidth;
	}

	public void setCropWidth(int cropWidth) {
		this.cropWidth = cropWidth;
	}

	public int getCropHeight() {
		return cropHeight;
	}

	public void setCropHeight(int cropHeight) {
		this.cropHeight = cropHeight;
	}

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Utils.setTranslucentStatus(true, this);
		init();
	}

	@TargetApi(Build.VERSION_CODES.M)
	private void requestPermission() {
		Permission.getInstance().setRationable(new IRationale() {
			@Override
			public void showRationale(String[] permissions) {
				Toast.makeText(CaptureActivity.this,
						"二维码扫描需要使用摄像头，请您允许该权限(已拒绝请手动修改)",
						Toast.LENGTH_LONG).
						show();
			}
		});
		Permission.getInstance().request(new IHandleCallback() {
			@Override
			public void granted(String[] permission) {
				Log.d("CaptureActivity", "granted: success!");
			}

			@Override
			public void denied(String[] permission) {
				Log.d("CaptureActivity", "granted: failed!");
				Toast.makeText(CaptureActivity.this,
						"二维码扫描需要获得相机使用权限，请您授权",
						Toast.LENGTH_LONG).show();
				CaptureActivity.this.finish();
			}

		}, CaptureActivity.this, Manifest.permission.CAMERA);
	}

	private void init() {
		// 初始化 CameraManager
		setContentView(R.layout.aty_scan);
		CameraManager.init(getApplication());
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);

		mContainer = (RelativeLayout) findViewById(R.id.rootView);
		mCropLayout = (RelativeLayout) findViewById(R.id.capture_crop_layout);

		ImageView mQrLineView = (ImageView) findViewById(R.id.capture_scan_line);
		TranslateAnimation mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE, 0f, TranslateAnimation.ABSOLUTE, 0f,
				TranslateAnimation.RELATIVE_TO_PARENT, 0f, TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
		mAnimation.setDuration(1500);
		mAnimation.setRepeatCount(-1);
		mAnimation.setRepeatMode(Animation.REVERSE);
		mAnimation.setInterpolator(new LinearInterpolator());
		mQrLineView.setAnimation(mAnimation);
		//按钮
		btnOpenLight = (Button) findViewById(R.id.btnOpenLights);
		btnOpenLight.setOnClickListener(this);
		btnChooseImg = (Button) findViewById(R.id.btnChoosePic);
		btnChooseImg.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.btnOpenLights) {
			if (!isOpenLight) {
				openLight();
			} else {
				closeLight();
			}
		} else if (i == R.id.btnChoosePic) {
			Utils.openGallery(this);
		}
	}

	private void openLight() {
		btnOpenLight.setText(getString(R.string.close_light));
		CameraManager.get().openLight();
		isOpenLight = true;
	}

	private void closeLight() {
		btnOpenLight.setText(getString(R.string.open_light));
		CameraManager.get().offLight();
		isOpenLight = false;
	}


	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.capture_preview);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		requestPermission();
	}

	@Override
	protected void onStart() {
		super.onStart();
		hasSurface = false;

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isOpenLight) {
			closeLight();
		}
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	/**
	 * 结果调度分配
	 *
	 * @param result 扫描结果
	 */
	public void dispatchDecode(String result) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		if (!TextUtils.isEmpty(result)) {
			decodeSuccess(result);
		} else {
			decodeFail();
		}
		if (isOpenLight) {
			closeLight();
		}
		// 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
		handler.sendEmptyMessage(R.id.restart_preview);
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);

			Point point = CameraManager.get().getCameraResolution();
			int width = point.y;
			int height = point.x;

			int x = mCropLayout.getLeft() * width / mContainer.getWidth();
			int y = mCropLayout.getTop() * height / mContainer.getHeight();

			int cropWidth = mCropLayout.getWidth() * width / mContainer.getWidth();
			int cropHeight = mCropLayout.getHeight() * height / mContainer.getHeight();

			setX(x);
			setY(y);
			setCropWidth(cropWidth);
			setCropHeight(cropHeight);
			// 设置是否需要截图
			setNeedCapture(false);


		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(CaptureActivity.this);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	public Handler getHandler() {
		return handler;
	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case Utils.REQ_OPEN_IMG:
					String imgPaht = Utils.getPath(this, data.getData());
					final String decodeStr = DecoderFile.decode(imgPaht);
					dispatchDecode(decodeStr);
					break;
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		Permission.getInstance().onRequestPermissionsResult(permissions, grantResults);
	}

	/**
	 * 实现该方法处理扫描结果
	 *
	 * @param result 扫描后的内容
	 */
	protected abstract void decodeSuccess(String result);


	/**
	 * 实现该方法处理扫描失败事件
	 */
	protected abstract void decodeFail();
}