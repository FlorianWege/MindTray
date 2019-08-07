package com.example.mindtray.shared;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.mindtray.R;

/*
    general purpose confirm dialog with OK/Abort button
 */

public class ConfirmDialog extends MyDialog {
    private View _view;

    private TextView _textView_title;
    private Button _button_ok;
    private Button _button_cancel;

    private String _title;

    public interface Listener extends MyDialog.Listener {
        void onDecline();
        void onAccept();
    }

    private Listener _listener;

    public void setListener(Listener val) {
        _listener = val;

        super.setListener(val);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _view = inflater.inflate(R.layout.dialog_confirm, container, false);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        _textView_title = (TextView) _view.findViewById(R.id.textView_title);

        _textView_title.setText(_title);

        _button_ok = (Button) _view.findViewById(R.id.button_ok);

        _button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_listener != null) _listener.onAccept();

                dismiss();
            }
        });

        _button_cancel = (Button) _view.findViewById(R.id.button_cancel);

        _button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_listener != null) _listener.onDecline();

                dismiss();
            }
        });

        return _view;
    }

    public void setArgs(String title, Listener listener) {
        _title = title;
        setListener(listener);
    }
}