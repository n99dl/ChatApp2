package com.example.chatapp2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp2.Audio.AudioPlayer;
import com.example.chatapp2.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class CallActivity extends AppCompatActivity {

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    TextView username, status;
    ImageButton back_button;
    CircleImageView profile_image;
    Button action_button, pickup_button;
    String type, userid;

    SinchClient sinchClient;
    Call call;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private int field = 0x00000020;

    private AudioPlayer mAudioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        Log.d("sinchtest", "onCreate: ");
        username = findViewById(R.id.username);
        status = findViewById(R.id.status);
        back_button = findViewById(R.id.back_button);
        profile_image = findViewById(R.id.profile_image);
        action_button = findViewById(R.id.action_button);
        pickup_button = findViewById(R.id.pickup_button);

        back_button.setVisibility(View.GONE);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(field, getLocalClassName());

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        initiateSinch();

        status.setText("Connecting");

        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        userid = intent.getStringExtra("userid");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);


        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user == null)
                    return;
                username.setText(user.getUsername());
                if (user.getImageURL().equals("default")) {
                    profile_image.setImageResource(R.mipmap.ic_user_default);
                } else {
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (type.equals("calling"))
        {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    call();
                }
            }, 500);
        } else if (type.equals("receiving")) {
            receive();
        }
    }

    private void receive()
    {
        call = MainActivity.getuCall();
        pickup_button.setVisibility(View.VISIBLE);
        mAudioPlayer = new AudioPlayer(this);
        mAudioPlayer.playRingtone();
        call.addCallListener(new SinchCallListener());
        status.setText("Incoming call");
        pickup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.stopRingtone();
                pickup_button.setVisibility(View.GONE);
                if (isRecordAudioPermissionGranted()) {
                    if (call == null) {
                        Toast.makeText(getApplicationContext(), "No call found", Toast.LENGTH_SHORT).show();
                        return ;
                    }
                    if(!wakeLock.isHeld()) {
                        wakeLock.acquire();
                    }
                    call.answer();
                    setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                }
            }
        });

        action_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wakeLock.isHeld()) {
                    wakeLock.release();
                }
                mAudioPlayer.stopRingtone();
                if (call != null)
                call.hangup();
                call = null;
                finish();
            }
        });
    }
    private void call()
    {
        mAudioPlayer = new AudioPlayer(this);
        if (isRecordAudioPermissionGranted()) {
            if (call == null) {
                call = sinchClient.getCallClient().callUser(userid);
                action_button.setText("Hang Up");
            }
            if(!wakeLock.isHeld()) {
                wakeLock.acquire();
            }
            call.addCallListener(new SinchCallListener());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
            action_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (call != null)
                    call.hangup();
                    if(!wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                    call = null;
                }
            });
        }
    }

    private class SinchCallListener implements CallListener {
        @Override
        public void onCallEnded(Call endedCall) {
            //call ended by either party
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            mAudioPlayer.stopProgressTone();
            mAudioPlayer.stopRingtone();
            if(wakeLock.isHeld()) {
                wakeLock.release();
            }
            call = null;
            status.setText("Call ended");
            if (mDurationTask != null)
            mDurationTask.cancel();
            if (mTimer != null)
            mTimer.cancel();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 2000);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {

        }

        @Override
        public void onCallEstablished(Call establishedCall) {
            //incoming call was picked up
            mAudioPlayer.stopProgressTone();
            status.setText("Connected");
            mTimer = new Timer();
            mDurationTask = new UpdateCallDurationTask();
            mTimer.schedule(mDurationTask, 0, 500);
        }

        @Override
        public void onCallProgressing(Call progressingCall) {
            //call is ringing
            status.setText("Ringing");
            mAudioPlayer.playProgressTone();
        }
    }

    private void initiateSinch()
    {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(firebaseUser.getUid())
                .applicationKey("e0f6bf72-e5d3-4714-81f3-d8d49e7f238d")
                .applicationSecret("eiLXA+ac+U2Wp7JlG9f65w==")
                .environmentHost("clientapi.sinch.com")
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.start();
        Log.d("sinchstart", "initiateSinch: ");
    }

    private final int AUDIO_RECORD_REQUEST_CODE = 1;

    private boolean isRecordAudioPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED) {
                // put your code for Version>=Marshmallow
                return true;
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
                    Toast.makeText(this,
                            "App required access to audio", Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO
                }, AUDIO_RECORD_REQUEST_CODE);
                return false;
            }

        } else {
            // put your code for Version < Marshmallow
            return true;
        }
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        if (call != null) {
            status.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }
    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initiateSinch();
        Log.d("sinchtest", "onResume: ");
    }
}
