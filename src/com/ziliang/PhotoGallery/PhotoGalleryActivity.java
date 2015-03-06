package com.ziliang.PhotoGallery;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class PhotoGalleryActivity extends SingleFragmentActivity {
    @Override
    public Fragment createFragment() {
        return new PhotoGalleryFragment();
    }
}
