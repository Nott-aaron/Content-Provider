package com.example.contentprovider;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements ContactAdapter.OnContactClickListener {

    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private RecyclerView rvContacts;
    private ContactAdapter contactAdapter;
    private EditText etSearchContact;
    private ImageView btnClearSearch;
    private TextView tvContactCount;

    private LinearLayout cardSelectedDetail;
    private TextView tvDetailAvatarInitial, tvDetailName, tvDetailPhone;
    private ImageView btnDeselectContact;
    private LinearLayout btnShareContact;

    private LinearLayout layoutEmptyState;
    private TextView tvEmptyMessage;
    private LinearLayout layoutPermissionDenied;
    private Button btnGrantPermission;

    private Contact selectedContact = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupRecyclerView();
        setupSearchListener();

        btnGrantPermission.setOnClickListener(v -> requestContactsPermission());
        btnDeselectContact.setOnClickListener(v -> clearSelectedContact());
        btnShareContact.setOnClickListener(v -> shareSelectedContact());
        btnClearSearch.setOnClickListener(v -> etSearchContact.setText(""));

        checkAndLoadContacts();
    }

    private void initViews() {
        rvContacts = findViewById(R.id.rvContacts);
        etSearchContact = findViewById(R.id.etSearchContact);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        tvContactCount = findViewById(R.id.tvContactCount);

        cardSelectedDetail = findViewById(R.id.cardSelectedDetail);
        tvDetailAvatarInitial = findViewById(R.id.tvDetailAvatarInitial);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailPhone = findViewById(R.id.tvDetailPhone);
        btnDeselectContact = findViewById(R.id.btnDeselectContact);
        btnShareContact = findViewById(R.id.btnShareContact);

        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        layoutPermissionDenied = findViewById(R.id.layoutPermissionDenied);
        btnGrantPermission = findViewById(R.id.btnGrantPermission);
    }

    private void setupRecyclerView() {
        contactAdapter = new ContactAdapter(this);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        rvContacts.setAdapter(contactAdapter);
    }

    private void setupSearchListener() {
        etSearchContact.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                btnClearSearch.setVisibility(!query.isEmpty() ? View.VISIBLE : View.GONE);
                contactAdapter.filter(query);
                updateUIState();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void checkAndLoadContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            layoutPermissionDenied.setVisibility(View.GONE);
            loadContactsFromProvider();
        } else {
            requestContactsPermission();
        }
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                PERMISSION_REQUEST_READ_CONTACTS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                layoutPermissionDenied.setVisibility(View.GONE);
                Toast.makeText(this, "Permission granted!", Toast.LENGTH_SHORT).show();
                loadContactsFromProvider();
            } else {
                layoutPermissionDenied.setVisibility(View.VISIBLE);
                rvContacts.setVisibility(View.GONE);
                layoutEmptyState.setVisibility(View.GONE);
                tvContactCount.setText("0 Contacts");
                Toast.makeText(this, "Permission denied to read contacts.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadContactsFromProvider() {
        List<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();

        // Step 1: Map contact IDs to phone numbers using ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        Map<String, String> phoneNumbersMap = new HashMap<>();
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] phoneProjection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        try (Cursor phoneCursor = contentResolver.query(phoneUri, phoneProjection, null, null, null)) {
            if (phoneCursor != null) {
                int idIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
                int numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                while (phoneCursor.moveToNext()) {
                    if (idIndex != -1 && numberIndex != -1) {
                        String contactId = phoneCursor.getString(idIndex);
                        String phoneNumber = phoneCursor.getString(numberIndex);
                        if (contactId != null && phoneNumber != null && !phoneNumbersMap.containsKey(contactId)) {
                            phoneNumbersMap.put(contactId, phoneNumber);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Step 2: Retrieve all contacts using ContactsContract.Contacts.CONTENT_URI
        Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;
        String[] contactsProjection = new String[]{
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        };
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC";

        try (Cursor cursor = contentResolver.query(contactsUri, contactsProjection, null, null, sortOrder)) {
            if (cursor != null) {
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY);

                while (cursor.moveToNext()) {
                    String contactId = idIndex != -1 ? cursor.getString(idIndex) : null;
                    String name = nameIndex != -1 ? cursor.getString(nameIndex) : "Unknown";

                    if (name != null && !name.trim().isEmpty()) {
                        String phone = phoneNumbersMap.getOrDefault(contactId, "No phone number available");
                        contacts.add(new Contact(contactId, name, phone));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        contactAdapter.setContacts(contacts);
        updateUIState();
    }

    private void updateUIState() {
        int count = contactAdapter.getFilteredCount();
        tvContactCount.setText(count + (count == 1 ? " Contact" : " Contacts"));

        if (count == 0) {
            rvContacts.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(View.VISIBLE);
            if (!etSearchContact.getText().toString().trim().isEmpty()) {
                tvEmptyMessage.setText("No matching contacts found");
            } else {
                tvEmptyMessage.setText("No contacts available");
            }
        } else {
            rvContacts.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }
    }

    @Override
    public void onContactClick(Contact contact) {
        selectedContact = contact;
        tvDetailAvatarInitial.setText(contact.getInitial());
        tvDetailName.setText(contact.getName());
        tvDetailPhone.setText(contact.getPhoneNumber());
        cardSelectedDetail.setVisibility(View.VISIBLE);
    }

    private void clearSelectedContact() {
        selectedContact = null;
        cardSelectedDetail.setVisibility(View.GONE);
    }

    private void shareSelectedContact() {
        if (selectedContact == null) {
            Toast.makeText(this, "Please select a contact first.", Toast.LENGTH_SHORT).show();
            return;
        }

        String shareText = "Contact Details:\n" +
                "Name: " + selectedContact.getName() + "\n" +
                "Phone: " + selectedContact.getPhoneNumber();

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Share Contact Info Via");
        startActivity(shareIntent);
    }
}