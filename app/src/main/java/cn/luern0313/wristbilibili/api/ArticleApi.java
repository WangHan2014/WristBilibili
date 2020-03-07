package cn.luern0313.wristbilibili.api;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;

import cn.luern0313.wristbilibili.models.article.ArticleModel;
import cn.luern0313.wristbilibili.util.DataProcessUtil;
import cn.luern0313.wristbilibili.util.NetWorkUtil;

/**
 * 被 luern0313 创建于 2020/2/20.
 */
public class ArticleApi
{
    private String cookie;
    private String mid;
    private String csrf;
    private String access_key;
    private ArrayList<String> appHeaders = new ArrayList<String>();
    private ArrayList<String> webHeaders = new ArrayList<String>();

    private String article_id;
    private ArticleModel articleModel;

    public ArticleApi(String cookies, String mid, String csrf, String access_key, String article_id)
    {
        this.mid = mid;
        this.cookie = cookies;
        this.csrf = csrf;
        this.access_key = access_key;

        this.article_id = article_id;
        appHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_OWN);
        }};
        webHeaders = new ArrayList<String>(){{
            add("Cookie"); add(cookie);
            add("Referer"); add("https://www.bilibili.com/");
            add("User-Agent"); add(ConfInfoApi.USER_AGENT_WEB);
        }};
    }

    public ArticleModel getArticleModel() throws IOException
    {
        try
        {
            String html = NetWorkUtil.get("https://www.bilibili.com/read/mobile/" + article_id, webHeaders).body().string();
            Document document = Jsoup.parse(html);
            Elements elements = document.selectFirst("*[class~=(:?(^)|(\\s))article-holder(:?(\\s)|($))]")
                    .select("figure[class=img-box] > img[class~=^(?:(video)|(fanju)|(article)|(music)|(shop)|(caricature)|(live))-card]");
            ArrayList<String> perList = new ArrayList<>();
            for (Element ele : elements)
            {
                String[] ids = ele.attr("aid").split(",");
                String type = ele.attr("class");
                for(String id : ids)
                {
                    if(type.indexOf("video") == 0) perList.add("av" + id);
                    else if(type.indexOf("article") == 0) perList.add("cv" + id);
                    else if(type.indexOf("caricature") == 0) perList.add("mc" + id);
                    else if(type.indexOf("live") == 0) perList.add("lv" + id);
                    else perList.add(id);
                }
            }
            String cardUrl = "https://api.bilibili.com/x/article/cards?ids=" + DataProcessUtil.joinArrayList(perList, ",");
            JSONObject cardJson = new JSONObject(NetWorkUtil.get(cardUrl, webHeaders).body().string()).optJSONObject("data");

            String infoUrl = "https://api.bilibili.com/x/article/viewinfo?id=" + article_id;
            JSONObject infoJson = new JSONObject(NetWorkUtil.get(infoUrl, webHeaders).body().string()).optJSONObject("data");

            String upUrl = "https://api.bilibili.com/x/article/more?aid=" + article_id;
            JSONObject upJson = new JSONObject(NetWorkUtil.get(upUrl, webHeaders).body().string()).optJSONObject("data");
            articleModel = new ArticleModel(article_id, infoJson, upJson, document, cardJson);
            return articleModel;
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return articleModel;
    }


    public String likeArticle(int mode) throws IOException  //1好评，2取消差评
    {
        try
        {
            String url = "https://api.bilibili.com/x/article/like";
            String per = "id=" + article_id + "&type=" + mode + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String coinArticle() throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/web-interface/coin/add";
            String per = "aid=" + article_id + "&multiply=1&avtype=2&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String favArticle(int mode) throws IOException //1添加 2删除
    {
        try
        {
            String url = mode == 1 ? "https://api.bilibili.com/x/article/favorites/add" :
                    "https://api.bilibili.com/x/article/favorites/del";
            String per = "id=" + article_id + "&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
            else
                return result.optString("message");
        }
        catch (JSONException | NullPointerException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String shareArticle(String text) throws IOException
    {
        try
        {
            String url = "https://api.vc.bilibili.com/dynamic_repost/v1/dynamic_repost/share";
            String per = "csrf_token=" + csrf + "&platform=pc&type=64&uid=&share_uid=" + mid +
                    "&content=" + URLEncoder.encode(text, "UTF-8") + "&repost_code=20000&rid=" +
                    articleModel.article_id;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.getInt("code") == 0)
                return "";
        }
        catch(JSONException e)
        {
            e.printStackTrace();
        }
        return "未知错误";
    }

    public String sendReply(String text) throws IOException
    {
        try
        {
            String url = "https://api.bilibili.com/x/v2/reply/add";
            String per = "oid=" + articleModel.article_id + "&type=12&message=" + text + "&plat=1&jsonp=jsonp&csrf=" + csrf;
            JSONObject result = new JSONObject(NetWorkUtil.post(url, per, webHeaders).body().string());
            if(result.optInt("code") == 0)
                return "";
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
        return "发送评论失败";
    }

}
