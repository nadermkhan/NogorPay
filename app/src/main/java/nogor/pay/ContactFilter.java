package nogor.pay;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import java.util.HashSet;
import java.util.Set;

public class ContactFilter {
    private static final String PREFS_NAME = "ContactSettings";
    private static final String KEY_ALLOWED_CONTACTS = "allowed_contacts";

    // Default allowed contacts
    private static final Set<String> DEFAULT_CONTACTS = new HashSet<String>() {{
        add("mom");
        add("dad");
        add("john");
    }};

    // Default phone numbers (add your actual numbers here)
    private static final Set<String> DEFAULT_NUMBERS = new HashSet<String>() {{
        add("+1234567890"); // Mom's number
        add("+0987654321"); // Dad's number
        add("+1122334455"); // John's number
    }};

    public static boolean isAllowedContact(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return false;

        // Clean phone number
        String cleanNumber = cleanPhoneNumber(phoneNumber);

        // Get saved allowed contacts
        Set<String> allowedContacts = getAllowedContacts(context);
        Set<String> allowedNumbers = getAllowedNumbers(context);

        // Check if phone number is in allowed list
        if (allowedNumbers.contains(cleanNumber)) {
            return true;
        }

        // Get contact name from phone number
        String contactName = getContactName(context, phoneNumber);
        if (contactName != null) {
            String cleanName = contactName.toLowerCase().trim();
            return allowedContacts.contains(cleanName);
        }

        return false;
    }

    private static String cleanPhoneNumber(String phoneNumber) {
        return phoneNumber.replaceAll("[^+\\d]", "");
    }

    // Add this method to ContactFilter.java class
    public static String getContactName(Context context, String phoneNumber) {
        String contactName = null;
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(phoneNumber));

        String[] projection = {ContactsContract.PhoneLookup.DISPLAY_NAME};

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                contactName = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactName;
    }
    public static Set<String> getAllowedContacts(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> contacts = prefs.getStringSet(KEY_ALLOWED_CONTACTS, null);
        return contacts != null ? contacts : new HashSet<>(DEFAULT_CONTACTS);
    }

    public static Set<String> getAllowedNumbers(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> numbers = prefs.getStringSet("allowed_numbers", null);
        return numbers != null ? numbers : new HashSet<>(DEFAULT_NUMBERS);
    }

    public static void saveAllowedContacts(Context context, Set<String> contacts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet(KEY_ALLOWED_CONTACTS, contacts).apply();
    }

    public static void saveAllowedNumbers(Context context, Set<String> numbers) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putStringSet("allowed_numbers", numbers).apply();
    }
}