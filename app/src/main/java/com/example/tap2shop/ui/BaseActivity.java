package com.example.tap2shop.ui;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tap2shop.util.LocaleHelper;
import com.example.tap2shop.util.SessionManager;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SessionManager sessionManager = new SessionManager(newBase);
        String lang = sessionManager.getLanguage();
        Context ctx = LocaleHelper.updateLocale(newBase, lang);
        super.attachBaseContext(ctx);
    }
}
