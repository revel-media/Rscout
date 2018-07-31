package com.krito.io.rscout.activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.krito.io.rscout.R;
import com.krito.io.rscout.application.AppConfig;
import com.krito.io.rscout.helper.VolleyCustomRequest;
import com.krito.io.rscout.pojo.Action;
import com.krito.io.rscout.pojo.Scout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.arieridwan.lib.PageLoader;

public class Search extends AppCompatActivity {

    private static final int QUESTION = 0;
    private static final int ANSWER = 1;

    ArrayList<Action> actions;
    ArrayList<String> actionIds;
    ArrayList<String> actionsResult;
    Map<String, String> result = new HashMap<>();
    String userId;

    RecyclerView recyclerView;
    ActionRecyclerAdapter adapter;
    ProgressBar progressBar;
    EditText search;
    LinearLayout userInfoLayout;
    CircleImageView userPp;
    TextView username;
    TextView done;
    PageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressBar = findViewById(R.id.scoutProgress);
        recyclerView = findViewById(R.id.scoutsRecView);
        search = findViewById(R.id.search);
        userInfoLayout = findViewById(R.id.userInfo);
        userPp = findViewById(R.id.userPpView);
        username = findViewById(R.id.usernameView);
        done = findViewById(R.id.doneView);
        loader = findViewById(R.id.pageLoader);

        userId = getSharedPreferences(AppConfig.SHARED_PREFERENCE_NAME, MODE_PRIVATE)
                .getString(AppConfig.LOGGED_IN_USER_ID_SHARED, null);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    performGetScouting(s.toString());
                } else {
                    if (actions != null) {
                        actions.clear();
                        actionIds.clear();
                        actionsResult.clear();
                        userInfoLayout.setVisibility(View.GONE);
                        updateUi();
                        done.setEnabled(false);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        done.setOnClickListener(v -> {
            loader.startProgress();
            preformSubmit();
        });

        if (userId == null || userId.isEmpty()){
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }

    private class QuestionViewHolder extends RecyclerView.ViewHolder{

        TextView question;
        RadioGroup radioGroup;

        public QuestionViewHolder(View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.questionView);
            radioGroup = itemView.findViewById(R.id.radioGroup);
        }

        public void bind(Action action, int position){
            if (action.getAction() != null) {
                question.setText(action.getAction());

                result.put(String.valueOf(action.getId()), "na");

                actionIds.add(position, String.valueOf(action.getId()));
                actionsResult.add(position, "na");

                radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    RadioButton tempRadio = itemView.findViewById(checkedId);
                    String r;
                    if (tempRadio.getText().toString().contentEquals(getResources().getString(R.string.yes))){
                        r = "yes";
                    } else if (tempRadio.getText().toString().contentEquals(getResources().getString(R.string.no))){
                        r = "no";
                    } else {
                        r = "na";
                    }

                    actionIds.set(position, String.valueOf(action.getId()));
                    actionsResult.set(position, r);

                    for (String rr:actionsResult) {
                        Log.i("action_result", "bind: " + rr);
                    }

                    result.put(String.valueOf(action.getId()), r);
                });
            }
        }
    }

    private class AnswerViewHolder extends RecyclerView.ViewHolder{

        RadioGroup radioGroup;

        public AnswerViewHolder(View itemView) {
            super(itemView);

            radioGroup = itemView.findViewById(R.id.radioGroup);
        }
    }

    private class ActionRecyclerAdapter extends RecyclerView.Adapter<QuestionViewHolder> {

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(Search.this).inflate(R.layout.scout_card, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            holder.bind(actions.get(position), position);
        }

        @Override
        public int getItemCount() {
            return actions.size();
        }
    }

    private void performGetScouting(String q){
        String url = "https://rezetopia.com/Apis/challenges?code_id=" + q;

        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.GET, url, Scout.class,
                new Response.Listener<Scout>() {
                    @Override
                    public void onResponse(Scout response) {
                        progressBar.setVisibility(View.GONE);
                        if (!response.isError()) {
                            if (response.getActions() != null && response.getActions().length > 0){
                                Log.i("scout_response", "onResponse: ");
                                actions = new ArrayList<>(Arrays.asList(response.getActions()));
                                if (actions.size() > 0){
                                    done.setEnabled(true);
                                    actionIds = new ArrayList<>();
                                    actionsResult = new ArrayList<>();
                                    userInfoLayout.setVisibility(View.VISIBLE);
                                } else {
                                    userInfoLayout.setVisibility(View.GONE);
                                    done.setEnabled(false);
                                }

                                if (response.getUsername() != null) {
                                    username.setText(response.getUsername());
                                }

                                if (response.getImgUrl() != null) {
                                    Picasso.with(Search.this).load(response.getImgUrl()).into(userPp);
                                }
                                updateUi();
                            }
                        } else if (response.isError()) {

                        } else {

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("scout_error", "onErrorResponse: " + error.getMessage());
                progressBar.setVisibility(View.GONE);
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void preformSubmit(){
        String url = "https://rezetopia.com/Apis/challenges";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loader.stopProgress();
                        Log.i("submit_scout", "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){

                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loader.stopProgress();
                Log.i("submit_scout_error", "onErrorResponse: " + error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();

                map.put("user_id", userId);

                if (actions != null && actions.size() > 0){
                    map.put("questions_size", String.valueOf(actions.size()));

                    for (int i = 0; i < actions.size(); i ++){
                        map.put("question_id" + String.valueOf(i), String.valueOf(actions.get(i).getId()));
                        map.put("answer_size" + String.valueOf(i), String.valueOf(actions.get(i).getAttempt()));
                        for (int j = 0; j < actions.get(i).getAttempt(); j++){
                            map.put("answer_" + String.valueOf(i) + "_" + String.valueOf(j), actionsResult.get(i));
                        }
                    }
                }

                return map;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

    private void updateUi(){
        if (adapter == null){
            adapter = new ActionRecyclerAdapter();
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } else {
            adapter.notifyDataSetChanged();
        }
    }
}
