package org.zsh.microinformation;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.zbar.lib.CaptureActivity;
import com.zbar.lib.encode.Encoder;

/**
 * @author：Administrator
 * @version:1.0
 */
public class ScanActivity extends CaptureActivity {
	private Bitmap createQrcode() {
		Encoder encoder = new Encoder.Builder()
				.setCharset("utf-8")
				.setBitmapPadding(2)
				.setBitmapHeight(500)
				.setBitmapWidth(500)
				.setBackgroundColor(Color.WHITE)
				.setCodeColor(Color.RED)
				.build();
		return encoder.encode("测试内容");
	}

	@Override
	protected void decodeSuccess(String result) {
		//扫描成功，处理结果
	}

	@Override
	protected void decodeFail() {
		//扫描失败，提示
	}
}
