package com.craexus.alex.untitledvideoplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    LinearLayout videoHolderLayout;
    LinearLayout linkHolderLayout;
    ProgressBar progressBar;
    EditText urlLink;
    Button setURLBtn;
    ArrayList<String> urls = new ArrayList<String>();
    ArrayList<String> urlTitles= new ArrayList<String>();
    ArrayList<String> hyperlinks= new ArrayList<String>();
    String tempUrl;
    int count;
    String baseURL = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        urlLink = (EditText) findViewById(R.id.urlLink);
        setURLBtn = (Button) findViewById(R.id.button);
        //Browser to find file

        linkHolderLayout = (LinearLayout) findViewById(R.id.linkContainerLayout);
        videoHolderLayout = (LinearLayout) findViewById(R.id.thumbnailLayout);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }

    // Downloads the HTML content of web page to get the links
    public class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);

                int data = reader.read();

                //while data stream is not empty keep reading
                while(data != -1){
                    char currentChar = (char) data;
                    result += currentChar;
                    data = reader.read();
                }
                in.close();
                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Parse the data and store the contents as an ArrayList in urls , urlTitles, and hyperlinks
     * @param input the html raw content as
     * @param bURL base url for recursive calls
     */
    public void parseData(String input , String bURL){
        Document doc = Jsoup.parse(input);
        doc.setBaseUri(bURL);
        Elements content = doc.getElementsByTag("a");
        Log.i("i", "----------------------------------------------");
        //if it's hyperlink tag check if its a video format
       for(Element el : content){
            //Log.i("element" , el.toString());
            if(el.is("a[href$=.mp4]")){
                //Log.i("URL" , el.toString());\
                //get the absolute url of the web content
                urls.add(el.absUrl("href"));
                //get the text associated with the hyperlink
                urlTitles.add(el.text());
            } else {
                hyperlinks.add(el.absUrl("href"));
            }
       }
        //System.out.println(urls.toString());
        //System.out.println(urlTitles.toString());
        System.out.println("-------------------------------------------------------------------123456789");
    }

    /**
     * The onClick method for button
     * @param view Button is passed
     */
    public void setURL(View view){
        String result;
        DownloadTask downloadTask = new DownloadTask();

        Log.i("url", urlLink.getText().toString());

        try{
          //  result = urlLink.getText().toString();
            //get HTML content of website and store as result
            result = downloadTask.execute(baseURL).get();
            //Log.i("Result" , result);
            parseData(result , baseURL);
            //launchVideo(urls.get(0).toString());
            //hyperlinkFactory();
            ImageFactory();

            // Test Example
//            FrameDownloader ft = new FrameDownloader();
//            Bitmap bm;
//            final String testVid = "";
//            bm = ft.execute(testVid).get();
//            ImageView iv = new ImageView(this);
//            iv.setImageBitmap(bm);
//            iv.setOnClickListener(new View.OnClickListener(){
//
//                @Override
//                public void onClick(View v) {
//                    launchVideo(testVid);
//                }
//            });
//            videoHolderLayout.addView(iv);

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Given that the user gives an incorrect url return is the url is either valid or invalid
     * @param str
     * @return
     */
    public boolean fixURL(String str){
        return URLUtil.isValidUrl(str);
    }

    //Create a textview containing hyperlinks of all non-media content
    public void hyperlinkFactory(){
//        TextView textView = new TextView(this);
//        textView.setText("RUNNING");

        for(int i=0 ; i < hyperlinks.size() ; i++){
            //textView.setText(Integer.toString(i));
            crawlLinks(hyperlinks.get(i).toString());
        }
        //textView.setText("FINISHED");
    }

    public void crawlLinks(final String url){
        TextView textView = new TextView(this);
        //append tags
        //String linkedText = String.format("<a href=\"%s\">%s</a> ", url , url);
        //textView.setText(Html.fromHtml(linkedText));
        textView.setText(url);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                generateNewLinks(url);
            }
        });
        linkHolderLayout.addView(textView);
    }

    public class FindLinks extends AsyncTask<String , Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            return null;
        }
    }

    /**
     * Calls FrameDownloader to download files
     */
    public void ImageFactory(){
        //Dynamically create thumbnail icons
        Log.i("ImageFactory Clicked" , "");
        for(int i = 0 ; i < urls.size() ; i++){
            //System.out.println("");
            count = i;
            Bitmap frameImg;
            //TextView tv = new TextView(this);
            ImageView imageView = new ImageView(this);
            //tv.setText(urls.get(i));
            FrameDownloader frameDownloader = new FrameDownloader();
            try {
                frameImg = frameDownloader.execute(urls.get(i)).get();
//                frameImg = retrieveVideoFrameFromVideo(urls.get(i));
                imageView.setImageBitmap(frameImg);
                videoHolderLayout.addView(imageView);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * Async class to retrieve thumbnails of video files
     */
    public class FrameDownloader extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                publishProgress(count);
                return retrieveVideoFrameFromVideo(strings[0]);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return null;
        }

        protected void onPreExecute(){
            Log.i("onPrexecute" ,"Running");
            progressBar.setMax(urls.size());
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //super.onProgressUpdate(values);
            progressBar.setProgress(values[0]);
        }

        public Bitmap retrieveVideoFrameFromVideo(String path) throws Throwable{
            Bitmap bitmap = null;
            MediaMetadataRetriever mediaMetadataRetriever = null;
            try {
                mediaMetadataRetriever = new MediaMetadataRetriever();
                if(Build.VERSION.SDK_INT >= 14) {
                    mediaMetadataRetriever.setDataSource(path , new HashMap<String, String>());
                } else {
                    mediaMetadataRetriever.setDataSource(path);
                }

                bitmap = mediaMetadataRetriever.getFrameAtTime();
            } catch (Exception e){
                e.printStackTrace();
                throw new Throwable("Exception from retrieveFrameFromVideo");
            } finally {
                if (mediaMetadataRetriever != null) {
                    mediaMetadataRetriever.release();
                }
            }
            return bitmap;
        }
    }


    /**
     *
     * @param url
     */
    public void generateNewLinks(String url){
        linkHolderLayout.removeAllViews();

        String result;
        DownloadTask downloadTask = new DownloadTask();

        Log.i("url", urlLink.getText().toString());

        try{
            //  result = urlLink.getText().toString();
            result = downloadTask.execute(url).get();
            //Log.i("Result" , result);
            urls.clear();
            urlTitles.clear();
            hyperlinks.clear();
            parseData(result, url);

        } catch(Exception e) {
            e.printStackTrace();
        }


    }

    public void launchVideo(String videoPath){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoPath));
        intent.setDataAndType(Uri.parse(videoPath), "video/mp4");
        startActivity(intent);
    }

