package planet.info.skyline;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import planet.info.skyline.adapter.CompanyNameAdapter;
import planet.info.skyline.adapter.JobNameAdapter;
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.util.Utility.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.URL_EP2;

public class SelectCompanyActivityNew extends AppCompatActivity {
    String userRole = "";
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    int count;
    int pankajtester_chutya;
    String job = "", comp_id_name = "";
    int aman_geneious = 0;

    ArrayList<HashMap<String, String>> list_AllJobs, list_AllJobs_forIndex;
    List<String> job_Name_list_Desc = new ArrayList<String>();
    List<String> company_id_list = new ArrayList<String>();
    List<String> company_Name_list = new ArrayList<String>();
    List<String> company_Logo_list = new ArrayList<String>();
    List<String> company_id_list_forIndex;
    List<String> company_Name_list_forIndex;
    List<String> job_Name_list_Desc_forIndex;
    CompanyNameAdapter companyNameAdapter;
    List<String> job_id_list = new ArrayList<String>();
    List<String> job_Name_list = new ArrayList<String>();
    List<String> job_des_list = new ArrayList<String>();
    List<String> status_list = new ArrayList<String>();
    List<String> show_list = new ArrayList<String>();
    List<String> jobtype_list = new ArrayList<String>();

    Dialog  dialog_ScanSWO;

    AutoCompleteTextView company_name;
    AutoCompleteTextView et_JobName;
    AlertDialog alertDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_company_new);

        sp = getApplicationContext().getSharedPreferences("skyline", getApplicationContext().MODE_PRIVATE);
        ed = sp.edit();
        userRole = sp.getString(Utility.LOGIN_USER_ROLE, "");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button Btn_SearchByJob = (Button) findViewById(R.id.Btn_SearchByJob);
        Button Btn_SearchByCompany = (Button) findViewById(R.id.Btn_SearchByCompany);
        Button scann_swo = (Button) findViewById(R.id.scan);






                if (new ConnectionDetector(SelectCompanyActivityNew.this).isConnectingToInternet()) {
                    new get_company_name().execute();
                } else {
                    Toast.makeText(SelectCompanyActivityNew.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                }



    }

    public void Getcompany_name()   ///by aman kaushik
    {
        String dealerId = sp.getString(Utility.DEALER_ID, "");

        count = 0;
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + "bindClientByDealer";
        final String METHOD_NAME = "bindClientByDealer";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("dealerID", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int j = 0; j < ks.getPropertyCount(); j++) {
                ks.getProperty(j);
            }
            String recved = ks.toString();
            if (recved.contains("Job not found!")) {
                count = 1;
            } else {

                String[] aa = recved.split("=");
                String a = aa[1];
                String b[] = a.split(";");
                String k = b[0];
                Log.d("BHANU", k);
                Log.d("punnu_chutiya", k);
                JSONObject jsonObject = new JSONObject(k);
                JSONArray jsonArray = jsonObject.getJSONArray("cds");
                company_id_list.clear();
                company_Name_list.clear();
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String comapny_id = jsonObject1.getString("id");
                    String company_name = jsonObject1.getString("Ename");
                    String company_logo = jsonObject1.getString("Imagepath");
                    //     company_id_list.put(company_name,comapny_id);

                    company_id_list.add(comapny_id);
                    company_Name_list.add(company_name);
                    company_Logo_list.add(company_logo);
                }
                //  company_id_list.add(0, "--Select--");
                //  company_Name_list.add(0,"--Select--");

                company_id_list_forIndex = new ArrayList<>(company_id_list);
                company_Name_list_forIndex = new ArrayList<>(company_Name_list);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dialog_SearchByCompany()    /////by aman kaushik
    {
        pankajtester_chutya = 0;


        company_name = (AutoCompleteTextView) findViewById(R.id.company);
      //  job_name = (AutoCompleteTextView) findViewById(R.id.et_job);

        Button btn_GO = (Button) findViewById(R.id.go_button);






        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, company_Name_list);
        //company_name.setAdapter(dataAdapter);
        companyNameAdapter = new CompanyNameAdapter(SelectCompanyActivityNew.this,
                android.R.layout.simple_list_item_1, company_Name_list);
        company_name.setAdapter(companyNameAdapter);
        company_name.setDropDownHeight(550);
// ontouch for et_company name-->
        company_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                company_name.showDropDown();
                /*to clear autocomplete*/
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (company_name.getRight() - company_name.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            company_name.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                /**/

                return false;
            }
        });

        //ontouch for et_job name---->


        company_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (company_name.getText().equals("--Select--") || (company_name.getText().length() == 0)) {

                } else {
                    pankajtester_chutya = 1;
                    //   String checc = company_name.getText().toString();
                    int index = company_Name_list_forIndex.indexOf(company_name.getText().toString());
                    comp_id_name = company_id_list_forIndex.get(index);


                    if (new ConnectionDetector(SelectCompanyActivityNew.this).isConnectingToInternet()) {
                          new get_company_job_id().execute(comp_id_name);
                    } else {
                        Toast.makeText(SelectCompanyActivityNew.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                    }


                }
            }
        });



        company_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (company_name.getText().length() == 0) {
                    company_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    company_Name_list = new ArrayList<>(company_Name_list_forIndex);
                    companyNameAdapter = new CompanyNameAdapter(SelectCompanyActivityNew.this, android.R.layout.simple_list_item_1, company_Name_list);
                    company_name.setAdapter(companyNameAdapter);
                    company_name.setDropDownHeight(550);

                } else {
                    company_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                    if (company_Name_list.size() == 0) {
                        company_name.setAdapter(null);
                    }

                }
            }
        });


        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (company_name.getText().toString().trim().equals("")) {
                    Toast.makeText(SelectCompanyActivityNew.this, "Please select Company!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String companyName = company_name.getText().toString().trim();
                if (!company_Name_list_forIndex.contains(companyName)) {
                    Toast.makeText(SelectCompanyActivityNew.this, "Please enter correct Company Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int j = 0; j < company_Name_list_forIndex.size(); j++) {
                    String compName  = company_Name_list_forIndex.get(j);
                    if (companyName.equals(compName)) {
                        comp_id_name = company_id_list_forIndex.get(j);
                        Log.e("comp_id--->", comp_id_name);
                        Toast.makeText(getApplicationContext(), "Comp:" + comp_id_name, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                SendDataToCallingActivity(comp_id_name);


            }
        });



        ed.putBoolean("billable", false).commit();
        //dial.dismiss();
    }

    public void scanqr() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.setPackage(getApplicationContext().getPackageName());
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
            startActivity(marketIntent);

        }
    }

    public void getAllJobsByDealerId() {

        list_AllJobs = new ArrayList<>();

        String dealerId = sp.getString(Utility.DEALER_ID, "");
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + "GetallJobByDealerID";
        final String METHOD_NAME = "GetallJobByDealerID";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("DealerId", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int j = 0; j < ks.getPropertyCount(); j++) {
                ks.getProperty(j);
            }
            String recved = ks.toString();


            Log.d("BHANU", recved);
            if (recved.contains("No Data Available")) {

            } else {
                recved = recved.substring(recved.indexOf("["), recved.lastIndexOf("]") + 1);
                JSONArray jsonArray = new JSONArray(recved);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String jobName = jsonObject.getString("txt_job");
                    String JobId = jsonObject.getString("job_id_pk");
                    String job_descripition = jsonObject.getString("JOB_DESC");
                    String compID = jsonObject.getString("compID");
                    String desc = job_descripition.trim();
                    char space = ' ';
                    int index = 0;
                    if (desc.length() > 30) {
                        for (int j = 30; j < desc.length(); j++) {
                            if (desc.charAt(j) == space) {
                                // String s=desc.substring(j);
                                index = j;
                                break;
                            }
                        }
                    }
                    String total_desc = "";
                    if (index != 0) {
                        desc = desc.substring(0, index) + System.getProperty("line.separator") + (desc.substring(index)).trim();
                    }
                    total_desc = jobName + System.getProperty("line.separator") + desc.trim();
                    /////

                    HashMap<String, String> hashMap = new HashMap();
                    hashMap.put("jobName", total_desc);
                    hashMap.put("JobId", JobId);
                    hashMap.put("compID", compID);
                    list_AllJobs.add(hashMap);


                }
                list_AllJobs_forIndex = new ArrayList<>(list_AllJobs);
            }
        } catch (Exception e) {
            e.getMessage();
        }


    }

    public void dialog_SearchByJob()    /////by aman kaushik
    {
        dialog_ScanSWO = new Dialog(SelectCompanyActivityNew.this);
        dialog_ScanSWO.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_ScanSWO.setContentView(R.layout.diloge_for_billable_only_new);
        dialog_ScanSWO.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog_ScanSWO.setCancelable(true);
        ImageView closebtn = (ImageView) dialog_ScanSWO.findViewById(R.id.close);
        Button scanswo = (Button) dialog_ScanSWO.findViewById(R.id.scan);
        TextView or = (TextView) dialog_ScanSWO.findViewById(R.id.textView);

        /*if (userRole.equals(Utility.USER_ROLE_APC) ||
                userRole.equals(Utility.USER_ROLE_ARTIST)) {
            scanswo.setVisibility(View.GONE);
            or.setVisibility(View.GONE);
        } else {
            scanswo.setVisibility(View.VISIBLE);
            or.setVisibility(View.VISIBLE);
        }*/


        et_JobName = (AutoCompleteTextView) dialog_ScanSWO.findViewById(R.id.jobtext);
        Button btn_GO = (Button) dialog_ScanSWO.findViewById(R.id.Btn_Yes);

////new work
        et_JobName.setText("");
        JobNameAdapter jobDescAdapter = new JobNameAdapter(SelectCompanyActivityNew.this, android.R.layout.simple_list_item_1, list_AllJobs);
        et_JobName.setAdapter(jobDescAdapter);
        et_JobName.setDropDownHeight(550);

        et_JobName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                et_JobName.showDropDown();
                /*to clear autocomplete*/
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (et_JobName.getRight() - et_JobName.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            et_JobName.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                /**/

                return false;
            }
        });


        et_JobName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

             /*   if (userRole.equals(Utility.USER_ROLE_APC) ||
                        userRole.equals(Utility.USER_ROLE_ARTIST)) {
                } else {
                    dialog_ScanSWO.hide();
                }*/


                //  search_byJob = true;
                ///
                String job_txt = et_JobName.getText().toString();
                job_txt = job_txt.substring(0, job_txt.indexOf("\n"));
                et_JobName.setText(job_txt);

/*
                if (userRole.equals(Utility.USER_ROLE_APC) ||
                        userRole.equals(Utility.USER_ROLE_ARTIST)) {
                } else {
                    new CheckSwo().execute(job_txt);
                }*/
                //  new MainActivity.CheckSwo().execute(job_txt);
            }
        });

        et_JobName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (et_JobName.getText().length() == 0) {

                    et_JobName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    //refresh adapter
                    list_AllJobs = new ArrayList<>(list_AllJobs_forIndex);
                    JobNameAdapter jobDescAdapter = new JobNameAdapter(SelectCompanyActivityNew.this, android.R.layout.simple_list_item_1, list_AllJobs);
                    et_JobName.setAdapter(jobDescAdapter);
                    et_JobName.setDropDownHeight(550);

                } else {
                    et_JobName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                }
            }
        });


        ///// new work


        closebtn.setOnClickListener(

                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            dialog_ScanSWO.dismiss();
                        } catch (Exception e) {
                            e.getMessage();
                        }


                    }

                });
        scanswo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanqr();
                try {
                    dialog_ScanSWO.dismiss();
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });

        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                job = et_JobName.getText().toString();
                ////


                if (job.length() == 0) {
                    Toast.makeText(SelectCompanyActivityNew.this, "Please enter Job Id", Toast.LENGTH_SHORT).show();

                } else {


                    for (int j = 0; j < list_AllJobs_forIndex.size(); j++) {
                        HashMap<String, String> hashMap = list_AllJobs_forIndex.get(j);
                        String jobName = hashMap.get("jobName");
                        jobName = jobName.substring(0, jobName.indexOf("\n"));
                        if (jobName.equals(job)) {
                            comp_id_name = hashMap.get("compID");
                            // String _JobId = hashMap.get("JobId");
                            // ed.putString(Utility.KEY_JOB_ID_FOR_JOBFILES, _JobId).commit();
                            Log.e("comp_id--->", comp_id_name);
                            Toast.makeText(getApplicationContext(), "Comp:" + comp_id_name, Toast.LENGTH_SHORT).show();

                            //   Log.e("job_id---->", _JobId);
                            break;
                        }
                    }
                    SendDataToCallingActivity(comp_id_name);

                    ///nks
                    /*if (comp_id_list != null && comp_id_list.size() > 0) {
                        ed.putString(Utility.COMPANY_ID_BILLABLE, comp_id_list.get(0)).commit();
                    }*/
                    dialog_ScanSWO.hide();

                 /*   if (new ConnectionDetector(MainActivity.this).isConnectingToInternet()) {
                        new MainActivity.get_company_Area().execute(et_job);
                    } else {
                        Toast.makeText(MainActivity.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                    }*/

//nks
                }


            }
        });

        try {
            dialog_ScanSWO.show();
        } catch (Exception e) {
            e.getMessage();
        }


        ed.putBoolean("billable", false).commit();
        //dial.dismiss();


    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {

            if (resultCode == RESULT_OK) {

            } else if (resultCode == RESULT_CANCELED) {

                Toast.makeText(getApplicationContext(),
                        "Press a button to start a scan.", Toast.LENGTH_SHORT).show();
                Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                String result = contents;
                //  System.out.println("jobid & client id-----------------" + result);
                Log.d("BHANU", "" + contents + " " + result);
                String Swo_Id = "";
                String clientid = "";
                String dealerid = "";

                if (result.contains(",")) {
                    try {
                        String[] jj = result.split(",");// jobid,clientid
                        Swo_Id = jj[0];
                        clientid = jj[1];
                        dealerid = jj[2];
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SWO error!", Toast.LENGTH_SHORT).show();
                    }
                    String login_dealerId = sp.getString(Utility.DEALER_ID, "");

                    if (!dealerid.equals(login_dealerId)) {
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (clientid.equalsIgnoreCase("")) {
                        dialog_SearchByJob();
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(getApplicationContext(), "Comp:" + clientid, Toast.LENGTH_SHORT).show();

                    SendDataToCallingActivity(clientid);
                } else {
                    Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                    return;
                }



            }
            if (resultCode == RESULT_CANCELED) {
                // handle cancel
                //finish();
            }
        }
        //nks


    }

    private class get_company_name extends AsyncTask<Void, Void, Void> {

        final ProgressDialog ringProgressDialog =
                new ProgressDialog(SelectCompanyActivityNew.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
                ringProgressDialog.setMessage(getString(R.string.Loading_text));
                ringProgressDialog.setCancelable(false);
                ringProgressDialog.setCanceledOnTouchOutside(false);

                try {
                    ringProgressDialog.show();
                } catch (Exception e) {
                    e.getMessage();
                }


            } catch (Exception e) {
                e.getMessage();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //    Send();
            Getcompany_name();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                ringProgressDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }

            if (company_id_list != null && company_id_list.size() > 0) {
                dialog_SearchByCompany();
            } else {
                Toast.makeText(SelectCompanyActivityNew.this, "No data found!", Toast.LENGTH_LONG).show();
            }
        }
    }
    private class get_company_job_id extends AsyncTask<String, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(SelectCompanyActivityNew.this);

        @Override
        protected Void doInBackground(String... strings) {
            Getcompany_job_id(strings[0]);
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  ringProgressDialog.setTitle("Kindly wait ...")
            ringProgressDialog.setMessage(getString(R.string.Loading_text));
            ringProgressDialog.setCancelable(false);

            try {
                ringProgressDialog.show();
            } catch (Exception e) {
                e.getMessage();
            }


        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            try {
                ringProgressDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }

            if (count == 1) {
              // company_name.setText("");
             //   job_name.setText("");
                f2();
            } else {
                //job_name.setText("");
                CompanyNameAdapter jobDescAdapter = new CompanyNameAdapter(SelectCompanyActivityNew.this, android.R.layout.simple_list_item_1, job_Name_list_Desc);
              //  job_name.setAdapter(jobDescAdapter);
               // job_name.setDropDownHeight(550);
            }

        }
    }
    public void f2() {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SelectCompanyActivityNew.this);
        LayoutInflater inflater = LayoutInflater.from(SelectCompanyActivityNew.this);
        final View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        dialogView.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogBuilder.setView(dialogView);
        final TextView title = dialogView.findViewById(R.id.textView1rr);
        final TextView message = dialogView.findViewById(R.id.texrtdesc);

        final Button positiveBtn = dialogView.findViewById(R.id.Btn_Yes);
        final Button negativeBtn = dialogView.findViewById(R.id.Btn_No);
        ImageView close = (ImageView) dialogView.findViewById(R.id.close);
        close.setVisibility(View.INVISIBLE);
        // dialogBuilder.setTitle("Device Details");
        title.setText("No jobs available!");
        message.setText("Kindly choose another Company!");
        positiveBtn.setText("Ok");
        negativeBtn.setText("No");
        negativeBtn.setVisibility(View.GONE);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

            }
        });
        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.getCause();
        }


    }

    public void Getcompany_job_id(String id)
    {
        count = 0;
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + "BindJob";
        final String METHOD_NAME = "BindJob";
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("ClientID", id);
        Log.d("BHANU--ID", id);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);

            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            String result = SoapPrimitiveresult.toString();
            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");



                job_id_list.clear();
                job_Name_list.clear();
                job_des_list.clear();
                status_list.clear();
                show_list.clear();
                jobtype_list.clear();
                job_Name_list_Desc.clear();//nks

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String comapny_id = jsonObject1.getString("JOB_ID_PK");
                    String company_name = jsonObject1.getString("JobName");
                    String job_descripition = jsonObject1.getString("txt_Job");


                    String status = jsonObject1.getString("Status");
                    String show = jsonObject1.getString("ShowName");
                    String jobtype = jsonObject1.getString("JOB_TYPE");

////
                    String desc = job_descripition.trim();
                    char space = ' ';
                    int index = 0;
                    if (desc.length() > 30) {
                        for (int j = 30; j < desc.length(); j++) {
                            if (desc.charAt(j) == space) {
                                // String s=desc.substring(j);
                                index = j;
                                break;
                            }
                        }
                    }
                    String total_desc = "";
                    if (index != 0) {
                        desc = desc.substring(0, index) + System.getProperty("line.separator") + (desc.substring(index)).trim();
                    }
                    total_desc = company_name + System.getProperty("line.separator") + desc.trim();
                    /////

                    job_id_list.add(comapny_id);
                    job_Name_list.add(company_name);
                    job_Name_list_Desc.add(total_desc);
                    job_des_list.add(job_descripition);
                    status_list.add(status);
                    show_list.add(show);
                    jobtype_list.add(jobtype);
                }

                job_Name_list_Desc_forIndex = new ArrayList<>(job_Name_list_Desc);
                //   job_id_list.add(0, "--Select--");
                //   job_Name_list.add(0, "--Select--");
                //   job_des_list.add(0,"--Select--");

                //    status_list.add(0,"--Select--");
                //    show_list.add(0,"--Select--");
                //   jobtype_list.add(0,"--Select--");
         //   }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private class getAllJObByDealer extends AsyncTask<Void, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(SelectCompanyActivityNew.this);
            progressDialog.setMessage(getString(R.string.Loading_text));
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(false);

            try {
                progressDialog.show();
            } catch (Exception e) {
                e.getMessage();
            }


        }

        @Override
        protected Integer doInBackground(Void... voids) {
            getAllJobsByDealerId();
            return 0;
        }

        @Override
        protected void onPostExecute(Integer aVoid) {

            try {
                progressDialog.dismiss();
                dialog_SearchByJob();
            } catch (Exception e) {
                e.getMessage();
            }


        }
    }

   private void SendDataToCallingActivity(String CompanyId){
       Intent returnIntent = new Intent();
       returnIntent.putExtra("CompID",CompanyId);

       setResult(Activity.RESULT_OK,returnIntent);
       finish();
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
