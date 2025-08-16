package nogor.pay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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


            String contactName = getContactDisplayName(context, senderNumber);
            String displayName = contactName != null ? contactName : senderNumber;


            displayName = displayName.toLowerCase().trim();
            senderNumber = senderNumber.toLowerCase().trim();


            Set<String> allowedSenders = new HashSet<>(Arrays.asList(
                "BKash", "Nagad", "NAGAD","IslamiBank", "IBBL", "Celefin"
            ));


            if (allowedSenders.contains(displayName) || allowedSenders.contains(senderNumber)) {
                Log.d(TAG, "SMS from allowed sender: " + displayName);


                databaseHelper.saveSms(displayName, messageText);


                sendSmsToServer(context, messageText, displayName);
            } else {
                Log.d(TAG, "SMS from non-allowed sender, ignoring: " + senderNumber);
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