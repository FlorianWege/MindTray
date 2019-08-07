package com.example.mindtray.shared;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;

/*
    all fragments should inherited from this one in order to bestow extra functionalities
 */

public class MyFragmentActivity extends FragmentActivity {
    public MyFragmentActivity getActivity() {
        return this;
    }

    protected Storage _storage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        _storage = Storage.getInstance(this);
    }

    @Override
    public void startActivity(Intent intent) {
        Util.lockViews(this, false);

        super.startActivity(intent);
    }

    private boolean _stopped = false;

    @Override
    protected void onResume() {
        super.onResume();

        if (_stopped) {
            _stopped = false;
            Util.lockViews(this, true);
        }
    }

    @Override
    protected void onPause() {
        if (!_stopped) {
            _stopped = true;
            Util.lockViews(this, true);
        }

        super.onPause();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Util.lockViews(this, false);
    }

    @Override
    public void onContextMenuClosed(Menu menu) {
        super.onContextMenuClosed(menu);

        Util.lockViews(this, true);
    }

    public void showDialog(MyDialog dialog) {
        Util.lockViews(this, false);

        dialog.show(getFragmentManager(), dialog.toString());

        dialog.addListener(new MyDialog.Listener() {
            @Override
            public void onDismiss() {
                Util.lockViews(MyFragmentActivity.this, true);
            }
        });
    }

    public boolean hasPermission(String name) {
        return (ContextCompat.checkSelfPermission(this, name) == PackageManager.PERMISSION_GRANTED);
    }

    public void acceptPermission(String permission) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                acceptPermission(permissions[i]);
            }
        }
    }

    public void requestPermission(String name) {
        if (Build.VERSION.SDK_INT >= 23) {
            //there are runtime permission inquires, yay
            if (hasPermission(name)) {
                acceptPermission(name);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{name}, 0);
            }
        }
    }
}