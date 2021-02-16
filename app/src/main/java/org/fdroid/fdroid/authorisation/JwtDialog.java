package org.fdroid.fdroid.authorisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;


public class JwtDialog{

    private AlertDialog alertDialog;
    final private Button retryButton;
    final private TextView messageTextView;

    public JwtDialog(@NonNull final Context context) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        final View view = View.inflate(context, R.layout.jwt_request, null);

        messageTextView = view.findViewById(R.id.jwt_dialog_wait);

        Button closeButton = view.findViewById(R.id.jwt_dialog_close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                ((Activity)context).finish();
            }
        });

        retryButton = view.findViewById(R.id.jwt_try_again_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Authorisation.requestOtp(context);
            }
        });

        builder.setTitle(context.getString(R.string.jwt_dialog_title));
        builder.setCancelable(false);
        builder.setView(view);
        alertDialog = builder.create();
    }

    public void show() {
       alertDialog.show();
    }

    public void setToken(String token){
        Context context = alertDialog.getContext();
        alertDialog.dismiss();
        Preferences.get().setAccessToken(token);
        Authorisation.repoInit(context);
    }

    public void forbidden(){
        retryButton.setVisibility(View.VISIBLE);
        messageTextView.setText(alertDialog.getContext().getString(R.string.jwt_otp_not_valid));
    }

    public void error(String errCode){
        retryButton.setVisibility(View.VISIBLE);
        messageTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_error, errCode));
    }

    public void serverError(){
        retryButton.setVisibility(View.VISIBLE);
        messageTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_error, "500"));
    }





}
