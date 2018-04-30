package edu.edu.rosehuman.alumbajt.exam3substate;

import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;

import edu.rosehulman.me435.AccessoryActivity;
import edu.rosehulman.me435.FieldGps;
import edu.rosehulman.me435.FieldGpsListener;
import edu.rosehulman.me435.FieldOrientationListener;


public class MainActivity extends AccessoryActivity implements FieldGpsListener {

    private TextView mHighLevelStateTextView;
    private TextView mMissionSubstateTextView;
    private TextView mGPSTextView;
    private TextView mTargetXYTextView;
    private TextView mTurnAmountTextView;
    private TextView mCommandTextView;
    private TextView mLeftBallTextView;
    private TextView mMiddleBallTextView;
    private TextView mRightBallTextView;

    private long mStateStartTime;
    private long mSubStateTime;
    private int mGPSCounter = 0;

    @Override
    public void onLocationChanged(double x, double y, double heading, Location location) {
        mGPSCounter++;
        mGPSTextView.setText("( " + (int) x + ", " + (int) y + ") " + (int) heading + "° " + mGPSCounter);
    }


    enum State {
        READY_FOR_MISSION,
        INITIAL_STRAIGHT,
        NEAR_BALL_MISSION,
        FAR_BALL_MISSION,
        HOME_CONE_MISSION,
        WAITING_FOR_PICKUP,
        SEEKING_HOME
    }

    enum subState {
        INACTIVE,
        GPS_SEEKING,
        IMAGE_REC_SEEKING,
        OPTIONAL_SCRIPT
    }

    private State mState = State.READY_FOR_MISSION;
    private subState mSubState = subState.INACTIVE;
    private Timer mTimer;
    public static final int LOOP_INTERVAL_MS = 100;
    private Handler mCommandHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setState(State.READY_FOR_MISSION);
        setSubState(subState.INACTIVE);

        mHighLevelStateTextView = findViewById(R.id.highLevelSubstateTextView);
        mMissionSubstateTextView = findViewById(R.id.missionSubstateTextView);
        mGPSTextView = findViewById(R.id.gPSTextView);
        mTargetXYTextView = findViewById(R.id.targetXYTextView);
        mTurnAmountTextView = findViewById(R.id.turnAmountTextView);
        mCommandTextView = findViewById(R.id.commandTextView);
        mLeftBallTextView = findViewById(R.id.leftBallTextView);
        mMiddleBallTextView = findViewById(R.id.middleBallTextView);
        mRightBallTextView = findViewById(R.id.rightBallTextView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        loop();
                    }
                });
            }
        }, 0, LOOP_INTERVAL_MS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mTimer.cancel();
        mTimer = null;
    }

    public void loop() {
        mHighLevelStateTextView.setText(mState.name() + " " + getStateTimeMS() / 1000);
        if (mSubState == subState.INACTIVE) {
            mMissionSubstateTextView.setText("---");
        } else {
            mMissionSubstateTextView.setText(mSubState.name() + " " + getSubStateTimeMS() / 1000);
        }
        switch (mState) {

            case READY_FOR_MISSION:
                break;
            case INITIAL_STRAIGHT:
                break;
            case NEAR_BALL_MISSION:
                break;
            case FAR_BALL_MISSION:
                break;
            case HOME_CONE_MISSION:
                break;
            case WAITING_FOR_PICKUP:
                if (getStateTimeMS() > 5000) {
                    setState(State.SEEKING_HOME);
                }
                break;
            case SEEKING_HOME:
                if (getStateTimeMS() > 5000) {
                    setState(State.WAITING_FOR_PICKUP);
                }
                break;
        }

        switch (mSubState) {

            case INACTIVE:
                break;
            case GPS_SEEKING:
                if (getSubStateTimeMS() > 15000) {
                    setSubState(subState.OPTIONAL_SCRIPT);
                }
                break;
            case IMAGE_REC_SEEKING:
                break;
            case OPTIONAL_SCRIPT:
                if (mState == State.NEAR_BALL_MISSION) {
                    if (getSubStateTimeMS() > 4000) {
                        setState(State.FAR_BALL_MISSION);
                        mLeftBallTextView.setText("---");
                        updateTargetXY(240, 50);
                        onLocationChanged(90, -50, -46, null);
                    }
                }
                if (mState == State.FAR_BALL_MISSION) {
                    if (getSubStateTimeMS() > 4000) {
                        mMiddleBallTextView.setText("---");
                    }
                    if (getSubStateTimeMS() > 8000) {
                        mRightBallTextView.setText("---");
                        setState(mState.HOME_CONE_MISSION);
                        updateTargetXY(0, 0);
                        onLocationChanged(240, 50, 45, null);
                    }
                }
                if (mState == State.HOME_CONE_MISSION) {
                    if (getSubStateTimeMS() > 1000) {
                        setState(State.WAITING_FOR_PICKUP);
                    }
                }
                if (mState == State.SEEKING_HOME) {
                    if (getStateTimeMS() > 5000) {
                        setState(State.WAITING_FOR_PICKUP);
                    }
                }
                break;
        }
    }

    private long getStateTimeMS() {
        return System.currentTimeMillis() - mStateStartTime;
    }

    private long getSubStateTimeMS() {
        return System.currentTimeMillis() - mSubStateTime;
    }

    private void setState(State newState) {
        mStateStartTime = System.currentTimeMillis();
        switch (newState) {
            case READY_FOR_MISSION:
                setSubState(subState.INACTIVE);
                break;
            case INITIAL_STRAIGHT:
                setSubState(subState.INACTIVE);
                mCommandHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setState(State.NEAR_BALL_MISSION);
                        onLocationChanged(60, -25, -30, null);
                    }
                }, 4000);
                break;
            case NEAR_BALL_MISSION:
                setSubState(subState.GPS_SEEKING);
                break;
            case FAR_BALL_MISSION:
                setSubState(subState.GPS_SEEKING);
                break;
            case HOME_CONE_MISSION:
                setSubState(subState.GPS_SEEKING);
                break;
            case WAITING_FOR_PICKUP:
                setSubState(subState.INACTIVE);
                break;
            case SEEKING_HOME:
                setSubState(subState.GPS_SEEKING);
                break;
        }


        mState = newState;
    }

    private void setSubState(subState newSubState) {
        mSubStateTime = System.currentTimeMillis();

        mSubState = newSubState;
    }

    public void handleReset(View view) {
//        Toast.makeText(this, "You pressed RESET", Toast.LENGTH_SHORT).show();
        setState(State.READY_FOR_MISSION);
        mLeftBallTextView.setText("Red\nBall");
        mMiddleBallTextView.setText("White\nBall");
        mRightBallTextView.setText("Blue\nBall");
        mTargetXYTextView.setText("---");
        mGPSCounter = 0;
        mGPSTextView.setText("---");
    }


    public void handleGo(View view) {
//        Toast.makeText(this, "You pressed GO", Toast.LENGTH_SHORT).show();
        if (mState == State.READY_FOR_MISSION) {
            setState(State.INITIAL_STRAIGHT);
            updateTargetXY(90, -50);
        }
    }

    public void handleNotSeen(View view) {
//        Toast.makeText(this, "You pressed NOT SEEN", Toast.LENGTH_SHORT).show();

        if (mSubState == subState.IMAGE_REC_SEEKING) {
            setSubState(subState.GPS_SEEKING);
        }
    }

    public void handleSeenSmall(View view) {
//        Toast.makeText(this, "You pressed SEEN SMALL", Toast.LENGTH_SHORT).show();

        if (mSubState == subState.GPS_SEEKING) {
            setSubState(subState.IMAGE_REC_SEEKING);
        }
    }

    public void handleSeenBig(View view) {
//        Toast.makeText(this, "You pressed SEEN BIG", Toast.LENGTH_SHORT).show();
        if (mSubState == subState.IMAGE_REC_SEEKING) {
            setSubState(subState.OPTIONAL_SCRIPT);
        }
    }

    public void handleMissionComplete(View view) {
//        Toast.makeText(this, "You pressed MISSION COMPLETE", Toast.LENGTH_SHORT).show();
        if (mState == State.WAITING_FOR_PICKUP) {
            setState(mState.READY_FOR_MISSION);
            mLeftBallTextView.setText("Red\nBall");
            mMiddleBallTextView.setText("White\nBall");
            mRightBallTextView.setText("Blue\nBall");
            mTargetXYTextView.setText("---");
            mGPSCounter = 0;
            mGPSTextView.setText("---");
        }
    }

    private void updateTargetXY(int x, int y) {
        mTargetXYTextView.setText("(" + x / 1 + ", " + y / 1 + ")");
    }

}
