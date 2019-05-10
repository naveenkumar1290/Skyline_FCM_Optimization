package planet.info.skyline;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.List;

import planet.info.skyline.adapter.CompanyNameAdapterNew;
import planet.info.skyline.adapter.JobNameAdapterNew;
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.model.Company;
import planet.info.skyline.model.Job_1;
import planet.info.skyline.model.UsageCharge;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.util.Utility.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.URL_EP2;

public class UsageChargesListActivity extends AppCompatActivity {
    TextView tv_msg;
    ArrayList<UsageCharge> usageChargesList = new ArrayList<>();
    AlertDialog alertDialog;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    ArrayList<Company> List_Company = new ArrayList<>();
    ArrayList<Company> List_Company_forIndex = new ArrayList<>();
    ArrayList<Job_1> List_Job = new ArrayList<>();
    ArrayList<Job_1> List_Job_forIndex = new ArrayList<>();
    Dialog dialog_companyName;
    AutoCompleteTextView job_name, company_name;
    String job = "", comp_id_name = "";
    String compID = "";
    String jobID = "";
    String company_Name = "";
    String job_Name = "";
    private RecyclerView recyclerView;
    private MoviesAdapter mAdapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_charges_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // setTitle("Usage Charges List");

        setTitle(Utility.getTitle("Usage Charges List"));
        // setTitle(Html.fromHtml("<small>Usage Charges List</small>"));
        sp = getApplicationContext().getSharedPreferences("skyline", getApplicationContext().MODE_PRIVATE);
        ed = sp.edit();
        setView();
        //Check_Clock_Status();
        boolean TIMER_STARTED_FROM_BILLABLE_MODULE = sp.getBoolean(Utility.TIMER_STARTED_FROM_BILLABLE_MODULE, false);
        if (TIMER_STARTED_FROM_BILLABLE_MODULE) {
            compID = sp.getString(Utility.COMPANY_ID_BILLABLE, "");
            jobID = sp.getString(Utility.KEY_JOB_ID_FOR_JOBFILES, "");
            company_Name = sp.getString("name", "");
            job_Name = sp.getString(Utility.JOB_ID_BILLABLE, "");
        }
        //////////////////

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UsageChargesListActivity.this, UsageChargesUpdateActivity.class);
                intent.putExtra(Utility.USAGE_CHARGE_ID, "0");
                intent.putExtra(Utility.IS_ADD, "1");
                intent.putExtra("jobID", jobID);
                intent.putExtra("compid", compID);
                intent.putExtra("vendorid", "");
                intent.putExtra("Job", job_Name);
                intent.putExtra("Company", company_Name);
                intent.putExtra("Vendor", "");
                intent.putExtra("desc", "");
                intent.putExtra("cost", "");
                intent.putExtra("quantity", "");
                intent.putExtra("total", "");

                startActivity(intent);
            }
        });
    }


    private void Check_Clock_Status() {

        new async_list_usage_report().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        //Check_Clock_Status();
        if (new ConnectionDetector(UsageChargesListActivity.this).isConnectingToInternet()) {
            new async_list_usage_report().execute();
        } else {
            Toast.makeText(UsageChargesListActivity.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
        }

    }

    private void setView() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        tv_msg = findViewById(R.id.tv_msg);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(UsageChargesListActivity.this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        tv_msg.setVisibility(View.VISIBLE);


    }


    public String ListUsageReport() {
        usageChargesList.clear();
        String dealer_id = Utility.getDealerID(UsageChargesListActivity.this);
        String user_id = Utility.getUserID(UsageChargesListActivity.this);
        String Name = sp.getString("tname", "");
        String LoginUsername = sp.getString(Utility.LOGIN_USERNAME, "");
        String role = sp.getString(Utility.LOGIN_USER_ROLE, "");

        String receivedString = "";

        final String NAMESPACE = KEY_NAMESPACE;
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String METHOD_NAME = Utility.Method_USAGE_REPORT_LIST;
        final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("client", compID);
        request.addProperty("job", jobID);
        request.addProperty("dealerID", dealer_id);
        request.addProperty("dealerType", "0");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            receivedString = SoapPrimitiveresult.toString();
            Log.e("receivedString", receivedString);

            // {"status":"1","Message":"Inserted Successfully.","ReturnID":"0"}
            //      JSONObject jsonObject = new JSONObject(receivedString);

            JSONArray jsonArray = new JSONArray(receivedString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String Usage_id = jsonObject.getString("Usage_id");
                String Quantity = jsonObject.getString("Quantity");
                String username = jsonObject.getString("username");
                String Username1 = jsonObject.getString("Username1");
                String cost = jsonObject.getString("cost");
                String Vendor = jsonObject.getString("Vendor");
                String Vendorid = jsonObject.getString("Vendorid");
                String Descriptions = jsonObject.getString("Descriptions");
                String Total = jsonObject.getString("Total");
                String job = jsonObject.getString("job");
                String jobid = jsonObject.getString("jobid");
                String Company = jsonObject.getString("Company");
                String Companyid = jsonObject.getString("Companyid");

                usageChargesList.add(new UsageCharge(Usage_id,
                        Quantity,
                        username,
                        Username1,
                        cost,
                        Vendor,
                        Vendorid,
                        Descriptions,
                        Total,
                        job,
                        jobid,
                        Company,
                        Companyid));
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return receivedString;
    }

    private void getCompany() {
        if (new ConnectionDetector(UsageChargesListActivity.this).isConnectingToInternet()) {
            new get_company_name().execute();
        } else {
            Toast.makeText(UsageChargesListActivity.this, Utility.NO_INTERNET, Toast.LENGTH_SHORT).show();
        }
    }

    public void Getcompany_name() {
        String dealerId = sp.getString(Utility.DEALER_ID, "");
        List_Company.clear();
        //  count = 0;
        final String NAMESPACE = KEY_NAMESPACE;
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

            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            String receivedString = SoapPrimitiveresult.toString();
            Log.e("receivedString", receivedString);


            if (receivedString.contains("Job not found!")) {
                //     count = 1;
            } else {


                JSONObject jsonObject = new JSONObject(receivedString);
                JSONArray jsonArray = jsonObject.getJSONArray("cds");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String comapny_id = jsonObject1.getString("id");
                    String company_name = jsonObject1.getString("Ename");
                    String company_logo = jsonObject1.getString("Imagepath");
                    List_Company.add(new Company(comapny_id, company_name, company_logo));
                }

                List_Company_forIndex = new ArrayList<>(List_Company);


            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dialog_SearchByCompany()    /////by aman kaushik
    {

        dialog_companyName = new Dialog(UsageChargesListActivity.this);
        dialog_companyName.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_companyName.setContentView(R.layout.test1_new);
        dialog_companyName.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog_companyName.setCancelable(true);
        ImageView closebtn = (ImageView) dialog_companyName.findViewById(R.id.close);
        company_name = (AutoCompleteTextView) dialog_companyName.findViewById(R.id.company);
        job_name = (AutoCompleteTextView) dialog_companyName.findViewById(R.id.job);

        Button btn_GO = (Button) dialog_companyName.findViewById(R.id.go_button);
        Button scann_swo = (Button) dialog_companyName.findViewById(R.id.Scann);


        TextView or = (TextView) dialog_companyName.findViewById(R.id.or);
        dialog_companyName.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        scann_swo.setVisibility(View.GONE);

        BindDataCompany();
        scann_swo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    dialog_companyName.dismiss();
                } catch (Exception e) {
                    e.getMessage();
                }
                //  scanqr();
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
                try {
                    dialog_companyName.hide();
                } catch (Exception e) {
                    e.getMessage();
                }
                company_Name = company_name.getText().toString().trim();
                job_Name = job_name.getText().toString().trim();


                compID = "";
                jobID = "";


                for (int i = 0; i < List_Company_forIndex.size(); i++) {
                    String cmp = List_Company_forIndex.get(i).getEname();
                    if (cmp.equals(company_Name)) {
                        compID = List_Company_forIndex.get(i).getId();
                        break;
                    }
                }
                for (int i = 0; i < List_Job_forIndex.size(); i++) {
                    String jobname = List_Job_forIndex.get(i).getJobName();
                    if (jobname.equals(job_Name)) {
                        jobID = List_Job_forIndex.get(i).getJobId();
                        break;
                    }
                }

               /* if (compID.equals("")) {

                    Toast.makeText(getApplicationContext(), "Please enter valid company name!", Toast.LENGTH_SHORT).show();
                } else if (jobID.equals("")) {

                    Toast.makeText(getApplicationContext(), "Please enter valid job name!", Toast.LENGTH_SHORT).show();
                } else {*/
                new async_list_usage_report().execute();
                //  }
            }
        });

        try {
            if (!dialog_companyName.isShowing())
                dialog_companyName.show();
        } catch (Exception e) {
            e.getMessage();
        }


    }

    private void BindJobData() {

        JobNameAdapterNew jobDescAdapter = new JobNameAdapterNew(UsageChargesListActivity.this, android.R.layout.simple_list_item_1, List_Job);

        job_name.setAdapter(jobDescAdapter);
        job_name.setDropDownHeight(550);

        job_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                String job_txt = job_name.getText().toString();
                job_txt = job_txt.substring(0, job_txt.indexOf("\n"));

                int index = 0;
                for (int j = 0; j < List_Job.size(); j++) {
                    Job_1 job = List_Job.get(j);
                    if (job.getJobName().equals(job_txt)) {
                        index = j;
                        break;
                    }
                }

                String main_jobid = List_Job.get(index).getJobName();
                job_name.setText(main_jobid);
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
                            List_Job = new ArrayList<>(List_Job_forIndex);
                            JobNameAdapterNew jobDescAdapter = new JobNameAdapterNew(UsageChargesListActivity.this, android.R.layout.simple_list_item_1, List_Job);
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


    }

    private void BindDataCompany() {
        CompanyNameAdapterNew companyNameAdapter = new CompanyNameAdapterNew(UsageChargesListActivity.this, android.R.layout.simple_list_item_1, List_Company);
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


        company_name.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {


                if (company_name.getText().equals("--Select--") || (company_name.getText().length() == 0)) {

                } else {
                    //    pankajtester_chutya = 1;
                    //   String checc = company_name.getText().toString();
                    //   int index = company_Name_list_forIndex.indexOf(company_name.getText().toString());
                    //


                    int index = 0;
                    for (int j = 0; j < List_Company_forIndex.size(); j++) {
                        Company company = List_Company_forIndex.get(j);
                        if (company.getEname() != null && company.getEname().equals(company_name.getText().toString().trim())) {
                            index = j;
                            break;
                        }
                    }


                    //comp_id_name = company_id_list_forIndex.get(index);
                    comp_id_name = List_Company_forIndex.get(index).getId();

                    if (new ConnectionDetector(UsageChargesListActivity.this).isConnectingToInternet()) {
                        new get_company_job_id().execute(comp_id_name);
                    } else {
                        Toast.makeText(UsageChargesListActivity.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
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
                    job_name.setAdapter(null);
                    job_name.setText("");
                    company_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

                    List_Company = new ArrayList<>(List_Company_forIndex);
                    CompanyNameAdapterNew companyNameAdapter = new CompanyNameAdapterNew(UsageChargesListActivity.this, android.R.layout.simple_list_item_1, List_Company);
                    company_name.setAdapter(companyNameAdapter);
                    company_name.setDropDownHeight(550);

                } else {
                    company_name.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.clear, 0);
                    if (List_Company.size() == 0) {
                        job_name.setAdapter(null);
                    }

                }
            }
        });
    }

    public void Getcompany_job_id(String id) {
        List_Job.clear();
        List_Job_forIndex.clear();


        //  count = 0;
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
            String receivedString = SoapPrimitiveresult.toString();
            Log.e("receivedString", receivedString);

            if (receivedString.contains("No Data Available.")) {
                //      count = 1;
            } else {

                JSONObject jsonObject = new JSONObject(receivedString);
                JSONArray jsonArray = jsonObject.getJSONArray("cds");


                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String job_id = jsonObject1.getString("JOB_ID_PK");
                    String job_name = jsonObject1.getString("JobName");
                    String job_descripition = jsonObject1.getString("txt_Job");


                    String status = jsonObject1.getString("Status");
                    String showName = jsonObject1.getString("ShowName");
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
                    total_desc = job_name + System.getProperty("line.separator") + desc.trim();


                    List_Job.add(new Job_1(job_id, job_descripition, showName, job_name, jobtype, status, total_desc));

                }


                List_Job_forIndex = new ArrayList<>(List_Job);


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Dialog_No_JobsAvailable() {


        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UsageChargesListActivity.this);
        LayoutInflater inflater = LayoutInflater.from(UsageChargesListActivity.this);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // API 5+ solution
                onBackPressed();
                return true;
            case R.id.choose:
                ///  Check_Clock_Status();

                Intent i = new Intent(UsageChargesListActivity.this, SelectCompanyActivity.class);
                startActivityForResult(i, Utility.CODE_SELECT_COMPANY);

                return true;


            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_usage_report, menu);
        // _menu = menu;
        this.menu = menu;
        return true;
    }

    private void updateMenuTitles() {

        try {
            MenuItem bedMenuItem = menu.findItem(R.id.choose);

            if (!company_Name.equals("")) {
                if (!job_Name.equals("")) {
                    bedMenuItem.setTitle(company_Name + "\n" + job_Name);
                } else {
                    bedMenuItem.setTitle(company_Name);
                }
            } else {
                bedMenuItem.setTitle(getApplicationContext().getResources().getString(R.string.Select_Job));
            }

        } catch (Exception e) {
            e.getCause();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Utility.CODE_SELECT_COMPANY) {

            if (resultCode == Activity.RESULT_OK) {
                try {
                    compID = data.getStringExtra("CompID");
                    jobID = data.getStringExtra("JobID");
                    company_Name = data.getStringExtra("CompName");
                    job_Name = data.getStringExtra("JobName");


                    onResume();
                } catch (Exception e) {
                    e.getMessage();
                    Toast.makeText(getApplicationContext(), "Exception caught!", Toast.LENGTH_SHORT).show();
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
                finish();
            }
        }
    }

    public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MyViewHolder> {
        private List<UsageCharge> moviesList;
        //   private HttpImageManager mHttpImageManager;

        public MoviesAdapter(Activity context, List<UsageCharge> moviesList) {
            this.moviesList = moviesList;
            //     mHttpImageManager = ((AppController) context.getApplication()).getHttpImageManager();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_usage_report, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final UsageCharge usageCharge = moviesList.get(position);
            holder.index_no.setText(String.valueOf(position + 1));


            holder.tv_job_number.setText(usageCharge.getJob());
            holder.tv_company.setText(usageCharge.getCompany());
            holder.tv_User.setText(usageCharge.getUsername());
            holder.tv_Vendor.setText(usageCharge.getVendor());
            holder.tv_Desc.setText(usageCharge.getDescriptions());

            holder.tv_quantity.setText(usageCharge.getQuantity());
            holder.tv_cost.setText(usageCharge.getCost());
            holder.tv_total.setText(usageCharge.getTotal());

            holder.parentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UsageChargesListActivity.this, UsageChargesUpdateActivity.class);
                    intent.putExtra(Utility.USAGE_CHARGE_ID, usageCharge.getUsage_id());
                    intent.putExtra(Utility.IS_ADD, "0");
                    intent.putExtra("jobID", usageCharge.getJobid());
                    intent.putExtra("compid", usageCharge.getCompanyid());
                    intent.putExtra("vendorid", usageCharge.getVendorid());
                    intent.putExtra("Job", usageCharge.getJob());
                    intent.putExtra("Company", usageCharge.getCompany());
                    intent.putExtra("Vendor", usageCharge.getVendor());
                    intent.putExtra("desc", usageCharge.getDescriptions());
                    intent.putExtra("cost", usageCharge.getCost());
                    intent.putExtra("quantity", usageCharge.getQuantity());
                    intent.putExtra("total", usageCharge.getTotal());
                    startActivity(intent);


                }
            });

            holder.imgvw_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UsageChargesListActivity.this, UsageChargesUpdateActivity.class);
                    intent.putExtra(Utility.USAGE_CHARGE_ID, usageCharge.getUsage_id());
                    intent.putExtra(Utility.IS_ADD, "0");

                    intent.putExtra("jobID", usageCharge.getJobid());
                    intent.putExtra("compid", usageCharge.getCompanyid());
                    intent.putExtra("vendorid", usageCharge.getVendorid());
                    intent.putExtra("Job", usageCharge.getJob());
                    intent.putExtra("Company", usageCharge.getCompany());
                    intent.putExtra("Vendor", usageCharge.getVendor());
                    intent.putExtra("desc", usageCharge.getDescriptions());
                    intent.putExtra("cost", usageCharge.getCost());
                    intent.putExtra("quantity", usageCharge.getQuantity());
                    intent.putExtra("total", usageCharge.getTotal());
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return moviesList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_job_number,
                    tv_company,
                    tv_User,
                    tv_Vendor,
                    tv_Desc,
                    tv_quantity,
                    tv_cost,
                    tv_total;
            ImageView imgvw_edit;
            Button index_no;
            LinearLayout parentView;

            public MyViewHolder(View convertview) {
                super(convertview);

                parentView = convertview.findViewById(R.id.row_jobFile);
                index_no = (Button) convertview.findViewById(R.id.serial_no);


                tv_job_number = (TextView) convertview.findViewById(R.id.tv_job_number);
                tv_company = (TextView) convertview.findViewById(R.id.tv_company);
                tv_User = (TextView) convertview.findViewById(R.id.tv_User);
                tv_Vendor = (TextView) convertview.findViewById(R.id.tv_Vendor);
                tv_Desc = (TextView) convertview.findViewById(R.id.tv_Desc);
                tv_quantity = (TextView) convertview.findViewById(R.id.tv_quantity);
                tv_cost = (TextView) convertview.findViewById(R.id.tv_cost);
                tv_total = (TextView) convertview.findViewById(R.id.tv_total);
                imgvw_edit = convertview.findViewById(R.id.imgvw_edit);

            }
        }
    }

    private class async_list_usage_report extends AsyncTask<Void, Void, String> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(UsageChargesListActivity.this);

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
        protected String doInBackground(Void... voids) {

            String result = ListUsageReport();
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//{"status":"1","Message":"Inserted Successfully.","ReturnID":"0"}
            try {
                ringProgressDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }
            updateMenuTitles();
            if (usageChargesList.isEmpty()) {
                // Toast.makeText(getApplicationContext(),"No data found!",Toast.LENGTH_SHORT).show();
                tv_msg.setVisibility(View.VISIBLE);
                /*try {
                    dialog_companyName.show();
                } catch (Exception e) {
                    e.getMessage();
                }*/
            } else {
                tv_msg.setVisibility(View.GONE);
                mAdapter = new MoviesAdapter(UsageChargesListActivity.this, usageChargesList);
                recyclerView.setAdapter(mAdapter);
            }


        }
    }

    private class get_company_name extends AsyncTask<Void, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(UsageChargesListActivity.this);

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

            if (List_Company != null && List_Company.size() > 0) {

                dialog_SearchByCompany();
            } else {
                Toast.makeText(UsageChargesListActivity.this, "No data found!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private class get_company_job_id extends AsyncTask<String, Void, Void> {

        final ProgressDialog ringProgressDialog = new ProgressDialog(UsageChargesListActivity.this);

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


            if (List_Job.size() < 1) {
                //  if (count == 1) {
                company_name.setText("");
                job_name.setText("");
                Dialog_No_JobsAvailable();
            } else {
                BindJobData();
            }

        }
    }

}
