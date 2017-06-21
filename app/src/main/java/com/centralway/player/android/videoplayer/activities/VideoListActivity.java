package com.centralway.player.android.videoplayer.activities;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.centralway.player.android.videoplayer.R;
import com.centralway.player.android.videoplayer.adapters.VideoListAdapter;
import com.centralway.player.android.videoplayer.listener.ClickListener;
import com.centralway.player.android.videoplayer.listener.RecyclerTouchListener;
import com.centralway.player.android.videoplayer.utilities.VideoAlbum;
import com.centralway.player.android.videoplayer.views.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.centralway.player.android.videoplayer.activities.VideoPlayerActivity.VIDEO_ID_LIST_EXTRA;

public class VideoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VideoListAdapter adapter;
    private ArrayList<VideoAlbum> videoList;
    private ArrayList<Integer> videoListId;
    private ArrayList<VideoAlbum> originalVideoList;
    private SharedPreferences permissionPref;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 10;
    private static final int REQUEST_PERMISSION_SETTING = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initCollapsingToolbar();

        permissionPref = getSharedPreferences("permissionStatus",MODE_PRIVATE);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        videoList = new ArrayList<>();
        videoListId = new ArrayList<>();
        adapter = new VideoListAdapter(this, videoList);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, dpToPx(10), true));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, final int position) {
               VideoListAdapter adapter = (VideoListAdapter)recyclerView.getAdapter();
               VideoAlbum item = adapter.getItem(position);
                int id = (int)item.getId();

                ArrayList<Integer> newList = (ArrayList<Integer>)videoListId.clone();
                newList.removeAll(new ArrayList<>(newList.subList(0,newList.indexOf(id))));

                Intent intent = new Intent(VideoListActivity.this, VideoPlayerActivity.class);
                intent.putExtra(VIDEO_ID_LIST_EXTRA, newList);
                startActivity(intent);
            }
        }));

        //prepareAlbums();

        try {
            //TODO change cover
            Glide.with(this).load(R.drawable.cover).into((ImageView) findViewById(R.id.backdrop));
        } catch (Exception e) {
            e.printStackTrace();
        }

       // retrieveVideoInfo();
        checkAllPermissions();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    /**
     * Initializing collapsing toolbar that Will show and hide the toolbar title on scroll
     */
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }
    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
    /**
     * Find all video name, size and cover picture from sdcard
     */
    private void retrieveVideoInfo() {
        Uri uri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { android.provider.MediaStore.Video.Media._ID,
                android.provider.MediaStore.Video.Media.DATA,
                android.provider.MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION};
        Cursor c = getContentResolver().query(uri, projection, null, null, null);
        if (c != null && c.moveToFirst()) {
            int _id = c.getColumnIndex(android.provider.MediaStore.Video.Media._ID);
            int _path = c.getColumnIndex(android.provider.MediaStore.Video.Media.DATA);
            int _title = c.getColumnIndex(android.provider.MediaStore.Video.Media.TITLE);
            int _duration = c.getColumnIndex(MediaStore.Video.Media.DURATION);
            do{
                VideoAlbum album = new VideoAlbum(c.getString(_title), c.getInt(_duration), c.getString(_path), c.getLong(_id));
                videoList.add(album);
                videoListId.add((int)(c.getLong(_id)));
            }while (c.moveToNext());
        }
        c.close();
        originalVideoList = (ArrayList<VideoAlbum>) videoList.clone();
        adapter.notifyDataSetChanged();
    }

    /**
     * So, User accept permission to access media content fro external storage
     */
    private void proceedAfterPermission(){
        retrieveVideoInfo();
    }

    /**
     * User didn't accept read permission media content that's why display
     * a simple message
     */
    private void proceedWithoutPermission(){
        ((LinearLayout)this.findViewById(R.id.full_screen_layout)).setVisibility(View.VISIBLE);
    }

    private void checkAllPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //Need to explain user about this permission.
                showPermissionExplanationDialog(false);

            }else if (permissionPref.getBoolean(Manifest.permission.READ_EXTERNAL_STORAGE,false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                showPermissionExplanationDialog(true);
            } else {
                //No explanation needed
                requestForExternalDataRead();
            }

            SharedPreferences.Editor editor = permissionPref.edit();
            editor.putBoolean(Manifest.permission.READ_EXTERNAL_STORAGE,true);
            editor.commit();
        }else{
            retrieveVideoInfo();
        }
    }
    //just request the permission
    private void requestForExternalDataRead(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
    }

    /**
      * Create and show permission explanation dialog to information user about this permission
      *
      * @param sendToSettings -- true if user previously canceled permission request with 'Dont Ask Again'
     */

    private void showPermissionExplanationDialog(final boolean sendToSettings){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.permission_msg_title))
                .setMessage(getString(R.string.permission_msg_detail))
                .setPositiveButton(getString(R.string.grant_permission), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //if user agree to grant file read permission then
                        // request for file read permission again.
                        if(!sendToSettings){
                            requestForExternalDataRead();
                        }else {
                            //Redirect to Settings after showing Information about why you need the permission
                            redirectToSetting();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        proceedWithoutPermission();
                    }
                })
                .setCancelable(false)
                .show();

    }

    private void redirectToSetting(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
        Toast.makeText(VideoListActivity.this, getString(R.string.redirect_to_settings), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, Do the contacts-related task you need to do.
                    proceedAfterPermission();

                } else {
                    // permission denied
                    checkAllPermissions();
                }
                return;
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // process the pending activity request...
        if (requestCode == REQUEST_PERMISSION_SETTING) {
            if (ActivityCompat.checkSelfPermission(VideoListActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //Got Permission
                //proceedAfterPermission();
            }
        }
    }

    private void updateListView(String charSequence) {
        if (charSequence == null || charSequence.length() == 0) {
            videoList.clear();
            videoList = (ArrayList<VideoAlbum>)originalVideoList.clone();
        }else {
            ArrayList<VideoAlbum> sortedVideoList = new ArrayList<>();
            for (VideoAlbum data : videoList) {
                if (data.getName().contains(charSequence)) {
                    if (!sortedVideoList.contains(data)) {
                        sortedVideoList.add(data);
                    }
                }
            }
            videoList.clear();
            videoList = (ArrayList<VideoAlbum>)sortedVideoList.clone();
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_video_list, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        int options = searchView.getImeOptions();
        searchView.setImeOptions(options| EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextChange(String newText)
            {

                System.out.println("on text change text: "+newText);
                updateListView(newText);
                return true;
            }
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // this is your adapter that will be filtered
                //  adapter.getFilter().filter(query);
                System.out.println("on query submit: "+query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return super.onCreateOptionsMenu(menu);
    }
}
