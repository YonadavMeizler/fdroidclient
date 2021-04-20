package org.fdroid.fdroid.authorisation;

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


public class OtpDialog extends AuthorisationDialog{

    final String deviceId;
    final EditText otpEditText;
    final LinearLayout otpLayout;
    final LinearLayout problemLayout;
    final Button tryAgainButton;
    final Button confirmOtpButton;
    final TextView deviceIdTextView;
    final TextView waitTextView;

    public OtpDialog(final Context context) {
        super(context, R.layout.otp_request);
        deviceId = Preferences.get().getDeviceID();
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
        deviceIdTextView.setText(context.getString(R.string.otp_device_id_text, deviceId));
        setTitle(context.getString(R.string.otp_dialog_title));
        createAlert();
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

}
