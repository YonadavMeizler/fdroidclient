package org.fdroid.fdroid.authorisation;


import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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

public class InitDialog extends AuthorisationDialog{

    private final Button againButton;

    public InitDialog(final Context context) {
        super(context, R.layout.repo_load);
        againButton = view.findViewById(R.id.again_button);
        againButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                Authorisation.repoInit(context);
            }
        });
        setTitle(context.getString(R.string.repo_init_title));
        createAlert();
    }

    @Override
    public void error(String errCode){
        super.error(errCode);
        againButton.setVisibility(View.VISIBLE);
    }

    public void forbidden(){
        clearRepos(context);
        dismiss();
        Authorisation.requestOtp(context);
    }

    public void setRepo(JSONObject jsonObject){
        dismiss();
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
