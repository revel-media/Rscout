package com.krito.io.rscout.activities;

import android.content.Intent;
import android.net.NetworkInfo;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.andrognito.flashbar.Flashbar;
import com.andrognito.flashbar.anim.FlashAnimBarBuilder;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.krito.io.rscout.R;
import com.krito.io.rscout.application.AppConfig;
import com.krito.io.rscout.recievers.ConnectivityReceiver;
import com.krito.io.rscout.views.CustomButton;
import com.krito.io.rscout.views.CustomEditText;
import com.krito.io.rscout.views.CustomTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.rmiri.buttonloading.ButtonLoading;

public class Login extends AppCompatActivity {

    RelativeLayout loginLayout;
    CustomEditText emailView;
    CustomEditText passwordView;
    CustomTextView forgetPassword;
    ButtonLoading buttonLoading;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        networkListener();

        loginLayout = findViewById(R.id.loginLayout);
        emailView = findViewById(R.id.emailView);
        passwordView = findViewById(R.id.passwordView);
        forgetPassword = findViewById(R.id.forgetPassword);
        buttonLoading = findViewById(R.id.btnSignIn);


        buttonLoading.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
            @Override
            public void onClick() {
                if (ConnectivityReceiver.isConnected(Login.this)){
                    if (validCredentials()) {
                        login(emailView.getText().toString(), passwordView.getText().toString());
                    } else {
                        String alert = getResources().getString(R.string.wrong_login);
                        Snackbar.make(loginLayout, alert, Snackbar.LENGTH_INDEFINITE).show();
                        buttonLoading.setProgress(false);
                    }
                }  else {
                    Snackbar.make(loginLayout, R.string.checkingNetwork, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                }
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFinish() {

            }
        });
    }

    private boolean validCredentials(){
        if (!emailView.getText().toString().isEmpty() && !passwordView.getText().toString().isEmpty()){
            return true;
        }

        return false;
    }

    private void login(String email, String password){
        String url = "https://rezetopia.com/Apis/login";
        StringRequest post = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("login_response", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    if (!jsonResponse.getBoolean("error")){
                        Log.e("login_response", response);
                        getSharedPreferences(AppConfig.SHARED_PREFERENCE_NAME, MODE_PRIVATE)
                                .edit().putString(AppConfig.LOGGED_IN_USER_ID_SHARED, jsonResponse.getString("id")).apply();
                        startActivity(new Intent(Login.this, Search.class));
                        finish();
                    } else {
                        String s = getResources().getString(R.string.wrong_login);
                        Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                        buttonLoading.setProgress(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("login_error", "onErrorResponse: " + error.getMessage());
                if (error instanceof NetworkError) {
                    String s = getResources().getString(R.string.checkingNetwork);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                } else if (error instanceof ServerError) {
                    String s = getResources().getString(R.string.server_error);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                } else if (error instanceof AuthFailureError) {
                    String s = getResources().getString(R.string.connection_error);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                } else if (error instanceof ParseError) {
                    String s = getResources().getString(R.string.parsing_error);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                } else if (error instanceof NoConnectionError) {
                    String s = getResources().getString(R.string.connection_error);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                } else if (error instanceof TimeoutError) {
                    String s = getResources().getString(R.string.time_out);
                    Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                    buttonLoading.setProgress(false);
                }

                String s = getResources().getString(R.string.time_out);
                Snackbar.make(loginLayout, s, Snackbar.LENGTH_INDEFINITE).show();
                buttonLoading.setProgress(false);
            }
        }
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                //params.put("login", "login");
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(post);
    }

    private void networkListener(){
        ReactiveNetwork.observeNetworkConnectivity(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    if (connectivity.getState() == NetworkInfo.State.CONNECTED){
                        Log.i("internetC", "onNext: " + "Connected");
                    } else if (connectivity.getState() == NetworkInfo.State.SUSPENDED){
                        Log.i("internetC", "onNext: " + "LowNetwork");
                    } else {
                        Log.i("internetC", "onNext: " + "NoInternet");
                        Flashbar.Builder builder = new Flashbar.Builder(this);
                        builder.gravity(Flashbar.Gravity.BOTTOM)
                                .backgroundColor(R.color.red2)
                                .enableSwipeToDismiss()
                                .message(R.string.checkingNetwork)
                                .enterAnimation(new FlashAnimBarBuilder(Login.this).slideFromRight().duration(200))
                                .build().show();
                    }
                });
    }
}
