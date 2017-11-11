package com.eespl.iotapp;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;

public class MainActivity extends Activity {
	
	
	SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#31A5E7")));
		getActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>IOTApp </font>"));
		sharedPreferences = getSharedPreferences("LoginDetails", Context.MODE_PRIVATE);
		if(savedInstanceState == null){
	    FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.add(R.id.container, new Fragment_Splash()).commit();
		}
	}
	 
}
