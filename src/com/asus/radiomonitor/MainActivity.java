package com.asus.radiomonitor;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import com.asus.radiomonitor.R;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.net.TrafficStats;

public class MainActivity extends Activity 
		implements OnClickListener, OnCheckedChangeListener {

	static final String TAG = "RadioMonitor";
	
	Activity activity;
	
    //private static final String URL =
          //"http://stackoverflow.com/feeds/tag?tagnames=android&sort=newest";
    	  //"http://www.163.com";
	
	ConnectivityManager conn;
	TelephonyManager tele;
	InputMethodManager imm;
	
	// subclass instance to PhoneStateListener
	private MobileListener listener;
	
	// handler for timing
	private Handler mRefreshHandler;
	
	public CustomEditText mDataState;
	public CustomEditText mNetworkType;
	public CustomEditText mSignalStrength;
	public CustomEditText mDataDirection;
	public DelimiteEditText mMobileRxBytes;
	public DelimiteEditText mMobileTxBytes;
	public LogEditText mLog;
	public ToggleButton mRefresh;
	public EditText mRefreshFrequency;
	public EditText mEditUrl;
	public Button	mGo;
	public WebView myWebView;
	public Button mTest;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		
		mDataState = (CustomEditText) findViewById(R.id.editDataState);
		mNetworkType = (CustomEditText) findViewById(R.id.editNetworkType);
		mSignalStrength = (CustomEditText) findViewById(R.id.editSignalStrength);
		mDataDirection = (CustomEditText) findViewById(R.id.editDataDirection);
		mMobileRxBytes = (DelimiteEditText) findViewById(R.id.delimiteRxBytes);
		mMobileTxBytes = (DelimiteEditText) findViewById(R.id.delimiteTxBytes);
		mLog = (LogEditText) findViewById(R.id.editLog);
		mRefresh = (ToggleButton) findViewById(R.id.toggleRefresh);
		mRefreshFrequency = (EditText) findViewById(R.id.editRefreshFrequency);
		mEditUrl = (EditText) findViewById(R.id.editUrl);
		mGo = (Button) findViewById(R.id.btnGo);
        myWebView = (WebView) findViewById(R.id.webView);
        mTest = (Button) findViewById(R.id.btnTest);
		
        mLog.createLogFile();
        
        activity = this;
        
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.setWebChromeClient(new WebChromeClient() {
        	public void onProgressChanged(WebView view, int progress) {
        		// Activities and WebViews measure progress with different scales.
        	    // The progress meter will automatically disappear when we reach 100%
        	    activity.setProgress(progress * 100);
        	}
        });
        myWebView.setWebViewClient(new WebViewClient() {
        	@Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {       
                view.loadUrl(url);       
                return true;       
            }

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				mEditUrl.setText(url);
				super.onPageStarted(view, url, favicon);
			}

        });
        
		conn = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		tele = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		listener = new MobileListener();
		//permission needed
		tele.listen(listener, 
				PhoneStateListener.LISTEN_DATA_ACTIVITY | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE | 
				PhoneStateListener.LISTEN_SERVICE_STATE | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		mRefreshHandler = new Handler();

		mGo.setOnClickListener(this);
		mTest.setOnClickListener(this);
		
		mRefresh.setOnCheckedChangeListener(this);
		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		tele.listen(listener, PhoneStateListener.LISTEN_NONE);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			new AlertDialog.Builder(this)
						.setTitle(getResources().getString(R.string.app_name))
						.setMessage("version: " + getResources().getString(R.string.app_version))
						.show();
			return true;
		case R.id.menu_full_screen_log:
			//Intent intent = new Intent();
			//startActivity(intent);
			//setContentView(R.layout.full_screen_log);
			return true;
		case R.id.menu_quit_full_screen_log:
			//setContentView(R.layout.activity_main);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class MobileListener extends PhoneStateListener {

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			Log.i(TAG, "state: " + dataStateToString(state) + " networkType: " + networkTypeToString(networkType));
			mDataState.setTextAndLogIfChanged(dataStateToString(state), mLog, getResources().getString(R.string.data_state));
			mNetworkType.setTextAndLogIfChanged(networkTypeToString(networkType), mLog, getResources().getString(R.string.network_type));
			
			if (state!=TelephonyManager.DATA_CONNECTED) {
				//mNetworkType.setText("");
				mDataDirection.setText("");
				mSignalStrength.setText("");
			}
        	
			super.onDataConnectionStateChanged(state, networkType);
		}

		@Override
		public void onDataActivity(int direction) {
			Log.i(TAG, "Data direction: " + dataDirectionToString(direction));
			mDataDirection.setTextAndHighLight(dataDirectionToString(direction));
			
			super.onDataActivity(direction);
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			Log.i(TAG, "Signal strength: " + String.valueOf(signalStrength.getGsmSignalStrength()));
			mSignalStrength.setTextAndHighLight(String.valueOf(signalStrength.getGsmSignalStrength()));
			
			super.onSignalStrengthsChanged(signalStrength);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			//String ss = "Operator - " + serviceState.getOperatorAlphaLong() + 
			//		", Roaming - " + serviceState.getRoaming() +
			//		", State - " + serviceState.getState() + "\n";
			
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
			Date now = new Date();
			String curTime = df.format(now);

			mLog.append("[ " + curTime + " ] " + serviceState.toString() + "\n");
			
			super.onServiceStateChanged(serviceState);
		}
		
	}

	
	public String dataStateToString(int state) {
		switch (state) {
		case TelephonyManager.DATA_DISCONNECTED:
			return "DISCONNECTED";
		case TelephonyManager.DATA_CONNECTING:
			return "CONNECTING";
		case TelephonyManager.DATA_CONNECTED:
			return "CONNECTED";
		case TelephonyManager.DATA_SUSPENDED:
			return "SUSPENDED";
		default:
			return "UNKNOWN";
		}
	}
	
	public String networkTypeToString(int type) {
		switch(type) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
            return "GPRS";
        case TelephonyManager.NETWORK_TYPE_EDGE:
            return "EDGE";
        case TelephonyManager.NETWORK_TYPE_UMTS:
            return "UMTS";
        case TelephonyManager.NETWORK_TYPE_HSDPA:
            return "HSDPA";
        case TelephonyManager.NETWORK_TYPE_HSUPA:
            return "HSUPA";
        case TelephonyManager.NETWORK_TYPE_HSPA:
            return "HSPA";
        case TelephonyManager.NETWORK_TYPE_CDMA:
            return "CDMA";
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
            return "CDMA - EvDo rev. 0";
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
            return "CDMA - EvDo rev. A";
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
            return "CDMA - EvDo rev. B";
        case TelephonyManager.NETWORK_TYPE_1xRTT:
            return "CDMA - 1xRTT";
        case TelephonyManager.NETWORK_TYPE_LTE:
            return "LTE";
        case TelephonyManager.NETWORK_TYPE_EHRPD:
            return "CDMA - eHRPD";
        case TelephonyManager.NETWORK_TYPE_IDEN:
            return "iDEN";
        case TelephonyManager.NETWORK_TYPE_HSPAP:
            return "HSPA+";
        default:
            return "UNKNOWN";
		}
	}
	
	public String dataDirectionToString(int direction) {
		switch (direction) {
		case TelephonyManager.DATA_ACTIVITY_IN:
			return "DOWNLINK";
		case TelephonyManager.DATA_ACTIVITY_OUT:
			return "UPLINK";
		case TelephonyManager.DATA_ACTIVITY_INOUT:
			return "DOWN/UP";
		case TelephonyManager.DATA_ACTIVITY_NONE:
			return "NONE";
		case TelephonyManager.DATA_ACTIVITY_DORMANT:
			return "DORMANT";
		default:
			return "UNKNOWN";
		}
	}
	

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.btnGo:
			mRefreshFrequency.clearFocus();
			mEditUrl.clearFocus();
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			loadPage();
			break;
		case R.id.btnTest:
			mLog.appendAndLog("abc\n");
			break;
		default:
			break;
		}
		
	}
	
	int mDelayMillis;
	
	Runnable runnable = new Runnable() {
		@Override
		public void run() {
			
			loadPage();
			
			mRefreshHandler.postDelayed(this, mDelayMillis);
		}
	};
	
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked==true) {
			try {
				mDelayMillis = Integer.parseInt(mRefreshFrequency.getText().toString()) * 1000;
			}
			catch(Exception e) {
				buttonView.setChecked(false);
				return;
			}

			if(mDelayMillis==0) {
				buttonView.setChecked(false);
				return;
			}

			mRefreshFrequency.setEnabled(false);
			mEditUrl.clearFocus();
			imm.hideSoftInputFromWindow(buttonView.getWindowToken(), 0);

			//mRefresh.setTextColor(getResources().getColor(R.color.LIGHTBLUE));
			
			loadPage();
			
			mRefreshHandler.postDelayed(runnable, mDelayMillis);
		}
		else {
			//mRefresh.setTextColor(getResources().getColor(R.color.BLACK));
			
			mRefreshHandler.removeCallbacks(runnable);
			mRefreshFrequency.setEnabled(true);
		}
		
	}
	
	
	// Uses AsyncTask subclass to download the XML feed from stackoverflow.com.
    // This avoids UI lock up. To prevent network operations from
    // causing a delay that results in a poor user experience, always perform
    // network operations on a separate thread from the UI.
    private void loadPage() {
        // AsyncTask subclass
        //new DownloadXmlTask().execute(URL);
    	String url = mEditUrl.getText().toString();
    	if (!url.startsWith("http://")) {
    		url = "http://" + url;
    	}
    	myWebView.loadUrl(url);
    	
        // refresh all the items on UI
        mDataState.setTextAndLogIfChanged(dataStateToString(tele.getDataState()), mLog, getResources().getString(R.string.data_state));
        if (mDataState.getText().toString().equals("CONNECTED") ||
        	mDataState.getText().toString().equals("CONNECTING")) {
        	mNetworkType.setTextAndLogIfChanged(networkTypeToString(tele.getNetworkType()), mLog, getResources().getString(R.string.network_type));
        	mDataDirection.setTextAndHighLight(dataDirectionToString(tele.getDataActivity()));
        	// sorry, can't get signal strength manually from TelephonyManager
        }
        else {
        	mNetworkType.setText("");
        	mDataDirection.setText("");
        	mSignalStrength.setText("");
        }
        
		long mobileRxBytes = TrafficStats.getMobileRxBytes();
		long mobileTxBytes = TrafficStats.getMobileTxBytes();

		mMobileRxBytes.setDelimiteText(String.valueOf(mobileRxBytes));
		mMobileTxBytes.setDelimiteText(String.valueOf(mobileTxBytes));
        
		
    }
	/*
	// Implementation of AsyncTask used to download XML feed from stackoverflow.com.
    private class DownloadXmlTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                return loadXmlFromNetwork(urls[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
           // setContentView(R.layout.activity_main);
            // Displays the HTML string in the UI via a WebView
            myWebView.loadData(result, "text/html", null);
        }
    }
	
    // Downloads XML from stackoverflow.com, parses it, and combines it with
    // HTML markup. Returns HTML string.
    private String loadXmlFromNetwork(String urlString) throws XmlPullParserException, IOException {
        InputStream stream = null;
        StackOverflowXmlParser stackOverflowXmlParser = new StackOverflowXmlParser();
        List<Entry> entries = null;
        String title = null;
        String url = null;
        String summary = null;
        Calendar rightNow = Calendar.getInstance();
       // DateFormat formatter = new SimpleDateFormat("MMM dd h:mm");
        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US);
        
        // Checks whether the user set the preference to include summary text
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean pref = sharedPrefs.getBoolean("summaryPref", false);

        StringBuilder htmlString = new StringBuilder();
        htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
        htmlString.append("<em>" + getResources().getString(R.string.updated) + " " +
                df.format(rightNow.getTime()) + "</em>");

        try {
            stream = downloadUrl(urlString);
            entries = stackOverflowXmlParser.parse(stream);
        // Makes sure that the InputStream is closed after the app is
        // finished using it.
        } finally {
            if (stream != null) {
                stream.close();
            }
        }

        // StackOverflowXmlParser returns a List (called "entries") of Entry objects.
        // Each Entry object represents a single post in the XML feed.
        // This section processes the entries list to combine each entry with HTML markup.
        // Each entry is displayed in the UI as a link that optionally includes
        // a text summary.
        for (Entry entry : entries) {
            htmlString.append("<p><a href='");
            htmlString.append(entry.link);
            htmlString.append("'>" + entry.title + "</a></p>");
            // If the user set the preference to include summary text,
            // adds it to the display.
            if (pref) {
                htmlString.append(entry.summary);
            }
        }
        return htmlString.toString();
    }
    
    // Given a string representation of a URL, sets up a connection and gets
    // an input stream.
    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);//milliseconds
        conn.setConnectTimeout(15000);//milliseconds
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        InputStream stream = conn.getInputStream();
        return stream;
    }
    */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (myWebView.canGoBack() && event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	myWebView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    
}
