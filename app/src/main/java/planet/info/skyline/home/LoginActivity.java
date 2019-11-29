package planet.info.skyline.home;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

import planet.info.skyline.R;
import planet.info.skyline.adapter.SelectClientUser_ListAdapter;
import planet.info.skyline.client.ClientHomeActivity;
import planet.info.skyline.crash_report.ConnectionDetector;
import planet.info.skyline.model.ClientModel;
import planet.info.skyline.network.SOAP_API_Client;
import planet.info.skyline.old_activity.BaseActivity;
import planet.info.skyline.tech.runtime_permission.PermissionActivity;
import planet.info.skyline.tech.shared_preference.Shared_Preference;
import planet.info.skyline.util.Utility;

import static planet.info.skyline.network.Api.API_varify_tech;
import static planet.info.skyline.network.SOAP_API_Client.KEY_NAMESPACE;


public class LoginActivity extends BaseActivity {//implements ActivityCompat.OnRequestPermissionsResultCallback {

    static final int RequestPermissionCode = 100;
    private static final int CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE = 100;
    EditText ed_username, ed_passwo;
    TextView rl_btnRegister;
    ProgressDialog pDialog;

    AlertDialog dialog;

    String uname, upass;
    int resultmy;
    String urlofwebservice = SOAP_API_Client.BASE_URL;

    JSONArray jsonArray = new JSONArray();
    ArrayList<ClientModel> List_Client = new ArrayList<>();
    String[] Permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_userregistat_new);
        ed_username = (EditText) findViewById(R.id.reg_fullname);
        ed_username = (EditText) findViewById(R.id.reg_fullname);
        // getToken();

     /*   if (CheckingPermissionIsEnabledOrNot()) {
        } else {
            RequestMultiplePermission();
        }
*/
        if (PermissionActivity.CheckingPermissionIsEnabledOrNot(LoginActivity.this)) {
        } else {
            Intent i = new Intent(getApplicationContext(), PermissionActivity.class);
            startActivityForResult(i, Utility.REQUEST_CODE_PERMISSIONS);
        }


        ed_passwo = (EditText) findViewById(R.id.reg_password);

        rl_btnRegister = (TextView) findViewById(R.id.btnRegister);
        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage(getString(R.string.Loading_text));
        pDialog.setCancelable(false);

        rl_btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(LoginActivity.this);
                String upasss = ed_passwo.getText().toString();
                String upnam = ed_username.getText().toString();
                uname = upnam;
                upass = upasss;
                if (uname.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Please Enter Username!", Toast.LENGTH_LONG).show();
                    return;
                } else if (upass.length() == 0) {
                    Toast.makeText(LoginActivity.this, "Please Enter Password!", Toast.LENGTH_LONG).show();
                    return;
                } else if (new ConnectionDetector(LoginActivity.this).isConnectingToInternet()) {
                    if (uname.contains("@")) {  //client login
                        new async_loginClient().execute();
                    } else {
                        new async_login().execute();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, Utility.NO_INTERNET, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.userregistat, menu);
        return true;
    }


    public void showprogressdialog() {

        pDialog.show();
    }

    public void hideprogressdialog() {
        pDialog.dismiss();
    }

    public void fun_login() {


        JSONObject json_obj = new JSONObject();

        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = urlofwebservice;
        final String SOAP_ACTION = KEY_NAMESPACE + API_varify_tech;
        final String METHOD_NAME = API_varify_tech;
        // Create SOAP request

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("uid", uname);
        request.addProperty("pass", upass);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        httpTransport.debug = true;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            //nks
            Log.d("HTTP REQUEST ", httpTransport.requestDump);
            Log.d("HTTP RESPONSE", httpTransport.responseDump);
            Object results = (Object) envelope.getResponse();

            String resultstring = results.toString();//{"cds":[{"id":"","name":"","mobile":"","result":2,"cat":0,"dealerID":0}]}
            JSONObject jsonObject = new JSONObject(resultstring);
            JSONArray jsonArray = jsonObject.getJSONArray("cds");
            if (jsonArray.length() > 0) {
                json_obj = jsonArray.getJSONObject(0);
            }
            String result = json_obj.getString("result");
            //pm and dc can only view 2 modules upload photo and show file.
            String role = json_obj.getString("cat");
            Shared_Preference.setUSER_ROLE(this, role);
            String dealerId = json_obj.getString("dealerID");
            Shared_Preference.setDEALER_ID(this, dealerId);
            //nks
            String name, mob, id;
            Shared_Preference.setLOGIN_ID(this, uname);

            if (result.equals("0") || result.equalsIgnoreCase("0")) {
                resultmy = 0;
                name = json_obj.getString("name");
                mob = json_obj.getString("mobile");
                id = json_obj.getString("id");

                Shared_Preference.setLOGIN_USERNAME(this, name);
                Shared_Preference.setUSER_MOBILE(this, mob);
                Shared_Preference.setLOGIN_USER_ID(this, id);


            } else if (result.equals("1") || result.equalsIgnoreCase("1")) {
                resultmy = 1;
                name = json_obj.getString("name");
                mob = json_obj.getString("mobile");
                id = json_obj.getString("id");
                /*By naveen            to           make login of Project Manger */

                Shared_Preference.setLOGIN_USERNAME(this, name);
                Shared_Preference.setUSER_MOBILE(this, mob);
                Shared_Preference.setLOGIN_USER_ID(this, id);
                /*By Naveen*/
            } else {
                resultmy = 2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fun_login_client() {

        JSONObject json_obj = new JSONObject();
        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = urlofwebservice;
        final String SOAP_ACTION = KEY_NAMESPACE + API_varify_tech;
        final String METHOD_NAME = API_varify_tech;

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("uid", uname);
        request.addProperty("pass", upass);
        request.addProperty("Connection", "Close");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        httpTransport.debug = true;
        try {
            httpTransport.call(SOAP_ACTION, envelope);

            Log.d("HTTP REQUEST ", httpTransport.requestDump);
            Log.d("HTTP RESPONSE", httpTransport.responseDump);
            Object results = (Object) envelope.getResponse();

            String resultstring = results.toString();//{"cds":[{"userID":0,"txt_Mail":"","CompID":0,"CompName":"","UserName":"","UserCategory":"","ctype":"","DealerID":0,"dtype":"","Login_Email":"","dealer_name":"","status":0}]}
            JSONObject jsonObject = new JSONObject(resultstring);
            jsonArray = jsonObject.getJSONArray("cds");

            if (jsonArray.length() > 0) {
                json_obj = jsonArray.getJSONObject(0);
                List_Client.clear();

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                    String userID = jsonObject1.getString("userID");
                    String txt_Mail = jsonObject1.getString("txt_Mail");
                    String CompID = jsonObject1.getString("CompID");
                    String CompName = jsonObject1.getString("CompName");
                    String UserName = jsonObject1.getString("UserName");
                    String UserCategory = jsonObject1.getString("UserCategory");
                    String CaType = jsonObject1.getString("CaType");
                    String DealerID = jsonObject1.getString("DealerID");
                    String dtype = jsonObject1.getString("dtype");
                    String Login_Email = jsonObject1.getString("Login_Email");
                    String dealer_name = jsonObject1.getString("dealer_name");
                    String status = jsonObject1.getString("status");

                    String Masterstatus = jsonObject1.getString("Masterstatus");
                    String Imagepath = jsonObject1.getString("Imagepath");


                    List_Client.add(new ClientModel(userID, txt_Mail, CompID, CompName, UserName, UserCategory,
                            CaType, DealerID, dtype, Login_Email, dealer_name, status, Imagepath, Masterstatus));
                }

            }

            String userID = json_obj.getString("userID");
            String status = json_obj.getString("status");
            if (userID.equalsIgnoreCase("0") && status.equalsIgnoreCase("0")) {
                resultmy = 0;//user or password wrong
            } else if (status.equalsIgnoreCase("2")) {
                resultmy = 2; //blocked
            } else {
                resultmy = 1; //success
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void dialog_user_detail() {

        /**/

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.alertfornamemobilenumber_new, null);

        dialogBuilder.setView(dialogView);
        dialog = dialogBuilder.create();
        dialog.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialog.setCancelable(false);
        TextView name = (TextView) dialogView.findViewById(R.id.namedata);
        TextView mobil = (TextView) dialogView.findViewById(R.id.mobiledata);
        TextView ok = (TextView) dialogView.findViewById(R.id.okbuton);
        TextView cancel = (TextView) dialogView.findViewById(R.id.cancelbuton);
       /* String mob = sp.getString("mob", "");
        String username = sp.getString("tname", "");*/

        String mob = Shared_Preference.getUSER_MOBILE(this);
        String username = Shared_Preference.getLOGIN_USERNAME(this);

        name.setText(username);
        mobil.setText(mob);

        ok.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                /**
                 * Bhanu work continue from 6/04/2016
                 */
                dialog.dismiss();
                Utility.setLoginTrue(LoginActivity.this, Utility.LOGIN_TYPE_NORMAL);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
        cancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                //  ed.putString("tname", "").commit();
                //  ed.clear().commit();
                Shared_Preference.delete_SharedPreference(LoginActivity.this);
            }
        });


        if (!username.equals("")) {
            dialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "Some error occurred!", Toast.LENGTH_SHORT).show();
        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //  is equal our requested code for draw permission
        if (requestCode == CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(LoginActivity.this)) {
                    //Permission Granted by Overlay!!!
                    //Do your Stuff
                } else {
                    //Permission Not Granted by Overlay!!!
                    //Do your Stuff
                    Toast.makeText(LoginActivity.this, "Please allow the permission." + System.getProperty("line.separator") + " This will be used to show the clock ! ", Toast.LENGTH_SHORT).show();
                    //   final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + LoginActivity.this.getPackageName()));
                    //   startActivityForResult(intent, CHATHEAD_OVERLAY_PERMISSION_REQUEST_CODE);
                }

            }
        }
        if (requestCode == Utility.REQUEST_CODE_PERMISSIONS) {
            if (resultCode == RESULT_OK) {
                Log.e("", "");
            } else {

            }
        }
    }


    void Dialog_SelectUser() {

        final Dialog dialog1 = new Dialog(LoginActivity.this);
        dialog1.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog1.setContentView(R.layout.login_client_list);
        dialog1.setCancelable(false);
        ListView listView = (ListView) dialog1.findViewById(R.id.listView);

        SelectClientUser_ListAdapter adapter = new SelectClientUser_ListAdapter(this, List_Client);
        listView.setAdapter(adapter);

        Button btn_cancel = (Button) dialog1.findViewById(R.id.btn_cancel);

        btn_cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.dismiss();


            }
        });


        dialog1.show();


    }

    private void RequestMultiplePermission() {
        // Creating String Array with Permissions.
        ActivityCompat.requestPermissions(LoginActivity.this, Permissions, RequestPermissionCode);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            case RequestPermissionCode:
                if (grantResults.length > 0) {
                    boolean ReadExternalStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean Camera = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if (ReadExternalStorage && Camera) {

                        checkDrawOverlayPermission();

                    } else {
                        Toast.makeText(LoginActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean CheckingPermissionIsEnabledOrNot() {
        boolean AllPermissionGranted = true;

        for (int i = 0; i < Permissions.length; i++) {
            int PermissionResult = ContextCompat.checkSelfPermission(getApplicationContext(), Permissions[i]);
            if (PermissionResult == PackageManager.PERMISSION_GRANTED) {
                AllPermissionGranted = true;
            } else {
                AllPermissionGranted = false;
            }
        }
        return AllPermissionGranted;
    }


    public void UpdateTokenOnServer() {


        // String empId = sp.getString("clientid", "");
        String empId = Shared_Preference.getLOGIN_USER_ID(this);
        // String fcm_token = sp.getString(Utility.FCM_TOKEN, "");
        String fcm_token = Shared_Preference.getFCM_TOKEN(this);


        final String NAMESPACE = KEY_NAMESPACE + "";
        final String URL = urlofwebservice;
        final String SOAP_ACTION = KEY_NAMESPACE + "";
        final String METHOD_NAME = "";
        // Create SOAP request

        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
        request.addProperty("emp", empId);
        request.addProperty("token", fcm_token);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11); // put all required data into a soap
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(URL);
        httpTransport.debug = true;
        try {
            httpTransport.call(SOAP_ACTION, envelope);
            Log.d("HTTP REQUEST ", httpTransport.requestDump);
            Log.d("HTTP RESPONSE", httpTransport.responseDump);
            Object results = (Object) envelope.getResponse();
            String resultstring = results.toString();
            JSONObject jsonObject = new JSONObject(resultstring);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(LoginActivity.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Utility.OVERLAY_PERMISSION_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    private void getToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                // send it to server
                Log.d("MYTAG", token);
            }
        });
    }

    public class async_login extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showprogressdialog();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            hideprogressdialog();
            if (resultmy == 0) {
                dialog_user_detail();
            } else if (resultmy == 1) {
                Utility.setLoginTrue(LoginActivity.this, Utility.LOGIN_TYPE_NORMAL);
                Toast.makeText(getApplicationContext(), "Mobile number not exist!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else if (resultmy == 2) {
                Toast.makeText(getApplicationContext(), "You are not a valid user!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Some api error occurred!", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            fun_login();
            ///    CheckUserAuthForTimeSheet();
            //  UpdateTokenOnServer();
            return null;
        }

    }

    public class async_loginClient extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            showprogressdialog();
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub
            hideprogressdialog();
            if (resultmy == 1) {
                Toast.makeText(getApplicationContext(), "Success!", Toast.LENGTH_LONG).show();

                if (jsonArray.length() > 1) {
                    //show dialog to select user
                    Dialog_SelectUser();
                } else {
                    try {
                        JSONObject jsonObject = jsonArray.getJSONObject(0);

                        String userID = jsonObject.getString("userID");
                        String txt_Mail = jsonObject.getString("txt_Mail");
                        String CompID = jsonObject.getString("CompID");
                        String CompName = jsonObject.getString("CompName");
                        String UserName = jsonObject.getString("UserName");
                        String UserCategory = jsonObject.getString("UserCategory");
                        String CaType = jsonObject.getString("CaType");
                        String DealerID = jsonObject.getString("DealerID");
                        String dtype = jsonObject.getString("dtype");
                        String Login_Email = jsonObject.getString("Login_Email");
                        String dealer_name = jsonObject.getString("dealer_name");
                        String status = jsonObject.getString("status");
                        String Imagepath = jsonObject.getString("Imagepath");

                        String Masterstatus = jsonObject.getString("Masterstatus");

                        Utility.SaveClientLoginData(LoginActivity.this, userID, txt_Mail, CompID, CompName, UserName, UserCategory,
                                CaType, DealerID, dtype, Login_Email, dealer_name, status, Imagepath, Masterstatus
                        );

                        Utility.setLoginTrue(LoginActivity.this, Utility.LOGIN_TYPE_CLIENT);

                        Intent i = new Intent(LoginActivity.this, ClientHomeActivity.class);
                        startActivity(i);
                        finish();

                    } catch (Exception e) {
                        e.getMessage();
                    }

                }


            } else if (resultmy == 0) {
                Toast.makeText(getApplicationContext(), "Username or Password is wrong!", Toast.LENGTH_LONG).show();

            } else if (resultmy == 2) {
                Toast.makeText(getApplicationContext(), "Blocked!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Some api_error occurred!", Toast.LENGTH_LONG).show();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            fun_login_client();
            return null;
        }

    }
}

