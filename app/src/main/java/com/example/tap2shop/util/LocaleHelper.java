package com.example.tap2shop.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;

import java.util.Locale;

public class LocaleHelper {

    public static Context updateLocale(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration config = new Configuration(resources.getConfiguration());

        // Sets the locale for the configuration
        config.setLocale(locale);

        // This line ensures that strings already loaded in the base resources
        // are also updated to the new language
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        // Returns a new context with the updated configuration
        return context.createConfigurationContext(config);
    }
}