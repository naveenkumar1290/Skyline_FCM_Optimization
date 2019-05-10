package planet.info.skyline;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import planet.info.skyline.controller.AppController;
import planet.info.skyline.util.Utils;

import static planet.info.skyline.util.Utility.URL_EP1;


public class Scanworklocationstart extends BaseActivity {
SharedPreferences sp;
Editor ed;
	 String arecode;
	int count;
	int punnu=0;
	List<String> arename=new ArrayList<String>();
	List<String> area_code=new ArrayList<String>();
	 ProgressDialog ringProgressDialog ;
	AlertDialog alertDialog;
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_scanworklocationstart);
		ringProgressDialog	= new ProgressDialog(Scanworklocationstart.this);
		ringProgressDialog.setMessage("Kindly Wait..");
		ringProgressDialog.setCancelable(false);
		sp = getApplicationContext().getSharedPreferences("skyline",
				MODE_PRIVATE);
		ed = sp.edit();
		try
		{
		Intent ii=getIntent();
		int tot=ii.getIntExtra("tot",0);
		ed.putInt("working_with_crate",tot).commit();
		}
		catch(Exception e)
		{
			
		}
		Send();
	}
	public void showscannewbin() {
		final Dialog showd = new Dialog(Scanworklocationstart.this);
		showd.requestWindowFeature(Window.FEATURE_NO_TITLE);
		showd.setContentView(R.layout.alerfinishwithjob_new);
		showd.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		showd.setCancelable(false);
		TextView nofo = (TextView) showd.findViewById(R.id.Btn_No);
		TextView yesfo = (TextView) showd.findViewById(R.id.Btn_Yes);
		ImageView close = (ImageView) showd.findViewById(R.id.close);
		TextView texrtdesc = (TextView) showd.findViewById(R.id.texrtdesc);
		texrtdesc.setText("Please Scan the Work Area");
		nofo.setText("  OK  ");
		yesfo.setText("Cancel");
		yesfo.setVisibility(View.GONE);

		TextView textviewheader = (TextView) showd.findViewById(R.id.textView1rr);
		textviewheader.setText("please select");
		nofo.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {

				showd.dismiss();
				try {

					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(intent, 1);

				} catch (Exception e)
				{
					Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
					Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
					startActivity(marketIntent);

				}
			}
		});
		close.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showd.dismiss();
				Scanworklocationstart.this.finish();
			}
		});
		yesfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showd.dismiss();
				Scanworklocationstart.this.finish();
			}
		});

		showd.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,Intent intent) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, intent);
		if (resultCode == RESULT_OK) {
			if (requestCode == 1)
			{
				String contents = intent.getStringExtra("SCAN_RESULT");
				String hom = contents;// "exhibitpower.com/admin/inventory/qrcrates1.php?act=edit_id=671_client=224_loc=S14-3-0";
				Log.d("BHANU",hom);
				String result = hom.substring(hom.indexOf("_loc=") + 5, hom.length());
				Log.d("BHANU",result);
				ed.putString("worklocation", result).commit();
				String name_client=sp.getString("client_name","");
				Utils.alert_with_intent_withoutintent(Scanworklocationstart.this,"Begin work in  "+result, "",name_client);
				String sdj=sp.getString("starttimenew","");
			}
		}
		else
		{
			Scanworklocationstart.this.finish();
		}
	}


	public void diloge_for_workArea()
	{
		final Dialog choose_for_scan = new Dialog(Scanworklocationstart.this);
		choose_for_scan.requestWindowFeature(Window.FEATURE_NO_TITLE);
		choose_for_scan.setContentView(R.layout.area_diloge_new);

		choose_for_scan.getWindow().setBackgroundDrawable(
				new ColorDrawable(android.graphics.Color.TRANSPARENT));
		choose_for_scan.setCancelable(false);
		ImageView closebtn = (ImageView) choose_for_scan.findViewById(R.id.close);
		final AutoCompleteTextView company_name = (AutoCompleteTextView)choose_for_scan.findViewById(R.id.company);
		Button btn_GO = (Button) choose_for_scan.findViewById(R.id.go_button);
		Button scann_swo = (Button) choose_for_scan.findViewById(R.id.Scann);

		ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(Scanworklocationstart.this, android.R.layout.simple_list_item_1,arename);
		company_name.setAdapter(dataAdapter1);

		company_name.setDropDownHeight(550);

		company_name.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {

				company_name.showDropDown();
				return false;
			}
		});

		company_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				punnu=12;
				int index = arename.indexOf(company_name.getText().toString());
				arecode = area_code.get(index);


			}
		});

		btn_GO.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (company_name.getText().toString().equals("--Select--") || (company_name.getText().length() == 0))
				{
					Toast.makeText(Scanworklocationstart.this, "Please enter Area", Toast.LENGTH_SHORT).show();
				}
				else
				{

					if(punnu==12)
					{
						Log.d("BHANU", arecode);
						ed.putString("worklocation", arecode).commit();
						String name_client = sp.getString("client_name", "");
						Utils.alert_with_intent_withoutintent(Scanworklocationstart.this, "Begin work in  " + arecode, "", name_client);
						String sdj = sp.getString("starttimenew", "");
					}
					else
					{

						Toast.makeText(Scanworklocationstart.this, "Please enter Correct Area", Toast.LENGTH_SHORT).show();

					}
				}

			}
		});


		closebtn.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});
		scann_swo.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				showscannewbin();
			}
		});

		choose_for_scan.show();
	}


	public void Send( )   ///by aman kaushik
	{
		ringProgressDialog.show();
		JsonObjectRequest updatess = new JsonObjectRequest(Request.Method.GET, URL_EP1+"/web_service_workarea.php",
				null, new Response.Listener<JSONObject>() {

			@Override
			public void onResponse(JSONObject jsono) {
				ringProgressDialog.dismiss();
				try
				{
					arename.clear();
					area_code.clear();
					JSONArray array=jsono.getJSONArray("cds");
					for(int i=0;i<array.length();i++)
					{
						JSONObject obj=array.getJSONObject(i);
						String areacodde= obj.getString("area_code");
						String area_name = obj.getString("area_name");

						arename.add(area_name);
						area_code.add(areacodde);
					}
			//		arename.add(0, "--Select--");
			//		area_code.add(0,"--Select--");
					diloge_for_workArea();

				}
				catch(Exception e)
				{
					e.printStackTrace();
				}

			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError arg0) {
				//

			}
		});
		AppController.getInstance().addToRequestQueue(updatess);
	}

/*class get_company_job_id extends AsyncTask<Void, Void, Void>
	{

		final ProgressDialog ringProgressDialog = new ProgressDialog(Scanworklocationstart.this);

		@Override
		protected Void doInBackground(Void... strings)
		{
			Getcompany_name();
			return null;
		}

		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			//  ringProgressDialog.setTitle("Kindly wait ...")
			ringProgressDialog.setMessage("Kindly wait");
			ringProgressDialog.setCancelable(false);
			ringProgressDialog.show();
		}
		@Override
		protected void onPostExecute(Void aVoid)
		{
			super.onPostExecute(aVoid);
			ringProgressDialog.dismiss();


		}
	}*/

}
