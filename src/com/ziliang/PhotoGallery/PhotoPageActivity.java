package com.ziliang.PhotoGallery;

import android.app.Fragment;

/**
 * Created by Kurt on 4/13/2015.
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment(){
        return new PhotoPageFragment();
    }
}
