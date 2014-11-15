package com.asus.radiomonitor;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.EditText;

public class LogEditText extends EditText {

	private File mFile = null;
	
	public LogEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public boolean createLogFile() {
		if(mFile!=null)
			return true;
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) {
			Log.e(MainActivity.TAG, "SD card is not available right now!");
			return false;
		}
		try {
			String strPath = Environment.getExternalStorageDirectory().getPath();
			strPath += "/RadioMonitor/";
			File path = new File(strPath);
			if(!path.exists()) {
				path.mkdir();
			}
			Calendar rightNow = Calendar.getInstance();
			DateFormat formatter = new SimpleDateFormat("MMdd_HHmm");
			String fileName = formatter.format(rightNow.getTime());
			fileName += ".txt";
			mFile = new File(strPath + fileName);
			FileOutputStream stream = new FileOutputStream(mFile, true);	// append if exists
			stream.close();
		} catch(Exception e) {
			Log.e(MainActivity.TAG, "Error in createLogFile!");
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public void appendAndLog(CharSequence text) {
		super.append(text);
		if (mFile!=null) {
			writeToSdCard(text);
		}
	}
	
	public void writeToSdCard(CharSequence content) {
		try {
			FileOutputStream stream = new FileOutputStream(mFile, true);
			byte[] buf = content.toString().getBytes(); 
			stream.write(buf);
			stream.close();
		} catch(Exception e) {
			Log.e(MainActivity.TAG, "Error in writeToSdCard!");
			e.printStackTrace();
		}
	}
	
}
