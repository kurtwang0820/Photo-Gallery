package com.ziliang.PhotoGallery;

import java.util.ArrayList;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class PhotoGalleryFragment extends Fragment {
    GridView mGridView;
    ArrayList<GalleryItem> mItems = new ArrayList<GalleryItem>();
    private int current_page = 1;
    private int fetched_page = 0;
    private int scrollPosition=0;
    private static final String TAG = "PhotoGalleryFragment";
    ThumbnailDownloader<ImageView> mThumbnailThread;
    private LruCache<String,Bitmap> memCache;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        new FetchItemsTask().execute(current_page);
        mThumbnailThread=new ThumbnailDownloader<ImageView>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>(){
            public void onThumbnailDownloaded(ImageView imageView,String url,Bitmap thumbnail){
                if(isVisible()){
                    memCache.put(url,thumbnail);
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();
        final int maxMemory=(int)(Runtime.getRuntime().maxMemory()/1024);
        final int cacheSize=maxMemory/5;
        memCache=new LruCache<String, Bitmap>(cacheSize);
        Log.i(TAG,"Background thread started");
    }
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            memCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        if(key == null) return null;

        return memCache.get(key);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mGridView = (GridView) v.findViewById(R.id.gridView);
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0 && current_page == fetched_page) {
                    current_page += 1;
                    new FetchItemsTask().execute(current_page);
                    scrollPosition=firstVisibleItem + visibleItemCount;
                    Log.i(TAG, "Scrolled: current page: " + current_page + " - fetched page: " + fetched_page + " -total item count: " + totalItemCount);
                }
            }
        });
        setupAdapter();

        return v;
    }

    void setupAdapter() {
        if (getActivity() == null || mGridView == null) return;

        if (mItems != null) {
            mGridView.setSelection(scrollPosition);
            mGridView.setAdapter(new GalleryItemAdapter(mItems));
        } else {
            mGridView.setAdapter(null);
        }
    }

    private class FetchItemsTask extends AsyncTask<Integer, Void, ArrayList<GalleryItem>> {
        @Override
        protected ArrayList<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetcher().fetchItems(params[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<GalleryItem> items) {
            mItems.addAll(items);
            setupAdapter();
            fetched_page += 1;
        }
    }
    private class GalleryItemAdapter extends ArrayAdapter<GalleryItem>{
        public GalleryItemAdapter(ArrayList<GalleryItem> items){
            super(getActivity(),0,items);
        }
        @Override
        public View getView(int position,View convertView,ViewGroup parent){
            if(convertView==null){
                convertView=getActivity().getLayoutInflater().inflate(R.layout.gallery_item,parent,false);
            }
            ImageView imageView=(ImageView)convertView.findViewById(R.id.gallery_item_imageView);
            imageView.setImageResource(R.drawable.brian_up_close);
            GalleryItem item=getItem(position);
            if(getBitmapFromMemCache(item.getmUrl())==null){
                mThumbnailThread.queueThumbnail(imageView,item.getmUrl());
            }else{
                if(isVisible()){
                    imageView.setImageBitmap(getBitmapFromMemCache(item.getmUrl()));
                }
            }
            return convertView;
        }
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mThumbnailThread.quit();
        Log.i(TAG,"Background thread destroyed");
    }
    @Override
    public void onDestroyView(){
        super.onDestroyView();
        mThumbnailThread.clearQueue();
    }
}
