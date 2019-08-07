package com.example.mindtray.shared;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.HashSet;
import java.util.Set;

/*
    all dialogs should inherit from this one in order to bestow extra functionalities
 */

public abstract class MyDialog extends DialogFragment {
    public interface Listener {
        void onDismiss();
    }

    public MyDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }

    private Listener _listener = null;

    public void setListener(Listener val) {
        _listener = val;
    }

    private Set<Listener> _listeners = new HashSet<>();

    public void addListener(Listener val) {
        _listeners.add(val);
    }

    public void removeListener(Listener val) {
        _listeners.remove(val);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (_listener != null) {
            _listener.onDismiss();
        }

        for (Listener listener : _listeners) {
            listener.onDismiss();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}