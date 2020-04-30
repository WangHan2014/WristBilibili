package cn.luern0313.wristbilibili.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.ReplyModel;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2019/2/20.
 * 好麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦
 * 麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦麻烦啊
 */

public class ReplyApi
{
    private String cookie;
    private String csrf;
    private String oid;
    private String type;

    public ArrayList<ReplyModel> replyArrayList = new ArrayList<>();
    public int replyCount;
    public boolean replyIsShowFloor;
    private ArrayList<String> webHeaders;

    public ReplyApi(final String cookie, String csrf, String oid, String type)
    {
        this.cookie = cookie;
        this.csrf = csrf;
        this.oid = oid;
        this.type = type;
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public String getOid()
    {
        return oid;
    }

    public int getReply(int page, String sort, int limit, ReplyModel root) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2" + (root == null ? "" : "/reply") + "/reply?pn=" + page + "&type=" + type + "&oid=" + oid + "&sort=" + sort + (root == null ? "" : ("&root=" + root.reply_id));
            JSONObject replyJson = new JSONObject(NetWorkUtil.get(url, webHeaders).body().string()).getJSONObject("data");
            replyIsShowFloor = !replyJson.has("config") || replyJson.optJSONObject("config").optInt("showfloor") == 1;
            JSONObject replyCountJson = replyJson.has("page") ? replyJson.optJSONObject("page") : new JSONObject();
            replyCount = replyCountJson.has("acount") ? replyCountJson.optInt("acount") : replyCountJson.optInt("count");
            JSONObject upper = replyJson.has("upper") ? replyJson.optJSONObject("upper") : new JSONObject();

            JSONArray replyJsonArray = replyJson.getJSONArray("replies");
            if(page == 1)
            {
                replyArrayList.clear();
                if(root != null)
                    replyArrayList.add(root);
                replyArrayList.add(new ReplyModel(3));
                replyArrayList.add(new ReplyModel(sort.equals("2") ? 1 : 2));
                if(upper.has("top") && upper.optJSONObject("top") != null && sort.equals("2"))
                    replyArrayList.add(new ReplyModel(upper.optJSONObject("top"), true, String.valueOf(upper.optInt("mid"))));
            }
            int l = limit != 0 ? Math.min(limit, replyJsonArray.length()) : replyJsonArray.length();
            for(int i = 0; i < l; i++)
                replyArrayList.add(new ReplyModel(replyJsonArray.getJSONObject(i), false, String.valueOf(upper.optInt("mid"))));

            return l;
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return 0;
        }
    }

    public String sendReply(String rpid, String text) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/add";
            String per = "oid=" + oid + "&type=" + type + (rpid.equals("") ? "" : ("&root=" + rpid + "&parent=" + rpid)) + "&message=" + text + "&jsonp=jsonp&csrf=" + csrf;
            JSONObject j = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(j.getInt("code") == 0)
                return "";
            else
                return j.getString("message");
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
            return "未知问题，请重试";
        }
    }

    public String likeReply(ReplyModel replyModel, int action, String type)
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/action";
            String per = "oid=" + oid + "&type=" + type + "&rpid=" + replyModel.reply_id + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf;
            JSONObject j = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(j.getInt("code") == 0)
            {
                replyModel.reply_user_like = action == 1;
                replyModel.reply_like_num += action * 2 - 1;
                replyModel.reply_user_dislike = false;
                return "";
            }
            else
                return j.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "未知问题，请重试？";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "网络错误！";
        }
    }

    public String hateReply(ReplyModel replyModel, int action, String type)
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/hate";
            String per = "oid=" + oid + "&type=" + type + "&rpid=" + replyModel.reply_id + "&action=" + action + "&jsonp=jsonp&csrf=" + csrf;
            JSONObject j = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(j.getInt("code") == 0)
            {
                if(replyModel.reply_user_like)
                {
                    replyModel.reply_like_num--;
                    replyModel.reply_user_like = false;
                }
                replyModel.reply_user_dislike = action == 1;
                return "";
            }
            else
                return j.getString("message");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
            return "未知问题，请重试？";
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "网络错误！";
        }
    }
}
