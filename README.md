#QrcodeLib ��ά������ɨ���

>�ÿ�Ӧ����ʵ����Ŀ�У�������ά��������ɨ�蹦�ܣ������������ȶ����ã���ֱ��Ͷ�뵽ʵ�ʵĿ�������
>
>���֧�ְ汾��**api19--->android4.4**
>����汾��**23**
>Ŀ��汾��targetSdkVersion����**22**����������֧��6.0����ʱȨ�ޣ�


####ʹ�÷���
######1.���ɣ����������Զ�������ά�뼶�����[��ά��](http://baike.baidu.com/link?url=KDS-yIbBSRYEfmebrqYmRUUtxTVYQN8j_rkgYFX9e1EISoqLCsgyXsI0zJKH3844LXFdZiSGyaOIny8jJ84Ib_)��
```java
Encoder encoder = new Encoder.Builder()
				//�ַ�����Ĭ��Ϊ��utf-8��
				.setCharset("utf-8")
				//ͼƬ�ڱ߾�
				.setBitmapPadding(2)
				//�������ɵ�ͼƬ�߶�
				.setBitmapHeight(dimension)
				//�������ɵ�ͼƬ���
				.setBitmapWidth(dimension)
				//���ö�ά�뱳����ɫ��Ĭ�ϰ�ɫ
				.setBackgroundColor(int color)
				//���ö�ά��ɫ����ɫ��Ĭ�Ϻ�ɫ
				.setCodeColor(int color)
				.build();

```
######2.ɨ��

>1.�����µ�Activity���̳�**com.zbar.lib.CaptureActivity**��
>2.ʵ�� **decodeSuccess(String result)**��**decodeFail()**����������
>3.������Activityɨ�裬��ȡɨ�����ݲ�����

######*����*
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
		return encoder.encode("��������");
	}

	@Override
	protected void decodeSuccess(String result) {
		//ɨ��ɹ���������
	}

	@Override
	protected void decodeFail() {
		//ɨ��ʧ�ܣ���ʾ
	}
}
```

####�ر���л
######1.zbar��Դɨ���
######2.Google��Դ��zxing��ά������ɨ���
*û�����ǵĿ�Դ����û�����������ĵ�������л��*