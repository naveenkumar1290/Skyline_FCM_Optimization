package planet.info.skyline;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.KvmSerializable;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import planet.info.skyline.adapter.CompanyNameAdapter;
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.util.Utility.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.URL_EP2;


public class Select_Company_job extends BaseActivity {
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    private static final int WIFI_SETTING_REQUEST_CODE = 458;
    private static final int MOBILE_DATA_SETTING_REQUEST_CODE = 459;

    static Select_Company_job activityMain;
    String result;
    SharedPreferences sp;
    int count;
    Editor ed;
    int pankajtester_chutya;
    Dialog dial, swo_dialog;
    String nextact = "";//, webhit = "";
    String sdd = "1452,1545";// http://exhibitpower.com/crate_web_      service.php?id=
    // String clientidme;
    String job, comp_id_name;
    // HashMap<String,String> company_id_list=new  HashMap<String,String>();
    ArrayList<String> swo_arrayList, comp_id_list, jobid_by_aman;
    ProgressDialog pDialog;
    int aman_geneious = 0;
    String comapny_id1;
    String company_name1;
    String job_descripition1;
    String status1;
    String show1;
    String jobtype1;
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
    String JOB_ID_forNonBillable;
    String COMP_ID_forNonBillable;
    ;
    String main_jobid = "";
    AutoCompleteTextView job_name, company_name;
    String new_job_id, new_des;
    String main_status;////EDIT IN THIS
    String new_jobtype;
    String new_show;
    int my_logic_of = 12;
    boolean doubleBackToExitPressedOnce = false;
    boolean Show_Job_File_New = false;
    List<String> job_Name_list_Desc = new ArrayList<String>();
    LinearLayout ll_billable_row;
    LinearLayout ll_billable_row_new;
    boolean search_byJob = false;
    ArrayList<String> pause_arrayList, cl_id_list_paused, jobid_paused, jobid_pausedNew, jobName_paused, compName_paused;

    Dialog dialog_ScanSWO;
    AutoCompleteTextView et_JobName;
    ArrayList<HashMap<String, String>> list_AllJobs, list_AllJobs_forIndex;
    String compName;

    //GetPausedJobs async_GetPausedJobs;
    boolean taskStartedforPauseList = false;
    String urlofwebservice11_new = URL_EP2 + "/WebService/techlogin_service.asmx?";
    String urlofwebservice = URL_EP2 + "/WebService/techlogin_service.asmx/bind_code";
    WifiManager wifiManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectcompany);

        pDialog = new ProgressDialog(Select_Company_job.this);
        pDialog.setMessage(getString(R.string.Loading_text));
        pDialog.setCancelable(false);
        sp = getApplicationContext().getSharedPreferences("skyline", getApplicationContext().MODE_PRIVATE);
        ed = sp.edit();

        setTitle("Select Company & Job");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (new ConnectionDetector(Select_Company_job.this).isConnectingToInternet()) {
            new get_company_name().execute();
        } else {
            Toast.makeText(Select_Company_job.this, Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();

        }


    }

    public void Getcompany_name() {
        String dealerId = sp.getString(Utility.CLIENT_LOGIN_DealerID, "");
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

    public void diloge_for_company_name()    /////by aman kaushik
    {

        ImageView closebtn = (ImageView) findViewById(R.id.close);
        company_name = (AutoCompleteTextView) findViewById(R.id.company);
        job_name = (AutoCompleteTextView) findViewById(R.id.job);

        Button btn_GO = (Button) findViewById(R.id.go_button);
        Button scann_swo = (Button) findViewById(R.id.Scann);
        TextView or = (TextView) findViewById(R.id.or);


        companyNameAdapter = new CompanyNameAdapter(Select_Company_job.this, android.R.layout.simple_list_item_1, company_Name_list);
        company_name.setAdapter(companyNameAdapter);
        company_name.setDropDownHeight(550);
// ontouch for company name-->
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

        //ontouch for job name---->
        job_name.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                job_name.showDropDown();
                /*to clear autocomplete*/
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (job_name.getRight() - job_name.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            job_name.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                /**/

                return false;
            }
        });

        scann_swo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                  scanqr();
            }
        });

        company_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d("company_name", company_name.getText().toString());
                if (company_name.getText().equals("--Select--") || (company_name.getText().length() == 0)) {

                } else {
                    pankajtester_chutya = 1;
                    String checc = company_name.getText().toString();
                    int index = company_Name_list_forIndex.indexOf(company_name.getText().toString());
                    comp_id_name = company_id_list_forIndex.get(index);


                    if (new ConnectionDetector(Select_Company_job.this).isConnectingToInternet()) {
                        new get_company_job_id().execute(comp_id_name);
                    } else {
                        Toast.makeText(Select_Company_job.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                    }


                }
            }
        });


        job_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                aman_geneious = 12;

                String job_txt = job_name.getText().toString();
                job_txt = job_txt.substring(0, job_txt.indexOf("\n"));

                int index = job_Name_list.indexOf(job_txt);
                ///

                main_jobid = job_Name_list.get(index);////EDIT IN THIS
                new_job_id = job_id_list.get(index);
                ed.putString(Utility.KEY_JOB_ID_FOR_JOBFILES, new_job_id).apply();
                new_des = job_des_list.get(index);
                main_status = status_list.get(index);////EDIT I
///  THIS
                new_jobtype = jobtype_list.get(index);
                new_show = show_list.get(index);
                job_name.setText("");
                job_name.setText(main_jobid);


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
                    job_name.setAdapter(null);
                    job_name.setText("");
                    company_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    company_Name_list = new ArrayList<>(company_Name_list_forIndex);
                    companyNameAdapter = new CompanyNameAdapter(Select_Company_job.this, android.R.layout.simple_list_item_1, company_Name_list);
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
        job_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (job_name.getText().length() == 0) {
                        job_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                        if (company_name.getText().toString().length() == 0) {
                            job_name.setAdapter(null);
                        } else {
                            //refresh adapter
                            job_Name_list_Desc = new ArrayList<>(job_Name_list_Desc_forIndex);
                            CompanyNameAdapter jobDescAdapter = new CompanyNameAdapter(Select_Company_job.this, android.R.layout.simple_list_item_1, job_Name_list_Desc);
                            job_name.setAdapter(jobDescAdapter);
                            job_name.setDropDownHeight(550);

                        }


                    } else {
                        job_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }


        });

        btn_GO.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.e("comp_id_name",comp_id_name);
                Log.e("new_job_id",new_job_id);


                compName = company_name.getText().toString().trim();

                if (compName.equalsIgnoreCase("") || !company_Name_list.contains(compName)) {
                    Toast.makeText(Select_Company_job.this, "Please select valid company !", Toast.LENGTH_SHORT).show();

                } else if (job_name.getText().toString().trim().equalsIgnoreCase("") || !job_Name_list.contains(job_name.getText().toString().trim())) {
                    Toast.makeText(Select_Company_job.this, "Please select valid job!", Toast.LENGTH_SHORT).show();

                } else {


                    final Dialog dialog_jobInfo = new Dialog(Select_Company_job.this);
                    dialog_jobInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog_jobInfo.setContentView(R.layout.test2_new);
                    dialog_jobInfo.setCancelable(false);

                    final TextView tv_Company = (TextView) dialog_jobInfo.findViewById(R.id.tv_company);
                    final TextView t1 = (TextView) dialog_jobInfo.findViewById(R.id.t1);
                    final TextView t2 = (TextView) dialog_jobInfo.findViewById(R.id.t2);
                    final TextView t3 = (TextView) dialog_jobInfo.findViewById(R.id.t3);
                    final TextView t4 = (TextView) dialog_jobInfo.findViewById(R.id.t4);
                    final TextView t5 = (TextView) dialog_jobInfo.findViewById(R.id.t5);
                    final Button cancle = (Button) dialog_jobInfo.findViewById(R.id.cancle);
                    final Button btn_GO = (Button) dialog_jobInfo.findViewById(R.id.proced);

                    if (new_jobtype == "null") {
                        tv_Company.setText((compName));
                        t1.setText(main_jobid);
                        t2.setText("");
                        t3.setText(main_status);
                        t4.setText(new_show);
                        t5.setText(new_des);
                    } else {
                        tv_Company.setText((compName));
                        t1.setText(main_jobid);
                        t2.setText(new_jobtype);
                        t3.setText(main_status);
                        t4.setText(new_show);
                        t5.setText(new_des);
                    }

                    btn_GO.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog_jobInfo.dismiss();

                           /* Intent i = new Intent(Select_Company_job.this, DashboardActivity.class);
                            i.putExtra("comp_id", comp_id_name);
                            i.putExtra("job_id", new_job_id);
                            startActivity(i);*/


                            Intent intent=new Intent();
                            intent.putExtra("comp_id",comp_id_name);
                            intent.putExtra("job_id",new_job_id);
                            setResult(121,intent);
                            finish();//finishing activity


                        }
                    });

                    cancle.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog_jobInfo.dismiss();

                        }
                    });


                    dialog_jobInfo.show();

                }


            }
        });


    }

    public void Getcompany_job_id(String id)   ///by aman kaushik
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
            KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
            for (int j = 0; j < ks.getPropertyCount(); j++) {
                ks.getProperty(j);
            }
            String recved = ks.toString();
            if (recved.contains("No Data Available.")) {
                count = 1;
            } else {

                String json = recved.substring(recved.indexOf("=") + 1, recved.lastIndexOf(";"));

                JSONObject jsonObject = new JSONObject(json);
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


                    job_id_list.add(comapny_id);
                    job_Name_list.add(company_name);
                    job_Name_list_Desc.add(total_desc);
                    job_des_list.add(job_descripition);
                    status_list.add(status);
                    show_list.add(show);
                    jobtype_list.add(jobtype);
                }

                job_Name_list_Desc_forIndex = new ArrayList<>(job_Name_list_Desc);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class get_company_name extends AsyncTask<Void, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(Select_Company_job.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ringProgressDialog.setMessage(getString(R.string.Loading_text));
            ringProgressDialog.setCancelable(false);
            ringProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Getcompany_name();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ringProgressDialog.dismiss();
            if (company_id_list != null && company_id_list.size() > 0) {
                diloge_for_company_name();
            } else {
                Toast.makeText(Select_Company_job.this, "No data found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    //***load the requerment diloge for new diloge for company Name**//
    private class get_company_job_id extends AsyncTask<String, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(Select_Company_job.this);

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
            ringProgressDialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ringProgressDialog.dismiss();

                job_name.setText("");

                CompanyNameAdapter jobDescAdapter = new CompanyNameAdapter(Select_Company_job.this, android.R.layout.simple_list_item_1, job_Name_list_Desc);
                job_name.setAdapter(jobDescAdapter);
                job_name.setDropDownHeight(550);


        }
    }
    public void scanqr() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.setPackage(getApplicationContext().getPackageName());
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 1);

        } catch (Exception e) {

          e.getMessage();

        }
    }
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String contents = intent.getStringExtra("SCAN_RESULT");
                result = contents;

                Log.d("Swo data",  result);
                String Swo_Id = "";
                String clientid = "";
                if (!(result.equals("hi") || result == "hi")) {

                    if (result.contains(",")) {
                        String[] jj = result.split(",");// jobid,clientid
                        Swo_Id = jj[0];
                        clientid = jj[1];
                     comp_id_name=clientid;

                        if (clientid.equalsIgnoreCase("")) {
                            Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (clientid.equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();


                        return;
                    }


                 //   getCompanyInfo(nextact);
                    check(Swo_Id);

                }
            }
            if (resultCode == RESULT_CANCELED) {

            }
        }




    }

    public void check(final String Swo_Id) {

        if (new ConnectionDetector((Select_Company_job.this)).isConnectingToInternet()) {
            new async_getJobDetailsBySwoId().execute(Swo_Id);
        } else {
            Toast.makeText(Select_Company_job.this, Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
        }




    }
    private class async_getJobDetailsBySwoId extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Select_Company_job.this);
            pDialog.setMessage("Kindly wait");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }


        @Override
        protected JSONObject doInBackground(String... param) {
            // TODO Auto-generated method stub

            JSONObject js_obj = new JSONObject();
            String swoid = param[0];
            String receivedString = "";

            final String NAMESPACE = KEY_NAMESPACE + "";
            final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
            final String SOAP_ACTION = KEY_NAMESPACE + "GetJobDetailsBySWO";
            final String METHOD_NAME = "GetJobDetailsBySWO";

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("SWO", swoid);
            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            try {
                httpTransport.call(SOAP_ACTION, envelope);
                KvmSerializable ks = (KvmSerializable) envelope.bodyIn;
                for (int j = 0; j < ks.getPropertyCount(); j++) {
                    ks.getProperty(j);
                }
                receivedString = ks.toString();
                receivedString = receivedString.substring(receivedString.indexOf("=") + 1, receivedString.indexOf(";"));
                Log.e("***detail by SWOID", receivedString);
            } catch (Exception e) {

                e.printStackTrace();
            }
            try {

                JSONObject jsonObject = new JSONObject(receivedString);
                JSONArray jsonArray = jsonObject.getJSONArray("cds");
                js_obj = jsonArray.getJSONObject(0);


            } catch (Exception e) {

                e.printStackTrace();
            }


            return js_obj;
        }

        @Override
        protected void onPostExecute(JSONObject js_obj) {
            super.onPostExecute(js_obj);

            Log.e("GetJobDetailsBySWO api ",js_obj.toString());
            try {
                pDialog.dismiss();

                String job_id = js_obj.getString("job_id");
                String jobName = js_obj.getString("jobName");
                String JOB_TYPE = js_obj.getString("JOB_TYPE");
                String jobstatus = js_obj.getString("jobstatus");
                String company = js_obj.getString("company");
                String Show_Name = js_obj.getString("Show_Name");
                String desciption = js_obj.getString("desciption");

              new_job_id=job_id;
                ed.putString(Utility.KEY_JOB_ID_FOR_JOBFILES, job_id).apply();
                ed.putString(Utility.JOB_ID_BILLABLE, jobName).apply();


                final Dialog dialog_jobInfo = new Dialog(Select_Company_job.this);
                dialog_jobInfo.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog_jobInfo.setContentView(R.layout.test2_new);
                dialog_jobInfo.setCancelable(false);

                final TextView tv_Company = (TextView) dialog_jobInfo.findViewById(R.id.tv_company);
                final TextView t1 = (TextView) dialog_jobInfo.findViewById(R.id.t1);
                final TextView t2 = (TextView) dialog_jobInfo.findViewById(R.id.t2);
                final TextView t3 = (TextView) dialog_jobInfo.findViewById(R.id.t3);
                final TextView t4 = (TextView) dialog_jobInfo.findViewById(R.id.t4);
                final TextView t5 = (TextView) dialog_jobInfo.findViewById(R.id.t5);
                final Button cancle = (Button) dialog_jobInfo.findViewById(R.id.cancle);
                final Button btn_GO = (Button) dialog_jobInfo.findViewById(R.id.proced);


                tv_Company.setText((company));
                t1.setText(jobName);
                t2.setText(JOB_TYPE);
                t3.setText(jobstatus);
                t4.setText(Show_Name);
                t5.setText(desciption);


                btn_GO.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //  dialog_companyName.dismiss();

                        dialog_jobInfo.dismiss();


                        Log.e("comp_id_name",comp_id_name);
                        Log.e("new_job_id",new_job_id);


                      /*  Intent i=new Intent(Select_Company_job.this, DashboardActivity.class);
                        i.putExtra("comp_id",comp_id_name);
                        i.putExtra("job_id",new_job_id);
                        startActivity(i);*/


                        Intent intent=new Intent();
                        intent.putExtra("comp_id",comp_id_name);
                        intent.putExtra("job_id",new_job_id);
                        setResult(121,intent);
                        finish();//finishing activity


                    }
                });

                cancle.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog_jobInfo.dismiss();

                    }
                });


                dialog_jobInfo.show();


            } catch (Exception e) {
                e.getMessage();
            }
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



}
