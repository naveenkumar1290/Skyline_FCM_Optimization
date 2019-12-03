package planet.info.skyline.RequestControler;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Iterator;

import planet.info.skyline.R;
import planet.info.skyline.network.SOAP_API_Client;

import static planet.info.skyline.network.SOAP_API_Client.KEY_NAMESPACE;

// The types specified here are the input data type, the progress type, and the result type
public class MyAsyncTask_MultiApiCall extends AsyncTask<Void, Void, JSONObject> {

    Context context;
    private ResponseInterface_MultiApiCall responseInterface;

    private JSONArray INPUT_JsonArray;
    private ProgressDialog pDialog;


    public MyAsyncTask_MultiApiCall(Context context, ResponseInterface_MultiApiCall responseInterface, JSONArray INPUT) {
        this.context = context;
        this.responseInterface = responseInterface;
        this.INPUT_JsonArray = INPUT;
    }

    protected void onPreExecute() {
        try {
            pDialog = new ProgressDialog(context);
            pDialog.setMessage(context.getString(R.string.Loading_text));
            pDialog.setCancelable(false);
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected JSONObject doInBackground(Void... strings) {
        return Execute();
    }

 /*   protected void onProgressUpdate(Progress... values) {
        // Executes whenever publishProgress is called from doInBackground
        // Used to update the progress indicator
        progressBar.setProgress(values[0]);
    }*/

    protected void onPostExecute(JSONObject response) {
        try {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.getMessage();
        }
        responseInterface.handleResponse(response);
    }

    public JSONObject Execute() {
        JSONObject jsonObject_Response = new JSONObject();
        for (int i = 0; i < INPUT_JsonArray.length(); i++) {
            try {
                JSONObject jsonObject = INPUT_JsonArray.getJSONObject(i);
                Iterator iterator = jsonObject.keys();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    final String NAMESPACE = KEY_NAMESPACE;
                    final String URL = SOAP_API_Client.BASE_URL;
                    final String METHOD_NAME = key;
                    final String SOAP_ACTION = KEY_NAMESPACE + METHOD_NAME;
                    // Create SOAP request
                    SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);
                    JSONObject inputs = new JSONObject(jsonObject.getString(key));
                    Iterator iterator1 = inputs.keys();

                    while (iterator1.hasNext()) {
                        String key1 = (String) iterator1.next();
                        String val1 = inputs.getString(key1);
                        request.addProperty(key1, val1);
                    }

                    SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                            SoapEnvelope.VER11);
                    envelope.dotNet = true;
                    envelope.setOutputSoapObject(request);
                    HttpTransportSE httpTransport = new HttpTransportSE(URL);

                    httpTransport.call(SOAP_ACTION, envelope);
                    SoapPrimitive SoapPrimitiveresult = (SoapPrimitive) envelope.getResponse();
                    String  receivedString = SoapPrimitiveresult.toString();
                    jsonObject_Response.put(METHOD_NAME, receivedString);
                }


            } catch (Exception e) {
                e.getMessage();
            }


        }

        return jsonObject_Response;
    }


}
