package nogor.pay;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    private DatabaseHelper databaseHelper;
    private RequestQueue requestQueue;

    @Override
    public void onReceive(Context context, Intent intent) {
        databaseHelper = new DatabaseHelper(context);

        if (intent != null && "android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) {
            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            StringBuilder messageBody = new StringBuilder();
            String senderNumber = "";

            for (SmsMessage message : messages) {
                messageBody.append(message.getDisplayMessageBody());
                senderNumber = message.getOriginatingAddress();
            }

            String messageText = messageBody.toString().trim();

            Log.d(TAG, "SMS received from: " + senderNumber + " Message: " + messageText);

            // Filter: Only process SMS from allowed contacts
            if (ContactFilter.isAllowedContact(context, senderNumber)) {
                String contactName = getContactDisplayName(context, senderNumber);
                String displayName = contactName != null ? contactName : senderNumber;

                Log.d(TAG, "SMS from allowed contact: " + displayName);

                // Save to database
                databaseHelper.saveSms(displayName, messageText);

                // Send to server
                sendSmsToServer(context, messageText, displayName);
            } else {
                Log.d(TAG, "SMS from non-allowed contact, ignoring: " + senderNumber);
            }
        }
    }

    private String getContactDisplayName(Context context, String phoneNumber) {
        return ContactFilter.getContactName(context, phoneNumber);
    }

    private void sendSmsToServer(Context context, String messageBody, String senderName) {
        ApiRequest.sendSmsData(context, messageBody, senderName, new ApiRequest.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "SMS sent to server successfully");
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to send SMS to server: " + error);
            }
        });
    }
}