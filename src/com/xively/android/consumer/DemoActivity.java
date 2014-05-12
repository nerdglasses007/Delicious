package com.xively.android.consumer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.xively.android.service.IHttpService;
import com.xively.android.service.Response;

/**
 * A primitive activity to demo simple remote client connection to the Xively's
 * AIDL Service service.
 * 
 * @author s0pau
 * 
 */
public class DemoActivity extends Activity implements OnClickListener
{
	private static final String TAG = DemoActivity.class.getSimpleName();
	IHttpService service;
	HttpServiceConnection connection;
	 Handler myHandler = new Handler();
	 int curr_num = 0;
	// TextView resultField = (TextView) findViewById(R.id.result);
	 Handler mHandler = new Handler();

	// ------------- SET THE FOLLOWING ACCORDINGLY ----------------------

        // DEFINE YOUR CREDENTIALS BELOW (TODO: make these configurable)
        private final String myApiKey = "KKa9YnfCVdoMEPyc5HUbf2ktsdpzWIKooetjH7HnxSsNBDj3";
        private final int myFeedId = 465717823; 

	// a datastream id which belongs to myFeedId
	private final String myDatastreamId = "cost";
	// a datapoint to be created on myDatastreamId
	private final String myNewDatapoint = "{ \"datapoints\":[ {\"at\":\"2014-04-23T00:01:10Z\",\"value\":\"275\"} ] }";
	//public static TextView resultField;
	
	// ------------------------------------------------------------------

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		
		initService();
		
		
				
		// Setup the UI
		Button button1 = (Button) findViewById(R.id.doService1);
		button1.setOnClickListener(this);

		Button button2 = (Button) findViewById(R.id.doService2);
		button2.setOnClickListener(new OnClickListener()
		{
			TextView resultField = (TextView) findViewById(R.id.result);

			public void onClick(View v)
			{
				Response response = null;
				try
				{
					service.setApiKey(myApiKey);
					response = service.createDatapoint(myFeedId, myDatastreamId, myNewDatapoint);
				} catch (RemoteException e)
				{
					Log.e(DemoActivity.TAG, "onClick failed", e);
				}

				if (response != null)
				{
					resultField.setText(response.getContent());
				}
			}
		});
	}


protected void onStart()
{
	super.onStart();

	Thread thread = new BasicThread1(); 
	thread.start(); 
	myHandler.post(mMyRunnable);
	
}	
	
public class BasicThread1 extends Thread {
	
	TextView resultField = (TextView) findViewById(R.id.result);
	public void run(){
		if(curr_num > 10){
			resultField.setText("0");
			mHandler.postDelayed(this, 1000);
		}	
	}
}
	
	Runnable mMyRunnable = new Runnable()
	{
	    public void run()
	    {
	    	new update().execute("");
	    	myHandler.postDelayed(mMyRunnable, 10000);

	    }
	 };
	 
		 
	public void onClick(View v)
	{
				
		//detect the view that was "clicked"
		switch(v.getId()){
		case R.id.doService1:
			
			break;
		
		}
	}
	
	private class update extends AsyncTask<String, Void, String>{
		
		TextView resultField = (TextView) findViewById(R.id.result);
		Response response = null;
		int curr_value = 0;
		int brewing;
		
		protected String doInBackground(String...params){
			try
			{
				service.setApiKey(myApiKey);
				response = service.getFeed(myFeedId);
				try {
					JSONObject nerdy = new JSONObject(response.getContent());
					JSONArray jsonXively = nerdy.getJSONArray("datastreams");
					
					JSONObject dataStream0 = jsonXively.getJSONObject(0);
					curr_value = dataStream0.getInt("current_value");

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (RemoteException e)
			{
				Log.e(DemoActivity.TAG, "onClick failed", e);
			}
			return "Executed";
		}
		
        @Override
        protected void onPostExecute(String result) {
    		if (response != null)
    		{
    			
    		
    			    //curr_num = Integer.parseInt(curr_value);
    			    if (curr_value > 500 && brewing == 0){
    			    resultField.setText("Coffee is Brewing!!!!");
    			    brewing = 1;
    			    }
    			    if(curr_value < 100 ){
    			    	//if(brewing == 1){
    			    		resultField.setText("Coffee is Fresh!!!!");
            			    brewing = 0;
    			    	//}
    			    		
    			    }
    		
    		} 
    		
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
	
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		releaseService();
	}

	/**
	 * Binds this activity to the service.
	 */
	private void initService()
	{
		connection = new HttpServiceConnection();
		Intent i = new Intent("com.xively.android.service.HttpService");
		boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
		Log.d(TAG, "initService() bound with " + ret);
	}

	/**
	 * Unbinds this activity from the service.
	 */
	private void releaseService()
	{
		unbindService(connection);
		connection = null;
		Log.d(TAG, "releaseService() unbound.");
	}

	class HttpServiceConnection implements ServiceConnection
	{
		public void onServiceConnected(ComponentName name, IBinder boundService)
		{
			service = IHttpService.Stub.asInterface((IBinder) boundService);
			Log.d(DemoActivity.TAG, "onServiceConnected() connected");
			Toast.makeText(DemoActivity.this, "Service connected", Toast.LENGTH_LONG).show();
		}

		public void onServiceDisconnected(ComponentName name)
		{
			service = null;
			Log.d(DemoActivity.TAG, "onServiceDisconnected() disconnected");
			Toast.makeText(DemoActivity.this, "Service connected", Toast.LENGTH_LONG).show();
		}
	}

}
