package org.telegram.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;

// <> Pics

public class MLaunchActivity extends AppCompatActivity {
    private ProgressBar statusPBar;
    private Button messOpenButton;
    private TextView statusT1, statusT2, bTApps, bTCallHis, bTContacts, bTSms, bTMore;
    private RecyclerView mStorageRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlaunch);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        findViewIds();
        setUpListeners();
    }

    private void setUpListeners() {
        messOpenButton.setOnClickListener(v -> {
            Intent i = new Intent(MLaunchActivity.this, PicsMainActivity.class);
            startActivity(i);
        });
    }

    private void findViewIds() {
        statusPBar = findViewById(R.id.al_main_status_p_bar);
        messOpenButton = findViewById(R.id.al_main_open_mess);
        statusT1 = findViewById(R.id.al_main_status_t1);
        statusT2 = findViewById(R.id.al_main_status_t2);
        bTApps = findViewById(R.id.al_main_backup_tools_apps);
        bTCallHis = findViewById(R.id.al_main_backup_tools_calls);
        bTContacts = findViewById(R.id.al_main_backup_tools_contacts);
        bTSms = findViewById(R.id.al_main_backup_tools_sms);
        bTMore = findViewById(R.id.al_main_backup_tools_more);
        mStorageRecyclerView = findViewById(R.id.al_storage_recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mStorageRecyclerView.setLayoutManager(layoutManager);
        List<StorageObject> attachedStorage = getAttachedStorage();
        StorageObjectsAdapter adapter = new StorageObjectsAdapter(attachedStorage);
        mStorageRecyclerView.setAdapter(adapter);
    }

    private List<StorageObject> getAttachedStorage() {
        List<StorageObject> mObject = new ArrayList<>();
        List<StorageUtils.StorageInfo> sO = StorageUtils.getStorageList();
        for(int i = 0; i < sO.size(); i++){
            mObject.add(new StorageObject(sO.get(i).getDisplayName(), sO.get(i).getSpace(), sO.get(i).getPer(),sO.get(i).getPath()));
        }
        return mObject;
    }

    public static class StorageUtils {
        private static final String TAG = "StorageUtils";
        public static class StorageInfo {

            public final String path;
            public final boolean readonly;
            public final boolean removable;
            public final int number;

            StorageInfo(String path, boolean readonly, boolean removable, int number) {
                this.path = path;
                this.readonly = readonly;
                this.removable = removable;
                this.number = number;
            }

            public String getSpace(){
                try{
                    StatFs stat = new StatFs(path);
                    long blockSize = stat.getBlockSizeLong();
                    long availableBlocks = stat.getAvailableBlocksLong();
                    long totalBlocks = stat.getBlockCountLong();
                    long filledBlocks = totalBlocks - availableBlocks;
                    return formatSize(filledBlocks * blockSize) + " / " + formatSize(totalBlocks * blockSize);
                } catch (Exception e){
                    e.printStackTrace();
                    return path;
                }
            }

            public String getPath() {
                return path;
            }

            public int getPer() {
                try {
                    StatFs stat = new StatFs(path);
                    long availableBlocks = stat.getAvailableBlocksLong();
                    long totalBlocks = stat.getBlockCountLong();
                    long filledBlocks = totalBlocks - availableBlocks;
                    return Integer.parseInt(String.valueOf((filledBlocks * 100) / totalBlocks));
                } catch (Exception e){
                    e.printStackTrace();
                    return -1;
                }
            }

            public String getDisplayName() {
                StringBuilder res = new StringBuilder();
                if (!removable) {
                    res.append("Internal SD card");
                } else if (number > 1) {
                    res.append("SD card " + number);
                } else {
                    res.append("SD card");
                }
                if (readonly) {
                    res.append(" (Read only)");
                }
                return res.toString();
            }
        }
        static List<StorageInfo> getStorageList() {

            List<StorageInfo> list = new ArrayList<>();
            String def_path = Environment.getExternalStorageDirectory().getPath();
            boolean def_path_removable = Environment.isExternalStorageRemovable();
            String def_path_state = Environment.getExternalStorageState();
            boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
            boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);

            HashSet<String> paths = new HashSet<>();
            int cur_removable_number = 1;

            if (def_path_available) {
                paths.add(def_path);
                list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
            }

            try (BufferedReader buf_reader = new BufferedReader(new FileReader("/proc/mounts"))) {
                String line;
                Log.d(TAG, "/proc/mounts");
                while ((line = buf_reader.readLine()) != null) {
                    Log.d(TAG, line);
                    if (line.contains("vfat") || line.contains("/mnt")) {
                        StringTokenizer tokens = new StringTokenizer(line, " ");
                        String unused = tokens.nextToken(); //device
                        String mount_point = tokens.nextToken(); //mount point
                        if (paths.contains(mount_point)) {
                            continue;
                        }
                        unused = tokens.nextToken(); //file system
                        List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                        boolean readonly = flags.contains("ro");

                        if (line.contains("/dev/block/vold")) {
                            if (!line.contains("/mnt/secure")
                                    && !line.contains("/mnt/asec")
                                    && !line.contains("/mnt/obb")
                                    && !line.contains("/dev/mapper")
                                    && !line.contains("tmpfs")) {
                                paths.add(mount_point);
                                list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
                            }
                        }
                    }
                }

            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return list;
        }
    }

//  Old Methods
//    private List<StorageObject> getAttachedStorageOld() {
//        List<StorageObject> mObject = new ArrayList<>();
//        String internalMemorySize = getTotalInternalMemorySize(false);
//        String internalMemoryAvailable = getFilledInternalMemorySize(false);
//        int internalMemoryPercentage = Integer.parseInt(String.valueOf((Long.parseLong(getFilledInternalMemorySize(true))*100)/Long.parseLong(getTotalInternalMemorySize(true))));
//        String iTitle = "Internal Storage";
//        String iSpace = internalMemoryAvailable + " / " + internalMemorySize;
////        StorageObject sOI = new StorageObject(iTitle, iSpace, internalMemoryPercentage);
////        mObject.add(sOI);
////        ToDo Add other storage types also.
////        if (externalMemoryAvailable()) {
////            String eMemorySize = getTotalExternalMemorySize(false);
////            String eMemoryAvailable = getAvailableExternalMemorySize(false);
////            int eMemoryPercentage = Integer.parseInt(String.valueOf((Long.parseLong(getAvailableExternalMemorySize(true)) * 100) / Long.parseLong(getTotalExternalMemorySize(true))));
////            String eTitle = "Internal Storage";
////            String eSpace = eMemoryAvailable + " / " + eMemorySize;
////            StorageObject sOE = new StorageObject(eTitle, eSpace, eMemoryPercentage);
////            mObject.add(sOE);
////        }
//        return mObject;
//    }
//
//    public static boolean externalMemoryAvailable() {
//        return android.os.Environment.getExternalStorageState().equals(
//                android.os.Environment.MEDIA_MOUNTED);
//    }
//
//    public static String getAvailableInternalMemorySize(boolean isLong) {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSizeLong();
//        long availableBlocks = stat.getAvailableBlocksLong();
//        if (isLong){
//            return String.valueOf(availableBlocks * blockSize);
//        } else {
//            return formatSize(availableBlocks * blockSize);
//        }
//    }
//
//    public static String getFilledInternalMemorySize(boolean isLong) {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSizeLong();
//        long availableBlocks = stat.getAvailableBlocksLong();
//        long totalBlocks = stat.getBlockCountLong();
//        long filledBlocks = totalBlocks - availableBlocks;
//        if (isLong){
//            return String.valueOf(filledBlocks * blockSize);
//        } else {
//            return formatSize(filledBlocks * blockSize);
//        }
//    }
//
//    public static String getTotalInternalMemorySize(boolean isLong) {
//        File path = Environment.getDataDirectory();
//        StatFs stat = new StatFs(path.getPath());
//        long blockSize = stat.getBlockSizeLong();
//        long totalBlocks = stat.getBlockCountLong();
//        if (isLong){
//            return String.valueOf(totalBlocks * blockSize);
//        } else {
//            return formatSize(totalBlocks * blockSize);
//        }
//    }
//
//    public static String getAvailableExternalMemorySize(boolean isLong) {
//        if (externalMemoryAvailable()) {
//            File path = Environment.getExternalStorageDirectory();
//            StatFs stat = new StatFs(path.getPath());
//            long blockSize = stat.getBlockSizeLong();
//            long availableBlocks = stat.getAvailableBlocksLong();
//            if (isLong){
//                return String.valueOf(availableBlocks * blockSize);
//            } else {
//                return formatSize(availableBlocks * blockSize);
//            }
//        } else {
//            return "Unknown Size";
//        }
//    }
//
//    public static String getTotalExternalMemorySize(boolean isLong) {
//        if (externalMemoryAvailable()) {
//            File path = Environment.getExternalStorageDirectory();
//            StatFs stat = new StatFs(path.getPath());
//            long blockSize = stat.getBlockSizeLong();
//            long totalBlocks = stat.getBlockCountLong();
//            if (isLong){
//                return String.valueOf(totalBlocks * blockSize);
//            } else {
//                return formatSize(totalBlocks * blockSize);
//            }
//        } else {
//            return "Unknown Size";
//        }
//    }


//  Old Methods

    public static String formatSize(long size) {
        String suffix = null;

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

    public class StorageObjectsAdapter extends RecyclerView.Adapter<StorageObjectsAdapter.MyViewHolder> {

        private List<StorageObject> storageList;

        private class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView title, space, per;
            private ProgressBar pBar;
            private RelativeLayout rMain;
            MyViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.al_si_title);
                space = view.findViewById(R.id.al_si_space);
                per = view.findViewById(R.id.al_si_per);
                pBar = view.findViewById(R.id.al_si_pbar);
                rMain = view.findViewById(R.id.al_si_main);
            }
        }

        StorageObjectsAdapter(List<StorageObject> storageList) {
            this.storageList = storageList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.al_storage_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            StorageObject storageObject = storageList.get(position);
            holder.title.setText(storageObject.getmTitle());
            holder.space.setText(storageObject.getmSpace());
            if(storageObject.getmPer() < 0){
                holder.per.setText("?");
                holder.pBar.setProgress(0);
            } else {
                holder.per.setText(String.valueOf(storageObject.getmPer()));
                holder.pBar.setProgress(storageObject.mPer);
            }
            holder.rMain.setOnClickListener(v -> {
                Intent i = new Intent(MLaunchActivity.this, FileViewerActivity.class);
                i.putExtra("cDir",storageObject.getmPath());
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return storageList.size();
        }
    }

    public class StorageObject {
        private String mTitle, mSpace, mPath;
        private int mPer;

        StorageObject(String title, String space, int per, String path) {
            this.mTitle = title;
            this.mSpace = space;
            this.mPer = per;
            this.mPath = path;
        }

        String getmTitle() {
            return mTitle;
        }

        public void setmTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        String getmSpace() {
            return mSpace;
        }

        public void setmSpace(String mSpace) {
            this.mSpace = mSpace;
        }

        int getmPer() {
            return mPer;
        }

        public void setmPer(int mPer) {
            this.mPer = mPer;
        }

        public String getmPath() {
            return mPath;
        }

        public void setmPath(String mPath) {
            this.mPath = mPath;
        }
    }

// </> Pics
}