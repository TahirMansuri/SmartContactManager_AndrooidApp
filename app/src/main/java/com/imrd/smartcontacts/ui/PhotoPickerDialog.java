package com.imrd.smartcontacts.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.imrd.smartcontacts.R;

/**
 * PhotoPickerDialog.java
 * -------------------------------------------------
 * A Material bottom sheet that lets the user choose
 * between Camera and Gallery as the photo source.
 *
 * Usage:
 *   PhotoPickerDialog dialog = new PhotoPickerDialog();
 *   dialog.setListener(new PhotoPickerDialog.PhotoSourceListener() {
 *       public void onCameraSelected()  { ... }
 *       public void onGallerySelected() { ... }
 *   });
 *   dialog.show(getSupportFragmentManager(), "photo_picker");
 * -------------------------------------------------
 */
public class PhotoPickerDialog extends BottomSheetDialogFragment {

    public interface PhotoSourceListener {
        void onCameraSelected();
        void onGallerySelected();
    }

    private PhotoSourceListener listener;

    public void setListener(PhotoSourceListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_photo_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout optionCamera  = view.findViewById(R.id.option_camera);
        LinearLayout optionGallery = view.findViewById(R.id.option_gallery);
        LinearLayout optionCancel  = view.findViewById(R.id.option_cancel);

        optionCamera.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onCameraSelected();
        });

        optionGallery.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onGallerySelected();
        });

        optionCancel.setOnClickListener(v -> dismiss());
    }
}
