# 📱 Contacts Content Provider — Android Data Sharing Application

![Android](https://img.shields.io/badge/Android-Java-green?style=flat&logo=android)
![Content Provider](https://img.shields.io/badge/Android-Contacts%20Content%20Provider-blue?style=flat)
![UI](https://img.shields.io/badge/Theme-Green%20%26%20White-brightgreen?style=flat)

> **Mobile Application Development (MAD) Lab — Experiment 10**  
> An Android application demonstrating contact retrieval, searching, viewing, and data sharing using the **Contacts Content Provider**, `ContactsContract`, `ContentResolver`, and implicit Intents. Developed using **Java and XML**.

---
## Student Details

- Name: Spencer Aaron Fernandes

- USN: 25MCAR0123

- Course: MCA – General

- College: Jain Deemed-to-be University
---

## 📱 Application Overview & Features

### 1. 📇 Contacts Content Provider
- **Retrieve Contacts:** Fetches contact names and phone numbers from the device's contacts database using `ContactsContract`.
- **ContentResolver:** Queries the Android Contacts Provider to access contact information.
- **Runtime Permission:** Requests `READ_CONTACTS` permission before accessing contacts.
- **Alphabetical Sorting:** Displays contacts alphabetically by name.
- **Contact Count:** Displays the number of contacts retrieved from the device.
- **Missing Phone Numbers:** Handles contacts without an available phone number cleanly.

### 2. 🔍 Search and Contact Details
- **Search Contacts:** Allows users to search for contacts by name in real-time.
- **Dynamic Filtering:** Updates the contact list as the user types.
- **Clear Search:** Clears the search query and restores the complete contact list.
- **Contact Selection:** Displays the selected contact's details in a separate card at the top.
- **Contact Information:** Shows the contact's name and available phone number.
- **Empty Search State:** Displays a user-friendly message when no matching contacts are found.

### 3. 📤 Share Contact Information
- **Share Contact:** Allows users to share the selected contact's name and phone number.
- **Implicit Intent:** Uses `Intent.ACTION_SEND` to share contact information with compatible applications.
- **Android Sharesheet:** Uses `Intent.createChooser()` to display available sharing applications.
- **Multiple Sharing Options:** Allows users to select any installed application (Messaging, Email, WhatsApp, Drive, etc.).

### 4. 🎨 User Interface and Design
- **Color Scheme:** Modern Green-and-White theme with emerald accents.
- **Card-Based Layout:** Displays contact information in clean, organized cards.
- **Search Interface:** Provides a search field with an instant clear button.
- **Permission Guidance:** Displays guidance when contact access is denied with a **Grant Access** button.
- **Empty States:** Shows appropriate messages when contacts or search results are unavailable.

---

## 📂 Project Directory Structure

```text
ContactsContentProvider/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/contentprovider/
│   │   │   │   ├── Contact.java              # Data model
│   │   │   │   ├── ContactAdapter.java       # RecyclerView Adapter with filtering
│   │   │   │   └── MainActivity.java         # Main logic, ContentResolver & Intents
│   │   │   ├── res/
│   │   │   │   ├── drawable/                 # Shapes and green vector icons
│   │   │   │   │   ├── bg_avatar.xml
│   │   │   │   │   ├── bg_avatar_light.xml
│   │   │   │   │   ├── bg_button_secondary.xml
│   │   │   │   │   ├── bg_button_share.xml
│   │   │   │   │   ├── bg_contact_card.xml
│   │   │   │   │   ├── bg_detail_card.xml
│   │   │   │   │   ├── bg_header.xml
│   │   │   │   │   ├── bg_search_bar.xml
│   │   │   │   │   ├── ic_chevron_right.xml
│   │   │   │   │   ├── ic_clear.xml
│   │   │   │   │   ├── ic_contacts_header.xml
│   │   │   │   │   ├── ic_person.xml
│   │   │   │   │   ├── ic_phone.xml
│   │   │   │   │   ├── ic_search.xml
│   │   │   │   │   ├── ic_share.xml
│   │   │   │   │   └── ic_shield.xml
│   │   │   │   ├── layout/
│   │   │   │   │   ├── activity_main.xml     # Main layout
│   │   │   │   │   └── item_contact.xml      # Row layout for contact card
│   │   │   │   └── values/
│   │   │   │       ├── colors.xml            # Green and White color palette
│   │   │   │       ├── strings.xml           # App strings
│   │   │   │       └── themes.xml            # Material 3 theme
│   │   │   └── AndroidManifest.xml           # Permissions and activity registration
│   │   └── build.gradle.kts
│   └── build.gradle.kts
└── README.md
```

---

## 💻 Complete Source Code

### 1. `MainActivity.java`
Location: `app/src/main/java/com/example/contentprovider/MainActivity.java`

```java
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
```

---

### 2. `ContactAdapter.java`
Location: `app/src/main/java/com/example/contentprovider/ContactAdapter.java`

```java
package com.example.contentprovider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private List<Contact> contactList = new ArrayList<>();
    private List<Contact> fullContactList = new ArrayList<>();
    private final OnContactClickListener listener;

    public ContactAdapter(OnContactClickListener listener) {
        this.listener = listener;
    }

    public void setContacts(List<Contact> contacts) {
        this.fullContactList = new ArrayList<>(contacts);
        this.contactList = new ArrayList<>(contacts);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        contactList.clear();
        if (query == null || query.trim().isEmpty()) {
            contactList.addAll(fullContactList);
        } else {
            String filterPattern = query.toLowerCase().trim();
            for (Contact contact : fullContactList) {
                if (contact.getName() != null && contact.getName().toLowerCase().contains(filterPattern)) {
                    contactList.add(contact);
                }
            }
        }
        notifyDataSetChanged();
    }

    public int getFilteredCount() {
        return contactList.size();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.bind(contact, listener);
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatarInitial, tvContactName, tvContactPhone;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatarInitial = itemView.findViewById(R.id.tvAvatarInitial);
            tvContactName = itemView.findViewById(R.id.tvContactName);
            tvContactPhone = itemView.findViewById(R.id.tvContactPhone);
        }

        public void bind(final Contact contact, final OnContactClickListener listener) {
            tvAvatarInitial.setText(contact.getInitial());
            tvContactName.setText(contact.getName());
            tvContactPhone.setText(contact.getPhoneNumber());

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onContactClick(contact);
                }
            });
        }
    }
}
```

---

### 3. `Contact.java`
Location: `app/src/main/java/com/example/contentprovider/Contact.java`

```java
package com.example.contentprovider;

public class Contact {
    private final String id;
    private final String name;
    private final String phoneNumber;

    public Contact(String id, String name, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getInitial() {
        if (name != null && !name.trim().isEmpty()) {
            return String.valueOf(name.trim().charAt(0)).toUpperCase();
        }
        return "?";
    }
}
```

---

### 4. `AndroidManifest.xml`
Location: `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Runtime permission required to access Contacts Content Provider -->
    <uses-permission android:name="android.permission.READ_CONTACTS" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.ContentProvider">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

### 5. `activity_main.xml` Layout
Location: `app/src/main/res/layout/activity_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/main"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/background_light"
    android:orientation="vertical"
    tools:context=".MainActivity">

    <!-- Stylish Green Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:background="@drawable/bg_header"
        android:elevation="4dp"
        android:orientation="horizontal"
        android:paddingStart="20dp"
        android:paddingTop="24dp"
        android:paddingEnd="20dp"
        android:paddingBottom="20dp">

        <ImageView
            android:layout_width="40dp"
            android:layout_height="40dp"
            android:layout_gravity="center_vertical"
            android:contentDescription="Header Icon"
            android:src="@drawable/ic_contacts_header" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="14dp"
            android:orientation="vertical">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/header_title"
                android:textColor="@color/white"
                android:textSize="22sp"
                android:textStyle="bold" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="2dp"
                android:text="@string/header_subtitle"
                android:textColor="@color/green_border"
                android:textSize="12sp" />
        </LinearLayout>
    </LinearLayout>

    <!-- Main Content Container with Padding -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        android:padding="16dp">

        <!-- Selected Contact Detail Card (Visible when a contact is selected) -->
        <LinearLayout
            android:id="@+id/cardSelectedDetail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="16dp"
            android:background="@drawable/bg_detail_card"
            android:elevation="2dp"
            android:orientation="vertical"
            android:padding="16dp"
            android:visibility="gone"
            tools:visibility="visible">

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <TextView
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:letterSpacing="0.08"
                    android:text="SELECTED CONTACT"
                    android:textColor="@color/green_border"
                    android:textSize="11sp"
                    android:textStyle="bold" />

                <ImageView
                    android:id="@+id/btnDeselectContact"
                    android:layout_width="24dp"
                    android:layout_height="24dp"
                    android:background="?android:attr/selectableItemBackgroundBorderless"
                    android:clickable="true"
                    android:contentDescription="Close Detail"
                    android:focusable="true"
                    android:src="@drawable/ic_clear"
                    app:tint="@color/green_border" />
            </LinearLayout>

            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="12dp"
                android:gravity="center_vertical"
                android:orientation="horizontal">

                <FrameLayout
                    android:layout_width="48dp"
                    android:layout_height="48dp"
                    android:background="@drawable/bg_button_share">

                    <TextView
                        android:id="@+id/tvDetailAvatarInitial"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_gravity="center"
                        android:text="C"
                        android:textColor="@color/green_primary_dark"
                        android:textSize="20sp"
                        android:textStyle="bold" />
                </FrameLayout>

                <LinearLayout
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:layout_marginStart="14dp"
                    android:orientation="vertical">

                    <TextView
                        android:id="@+id/tvDetailName"
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:ellipsize="end"
                        android:maxLines="1"
                        android:text="Selected Contact Name"
                        android:textColor="@color/white"
                        android:textSize="18sp"
                        android:textStyle="bold" />

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:layout_marginTop="4dp"
                        android:gravity="center_vertical"
                        android:orientation="horizontal">

                        <ImageView
                            android:layout_width="14dp"
                            android:layout_height="14dp"
                            android:contentDescription="Phone"
                            android:src="@drawable/ic_phone"
                            app:tint="@color/green_border" />

                        <TextView
                            android:id="@+id/tvDetailPhone"
                            android:layout_width="match_parent"
                            android:layout_height="wrap_content"
                            android:layout_marginStart="6dp"
                            android:ellipsize="end"
                            android:maxLines="1"
                            android:text="+1 987 654 3210"
                            android:textColor="@color/green_border"
                            android:textSize="14sp" />
                    </LinearLayout>
                </LinearLayout>
            </LinearLayout>

            <!-- Action Buttons Row -->
            <LinearLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:orientation="horizontal">

                <LinearLayout
                    android:id="@+id/btnShareContact"
                    android:layout_width="0dp"
                    android:layout_height="44dp"
                    android:layout_weight="1"
                    android:background="@drawable/bg_button_share"
                    android:clickable="true"
                    android:focusable="true"
                    android:foreground="?android:attr/selectableItemBackground"
                    android:gravity="center"
                    android:orientation="horizontal"
                    android:paddingStart="12dp"
                    android:paddingEnd="12dp">

                    <ImageView
                        android:layout_width="18dp"
                        android:layout_height="18dp"
                        android:contentDescription="Share"
                        android:src="@drawable/ic_share"
                        app:tint="@color/green_primary_dark" />

                    <TextView
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:layout_marginStart="8dp"
                        android:text="Share Contact"
                        android:textColor="@color/green_primary_dark"
                        android:textSize="14sp"
                        android:textStyle="bold" />
                </LinearLayout>
            </LinearLayout>
        </LinearLayout>

        <!-- Search Bar -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="48dp"
            android:layout_marginBottom="12dp"
            android:background="@drawable/bg_search_bar"
            android:gravity="center_vertical"
            android:orientation="horizontal"
            android:paddingStart="12dp"
            android:paddingEnd="12dp">

            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:contentDescription="Search Icon"
                android:src="@drawable/ic_search" />

            <EditText
                android:id="@+id/etSearchContact"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:layout_marginStart="10dp"
                android:layout_marginEnd="8dp"
                android:layout_weight="1"
                android:background="@null"
                android:hint="@string/search_hint"
                android:imeOptions="actionSearch"
                android:inputType="textPersonName"
                android:maxLines="1"
                android:textColor="@color/green_text_dark"
                android:textColorHint="@color/green_text_medium"
                android:textSize="14sp" />

            <ImageView
                android:id="@+id/btnClearSearch"
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:background="?android:attr/selectableItemBackgroundBorderless"
                android:clickable="true"
                android:contentDescription="Clear Search"
                android:focusable="true"
                android:src="@drawable/ic_clear"
                android:visibility="gone"
                tools:visibility="visible" />
        </LinearLayout>

        <!-- Section Title & Count Badge -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginBottom="8dp"
            android:gravity="center_vertical"
            android:orientation="horizontal">

            <TextView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:text="ALL CONTACTS"
                android:textColor="@color/green_text_medium"
                android:textSize="12sp"
                android:textStyle="bold" />

            <TextView
                android:id="@+id/tvContactCount"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:background="@drawable/bg_avatar_light"
                android:paddingStart="10dp"
                android:paddingTop="3dp"
                android:paddingEnd="10dp"
                android:paddingBottom="3dp"
                android:text="0 Contacts"
                android:textColor="@color/green_primary_dark"
                android:textSize="11sp"
                android:textStyle="bold" />
        </LinearLayout>

        <!-- Contacts RecyclerView -->
        <androidx.recyclerview.widget.RecyclerView
            android:id="@+id/rvContacts"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:clipToPadding="false"
            android:paddingBottom="8dp"
            tools:listitem="@layout/item_contact" />

        <!-- Empty State View -->
        <LinearLayout
            android:id="@+id/layoutEmptyState"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="24dp"
            android:visibility="gone"
            tools:visibility="gone">

            <ImageView
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:contentDescription="No Contacts"
                android:src="@drawable/ic_person" />

            <TextView
                android:id="@+id/tvEmptyMessage"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:gravity="center"
                android:text="No contacts found"
                android:textColor="@color/green_text_dark"
                android:textSize="16sp"
                android:textStyle="bold" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="6dp"
                android:gravity="center"
                android:text="Try searching with a different name or add contacts on your device."
                android:textColor="@color/green_text_medium"
                android:textSize="13sp" />
        </LinearLayout>

        <!-- Permission Denied View -->
        <LinearLayout
            android:id="@+id/layoutPermissionDenied"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="center"
            android:orientation="vertical"
            android:padding="24dp"
            android:visibility="gone"
            tools:visibility="gone">

            <ImageView
                android:layout_width="64dp"
                android:layout_height="64dp"
                android:contentDescription="Permission Required"
                android:src="@drawable/ic_shield" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="16dp"
                android:gravity="center"
                android:text="Contact Permission Required"
                android:textColor="@color/green_text_dark"
                android:textSize="18sp"
                android:textStyle="bold" />

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="8dp"
                android:gravity="center"
                android:text="This application uses Android ContentResolver to query ContactsContract. Please grant contact access permission to view and share contacts."
                android:textColor="@color/green_text_medium"
                android:textSize="13sp" />

            <Button
                android:id="@+id/btnGrantPermission"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginTop="20dp"
                android:backgroundTint="@color/green_primary"
                android:paddingStart="24dp"
                android:paddingEnd="24dp"
                android:text="Grant Access"
                android:textColor="@color/white" />
        </LinearLayout>

    </LinearLayout>

</LinearLayout>
```

---

### 6. `item_contact.xml` Row Layout
Location: `app/src/main/res/layout/item_contact.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginBottom="8dp"
    android:background="@drawable/bg_contact_card"
    android:clickable="true"
    android:focusable="true"
    android:foreground="?android:attr/selectableItemBackground"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="12dp">

    <!-- Avatar Circle -->
    <FrameLayout
        android:layout_width="44dp"
        android:layout_height="44dp"
        android:background="@drawable/bg_avatar">

        <TextView
            android:id="@+id/tvAvatarInitial"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center"
            android:textColor="@color/white"
            android:textSize="18sp"
            android:textStyle="bold"
            android:text="C" />
    </FrameLayout>

    <!-- Name and Phone Info -->
    <LinearLayout
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="14dp"
        android:layout_weight="1"
        android:orientation="vertical">

        <TextView
            android:id="@+id/tvContactName"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:ellipsize="end"
            android:maxLines="1"
            android:textColor="@color/green_text_dark"
            android:textSize="16sp"
            android:textStyle="bold"
            android:text="Contact Name" />

        <TextView
            android:id="@+id/tvContactPhone"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="2dp"
            android:ellipsize="end"
            android:maxLines="1"
            android:textColor="@color/green_text_medium"
            android:textSize="13sp"
            android:text="+1 234 567 8900" />
    </LinearLayout>

    <ImageView
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:contentDescription="Select"
        android:src="@drawable/ic_chevron_right" />

</LinearLayout>
```

---

### 7. Resource XML Files

#### `colors.xml`
Location: `app/src/main/res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Green and White Color Palette -->
    <color name="white">#FFFFFFFF</color>
    <color name="green_primary">#FF2E7D32</color>
    <color name="green_primary_dark">#FF1B5E20</color>
    <color name="green_primary_light">#FFE8F5E9</color>
    <color name="green_accent">#FF4CAF50</color>
    <color name="green_border">#FFC8E6C9</color>
    <color name="green_text_dark">#FF0D3311</color>
    <color name="green_text_medium">#FF38663D</color>
    <color name="green_text_muted">#FF558B2F</color>
    <color name="green_subtle_bg">#FFF4FBF5</color>
    <color name="background_light">#FFF7FAF7</color>
</resources>
```

#### `strings.xml`
Location: `app/src/main/res/values/strings.xml`

```xml
<resources>
    <string name="app_name">ContactsContentProvider</string>
    <string name="header_title">Contacts Manager</string>
    <string name="header_subtitle">Experiment 10 &#8226; Contacts Content Provider</string>
    <string name="search_hint">Search contacts by name&#8230;</string>
</resources>
```

#### `themes.xml`
Location: `app/src/main/res/values/themes.xml`

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <style name="Base.Theme.ContentProvider" parent="Theme.Material3.DayNight.NoActionBar">
        <item name="colorPrimary">@color/green_primary</item>
        <item name="colorOnPrimary">@color/white</item>
        <item name="colorSecondary">@color/green_primary_dark</item>
        <item name="colorOnSecondary">@color/white</item>
        <item name="android:statusBarColor">@color/green_primary_dark</item>
        <item name="android:windowLightStatusBar">false</item>
        <item name="android:windowBackground">@color/background_light</item>
    </style>

    <style name="Theme.ContentProvider" parent="Base.Theme.ContentProvider" />
</resources>
```

---

## 🛠️ How Content Provider Integration Works

1. **`ContentResolver` Query Interface:** The application accesses device contacts via `getContentResolver().query()`.
2. **`ContactsContract` URIs Used:**
   - `ContactsContract.Contacts.CONTENT_URI`: Main contact registry.
   - `ContactsContract.CommonDataKinds.Phone.CONTENT_URI`: Contact phone numbers data table.
3. **Optimized Two-Pass Querying:**
   - **Pass 1:** Builds a lookup hash map of `CONTACT_ID -> PHONE_NUMBER` from `Phone.CONTENT_URI`.
   - **Pass 2:** Queries `Contacts.CONTENT_URI` sorted alphabetically by `DISPLAY_NAME_PRIMARY ASC`. If a contact has no phone entry in Pass 1, `"No phone number available"` is cleanly assigned.
4. **Automatic Resource Cleanup:** Database cursors are wrapped in Java 7+ `try (Cursor cursor = ...)` blocks to guarantee proper closure.
5. **Data Sharing via Implicit Intent:** Selected contact information triggers `Intent.ACTION_SEND` and `Intent.createChooser()` to invoke the Android Sharesheet.

---

## 🚀 Building and Running

1. Open **Android Studio**.
2. Select **Open** and choose the `ContactsContentProvider` project directory.
3. Allow Gradle synchronization to complete.
4. Connect an Android phone or launch an Android Virtual Device (AVD) emulator (API 24+).
5. Click **Run > Run 'app'** (`Shift + F10`).
6. Grant contact permission when prompted.
7. Test contact retrieval, searching, viewing details, and sharing.

---

## 🧪 Testing and Verification Guide

| Test Case | Scenario | Execution Step | Expected Result | Status |
| :---: | :--- | :--- | :--- | :---: |
| **TC-01** | **Permission Request** | Launch app for first time | System permission dialog pops up asking for `READ_CONTACTS` access. | Passed |
| **TC-02** | **Permission Denial** | Tap "Don't allow" on dialog | App displays green "Contact Permission Required" screen with "Grant Access" button. | Passed |
| **TC-03** | **Contact Retrieval** | Grant permission on prompt | Contact list populates sorted alphabetically by display name. Total count badge updates. | Passed |
| **TC-04** | **Missing Phone Number** | View contact without phone | Contact displays "No phone number available" cleanly under name without crashing. | Passed |
| **TC-05** | **Search Filter** | Type "Alice" in search bar | RecyclerView filters dynamically to show contacts containing "Alice". Clear (`X`) icon appears. | Passed |
| **TC-06** | **Clear Search** | Tap clear (`X`) icon | Search text clears, full contact list restores, clear icon hides. | Passed |
| **TC-07** | **Select Contact** | Tap on any contact row | Selected Contact card appears at top showing initial avatar, name, phone, and Share button. | Passed |
| **TC-08** | **Share Contact** | Tap "Share Contact" button | Android Sharesheet chooser appears showing messaging/email apps to share contact details. | Passed |

---

## 🔬 Lab Record Details

- **Experiment Number:** 10
- **Title:** Implement Data Sharing Using Contacts Content Provider
- **Aim:** To develop an Android application using Java and XML that retrieves contact information through the Contacts Content Provider and shares selected contact details using implicit Intents.
- **Theory:**
  - A **Content Provider** is an Android component that manages and provides access to structured data across application boundaries.
  - `ContactsContract` defines the data structures used to access Android contact information.
  - `ContentResolver` communicates with the Contacts Content Provider to execute queries.
  - Runtime permissions (`READ_CONTACTS`) protect sensitive user data.
  - `Intent.ACTION_SEND` and `Intent.createChooser()` allow sharing contact information with third-party applications.
- **Result:** The application successfully demonstrates reading contact data from `ContactsContract`, filtering contacts by name, displaying selected contact details, and sharing contact information using implicit Intents.

---

