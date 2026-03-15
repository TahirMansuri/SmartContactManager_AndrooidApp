package com.imrd.smartcontacts.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.util.ImageHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * ContactAdapter.java
 * RecyclerView adapter for the contact list.
 * Shows circle photo if available, coloured initials otherwise.
 */
public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private static final int[] AVATAR_COLORS = {
        0xFF1565C0, 0xFF2E7D32, 0xFF6A1B9A, 0xFFC62828, 0xFFE65100,
        0xFF00695C, 0xFF4527A0, 0xFF558B2F, 0xFF283593, 0xFF4E342E,
    };

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private final Context               context;
    private       List<Contact>         contactList;
    private final OnContactClickListener clickListener;

    public ContactAdapter(Context context, List<Contact> contactList, OnContactClickListener listener) {
        this.context       = context;
        this.contactList   = contactList != null ? contactList : new ArrayList<>();
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);

        if (contact.hasPhoto()) {
            // Show circle photo
            Bitmap bmp = ImageHelper.bytesToBitmap(contact.getPhoto());
            if (bmp != null) {
                holder.ivPhoto.setImageBitmap(ImageHelper.toCircle(bmp));
                holder.ivPhoto.setVisibility(View.VISIBLE);
                holder.tvInitials.setVisibility(View.GONE);
                holder.cvAvatar.setCardBackgroundColor(0xFFEEEEEE);
            } else {
                showInitials(holder, contact, position);
            }
        } else {
            showInitials(holder, contact, position);
        }

        holder.tvName    .setText(contact.getFullName());
        holder.tvMobile  .setText(contact.getMobile());
        holder.tvLocation.setText(contact.getCity() + ", " + contact.getState());
        holder.itemView.setOnClickListener(v -> clickListener.onContactClick(contact));
    }

    private void showInitials(ContactViewHolder holder, Contact contact, int position) {
        holder.ivPhoto.setVisibility(View.GONE);
        holder.tvInitials.setVisibility(View.VISIBLE);
        holder.tvInitials.setText(contact.getInitials());
        holder.cvAvatar.setCardBackgroundColor(AVATAR_COLORS[position % AVATAR_COLORS.length]);
    }

    @Override
    public int getItemCount() { return contactList == null ? 0 : contactList.size(); }

    public void updateList(List<Contact> newList) {
        this.contactList = newList == null ? new ArrayList<>() : newList;
        notifyDataSetChanged();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        CardView  cvAvatar;
        ImageView ivPhoto;
        TextView  tvInitials, tvName, tvMobile, tvLocation;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            cvAvatar   = itemView.findViewById(R.id.cv_avatar);
            ivPhoto    = itemView.findViewById(R.id.iv_photo_list);
            tvInitials = itemView.findViewById(R.id.tv_initials);
            tvName     = itemView.findViewById(R.id.tv_name);
            tvMobile   = itemView.findViewById(R.id.tv_mobile);
            tvLocation = itemView.findViewById(R.id.tv_location);
        }
    }
}
