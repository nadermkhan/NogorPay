package nogor.pay;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactSettingsActivity extends AppCompatActivity {
    private EditText contactNameEditText, phoneNumberEditText;
    private Button addContactButton, addNumberButton;
    private ListView contactsListView, numbersListView;
    private ContactListAdapter contactsAdapter, numbersAdapter;
    private List<String> contactsList, numbersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_settings);

        initViews();
        loadCurrentSettings();
        setupClickListeners();
    }

    private void initViews() {
        contactNameEditText = findViewById(R.id.contactNameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        addContactButton = findViewById(R.id.addContactButton);
        addNumberButton = findViewById(R.id.addNumberButton);
        contactsListView = findViewById(R.id.contactsListView);
        numbersListView = findViewById(R.id.numbersListView);

        contactsList = new ArrayList<>();
        numbersList = new ArrayList<>();

        contactsAdapter = new ContactListAdapter(this, contactsList, new ContactListAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeContact(position);
            }
        });

        numbersAdapter = new ContactListAdapter(this, numbersList, new ContactListAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(int position) {
                removeNumber(position);
            }
        });

        contactsListView.setAdapter(contactsAdapter);
        numbersListView.setAdapter(numbersAdapter);
    }

    private void loadCurrentSettings() {
        Set<String> contacts = ContactFilter.getAllowedContacts(this);
        Set<String> numbers = ContactFilter.getAllowedNumbers(this);

        contactsList.clear();
        contactsList.addAll(contacts);

        numbersList.clear();
        numbersList.addAll(numbers);

        contactsAdapter.notifyDataSetChanged();
        numbersAdapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        addContactButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addContact();
            }
        });

        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNumber();
            }
        });
    }

    private void addContact() {
        String contactName = contactNameEditText.getText().toString().trim().toLowerCase();

        if (contactName.isEmpty()) {
            Toast.makeText(this, "Please enter a contact name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contactsList.contains(contactName)) {
            Toast.makeText(this, "Contact already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        contactsList.add(contactName);
        contactsAdapter.notifyDataSetChanged();
        contactNameEditText.setText("");

        saveSettings();
        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
    }

    private void addNumber() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();

        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }


        String cleanNumber = phoneNumber.replaceAll("[^+\\d]", "");

        if (numbersList.contains(cleanNumber)) {
            Toast.makeText(this, "Phone number already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        numbersList.add(cleanNumber);
        numbersAdapter.notifyDataSetChanged();
        phoneNumberEditText.setText("");

        saveSettings();
        Toast.makeText(this, "Phone number added successfully", Toast.LENGTH_SHORT).show();
    }

    private void removeContact(int position) {
        if (position >= 0 && position < contactsList.size()) {
            contactsList.remove(position);
            contactsAdapter.notifyDataSetChanged();
            saveSettings();
            Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeNumber(int position) {
        if (position >= 0 && position < numbersList.size()) {
            numbersList.remove(position);
            numbersAdapter.notifyDataSetChanged();
            saveSettings();
            Toast.makeText(this, "Phone number removed", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveSettings() {
        Set<String> contactsSet = new HashSet<>(contactsList);
        Set<String> numbersSet = new HashSet<>(numbersList);

        ContactFilter.saveAllowedContacts(this, contactsSet);
        ContactFilter.saveAllowedNumbers(this, numbersSet);
    }
}