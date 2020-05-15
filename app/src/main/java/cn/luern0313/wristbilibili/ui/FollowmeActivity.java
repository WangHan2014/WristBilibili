package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.api.UserApi;
import cn.luern0313.wristbilibili.api.VideoApi;
import cn.luern0313.wristbilibili.models.ListVideoModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * Created by liupe on 2018/11/11.
 * 关注我~
 */

public class FollowmeActivity extends AppCompatActivity
{
    Context ctx;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    String cookies;
    String csrf;
    String mid;
    String access_key;

    Handler handler = new Handler();
    Runnable runnVideo;
    Runnable runnImg;

    CardView cardView;
    RelativeLayout cardViewLay;
    TextView cardViewText;
    RelativeLayout uiVideo;
    ImageView uiVideoImg;
    TextView uiVideoTitle;
    LinearLayout uiVoteLin;
    TextView uiVote;
    LinearLayout uiVideoStarLin;
    ImageView uiVideoStar;
    LinearLayout uiVideoLC;
    LinearLayout uiVideoLike;
    ImageView uiVideoLikeImg;
    LinearLayout uiVideoCoin;
    ImageView uiVideoCoinImg;

    UserApi userApi;
    VideoApi videoDetail;
    JSONObject videoJson = null;
    Bitmap videoCover;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followme);
        ctx = this;

        sharedPreferences = getSharedPreferences("default", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        cookies = sharedPreferences.getString("cookies", "");
        csrf = sharedPreferences.getString("csrf", "");
        mid = sharedPreferences.getString("mid", "");
        access_key = sharedPreferences.getString("access_key", "");

        userApi = new UserApi(cookies, csrf, access_key, "8014831");

        cardView = findViewById(R.id.fme_card);
        cardViewLay = findViewById(R.id.fme_card_lay);
        cardViewText = findViewById(R.id.fme_card_button);
        uiVideo = findViewById(R.id.fme_video);
        uiVideoImg = findViewById(R.id.fme_video_img);
        uiVideoTitle = findViewById(R.id.fme_video_title);
        uiVoteLin = findViewById(R.id.fme_vote);
        uiVote = findViewById(R.id.fme_vote_button);
        uiVideoStarLin = findViewById(R.id.fme_star);
        uiVideoStar = findViewById(R.id.fme_star_rating);
        uiVideoLC = findViewById(R.id.fme_lc);
        uiVideoLike = findViewById(R.id.fme_like);
        uiVideoLikeImg = findViewById(R.id.fme_like_img);
        uiVideoCoin = findViewById(R.id.fme_coin);
        uiVideoCoinImg = findViewById(R.id.fme_coin_img);

        runnVideo = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiVideoTitle.setText(videoJson.optString("title", ""));
                    if(videoJson.optString("title").startsWith("【互动"))
                        uiVideoStarLin.setVisibility(View.VISIBLE);
                    uiVideoLC.setVisibility(View.VISIBLE);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        runnImg = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    uiVideoImg.setImageBitmap(videoCover);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if(!sharedPreferences.contains("cookies"))
            findViewById(R.id.fme_nologin).setVisibility(View.VISIBLE);
        else
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        ArrayList<ListVideoModel> v = userApi.getUserVideo(1);
                        videoDetail = new VideoApi(cookies, csrf, mid, access_key, "", v.get(0).video_bvid);
                        handler.post(runnVideo);

                        byte[] picByte = NetWorkUtil.readStream(NetWorkUtil.get("http:" + videoJson.optString("pic", "")).body().byteStream());
                        videoCover = BitmapFactory.decodeByteArray(picByte, 0, picByte.length);
                        handler.post(runnImg);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        cardView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                cardViewText.setText("已关注");
                cardViewText.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            userApi.follow();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            Looper.prepare();
                            Toast.makeText(ctx, "关注失败...", Toast.LENGTH_SHORT).show();
                            Looper.loop();
                        }
                    }
                }).start();
            }
        });

        uiVideo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(videoDetail != null)
                    startActivity(VideoActivity.getActivityIntent(ctx, videoDetail.aid, ""));
            }
        });

        uiVote.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVote.setText("感谢投票~");
                uiVote.setBackgroundResource(R.drawable.shape_anre_followbgyes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.scoreVideo(5);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoStarLin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoStar.setImageResource(R.drawable.img_fme_star_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.scoreVideo(5);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoLike.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoLikeImg.setImageResource(R.drawable.icon_like_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.likeVideo(1);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        uiVideoCoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                uiVideoCoinImg.setImageResource(R.drawable.icon_coin_yes);
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            videoDetail.coinVideo(2);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
