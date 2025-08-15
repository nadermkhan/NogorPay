package nogor.pay;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ApiRequest {
    private static final String TAG = "ApiRequest";
    private static final String BASE_URL = "https://pay.itnogor.com/api/";
    private static final String LOGIN_ENDPOINT = "device-connect";
    private static final String SMS_ENDPOINT = "add-data";

    public interface ApiCallback {
        void onSuccess(String response);
        void onError(String error);
    }

    public static void loginDevice(Context context, String email, String deviceKey,
                                   String androidId, ApiCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = BASE_URL + LOGIN_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("device_key", deviceKey);
                params.put("device_ip", androidId);
                return params;
            }
        };

        queue.add(request);
    }

    public static void sendSmsData(Context context, String message, String sender, ApiCallback callback) {
        SharedPreferences prefs = context.getSharedPreferences("LoginPrefs", Context.MODE_PRIVATE);
        String email = prefs.getString("user_email", "");
        String deviceKey = prefs.getString("device_key", "");
        String deviceIp = prefs.getString("device_ip", "");

        if (email.isEmpty() || deviceKey.isEmpty()) {
            callback.onError("User not logged in");
            return;
        }

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = BASE_URL + SMS_ENDPOINT;

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "SMS API Response: " + response);
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "SMS API Error: " + error.getMessage());
                        callback.onError(error.getMessage());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("device_key", deviceKey);
                params.put("device_ip", deviceIp);
                params.put("title", sender);
                params.put("body", message);
                return params;
            }
        };

        queue.add(request);
    }
}