package com.krito.io.rscout.activities;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.krito.io.rscout.pojo.ActionItem;
import com.krito.io.rscout.recievers.ConnectivityReceiver;
import com.krito.io.rscout.views.CustomTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import id.arieridwan.lib.PageLoader;
import io.rmiri.buttonloading.ButtonLoading;

public class Answer extends AppCompatActivity {

    private static final int QUESTION = 0;
    private static final int ANSWER = 1;
    private static final int SUBMITTING = 2;

    ArrayList<ActionItem> items;
    ArrayList<String> questionIds;
    ArrayList<String> answers;
    String userId;

    RecyclerView recyclerView;
    ActionRecyclerAdapter adapter;
    //PageLoader loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer);

        //loader = findViewById(R.id.pageLoader);
        recyclerView = findViewById(R.id.scoutsRecView);

        userId = getSharedPreferences(AppConfig.SHARED_PREFERENCE_NAME, MODE_PRIVATE)
                .getString(AppConfig.LOGGED_IN_USER_ID_SHARED, null);

        items = (ArrayList<ActionItem>) getIntent().getExtras().getSerializable("items");
        answers = (ArrayList<String>) getIntent().getExtras().getSerializable("answers");
        questionIds = (ArrayList<String>) getIntent().getExtras().getSerializable("questionIds");

        if (items != null && items.size() > 0){
            ActionItem item = new ActionItem();
            item.setType(ActionItem.SUBMITTING);
            items.add(item);
        }

        updateUi();
    }

    private class QuestionViewHolder extends RecyclerView.ViewHolder{

        CustomTextView question;

        public QuestionViewHolder(View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.questionView);
        }

        public void bind(ActionItem item, int position){
            if (item.getQuestionString() != null) {
                question.setText(item.getQuestionString());
                questionIds.add(position, String.valueOf(item.getQuestionId()));
            }
        }
    }

    private class AnswerViewHolder extends RecyclerView.ViewHolder{

        RadioGroup radioGroup;

        public AnswerViewHolder(View itemView) {
            super(itemView);

            radioGroup = itemView.findViewById(R.id.radioGroup);
        }

        public void bind(ActionItem item, int position){
            answers.set(position, "na");

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

                answers.set(position, r);
                items.get(position).setAnswerString(r);

                for (String rr: answers) {
                    Log.i("action_result", "bind: " + rr);
                }
            });
        }
    }

    private class SubmittingViewHolder extends RecyclerView.ViewHolder{

        ButtonLoading done;

        public SubmittingViewHolder(View itemView) {
            super(itemView);

            done = itemView.findViewById(R.id.doneBtn);

//            done.setOnClickListener(v -> {
//                Log.i("answer_pressed", "SubmittingViewHolder: ");
//                //loader.startProgress();
//                preformSubmit();
//            });

            done.setOnButtonLoadingListener(new ButtonLoading.OnButtonLoadingListener() {
                @Override
                public void onClick() {
                    Log.i("answer_pressed", "SubmittingViewHolder: ");
                    //loader.startProgress();
                    preformSubmit();
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        }
    }

    private class ActionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == QUESTION){
                View view = LayoutInflater.from(Answer.this).inflate(R.layout.question_card, parent, false);
                return new QuestionViewHolder(view);
            } else if (viewType == ANSWER) {
                View view = LayoutInflater.from(Answer.this).inflate(R.layout.answer_card, parent, false);
                return new AnswerViewHolder(view);
            } else {
                View view = LayoutInflater.from(Answer.this).inflate(R.layout.submit_view, parent, false);
                return new SubmittingViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof QuestionViewHolder) {
                QuestionViewHolder pHolder = (QuestionViewHolder) holder;
                pHolder.bind(items.get(position), position);
            } else if (holder instanceof AnswerViewHolder) {
                AnswerViewHolder pHolder = (AnswerViewHolder) holder;
                pHolder.bind(items.get(position), position);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position).getType() == ActionItem.QUESTION){
                return QUESTION;
            } else if (items.get(position).getType() == ActionItem.ANSWER) {
                return ANSWER;
            } else {
                return SUBMITTING;
            }
        }
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

    private void preformSubmit(){
        String url = "https://rezetopia.com/Apis/challenges";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //loader.stopProgress();

                        Log.i("submit_scout", "onResponse: " + response);
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){
                                onBackPressed();
                            } else {

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //loader.stopProgress();
                Log.i("submit_scout_error", "onErrorResponse: " + error.getMessage());
                updateUi();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<>();

                map.put("user_id", userId);

                int questionsSize = 0;
                int iCount = 0;
                for (ActionItem item:items) {
                    if (item.getType() == ActionItem.QUESTION) {
                        map.put("question_id" + String.valueOf(questionsSize), String.valueOf(item.getQuestionId()));
                        Log.i("params", "getParams: " + questionsSize);
                        Log.i("params_question_id", "getParams: " + String.valueOf(item.getQuestionId()));
                        int answerSize = item.getAttempts();
                        map.put("answer_size" + String.valueOf(questionsSize), String.valueOf(answerSize));
                        Log.i("params_answer_size", "getParams: " + answerSize);
                        ArrayList<String> tempAnswers = new ArrayList<>(answers.subList(iCount + 1, iCount + 1 + item.getAttempts()));
                        for (int i = 0; i < answerSize; i++) {
                            map.put("answer_" + questionsSize + "_" + String.valueOf(i), tempAnswers.get(i));
                            Log.i("params_answer_" + questionsSize + "_" + String.valueOf(i), "getParams: " + tempAnswers.get(i));
                        }
                        questionsSize++;
                    }
                    iCount ++;
                }
                map.put("questions_size", String.valueOf(questionsSize));

                return map;
            }
        };

        Volley.newRequestQueue(getApplicationContext()).add(request);
    }
}
