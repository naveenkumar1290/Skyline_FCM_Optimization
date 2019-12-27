package planet.info.skyline.tech.swo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.model.MySwo;
import planet.info.skyline.network.Api;
import planet.info.skyline.network.SOAP_API_Client;
import planet.info.skyline.progress.ProgressHUD;
import planet.info.skyline.tech.choose_job_company.SelectCompanyActivityNew;
import planet.info.skyline.shared_preference.Shared_Preference;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.network.SOAP_API_Client.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.LOADING_TEXT;

public class SwoListActivity extends AppCompatActivity {
    TextView tv_msg, txtvw_count;
    String compID = "";
    String jobID = "";
    String company_Name = "";
    String job_Name = "";
    String swo_id = "";
    boolean IsMySwo = false;
    ArrayList<MySwo> mySwoArrayList = new ArrayList<>();
    ListView listView;
    //   private RecyclerView recyclerView;
    private Menu menu;
    Context context;
    ProgressHUD mProgressHUD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=SwoListActivity.this;
        setContentView(R.layout.swo_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setView();
        getIntentData();

    }

    private void getIntentData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            IsMySwo = bundle.getBoolean("MySwo", false);
            String userRole = Shared_Preference.getUSER_ROLE(SwoListActivity.this);
            if (IsMySwo) {  // My SWO/AWO
                if (Shared_Preference.get_EnterTimesheetByAWO(this)) {
                    // if (userRole.equals(Utility.USER_ROLE_ARTIST)) {  //artist
                    setTitle(Utility.getTitle("My AWO(s)"));
                } else {
                    setTitle(Utility.getTitle("My SWO(s)"));
                }

            } else {  // Unassigned SWO/AWO
                if (Shared_Preference.get_EnterTimesheetByAWO(this)) {
                    // if (userRole.equals(Utility.USER_ROLE_ARTIST)) {  //artist
                    setTitle(Utility.getTitle("Unassigned AWO(s"));
                } else {
                    setTitle(Utility.getTitle("Unassigned SWO(s)"));
                }

            }
            if (new ConnectionDetector(SwoListActivity.this).isConnectingToInternet()) {
                new async_getSWO_AWO_List().execute();
            } else {
                Toast.makeText(SwoListActivity.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
            }

        }


    }

    private void setView() {

        txtvw_count = findViewById(R.id.txtvw_count);
        tv_msg = findViewById(R.id.tv_msg);
        tv_msg.setVisibility(View.GONE);
        listView = (ListView) findViewById(R.id.listView);


        // recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        // RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(SwoListActivity.this);
        //  recyclerView.setLayoutManager(mLayoutManager);
        // recyclerView.setItemAnimator(new DefaultItemAnimator());
        // recyclerView.addItemDecoration(new MyDividerItemDecoration(this, DividerItemDecoration.VERTICAL, 36));


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

                Intent i = new Intent(SwoListActivity.this, SelectCompanyActivityNew.class);
                i.putExtra(Utility.IS_JOB_MANDATORY, "0");
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
                    if (data.hasExtra("Swo_Id")) { // Means result is from scanning a awo qr / swo qr code
                        swo_id = data.getStringExtra("Swo_Id");
                    }

                    updateMenuTitles();
                    filterSWOList();

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

    public int getMySwoAwoList() {
        String receivedString = "";
        final String NAMESPACE = KEY_NAMESPACE;
        final String URL = SOAP_API_Client.BASE_URL;
        final String METHOD_NAME = Api.API_MY_SWO_AWO_by_Type;
        final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;


        // Create SOAP request
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        String dealerId = Shared_Preference.getDEALER_ID(this);
        String userID = Shared_Preference.getLOGIN_USER_ID(this);
        /* String Role = Shared_Preference.getUSER_ROLE(this);
        if(Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)){
            Role=Utility.USER_ROLE_ARTIST;
        }*/

        request.addProperty("user", userID);
        request.addProperty("DealerID", dealerId);
        if (Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)) {
            request.addProperty("Type", Utility.TYPE_AWO);
        } else {
            request.addProperty("Type", Utility.TYPE_SWO);
        }


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            receivedString = SoapPrimitiveresult.toString();
            JSONObject jsonObject = new JSONObject(receivedString);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");

            mySwoArrayList.clear();
            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String JOB_ID = jsonObject1.getString("JOB_ID");
                    String JOB_DESC = jsonObject1.getString("JOB_DESC");
                    String CompanyName = jsonObject1.getString("CompanyName");

                    String COMP_ID = jsonObject1.getString("COMP_ID");
                    String txt_job = jsonObject1.getString("txt_job");
                    String SWO_Status_new = jsonObject1.getString("SWO_Status_new");
                    String swo_id = jsonObject1.getString("swo_id");
                    String name = jsonObject1.getString("name");
                    String TECH_ID1 = jsonObject1.getString("TECH_ID1");
                    String TECH_ID = jsonObject1.getString("TECH_ID");

                    mySwoArrayList.add(new MySwo(JOB_ID, JOB_DESC, COMP_ID, txt_job,
                            SWO_Status_new, swo_id, name, TECH_ID1, TECH_ID, CompanyName));

                }


            }

            // sort list alphabetically
           /* Collections.sort(mySwoArrayList, new Comparator<MySwo>() {
                @Override
                public int compare(MySwo u1, MySwo u2) {
                    return u1.getName().compareToIgnoreCase(u2.getName());
                }
            });*/


        } catch (Exception e) {
            e.getMessage();
        }
        return mySwoArrayList.size();

    }

    public int getUnassignedSwoAwo() {
        String receivedString = "";
        final String NAMESPACE = KEY_NAMESPACE;
        final String URL = SOAP_API_Client.BASE_URL;
        final String METHOD_NAME = Api.API_UNASSIGNED_SWO_AWO_by_Type;
        final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;


        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        String dealerId = Shared_Preference.getDEALER_ID(this);
       /*  String Role = Shared_Preference.getUSER_ROLE(this);

        if(Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)){
            Role=Utility.USER_ROLE_ARTIST;
        }*/

        request.addProperty("DealerID", dealerId);
        if (Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)) {
            request.addProperty("Type", Utility.TYPE_AWO);
        } else {
            request.addProperty("Type", Utility.TYPE_SWO);
        }


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            receivedString = SoapPrimitiveresult.toString();
            JSONObject jsonObject = new JSONObject(receivedString);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");

            mySwoArrayList.clear();
            if (jsonArray.length() > 0) {

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String JOB_ID = jsonObject1.getString("JOB_ID");
                    String JOB_DESC = jsonObject1.getString("JOB_DESC");
                    String CompanyName = jsonObject1.getString("CompanyName");

                    String COMP_ID = jsonObject1.getString("COMP_ID");
                    String txt_job = jsonObject1.getString("txt_job");
                    String SWO_Status_new = jsonObject1.getString("SWO_Status_new");
                    String swo_id = jsonObject1.getString("swo_id");
                    String name = jsonObject1.getString("name");
                    String TECH_ID1 = jsonObject1.getString("TECH_ID1");
                    String TECH_ID = jsonObject1.getString("TECH_ID");

                    mySwoArrayList.add(new MySwo(JOB_ID, JOB_DESC, COMP_ID, txt_job,
                            SWO_Status_new, swo_id, name, TECH_ID1, TECH_ID, CompanyName));

                }
                // sort list alphabetically
               /* Collections.sort(mySwoArrayList, new Comparator<MySwo>() {
                    @Override
                    public int compare(MySwo u1, MySwo u2) {
                        return u1.getName().compareToIgnoreCase(u2.getName());
                    }
                });*/

            }


        } catch (Exception e) {
            e.getMessage();
        }
        return mySwoArrayList.size();

    }

    private void setListAdapter(final ArrayList<MySwo> arrayList) {

        txtvw_count.setText("Total: " + arrayList.size());

        ArrayAdapter<MySwo> stringArrayAdapter = new ArrayAdapter<MySwo>(SwoListActivity.this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(stringArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                new async_getJobDetailsBySwoId().execute(arrayList.get(i).getSwo_id());

               /* String comp_id = arrayList.get(i).getCOMP_ID();
                String swo_id = arrayList.get(i).getSwo_id();
                String CompanyName = arrayList.get(i).getCompanyName();
                String JobName = arrayList.get(i).getTxt_job();
                String JOB_ID = arrayList.get(i).getJOB_ID();


                Intent intent = new Intent();
                intent.putExtra("Comp_id", comp_id);
                intent.putExtra("Swo_id", swo_id);
                intent.putExtra("CompanyName", CompanyName);
                intent.putExtra("JobName", JobName);
                intent.putExtra("JOB_ID", JOB_ID);
                setResult(Utility.SWO_LIST_REQUEST_CODE, intent);
                finish();
*/


            }
        });
    }

    private void filterSWOList() {
        if (mySwoArrayList != null && mySwoArrayList.size() > 0) {
            ArrayList<MySwo> filteredSWOList = new ArrayList<>();
            if (!swo_id.equals("")) { // only for swo/awo id
                for (MySwo mySwo : mySwoArrayList) {
                    if (mySwo.getSwo_id().equals(swo_id)) {
                        filteredSWOList.add(mySwo);
                    }
                }

            } else if (!compID.equals("") && jobID.equals("")) {  // only compId
                for (MySwo mySwo : mySwoArrayList) {
                    if (mySwo.getCOMP_ID().equals(compID)) {
                        filteredSWOList.add(mySwo);
                    }
                }
            } else if (compID.equals("") && !jobID.equals("")) {  // only jobId
                for (MySwo mySwo : mySwoArrayList) {
                    if (mySwo.getJOB_ID().equals(jobID)) {
                        filteredSWOList.add(mySwo);
                    }
                }
            } else if (!compID.equals("") && !jobID.equals("")) {  //  compId && jobID
                for (MySwo mySwo : mySwoArrayList) {
                    if (mySwo.getCOMP_ID().equals(compID) && mySwo.getJOB_ID().equals(jobID)) {
                        filteredSWOList.add(mySwo);
                    }
                }
            }

            if (filteredSWOList.size() == 0) {
                tv_msg.setText("No swo found for this Job/Company!");
                tv_msg.setVisibility(View.VISIBLE);
            } else {
                tv_msg.setVisibility(View.GONE);
            }

            setListAdapter(filteredSWOList);


        }
    }

    private void dialog_SHOW_Info(final String CompanyID, final String JobID, final String swo_awo_id, final String CompanyName, final String JobName, final String JobType, final String JobDesc, final String Status, final String ShowName, final String AWO_SWO_Name) {

        final Dialog dialog_JobShowDetails = new Dialog(SwoListActivity.this);
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
        ll_swo_awo_info.setVisibility(View.VISIBLE);
        final Button cancle = (Button) dialog_JobShowDetails.findViewById(R.id.cancle);
        final Button btn_GO = (Button) dialog_JobShowDetails.findViewById(R.id.proced);
        tv_Company.setText(CompanyName);
        t1.setText(JobName);
        t2.setText(JobType);
        t3.setText(Status);
        if(ShowName==null || ShowName.equalsIgnoreCase("null")){
            t4.setText("N/A");
        }else {
            t4.setText(ShowName);
        }

        t5.setText(JobDesc);
        if (Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)) {
            tv_title_awo_swo.setText("AWO #");
        } else {
            tv_title_awo_swo.setText("SWO #");
        }
        tv_awo_swo.setText(AWO_SWO_Name);

        btn_GO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_JobShowDetails.dismiss();
                SendDataToCallingActivity(CompanyID, JobID, CompanyName, JobName, swo_awo_id);
            }
        });

        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog_JobShowDetails.dismiss();
            }
        });

    }

    private void SendDataToCallingActivity(String CompanyId, String JObId, String CompanyName, String JobName, String swo_awo_id) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("Comp_id", CompanyId);
        returnIntent.putExtra("JOB_ID", JObId);
        returnIntent.putExtra("CompanyName", CompanyName);
        returnIntent.putExtra("JobName", JobName);
        returnIntent.putExtra("Swo_id", swo_awo_id);

        setResult(Activity.RESULT_OK, returnIntent);
        finish();

    }

    private class async_getSWO_AWO_List extends AsyncTask<Void, Void, Integer> {
      //  ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  pDialog = new ProgressDialog(SwoListActivity.this);
            pDialog.setMessage("Kindly wait");
            pDialog.setCancelable(false);
            try {
                pDialog.show();
            } catch (Exception e) {
                e.getMessage();
            }*/
          showprogressdialog();
        }


        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            /*try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }*/
            hideprogressdialog();
            String userRole = Shared_Preference.getUSER_ROLE(SwoListActivity.this);
            if (mySwoArrayList.size() > 0) {
                tv_msg.setVisibility(View.GONE);
            } else {
                tv_msg.setVisibility(View.VISIBLE);
            }

            tv_msg.setText("No data Available!");

           /* if (IsMySwo) {  // My SWO/AWO
                if (userRole.equals(Utility.USER_ROLE_ARTIST)) {  //artist
                    tv_msg.setText("No AWO Available!");
                } else {
                    tv_msg.setText("No SWO Available!");
                }
            } else {  // Unassigned SWO/AWO
                if (userRole.equals(Utility.USER_ROLE_ARTIST)) {  //artist
                    tv_msg.setText("No AWO Available!");
                } else {
                    tv_msg.setText("No SWO Available!");
                }
            }
*/
            setListAdapter(mySwoArrayList);
        }

        @Override
        protected Integer doInBackground(Void... params) {

            int size = 0;
            if (IsMySwo) {

                size = getMySwoAwoList();
            } else {

                size = getUnassignedSwoAwo();


            }
            return size;
        }

    }

    private class async_getJobDetailsBySwoId extends AsyncTask<String, Void, JSONObject> {
       // ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
           /* pDialog = new ProgressDialog(SwoListActivity.this);
            pDialog.setMessage("Kindly wait");
            pDialog.setCancelable(false);
            try {
                pDialog.show();
            } catch (Exception e) {
                e.getMessage();
            }*/
           showprogressdialog();
            super.onPreExecute();
        }


        @Override
        protected JSONObject doInBackground(String... param) {

            JSONObject js_obj = new JSONObject();
            final String NAMESPACE = KEY_NAMESPACE;
            final String URL = SOAP_API_Client.BASE_URL;
            final String METHOD_NAME = Api.API_GetJobDetailsBy_SWO_AWO;
            final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;

            String awo_swo_id = param[0];
            SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
            request.addProperty("AWO_SWO", awo_swo_id);
            if (Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)) {
                request.addProperty("type", Utility.TYPE_AWO);
            } else {
                request.addProperty("type", Utility.TYPE_SWO);
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
                js_obj.put("swo_awo_id", awo_swo_id);

            } catch (Exception e) {

                e.printStackTrace();
            }
            return js_obj;
        }

        @Override
        protected void onPostExecute(JSONObject js_obj) {
            super.onPostExecute(js_obj);
            Utility.hideKeyboard((Activity) SwoListActivity.this);
           /* try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }*/
           hideprogressdialog();
            try {
                String login_dealerId = Shared_Preference.getDEALER_ID(SwoListActivity.this);
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
                String swo_awo_id = js_obj.getString("swo_awo_id");
                String Swo_Awo_Name = "";
                if (Shared_Preference.get_EnterTimesheetByAWO(SwoListActivity.this)) {
                    Swo_Awo_Name = js_obj.getString("AwoName");
                } else {
                    Swo_Awo_Name = js_obj.getString("SwoName");
                }

                dialog_SHOW_Info(comp_id, job_id, swo_awo_id, companyName, jobName, JOB_TYPE, desciption, jobstatus, Show_Name, Swo_Awo_Name);

            } catch (Exception e) {
                e.getMessage();
                Toast.makeText(getApplicationContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void showprogressdialog() {
        try {
            mProgressHUD = ProgressHUD.show(context, LOADING_TEXT, false);
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public void hideprogressdialog() {
        try {
            if (mProgressHUD.isShowing()) {
                mProgressHUD.dismiss();
            }
        } catch (Exception e) {
            e.getMessage();
        }
    }

}
