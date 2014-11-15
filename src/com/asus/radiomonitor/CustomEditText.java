package com.asus.radiomonitor;

import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Handler;
import java.text.DateFormat;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {
	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	//private String TAG = "RadioMonitor";
	
	private Handler mHandler = new Handler();

	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			setTextColor(getResources().getColor(R.color.BLACK));
		}
	};
	
	public void setTextAndHighLight(CharSequence text){
		super.setText(text);

		// 0.5s highlight
		setTextColor(getResources().getColor(R.color.RED));
		mHandler.postDelayed(runnable, 500);
	}

	private CharSequence oldText = "";
	
	public void setTextAndLogIfChanged(CharSequence text, LogEditText log, CharSequence prompt){
		super.setText(text);
		
		// 0.5s highlight
		setTextColor(getResources().getColor(R.color.RED));
		mHandler.postDelayed(runnable, 500);
		
		if (!oldText.equals(text)) {
			oldText = text;

			//DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
			Date now = new Date();
			String curTime = df.format(now);
			log.appendAndLog("[ " + curTime + " ] " + prompt + " ==> " + text + "\n");
		}
	}
	
}
