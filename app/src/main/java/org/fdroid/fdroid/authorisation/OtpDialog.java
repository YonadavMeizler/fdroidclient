package org.fdroid.fdroid.authorisation;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.receiver.OtpReceiver;


public class OtpDialog extends AuthorisationDialog{

    final EditText otpEditText;
    final LinearLayout otpLayout;
    final LinearLayout problemLayout;
    final Button tryAgainButton;
    final Button confirmOtpButton;
    final TextView deviceIdTextView;
    final TextView waitTextView;
    final OtpReceiver otpReceiver;

    public OtpDialog(final Context context) {
        super(context, R.layout.otp_request);
        //Controls initialization
        otpEditText = view.findViewById(R.id.otp_edit_text);
        waitTextView = view.findViewById(R.id.otp_dialog_wait);
        otpEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    otpValidation(context);
                }
                return false;
            }
        });
        otpReceiver = new OtpReceiver();
        otpLayout = view.findViewById(R.id.otp_confirm_layout);
        problemLayout = view.findViewById(R.id.otp_dialog_problem_layout);
        tryAgainButton = view.findViewById(R.id.otp_dialog_try_again_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Authorisation.requestOtp(context);
            }
        });

        confirmOtpButton = view.findViewById(R.id.otp_confirm_button);
        confirmOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpValidation(context);
            }
        });
        deviceIdTextView = view.findViewById(R.id.otp_dialog_deviceId_text);
        deviceIdTextView.setText(context.getString(R.string.otp_device_id_text, Preferences.get().getDeviceID()));
        setTitle(context.getString(R.string.otp_dialog_title));

        createAlert();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent == null){
                return;
            }
            if(intent.hasExtra(OtpReceiver.extra)){
                final String otp = intent.getStringExtra(OtpReceiver.extra);
                otpEditText.setText(otp);
            }
        }
    };

    @Override
    public void show() {
        context.registerReceiver(otpReceiver, new IntentFilter(OtpReceiver.INTENT_FILTER));
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter(OtpReceiver.extra));
        super.show();
    }

    @Override
    protected void dismiss() {
        context.unregisterReceiver(otpReceiver);
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        super.dismiss();
    }

    public void notFoundDialog(){
        waitTextGone();
        setMessage(context.getString(R.string.otp_dialog_service_not_found));
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void forbiddenDialog(){
        waitTextGone();
        setMessage(context.getString(R.string.otp_dialog_service_forbidden));
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void noDeviceIdDialog(){
        waitTextGone();
        setMessage(context.getString(R.string.otp_dialog_service_odin));
        deviceIdTextView.setText(context.getString(R.string.otp_device_id_text, "None"));
        problemLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void error(String responseCode){
        super.error(responseCode);
        waitTextGone();
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void correctDialog(){
        waitTextGone();
        otpLayout.setVisibility(View.VISIBLE);
    }

    private void waitTextGone(){
        progressBar.setVisibility(View.GONE);
        waitTextView.setVisibility(View.GONE);
    }

    private void otpValidation(Context context){
        String otp = otpEditText.getText().toString();
        dismiss();
        Authorisation.validationOtp(otp, context);
    }

    public void setDeviceIdTextView(@NonNull final String deviceId){
        deviceIdTextView.setText(context.getString(R.string.otp_device_id_text, deviceId));
    }



}
