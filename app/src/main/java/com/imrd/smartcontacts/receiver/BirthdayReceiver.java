package com.imrd.smartcontacts.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.imrd.smartcontacts.R;
import com.imrd.smartcontacts.database.DatabaseHelper;
import com.imrd.smartcontacts.model.Contact;
import com.imrd.smartcontacts.ui.MainActivity;
import com.imrd.smartcontacts.util.SessionManager;

import java.util.Calendar;
import java.util.List;

/**
 * BirthdayReceiver.java  — NEW FILE (Batch 1)
 * BroadcastReceiver triggered by AlarmManager at 8 AM daily.
 * Checks all contacts' DOBs and fires notifications for birthdays today.
 * Also handles BOOT_COMPLETED to reschedule alarm after reboot.
 */
public class BirthdayReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID   = "birthday_channel";
    private static final String CHANNEL_NAME = "Birthday Reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Reschedule after reboot
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            com.imrd.smartcontacts.util.BirthdayScheduler.scheduleDailyCheck(context);
            return;
        }

        SessionManager session = new SessionManager(context);
        if (!session.isLoggedIn()) return;
        int userId = session.getLoggedInUserId();

        DatabaseHelper db = DatabaseHelper.getInstance(context);
        List<Contact> contacts = db.getAllContacts(userId);

        Calendar today = Calendar.getInstance();
        int todayDay   = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);

        createNotificationChannel(context);

        int notifId = 2000;
        for (Contact c : contacts) {
            if (c.getDob() == null || c.getDob().isEmpty()) continue;
            Calendar dob = parseDob(c.getDob());
            if (dob == null) continue;
            if (dob.get(Calendar.DAY_OF_MONTH) == todayDay &&
                dob.get(Calendar.MONTH) == todayMonth) {
                fireNotification(context, c, notifId++);
            }
        }
    }

    private void fireNotification(Context context, Contact contact, int notifId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_birthday)
            .setContentTitle("🎂 Birthday Reminder!")
            .setContentText("Today is " + contact.getFullName() + "'s birthday!")
            .setStyle(new NotificationCompat.BigTextStyle()
                .bigText("Wish " + contact.getFullName() + " a Happy Birthday! 🎉"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notifId, builder.build());
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Daily birthday reminders for your contacts");
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Calendar parseDob(String dob) {
        try {
            String[] parts = dob.split("/");
            if (parts.length != 3) return null;
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
            cal.set(Calendar.MONTH,        Integer.parseInt(parts[1]) - 1);
            cal.set(Calendar.YEAR,         Integer.parseInt(parts[2]));
            return cal;
        } catch (Exception e) { return null; }
    }
}
