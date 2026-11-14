package com.ssrtunnel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnConnect, btnAddHours;
    private TextView txtTimer, txtStatus;
    private SharedPreferences sharedPreferences;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnConnect = findViewById(R.id.btnConnect);
        btnAddHours = findViewById(R.id.btnAddHours);
        txtTimer = findViewById(R.id.txtTimer);
        txtStatus = findViewById(R.id.txtStatus);

        sharedPreferences = getSharedPreferences("SSR_Tunnel_Prefs", MODE_PRIVATE);
        timeLeftInMillis = sharedPreferences.getLong("timeLeft", 0);

        updateTimerUI();
        checkForAppUpdate(); // অ্যাপ ওপেন হওয়ার সাথে সাথে আপডেট ও নোটিফিকেশন চেক করবে

        // Watch Ad Button to Add Time
        btnAddHours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardedAdSimulation();
            }
        });

        // Connect Button Logic
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (timeLeftInMillis > 0) {
                    toggleVpnConnection();
                } else {
                    Toast.makeText(MainActivity.this, "Time is up! Please watch an ad to add time.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Rewarded Ad Simulation for Adding 5 Hours
    private void showRewardedAdSimulation() {
        long extraTime = 5L * 60L * 60L * 1000L; // 5 Hours in milliseconds
        timeLeftInMillis += extraTime;
        
        saveTimerState();
        updateTimerUI();
        Toast.makeText(this, "Success! 5 hours added to your account.", Toast.LENGTH_SHORT).show();
    }

    private void toggleVpnConnection() {
        txtStatus.setText("Connected");
        txtStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        startTimer();
        Toast.makeText(this, "SSR Tunnel Connected Successfully!", Toast.LENGTH_SHORT).show();
    }

    private void startTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                updateTimerUI();
                saveTimerState();
            }

            @Override
            public void onFinish() {
                timeLeftInMillis = 0;
                updateTimerUI();
                saveTimerState();
                txtStatus.setText("Disconnected");
                txtStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                Toast.makeText(MainActivity.this, "Connection Time Out!", Toast.LENGTH_SHORT).show();
            }
        }.start();
    }

    private void updateTimerUI() {
        long hours = (timeLeftInMillis / 1000) / 3600;
        long minutes = ((timeLeftInMillis / 1000) % 3600) / 60;
        long seconds = (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        txtTimer.setText(timeLeftFormatted);
    }

    private void saveTimerState() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("timeLeft", timeLeftInMillis);
        editor.apply();
    }

    // Auto Update & Notification System Check
    private void checkForAppUpdate() {
        boolean isFirstLaunchAfterUpdate = sharedPreferences.getBoolean("Update_Checked_V1", true);

        if (isFirstLaunchAfterUpdate) {
            // Update Success Notification Dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Update Successful");
            builder.setMessage("SSR Tunnel has been updated to the latest version successfully. Enjoy high-speed servers!");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("Update_Checked_V1", false);
            editor.apply();
        }
    }
}
