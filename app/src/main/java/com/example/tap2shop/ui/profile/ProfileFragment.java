package com.example.tap2shop.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.tap2shop.R;
import com.example.tap2shop.ui.LoginActivity;
import com.example.tap2shop.ui.orders.MyOrdersActivity;
import com.example.tap2shop.util.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        sessionManager = new SessionManager(requireContext());

        // 1. Corrected ID references and unique variable names
        RadioButton rbEnglish = view.findViewById(R.id.rbEnglish);
        RadioButton rbNyanja = view.findViewById(R.id.rbNyanja);
        RadioButton rbBemba = view.findViewById(R.id.rbBemba);

        MaterialButton btnSaveLang = view.findViewById(R.id.btnSaveLanguage);
        MaterialButton btnMyOrders = view.findViewById(R.id.btnMyOrders);
        MaterialButton btnLogout = view.findViewById(R.id.btnLogout);

        // 2. Set the current checked state based on saved preference
        String lang = sessionManager.getLanguage();
        if ("ny".equals(lang)) {
            rbNyanja.setChecked(true);
        } else if ("bem".equals(lang)) {
            rbBemba.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }

        // 3. Updated save logic for three languages
        btnSaveLang.setOnClickListener(v -> {
            String selected = "en";
            if (rbNyanja.isChecked()) selected = "ny";
            else if (rbBemba.isChecked()) selected = "bem";

            sessionManager.setLanguage(selected);
            Snackbar.make(v, R.string.message_language_saved, Snackbar.LENGTH_SHORT).show();

            // Recreate the activity to apply the new language
            requireActivity().recreate();
        });

        btnMyOrders.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), MyOrdersActivity.class)));

        btnLogout.setOnClickListener(v -> {
            sessionManager.clearSession();
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }
}