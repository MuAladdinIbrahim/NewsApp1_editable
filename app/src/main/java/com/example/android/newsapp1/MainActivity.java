package com.example.android.newsapp1;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements LoaderCallbacks<List<NewsModel>> {

    RecyclerView recyclerView;
    SwipeRefreshLayout refreshLayout;
    NewsAdapter newsAdapter;
    ArrayList<NewsModel> newsList;
    private TextView mEmptyStateTextView;
    private View loadingBar;

    private static final String Guardian_REQUEST_URL =
            "http://content.guardianapis.com/search?show-tags=contributor&api-key=8d158f50-c87b-451a-8cd6-eb6fad30df86";
    private static final int LOADER_NEWS_ID = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         // SwipeToRefresh
        final SwipeRefreshLayout refreshLayout = findViewById(R.id.swipeRefresh);
        refreshLayout.setColorSchemeColors(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            }
        });

        mEmptyStateTextView =findViewById(R.id.empty_view);
        recyclerView = findViewById(R.id.recyclerView);
        loadingBar= findViewById(R.id.loading_bar);

        newsList = new ArrayList<>();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        newsAdapter = new NewsAdapter( this, newsList);
        recyclerView.setAdapter(newsAdapter);
// refresh
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int topRowVerticalPosition =
                        (recyclerView == null || recyclerView.getChildCount() == 0) ? 0 : recyclerView.getChildAt(0).getTop();
                refreshLayout.setEnabled(topRowVerticalPosition >= 0);
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        
        // ConnectivityManager - check connection to internet (get info about connection of not null)
        ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr != null ? connMgr.getActiveNetworkInfo() : null;

        if (networkInfo != null && networkInfo.isConnected()) {

            // Get a reference to the LoaderManager adn initialize with chosen ID
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(LOADER_NEWS_ID, null, this);
        } else {

            //If have not connection with internet
            loadingBar.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.no_connection);
        }
    }


    // refresh
//    @Override
//    protected void onCreate(final Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        final SwipeRefreshLayout swipeRefreshLayout= findViewById(R.id.swipeRefresh);
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
//        {
//            @Override
//            public void onRefresh()
//            {
//                refreshContent();
//            }
//        });
//    }

//    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//        @Override
//        public void onRefresh() {
//            // Refresh items
//            refreshItems();
//        }
//    });
//
//    void refreshItems() {
//
//        onItemsLoadComplete();
//    }
//
//    void onItemsLoadComplete() {
//        // Stop refresh animation
//        mSwipeRefreshLayout.setRefreshing(false);
//    }

    @Override
    // onCreateLoader instantiates and returns a new Loader for the given ID
    public Loader<List<NewsModel>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // getString retrieves a String value from the preferences. The second parameter is the default value for this preference.
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        // parse breaks apart the URI string that's passed into its parameter
        Uri baseUri = Uri.parse(Guardian_REQUEST_URL);

        // buildUpon prepares the baseUri that we just parsed so we can add query parameters to it
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // Append query parameter and its value. For example, the `format=politics`
        uriBuilder.appendQueryParameter("format", "json");
        uriBuilder.appendQueryParameter("q", orderBy);
        uriBuilder.appendQueryParameter("pageSize", minMagnitude);

        return new NewsLoader(this, uriBuilder.toString());
    }

    //what happens when the loading finished
    @Override
    public void onLoadFinished(Loader<List<NewsModel>> loader, List<NewsModel> myNewsList) {

        loadingBar.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.no_content);
        newsAdapter.clearAllData();

        if (myNewsList != null && !myNewsList.isEmpty()) {
            mEmptyStateTextView.setVisibility(View.GONE);
            newsAdapter.addAllData(myNewsList);
        }
    }

    //what to do when loader reset
    @Override
    public void onLoaderReset(Loader<List<NewsModel>> loader) {
        newsAdapter.clearAllData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
