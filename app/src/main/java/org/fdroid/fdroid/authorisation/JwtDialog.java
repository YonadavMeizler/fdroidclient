package org.fdroid.fdroid.authorisation;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;

public class JwtDialog extends AuthorisationDialog{

    final private Button retryButton;

    public JwtDialog(@NonNull final Context context) {
        super(context, R.layout.jwt_request);
        messageTextView = view.findViewById(R.id.jwt_dialog_wait);
        retryButton = view.findViewById(R.id.jwt_try_again_button);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Authorisation.requestOtp(context);
            }
        });
        setTitle(context.getString(R.string.jwt_dialog_title));
        createAlert();
    }

    public void setToken(String token){
        Preferences.get().setAccessToken(token);
        dismiss();
        Authorisation.repoInit(context);
    }

    @Override
    public void error(String errCode){
        super.error(errCode);
        retryButton.setVisibility(View.VISIBLE);
    }

}
