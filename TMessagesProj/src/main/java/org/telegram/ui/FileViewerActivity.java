package org.telegram.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.telegram.messenger.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileViewerActivity  extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private FilesAdapter mAdapter;
    private String defaultStorage = Environment.getExternalStorageDirectory().getPath();
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
            defaultStorage = currentDir;
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
                if(aFile.isFile()){
                    mAdapter.add(new FileObject(aFile.getName(),formatSize(aFile.length()), aFile.getPath(), getFileType(aFile.getName())));
                } else {
                    mAdapter.add(new FileObject(aFile.getName(), aFile.listFiles().length + " item", aFile.getPath(), TYPE_FOLDER));
                }
            }
        } else {
            ts("Nothing Found...");
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private final int TYPE_FOLDER = 0,
            TYPE_FILE = 1,
            TYPE_APK = 2,
            TYPE_IMAGE = 3,
            TYPE_DOC = 5,
            TYPE_EXCEL = 6,
            TYPE_HTM = 7,
            TYPE_MUSIC = 8,
            TYPE_PDF = 9,
            TYPE_PPT = 10,
            TYPE_TXT = 11,
            TYPE_ZIP = 12,
            TYPE_VIDEO = 13;

    private int getFileType(String name) {
        if (name.contains(".")){
            String extension = name.substring(name.lastIndexOf(".")).toLowerCase();
            switch (extension) {
                case ".apk":
                    return TYPE_APK;
                case ".zip":
                case ".arj":
                case ".tar":
                case ".gz":
                case ".tgz":
                case ".rar":
                    return TYPE_ZIP;
                case ".jpg":
                case ".jpeg":
                case ".tiff":
                case ".png":
                    return TYPE_IMAGE;
                case ".doc":
                case ".docm":
                case ".rtf":
                case ".docb":
                case ".docx":
                    return TYPE_DOC;
                case ".xlsx":
                case ".xlsm":
                case ".xlsb":
                case ".xls":
                    return TYPE_EXCEL;
                case ".htm":
                case ".html":
                    return TYPE_HTM;
                case ".aa":
                case ".aac":
                case ".aax":
                case ".act":
                case ".aiff":
                case ".amr":
                case ".ape":
                case ".au":
                case ".awb":
                case ".dct":
                case ".dss":
                case ".dvf":
                case ".flac":
                case ".gsm":
                case ".iklax":
                case ".ivx":
                case ".m4a":
                case ".m4b":
                case ".mmf":
                case ".mp3":
                case ".mpc":
                case ".msv":
                case ".nmf":
                case ".nsf":
                case ".oga":
                case ".mogg":
                case ".opus":
                case ".ra":
                case ".sln":
                case ".tta":
                case ".voc":
                case ".vox":
                case ".wav":
                case ".wma":
                case ".wv":
                case ".8svx":
                    return TYPE_MUSIC;
                case ".pdf":
                    return TYPE_PDF;
                case ".ppt":
                case ".pptx":
                case ".pps":
                case ".ppsx":
                case ".pptm":
                    return TYPE_PPT;
                case ".txt":
                    return TYPE_TXT;
                case ".webm":
                case ".mkv":
                case ".flv":
                case ".vob":
                case ".ogv":
                case ".ogg":
                case ".drc":
                case ".gifv":
                case ".mng":
                case ".gif":
                case ".avi":
                case ".mts":
                case ".m2ts":
                case ".mov":
                case ".qt":
                case ".wmv":
                case ".yuv":
                case ".rm":
                case ".rmvb":
                case ".asf":
                case ".amv":
                case ".mp4":
                case ".m4p":
                case ".mpeg":
                case ".mpg":
                case ".mp2":
                case ".mpe":
                case ".mpv":
                case ".m2v":
                case ".m4v":
                case ".svi":
                case ".3gp":
                case ".3g2":
                case ".mxf":
                case ".roq":
                case ".nsv":
                case ".f4b":
                case ".f4v":
                case ".f4p":
                case ".f4a":
                    return TYPE_VIDEO;
                default:
                    return TYPE_FILE;
            }
        } else {
            return TYPE_FILE;
        }
    }

    public static String formatSize(long size) {
        String suffix = "B";

        if (size >= 1024) {
            suffix = "KB";
            size /= 1024;
            if (size >= 1024) {
                suffix = "MB";
                size /= 1024;
            }
        }
        float mSize;
        if (size > 1024){
            mSize = ((float) size)/1024;
            suffix = "GB";
            if(mSize > 1024) {
                suffix = "TB";
                mSize = mSize / 1024;
            }
        } else {
            mSize = (float) size;
        }

        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String fSize = decimalFormat.format(mSize);

        return fSize + " " + suffix;
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
                TextView tSubHead = convertView.findViewById(R.id.if_sub_title);
                ImageView icon = convertView.findViewById(R.id.if_icon);

                switch (fO.getType()){
                    case TYPE_APK:
                        icon.setImageResource(R.drawable.ic_p_format_apk_48dp);
                        break;
                    case TYPE_DOC:
                        icon.setImageResource(R.drawable.ic_p_format_doc_48dp);
                        break;
                    case TYPE_EXCEL:
                        icon.setImageResource(R.drawable.ic_p_format_excel_48dp);
                        break;
                    case TYPE_FILE:
                        icon.setImageResource(R.drawable.ic_p_format_file_48dp);
                        break;
                    case TYPE_FOLDER:
                        icon.setImageResource(R.drawable.ic_p_format_folder_48dp);
                        break;
                    case TYPE_HTM:
                        icon.setImageResource(R.drawable.ic_p_format_htm_48dp);
                        break;
                    case TYPE_IMAGE:
                        icon.setImageResource(R.drawable.ic_p_format_img_48dp);
                        break;
                    case TYPE_MUSIC:
                        icon.setImageResource(R.drawable.ic_p_format_music_48dp);
                        break;
                    case TYPE_PDF:
                        icon.setImageResource(R.drawable.ic_p_format_pdf_48dp);
                        break;
                    case TYPE_PPT:
                        icon.setImageResource(R.drawable.ic_p_format_ppt_48dp);
                        break;
                    case TYPE_TXT:
                        icon.setImageResource(R.drawable.ic_p_format_txt_48dp);
                        break;
                    case TYPE_VIDEO:
                        icon.setImageResource(R.drawable.ic_p_format_video_48dp);
                        break;
                    case TYPE_ZIP:
                        icon.setImageResource(R.drawable.ic_p_format_zip_48dp);
                        break;
                    default: icon.setImageResource(R.drawable.ic_p_format_file_48dp);
                }

                tHead.setText(fO.getTitle());
                tSubHead.setText(fO.getSubTitle());

                convertView.findViewById(R.id.if_main).setOnClickListener(v -> {
                    if (fO.getType() > 0){
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
        private String title, path, subTitle;
        private int type = 0;

        FileObject(String mTitle, String mSubTitle, String mPath, int mType) {
            this.title = mTitle;
            this.path = mPath;
            this.subTitle = mSubTitle;
            this.type = mType;
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

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }

}
