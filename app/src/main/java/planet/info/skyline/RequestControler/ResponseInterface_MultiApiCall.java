package planet.info.skyline.RequestControler;

import org.json.JSONObject;

/**
 * Created by admin on 04-Oct-17.
 */

public interface ResponseInterface_MultiApiCall {
        void handleResponse(JSONObject responseJsonObject);// throws JSONException;
       // void handleErrorMessage(String response, String api);
}
