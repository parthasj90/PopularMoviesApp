package com.example.android.popularmovies1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivityFragment extends Fragment {
    String thumbnail_path, original_title, plot, release_date, vote_avg;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT) && intent.hasExtra(Intent.EXTRA_LOCAL_ONLY)) {
            String movieId = intent.getStringExtra(Intent.EXTRA_TEXT);
            String mMovieJsonStr = intent.getStringExtra(Intent.EXTRA_LOCAL_ONLY);
            try {
                movieDetailsExtractor(mMovieJsonStr, movieId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ImageView image = (ImageView) rootView.findViewById(R.id.thumbnail_imageview);
            Picasso.with(getContext())
                    .load(thumbnail_path)
                    .placeholder(R.mipmap.image_loading)
                    .error(R.mipmap.no_image)
                    .into(image);
            ((TextView) rootView.findViewById(R.id.originaltitle_textview)).setText("Title: " + original_title);
            ((TextView) rootView.findViewById(R.id.plot_textview)).setText("Synopsis: " + plot);
            ((TextView) rootView.findViewById(R.id.releasedate_textview)).setText("Release Date: " + release_date);
            ((TextView) rootView.findViewById(R.id.rating_textview)).setText("Rating: " + vote_avg);
        }
        return rootView;
    }

    public void movieDetailsExtractor(String mMovieJsonStr, String movieId) throws JSONException {
        JSONObject movieJson = new JSONObject(mMovieJsonStr);
        JSONArray resultsArray = movieJson.getJSONArray("results");
        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject movieObject = resultsArray.getJSONObject(i);
            String movieId_Now = movieObject.getString("id");

            if (movieId_Now.equals(movieId)) {
                String movieImagePath = movieObject.getString("backdrop_path");
                thumbnail_path = "http://image.tmdb.org/t/p/w185/" + movieImagePath;
                original_title = movieObject.getString("original_title");
                plot = movieObject.getString("overview");
                release_date = movieObject.getString("release_date");
                vote_avg = movieObject.getString("vote_average");
            }

        }

    }
}

