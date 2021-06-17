package cn.luern0313.wristbilibili.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.fragment.PopularWeeklyFragment;
import cn.luern0313.wristbilibili.widget.TitleView;

public class PopularWeeklyActivity extends BaseActivity implements TitleView.TitleViewListener
{
    Context ctx;
    LayoutInflater inflater;
    Intent intent;

    TitleView titleView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_popular_weekly);
        ctx = this;
        inflater = getLayoutInflater();
        intent = getIntent();

        titleView = findViewById(R.id.popular_weekly_title);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.popular_weekly_frame, PopularWeeklyFragment.newInstance());
        transaction.commit();
    }

    @Override
    public boolean hideTitle()
    {
        return titleView.hide();
    }

    @Override
    public boolean showTitle()
    {
        return titleView.show();
    }
}
