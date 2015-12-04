package sausure.youtubeplayeffectsample;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements YouTubePlayEffect.Callback,
        SurfaceHolder.Callback,View.OnClickListener,MediaPlayer.OnPreparedListener
{
    private SurfaceView mPlayer;
    private MediaPlayer mediaPlayer;
    private SurfaceHolder holder;
    private YouTubePlayEffect mEffectPlayer;
    private Button testButton;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPlayer = (SurfaceView) findViewById(R.id.player);
        holder = mPlayer.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mediaPlayer = MediaPlayer.create(this,R.raw.welcome_video);
        mediaPlayer.setOnPreparedListener(this);

        mEffectPlayer = (YouTubePlayEffect) findViewById(R.id.youtube_effect);
        mEffectPlayer.setCallback(this);

        testButton = (Button) findViewById(R.id.test);
        testButton.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.list_view);
        initList();
    }

    private void initList(){
        listView.setAdapter(ArrayAdapter.createFromResource(this,R.array.test_list,android.R.layout.simple_list_item_1));
    }

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
        testButton.setVisibility(View.VISIBLE);
        Toast.makeText(this,"destroy-direction-"+(direct == YouTubePlayEffect.SLIDE_TO_LEFT ?
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
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    @Override
    public void onClick(View v) {
        playVideo();
        testButton.setVisibility(View.GONE);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaPlayer.setLooping(true);
    }
}
