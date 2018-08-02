package com.krito.io.rscout.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.krito.io.rscout.R;
import com.krito.io.rscout.application.AppConfig;
import com.krito.io.rscout.helper.VolleyCustomRequest;
import com.krito.io.rscout.pojo.Action;
import com.krito.io.rscout.pojo.ActionItem;
import com.krito.io.rscout.pojo.Scout;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import id.arieridwan.lib.PageLoader;
import ru.whalemare.sheetmenu.SheetMenu;

public class Search extends AppCompatActivity {

    private static final int QUESTION = 0;
    private static final int ANSWER = 1;

    ArrayList<ActionItem> items;
    ArrayList<String> questionIds;
    ArrayList<String> answers;
    String userId;



    ProgressBar progressBar;
    EditText search;
    FrameLayout userInfoLayout;
    ImageView userPp;
    TextView username;
    TextView start;
    ImageView more;
    ImageView searchIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressBar = findViewById(R.id.scoutProgress);
        search = findViewById(R.id.search);
        userInfoLayout = findViewById(R.id.userInfoLayout);
        userPp = findViewById(R.id.userPpView);
        username = findViewById(R.id.usernameView);
        start = findViewById(R.id.start);
        more = findViewById(R.id.moreView);
        searchIcon = findViewById(R.id.searchIcon);

        userId = getSharedPreferences(AppConfig.SHARED_PREFERENCE_NAME, MODE_PRIVATE)
                .getString(AppConfig.LOGGED_IN_USER_ID_SHARED, null);

        progressBar.getIndeterminateDrawable().setColorFilter(getResources()
                .getColor(R.color.colorAccent), PorterDuff.Mode.SRC_IN);

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() > 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    searchIcon.setVisibility(View.GONE);
                    performGetScouting(s.toString());
                } else {
                    userInfoLayout.setVisibility(View.GONE);
                    Search.this.start.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        more.setOnClickListener(v -> {
            SheetMenu.with(Search.this)
                    .setMenu(R.menu.search_menu)
                    .setAutoCancel(true)
                    .setClick(item -> {
                        switch (item.getItemId()){
                            case R.id.logout:
                                getSharedPreferences(AppConfig.SHARED_PREFERENCE_NAME, MODE_PRIVATE)
                                        .edit().putString(AppConfig.LOGGED_IN_USER_ID_SHARED, null).apply();
                                startActivity(new Intent(this, Login.class));
                                finish();
                                return false;
                                default:
                                    return false;
                        }
                    }).show();
        });

        start.setOnClickListener(v -> {
            if (items != null && items.size() > 0){
                Intent intent = new Intent(Search.this, Answer.class);
                intent.putExtra("items", items);
                intent.putExtra("answers", answers);
                intent.putExtra("questionIds", questionIds);
                startActivity(intent);
            }
        });

        if (userId == null || userId.isEmpty()){
            startActivity(new Intent(this, Login.class));
            finish();
        }
    }


    private void performGetScouting(String q){
        String url = "https://rezetopia.com/Apis/challenges?code_id=" + q;

        VolleyCustomRequest request = new VolleyCustomRequest(Request.Method.GET, url, Scout.class,
                new Response.Listener<Scout>() {
                    @Override
                    public void onResponse(Scout response) {
                        progressBar.setVisibility(View.GONE);
                        searchIcon.setVisibility(View.VISIBLE);
                        if (!response.isError()) {
                            if (response.getActions() != null && response.getActions().length > 0){
                                Log.i("scout_response", "onResponse: ");
                                ArrayList<Action> tmpActions = new ArrayList<>(Arrays.asList(response.getActions()));
                                items = new ArrayList<>();

                                for (Action action:tmpActions) {
                                    ActionItem item = new ActionItem();
                                    item.setType(ActionItem.QUESTION);
                                    item.setAttempts(action.getAttempt());
                                    item.setQuestionId(action.getId());
                                    item.setQuestionString(action.getAction());
                                    items.add(item);

                                    for (int i = 0; i < action.getAttempt(); i++){
                                        ActionItem item1 = new ActionItem();
                                        item1.setType(ActionItem.ANSWER);
                                        item1.setAnswerString("na");
                                        items.add(item1);
                                    }
                                }

                                if (tmpActions.size() > 0){
                                    start.setEnabled(true);
                                    questionIds = new ArrayList<>(items.size());
                                    answers = new ArrayList<>(items.size());
                                    for (int i = 0; i < items.size(); i++) {
                                        answers.add(i , "na");
                                        questionIds.add(i, "0");
                                    }
                                    userInfoLayout.setVisibility(View.VISIBLE);
                                } else {
                                    userInfoLayout.setVisibility(View.GONE);
                                    start.setEnabled(false);
                                }

                                if (response.getUsername() != null) {
                                    username.setText(response.getUsername());
                                }

                                if (response.getImgUrl() != null) {
                                    Picasso.with(Search.this).load(response.getImgUrl()).into(userPp);
                                }
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
                searchIcon.setVisibility(View.VISIBLE);
            }
        });

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }

}
