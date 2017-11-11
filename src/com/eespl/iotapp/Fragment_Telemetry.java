package com.eespl.iotapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.eespl.iotapp.Fragment_Teleremote.SendOnOffInfo;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

public class Fragment_Telemetry extends Fragment implements OnClickListener,
		OnItemSelectedListener {
	EditText edtFromDate, edtToDate,edcritical;
	ImageView ivFromDate, ivToDate;
int criticalvalue,lastvalue;
	TableLayout table_layout;
	Button btnSub,btnExport,btplus;
	final String CRITICAL_VALUES = "Current Values";
	Boolean flag = false, flag2 = false, privG = false, privT = false;;
	Spinner spnParameters;
	String c;
	String comment_value;
	GraphView graphView;
	ArrayList<String> listOfCommentValues, listOfParameterNames,
			listOfGraphValues, listOfCriticalValues, listOfLL, listOfUL,listOfGraphDate,listOfGraphTime;
	String[] arrayOfParameters;
	ArrayAdapter<String> adapter;
	LineGraphSeries<DataPoint> seriesLine;
	BarGraphSeries<DataPoint> seriesBar;
	File file, dir;
	FileOutputStream outputStream = null;
	
	SharedPreferences sharedPreferences;
	int ivNo,rows,cols=4;
	
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
		View rootView = inflater.inflate(R.layout.telemetry_fragment,
				container, false);

		sharedPreferences = this.getActivity().getSharedPreferences(
				"LoginDetails", Context.MODE_PRIVATE);
		edcritical=(EditText)rootView.findViewById(R.id.edcritical);
		
		edtFromDate = (EditText) rootView.findViewById(R.id.edtFromDate);
		edtToDate = (EditText) rootView.findViewById(R.id.edtToDate);
		ivFromDate = (ImageView) rootView.findViewById(R.id.ivFromDate);
		ivToDate = (ImageView) rootView.findViewById(R.id.ivToDate);
		btnSub = (Button) rootView.findViewById(R.id.btnSub);
		btnExport = (Button) rootView.findViewById(R.id.btnExport);
		btplus=(Button)rootView.findViewById(R.id.btcritical);
		btplus.setOnClickListener(this);
		spnParameters = (Spinner) rootView.findViewById(R.id.spnParameters);
		graphView = (GraphView) rootView.findViewById(R.id.graphView);
		table_layout = (TableLayout)rootView.findViewById(R.id.tableLayout1);
		listOfGraphValues = new ArrayList<String>();
		listOfGraphDate= new ArrayList<String>();
		listOfGraphTime= new ArrayList<String>();
		
		listOfCommentValues = new ArrayList<String>();
		listOfParameterNames = new ArrayList<String>();
		listOfCriticalValues = new ArrayList<String>();
		listOfLL = new ArrayList<String>();
		listOfUL = new ArrayList<String>();

		String[] privilages = (sharedPreferences.getString("privilege", ""))
				.split("_");
		for (String priv : privilages) 
		{
			if (priv.equals("G"))
			{
				privG = true;
			}
			if (priv.equals("T"))
			{
				privT = true;
			}
		}
		if (sharedPreferences.contains("parameter_size")) {
			int parameter_size = sharedPreferences.getInt("parameter_size", 0);
			for (int i = 0; i < parameter_size; i++) {
				listOfParameterNames.add(i,
						sharedPreferences.getString("para_" + i, ""));
				listOfCommentValues.add(i,
						sharedPreferences.getString("comm_" + i, ""));
			}
			listOfParameterNames.add(CRITICAL_VALUES);
			arrayOfParameters = new String[listOfCommentValues.size()];
			arrayOfParameters = listOfCommentValues.toArray(arrayOfParameters);
			comment_value = listOfCommentValues.get(0);
			adapter = new ArrayAdapter<String>(getActivity(),
					R.layout.spinner_layout, listOfParameterNames);
			spnParameters.setAdapter(adapter);
			flag2 = false;
		} else {
			flag2 = true;
		}
		Bundle bundle = getArguments();
		if (bundle != null) {
			listOfGraphValues = bundle.getStringArrayList("graph_values_list");
			listOfGraphDate = bundle.getStringArrayList("graph_date_list");
			listOfGraphTime = bundle.getStringArrayList("graph_time_list");
			if (privG == true)
				plotGraph(listOfParameterNames.get(Integer
						.parseInt(comment_value) - 1));
			else {
				graphView.setVisibility(View.GONE);
				Toast.makeText(getActivity(), "No Privilage of Graph",
						Toast.LENGTH_LONG).show();
			}

		} else {
			if (flag2 == false) {
				new GetGraphValues().execute();
			}
		}
		edtFromDate.setOnClickListener(this);
		edtToDate.setOnClickListener(this);
		ivFromDate.setOnClickListener(this);
		ivToDate.setOnClickListener(this);
		btnSub.setOnClickListener(this);
		btnExport.setOnClickListener(this);
		spnParameters.setOnItemSelectedListener(this);
		

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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.ivFromDate:
			ivNo = 0;
			selectDate();
			break;
		case R.id.ivToDate:
			ivNo = 1;
			selectDate();
			break;
		case R.id.edtFromDate:
			ivNo = 0;
			selectDate();
			break;
		case R.id.edtToDate:
			ivNo = 1;
			selectDate();
			break;
		case R.id.btnSub:
	
			if (((String) spnParameters.getSelectedItem())
					.equals(CRITICAL_VALUES)) {
				Toast.makeText(getActivity(), "Critical Values Graph",
						Toast.LENGTH_SHORT).show();
				new GetCriticalGraphValues().execute();

			} else {
				new GetGraphValues().execute();
			}
			break;
		case R.id.btnExport:
			exportExcel((String) spnParameters.getSelectedItem());
			break;
		case R.id.btcritical:
		{
			criticalvalue=Integer.parseInt(edcritical.getText().toString());
			Toast.makeText(getActivity(),"Value entered is "+criticalvalue,Toast.LENGTH_LONG).show();
			
		if(lastvalue>=criticalvalue)
		{
			Toast.makeText(getActivity(),"Device should be turned off now",Toast.LENGTH_LONG).show();
			new TurnoffDevice().execute("N");
		}
			
			break;
		}
		}
	}

	
	class TurnoffDevice extends AsyncTask<String, Integer, String> {

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
					TurnoffDevice.this.cancel(true);

				}
			});

		}

		@Override
		protected String doInBackground(String... params)
		{
			String scriptFileName = "sendOnOffInfo_new.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(1);
		
			postParameters.add(new BasicNameValuePair("kit_id",
					sharedPreferences.getString("kit_id", "")));
			postParameters.add(new BasicNameValuePair("user_id",
					sharedPreferences.getString("user_id", "")));

			postParameters.add(new BasicNameValuePair("val_string", params[0]));
			// postParameters.add(new
			// BasicNameValuePair("custom_id",sharedPreferences.getString("custom_id",
			// "")));

			

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
				Toast.makeText(getActivity(), "Device is turned off", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(getActivity(), "not turned off",
						Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	
	@SuppressLint("NewApi") private void exportExcel(String parameter) {
		// TODO Auto-generated method stub
		if (isExternalStorageWritable()) {
			try {
				dir = new File(Environment.getExternalStorageDirectory()
						+ "/IOT Excel Data");
				dir.mkdir();
				file = new File(dir, "IOT_" + parameter + ".xls");

				if (file.exists() == false) {

					writeExcelHeader("Sr. No.", "Value", "Created Date",
							"Created Time");
					for(int i=0;i<listOfGraphValues.size();i++){
						writeExcelData((i+1)+"",listOfGraphValues.get(i),listOfGraphDate.get(i),listOfGraphTime.get(i));
					}
				}
				Toast.makeText(getActivity(), "Exported Successfully",
						Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
	
	private void writeExcelHeader(String h1, String h2, String h3, String h4)
			throws IOException {
		Workbook wb = new HSSFWorkbook();
		 
	        Cell c = null;
	 
	        //Cell style for header row
	        CellStyle cs = wb.createCellStyle();
	        cs.setFillForegroundColor(HSSFColor.LIME.index);
	        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
	        
	        //New Sheet
	   
	      Sheet  sheet1 = wb.createSheet("IOT");
	 
	        // Generate column headings
	      Row row = sheet1.createRow(0);
	 
	        c = row.createCell(0);
	        c.setCellValue(h1);
	        c.setCellStyle(cs);
	 
	        c = row.createCell(1);
	        c.setCellValue(h2);
	        c.setCellStyle(cs);
	 
	        c = row.createCell(2);
	        c.setCellValue(h3);
	        c.setCellStyle(cs);
	        
	        c = row.createCell(3);
	        c.setCellValue(h4);
	        c.setCellStyle(cs);
	        
	       
	 
	        sheet1.setColumnWidth(0, (15 * 500));
	        sheet1.setColumnWidth(1, (15 * 500));
	        sheet1.setColumnWidth(2, (15 * 500));
	        sheet1.setColumnWidth(3, (15 * 500));
	       
		
		outputStream = new FileOutputStream(file, true);
		wb.write(outputStream);
		outputStream.close();
	}
	private void writeExcelData(String d, String e, String f, String g)
			throws IOException {
		
		FileInputStream fileIn = new FileInputStream(file);

        HSSFWorkbook workbook = new HSSFWorkbook(fileIn);
        HSSFSheet sheet = workbook.getSheetAt(0);
        Cell cell = null;
        
        int rowCount=sheet.getLastRowNum();
        cell = sheet.createRow(rowCount+1).createCell(0);
        cell.setCellValue(d);
        cell = sheet.getRow(rowCount+1).createCell(1);
        cell.setCellValue(e);
        cell = sheet.getRow(rowCount+1).createCell(2);
        cell.setCellValue(f);
        cell = sheet.getRow(rowCount+1).createCell(3);
        cell.setCellValue(g);
        
        
        fileIn.close();

        FileOutputStream outFile =new FileOutputStream(file);
        workbook.write(outFile);
        outFile.close();
	}


	public void selectDate() {
		DialogFragment newFragment = new SelectDateFragment();
		newFragment.show(getFragmentManager(), "DatePicker");
	}

	public void populateSetDate(int year, int month, int day) {
		if (ivNo == 0)
			edtFromDate.setText(year + "-" + month + "-" + day);
		else if (ivNo == 1)
			edtToDate.setText(year + "-" + month + "-" + day);
	}

	public class SelectDateFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar calendar = Calendar.getInstance();
			int yy = calendar.get(Calendar.YEAR);
			int mm = calendar.get(Calendar.MONTH);
			int dd = calendar.get(Calendar.DAY_OF_MONTH);
			return new DatePickerDialog(getActivity(), this, yy, mm, dd);
		}

		public void onDateSet(DatePicker view, int yy, int mm, int dd) {
			populateSetDate(yy, mm + 1, dd);
		}
	}
	
	
	
	
	
	class GetGraphValues extends AsyncTask<String, Integer, String> {

		String returnString, result;
		ProgressDialog pd;
		JSONTokener tokener;
		JSONObject jObject, jObjectValues;
		JSONArray jArray1,jArray2,jArray3;;
		String json;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog.show(getActivity(), "Getting Graph Values",
					"Please wait");
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					GetGraphValues.this.cancel(true);
				}
			});
		}

		@Override
		protected String doInBackground(String... params) {
			String scriptFileName = "getGraphValuesOnly.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(4);
			postParameters.add(new BasicNameValuePair("kit_id",
					sharedPreferences.getString("kit_id", "")));
			postParameters.add(new BasicNameValuePair("parameter_number",
					comment_value));
			postParameters.add(new BasicNameValuePair("from_date", edtFromDate
					.getText().toString()));
			postParameters.add(new BasicNameValuePair("to_date", edtToDate
					.getText().toString()));

			try {

				jObject = sc.Connection(scriptFileName, postParameters);
				jArray1 = jObject.getJSONArray("graph_values");
				jArray2 = jObject.getJSONArray("entry_date");
				jArray3 = jObject.getJSONArray("entry_time");

				listOfGraphValues.clear();
				listOfGraphDate.clear();
				listOfGraphTime.clear();
				for (int i = 0; i < jArray1.length(); i++) 
				{
					
					listOfGraphValues.add(i,(String)jArray1.get(i));
					listOfGraphDate.add(i,(String)jArray2.get(i));
					listOfGraphTime.add(i,(String)jArray3.get(i));
					//String c= listOfGraphValues.get(1);
					//System.out.println("Value of 1st item is "+c);
					//jObjectValues = jArray1.getJSONObject(i);
					//listOfGraphValues.add(i,
					//		jObjectValues.getString("parameter_value"));
					//System.out.println("VALUES OF GRAPH="
						//	+ jObjectValues.getString("parameter_value"));
				}
				  c= listOfGraphValues.get(jArray1.length()-10);
				Log.d("upma","value of last item is "+c);
				 lastvalue=	Integer.parseInt(c);
				
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
			if (privG == true)
				plotGraph(listOfParameterNames.get(Integer
						.parseInt(comment_value) - 1));
			else {
				graphView.setVisibility(View.GONE);
				Toast.makeText(getActivity(), "No Privilage of Graph",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class GetCriticalGraphValues extends AsyncTask<String, Integer, String> {

		String returnString, result;
		ProgressDialog pd;
		JSONTokener tokener;
		JSONObject jObject, jObjectValues;
		JSONArray jArray, jArrayUL, jArrayLL;
		String json;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			pd = ProgressDialog.show(getActivity(), "Getting Graph Values",
					"Please wait");
			pd.setCancelable(true);
			pd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					GetCriticalGraphValues.this.cancel(true);
				}
			});
		}

		@Override
		protected String doInBackground(String... params) {
			String scriptFileName = "getCriticalGraphValues.php";

			List<NameValuePair> postParameters = new ArrayList<NameValuePair>(4);
			postParameters.add(new BasicNameValuePair("kit_id",
					sharedPreferences.getString("kit_id", "")));
			for (int i = 0; i < arrayOfParameters.length; i++)
				postParameters.add(new BasicNameValuePair("parameters[]",
						arrayOfParameters[i]));
			postParameters.add(new BasicNameValuePair("user_id",
					sharedPreferences.getString("user_id", "")));
			postParameters.add(new BasicNameValuePair("app_id",
					sharedPreferences.getString("app_id", "")));
			postParameters.add(new BasicNameValuePair("app_type",
					sharedPreferences.getString("app_type", "")));
			postParameters.add(new BasicNameValuePair("from_date", edtFromDate
					.getText().toString()));
			postParameters.add(new BasicNameValuePair("to_date", edtToDate
					.getText().toString()));

			try {

				jObject = sc.Connection(scriptFileName, postParameters);
				jArray = jObject.getJSONArray("graph_values");
				jArrayUL = jObject.getJSONArray("parameter_range_max");
				jArrayLL = jObject.getJSONArray("parameter_range_min");
				listOfCriticalValues.clear();
				for (int i = 0; i < jArray.length(); i++) {
					jObjectValues = jArray.getJSONObject(i);
					listOfCriticalValues.add(i,
							jObjectValues.getString("parameter_value"));
					listOfLL.add(i, jArrayLL.getString(i));
					listOfUL.add(i, jArrayUL.getString(i));
					System.out.println("VALUES OF GRAPH="
							+ jObjectValues.getString("parameter_value"));
					System.out.println("VALUES OF LL=" + jArrayLL.getString(i));
					System.out.println("VALUES OF UL=" + jArrayUL.getString(i));

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
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			pd.dismiss();

			if (privG == true)
				plotBarGraph("Current Values");
			else {
				graphView.setVisibility(View.GONE);
				Toast.makeText(getActivity(), "No Privilage of Graph",
						Toast.LENGTH_LONG).show();
			}

		}
	}

	private void plotGraph(String graphTitle) {
		DataPoint[] data = new DataPoint[listOfGraphValues.size()];
		for (int i = 0; i < listOfGraphValues.size(); i++) {
			data[i] = new DataPoint(i, Double.parseDouble(listOfGraphValues
					.get(i)));
			System.out.println("PLOTTING VALUES ARE="
					+ Double.parseDouble(listOfGraphValues.get(i)));
		}
		seriesLine = new LineGraphSeries<DataPoint>(data);
		graphView.removeAllSeries();
		graphView.getViewport().setYAxisBoundsManual(true);
		graphView.getViewport().setMinY(0.0);
		graphView.getViewport().setMaxY(200.0);
		// graphView.getViewport().setScrollable(true);
		graphView.setTitle(graphTitle);
		graphView.setTitleColor(Color.rgb(63, 169, 231));
		graphView.addSeries(seriesLine);
		
		System.out.println("SIZE of values=="+listOfGraphValues.size());
		
		table_layout.removeAllViews();
	    BuildTable(listOfGraphValues.size(), cols);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long arg3) {
		// TODO Auto-generated method stub
		if (flag == true
				&& (!(listOfParameterNames.get(position)
						.equals(CRITICAL_VALUES)))) {
			comment_value = listOfCommentValues.get(position);
		} else {
			flag = true;
		}
		if (parent.getItemAtPosition(position).equals(CRITICAL_VALUES)) {
			Toast.makeText(getActivity(), "Critical Values Graph",
					Toast.LENGTH_SHORT).show();
			new GetCriticalGraphValues().execute();

		}

	}

	 private void BuildTable(int rows, int cols) {
		 String []headers={"Sr.No.","Value","Created Date","Created Time"};
		 TableRow hrow = new TableRow(getActivity());
		  hrow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
		     LayoutParams.WRAP_CONTENT));
		  for (int h = 1; h <= cols; h++) {

			    TextView tv = new TextView(getActivity());
			    tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
			      LayoutParams.WRAP_CONTENT));
			    tv.setBackgroundResource(R.drawable.cell_shape);
			    tv.setPadding(5, 5, 5, 5);
			    tv.setText(headers[h-1]);
			    tv.setTextColor(Color.parseColor("#31A5E7"));
			    tv.setGravity(Gravity.CENTER);
			    hrow.addView(tv);

			   }
		  table_layout.addView(hrow);
		   
		  // outer for loop
		  for (int i = 1; i <= rows; i++) {

		   TableRow row = new TableRow(getActivity());
		   row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
		     LayoutParams.WRAP_CONTENT));
		   
		   // inner for loop
		   for (int j = 1; j <= cols; j++) {

		    TextView tv = new TextView(getActivity());
		    tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
		      LayoutParams.WRAP_CONTENT));
		    tv.setBackgroundResource(R.drawable.cell_shape);
		    tv.setPadding(5, 5, 5, 5);
		    tv.setText("");
		    tv.setTextColor(Color.parseColor("#31A5E7"));
		    tv.setGravity(Gravity.CENTER);
		    if(j==1){
		    	tv.setText(i+"");
		    }
		    if(j==2){
		    	tv.setText(listOfGraphValues.get(i-1));
		    	
		    }if(j==3){
		    	tv.setText(listOfGraphDate.get(i-1));
		    	
		    }if(j==4){
		    	tv.setText(listOfGraphTime.get(i-1));
		    	
		    }

		    row.addView(tv);

		   }

		   table_layout.addView(row);
		   System.out.println("ROW ADDED");

		  }
		 }
	private void plotBarGraph(String graphTitle) {
		// TODO Auto-generated method stub

		DataPoint[] data = new DataPoint[listOfCriticalValues.size()];
		for (int i = 0; i < listOfCriticalValues.size(); i++) {
			data[i] = new DataPoint(i, Double.parseDouble(listOfCriticalValues
					.get(i)));
			System.out.println("PLOTTING VALUES ARE="
					+ Double.parseDouble(listOfGraphValues.get(i)));
		}

		seriesBar = new BarGraphSeries<DataPoint>(data);
		seriesBar.setDrawValuesOnTop(true);
		seriesBar.setValuesOnTopColor(Color.RED);
		seriesBar.setSpacing(20);
		seriesBar.setValueDependentColor(new ValueDependentColor<DataPoint>() {
			int val = 0;

			@Override
			public int get(DataPoint data) {
				int clr = Color.rgb(0, 255, 0);
				if (data.getY() < Integer.parseInt(listOfLL.get(val))
						|| data.getY() > Integer.parseInt(listOfUL.get(val))) {
					clr = Color.rgb(255, 0, 0);
				}
				val++;
				if (val == listOfCriticalValues.size()) {
					val = 0;
				}
				return clr;
			}
		});
		graphView.removeAllSeries();
		graphView.setTitle(graphTitle);
		graphView.setTitleColor(Color.rgb(63, 169, 231));
		graphView.addSeries(seriesBar);

	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub

	}

}
