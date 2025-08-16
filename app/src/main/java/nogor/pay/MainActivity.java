package nogor.pay;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;

    private ListView smsListView;
    private TextView statusTextView;
    private DatabaseHelper databaseHelper;
    private ArrayList<HashMap<String, String>> smsList;
    private SmsAdapter smsAdapter;
    private NetworkChangeReceiver networkReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(MainActivity.this, "PreRelease by Nader", Toast.LENGTH_LONG).show();
        initViews();
        setupDatabase();
        checkSmsAndContactsPermissions(); // Updated method name
        startBackgroundService();
        loadSmsData();
        updateNetworkStatus();
    }

    private void initViews() {
        smsListView = findViewById(R.id.smsListView);
        statusTextView = findViewById(R.id.statusTextView);

        smsList = new ArrayList<>();
        smsAdapter = new SmsAdapter(this, smsList);
        smsListView.setAdapter(smsAdapter);
    }

    private void setupDatabase() {
        databaseHelper = new DatabaseHelper(this);
    }

    // Updated method to focus only on SMS and Contacts permissions
    private void checkSmsAndContactsPermissions() {
        String[] permissions = {
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_CONTACTS
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.RECEIVE_SMS,
                    Manifest.permission.READ_SMS,
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        }

        boolean allPermissionsGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionsGranted = false;
                break;
            }
        }

        if (!allPermissionsGranted) {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void loadSmsData() {
        smsList.clear();
        smsList.addAll(databaseHelper.getAllSms());
        smsAdapter.notifyDataSetChanged();
    }

    private void updateNetworkStatus() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        statusTextView.setText(isConnected ? "Online - Monitoring Active" : "Offline - No Internet Connection");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void updateNetworkStatusb(boolean isConnected) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isConnected) {
                    statusTextView.setText("Online - Monitoring Active");
                    statusTextView.setBackgroundColor(getResources().getColor(R.color.status_background));
                } else {
                    statusTextView.setText("Offline - No Internet Connection");
                    statusTextView.setBackgroundColor(getResources().getColor(R.color.delete_button_color));
                }
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, ContactSettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_refresh) {
            loadSmsData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSmsData();
        updateNetworkStatus();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean smsPermissionGranted = false;
            boolean contactsPermissionGranted = false;
            
            // Check which permissions were granted
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if (permissions[i].equals(Manifest.permission.RECEIVE_SMS) || 
                        permissions[i].equals(Manifest.permission.READ_SMS)) {
                        smsPermissionGranted = true;
                    } else if (permissions[i].equals(Manifest.permission.READ_CONTACTS)) {
                        contactsPermissionGranted = true;
                    }
                }
            }

            // Show specific messages based on permissions
            if (!smsPermissionGranted) {
                Toast.makeText(this, "SMS permissions are required for the app to monitor transactions",
                        Toast.LENGTH_LONG).show();
            }
            
            if (!contactsPermissionGranted) {
                Toast.makeText(this, "Contacts permission is required to identify transaction senders",
                        Toast.LENGTH_LONG).show();
            }
            
            // If critical permissions are missing, show general message
            if (!smsPermissionGranted || !contactsPermissionGranted) {
                Toast.makeText(this, "Please grant all requested permissions in Settings for full functionality",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}