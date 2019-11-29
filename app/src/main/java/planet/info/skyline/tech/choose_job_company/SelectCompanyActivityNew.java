package planet.info.skyline.tech.choose_job_company;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

import planet.info.skyline.R;
import planet.info.skyline.adapter.AllJobsAdapter;
import planet.info.skyline.adapter.CompanyNameAdapterNew2;
import planet.info.skyline.adapter.JobNameAdapterNew2;
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.model.All_Jobs;
import planet.info.skyline.model.Awo;
import planet.info.skyline.model.Company;
import planet.info.skyline.model.Job_2;
import planet.info.skyline.model.SWO_Details;
import planet.info.skyline.network.Api;
import planet.info.skyline.network.SOAP_API_Client;
import planet.info.skyline.tech.shared_preference.Shared_Preference;
import planet.info.skyline.tech.swo.SwoListActivity;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.network.Api.API_BindJob;
import static planet.info.skyline.network.Api.API_GetAllDetailbyJobtext_New;
import static planet.info.skyline.network.Api.API_GetAwoDetailByJob;
import static planet.info.skyline.network.Api.API_GetallJobByDealerID;
import static planet.info.skyline.network.SOAP_API_Client.KEY_NAMESPACE;

public class SelectCompanyActivityNew extends AppCompatActivity {
    String userRole = "";

    String Swo_Id = "";


    //search by All job
    ArrayList<All_Jobs> list_AllJobs = new ArrayList<>();
    //  fetch company // search by company -job
    ArrayList<Company> list_company = new ArrayList<>();

    CompanyNameAdapterNew2 companyNameAdapter;
    // fetch jobs //search by company-job
    ArrayList<Job_2> list_Jobs = new ArrayList<>();

    ArrayList<SWO_Details> list_SWO = new ArrayList<>();
    ArrayList<Awo> list_Awo = new ArrayList<>();

    Dialog dialog_companyName, dialog_ScanSWO;

    AutoCompleteTextView txtvw_JOB_NAME_SearchByCompany, txtvw_COMP_NAME_SearchByCompany;
    AutoCompleteTextView txtvw_JOB_NAME_SearchByJob;
    AlertDialog alertDialog;

    String Is_Job_Mandatory = "";
    boolean showDialog_SHOW_INFO = false;
    boolean STARTING_BILLABLE_JOB = false;

    Context context;
    boolean IsMySwo = false;
    boolean Scan_QR_CODE = false;
    boolean Search_by_Job = false;

    Button Btn_SearchByJob;
    Button Btn_SearchByCompany;
    Button scan_swo;
    Button Btn_MySwo;
    Button Btn_UnassignedSwo;
    LinearLayout ll_BySWO;


    public void onCompanySelected1(Company company) {
        txtvw_COMP_NAME_SearchByCompany.setText(company.getEname());
        txtvw_COMP_NAME_SearchByCompany.clearFocus();
        if (new ConnectionDetector(context).isConnectingToInternet()) {
            new fetch_Jobs_by_CompID().execute(company.getId());
        } else {
            Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
        }
    }

    public void onJobSelected1(Job_2 job) {

        txtvw_JOB_NAME_SearchByCompany.setText(job.getJobName());
        txtvw_JOB_NAME_SearchByCompany.clearFocus();
        if (STARTING_BILLABLE_JOB) {
            if (new ConnectionDetector(context).isConnectingToInternet()) {
                new Async_fetch_SWO_AWO_List().execute(job.getJobName(), job.getJOB_ID_PK());
            } else {
                Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
            }
        }

    }

    public void onJobSelectedFromAllJobs(All_Jobs job) {
        txtvw_JOB_NAME_SearchByJob.setText(job.getTxt_job());
        txtvw_JOB_NAME_SearchByJob.clearFocus();

        if (STARTING_BILLABLE_JOB) {
            if (new ConnectionDetector(context).isConnectingToInternet()) {
                new Async_fetch_SWO_AWO_List().execute(job.getTxt_job(), job.getJob_id_pk());
            } else {
                Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_company);
        setTitle(Utility.getTitle("Choose Company/Job"));
        context = SelectCompanyActivityNew.this;


        userRole = Shared_Preference.getUSER_ROLE(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Btn_SearchByJob = findViewById(R.id.Btn_SearchByJob);
        Btn_SearchByCompany = findViewById(R.id.Btn_SearchByCompany);
        scan_swo = findViewById(R.id.scan_swo);

        Btn_MySwo = findViewById(R.id.Btn_MySwo);
        Btn_UnassignedSwo = findViewById(R.id.Btn_UnassignedSwo);
        ll_BySWO = findViewById(R.id.ll_BySWO);
        ll_BySWO.setVisibility(View.GONE);

        getIntentData();
        setVisibility();
        setListener();


    }

    private void getIntentData() {
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                if (bundle.containsKey(Utility.IS_JOB_MANDATORY)) {
                    Is_Job_Mandatory = bundle.getString(Utility.IS_JOB_MANDATORY, "0");
                }
                if (bundle.containsKey(Utility.Show_DIALOG_SHOW_INFO)) {
                    showDialog_SHOW_INFO = bundle.getBoolean(Utility.Show_DIALOG_SHOW_INFO, false);
                }
                if (bundle.containsKey(Utility.Show_SWO_LIST_OPTION)) {
                    boolean Show_SWO_LIST = bundle.getBoolean(Utility.Show_SWO_LIST_OPTION, false);
                    if (Show_SWO_LIST)
                        ll_BySWO.setVisibility(View.VISIBLE);
                    else
                        ll_BySWO.setVisibility(View.GONE);
                }
                if (bundle.containsKey(Utility.STARTING_BILLABLE_JOB)) {
                    STARTING_BILLABLE_JOB = bundle.getBoolean(Utility.STARTING_BILLABLE_JOB, false);

                }

            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

    private void setVisibility() {
        //    if (userRole.equals(Utility.USER_ROLE_ARTIST ) || userRole.equals(Utility.USER_ROLE_APC )) {  // artist
        if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
            scan_swo.setVisibility(View.VISIBLE);

            //  Btn_MySwo.setVisibility(View.GONE);
            //  Btn_UnassignedSwo.setVisibility(View.GONE);
            Btn_MySwo.setVisibility(View.VISIBLE);
            Btn_UnassignedSwo.setVisibility(View.VISIBLE);
            scan_swo.setText("Scan AWO QR Code");
            Btn_MySwo.setText("My AWOs");
            Btn_UnassignedSwo.setText("Unassigned AWOs");

        } else { //if (userRole.equals(Utility.USER_ROLE_TECH)) {
            scan_swo.setVisibility(View.VISIBLE);
            Btn_MySwo.setVisibility(View.VISIBLE);
            Btn_UnassignedSwo.setVisibility(View.VISIBLE);
            scan_swo.setText("Scan SWO QR Code");
            Btn_MySwo.setText("My SWOs");
            Btn_UnassignedSwo.setText("Unassigned SWOs");
        } /*else {  //APC,PM
            scan_swo.setVisibility(View.GONE);
            Btn_MySwo.setVisibility(View.GONE);
            Btn_UnassignedSwo.setVisibility(View.GONE);
        }*/


        if (userRole.equals(Utility.USER_ROLE_PM) || userRole.equals(Utility.USER_ROLE_DC)) { // PM/DC can enter time sheet by AWO/SWO both
            scan_swo.setVisibility(View.VISIBLE);
            // Btn_MySwo.setVisibility(View.GONE);
            //Btn_UnassignedSwo.setVisibility(View.GONE);
        }


    }

    private void setListener() {
        Btn_SearchByCompany.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Search_by_Job = false;
                Scan_QR_CODE = false;
                if (new ConnectionDetector(context).isConnectingToInternet()) {
                    new fetch_Clients().execute();
                } else {
                    Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                }
            }
        });

        Btn_SearchByJob.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Search_by_Job = true;
                Scan_QR_CODE = false;
                if (new ConnectionDetector(context).isConnectingToInternet()) {
                    new getAllJObByDealer().execute();
                } else {
                    Toast.makeText(context, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                }

            }
        });

        scan_swo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Scan_QR_CODE = true;
                scanqr();
            }
        });
        Btn_MySwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IsMySwo = true;
                Scan_QR_CODE = false;
                goToSwoListActivity();
            }
        });

        Btn_UnassignedSwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IsMySwo = false;
                Scan_QR_CODE = false;
                goToSwoListActivity();
            }
        });
    }

    private void goToSwoListActivity() {
        Intent i = new Intent(context, SwoListActivity.class);
        i.putExtra("MySwo", IsMySwo);
        startActivityForResult(i, Utility.SWO_LIST_REQUEST_CODE);

    }

    public void Getcompany_name() {
        list_company.clear();
        String dealerId = Shared_Preference.getDEALER_ID(this);

        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String METHOD_NAME = Api.API_bindClientByDealer;
        final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("dealerID", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Object results = (Object) envelope.getResponse();
            String resultstring = results.toString();
            JSONObject jsonObject = new JSONObject(resultstring);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String comapny_id = jsonObject1.getString("id");
                String company_name = jsonObject1.getString("Ename");
                String company_logo = jsonObject1.getString("Imagepath");
                list_company.add(new Company(comapny_id, company_name, company_logo));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dialog_SearchByCompany() {
        dialog_companyName = new Dialog(context);
        dialog_companyName.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_companyName.setContentView(R.layout.test_new);
        dialog_companyName.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog_companyName.setCancelable(true);
        dialog_companyName.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        try {
            dialog_companyName.show();
        } catch (Exception e) {
            e.getMessage();
        }
        ImageView closebtn = (ImageView) dialog_companyName.findViewById(R.id.close);
        txtvw_COMP_NAME_SearchByCompany = (AutoCompleteTextView) dialog_companyName.findViewById(R.id.company);
        txtvw_JOB_NAME_SearchByCompany = (AutoCompleteTextView) dialog_companyName.findViewById(R.id.job);
        TextView txt_optional = (TextView) dialog_companyName.findViewById(R.id.txt_optional);

        if (Is_Job_Mandatory.equals("1")) {
            txt_optional.setVisibility(View.INVISIBLE);
        } else {
            txt_optional.setVisibility(View.VISIBLE);
        }

        Button btn_GO = (Button) dialog_companyName.findViewById(R.id.go_button);

        companyNameAdapter = new CompanyNameAdapterNew2(context,
                android.R.layout.simple_list_item_1, list_company);
        txtvw_COMP_NAME_SearchByCompany.setAdapter(companyNameAdapter);

        txtvw_COMP_NAME_SearchByCompany.setDropDownHeight(550);

        txtvw_COMP_NAME_SearchByCompany.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                txtvw_COMP_NAME_SearchByCompany.showDropDown();

                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (txtvw_COMP_NAME_SearchByCompany.getRight() - txtvw_COMP_NAME_SearchByCompany.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            txtvw_COMP_NAME_SearchByCompany.setText("");
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
        txtvw_JOB_NAME_SearchByCompany.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                txtvw_JOB_NAME_SearchByCompany.showDropDown();
                /*to clear autocomplete*/
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (txtvw_JOB_NAME_SearchByCompany.getRight() - txtvw_JOB_NAME_SearchByCompany.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            txtvw_JOB_NAME_SearchByCompany.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                /**/

                return false;
            }
        });


        txtvw_COMP_NAME_SearchByCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (txtvw_COMP_NAME_SearchByCompany.getText().length() == 0) {
                    txtvw_JOB_NAME_SearchByCompany.setText("");
                    txtvw_COMP_NAME_SearchByCompany.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);


                } else {
                    txtvw_COMP_NAME_SearchByCompany.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);


                }
            }
        });

        txtvw_JOB_NAME_SearchByCompany.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (txtvw_JOB_NAME_SearchByCompany.getText().length() == 0) {
                        txtvw_JOB_NAME_SearchByCompany.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    } else {
                        txtvw_JOB_NAME_SearchByCompany.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                    }
                } catch (Exception e) {
                    e.getMessage();
                }
            }


        });
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog_companyName.dismiss();
                } catch (Exception e) {
                    e.getMessage();
                }

            }
        });
        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Utility.hideKeyboard((Activity) context);
                String str_CompanyName = txtvw_COMP_NAME_SearchByCompany.getText().toString().trim();
                String str_JobName = txtvw_JOB_NAME_SearchByCompany.getText().toString().trim();

                String CompanyId = "";
                String JObId = "";

                if (str_CompanyName.equals("")) {
                    Toast.makeText(context, "Please select Company!", Toast.LENGTH_SHORT).show();
                    return;
                }

                CompanyId = getCompanyID(str_CompanyName);
                JObId = getJobID(str_JobName);
                if (CompanyId.equals("")) {
                    Toast.makeText(context, "Please enter correct Company Name!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Is_Job_Mandatory.equals("1") && str_JobName.equals("")) {
                    Toast.makeText(context, "Please select Job!", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (Is_Job_Mandatory.equals("1") && JObId.equals("")) {
                    Toast.makeText(context, "Please enter correct Job Name!", Toast.LENGTH_SHORT).show();
                    return;
                }


                dialog_companyName.dismiss();
                if (showDialog_SHOW_INFO) {
                    new get_company_Area(CompanyId).execute(str_JobName);
                } else {
                    SendDataToCallingActivity(CompanyId, JObId, str_CompanyName, str_JobName);
                }


            }
        });


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

        list_AllJobs.clear();
        String dealerId = Shared_Preference.getDEALER_ID(this);

        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String SOAP_ACTION = KEY_NAMESPACE + API_GetallJobByDealerID;
        final String METHOD_NAME = API_GetallJobByDealerID;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("DealerId", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);

        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            String receivedString = SoapPrimitiveresult.toString();
            JSONObject jsonObject1 = new JSONObject(receivedString);
            JSONArray jsonArray = jsonObject1.getJSONArray("cds");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String jobName = jsonObject.getString("txt_job");
                String JobId = jsonObject.getString("job_id_pk");
                String job_descripition = jsonObject.getString("JOB_DESC").trim();
                String compID = jsonObject.getString("compID");
                String companyName = jsonObject.getString("company");
                list_AllJobs.add(new All_Jobs(JobId, jobName, job_descripition, compID, companyName));

            }


        } catch (Exception e) {
            e.getMessage();
        }


    }

    public void dialog_SearchByJob() {
        dialog_ScanSWO = new Dialog(context);
        dialog_ScanSWO.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_ScanSWO.setContentView(R.layout.diloge_for_billable_only_new);
        dialog_ScanSWO.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog_ScanSWO.setCancelable(true);
        try {
            dialog_ScanSWO.show();
        } catch (Exception e) {
            e.getMessage();
        }
        ImageView closebtn = (ImageView) dialog_ScanSWO.findViewById(R.id.close);
        Button scanswo = (Button) dialog_ScanSWO.findViewById(R.id.scan);

        txtvw_JOB_NAME_SearchByJob = (AutoCompleteTextView) dialog_ScanSWO.findViewById(R.id.jobtext);
        Button btn_GO = (Button) dialog_ScanSWO.findViewById(R.id.Btn_Yes);


        txtvw_JOB_NAME_SearchByJob.setText("");
        AllJobsAdapter jobDescAdapter = new AllJobsAdapter(context, android.R.layout.simple_list_item_1, list_AllJobs);
        txtvw_JOB_NAME_SearchByJob.setAdapter(jobDescAdapter);
        txtvw_JOB_NAME_SearchByJob.setDropDownHeight(550);

        txtvw_JOB_NAME_SearchByJob.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                txtvw_JOB_NAME_SearchByJob.showDropDown();
                /*to clear autocomplete*/
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;
                try {
                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (motionEvent.getRawX() >= (txtvw_JOB_NAME_SearchByJob.getRight() - txtvw_JOB_NAME_SearchByJob.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            // your action here
                            txtvw_JOB_NAME_SearchByJob.setText("");
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
                /**/

                return false;
            }
        });


        txtvw_JOB_NAME_SearchByJob.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (txtvw_JOB_NAME_SearchByJob.getText().length() == 0) {

                    txtvw_JOB_NAME_SearchByJob.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    txtvw_JOB_NAME_SearchByJob.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                }
            }
        });

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
                Utility.hideKeyboard((Activity) context);
                String str_CompanyName = "";
                String str_JobName = txtvw_JOB_NAME_SearchByJob.getText().toString();
                //   boolean isCorrectJob = false;
                String CompanyId = "";
                String JObId = "";

                if (str_JobName.length() == 0) {
                    Toast.makeText(context, "Please select job!", Toast.LENGTH_SHORT).show();
                    return;
                }


                All_Jobs job = getJobIDFromAllJobs(str_JobName);
                if (job == null) {
                    Toast.makeText(context, "Please enter correct job!", Toast.LENGTH_SHORT).show();
                } else {
                    dialog_ScanSWO.dismiss();
                    CompanyId = job.getCompID();
                    str_JobName = job.getTxt_job();
                    JObId = job.getJob_id_pk();
                    str_CompanyName = job.getCompany();

                    if (showDialog_SHOW_INFO) {
                        new get_company_Area(CompanyId).execute(str_JobName);
                    } else {
                        SendDataToCallingActivity(CompanyId, JObId, str_CompanyName, str_JobName);
                    }
                }

            }
        });


    }

    public void getAwoListByJobID(String JobId) {
        list_Awo.clear();

        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String SOAP_ACTION = KEY_NAMESPACE + API_GetAwoDetailByJob;
        final String METHOD_NAME = API_GetAwoDetailByJob;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("Job", JobId);//nks
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Object results = (Object) envelope.getResponse();
            String resultstring = results.toString();
            JSONArray jsonArray = new JSONArray(resultstring);

            for (int k = 0; k < (jsonArray.length()); k++) {
                JSONObject json_obj = jsonArray.getJSONObject(k);
                String ID_PK = json_obj.getString("ID_PK");
                String JOB_ID = json_obj.getString("JOB_ID");
                String swo_name = json_obj.getString("swo_name");
                String tech_id = "";
                String SWO_Status_new = json_obj.getString("SWO_Status_new");
                list_Awo.add(new Awo(ID_PK, JOB_ID, swo_name, tech_id, SWO_Status_new));
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


    }

    public void GetSwoListByJob(String job) {
        list_SWO.clear();
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String METHOD_NAME = Api.API_GetSwoByJObDealer;
        final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        String dealerId = Shared_Preference.getDEALER_ID(this);
        request.addProperty("job", job);
        request.addProperty("DealerID", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Object results = (Object) envelope.getResponse();
            String resultstring = results.toString();
            JSONObject jsonObject = new JSONObject(resultstring);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String swo_id = jsonObject1.getString("swo_id");
                String JOB_ID = jsonObject1.getString("JOB_ID");
                String JOB_DESC = jsonObject1.getString("JOB_DESC");
                String COMP_ID = jsonObject1.getString("COMP_ID");
                String SWO_NAME = jsonObject1.getString("swo_name");
                String SWO_Status_new = jsonObject1.getString("SWO_Status_new");
                String TXT_JOB = jsonObject1.getString("txt_job");

                list_SWO.add(new SWO_Details(JOB_ID, JOB_DESC, COMP_ID, SWO_NAME, TXT_JOB, SWO_Status_new, swo_id, SWO_NAME));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
                Swo_Id = "";
                String clientid = "";
                if (result.contains(",")) {
                    try {
                        String[] jj = result.split(",");
                        Swo_Id = jj[0];
                        clientid = jj[1];
                        // dealerid = jj[2];
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "SWO error!", Toast.LENGTH_SHORT).show();
                    }
                    if (clientid.equalsIgnoreCase("")) {
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new async_getJobDetailsBySwoId().execute(Swo_Id);
                } else {
                    Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            if (resultCode == RESULT_CANCELED) {
            }
        }
        if (requestCode == Utility.SWO_LIST_REQUEST_CODE && resultCode != RESULT_CANCELED) {
            //  new MainActivity().onActivityResult( requestCode,  resultCode,  intent);
            setResult(requestCode, intent);
            finish();
        }
        //nks


    }

    public void dialog_NO_JObs_Available() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        dialogView.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogBuilder.setView(dialogView);

        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        try {
            alertDialog.show();
        } catch (Exception e) {
            e.getCause();
        }

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


    }

    public void fetch_Jobs(String id) {

        list_Jobs.clear();

        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String SOAP_ACTION = KEY_NAMESPACE + API_BindJob;
        final String METHOD_NAME = API_BindJob;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("ClientID", id);
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

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                String job_id = jsonObject1.getString("JOB_ID_PK");
                String jobName = jsonObject1.getString("JobName");
                String job_descripition = jsonObject1.getString("txt_Job");
                String status = jsonObject1.getString("Status");
                String show = jsonObject1.getString("ShowName");
                String jobtype = jsonObject1.getString("JOB_TYPE");

                list_Jobs.add(new Job_2(job_id, job_descripition, show, jobName, jobtype, status));

            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void SendDataToCallingActivity(String CompanyId, String JObId, String CompanyName, String JobName) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("CompID", CompanyId);
        returnIntent.putExtra("JobID", JObId);
        returnIntent.putExtra("CompName", CompanyName);
        returnIntent.putExtra("JobName", JobName);
        // if (STARTING_BILLABLE_JOB) {
        if (Scan_QR_CODE) {
            returnIntent.putExtra("Swo_Id", Swo_Id);
            returnIntent.putExtra(Utility.STARTING_BILLABLE_JOB_by_SCAN_QR_CODE, true);
        } else {
            Bundle bundle = new Bundle();
            bundle.putSerializable("Awo", list_Awo);
            bundle.putSerializable("Swo", list_SWO);
            returnIntent.putExtra("BUNDLE", bundle);
        }
        //   }

        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public String fetch_JobDetailByJobName(String id) {
        String receivedString = "";
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = SOAP_API_Client.BASE_URL;
        final String SOAP_ACTION = KEY_NAMESPACE + API_GetAllDetailbyJobtext_New;
        final String METHOD_NAME = API_GetAllDetailbyJobtext_New;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        String dealerId = Shared_Preference.getDEALER_ID(this);

        request.addProperty("job", id);
        request.addProperty("dealerid", dealerId);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            receivedString = SoapPrimitiveresult.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedString;
    }

    private void dialog_SHOW_Info(final String CompanyID, final String JobID, final String CompanyName, final String JobName, final String JobType, final String JobDesc, final String Status, final String ShowName, final String AWO_SWO_Name) {

        final Dialog dialog_JobShowDetails = new Dialog(context);
        dialog_JobShowDetails.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_JobShowDetails.setContentView(R.layout.test2_new);
        dialog_JobShowDetails.setCancelable(false);
        dialog_JobShowDetails.show();
        final TextView tv_Company = (TextView) dialog_JobShowDetails.findViewById(R.id.tv_company);

        final TextView t1 = (TextView) dialog_JobShowDetails.findViewById(R.id.t1);
        final TextView t2 = (TextView) dialog_JobShowDetails.findViewById(R.id.t2);
        final TextView t3 = (TextView) dialog_JobShowDetails.findViewById(R.id.t3);
        final TextView t4 = (TextView) dialog_JobShowDetails.findViewById(R.id.t4);
        final TextView t5 = (TextView) dialog_JobShowDetails.findViewById(R.id.t5);

        final LinearLayout ll_swo_awo_info = dialog_JobShowDetails.findViewById(R.id.ll_swo_awo_info);
        final TextView tv_title_awo_swo = (TextView) dialog_JobShowDetails.findViewById(R.id.tv_title_awo_swo);
        final TextView tv_awo_swo = (TextView) dialog_JobShowDetails.findViewById(R.id.tv_awo_swo);

        final Button cancle = (Button) dialog_JobShowDetails.findViewById(R.id.cancle);
        final Button btn_GO = (Button) dialog_JobShowDetails.findViewById(R.id.proced);
        tv_Company.setText(CompanyName);
        t1.setText(JobName);
        t2.setText(JobType);
        t3.setText(Status);
        t4.setText(ShowName);
        t5.setText(JobDesc);
        if (Scan_QR_CODE) {
            ll_swo_awo_info.setVisibility(View.VISIBLE);
        } else {
            ll_swo_awo_info.setVisibility(View.GONE);
        }
        if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
            tv_title_awo_swo.setText("AWO #");
        } else {
            tv_title_awo_swo.setText("SWO #");
        }
        tv_awo_swo.setText(AWO_SWO_Name);

        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_JobShowDetails.dismiss();
                SendDataToCallingActivity(CompanyID, JobID, CompanyName, JobName);
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_JobShowDetails.dismiss();
            }
        });

    }

    public void validation_dialog(String msg) {
        final Dialog showd = new Dialog(context);
        showd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showd.setContentView(R.layout.labourcode_new);
        showd.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        showd.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        showd.setCancelable(false);
        TextView textView1rr = (TextView) showd.findViewById(R.id.textView1rr);
        TextView texrtdesc = (TextView) showd.findViewById(R.id.texrtdesc);
        ImageView close = (ImageView) showd.findViewById(R.id.close);
        TextView yesforloc = (TextView) showd.findViewById(R.id.Btn_Yes);
        texrtdesc.setText(msg);
        textView1rr.setVisibility(View.GONE);

        try {
            showd.show();
        } catch (Exception e) {
            e.getMessage();
        }
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    showd.dismiss();
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });

        yesforloc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    showd.dismiss();
                } catch (Exception e) {
                    e.getMessage();
                }


            }
        });


    }

    private String getCompanyID(String CompanyName) {
        String CompanyID = "";
        for (Company company : list_company) {
            if (CompanyName.equalsIgnoreCase(company.getEname())) {
                CompanyID = company.getId();
                break;
            }
        }
        return CompanyID;
    }

    private String getJobID(String JobName) {
        String JobID = "";
        for (Job_2 job : list_Jobs) {
            if (JobName.equalsIgnoreCase(job.getJobName())) {
                JobID = job.getJOB_ID_PK();
                break;
            }
        }
        return JobID;
    }

    private All_Jobs getJobIDFromAllJobs(String JobName) {
        All_Jobs job_ = null;
        for (All_Jobs job : list_AllJobs) {
            if (JobName.equalsIgnoreCase(job.getTxt_job())) {
                job_ = job;
                break;
            }
        }
        return job_;
    }

    private class Async_fetch_SWO_AWO_List extends AsyncTask<String, Void, Void> {
        ProgressDialog progressDoalog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDoalog = new ProgressDialog(context);
            progressDoalog.setMessage(getString(R.string.Loading_text));
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.setCancelable(false);
            try {
                progressDoalog.show();
            } catch (Exception e) {
                e.getMessage();
            }

        }

        @Override
        protected Void doInBackground(String... param) {
            String jobName = param[0];
            String jobId = param[1];

            if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
                // if (userRole.equals(Utility.USER_ROLE_APC) || userRole.equals(Utility.USER_ROLE_ARTIST)) {
                getAwoListByJobID(jobId);
            } else {
                GetSwoListByJob(jobName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                progressDoalog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }
            //if (userRole.equals(Utility.USER_ROLE_APC) || userRole.equals(Utility.USER_ROLE_ARTIST)) {
            if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
                if (list_Awo.size() < 1) {
                    if (Search_by_Job) txtvw_JOB_NAME_SearchByJob.setText("");
                    else txtvw_JOB_NAME_SearchByCompany.setText("");
                    String msg = "You cannot book time to a Job without a Art Work Order.";
                    validation_dialog(msg);
                }
            } else if (list_SWO.size() < 1) {   //tech

                if (Search_by_Job) txtvw_JOB_NAME_SearchByJob.setText("");
                else txtvw_JOB_NAME_SearchByCompany.setText("");
                String msg = "You cannot book time to a Job without a Service Work Order.";
                validation_dialog(msg);
            }
        }
    }

    private class fetch_Clients extends AsyncTask<Void, Void, Void> {

        final ProgressDialog ringProgressDialog =
                new ProgressDialog(context);

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

            if (list_company != null && list_company.size() > 0) {
                dialog_SearchByCompany();
            } else {
                Toast.makeText(context, "No data found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class fetch_Jobs_by_CompID extends AsyncTask<String, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(context);

        @Override
        protected Void doInBackground(String... strings) {
            fetch_Jobs(strings[0]);
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
            if (list_Jobs.size() < 1) {
                txtvw_JOB_NAME_SearchByCompany.setText("");
                if (Is_Job_Mandatory.equals("1")) {
                    txtvw_COMP_NAME_SearchByCompany.setText("");
                    dialog_NO_JObs_Available();
                }
            } else {
                txtvw_JOB_NAME_SearchByCompany.setText("");
                JobNameAdapterNew2 jobDescAdapter = new JobNameAdapterNew2(context, android.R.layout.simple_list_item_1, list_Jobs);
                txtvw_JOB_NAME_SearchByCompany.setAdapter(jobDescAdapter);
                txtvw_JOB_NAME_SearchByCompany.setDropDownHeight(550);
            }

        }
    }

    private class getAllJObByDealer extends AsyncTask<Void, Void, Integer> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
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

    private class async_getJobDetailsBySwoId extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage("Kindly wait");
            pDialog.setCancelable(false);
            try {
                pDialog.show();
            } catch (Exception e) {
                e.getMessage();
            }
            super.onPreExecute();
        }


        @Override
        protected JSONObject doInBackground(String... param) {

            JSONObject js_obj = new JSONObject();
            String awo_swo_id = param[0];
            final String NAMESPACE = KEY_NAMESPACE;
            final String URL = SOAP_API_Client.BASE_URL;
            final String METHOD_NAME = Api.API_GetJobDetailsBy_SWO_AWO;
            final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;

            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("AWO_SWO", awo_swo_id);
            if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
                request.addProperty("type", "2");
            } else {
                request.addProperty("type", "1");
            }


            SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                    SoapEnvelope.VER11);
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            try {
                httpTransport.call(SOAP_ACTION, envelope);
                Object results = (Object) envelope.getResponse();
                String resultstring = results.toString();
                JSONObject jsonObject = new JSONObject(resultstring);
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
            Utility.hideKeyboard((Activity) context);
            try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }

            if (js_obj.length() == 0) {
                if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
                    Toast.makeText(getApplicationContext(), "Please scan a valid qr code of AWO!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please scan a valid qr code of SWO!", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            try {
                String login_dealerId = Shared_Preference.getDEALER_ID(context);
                String job_id = js_obj.getString("job_id");
                String jobName = js_obj.getString("jobName");
                String JOB_TYPE = js_obj.getString("JOB_TYPE");
                String jobstatus = js_obj.getString("jobstatus");
                String companyName = js_obj.getString("company");
                String Show_Name = js_obj.getString("Show_Name");
                String desciption = js_obj.getString("desciption");
                String SWO_Status_new = js_obj.getString("SWO_Status_new");
                String dealerID = js_obj.getString("dealerID");
                String comp_id = js_obj.getString("compID");
                String Swo_Awo_Name = "";
                if (Shared_Preference.get_EnterTimesheetByAWO(context)) {
                    Swo_Awo_Name = js_obj.getString("AwoName");
                } else {
                    Swo_Awo_Name = js_obj.getString("SwoName");
                }

                if (showDialog_SHOW_INFO) {
                    if (!dealerID.equals(login_dealerId)) {
                        Toast.makeText(getApplicationContext(), "Please scan valid QR Code of SWO!", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog_SHOW_Info(comp_id, job_id, companyName, jobName, JOB_TYPE, desciption, jobstatus, Show_Name, Swo_Awo_Name);
                    }
                } else {
                    SendDataToCallingActivity(comp_id, job_id, companyName, jobName);
                }
            } catch (Exception e) {
                e.getMessage();
                Toast.makeText(getApplicationContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class get_company_Area extends AsyncTask<String, Void, String> {
        final ProgressDialog ringProgressDialog = new ProgressDialog(context);
        String CompanyId = "";

        get_company_Area(String Comp_Id) {
            this.CompanyId = Comp_Id;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ringProgressDialog.setMessage(getString(R.string.Loading_text));
            ringProgressDialog.setCancelable(false);
            ringProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... par) {
            return fetch_JobDetailByJobName(par[0]);
        }

        @Override
        protected void onPostExecute(String Result) {
            super.onPostExecute(Result);
            ringProgressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(Result);
                JSONArray jsonArray = jsonObject.getJSONArray("cds");
                if (jsonArray != null && jsonArray.length() > 0) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(0);
                    String txt_Job = jsonObject1.getString("txt_Job");
                    String ShowName = jsonObject1.getString("ShowName");
                    String JOB_TYPE = jsonObject1.getString("JOB_TYPE");
                    String JobName = jsonObject1.getString("JobName");
                    String Status = jsonObject1.getString("Status");
                    String compname = jsonObject1.getString("compname");
                    String JOB_ID_PK = jsonObject1.getString("JOB_ID_PK");
                    dialog_SHOW_Info(CompanyId, JOB_ID_PK, compname, JobName, JOB_TYPE, txt_Job, Status, ShowName, "");
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

}
