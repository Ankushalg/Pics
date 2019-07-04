package org.telegram.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

// <> Pics

public class MLaunchActivity  extends AppCompatActivity {
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
        String internalMemorySize = getTotalInternalMemorySize(false);
        String internalMemoryAvailable = getFilledInternalMemorySize(false);
        int internalMemoryPercentage = Integer.parseInt(String.valueOf((Long.parseLong(getFilledInternalMemorySize(true))*100)/Long.parseLong(getTotalInternalMemorySize(true))));
        String iTitle = "Internal Storage";
        String iSpace = internalMemoryAvailable + " / " + internalMemorySize;
        StorageObject sOI = new StorageObject(iTitle, iSpace, internalMemoryPercentage);
        mObject.add(sOI);
//        ToDo Add other storage types also.
//        if (externalMemoryAvailable()) {
//            String eMemorySize = getTotalExternalMemorySize(false);
//            String eMemoryAvailable = getAvailableExternalMemorySize(false);
//            int eMemoryPercentage = Integer.parseInt(String.valueOf((Long.parseLong(getAvailableExternalMemorySize(true)) * 100) / Long.parseLong(getTotalExternalMemorySize(true))));
//            String eTitle = "Internal Storage";
//            String eSpace = eMemoryAvailable + " / " + eMemorySize;
//            StorageObject sOE = new StorageObject(eTitle, eSpace, eMemoryPercentage);
//            mObject.add(sOE);
//        }
        return mObject;
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getAvailableInternalMemorySize(boolean isLong) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        if (isLong){
            return String.valueOf(availableBlocks * blockSize);
        } else {
            return formatSize(availableBlocks * blockSize);
        }
    }

    public static String getFilledInternalMemorySize(boolean isLong) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long totalBlocks = stat.getBlockCountLong();
        long filledBlocks = totalBlocks - availableBlocks;
        if (isLong){
            return String.valueOf(filledBlocks * blockSize);
        } else {
            return formatSize(filledBlocks * blockSize);
        }
    }

    public static String getTotalInternalMemorySize(boolean isLong) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        if (isLong){
            return String.valueOf(totalBlocks * blockSize);
        } else {
            return formatSize(totalBlocks * blockSize);
        }
    }

    public static String getAvailableExternalMemorySize(boolean isLong) {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long availableBlocks = stat.getAvailableBlocksLong();
            if (isLong){
                return String.valueOf(availableBlocks * blockSize);
            } else {
                return formatSize(availableBlocks * blockSize);
            }
        } else {
            return "Unknown Size";
        }
    }

    public static String getTotalExternalMemorySize(boolean isLong) {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSizeLong();
            long totalBlocks = stat.getBlockCountLong();
            if (isLong){
                return String.valueOf(totalBlocks * blockSize);
            } else {
                return formatSize(totalBlocks * blockSize);
            }
        } else {
            return "Unknown Size";
        }
    }

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

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView title, space, per;
            public ProgressBar pBar;
            MyViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.al_si_title);
                space = view.findViewById(R.id.al_si_space);
                per = view.findViewById(R.id.al_si_per);
                pBar = view.findViewById(R.id.al_si_pbar);
            }
        }

        public StorageObjectsAdapter(List<StorageObject> storageList) {
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
            holder.per.setText(String.valueOf(storageObject.getmPer()));
            holder.pBar.setProgress(storageObject.mPer);

        }

        @Override
        public int getItemCount() {
            return storageList.size();
        }
    }

    public class StorageObject {
        private String mTitle, mSpace;
        private int mPer;

        public StorageObject(String title, String space, int per) {
            this.mTitle = title;
            this.mSpace = space;
            this.mPer = per;
        }

        public String getmTitle() {
            return mTitle;
        }

        public void setmTitle(String mTitle) {
            this.mTitle = mTitle;
        }

        public String getmSpace() {
            return mSpace;
        }

        public void setmSpace(String mSpace) {
            this.mSpace = mSpace;
        }

        public int getmPer() {
            return mPer;
        }

        public void setmPer(int mPer) {
            this.mPer = mPer;
        }
    }

// </> Pics
}