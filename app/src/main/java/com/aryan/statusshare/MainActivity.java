package com.aryan.statusshare;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private String uname = "";
    private int uid = 0;
    static final String COOKIES_HEADER = "Set-Cookie";
    static final String COOKIE = "Cookie";
    final static HostnameVerifier VERIFY_LOCALHOST = new HostnameVerifier()
    {
        public boolean verify(String hostname, SSLSession session)
        {
            if (hostname.equals("192.168.0.10"))
                return true;
            else
                return false;
        }
    };
    static CookieManager msCookieManager = new CookieManager();
    DBHelper cache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_page);
        uname = "";
        uid = 0;
        msCookieManager = new CookieManager();
        cache = new DBHelper(this);
        initializeDB();
    }

    public void onRegisterClick(View view){
        setContentView(R.layout.register_user);
    }

    public void onBackClick (View view){
        setContentView(R.layout.login_page);
    }

    public void BackToMain (View view){
        setContentView(R.layout.main_page);
    }

    public void onPost (View view){
        setContentView(R.layout.post_status);
    }

    public void onLogOut (View view){
        setContentView(R.layout.login_page);
        uname = "";
        uid = 0;
        cache = new DBHelper(this);
        msCookieManager.getCookieStore().removeAll();
    }

    @Override
    public void onDestroy () {
        super.onDestroy();
        uname = "";
        uid = 0;
        cache = new DBHelper(this);
        msCookieManager.getCookieStore().removeAll();
    }

    public void onSearchUsers (View view){
        setContentView(R.layout.search_user);
    }

    public void onStatusHistory (View view){
        String URL = "https://192.168.0.10/Android/showownposts.php";
        getOwnPost(uid,URL,uname);
    }

    public void onSearch (View view){
        String URL = "https://192.168.0.10/Android/searchuser.php";
        EditText x = (EditText) findViewById(R.id.SearchUser);
        String key = x.getText().toString();
        SearchUser(key,URL);
    }

    public void doPost (View view){
        String URL = "https://192.168.0.10/Android/poststatus.php";
        EditText x = (EditText) findViewById(R.id.Status);
        String status = x.getText().toString();
        if (status.length() > 200){
            Toast.makeText(getApplicationContext(),"Word limit exceeded", Toast.LENGTH_LONG).show();
        }
        else if (status.equals("")){
            Toast.makeText(getApplicationContext(),"Please enter something",Toast.LENGTH_LONG).show();
        }
        else {
            post(status,URL);
            setContentView(R.layout.main_page);
        }
    }

    public void onRegister (View view) {
        EditText un = (EditText) findViewById(R.id.UsernameReg);
        String username = un.getText().toString();
        EditText pw = (EditText) findViewById(R.id.PasswordReg);
        String password = pw.getText().toString();
        String URL = "https://192.168.0.10/Android/reguser.php";
        try {
            RegisterUser(username,password,URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onLogin (View view){
        EditText un = (EditText) findViewById(R.id.Username);
        String username = un.getText().toString();
        EditText pw = (EditText) findViewById(R.id.Password);
        String password = pw.getText().toString();
        String URL = "https://192.168.0.10/Android/login.php";
        try {
            LoginUser(username,password,URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onPending (View view){
        String url = "https://192.168.0.10/Android/pendingreq.php";
        GetPending(url);
    }

    public void onFeed (View view){
        String url = "https://192.168.0.10/Android/showfeed.php";
        ShowFeed(url);
    }

    public void onYourFriends (View view){
        String url = "https://192.168.0.10/Android/showfriends.php";
        ShowFriends(url);
    }

    private static KeyStore getKeyStore() {
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return keyStore;
    }

    private void initializeDB(){
        class Init extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
            @Override
            protected String doInBackground(Void... voids) {
                String URL2 = "https://192.168.0.10/Android/initialize.php";
                JSONObject json = new JSONObject();
                return connectNotVerify(URL2,json);
            }
        }
        Init x = new Init();
        x.execute();
    }

    private String Encrypt (String message, String alias)  {
        try {
            KeyStore keyStore = getKeyStore();
            SecretKey secretKey;
            if (!keyStore.containsAlias(alias)) {
                final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setRandomizedEncryptionRequired(false)
                        .build();
                keyGenerator.init(keyGenParameterSpec);
                secretKey = keyGenerator.generateKey();
            }
            else{
                secretKey = ((KeyStore.SecretKeyEntry)keyStore.getEntry(alias,null)).getSecretKey();
            }
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(message.getBytes());
            return Base64.encodeToString(cipher.getIV(), Base64.URL_SAFE)+Base64.encodeToString(encrypted, Base64.URL_SAFE);
        }
        catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    private String Decrypt (String message, String alias){
        try {
            KeyStore keyStore = getKeyStore();
            if (!keyStore.containsAlias(alias))
                return alias;
            SecretKey secretKey;
            secretKey = ((KeyStore.SecretKeyEntry)keyStore.getEntry(alias,null)).getSecretKey();
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, Base64.decode((message.substring(0,16)), Base64.URL_SAFE));
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            byte[] decrypted = cipher.doFinal(Base64.decode((message.substring(16)), Base64.URL_SAFE));
            return new String(decrypted);
        }
        catch (Exception e){
            e.printStackTrace();
            return e.toString();
        }
    }

    private boolean SessionFailed (String s){
        if (s == null){
            setContentView(R.layout.login_page);
            Toast.makeText(getApplicationContext(),"Session Expired",Toast.LENGTH_LONG).show();
            msCookieManager.getCookieStore().removeAll();
            return true;
        }
        return false;
    }

    private boolean verifySession () throws JSONException {
        JSONObject json = new JSONObject();
        json.put("username",uname);
        try {
            URL url = new URL("https://192.168.0.10/Android/verifycookie.php");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(VERIFY_LOCALHOST);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                conn.setRequestProperty(COOKIE,
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }
            else {
                return false;
            }
            conn.setDoOutput(true);
            OutputStreamWriter wr;
            try (OutputStream w = conn.getOutputStream()) {
                wr = new OutputStreamWriter(w);
                wr.write(json.toString());
                wr.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json1;
            while ((json1 = bufferedReader.readLine()) != null) sb.append(json1 + "\n");
            String s = sb.toString().trim();
            JSONObject rs = new JSONObject(s);
            if (rs.getString("error").equals("false"))
                return true;
            else
                return false;
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private String connectNotVerify (final String URL2, final JSONObject json){
        try {
            if (!isNetworkAvailable() || !isOnline()){
                return "timeout";
            }
            URL url = new URL(URL2);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(VERIFY_LOCALHOST);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                conn.setRequestProperty(COOKIE,
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }
            conn.setDoOutput(true);
            OutputStreamWriter wr;
            try (OutputStream w = conn.getOutputStream()) {
                wr = new OutputStreamWriter(w);
                wr.write(json.toString());
                wr.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json1;
            while ((json1 = bufferedReader.readLine()) != null) sb.append(json1 + "\n");
            return sb.toString().trim();
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 -w 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException | InterruptedException e)          { e.printStackTrace(); }

        return false;
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private String connect (final String URL2, final JSONObject json){
        try {
            if (!isNetworkAvailable() || !isOnline()){
                return "timeout";
            }
            if (!verifySession()){
                return null;
            }
            URL url = new URL(URL2);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setHostnameVerifier(VERIFY_LOCALHOST);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(1000);
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                conn.setRequestProperty(COOKIE,
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }
            conn.setDoOutput(true);
            OutputStreamWriter wr;
            try (OutputStream w = conn.getOutputStream()) {
                wr = new OutputStreamWriter(w);
                wr.write(json.toString());
                wr.flush();
            }
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }
            StringBuilder sb = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String json1;
            while ((json1 = bufferedReader.readLine()) != null) sb.append(json1 + "\n");
            return sb.toString().trim();
        }
        catch (Exception e){
            return null;
        }
    }

    private void ShowFeedOffline(){
        setContentView(R.layout.own_posts);
        TextView view = (TextView) findViewById(R.id.OwnStatusHead);
        view.setText("Your Feed");
        Toast.makeText(getApplicationContext(),"Offline Mode. Please connect to internet and refresh",Toast.LENGTH_LONG).show();
        String[] status = cache.showFeedStatus();
        String[] timestamp = cache.showFeedTimeStamp();
        String[] username = cache.showFeedUsername();
        String[] res = new String[status.length];
        for (int i = 0; i < status.length; i++){
            res[i] = username[i]+":   "+ Decrypt(status[i],username[i]+timestamp[i])+"   "+timestamp[i];
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, res);
        ListView listView = (ListView) findViewById(R.id.ownstatus);
        listView.setAdapter(arrayAdapter);
    }

    private void ShowFeed (final String URL2){
        class FeedHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equals("timeout")){
                    ShowFeedOffline();
                    return;
                }
                if (SessionFailed(s)){
                    return;
                }
                setContentView(R.layout.own_posts);
                TextView view = (TextView) findViewById(R.id.OwnStatusHead);
                view.setText("Your Feed");
                try {
                    LoadIntoListFeed(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        FeedHelper x = new FeedHelper();
        x.execute();
    }

    private void LoadIntoListFeed (String json) throws JSONException {
        JSONObject js = new JSONObject(json);
        int n = (int) (js.get("size"));
        final String[] status = new String[n - 2];
        for (int i = 0; i < n - 2; i++) {
            JSONObject obj = new JSONObject(js.getString(String.valueOf(i)));
            status[i] = obj.getString("uname") + ":    " + obj.getString("status") + "   " + obj.getString("time");
            cache.addPost(Encrypt(obj.getString("status"),obj.getString("uname")+obj.getString("time")),obj.getInt("uid"),obj.getString("time"),obj.getString("uname"),obj.getInt("statusid"));
            //Toast.makeText(getApplicationContext(),status[i],Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, status);
        ListView listView = (ListView) findViewById(R.id.ownstatus);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(MainActivity.this, status[position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowFriendsOffline (){
        setContentView(R.layout.your_friends);
        Toast.makeText(getApplicationContext(),"Offline Mode. Please connect to internet and refresh",Toast.LENGTH_LONG).show();
        final String[] names = cache.getFriends();
        final int[] ID = cache.getFriendsID();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView) findViewById(R.id.searchlist);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getOwnPostOffline(ID[position],names[position]);
                //Toast.makeText(MainActivity.this, status[position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void ShowFriends (final String URL2){
        class FriendHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equals("timeout")){
                    ShowFriendsOffline();
                    return;
                }
                if (SessionFailed(s)){
                    return;
                }
                setContentView(R.layout.your_friends);
                try {
                    LoadIntoListSearch(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        FriendHelper x = new FriendHelper();
        x.execute();
    }

    private void ShowPendingOffline (){
        setContentView(R.layout.pending_requests);
        Toast.makeText(getApplicationContext(),"Offline Mode. Please connect to internet and refresh",Toast.LENGTH_LONG).show();
        String[] names = cache.getPendingFriends();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView) findViewById(R.id.pendinglist);
        listView.setAdapter(arrayAdapter);
    }

    private void GetPending (final String URL2){
        class PendingHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                if (s.equals("timeout")){
                    ShowPendingOffline();
                    return;
                }
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                setContentView(R.layout.pending_requests);
                try {
                    LoadIntoListPending(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        PendingHelper x = new PendingHelper();
        x.execute();
    }

    private void LoadIntoListPending (String json) throws JSONException {
        JSONObject js = new JSONObject(json);
        int n = (int) (js.get("size"));
        final String[] names = new String[n - 2];
        final int[] ids = new int[n-2];
        for (int i = 0; i < n - 2; i++) {
            JSONObject obj = new JSONObject(js.getString(String.valueOf(i)));
            names[i] = obj.getString("name");
            ids[i] = obj.getInt("id");
            cache.addFriendPending(obj.getString("name"),obj.getInt("id"));
            //Toast.makeText(getApplicationContext(),status[i],Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView) findViewById(R.id.pendinglist);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Confirm");
                builder.setMessage("Accept Friend Request?");
                builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AcceptDeleteReq(ids[position],"https://192.168.0.10/Android/acceptreq.php");
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AcceptDeleteReq(ids[position],"https://192.168.0.10/Android/deletereq.php");
                        dialog.dismiss();
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    private void AcceptDeleteReq (final int fid, final String URL2){
        class ADRHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                try {
                    JSONObject js = new JSONObject(s);
                    Toast.makeText(getApplication(),js.getString("message"),Toast.LENGTH_LONG).show();
                    setContentView(R.layout.pending_requests);
                    String url = "https://192.168.0.10/Android/pendingreq.php";
                    GetPending(url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("fid",fid);
                    json.put("id",uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        ADRHelper x = new ADRHelper();
        x.execute();
    }

    private void LoadIntoList (String json, int usid, String uname) throws JSONException {
            JSONObject js = new JSONObject(json);
            int n = (int) (js.get("size"));
            final String[] status = new String[n - 2];
            for (int i = 0; i < n - 2; i++) {
                JSONObject obj = new JSONObject(js.getString(String.valueOf(i)));
                status[i] = obj.getString("status") + "   " + obj.getString("time");
                cache.addPost(obj.getString("status"),usid,obj.getString("time"),uname,obj.getInt("statusid"));
                //Toast.makeText(getApplicationContext(),status[i],Toast.LENGTH_LONG).show();
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, status);
            ListView listView = (ListView) findViewById(R.id.ownstatus);
            listView.setAdapter(arrayAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //Toast.makeText(MainActivity.this, status[position], Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void getOwnPostOffline(int usid, String uname){
        setContentView(R.layout.own_posts);
        if (usid != uid){
            TextView view = (TextView) findViewById(R.id.OwnStatusHead);
                view.setText(uname+"'s Posts");
        }
        Toast.makeText(getApplicationContext(),"Offline Mode. Please connect to internet and refresh",Toast.LENGTH_LONG).show();
        String[] status = cache.getOwnPostStatus(usid);
        String[] timestamp = cache.getOwnPostTimestamp(usid);
        String[] res = new String[status.length];
        for (int i = 0; i < status.length; i++) {
            res[i] = Decrypt(status[i], uname + timestamp[i]) + "   " + timestamp[i];
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, res);
        ListView listView = (ListView) findViewById(R.id.ownstatus);
        listView.setAdapter(arrayAdapter);
    }

    private void getOwnPost (final int usid, final String URL2, final String uname){
        class OwnPostHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s.equals("timeout")){
                    getOwnPostOffline(usid,uname);
                    return;
                }
                if (SessionFailed(s)){
                    return;
                }
                setContentView(R.layout.own_posts);
                if (usid != uid){
                    TextView view = (TextView) findViewById(R.id.OwnStatusHead);
                    view.setText(uname+"'s Posts");
                }
                try {
                    LoadIntoList(s,usid,uname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",usid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        OwnPostHelper x = new OwnPostHelper();
        x.execute();
    }

    private void AddFriend (final int usid, final String URL2){
        class AddFriendHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                try {
                    JSONObject js = new JSONObject(s);
                    Toast.makeText(getApplicationContext(),js.getString("message"),Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                    json.put("fid",usid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        AddFriendHelper x = new AddFriendHelper();
        x.execute();
    }

    private void onSearchClick (final int id, final String URL2, final String uname){
        class AfterSearchHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                try {
                    JSONObject js = new JSONObject(s);
                    int res = js.getInt("ans");
                    if (res == 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to send a friend request?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                AddFriend(id,"https://192.168.0.10/Android/addfriend.php");
                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    else if (res == 1){
                        Toast.makeText(getApplicationContext(),js.getString("message"),Toast.LENGTH_LONG).show();
                    }
                    else {
                        String URLs = "https://192.168.0.10/Android/showownposts.php";
                        getOwnPost(id,URLs,uname);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                    json.put("fid",id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        AfterSearchHelper x = new AfterSearchHelper();
        x.execute();
    }

    private void LoadIntoListSearch (String json) throws JSONException {
        JSONObject js = new JSONObject(json);
        int n = (int) (js.get("size"));
        if (n == 2)
            Toast.makeText(getApplicationContext(),"No User Found",Toast.LENGTH_LONG).show();
        final String[] names = new String[n - 2];
        final int[] ids = new int[n-2];
        for (int i = 0; i < n - 2; i++) {
            JSONObject obj = new JSONObject(js.getString(String.valueOf(i)));
            names[i] = obj.getString("name");
            ids[i] = obj.getInt("id");
            cache.addFriend(names[i],ids[i]);
            //Toast.makeText(getApplicationContext(),status[i],Toast.LENGTH_LONG).show();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        ListView listView = (ListView) findViewById(R.id.searchlist);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onSearchClick(ids[position], "https://192.168.0.10/Android/checkfriends.php", names[position]);
                //Toast.makeText(MainActivity.this, status[position], Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SearchUser (final String key, final String URL2){
        class SearchHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                try {
                    LoadIntoListSearch(s);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("key","%"+key+"%");
                    json.put("id",uid);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        SearchHelper x = new SearchHelper();
        x.execute();
    }

    private void post (final String status, final String URL2){
        class PostHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                if (s.equals("timeout")){
                    Toast.makeText(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                super.onPostExecute(s);
                if (SessionFailed(s)){
                    return;
                }
                try {
                    JSONObject js = new JSONObject(s);
                    Toast.makeText(getApplicationContext(),js.getString("message"),Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id",uid);
                    json.put("status",status);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connect(URL2,json);
            }
        }
        PostHelper x = new PostHelper();
        x.execute();
    }

    private void AddSession (final String URL2){
        class SessionHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username",uname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connectNotVerify(URL2,json);
            }
        }
        SessionHelper x = new SessionHelper();
        x.execute();
    }

    private void getID (final String username, final String URL2){
        class getIDHelper extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                try {
                    JSONObject js = new JSONObject(s);
                    uid = Integer.parseInt(js.getString("id"));
                    AddSession("https://192.168.0.10/Android/storesession.php");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username",username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connectNotVerify(URL2,json);
            }
        }
        getIDHelper x = new getIDHelper();
        x.execute();
    }
    private void AddUserToMain (final String username, final String URL2){
        class Add extends AsyncTask<Void,Void,String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username",username);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connectNotVerify(URL2,json);
            }
        }
        Add x = new Add();
        x.execute();
    }

    private void RegisterUser (final String username, final String password, final String URL2) {
        class Reg extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s) {
                if (s.equals("timeout")){
                    Toast.makeText(getApplicationContext(), "Internet not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                super.onPostExecute(s);
                try {
                    JSONObject x = new JSONObject(s);
                    if (x.getString("error").equals("false")){
                        AddUserToMain(username,"https://192.168.0.10/Android/addusertomain.php");
                    }
                    Toast.makeText(getApplicationContext(), x.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username",username);
                    json.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connectNotVerify(URL2,json);
            }
        }
        Reg reguser = new Reg();
        reguser.execute();
    }

    private void LoginUser (final String username, final String password, final String URL2) {
        class Log extends AsyncTask<Void,Void,String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s) {
                if (s.equals("timeout")){
                    Toast.makeText(getApplicationContext(),"Internet not available", Toast.LENGTH_SHORT).show();
                    return;
                }
                super.onPostExecute(s);
                try {
                    JSONObject x = new JSONObject(s);
                    if (x.getString("error").equals("false")){
                        uname = username;
                        getID(username,"https://192.168.0.10/Android/getid.php");
                        setContentView(R.layout.main_page);
                        cache.refresh();
                    }
                    Toast.makeText(getApplicationContext(), x.getString("message"), Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            protected String doInBackground(Void... voids) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username",username);
                    json.put("password",password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return connectNotVerify(URL2,json);
            }
        }
        Log reguser = new Log();
        reguser.execute();
    }
}