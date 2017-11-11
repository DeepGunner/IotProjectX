package com.eespl.iotapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class Fragment_Teleremote extends Fragment implements
		OnCheckedChangeListener {
	SharedPreferences sharedPreferences;
	LinearLayout teleremoteLayout;
	int deviceCount;
	String[] deviceOnOff;
	ServerConnection sc = new ServerConnection();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.teleremote_fragment,
				container, false);

		sharedPreferences = this.getActivity().getSharedPreferences(
				"LoginDetails", Context.MODE_PRIVATE);
		teleremoteLayout = (LinearLayout) rootView
				.findViewById(R.id.teleremoteLayout);
		teleremoteLayout.setOrientation(LinearLayout.VERTICAL);
		new GetDeviceDetails().execute();
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.logout) {
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					getActivity());
			alertDialog.setTitle("Confirm Logout");
			alertDialog.setMessage("Are you sure you want to logout?");

			alertDialog.setPositiveButton("YES",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(getActivity(), "You clicked on YES",
									Toast.LENGTH_SHORT).show();
							Editor editor = sharedPreferences.edit();
							editor.clear();
							editor.commit();
							FragmentTransaction transaction = getFragmentManager()
									.beginTransaction();
							transaction.replace(R.id.container,
									new Fragment_Login()).commit();
						}
					});
			alertDialog.setNegativeButton("NO",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(getActivity(), "You clicked on NO",
									Toast.LENGTH_SHORT).show();
							dialog.cancel();
						}
					});
			alertDialog.show();
		}
		return super.onOptionsItemSelected(item);
	}

	class GetDeviceDetails extends AsyncTask<String, Integer, String[]> {

		String returnString, result;
		ProgressDialog pd;
		JSONTokener tokener;
		JSONObject jObject;
		String[] deviceNames;
		JSONArray jArrayOnOff, jArrayDeviceNames;
		String json;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog.show(getActivity(), "Getting Devices",
					"Please wait");

		}

		@Override
		protected String[] doInBackground(String... params) {
			// TODO Auto-generated method stub
			String scriptFileName = "getTeleremoteData.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(4);
			postParameters.add(new BasicNameValuePair("app_id",
					sharedPreferences.getString("app_id", "")));
			postParameters.add(new BasicNameValuePair("app_type",
					sharedPreferences.getString("app_type", "")));
			postParameters.add(new BasicNameValuePair("custom_id",
					sharedPreferences.getString("custom_id", "")));
			postParameters.add(new BasicNameValuePair("user_id",
					sharedPreferences.getString("user_id", "")));

			try {

				jObject = sc.Connection(scriptFileName, postParameters);
				jArrayDeviceNames = jObject.getJSONArray("device_names");
				jArrayOnOff = jObject.getJSONArray("on_off_values");
				deviceCount = jArrayOnOff.length();
				deviceNames = new String[deviceCount];
				deviceOnOff = new String[deviceCount];

				Editor editor = sharedPreferences.edit();
				editor.putInt("device_count", deviceCount);
				editor.commit();
				for (int i = 0; i < jArrayOnOff.length(); i++) {
					System.out.println(jArrayDeviceNames.get(i));
					System.out.println(jArrayOnOff.get(i));
					deviceNames[i] = jArrayDeviceNames.getString(i);
					deviceOnOff[i] = jArrayOnOff.getString(i);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return deviceNames;
		}

		@Override
		protected void onPostExecute(String[] result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pd.dismiss();
			generateSwitchView(result);
		}
	}

	@SuppressLint("NewApi")
	private void generateSwitchView(String[] deviceNames) {

		for (int j = 0; j < deviceCount; j++) {
			Switch deviceSwch = new Switch(getActivity());
			deviceSwch.setLayoutParams(new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			deviceSwch.setText(deviceNames[j]);
			if (deviceOnOff[j].equals("Y")) {
				deviceSwch.setChecked(true);
			} else {
				deviceSwch.setChecked(false);
			}
			deviceSwch.setId(j + 1);
			deviceSwch.setOnCheckedChangeListener(this);
			teleremoteLayout.addView(deviceSwch);
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (isChecked) {
			deviceOnOff[buttonView.getId() - 1] = "Y";
		} else {
			deviceOnOff[buttonView.getId() - 1] = "N";
		}
		String toSend = "";
		for (int i = 0; i < deviceCount; i++) {

			if (i == 0) {
				if (deviceCount == 1)
					toSend = deviceOnOff[i];
				else
					toSend = toSend + deviceOnOff[i] + "'";
			} else if (i == deviceCount - 1 || deviceCount == 2) {
				toSend = toSend + ",'" + deviceOnOff[i];
			} else {
				toSend = toSend + ",'" + deviceOnOff[i] + "'";
			}
		}
		System.out.println("INSERTEDDDDDDDDDD=" + toSend);
		new SendOnOffInfo().execute(toSend);
	}

	class SendOnOffInfo extends AsyncTask<String, Integer, String> {

		String returnString, result;
		ProgressDialog pd;
		JSONTokener tokener;
		JSONObject jObject;
		String[] deviceNames;
		JSONArray jArrayOnOff, jArrayDeviceNames;
		String json;
		String res = "";

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog.show(getActivity(), "Setting Device",
					"Please wait");
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					SendOnOffInfo.this.cancel(true);

				}
			});

		}

		@Override
		protected String doInBackground(String... params)
		{
			String scriptFileName = "sendOnOffInfo.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(4);
			postParameters.add(new BasicNameValuePair("app_id",
					sharedPreferences.getString("app_id", "")));
			postParameters.add(new BasicNameValuePair("app_type",
					sharedPreferences.getString("app_type", "")));
			postParameters.add(new BasicNameValuePair("kit_id",
					sharedPreferences.getString("kit_id", "")));
			postParameters.add(new BasicNameValuePair("user_id",
					sharedPreferences.getString("user_id", "")));
			// postParameters.add(new
			// BasicNameValuePair("custom_id",sharedPreferences.getString("custom_id",
			// "")));

			postParameters.add(new BasicNameValuePair("val_string", params[0]));

			try 
			{

				jObject = sc.Connection(scriptFileName, postParameters);
				res = jObject.getString("status");

			} catch (JSONException e) {
				e.printStackTrace();
			}

			return res;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pd.dismiss();
			if (result.equals("yes")) {
				Toast.makeText(getActivity(), "inserted", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getActivity(), "not inserted",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}