package nogor.pay;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
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
    private boolean isEditingContact = false;
    private boolean isEditingNumber = false;
    private int editingContactPosition = -1;
    private int editingNumberPosition = -1;

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

        contactsAdapter = new ContactListAdapter(this, contactsList, new ContactListAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                editContact(position);
            }

            @Override
            public void onDeleteClick(int position) {
                removeContact(position);
            }
        });

        numbersAdapter = new ContactListAdapter(this, numbersList, new ContactListAdapter.OnItemClickListener() {
            @Override
            public void onEditClick(int position) {
                editNumber(position);
            }

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
                if (isEditingContact) {
                    updateContact();
                } else {
                    addContact();
                }
            }
        });

        addNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditingNumber) {
                    updateNumber();
                } else {
                    addNumber();
                }
            }
        });
    }

    private void addContact() {
        String contactName = contactNameEditText.getText().toString().trim();

        if (contactName.isEmpty()) {
            Toast.makeText(this, "Please enter a contact name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert to lowercase for consistency
        contactName = contactName.toLowerCase();

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

    private void editContact(int position) {
        if (position >= 0 && position < contactsList.size()) {
            String contactName = contactsList.get(position);
            contactNameEditText.setText(contactName);
            isEditingContact = true;
            editingContactPosition = position;
            addContactButton.setText("Update");
        }
    }

    private void editNumber(int position) {
        if (position >= 0 && position < numbersList.size()) {
            String phoneNumber = numbersList.get(position);
            phoneNumberEditText.setText(phoneNumber);
            isEditingNumber = true;
            editingNumberPosition = position;
            addNumberButton.setText("Update");
        }
    }

    private void updateContact() {
        String contactName = contactNameEditText.getText().toString().trim();
        
        if (contactName.isEmpty()) {
            Toast.makeText(this, "Please enter a contact name", Toast.LENGTH_SHORT).show();
            return;
        }

        contactName = contactName.toLowerCase();

        // Check if another contact already has this name
        if (contactsList.contains(contactName) && !contactsList.get(editingContactPosition).equals(contactName)) {
            Toast.makeText(this, "Contact name already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        contactsList.set(editingContactPosition, contactName);
        contactsAdapter.notifyDataSetChanged();
        resetContactForm();
        saveSettings();
        Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void updateNumber() {
        String phoneNumber = phoneNumberEditText.getText().toString().trim();
        
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        String cleanNumber = phoneNumber.replaceAll("[^+\\d]", "");

        // Check if another number already has this value
        if (numbersList.contains(cleanNumber) && !numbersList.get(editingNumberPosition).equals(cleanNumber)) {
            Toast.makeText(this, "Phone number already exists", Toast.LENGTH_SHORT).show();
            return;
        }

        numbersList.set(editingNumberPosition, cleanNumber);
        numbersAdapter.notifyDataSetChanged();
        resetNumberForm();
        saveSettings();
        Toast.makeText(this, "Phone number updated successfully", Toast.LENGTH_SHORT).show();
    }

    private void removeContact(int position) {
        if (position >= 0 && position < contactsList.size()) {
            showDeleteConfirmation("contact", () -> {
                contactsList.remove(position);
                contactsAdapter.notifyDataSetChanged();
                saveSettings();
                Toast.makeText(this, "Contact removed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void removeNumber(int position) {
        if (position >= 0 && position < numbersList.size()) {
            showDeleteConfirmation("phone number", () -> {
                numbersList.remove(position);
                numbersAdapter.notifyDataSetChanged();
                saveSettings();
                Toast.makeText(this, "Phone number removed", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void showDeleteConfirmation(String itemType, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle("Delete " + itemType)
                .setMessage("Are you sure you want to delete this " + itemType + "?")
                .setPositiveButton("Delete", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void saveSettings() {
        Set<String> contactsSet = new HashSet<>(contactsList);
        Set<String> numbersSet = new HashSet<>(numbersList);

        ContactFilter.saveAllowedContacts(this, contactsSet);
        ContactFilter.saveAllowedNumbers(this, numbersSet);
    }

    private void resetContactForm() {
        contactNameEditText.setText("");
        isEditingContact = false;
        editingContactPosition = -1;
        addContactButton.setText("Add");
    }

    private void resetNumberForm() {
        phoneNumberEditText.setText("");
        isEditingNumber = false;
        editingNumberPosition = -1;
        addNumberButton.setText("Add");
    }

    @Override
    public void onBackPressed() {
        // Reset forms if user is editing
        if (isEditingContact || isEditingNumber) {
            resetContactForm();
            resetNumberForm();
        } else {
            super.onBackPressed();
        }
    }
}