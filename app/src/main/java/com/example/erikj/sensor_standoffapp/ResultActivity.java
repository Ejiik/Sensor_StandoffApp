package com.example.erikj.sensor_standoffapp;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class ResultActivity extends AppCompatActivity {

    private TextView tvSTime;
    private TextView tvETime;
    private TextView tvTime;
    private TextView tvReact;
    private TextView tvAcc;
    private TextView tvResult;
    private TextView tvPlayerTwoPoints;
    private TextView tvOppPoints;
    private EditText etName;
    private TextView tvWinOrLose;

    private DBHandler dbHandler;

    private Context context;

    private long time;
    private long react;
    private double acc;

    private Button btnMenu;
    private Button btnSaveScore;

    private int totalPoints;
    private int opponentsPoints;
    private int finalResult;

    private Score score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        context = getApplicationContext();

        tvTime = (TextView) findViewById(R.id.tvTime);
        tvSTime = (TextView) findViewById(R.id.tvStartTime);
        tvETime = (TextView) findViewById(R.id.tvEndTime);
        tvAcc = (TextView) findViewById(R.id.tvAcc);
        tvReact = (TextView) findViewById(R.id.tvReac);
        tvResult = (TextView) findViewById(R.id.tvResult);
        etName = (EditText) findViewById(R.id.etName);
        tvWinOrLose = (TextView) findViewById(R.id.tvWinOrLose);
        tvPlayerTwoPoints = (TextView) findViewById(R.id.tvPlayerTwoPoints);
        tvOppPoints = (TextView) findViewById(R.id.tvOppPoints);

        btnMenu = (Button) findViewById(R.id.btnMenu);
        btnSaveScore = (Button) findViewById(R.id.btnSaveScore);
        btnSaveScore.setEnabled(true);
        etName.setEnabled(true);

        Intent intent = getIntent();

        dbHandler = new DBHandler(this);

        time = intent.getLongExtra("totalTime", 0);
        react = intent.getLongExtra("reaction", 0);
        acc = intent.getDoubleExtra("accurate", 0);
        totalPoints = intent.getIntExtra("myPoints", 0);
        opponentsPoints = intent.getIntExtra("hostPoints", 0);

        tvSTime.setText(Long.toString(intent.getLongExtra("timeStart", 0)));
        tvETime.setText(Long.toString(intent.getLongExtra("timeEnd", 0)));
        tvTime.setText(Long.toString(time));       //the draw time
        tvReact.setText(Long.toString(react));
        tvAcc.setText(Double.toString(acc));
        tvPlayerTwoPoints.setText(Integer.toString(opponentsPoints));

        if (intent.getBooleanExtra("multiplayer", false)){
            updatePlayerTwo(intent);
        } else{
            tvOppPoints.setVisibility(View.INVISIBLE);
            tvPlayerTwoPoints.setVisibility(View.INVISIBLE);
        }

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toMainMenu();
            }
        });

        Log.d("DeviceListActivity", String.valueOf(totalPoints));
        tvResult.setText(Integer.toString(totalPoints));

        btnSaveScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!etName.getText().toString().equals("")) {

                    String cal = Calendar.getInstance().getTime().toString();
                    String date = cal.substring(3, 16) + " " + cal.substring(29);

                    score = new Score(totalPoints, etName.getText().toString(), date);

                    dbHandler.addScore(score);
                    btnSaveScore.setEnabled(false);
                    etName.setEnabled(false);
                    Toast.makeText(context, "Score saved!", Toast.LENGTH_SHORT).show();

                } else{
                    Toast.makeText(context, "You must enter a name if you want to save your score.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public void updatePlayerTwo(Intent intent){
        Log.d("UPDATEPLAYERTWO", "KOMMER JAG HIT?");
        //int opponentsPoints = intent.getIntExtra("opponentsPoints", 0);
        int result = intent.getIntExtra("finalResult", 0);
        //tvPlayerTwoPoints.setText(opponentsPoints);
        switch (result){
            case Constants.YOU_WON:
                tvWinOrLose.setText("YOU WIN!");
                break;
            case Constants.YOU_LOST:
                tvWinOrLose.setText("YOU LOSE!");
                break;
            case Constants.DRAW:
                tvWinOrLose.setText("IT'S A DRAW!");
                break;
        }
        /* P2TotalPoints = intent.getIntExtra("totalPoints", 10);
        tvPlayerTwoPoints.setText(Integer.toString(P2TotalPoints));

        if (P2TotalPoints < totalPoints){
            tvWinOrLose.setText(R.string.tvWin);
        }else if(P2TotalPoints > totalPoints){
            tvWinOrLose.setText(R.string.tvLose);
        }else if(P2TotalPoints == totalPoints){
            tvWinOrLose.setText(R.string.tvDraw);
        }
        */
    }

    @Override
    public void onBackPressed(){        //makes the back button take you to main menu
        toMainMenu();
    }

    public void toMainMenu(){
        Intent intent = new Intent(ResultActivity.this, MainActivity.class);
        startActivity(intent);
    }
}
