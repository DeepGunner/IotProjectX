package com.eespl.iotapp;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Fragment_Details extends Fragment implements TabListener {
	SharedPreferences sharedPreferences;
	String[] tabs = null;
	ActionBar actionBar;
	ImageView imageView;
	boolean isConnected = false;
	ArrayList<String> listOfValues,listOfDate,listOfTime;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		listOfValues = new ArrayList<String>();
		listOfDate = new ArrayList<String>();
		listOfTime = new ArrayList<String>();
		isConnected = checkNetwork();
	
		sharedPreferences = this.getActivity().getSharedPreferences(
				"LoginDetails", Context.MODE_PRIVATE);
		actionBar = getActivity().getActionBar();
		actionBar.show();
		String appName = sharedPreferences.getString("app_name", "");
		actionBar.setSubtitle(Html.fromHtml("<font color='#ffffff'>" + appName
				+ "</font>"));
		actionBar.setTitle(Html
				.fromHtml("<font color='#ffffff'>IOTApp </font>"));
		actionBar.setCustomView(imageView);
		actionBar.setHomeButtonEnabled(false);

		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setBackgroundDrawable(new ColorDrawable(Color
				.parseColor("#31A5E7")));
		actionBar.setDisplayOptions(actionBar.getDisplayOptions()
				| ActionBar.DISPLAY_SHOW_CUSTOM);
		imageView = new ImageView(actionBar.getThemedContext());
		imageView.setScaleType(ImageView.ScaleType.CENTER);
		imageView.setImageResource(R.drawable.ic_launcher);
		ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(100,
				100, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
		setHasOptionsMenu(true);
		imageView.setLayoutParams(layoutParams);

		tabs = (sharedPreferences.getString("app_com_type", "")).split(",");
	
		actionBar.removeAllTabs();
		
		for (String tab_name : tabs) {
			System.out.println("Yes u r here"+tab_name);
			actionBar.addTab(actionBar.newTab().setText(tab_name)
					.setTabListener(this));
		}
		
		if (isConnected) {
			// new GetImage().execute(sharedPreferences.getString("app_image",
			// ""));
		}

	}

	@SuppressLint("NewApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.details_fragment, container,
				false);

		if (isConnected) {
			if (!(sharedPreferences.contains("parameter_size"))) {
				new GetParameters().execute();
			}
		}
		return rootView;
	}

	private boolean checkNetwork() {

		ConnectivityManager cm = (ConnectivityManager) getActivity()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo[] info = cm.getAllNetworkInfo();
		if (cm != null) {
			if (info != null) {
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						System.out.println(info[i].getState());
						return true;
					}
			} else {
				return false;
			}
		}
		return false;
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction transaction) {
		String selectedTab = tab.getText().toString().trim();
		
		if (selectedTab.equalsIgnoreCase("Teleremote")) {
			if (sharedPreferences.contains("parameter_size"))
				transaction.replace(R.id.container, new Fragment_Teleremote());
		}
		
		
		else if (selectedTab.equalsIgnoreCase("Telemetry")) {

			System.out.println("TELEMETRYYY");

			if (sharedPreferences.contains("parameter_size"))
				transaction.replace(R.id.container, new Fragment_Telemetry());
		}  
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		System.out.println("TELEREMOTEE");

	}

	class GetImage extends AsyncTask<String, Integer, Bitmap> {

		ProgressDialog pd;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog.show(getActivity(), "Getting Image",
					"Please wait");
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					GetImage.this.cancel(true);
				}
			});

		}

		@Override
		protected Bitmap doInBackground(String... vals) {
			// TODO Auto-generated method stub
			String url = "http://www.techpacs.com/iot/admn/app_images/"
					+ vals[0];
			System.out.println("Image url Value=" + url);
			Bitmap bitmap = null;
			InputStream stream = null;
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inSampleSize = 1;
			try {
				stream = getHttpConnection(url);
				bitmap = BitmapFactory.decodeStream(stream, null, bmOptions);
				stream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return bitmap;

		}

		private InputStream getHttpConnection(String urlString)
				throws IOException {
			InputStream stream = null;
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();

			try {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("GET");
				httpConnection.connect();

				if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
					stream = httpConnection.getInputStream();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return stream;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pd.dismiss();
			imageView.setImageBitmap(result);
		}
	}

	class GetParameters extends AsyncTask<String, Integer, String> {

		String returnString = "", result;
		ProgressDialog pd;
		JSONTokener tokener;
		JSONObject jObject, jObjectOfValues,jObjectOfDate,jObjectOfTime;
		JSONArray jArray1, jArray2, jArray3,jArray4,jArray5;
		String json;
		ServerConnection sc = new ServerConnection();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			pd = ProgressDialog.show(getActivity(), "Getting Parameters",
					"Please wait");

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String scriptFileName = "getParameters.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(6);
			postParameters.add(new BasicNameValuePair("app_id",
					sharedPreferences.getString("app_id", "")));
			postParameters.add(new BasicNameValuePair("app_type",
					sharedPreferences.getString("app_type", "")));
			postParameters.add(new BasicNameValuePair("app_com_type",
					sharedPreferences.getString("app_com_type", "")));
			postParameters.add(new BasicNameValuePair("user_id",
					sharedPreferences.getString("user_id", "")));
		//	postParameters.add(new BasicNameValuePair("custom_id",
			//		sharedPreferences.getString("custom_id", "")));
			postParameters.add(new BasicNameValuePair("kit_id",
					sharedPreferences.getString("kit_id", "")));

			try {
				Editor editor = sharedPreferences.edit();
				jObject = sc.Connection(scriptFileName, postParameters);
				
				jArray1 = jObject.getJSONArray("comment_values");
				jArray2 = jObject.getJSONArray("parameter_values");

				editor.putInt("parameter_size", jArray1.length());

				for (int i = 0; i < jArray1.length(); i++) {
					editor.putString("comm_" + i, jArray1.getString(i));
					editor.putString("para_" + i, jArray2.getString(i));
				}
				editor.commit();
				jArray3 = jObject.getJSONArray("graph_values");
				jArray4 = jObject.getJSONArray("entry_date");
				jArray5 = jObject.getJSONArray("entry_time");

				for (int i = 0; i < jArray3.length(); i++) {
					//jObjectOfValues = jArray3.get(i);
					listOfValues.add(i,(String)jArray3.get(i));
					listOfDate.add(i,(String)jArray4.get(i));
					listOfTime.add(i,(String)jArray5.get(i));
					//jObjectOfDate = jArray4.getJSONObject(i);
					//listOfDate.add(i,
							//jObjectOfDate.getString("entry_date"));
					
					//jObjectOfTime = jArray5.getJSONObject(i);
					//listOfTime.add(i,
							//jObjectOfTime.getString("entry_time"));
				}
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
				e.printStackTrace();
			} catch (Exception e) {
				Log.e("log_tag", "Error in http connection!!" + e.toString());
			}
			return returnString;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			pd.dismiss();
			for (int i = 0; i < jArray3.length(); i++) {
				System.out.println("ope=" + listOfValues.get(i));
				System.out.println("date=" + listOfDate.get(i));
				System.out.println("time=" + listOfTime.get(i));
				
			}
			Bundle bundle = new Bundle();
			Fragment_Telemetry fragment = new Fragment_Telemetry();
			bundle.putStringArrayList("graph_values_list", listOfValues);
			bundle.putStringArrayList("graph_date_list", listOfDate);
			bundle.putStringArrayList("graph_time_list", listOfTime);
			
			fragment.setArguments(bundle);
			FragmentTransaction transaction = getActivity()
					.getFragmentManager().beginTransaction();
			transaction.replace(R.id.container, fragment);
			transaction.commit();
		}
	}
}
