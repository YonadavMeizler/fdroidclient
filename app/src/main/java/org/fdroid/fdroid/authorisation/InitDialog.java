package org.fdroid.fdroid.authorisation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.fdroid.fdroid.AddRepoIntentService;
import org.fdroid.fdroid.Preferences;
import org.fdroid.fdroid.R;
import org.fdroid.fdroid.UpdateService;
import org.fdroid.fdroid.data.Repo;
import org.fdroid.fdroid.data.RepoProvider;
import org.fdroid.fdroid.data.Schema;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

public class InitDialog{

    private final Button cancelButton;
    private final Button againButton;
    private final TextView messageTextView;
    private final AlertDialog alertDialog;

    public InitDialog(final Context context) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = View.inflate(context, R.layout.repo_load, null);
        cancelButton = view.findViewById(R.id.close_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)context).finish();
            }
        });

        againButton = view.findViewById(R.id.again_button);
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                Authorisation.repoInit(context);
            }
        });

        messageTextView = view.findViewById(R.id.init_message);

        builder.setTitle(context.getString(R.string.repo_init_title));
        builder.setCancelable(false);
        builder.setView(view);

        alertDialog = builder.create();
    }

    public void error(String errCode){
        cancelButton.setVisibility(View.VISIBLE);
        againButton.setVisibility(View.VISIBLE);
        messageTextView.setText(alertDialog.getContext().getString(R.string.repo_server_error_resp, errCode));
    }

    public void networkError(String errCode){
        cancelButton.setVisibility(View.VISIBLE);
        againButton.setVisibility(View.VISIBLE);
        messageTextView.setText(alertDialog.getContext().getString(R.string.otp_dialog_service_error, errCode));
    }

    public void show(){
        alertDialog.show();
    }

    public void forbidden(){
        Context context = alertDialog.getContext();
        clearRepos(context);
        Authorisation.requestOtp(context);
    }

    public void setRepo(JSONObject jsonObject){
        alertDialog.dismiss();
        Context context = alertDialog.getContext();
        String hostname = Preferences.get().getHostName();
        clearRepos(context);
        String TAG = "RepoInit";
        try {
            if(jsonObject.has("repo_list")) {
                JSONArray array = jsonObject.getJSONArray("repo_list");
                for(int i=0; i<array.length(); i++){
                    try {
                        String group_id = array.getJSONObject(i).getString("group_id");
                        String url = "https://" + hostname + "/" + group_id;
                        String address = AddRepoIntentService.normalizeUrl(url);
                        ContentValues values = new ContentValues();
                        values.put(Schema.RepoTable.Cols.ADDRESS, address);
                        RepoProvider.Helper.insert(context, values);
                    }
                    catch(Exception e){
                        Log.w(TAG, e.getMessage());
                    }
                }
                UpdateService.forceUpdateRepo(context);
            }
        }
        catch(JSONException e){
            Log.e(TAG, e.getMessage());
        }

    }

    private void clearRepos(Context context){
        List<Repo> repos =  RepoProvider.Helper.all(context);
        for(Repo repo : repos){
            RepoProvider.Helper.remove(context, repo.getId());
        }
    }




}
