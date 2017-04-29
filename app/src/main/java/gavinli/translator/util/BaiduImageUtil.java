package gavinli.translator.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GavinLi
 * on 17-3-12.
 */

public class BaiduImageUtil extends NetworkImageUtil {
    private static final String BAIDU_IMAGE_SEARCH_URL = "http://image.baidu.com/search/index?tn=baiduimage&ps=1&ct=201326592&lm=-1&cl=2&nc=1&ie=utf-8&word=";

    private String mKey;

    public BaiduImageUtil(String key) {
        mKey = key;
    }

    @Override
    protected void buildImageUrl() throws IOException {
        Request request = new Request.Builder()
                .url(BAIDU_IMAGE_SEARCH_URL + mKey)
                .build();
        Response response = new OkHttpClient().newCall(request).execute();
        if(!response.isSuccessful())
            throw new IOException("网络连接错误(" + response.code() + ")");
        Pattern pattern = Pattern.compile("\"thumbURL\":\"(.+?)\"");
        Matcher matcher = pattern.matcher(response.body().string());
        while(matcher.find()) {
            mImageUrls.add(matcher.group(1).replaceAll("\\\\", ""));
        }
    }

    @Override
    public List<Bitmap> getImages(int num, int offset) throws IOException {
        if(mImageUrls.isEmpty()) buildImageUrl();
        List<Bitmap> images = new ArrayList<>();
        for(int i = offset; i < num + offset; i++) {
            InputStream in = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(mImageUrls.get(i));
                connection = (HttpURLConnection) url.openConnection();
                in = connection.getInputStream();
                images.add(BitmapFactory.decodeStream(in));
            } finally {
                if(in != null) in.close();
                if(connection != null) connection.disconnect();
            }
        }
        return images;
    }
}