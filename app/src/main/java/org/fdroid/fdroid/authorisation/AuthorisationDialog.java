package org.fdroid.fdroid.authorisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.fdroid.fdroid.R;


public class AuthorisationDialog {
    protected Button cancelButton;
    protected TextView messageTextView;
    protected ProgressBar progressBar;
    private AlertDialog alertDialog;
    protected final Context context;
    protected View view;
    private AlertDialog.Builder builder;
    public AuthorisationDialog(final Context context, int viewId){
        view = View.inflate(context, viewId, null);
        builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(view);
        this.context = context;
        messageTextView = view.findViewById(R.id.message_view);
        progressBar = view.findViewById(R.id.wait_progress);
        cancelButton = view.findViewById(R.id.close_button);
        if(cancelButton!=null) {
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((Activity) context).finish();
                }
            });
        }
    }

    protected void setTitle(String title){
        builder.setTitle(title);
    }

    protected void createAlert(){
        alertDialog = builder.create();
    }

    public void show(){
        alertDialog.show();
    }

    protected void dismiss(){
        alertDialog.dismiss();
    }

    public void error(String errCode) {
        cancelButton.setVisibility(View.VISIBLE);
        if (errCode.equals(Authorisation.IO_ERROR)) {
            messageTextView.setText(
                    alertDialog.getContext().getString(R.string.otp_dialog_service_error, errCode));
        }
        else if (errCode.equals(Authorisation.INTERNAL_ERROR)){
            messageTextView.setText(
                    alertDialog.getContext().getString(R.string.otp_dialog_service_error,
                                                                       Authorisation.INTERNAL_ERROR));
        }
        else if(errCode.equals(Authorisation.FORBIDDEN)){
            messageTextView.setText(alertDialog.getContext().getString(R.string.jwt_otp_not_valid));
        }
        else {
            messageTextView.setText(
                    alertDialog.getContext().getString(R.string.repo_server_error_resp, errCode));
        }
        progressBar.setVisibility(View.GONE);
    }

    protected void setMessage(String message){
        messageTextView.setText(message);
    }

}
