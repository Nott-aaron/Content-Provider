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