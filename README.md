#QrcodeLib 二维码生成扫描库

>该库应用在实际项目中，包含二维码生成与扫描功能，功能完善且稳定易用，可直接投入到实际的开发当中
>
>
>最低支持版本：**api19--->android4.4**   
>
>编译版本：**23**
>
>目标版本（targetSdkVersion）：**22**（后续将会支持6.0运行时权限）



####使用方法
#####添加依赖(module下build.gradle)
```gradle
dependencies {
    compile 'org.zsh.support:qrlib:1.0.0'
}
```
######1.生成（根据字数自动调整二维码级别，详见[二维码](http://baike.baidu.com/link?url=KDS-yIbBSRYEfmebrqYmRUUtxTVYQN8j_rkgYFX9e1EISoqLCsgyXsI0zJKH3844LXFdZiSGyaOIny8jJ84Ib_)）
```java
Encoder encoder = new Encoder.Builder()
				//字符集，默认为“utf-8”
				.setCharset("utf-8")
				//图片内边距
				.setBitmapPadding(2)
				//设置生成的图片高度
				.setBitmapHeight(dimension)
				//设置生成的图片宽度
				.setBitmapWidth(dimension)
				//设置二维码背景颜色，默认白色
				.setBackgroundColor(int color)
				//设置二维码色块颜色，默认黑色
				.setCodeColor(int color)
				.build();

```
######2.扫描

>1.创建新的Activity并继承**com.zbar.lib.CaptureActivity**类
>
>2.实现 **decodeSuccess(String result)**和**decodeFail()**这两个方法
>
>3.启动该Activity扫描，获取扫描数据并处理。

######*范例*
```java
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
```

####特别鸣谢
######1.zbar开源扫描库
######2.Google开源的zxing二维码生成扫描库
*没有他们的开源，就没有现在这个库的诞生，拜谢。*