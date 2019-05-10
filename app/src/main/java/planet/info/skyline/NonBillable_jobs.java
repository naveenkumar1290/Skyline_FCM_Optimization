package planet.info.skyline;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.util.Utility.KEY_NAMESPACE;
import static planet.info.skyline.util.Utility.URL_EP1;
import static planet.info.skyline.util.Utility.URL_EP2;


/**
 * Created by planet on 5/5/16.
 */
public class NonBillable_jobs extends BaseActivity {
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    int[] arrid;//=new int[4];
    String[] arrname;//=new String[4];
    //String end_time = "";
    String idddd;
    SharedPreferences sp;
    SharedPreferences.Editor ed;
    String start_time, descriptionm;
    //String urlofwebservice11 = URL_EP2 + "/WebService/techlogin_service.asmx?op=bind_billable_nonBillable_code";
    String urlofwebservice11_new = URL_EP2 + "/WebService/techlogin_service.asmx?";
    //nks
    Dialog dialog_desc;
    // String clientidme;
    String jobid = "";
    String clientid = "";


    String nextact = URL_EP1 + "/crate_web_service.php?id="
            + clientid;
    Dialog dialog_NonBillableCodes;
    String UniqueId = "";
    String imei = "";

    String JOB_STOP_HrsMinuts, JOB_START_HrsMinuts, Total_time, dayInfo;
    SoapObject request;

    String JOB_START_DateTime;//dd-MM-yyyy HH:mm:ss
    String JOB_STOP_DateTime;//dd-MM-yyyy HH:mm:ss
    AlertDialog alertDialog;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanforworkstation);

        sp = getApplicationContext().getSharedPreferences("skyline", MODE_PRIVATE);
        ed = sp.edit();
        start_time = sp.getString("starttimenew", "");
        jobid = "605";
        clientid = sp.getString("clientid", "");
        nextact = URL_EP1 + "/crate_web_service.php?id="
                + clientid;

        Utility.showChatHead(NonBillable_jobs.this);


        if (new ConnectionDetector(NonBillable_jobs.this).isConnectingToInternet()) {
            new Async_Get_NonBillableCodes().execute();
        } else {
            Toast.makeText(NonBillable_jobs.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();

        }

    }

    /*nks*/
    /*on floating view click this class opens and when this class alreay open and we click
     * on floating view then a new window also opens So we have to store in preference
     * the state of this class*/
    @Override
    protected void onResume() {
        super.onResume();
        sp = getApplicationContext().getSharedPreferences("skyline", MODE_PRIVATE);
        ed = sp.edit();
        ed.putBoolean(Utility.IS_NON_BILLABLE_IN_FRONT, true).apply();
        // AppController.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sp = getApplicationContext().getSharedPreferences("skyline", MODE_PRIVATE);
        ed = sp.edit();
        ed.putBoolean(Utility.IS_NON_BILLABLE_IN_FRONT, false).apply();

        //   AppController.activityPaused();
    }

    public void Dialog_Choose_NonBillableCode() {

        dialog_NonBillableCodes = new Dialog(NonBillable_jobs.this);
        dialog_NonBillableCodes.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_NonBillableCodes.setContentView(R.layout.radiogroupfortimesheat);
        dialog_NonBillableCodes.setCancelable(true);//nks
        dialog_NonBillableCodes.setCanceledOnTouchOutside(false);
      /*
        dialog_NonBillableCodes.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
*/

        RadioGroup rg = (RadioGroup) dialog_NonBillableCodes.findViewById(R.id.radioGroup1);
        // int ii = 1000;

        try {
//            for (int i = 0; i < 8; i++) {
//                ii = ii--;
//                RadioButton button = new RadioButton(this);
//                button.setId(ii);
//                button.setText("");
//                button.setChecked(false);
//                rg.addView(button);
//                button.setVisibility(View.GONE);
//            }

            for (int i = 0; i <= arrid.length - 1; i++) {
                RadioButton button = new RadioButton(this);
                button.setId(arrid[i]);
                String names = arrname[i];
                button.setText(names);
                button.setChecked(false);


                rg.addView(button);

            }

        } catch (Exception e) {
            e.getMessage();
        }

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                idddd = String.valueOf(checkedId);
                dialog_NonBillableCodes.dismiss();
                Dialog_EnterDesc();


            }
        });

        //nks
        dialog_NonBillableCodes.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                onBackPressed();
            }
        });




        /**/

        try {
            dialog_NonBillableCodes.show();
        } catch (Exception e) {
            e.getMessage();
        }


        /**/


    }

    public void Dialog_EnterDesc() {

        //final Dialog showd = new Dialog(NonBillable_jobs.this);
        dialog_desc = new Dialog(NonBillable_jobs.this);
        dialog_desc.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog_desc.setContentView(R.layout.enterdescriptiondata_new);

        dialog_desc.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog_desc.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);


        dialog_desc.setCancelable(true);
        dialog_desc.setCanceledOnTouchOutside(false);


        final EditText ed1 = (EditText) dialog_desc.findViewById(R.id.texrtdesc);
        TextView btn_Submit = (TextView) dialog_desc.findViewById(R.id.Btn_No);
        btn_Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (ed1.getText().length() == 0) {

                    Toast.makeText(NonBillable_jobs.this, "Please enter description!", Toast.LENGTH_SHORT).show();
                    return;

                } else {
                    dialog_desc.dismiss();

                    Utility.hideKeyboard(NonBillable_jobs.this);
                    PrepareApiData(ed1.getText().toString().trim());
                }
            }
        });
        //nks start

        ImageView close = (ImageView) dialog_desc.findViewById(R.id.close);
        close.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                onBackPressed();
                return true;
            }
        });


        dialog_desc.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                dialog_desc.dismiss();
                onBackPressed();
            }

        });


        try {
            dialog_desc.show();
        } catch (Exception e) {
            e.getMessage();
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        //  startActivity(new Intent(NonBillable_jobs.this, MainActivity.class));
        finish();

    }

    public String submit_NonBillable_Timesheet() {
        String receivedString = "";
        String NonBillableTimeSheetID = "";
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = URL_EP2 + "/WebService/techlogin_service.asmx";
        final String SOAP_ACTION = KEY_NAMESPACE + Utility.Method_NON_BILLABLE_TIMESHEET;
        final String METHOD_NAME = Utility.Method_NON_BILLABLE_TIMESHEET;
/*
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("tech_id", clientid);
        request.addProperty("jobId", jobid);

        request.addProperty("start_time", JOB_START_HrsMinuts);
        request.addProperty("end_time", JOB_STOP_HrsMinuts);
        request.addProperty("description", descriptionm);
        request.addProperty("code", idddd);
        request.addProperty("region", descriptionm + "  " + "(" + Total_time + ")");
        request.addProperty("status", "3");
        //  request.addProperty("dayInfo", dayInfo);*/

        Log.e("api data", request.toString());
        //
        // UniqueId = Utility.getUniqueId();
        //  Utility.saveOfflineIncompleteAsynctask(NonBillable_jobs.this, request.toString(), UniqueId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
            receivedString = SoapPrimitiveresult.toString();
            Log.e("receivedString", receivedString);
            JSONObject jsonObject = new JSONObject(receivedString);
            //  {"ID":"0","Output":"0","Message":"Overlapping Time entries"}
            NonBillableTimeSheetID = jsonObject.getString("ID");
            ed.putString(Utility.TIME_SHEET_ID, NonBillableTimeSheetID).commit();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Exception", e.getMessage());
        }

        return receivedString;
    }

    public void Get_NonBillableCodes() {
        String receivedString = "";
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = urlofwebservice11_new;
        final String METHOD_NAME =  Utility.METHOD_BILLABLE_NONBILLABLE_CODE;
        final String SOAP_ACTION = KEY_NAMESPACE +  METHOD_NAME;

        // Create SOAP request
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        // Boolean jobid = sp.getBoolean("billable", false);
        String dealerId = sp.getString(Utility.DEALER_ID, "0");
        String role = sp.getString(Utility.LOGIN_USER_ROLE, "0");

        /*request.addProperty("type", "2");
        //nks
        request.addProperty("dealerID", dealerId);*/


        //
        request.addProperty("dealerID", dealerId);
        request.addProperty("cat", role);
        request.addProperty("type", "2");

        //nks
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

        } catch (Exception e) {

            e.printStackTrace();
        }

        try {
            String Jsonstring = receivedString;
            String news = Jsonstring.substring(Jsonstring.indexOf("["), Jsonstring.indexOf("]") + 1);
            String n1 = news;
            JSONArray jArray = new JSONArray(n1);
            int len = jArray.length();
            arrid = new int[len];
            arrname = new String[len];
            for (int k = 0; k < (jArray.length()); k++) {
                JSONObject json_obj = jArray.getJSONObject(k);
                arrid[k] = json_obj.getInt("Lalor_ID_PK");
                arrname[k] = json_obj.getString("Labor_name");

            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    private void PrepareApiData(String desc) {

        try {
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            e.getCause();
        }
        descriptionm = desc;
        final String NAMESPACE = KEY_NAMESPACE;
        final String METHOD_NAME = Utility.Method_NON_BILLABLE_TIMESHEET;
        ;
        request = new SoapObject(NAMESPACE, METHOD_NAME);

        Total_time = Utility.get_TotalClockTime(NonBillable_jobs.this);

        JOB_START_DateTime = sp.getString(Utility.CLOCK_START_TIME, "");//dd-MM-yyyy HH:mm:ss
        JOB_STOP_DateTime = Utility.getCurrentTimeString();//dd-MM-yyyy HH:mm:ss

       /* ed.putString(Utility.TIMEGAP_JOB_END_TIME, JOB_STOP_DateTime).commit();//for next job time gap
        ed.putString(Utility.TIMEGAP_PREV_JOB_START_TIME, JOB_START_DateTime).commit();//for next job time gap
       */
        boolean bool_IsSameDay = Utility.IsSameDay(JOB_START_DateTime, JOB_STOP_DateTime);
        dayInfo = "0";
        if (bool_IsSameDay) dayInfo = "0";
        else dayInfo = "1";

        String arr1[] = JOB_START_DateTime.split(" ");
        String str1 = arr1[1];
        JOB_START_HrsMinuts = str1.substring(0, str1.lastIndexOf(":"));//HH:mm

        String arr[] = JOB_STOP_DateTime.split(" ");
        String str = arr[1];
        JOB_STOP_HrsMinuts = str.substring(0, str.lastIndexOf(":"));//HH:mm

        /**/
        request.addProperty("tech_id", clientid);
        request.addProperty("jobId", jobid);
        request.addProperty("start_time", JOB_START_HrsMinuts);
        request.addProperty("end_time", JOB_STOP_HrsMinuts);
        request.addProperty("description", descriptionm);
        request.addProperty("code", idddd);
        request.addProperty("region", descriptionm + "  " + "(" + Total_time + ")");
        request.addProperty("status", "3");
        request.addProperty("PhoneType", "Android");
        request.addProperty("EMI", imei);

        //  request.addProperty("dayInfo", dayInfo);

        if (new ConnectionDetector(NonBillable_jobs.this).isConnectingToInternet()) {
            /*
            ed.putString(Utility.TIMEGAP_JOB_END_TIME, JOB_STOP_DateTime).commit();//for next job time gap
            ed.putString(Utility.TIMEGAP_PREV_JOB_START_TIME, JOB_START_DateTime).commit();//for next job time gap
           */

            Utility.StopRunningClock(NonBillable_jobs.this);
            new Async_Submit_NonBillable_Timesheet().execute();

        } else {
            Toast.makeText(NonBillable_jobs.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
            showDialog_for_OfflineApiDataSave(NonBillable_jobs.this, request.toString(), Utility.getUniqueId());
        }
    }

    public void showDialog_for_OfflineApiDataSave(final Context context, final String api_input, final String unique_apiId) {
//ToDo
        final Dialog showd = new Dialog(context);
        showd.requestWindowFeature(Window.FEATURE_NO_TITLE);
        showd.setContentView(R.layout.time_gap);
        showd.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        showd.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        showd.setCancelable(false);
        showd.setCanceledOnTouchOutside(false);

        TextView tv_title = (TextView) showd.findViewById(R.id.tv_title);
        TextView tv_msg = (TextView) showd.findViewById(R.id.tv_msg);
        ImageView close = (ImageView) showd.findViewById(R.id.close);

        final TextView Btn_Done = (TextView) showd.findViewById(R.id.Btn_Done);
        final TextView Btn_Cancel = (TextView) showd.findViewById(R.id.Btn_Select_Another);
        Btn_Done.setText("OK");
        Btn_Cancel.setText("Cancel");

        tv_title.setText("Lost Internet!");
        tv_title.setVisibility(View.GONE);

        tv_msg.setText(Utility.MSG_OFFLINE_DATA_SAVE);
        //tv_msg.setGravity( Gravity.CENTER|Gravity.LEFT );
        Btn_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showd.dismiss();
                Utility.StopRunningClock(NonBillable_jobs.this);
                Utility.saveOfflineIncompleteAsynctask(context, api_input, unique_apiId, "Non-Billable");
                Toast.makeText(NonBillable_jobs.this, Utility.SAVED, Toast.LENGTH_SHORT).show();
                onBackPressed();

            }
        });
        Btn_Cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showd.dismiss();
                onBackPressed();

            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showd.dismiss();
                onBackPressed();
            }
        });

        showd.show();


    }

    private void dialog_OverlappingTimeEntry() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_yes_no, null);
        dialogView.setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialogBuilder.setView(dialogView);

        final TextView desc = dialogView.findViewById(R.id.texrtdesc);
        final TextView title = dialogView.findViewById(R.id.textView1rr);


        final Button positiveBtn = dialogView.findViewById(R.id.Btn_Yes);
        final Button negativeBtn = dialogView.findViewById(R.id.Btn_No);
        ImageView close = (ImageView) dialogView.findViewById(R.id.close);
        close.setVisibility(View.INVISIBLE);
        // dialogBuilder.setTitle("Device Details");
        title.setText("Overlapping Time Entry.");
        desc.setText("Edit the Entry!");
        // message.setText("Please set Automatic date and Time");
        //
        positiveBtn.setText("Ok");
        negativeBtn.setText("No");
        negativeBtn.setVisibility(View.VISIBLE);
        positiveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();

                String reason = descriptionm + "  " + "(" + Total_time + ")";
                Utility.saveOverlapTimeSheetData(NonBillable_jobs.this,
                        jobid,
                        JOB_START_HrsMinuts,
                        JOB_STOP_HrsMinuts,
                        descriptionm,
                        idddd,
                        reason,
                        "3",
                        "",
                        "Non-Billable",
                        Utility.NON_BILLABLE, "","","");

                Intent i = new Intent(getApplicationContext(), TimeSheetList1Activity.class);

              /*  i.putExtra("jobId", jobid);
                i.putExtra("start_time", JOB_START_HrsMinuts);
                i.putExtra("end_time", JOB_STOP_HrsMinuts);
                i.putExtra("description", descriptionm);
                i.putExtra("code", idddd);
                i.putExtra("region", reason);
                i.putExtra("status", "3");
                i.putExtra("jobDesc", "Non-Billable");
                i.putExtra("jobType", Utility.NON_BILLABLE);*/

                startActivity(i);
                finish();


            }
        });
        negativeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
                String reason = descriptionm + "  " + "(" + Total_time + ")";
                Utility.saveOverlapTimeSheetData(NonBillable_jobs.this,
                        jobid,
                        JOB_START_HrsMinuts,
                        JOB_STOP_HrsMinuts,
                        descriptionm,
                        idddd,
                        reason,
                        "3",
                        "",
                        "Non-Billable",
                        Utility.NON_BILLABLE, "","","");

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(i);
                finish();


            }
        });
        alertDialog = dialogBuilder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    class Async_Get_NonBillableCodes extends AsyncTask<Void, Void, Void> {
        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(NonBillable_jobs.this);
            pDialog.setMessage("Kindly wait");
            pDialog.setCancelable(false);
            pDialog.show();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                pDialog.dismiss();
            } catch (Exception e) {
                e.getMessage();
            }
            if (arrid != null && arrid.length > 0) {
                Dialog_Choose_NonBillableCode();
            } else {
                Toast.makeText(NonBillable_jobs.this, "Error! There are NO Job Codes currently in the system!", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            Get_NonBillableCodes();
            return null;
        }

    }

    public class Async_Submit_NonBillable_Timesheet extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            //  Utility.StopRunningClock(NonBillable_jobs.this);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            JSONObject jsonObject = null;
            String output = "";
            String msg = "";
            try {
                jsonObject = new JSONObject(result);
                //  {"ID":"0","Output":"0","Message":"Overlapping Time entries"}
                Log.e("AdminTimeSheetId", result);
                output = jsonObject.getString("Output");
                msg = jsonObject.getString("Message");
            } catch (Exception e) {
                e.getCause();
            }
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            if (output.equals("1")) {   //means sheet submitted successfully
                ed.putString(Utility.TIMEGAP_JOB_END_TIME, JOB_STOP_DateTime).commit();//for next job time gap
                ed.putString(Utility.TIMEGAP_PREV_JOB_START_TIME, JOB_START_DateTime).commit();//for next job time gap

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();

            } else if (output.equals("0")) { //overlap
                dialog_OverlappingTimeEntry();
            }


        }

        @Override
        protected String doInBackground(Void... params) {
            // TODO Auto-generated method stub
            String NonBillableTimeSheetID = submit_NonBillable_Timesheet();
            Log.e("NonBillableTimeSheetID", NonBillableTimeSheetID);
            return NonBillableTimeSheetID;
        }

    }
}
