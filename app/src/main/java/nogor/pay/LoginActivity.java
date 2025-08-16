package nogor.pay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity; // Keep this for newer devices
import android.app.Activity; // Alternative for older devices
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity { 
    private EditText emailEditText, deviceKeyEditText;
    private Button loginButton;
    private CustomDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        deviceKeyEditText = findViewById(R.id.deviceKeyEditText);
        loginButton = findViewById(R.id.loginButton);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String deviceKey = deviceKeyEditText.getText().toString().trim();

        if (email.isEmpty()) {
            emailEditText.setError("Email is required");
            return;
        }

        if (deviceKey.isEmpty()) {
            deviceKeyEditText.setError("Device key is required");
            return;
        }

        progressDialog = new CustomDialog(this);
        progressDialog.showDialog();

        String androidId = getAndroidId();

        ApiRequest.loginDevice(this, email, deviceKey, androidId, new ApiRequest.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                if (progressDialog != null) {
                    progressDialog.hideDialog();
                }
                handleLoginResponse(response, email, deviceKey, androidId);
            }

            @Override
            public void onError(String error) {
                if (progressDialog != null) {
                    progressDialog.hideDialog();
                }
                Toast.makeText(LoginActivity.this, "Login failed: " + (error != null ? error : "Unknown error"), 
                             Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleLoginResponse(String response, String email, String deviceKey, String androidId) {
        try {
            if (response == null || response.isEmpty()) {
                Toast.makeText(this, "Empty response from server", Toast.LENGTH_SHORT).show();
                return;
            }
            
            JSONObject jsonResponse = new JSONObject(response);
            int status = jsonResponse.getInt("status");

            if (status == 1) {
                saveLoginInfo(email, deviceKey, androidId);
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                String message = jsonResponse.optString("message", "Login failed. Please check your credentials.");
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "Error parsing response: " + response, Toast.LENGTH_LONG).show();
        }
    }

    private void saveLoginInfo(String email, String deviceKey, String androidId) {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_email", email);
        editor.putString("device_key", deviceKey);
        editor.putString("device_ip", androidId);
        editor.apply(); //  apply() instead of commit()
    }
    private String getAndroidId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}