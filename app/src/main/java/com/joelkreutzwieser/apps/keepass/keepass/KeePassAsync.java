package com.joelkreutzwieser.apps.keepass.keepass;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.view.WindowManager;
import android.widget.Toast;

import com.joelkreutzwieser.apps.keepass.ApplicationBase;
import com.joelkreutzwieser.apps.keepass.GroupViewActivity;
import com.joelkreutzwieser.apps.keepass.R;
import com.joelkreutzwieser.apps.keepass.keepass.domain.KeePassFile;

import java.io.InputStream;

public class KeePassAsync extends AsyncTask<Void, Void, KeePassFile> {

    private InputStream inputStream;
    private String password;
    private ProgressDialog progressDialog;
    private GroupViewActivity groupViewActivity;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.setMessage("Decrypting and parsing database...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressNumberFormat("");
        progressDialog.show();
        groupViewActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    public KeePassAsync(InputStream inputStream, String password, GroupViewActivity groupViewActivity) {
        this.inputStream = inputStream;
        this.password = password;
        this.groupViewActivity = groupViewActivity;
        progressDialog = new ProgressDialog(groupViewActivity);
    }

    @Override
    protected KeePassFile doInBackground(Void... params) {
        try {
            return KeePassDatabase.getInstance(inputStream).openDatabase(password, progressDialog);
        } catch (Exception e) {
            cancel(true);
            return null;
        }
    }

    @Override
    protected void onPostExecute(KeePassFile keePassFile) {
        super.onPostExecute(keePassFile);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ((ApplicationBase) groupViewActivity.getApplication()).setDatabase(keePassFile);
        Toast.makeText(groupViewActivity, R.string.openedDatabase, Toast.LENGTH_SHORT).show();
        groupViewActivity.drawerFragment.drawerLayout.closeDrawers();
        groupViewActivity.drawerFragment.sendToActivity.onNavigationItemSelected(((ApplicationBase) groupViewActivity.getApplication()).getDatabaseRoot());
        groupViewActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onCancelled(KeePassFile keePassFile) {
        super.onCancelled(keePassFile);
        if(progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ((ApplicationBase) groupViewActivity.getApplication()).setDatabase(keePassFile);
        Toast.makeText(groupViewActivity, R.string.failedToOpenDatabase, Toast.LENGTH_LONG).show();
        groupViewActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
