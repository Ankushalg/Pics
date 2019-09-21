package org.telegram.ui;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
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
    private ArrayList<ListItem> items = new ArrayList<>(),
            otherItems = new ArrayList<>();

//    private boolean isPendingSelection = false;
//    private ArrayList<String> selectedFiles;
//    private int selectedAction = 0;
//    private final int FILE_VIEWER = 101;

    private boolean receiverRegistered = false;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = () -> {
                try {
//                    if (currentDir == null) {
                        listRoots();
//                    } else {
//                        listFiles(currentDir);
//                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            };
            if (Intent.ACTION_MEDIA_UNMOUNTED.equals(intent.getAction())) {
                mStorageRecyclerView.postDelayed(r, 1000);
            } else {
                r.run();
            }
        }
    };


    private class ListItem {
        int icon;
        int per;
        String title;
        String subtitle = "";
        String ext = "";
        String thumb;
        File file;
        long date;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mlaunch);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        if (!receiverRegistered) {
            receiverRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
            filter.addAction(Intent.ACTION_MEDIA_CHECKING);
            filter.addAction(Intent.ACTION_MEDIA_EJECT);
            filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            filter.addAction(Intent.ACTION_MEDIA_NOFS);
            filter.addAction(Intent.ACTION_MEDIA_REMOVED);
            filter.addAction(Intent.ACTION_MEDIA_SHARED);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
            filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            filter.addDataScheme("file");
            ApplicationLoader.applicationContext.registerReceiver(receiver, filter);
        }

        findViewIds();
        setUpListeners();
    }

    private void setUpLists() {
        StorageObjectsAdapter adapter = new StorageObjectsAdapter(items);
        mStorageRecyclerView.setAdapter(adapter);
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == FILE_VIEWER && resultCode == RESULT_OK && data != null){
//            isPendingSelection = true;
//            selectedFiles = data.getStringArrayListExtra("selectedFiles");
//            selectedAction = data.getIntExtra("action", 0);
//        }
//    }

    @SuppressLint("NewApi")
    private void listRoots() {
        items.clear();
        HashSet<String> paths = new HashSet<>();
        String defaultPath = Environment.getExternalStorageDirectory().getPath();
        boolean isDefaultPathRemovable = Environment.isExternalStorageRemovable();
        String defaultPathState = Environment.getExternalStorageState();
        if (defaultPathState.equals(Environment.MEDIA_MOUNTED) || defaultPathState.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            ListItem ext = new ListItem();
            if (Environment.isExternalStorageRemovable()) {
                ext.title = LocaleController.getString("SdCard", R.string.SdCard);
                ext.icon = R.drawable.ic_external_storage;
            } else {
                ext.title = LocaleController.getString("InternalStorage", R.string.InternalStorage);
                ext.icon = R.drawable.ic_storage;
            }
            ext.subtitle = getRootSubtitle(defaultPath);
            ext.per = getRootPer(defaultPath);
            ext.file = Environment.getExternalStorageDirectory();
            items.add(ext);
            paths.add(defaultPath);
        }

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    if (BuildVars.LOGS_ENABLED) {
                        FileLog.d(line);
                    }
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken();
                    String path = tokens.nextToken();
                    if (paths.contains(path)) {
                        continue;
                    }
                    if (line.contains("/dev/block/vold")) {
                        if (!line.contains("/mnt/secure") && !line.contains("/mnt/asec") && !line.contains("/mnt/obb") && !line.contains("/dev/mapper") && !line.contains("tmpfs")) {
                            if (!new File(path).isDirectory()) {
                                int index = path.lastIndexOf('/');
                                if (index != -1) {
                                    String newPath = "/storage/" + path.substring(index + 1);
                                    if (new File(newPath).isDirectory()) {
                                        path = newPath;
                                    }
                                }
                            }
                            paths.add(path);
                            try {
                                ListItem item = new ListItem();
                                if (path.toLowerCase().contains("sd")) {
                                    item.title = LocaleController.getString("SdCard", R.string.SdCard);
                                } else {
                                    item.title = LocaleController.getString("ExternalStorage", R.string.ExternalStorage);
                                }
                                item.icon = R.drawable.ic_external_storage;
                                item.subtitle = getRootSubtitle(path);
                                item.per = getRootPer(path);
                                item.file = new File(path);
                                items.add(item);
                            } catch (Exception e) {
                                FileLog.e(e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            FileLog.e(e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        }

        ListItem fs = new ListItem();
        fs.title = "/";
        fs.subtitle = LocaleController.getString("SystemRoot", R.string.SystemRoot);
        fs.icon = R.drawable.ic_directory;
        fs.file = new File("/");
        otherItems.add(fs);

        try {
            File telegramPath = new File(Environment.getExternalStorageDirectory(), "Pics");
            if (telegramPath.exists()) {
                fs = new ListItem();
                fs.title = "Pics";
                fs.subtitle = telegramPath.toString();
                fs.icon = R.drawable.ic_directory;
                fs.file = telegramPath;
                otherItems.add(fs);
            }
        } catch (Exception e) {
            FileLog.e(e);
        }

        fs = new ListItem();
        fs.title = LocaleController.getString("Gallery", R.string.Gallery);
        fs.subtitle = LocaleController.getString("GalleryInfo", R.string.GalleryInfo);
        fs.icon = R.drawable.ic_storage_gallery;
        fs.file = null;
        otherItems.add(fs);

        fs = new ListItem();
        fs.title = LocaleController.getString("AttachMusic", R.string.AttachMusic);
        fs.subtitle = LocaleController.getString("MusicInfo", R.string.MusicInfo);
        fs.icon = R.drawable.ic_storage_music;
        fs.file = null;
        otherItems.add(fs);

        setUpLists();
    }

    private int getRootPer(String path) {
        try {
            StatFs stat = new StatFs(path);
            long total = (long)stat.getBlockCount() * (long)stat.getBlockSize();
            long free = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
            if (total == 0) {
                return -1;
            }
            return Integer.parseInt(String.valueOf(((total - free)*100)/total));
        } catch (Exception e) {
            FileLog.e(e);
        }
        return -1;
    }

    private String getRootSubtitle(String path) {
        try {
            StatFs stat = new StatFs(path);
            long total = (long)stat.getBlockCount() * (long)stat.getBlockSize();
            long free = (long)stat.getAvailableBlocks() * (long)stat.getBlockSize();
            if (total == 0) {
                return "";
            }
            return LocaleController.formatString("FreeOfTotal", R.string.FreeOfTotal, AndroidUtilities.formatFileSize(free), AndroidUtilities.formatFileSize(total));
        } catch (Exception e) {
            FileLog.e(e);
        }
        return path;
    }

    private void setUpListeners() {
        messOpenButton.setOnClickListener(v -> {
            Intent i = new Intent(MLaunchActivity.this, LaunchActivity.class);
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
//        selectedFiles = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mStorageRecyclerView.setLayoutManager(layoutManager);
//        List<StorageObject> attachedStorage = getAttachedStorage();
//        StorageObjectsAdapter adapter = new StorageObjectsAdapter(attachedStorage);
//        mStorageRecyclerView.setAdapter(adapter);
        listRoots();
    }

//    private List<StorageObject> getAttachedStorage() {
//        List<StorageObject> mObject = new ArrayList<>();
//        List<StorageUtils.StorageInfo> sO = StorageUtils.getStorageList();
//        for(int i = 0; i < sO.size(); i++){
//            mObject.add(new StorageObject(sO.get(i).getDisplayName(), sO.get(i).getSpace(), sO.get(i).getPer(),sO.get(i).getPath()));
//        }
//        return mObject;
//    }

//    public static class StorageUtils {
//        private static final String TAG = "StorageUtils";
//        public static class StorageInfo {
//
//            public final String path;
//            public final boolean readonly;
//            public final boolean removable;
//            public final int number;
//
//            StorageInfo(String path, boolean readonly, boolean removable, int number) {
//                this.path = path;
//                this.readonly = readonly;
//                this.removable = removable;
//                this.number = number;
//            }
//
//            public String getSpace(){
//                try{
//                    StatFs stat = new StatFs(path);
//                    long blockSize = stat.getBlockSizeLong();
//                    long availableBlocks = stat.getAvailableBlocksLong();
//                    long totalBlocks = stat.getBlockCountLong();
//                    long filledBlocks = totalBlocks - availableBlocks;
//                    return formatSize(filledBlocks * blockSize) + " / " + formatSize(totalBlocks * blockSize);
//                } catch (Exception e){
//                    e.printStackTrace();
//                    return path;
//                }
//            }
//
//            public String getPath() {
//                return path;
//            }
//
//            public int getPer() {
//                try {
//                    StatFs stat = new StatFs(path);
//                    long availableBlocks = stat.getAvailableBlocksLong();
//                    long totalBlocks = stat.getBlockCountLong();
//                    long filledBlocks = totalBlocks - availableBlocks;
//                    return Integer.parseInt(String.valueOf((filledBlocks * 100) / totalBlocks));
//                } catch (Exception e){
//                    e.printStackTrace();
//                    return -1;
//                }
//            }
//
//            public String getDisplayName() {
//                StringBuilder res = new StringBuilder();
//                if (!removable) {
//                    res.append("Internal SD card");
//                } else if (number > 1) {
//                    res.append("SD card " + number);
//                } else {
//                    res.append("SD card");
//                }
//                if (readonly) {
//                    res.append(" (Read only)");
//                }
//                return res.toString();
//            }
//        }
//        static List<StorageInfo> getStorageList() {
//
//            List<StorageInfo> list = new ArrayList<>();
//            String def_path = Environment.getExternalStorageDirectory().getPath();
//            boolean def_path_removable = Environment.isExternalStorageRemovable();
//            String def_path_state = Environment.getExternalStorageState();
//            boolean def_path_available = def_path_state.equals(Environment.MEDIA_MOUNTED)
//                    || def_path_state.equals(Environment.MEDIA_MOUNTED_READ_ONLY);
//            boolean def_path_readonly = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
//
//            HashSet<String> paths = new HashSet<>();
//            int cur_removable_number = 1;
//
//            if (def_path_available) {
//                paths.add(def_path);
//                list.add(0, new StorageInfo(def_path, def_path_readonly, def_path_removable, def_path_removable ? cur_removable_number++ : -1));
//            }
//
//            try (BufferedReader buf_reader = new BufferedReader(new FileReader("/proc/mounts"))) {
//                String line;
//                Log.d(TAG, "/proc/mounts");
//                while ((line = buf_reader.readLine()) != null) {
//                    Log.d(TAG, line);
//                    if (line.contains("vfat") || line.contains("/mnt")) {
//                        StringTokenizer tokens = new StringTokenizer(line, " ");
//                        String unused = tokens.nextToken(); //device
//                        String mount_point = tokens.nextToken(); //mount point
//                        if (paths.contains(mount_point)) {
//                            continue;
//                        }
//                        unused = tokens.nextToken(); //file system
//                        List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
//                        boolean readonly = flags.contains("ro");
//
//                        if (line.contains("/dev/block/vold")) {
//                            if (!line.contains("/mnt/secure")
//                                    && !line.contains("/mnt/asec")
//                                    && !line.contains("/mnt/obb")
//                                    && !line.contains("/dev/mapper")
//                                    && !line.contains("tmpfs")) {
//                                paths.add(mount_point);
//                                list.add(new StorageInfo(mount_point, readonly, true, cur_removable_number++));
//                            }
//                        }
//                    }
//                }
//
//            } catch (FileNotFoundException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            return list;
//        }
//    }

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

//    public static String formatSize(long size) {
//        String suffix = null;
//
//        if (size >= 1024) {
//            suffix = "KB";
//            size /= 1024;
//            if (size >= 1024) {
//                suffix = "MB";
//                size /= 1024;
//            }
//        }
//        float mSize;
//        if (size > 1024){
//            mSize = ((float) size)/1024;
//            suffix = "GB";
//            if(mSize > 1024) {
//                suffix = "TB";
//                mSize = mSize / 1024;
//            }
//        } else {
//            mSize = (float) size;
//        }
//
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String fSize = decimalFormat.format(mSize);
//
//        return fSize + " " + suffix;
//    }

    public class StorageObjectsAdapter extends RecyclerView.Adapter<StorageObjectsAdapter.MyViewHolder> {

        private List<ListItem> storageList;

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

        StorageObjectsAdapter(List<ListItem> storageList) {
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
            ListItem storageObject = storageList.get(position);
            holder.title.setText(storageObject.title);
            holder.space.setText(storageObject.subtitle);
            if(storageObject.per < 0){
                holder.per.setText("?");
                holder.pBar.setProgress(0);
            } else {
                holder.per.setText(String.valueOf(storageObject.per));
                holder.pBar.setProgress(storageObject.per);
            }
            holder.rMain.setOnClickListener(v -> {
                Intent i = new Intent(MLaunchActivity.this, FileViewerActivity.class);
                i.putExtra("cDir",storageObject.file.getAbsolutePath());
                i.putExtra("cDirName", storageObject.title);
//                if (isPendingSelection) {
//                    i.putExtra("action",selectedAction);
//                    i.putStringArrayListExtra("selectedFiles",selectedFiles);
//                }
//                startActivityForResult(i, FILE_VIEWER);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return storageList.size();
        }
    }

// </> Pics
}