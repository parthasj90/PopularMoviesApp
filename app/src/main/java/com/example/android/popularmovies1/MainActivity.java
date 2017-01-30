package com.example.android.popularmovies1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public ArrayList<String> mPosterMoviePaths = new ArrayList<>();
    public ArrayList<String> mPosterMovieIds = new ArrayList<>();
    String mMovieJsonStr = null;
    private GridView mMoviesGrid;
    private ListAdapter mMoviesAdapter;

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
            startActivityForResult(new Intent(this, SettingsActivity.class), 101);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101) {

            finish();
            startActivity(getIntent());
            return;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMoviesGrid = (GridView) findViewById(R.id.gridview_movies);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        String sort_by = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_popular));
        new FetchMovieData().execute(sort_by);

    }

    private class MovieAdapter extends ArrayAdapter {
        private Context mContext;
        private ArrayList<String> mItems;

        public MovieAdapter(Context context, ArrayList<String> objects) {
            super(context, R.layout.activity_main, objects);
            this.mContext = context;
            this.mItems = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(R.layout.grid_item_movies, parent, false);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.grid_item_movies_imageview);
            Picasso.with(mContext)
                    .load(mItems.get(position))
                    .placeholder(R.mipmap.image_loading)
                    .error(R.mipmap.no_image)
                    .into(image);
            return convertView;
        }

    }

    public class FetchMovieData extends AsyncTask<String, Void, Void> {

        public void getMoviePosterPaths(String mMovieJsonStr) throws JSONException {


            JSONObject movieJson = new JSONObject(mMovieJsonStr);
            JSONArray resultsArray = movieJson.getJSONArray("results");
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject movieObject = resultsArray.getJSONObject(i);
                String movieImagePath = movieObject.getString("poster_path");
                String imagePath = "http://image.tmdb.org/t/p/w185/" + movieImagePath;
                String movieId = movieObject.getString("id");
                mPosterMoviePaths.add(imagePath);
                mPosterMovieIds.add(movieId);

            }
        }

        @Override
        protected Void doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String api_key = "c8809df77a223935f4fd4687ca4df05c";

            try {

                final String BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie?";
                final String SORT_PARAM = "sort_by";
                final String API_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, params[0] + ".desc")
                        .appendQueryParameter(API_PARAM, api_key)
                        .build();

                URL url = new URL(builtUri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    mMovieJsonStr = null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {

                    return null;
                }
                mMovieJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e("Mainactivity", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("Mainactivity", "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mMovieJsonStr != null) {
                try {
                    getMoviePosterPaths(mMovieJsonStr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            mMoviesAdapter = new MovieAdapter(MainActivity.this, mPosterMoviePaths);
            mMoviesGrid.setAdapter(mMoviesAdapter);
            mMoviesGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                    Intent intent = new Intent(MainActivity.this, DetailActivity.class)
                            .putExtra(Intent.EXTRA_TEXT, mPosterMovieIds.get(position))
                            .putExtra(Intent.EXTRA_LOCAL_ONLY, mMovieJsonStr);
                    startActivity(intent);
                }
            });

        }

    }

}