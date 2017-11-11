package com.eespl.iotapp;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Fragment_Login extends Fragment implements OnClickListener {

	Button btnSubmit;
	EditText edtUser;
	EditText edtPass;
	boolean isConnected = false;
	String username, password;
	TextView txtLogin;
	SharedPreferences sharedPreferences;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.login_fragment, container,
				false);
		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setSubtitle(null);
		sharedPreferences = this.getActivity().getSharedPreferences(
				"LoginDetails", Context.MODE_PRIVATE);
		isConnected = checkNetwork();
		getActivity().getActionBar().show();
		btnSubmit = (Button) rootView.findViewById(R.id.btnLogin);
		edtUser = (EditText) rootView.findViewById(R.id.edtUser);
		edtPass = (EditText) rootView.findViewById(R.id.edtPass);
		txtLogin = (TextView) rootView.findViewById(R.id.txtLogin);
		Typeface custom_font = Typeface.createFromAsset(getActivity()
				.getAssets(), "fonts/Marlboro.ttf");
		txtLogin.setTypeface(custom_font);
		btnSubmit.setTypeface(custom_font);
		btnSubmit.setOnClickListener(this);
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
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {

		case R.id.btnLogin:

			username = edtUser.getText().toString();
			password = edtPass.getText().toString();

			if (username.equals("".trim())) {

				edtUser.setError("Fill username first");

			} else if (password.equals("".trim())) {

				edtPass.setError("Fill password first");
			} else if (isConnected == false) {

				Toast.makeText(getActivity(),
						"Check your network connection" + " " + isConnected,
						Toast.LENGTH_LONG).show();

			} else {
				Toast.makeText(getActivity(),
						"Check your network connection" + " " + isConnected,
						Toast.LENGTH_LONG).show();
				new LoginTask().execute();
			}
			break;

		}

	}

	class LoginTask extends AsyncTask<String, Integer, String> {

		String returnString, result;
		ProgressDialog pd;
		// JSONTokener tokener;
		JSONObject jObject;
		String json;
		ServerConnection sc = new ServerConnection();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog
					.show(getActivity(), "Logging in", "Please wait");
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					// TODO Auto-generated method stub
					LoginTask.this.cancel(true);

				}
			});
		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			String scriptFileName = "login.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(2);
			postParameters.add(new BasicNameValuePair("user", edtUser.getText()
					.toString()));
			postParameters.add(new BasicNameValuePair("pswd", edtPass.getText()
					.toString()));

			try {

				jObject = sc.Connection(scriptFileName, postParameters);
				returnString = jObject.getString("status");

				System.out.println("status=" + returnString);
				if (returnString.equals("yes")) {

					String user_id = jObject.getString("user_id");
					String app_id = jObject.getString("app_id");
					String kit_id = jObject.getString("kit_id");
					String app_name = jObject.getString("app_name");
					String app_type = jObject.getString("app_type");
					String app_image = jObject.getString("app_image");
					String app_com_type = jObject.getString("app_com_type");
					String custom_id = jObject.getString("custom_id");
					String privilege = jObject.getString("privilege");

					System.out.println("Login Successful");
					Editor editor = sharedPreferences.edit();
					editor.putString("username", username);
					editor.putString("password", password);
					editor.putString("user_id", user_id);
					editor.putString("app_id", app_id);
					editor.putString("kit_id", kit_id);
					editor.putString("app_name", app_name);
					editor.putString("app_type", app_type);
					editor.putString("app_image", app_image);
					editor.putString("app_com_type", app_com_type);
					editor.putString("custom_id", custom_id);
					editor.putString("privilege", privilege);
					editor.commit();

				} else if (returnString.equals("no")) {

					System.out.println("Login not Successful");

				}
			} catch (JSONException e) {
				Log.e("log_tag", "Error parsing data " + e.toString());
				e.printStackTrace();
			}

			catch (Exception e) {
				Log.e("log_tag", "Error in http connection!!" + e.toString());
			}
			return returnString;
		}

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pd.dismiss();
			if (result.equals("yes")) {
				Toast.makeText(getActivity(), "Login Successful",
						Toast.LENGTH_LONG).show();
				FragmentTransaction transaction = getActivity()
						.getFragmentManager().beginTransaction();
				
				transaction.replace(R.id.container, new Fragment_Details());
				transaction.commit();
			} else {
				Toast.makeText(getActivity(), "Login Unsuccessful",
						Toast.LENGTH_LONG).show();
			}
		}
	}

}
