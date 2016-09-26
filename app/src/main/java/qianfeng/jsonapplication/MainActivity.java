package qianfeng.jsonapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private Button mBtn;
    private TextView mTv_sub;
    private ImageView mIv;
    private ProgressBar pb;
    private TextView mTv_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtn = ((Button) findViewById(R.id.btn_nextPage));
        mTv_sub = ((TextView) findViewById(R.id.tv_subject));
        mIv = ((ImageView) findViewById(R.id.iv));
        pb = ((ProgressBar) findViewById(R.id.pb));
        mTv_title = ((TextView) findViewById(R.id.tv_title));


    }

    public void pressed(View view) {
        new MyAsync().execute("http://litchiapi.jstv.com/api/getTops?limit=5&column=0&val=F467412B44B421716757A6B2D7635B4A");
    }


    private class MyAsync extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... params) {
            String st = http(params[0]);
            Log.d("qianfeng", "doInBackground: " + st);
            return st;
        }


        @Override
        protected void onPostExecute(String s) {
            // 开始JSON解析吧
            try {
                JSONObject js = new JSONObject(s);
                JSONObject paramz = js.getJSONObject("paramz");
                JSONArray tops = paramz.getJSONArray("tops");
                Log.d("qianfeng", "onPostExecute: " + tops);
                for (int i = 0; i < tops.length(); i++) {
                    JSONObject jsonObject = tops.getJSONObject(i);
                    String title = jsonObject.getString("category");
                    String sub = jsonObject.getString("subject");
                    String photo = jsonObject.getString("photo");
                    mTv_title.setText(title);
                    mTv_sub.setText(sub);
                    // 可以在这里启动子线程下载啊
                    new download().execute(photo);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        private String http(String params)
        {
            HttpURLConnection httpURLConnection = null;
            InputStream is = null;
            BufferedReader br = null;
            try {
                URL url = new URL(params);
               httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);
                if(httpURLConnection.getResponseCode() == 200)
                {
                    br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String str = null;
                    while((str = br.readLine())!=null)
                    {
                        builder.append(str);
                    }
                    return builder.toString();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                try {
                    if (br != null)
                    {
                        br.close();
                    }
                    if(httpURLConnection != null)
                    {
                        httpURLConnection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


    }

    private class download extends AsyncTask<String, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
           return http(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
           if(bitmap != null)
           {
               mIv.setImageBitmap(bitmap);
           }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if(values.length > 0)
            {
             pb.setProgress(values[0]);
            }
        }

        private Bitmap http(String params) {
            HttpURLConnection httpURLConnection = null;
            InputStream is = null;
            BufferedReader br = null;
            BufferedOutputStream bos = null;
            try {
                URL url = new URL(params);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setConnectTimeout(5000);

                if (httpURLConnection.getResponseCode() == 200) {

                    Bitmap bitmap = BitmapFactory.decodeStream(httpURLConnection.getInputStream());
                    bos = new BufferedOutputStream(new FileOutputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"123.jpg")));
                    // 下载，并更新进度条
                    int len = 0;
                    int current = 0;
                    long totalByte = httpURLConnection.getContentLength();
                    byte[] data = new byte[1024];
                    while ((len = is.read(data)) != -1) {

                        bos.write(data, 0, len);
                        bos.flush();
                        current += len;
                        publishProgress((int) (current * 100 / totalByte)); // 传送到主线程的data，更新UI

                    }
                    return bitmap;


                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bos != null) {
                        bos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

    }

    }


