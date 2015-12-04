package sausure.youtubeplayeffectsample;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements YouTubePlayEffect.Callback,SurfaceHolder.Callback{

    private SurfaceView mPlayer;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder holder;
    private YouTubePlayEffect mEffectPlayer;
    private Button test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayer = (SurfaceView) findViewById(R.id.player);
        mEffectPlayer = (YouTubePlayEffect) findViewById(R.id.youtube_effect);
        test = (Button) findViewById(R.id.test);

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playVideo();
            }
        });

        mEffectPlayer.setCallback(this);

        holder = mPlayer.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer = MediaPlayer.create(this,R.raw.welcome_video);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
    }

//    public void test(View v){
//
//        playVideo();
//    }

    private void playVideo(){
        mEffectPlayer.show();

        if(mediaPlayer.isPlaying())
            return;

        try {
            mediaPlayer.prepare();
        }catch (Exception e){
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    @Override
    public void onDisappear(int direct) {
        mediaPlayer.pause();
        Toast.makeText(this,"destroy-direct-"+(direct == YouTubePlayEffect.SLIDE_TO_LEFT ?
                "left" : "right"),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.i("debug","surface-changed:width="+width+",height="+height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.i("debug", "surface-destroyed");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
}
