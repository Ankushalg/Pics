package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.telegram.messenger.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileViewerActivity  extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private FilesAdapter mAdapter;
    private final String defaultStorage = Environment.getExternalStorageDirectory().getPath();
    private String currentDir = defaultStorage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        findViewIds();
        Intent i = getIntent();
        if(i.hasExtra("cDir")){
            currentDir = i.getStringExtra("cDir");
        }
        getCurrentDirContent();
    }

    @Override
    public void onBackPressed() {
        if(!currentDir.equals(defaultStorage)){
            currentDir = currentDir.substring(0,currentDir.lastIndexOf("/"));
            getCurrentDirContent();
        } else {
            super.onBackPressed();
        }
    }

    private void findViewIds() {
        mProgressBar = findViewById(R.id.fv_p_bar);
        ListView mFileViewerListView = findViewById(R.id.fv_list_view);

        List<FileObject> files = new ArrayList<>();
        mAdapter = new FilesAdapter(FileViewerActivity.this, R.layout.item_file, files);
        mFileViewerListView.setAdapter(mAdapter);

        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void getCurrentDirContent(){
        mAdapter.clear();
        mProgressBar.setVisibility(View.VISIBLE);
        ts(currentDir);
        File f = new File(currentDir);
        if(!f.exists()){
            Toast.makeText(FileViewerActivity.this,"Read Permission Denied...",Toast.LENGTH_LONG).show();
            finish();
        }
        File[] file = f.listFiles();
        if(file != null){
            for (File aFile : file) {
                mAdapter.add(new FileObject(aFile.getName(), aFile.getPath(), aFile.isFile()));
            }
        } else {
            ts("Nothing Found...");
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private void ts(String message){
        Toast.makeText(FileViewerActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public class FilesAdapter extends ArrayAdapter<FileObject> {
        FilesAdapter(Context context, int resource, List<FileObject> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.item_file, parent, false);
            }
            FileObject fO = getItem(position);
            if(fO != null){
                TextView tHead = convertView.findViewById(R.id.if_title);
                tHead.setText(fO.getTitle());

                convertView.findViewById(R.id.if_main).setOnClickListener(v -> {
                    if (fO.isFile){
                        ts (fO.title + " is a File.");
                    } else {
                        currentDir = fO.path;
                        getCurrentDirContent();
                    }
                });


            } else {
                convertView.findViewById(R.id.if_main).setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    public class FileObject {
        private String title, path;
        private boolean isFile;

        FileObject(String mTitle, String mPath, boolean mIsFile) {
            this.title = mTitle;
            this.path = mPath;
            this.isFile = mIsFile;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public boolean isFile() {
            return isFile;
        }

        public void setFile(boolean file) {
            isFile = file;
        }
    }

}
