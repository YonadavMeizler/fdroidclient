package org.fdroid.fdroid.authorisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;


public class OtpDialog{

    private final AlertDialog alertDialog;
    final String deviceId;
    final EditText otpEditText;
    final LinearLayout otpLayout;
    final LinearLayout problemLayout;
    final Button tryAgainButton;
    final Button closeButton;
    final Button confirmOtpButton;
    final TextView otpProblemTextView;
    final TextView deviceIdTextView;
    final TextView waitTextView;

    public OtpDialog(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final View view = View.inflate(context, R.layout.otp_request, null);
        deviceId = Preferences.get().getDeviceID();
        //Controls initialization
        otpEditText = view.findViewById(R.id.otp_edit_text);

        otpEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    otpValidation(context);
                }
                return false;
            }
        });

        otpLayout = view.findViewById(R.id.otp_confirm_layout);

        problemLayout = view.findViewById(R.id.otp_dialog_problem_layout);

        tryAgainButton = view.findViewById(R.id.otp_dialog_try_again_button);
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Authorisation.requestOtp(context);
            }
        });

        closeButton = view.findViewById(R.id.otp_dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                ((Activity)context).finish();
            }
        });

        confirmOtpButton = view.findViewById(R.id.otp_confirm_button);
        confirmOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otpValidation(context);
            }
        });

        otpProblemTextView = view.findViewById(R.id.otp_dialog_alert_text);

        deviceIdTextView = view.findViewById(R.id.otp_dialog_deviceId_text);
        deviceIdTextView.setText(context.getString(R.string.otp_device_id_text, deviceId));

        waitTextView = view.findViewById(R.id.otp_dialog_wait);

        builder.setView(view);
        builder.setTitle(context.getString(R.string.otp_dialog_title));
        builder.setCancelable(false);

        alertDialog = builder.create();
    }

    public void notFoundDialog(){
        waitTextGone();
        otpProblemTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_not_found));
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void forbiddenDialog(){
        waitTextGone();
        otpProblemTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_forbidden));
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void errorDialog(String responseCode){
        waitTextGone();
        otpProblemTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_error, responseCode));
        problemLayout.setVisibility(View.VISIBLE);
    }

    public void correctDialog(){
        waitTextGone();
        otpLayout.setVisibility(View.VISIBLE);
    }

    public void show() {
        alertDialog.show();
    }

    private void waitTextGone(){
        waitTextView.setVisibility(View.GONE);
    }

    private void otpValidation(Context context){
        String otp = otpEditText.getText().toString();
        alertDialog.dismiss();
        Authorisation.validationOtp(otp, context);
    }

}
