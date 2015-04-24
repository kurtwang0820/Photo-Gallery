package com.ziliang.PhotoGallery;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity {
//    private static final String TAG="PhotoGalleryActivity";
    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
    @Override
    public void onNewIntent(Intent intent){
        PhotoGalleryFragment fragment=(PhotoGalleryFragment)getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query=intent.getStringExtra(SearchManager.QUERY);
//            Log.i(TAG, "Received a new search query: " + query);
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString(FlickrFetcher.PREF_SEARCH_QUERY,query).commit();
        }
        fragment.updateItems();
    }
}
