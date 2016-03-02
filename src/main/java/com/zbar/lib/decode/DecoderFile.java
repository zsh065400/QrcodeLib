package com.zbar.lib.decode;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;

/**
 * @author :   赵树豪
 * @since 1.0
 * 二维码图片解码成文本信息
 */
public class DecoderFile {

	public static final String TAG = DecoderFile.class.getSimpleName();

	public static final String decode(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		// DecodeHintType 和EncodeHintType
		Hashtable<DecodeHintType, String> hints = new Hashtable<>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // 设置二维码内容的编码
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // 先获取原大小
		Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false; // 获取新的大小

		int sampleSize = (int) (options.outHeight / (float) 200);

		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);

		if (scanBitmap == null) {
			return null;
		}

		// TODO: 2016/2/4 偶发性空指针异常
		LuminanceSource source1 = new com.google.zxing.PlanarYUVLuminanceSource(
				rgb2YUV(scanBitmap), scanBitmap.getWidth(),
				scanBitmap.getHeight(), 0, 0, scanBitmap.getWidth(),
				scanBitmap.getHeight(), false);
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				source1));
		MultiFormatReader reader1 = new MultiFormatReader();
		Result result = null;
		try {
			result = reader1.decode(binaryBitmap);
		} catch (NotFoundException e1) {
			Log.d(TAG, "decode qrcode failed, now start second decode method...");
			e1.printStackTrace();
		}
		if (result == null) {
			com.zbar.lib.decode.RGBLuminanceSource source =
					new com.zbar.lib.decode.RGBLuminanceSource(scanBitmap);
			BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
			QRCodeReader reader = new QRCodeReader();
			try {
				result = reader.decode(bitmap1, hints);
			} catch (NotFoundException e) {
				e.printStackTrace();
			} catch (ChecksumException e) {
				e.printStackTrace();
			} catch (FormatException e) {
				e.printStackTrace();
			}
		}
		if (result != null) {
			String str = result.getText();
			Log.d(TAG, "decode succeed, string is : " + str);
			return str;
		}
		return null;
	}

	/**
	 * 将bitmap由RGB转换为YUV
	 *
	 * @param bitmap 转换的图形
	 * @return YUV数据
	 */
	private static byte[] rgb2YUV(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		int len = width * height;
		byte[] yuv = new byte[len * 3 / 2];
		int y, u, v;
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int rgb = pixels[i * width + j] & 0x00FFFFFF;

				int r = rgb & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb >> 16) & 0xFF;

				y = ((66 * r + 129 * g + 25 * b + 128) >> 8) + 16;
				y = y < 16 ? 16 : (y > 255 ? 255 : y);

				yuv[i * width + j] = (byte) y;
			}
		}
		return yuv;
	}

}
