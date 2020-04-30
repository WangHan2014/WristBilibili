package cn.luern0313.wristbilibili.adapter;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.R;
import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.ImageDownloaderUtil;
import cn.luern0313.wristbilibili.util.ReplyHtmlImageHandlerUtil;
import cn.luern0313.wristbilibili.util.ReplyHtmlTagHandlerUtil;

/**
 * 被 luern0313 创建于 2020/1/31.
 */

public class ReplyAdapter extends BaseAdapter
{
    private LayoutInflater mInflater;

    private LruCache<String, BitmapDrawable> mImageCache;
    private ReplyAdapterListener replyAdapterListener;

    private ArrayList<ReplyModel> replyList;
    private ListView listView;

    private boolean isShowFloor;
    private boolean isHasRoot;
    private int replyCount;

    public ReplyAdapter(LayoutInflater inflater, ListView listView, ArrayList<ReplyModel> replyList, boolean isShowFloor, boolean isHasRoot, int replyCount, ReplyAdapterListener replyAdapterListener)
    {
        mInflater = inflater;
        this.replyList = replyList;
        this.listView = listView;
        this.isShowFloor = isShowFloor;
        this.isHasRoot = isHasRoot;
        this.replyCount = replyCount;
        this.replyAdapterListener = replyAdapterListener;

        int maxCache = (int) Runtime.getRuntime().maxMemory();
        int cacheSize = maxCache / 8;
        mImageCache = new LruCache<String, BitmapDrawable>(cacheSize)
        {
            @Override
            protected int sizeOf(String key, BitmapDrawable value)
            {
                try
                {
                    return value.getBitmap().getByteCount();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                return 0;
            }
        };
    }

    @Override
    public int getCount()
    {
        return replyList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getViewTypeCount()
    {
        return 4;
    }

    @Override
    public int getItemViewType(int position)
    {
        return replyList.get(position).reply_mode;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup)
    {
        final ReplyModel replyModel = replyList.get(position);
        int type = getItemViewType(position);
        ViewHolder viewHolder = null;
        if(convertView == null)
        {
            switch(type)
            {
                case 0:
                    convertView = mInflater.inflate(R.layout.item_reply_reply, null);
                    viewHolder = new ViewHolder();
                    convertView.setTag(viewHolder);
                    viewHolder.reply_img = convertView.findViewById(R.id.item_reply_head);
                    viewHolder.reply_name = convertView.findViewById(R.id.item_reply_name);
                    viewHolder.reply_is_up = convertView.findViewById(R.id.item_reply_up);
                    viewHolder.reply_time = convertView.findViewById(R.id.item_reply_time);
                    viewHolder.reply_floor = convertView.findViewById(R.id.item_reply_floor);
                    viewHolder.reply_level = convertView.findViewById(R.id.item_reply_level);
                    viewHolder.reply_text = convertView.findViewById(R.id.item_reply_text);
                    viewHolder.reply_reply_show = convertView.findViewById(R.id.item_reply_reply_show);
                    viewHolder.reply_reply_show_1 = convertView.findViewById(R.id.item_reply_reply_show_1);
                    viewHolder.reply_reply_show_2 = convertView.findViewById(R.id.item_reply_reply_show_2);
                    viewHolder.reply_reply_show_3 = convertView.findViewById(R.id.item_reply_reply_show_3);
                    viewHolder.reply_reply_show_show = convertView.findViewById(R.id.item_reply_reply_show_show);
                    viewHolder.reply_is_up_like = convertView.findViewById(R.id.item_reply_up_like);

                    viewHolder.reply_like = convertView.findViewById(R.id.item_reply_like);
                    viewHolder.reply_like_img = convertView.findViewById(R.id.item_reply_like_i);
                    viewHolder.reply_like_num = convertView.findViewById(R.id.item_reply_like_n);
                    viewHolder.reply_dislike = convertView.findViewById(R.id.item_reply_dislike);
                    viewHolder.reply_dislike_img = convertView.findViewById(R.id.item_reply_dislike_i);
                    viewHolder.reply_reply = convertView.findViewById(R.id.item_reply_reply);
                    viewHolder.reply_reply_num = convertView.findViewById(R.id.item_reply_reply_n);
                    break;

                case 1:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText("热门评论");
                    Drawable changeNewDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeNewDrawable.setBounds(0, 0, DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change)).setCompoundDrawables(changeNewDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 2:
                    convertView = mInflater.inflate(R.layout.widget_reply_changemode, null);
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_sign)).setText("最新评论");
                    Drawable changeHotDrawable = convertView.getResources().getDrawable(R.drawable.icon_reply_sort);
                    changeHotDrawable.setBounds(0, 0,DataProcessUtil.dip2px(listView.getContext(), 12), DataProcessUtil.dip2px(listView.getContext(), 12));
                    ((TextView) convertView.findViewById(R.id.item_reply_sort_change))
                            .setCompoundDrawables(changeHotDrawable,null, null,null);
                    if(isHasRoot)
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.GONE);
                    else
                        convertView.findViewById(R.id.item_reply_sort_change).setVisibility(View.VISIBLE);
                    break;

                case 3:
                    convertView = mInflater.inflate(R.layout.widget_reply_sendreply, null);
            }
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(type == 0)
        {
            viewHolder.reply_img.setImageResource(R.drawable.img_default_avatar);
            viewHolder.reply_name.setText(replyModel.reply_owner_name);
            viewHolder.reply_time.setText(replyModel.reply_time);

            viewHolder.reply_is_up.setVisibility(replyModel.reply_is_up ? View.VISIBLE : View.GONE);
            viewHolder.reply_floor.setVisibility(isShowFloor ? View.VISIBLE : View.GONE);
            if(isShowFloor)
                viewHolder.reply_floor.setText(replyModel.reply_floor);
            viewHolder.reply_level.setText("LV" + replyModel.reply_owner_lv);

            viewHolder.reply_text.setMovementMethod(LinkMovementMethod.getInstance());
            viewHolder.reply_text.setText(Html.fromHtml(replyModel.reply_text, new ReplyHtmlImageHandlerUtil(
                    listView.getContext(), mImageCache, viewHolder.reply_text, replyModel.reply_emote_size), new ReplyHtmlTagHandlerUtil(listView.getContext())));

            /*viewHolder.reply_text.setOnClickATagListener(new OnClickATagListener()
            {
                @Override
                public void onClick(View widget, @Nullable String href)
                {
                    Uri uri = Uri.parse(href);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setClassName("cn.luern0313.wristbilibili","cn.luern0313.wristbilibili.ui.UnsupportedLinkActivity");
                    listView.getContext().startActivity(intent);
                }
            });*/

            if(replyModel.reply_reply_show.size() > 0 && !isHasRoot)
            {
                viewHolder.reply_reply_show.setVisibility(View.VISIBLE);
                viewHolder.reply_reply_show_1.setVisibility(View.GONE);
                viewHolder.reply_reply_show_2.setVisibility(View.GONE);
                viewHolder.reply_reply_show_3.setVisibility(View.GONE);
                viewHolder.reply_reply_show_1.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolder.reply_reply_show_2.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolder.reply_reply_show_3.setMovementMethod(LinkMovementMethod.getInstance());
                viewHolder.reply_reply_show_1.setOnClickListener(onViewClick(position));
                viewHolder.reply_reply_show_2.setOnClickListener(onViewClick(position));
                viewHolder.reply_reply_show_3.setOnClickListener(onViewClick(position));
                switch(replyModel.reply_reply_show.size())
                {
                    case 3:
                        viewHolder.reply_reply_show_3.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_3.setText(
                                Html.fromHtml(replyModel.reply_reply_show.get(2),
                                              new ReplyHtmlImageHandlerUtil(listView.getContext(), mImageCache, viewHolder.reply_reply_show_3, replyModel.reply_emote_size),
                                              new ReplyHtmlTagHandlerUtil(listView.getContext())));
                    case 2:
                        viewHolder.reply_reply_show_2.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_2.setText(
                                Html.fromHtml(replyModel.reply_reply_show.get(1),
                                              new ReplyHtmlImageHandlerUtil(listView.getContext(), mImageCache, viewHolder.reply_reply_show_2, replyModel.reply_emote_size),
                                              new ReplyHtmlTagHandlerUtil(listView.getContext())));
                    case 1:
                        viewHolder.reply_reply_show_1.setVisibility(View.VISIBLE);
                        viewHolder.reply_reply_show_1.setText(
                                Html.fromHtml(replyModel.reply_reply_show.get(0),
                                              new ReplyHtmlImageHandlerUtil(listView.getContext(), mImageCache, viewHolder.reply_reply_show_1, replyModel.reply_emote_size),
                                              new ReplyHtmlTagHandlerUtil(listView.getContext())));
                }
                if(!replyModel.reply_is_up_reply)
                    viewHolder.reply_reply_show_show.setText(Html.fromHtml("<font color=\"#3f51b5\">共" + replyModel.reply_reply_num + "条回复 ></font>"));
                else
                    viewHolder.reply_reply_show_show.setText(Html.fromHtml("UP主等人<font color=\"#3f51b5\">共" + replyModel.reply_reply_num + "条回复 ></font>"));
            }
            else
                viewHolder.reply_reply_show.setVisibility(View.GONE);

            viewHolder.reply_is_up_like.setVisibility(replyModel.reply_is_up_like ? View.VISIBLE : View.GONE);

            viewHolder.reply_like_num.setText(DataProcessUtil.getView(replyModel.reply_like_num));
            viewHolder.reply_reply_num.setText(replyModel.reply_reply_num);

            if(replyModel.reply_user_like) viewHolder.reply_like_img.setImageResource(R.drawable.icon_liked);
            else viewHolder.reply_like_img.setImageResource(R.drawable.icon_like);
            if(replyModel.reply_user_dislike) viewHolder.reply_dislike_img.setImageResource(R.drawable.icon_disliked);
            else viewHolder.reply_dislike_img.setImageResource(R.drawable.icon_dislike);
            if(replyModel.reply_owner_vip == 2)
            {
                viewHolder.reply_name.setTextColor(listView.getResources().getColor(R.color.mainColor));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }
            else
            {
                viewHolder.reply_name.setTextColor(listView.getResources().getColor(R.color.black));
                viewHolder.reply_name.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            }

            viewHolder.reply_img.setTag(replyModel.reply_owner_face);
            BitmapDrawable h = setImageFormWeb(replyModel.reply_owner_face);
            if(h != null) viewHolder.reply_img.setImageDrawable(h);

            viewHolder.reply_img.setOnClickListener(onViewClick(position));
            viewHolder.reply_like.setOnClickListener(onViewClick(position));
            viewHolder.reply_dislike.setOnClickListener(onViewClick(position));
            viewHolder.reply_reply.setOnClickListener(onViewClick(position));
        }
        else if(type == 1 || type == 2)
        {
            convertView.findViewById(R.id.item_reply_sort_change).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    replyAdapterListener.onSortModeChange();
                }
            });
        }
        else if(type == 3)
        {
            if(isHasRoot)
                ((TextView) convertView.findViewById(R.id.reply_toolbar_sendreply)).setText("发送回复");
            ((TextView) convertView.findViewById(R.id.reply_toolbar_total)).setText("共" + DataProcessUtil.getView(replyCount) + "条" + (isHasRoot ? "回复" : "评论"));
            convertView.findViewById(R.id.reply_toolbar_sendreply).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    replyAdapterListener.onClick(v.getId(), -1, 1);
                }
            });
        }
        return convertView;
    }

    private View.OnClickListener onViewClick(final int position)
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                replyAdapterListener.onClick(v.getId(), position, 0);
            }
        };
    }

    class ViewHolder
    {
        ImageView reply_img;
        TextView reply_name;
        TextView reply_is_up;
        TextView reply_time;
        TextView reply_floor;
        TextView reply_level;
        TextView reply_text;
        LinearLayout reply_reply_show;
        TextView reply_reply_show_1;
        TextView reply_reply_show_2;
        TextView reply_reply_show_3;
        TextView reply_reply_show_show;
        TextView reply_is_up_like;

        LinearLayout reply_like;
        ImageView reply_like_img;
        TextView reply_like_num;
        LinearLayout reply_dislike;
        ImageView reply_dislike_img;
        LinearLayout reply_reply;
        TextView reply_reply_num;
    }

    private BitmapDrawable setImageFormWeb(String url)
    {
        if(mImageCache.get(url) != null)
        {
            return mImageCache.get(url);
        }
        else
        {
            ImageTask it = new ImageTask(listView);
            it.execute(url);
            return null;
        }
    }

    class ImageTask extends AsyncTask<String, Void, BitmapDrawable>
    {
        private String imageUrl;
        private Resources listViewResources;

        ImageTask(ListView listView)
        {
            this.listViewResources = listView.getResources();
        }

        @Override
        protected BitmapDrawable doInBackground(String... params)
        {
            try
            {
                imageUrl = params[0];
                Bitmap bitmap = null;
                bitmap = ImageDownloaderUtil.downloadImage(imageUrl);
                BitmapDrawable db = new BitmapDrawable(listViewResources, bitmap);
                if(mImageCache.get(imageUrl) == null && bitmap != null)
                {
                    mImageCache.put(imageUrl, db);
                }
                return db;
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(BitmapDrawable result)
        {
            ImageView iv = listView.findViewWithTag(imageUrl);
            if(iv != null && result != null)
            {
                iv.setImageDrawable(result);
            }
        }
    }

    public interface ReplyAdapterListener
    {
        void onClick(int viewId, int position, int mode);
        void onSortModeChange();
    }
}
