package com.imrd.smartcontacts.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * ImageHelper.java
 * -------------------------------------------------
 * Utility class for all image operations:
 *   • Convert Uri  → compressed byte[]  (for DB storage)
 *   • Convert byte[] → Bitmap           (for display)
 *   • Crop Bitmap to circle             (WhatsApp style)
 *   • Compress Bitmap to byte[]         (after camera capture)
 *
 * Max stored size: 200×200 px JPEG at 80% quality
 * This keeps each photo ~10–30 KB in the DB.
 * -------------------------------------------------
 */
public class ImageHelper {

    // Max dimension in pixels before we scale down
    private static final int MAX_SIZE = 200;
    // JPEG compression quality (0-100)
    private static final int QUALITY  = 80;

    /**
     * Reads an image Uri (Gallery pick), scales it down,
     * and returns compressed JPEG bytes ready for DB storage.
     */
    public static byte[] uriToBytes(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap original = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();
            if (original == null) return null;
            return bitmapToBytes(original);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Scales a Bitmap down to MAX_SIZE and returns
     * compressed JPEG bytes ready for DB storage.
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        if (bitmap == null) return null;
        Bitmap scaled = scaleBitmap(bitmap);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, baos);
        return baos.toByteArray();
    }

    /**
     * Converts stored byte[] back to a Bitmap for display.
     * Returns null if bytes is null or empty.
     */
    public static Bitmap bytesToBitmap(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * Crops a Bitmap into a circle (for WhatsApp-style avatars).
     */
    public static Bitmap toCircle(Bitmap bitmap) {
        if (bitmap == null) return null;
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        Rect rect = new Rect(0, 0, size, size);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // Centre-crop the source bitmap
        int xOffset = (bitmap.getWidth()  - size) / 2;
        int yOffset = (bitmap.getHeight() - size) / 2;
        Rect srcRect = new Rect(xOffset, yOffset,
                                xOffset + size, yOffset + size);
        canvas.drawBitmap(bitmap, srcRect, rect, paint);
        return output;
    }

    // ── Private helpers ──────────────────────────────

    /** Scale bitmap so its largest side is MAX_SIZE. */
    private static Bitmap scaleBitmap(Bitmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= MAX_SIZE && h <= MAX_SIZE) return src;
        float ratio = (float) MAX_SIZE / Math.max(w, h);
        int newW = Math.round(w * ratio);
        int newH = Math.round(h * ratio);
        return Bitmap.createScaledBitmap(src, newW, newH, true);
    }
}
