package com.imrd.smartcontacts.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import com.imrd.smartcontacts.model.Contact;

import java.io.File;
import java.io.FileOutputStream;

/**
 * VCardHelper.java  — NEW FILE (Batch 2)
 * Generates a vCard (.vcf) file from a Contact object
 * and opens the Android share sheet so the user can
 * send it via WhatsApp, Email, Bluetooth, etc.
 *
 * vCard format used: vCard 3.0
 */
public class VCardHelper {

    /**
     * Creates a .vcf file and opens the share sheet.
     * @param context Activity context
     * @param contact The contact to share
     */
    public static void shareContact(Context context, Contact contact) {
        try {
            // Build vCard content
            StringBuilder sb = new StringBuilder();
            sb.append("BEGIN:VCARD\n");
            sb.append("VERSION:3.0\n");
            sb.append("FN:").append(contact.getFullName()).append("\n");
            sb.append("N:").append(contact.getLastName()).append(";")
              .append(contact.getFirstName()).append(";;;\n");
            sb.append("TEL;TYPE=CELL:").append(contact.getMobile()).append("\n");
            sb.append("EMAIL:").append(contact.getEmail()).append("\n");
            sb.append("ADR;TYPE=HOME:;;").append(contact.getCity()).append(";")
              .append(contact.getState()).append(";;IN\n");
            if (contact.hasDob()) {
                // Convert dd/MM/yyyy → yyyyMMdd for vCard BDAY field
                try {
                    String[] parts = contact.getDob().split("/");
                    String bday = parts[2] + parts[1] + parts[0];
                    sb.append("BDAY:").append(bday).append("\n");
                } catch (Exception ignored) {}
            }
            if (contact.hasGroup()) {
                sb.append("CATEGORIES:").append(contact.getGroupTag()).append("\n");
            }
            sb.append("END:VCARD\n");

            // Write to cache file
            String fileName = contact.getFullName().replaceAll("\\s+", "_") + ".vcf";
            File vcfFile = new File(context.getCacheDir(), fileName);
            FileOutputStream fos = new FileOutputStream(vcfFile);
            fos.write(sb.toString().getBytes("UTF-8"));
            fos.close();

            // Get URI via FileProvider
            Uri uri = FileProvider.getUriForFile(context,
                context.getPackageName() + ".provider", vcfFile);

            // Open share sheet
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/vcard");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Contact: " + contact.getFullName());
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(shareIntent,
                "Share " + contact.getFullName()));

        } catch (Exception e) {
            e.printStackTrace();
            android.widget.Toast.makeText(context,
                "Could not share contact.", android.widget.Toast.LENGTH_SHORT).show();
        }
    }
}
