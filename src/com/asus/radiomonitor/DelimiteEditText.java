package com.asus.radiomonitor;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.EditText;

public class DelimiteEditText extends EditText {
	public DelimiteEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	private StringBuilder builder = new StringBuilder();
	
	private Handler mHandler = new Handler();

	public void setDelimiteText(CharSequence text) {
		builder.delete(0, builder.length());
		for (int i=text.length()-1; i>=0; i--) {
			builder.append(text.charAt(i));
			if ((text.length()-i)%3==0 && i!=0) {
				builder.append(",");
			}
		}
		builder.reverse().toString();
		super.setText(builder);
		
		// 0.5s highlight
		setTextColor(getResources().getColor(R.color.RED));
		mHandler.postDelayed(runnable, 500);
	}
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			setTextColor(getResources().getColor(R.color.BLACK));
		}
	};
}
