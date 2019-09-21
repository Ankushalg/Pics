package org.telegram.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

public class FileViewerActivity  extends AppCompatActivity {
    private ProgressBar mProgressBar;
    private FilesAdapter mAdapter;
    private TextView heading, subHeading;
    private LinearLayout selectionBottomNavi;
    private String defaultStorage = Environment.getExternalStorageDirectory().getPath();
    private String currentDir = defaultStorage;
    private static SharedMemory shared;
    private boolean isFirst = false;
    private static final String PICS_STRING_BREAK = "<<!PicsBreak12846>>:";
    private static final String PICS_ERROR = "<<!PicsError12846>>:";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        shared = new SharedMemory(this);
        findViewIds();
        setUpListeners();
        setUpSelectionNavigation();
        Intent i = getIntent();
        if(i.hasExtra("cDir")){
            currentDir = i.getStringExtra("cDir");
            defaultStorage = currentDir;
            subHeading.setText(currentDir);
        }
        if (i.hasExtra("cDirName")){
            heading.setText(i.getStringExtra("cDirName"));
        }
        getCurrentDirContent();
        isFirst = true;
        setUpRoots();
        checkSelection();
    }

    private void checkFOTask() {
        if(shared.getCFileAction() > 0){
            showFODialog();
        }
    }

    private void checkSelection() {
        if (shared.getFileAction() > 0){
            selectionBottomNavi.setVisibility(View.VISIBLE);
            naCopy.setVisibility(View.GONE);
            naCut.setVisibility(View.GONE);
            naRename.setVisibility(View.GONE);
            naDelete.setVisibility(View.GONE);
            naBackup.setVisibility(View.GONE);
            naShare.setVisibility(View.GONE);
            naHide.setVisibility(View.GONE);
            naShow.setVisibility(View.GONE);
            naNewFolder.setVisibility(View.VISIBLE);
            naPaste.setVisibility(View.VISIBLE);
            naCancel.setVisibility(View.VISIBLE);
        }
    }

    private TextView naCopy, naCut, naRename, naDelete, naHide, naShow, naBackup, naShare, naNewFolder, naPaste, naCancel, naOpen;
    private void setUpSelectionNavigation() {
        naCopy = findViewById(R.id.fv_sn_copy);
        naOpen = findViewById(R.id.fv_sn_open);
        naCut = findViewById(R.id.fv_sn_cut);
        naRename = findViewById(R.id.fv_sn_rename);
        naDelete = findViewById(R.id.fv_sn_delete);
        naBackup = findViewById(R.id.fv_sn_backup);
        naShare = findViewById(R.id.fv_sn_share);
        naHide = findViewById(R.id.fv_sn_hide);
        naShow = findViewById(R.id.fv_sn_show);
        naNewFolder = findViewById(R.id.fv_sn_new);
        naPaste = findViewById(R.id.fv_sn_paste);
        naCancel = findViewById(R.id.fv_sn_cancel);

        naPaste.setOnClickListener(v -> {
            shared.setFileDestination(currentDir);
//            Intent i =  new Intent(FileViewerActivity.this, FileOperationsActivity.class);
//            startActivity(i);
            doFileTask();
            naCopy.setVisibility(View.VISIBLE);
            naCut.setVisibility(View.VISIBLE);
            naRename.setVisibility(View.VISIBLE);
            naDelete.setVisibility(View.VISIBLE);
            naBackup.setVisibility(View.VISIBLE);
            naShare.setVisibility(View.VISIBLE);
            naHide.setVisibility(View.VISIBLE);
            naShow.setVisibility(View.VISIBLE);
            naNewFolder.setVisibility(View.GONE);
            naPaste.setVisibility(View.GONE);
            naCancel.setVisibility(View.GONE);
            selectionBottomNavi.setVisibility(View.GONE);
        });
        naShare.setOnClickListener(v -> {
            shareSelection();
        });
        naOpen.setOnClickListener(v -> {
            openFile(selectedFiles.get(0), TYPE_FILE);
        });
        naNewFolder.setOnClickListener(v -> {
            openMenu(3);
        });
        naCancel.setOnClickListener(v -> {
            shared.setFileTask(null);
            shared.setFileAction(0);
            shared.setFileDestination("");
            naCopy.setVisibility(View.VISIBLE);
            naCut.setVisibility(View.VISIBLE);
            naRename.setVisibility(View.VISIBLE);
            naDelete.setVisibility(View.VISIBLE);
            naBackup.setVisibility(View.VISIBLE);
            naShare.setVisibility(View.VISIBLE);
            naHide.setVisibility(View.VISIBLE);
            naShow.setVisibility(View.VISIBLE);
            naNewFolder.setVisibility(View.GONE);
            naPaste.setVisibility(View.GONE);
            naCancel.setVisibility(View.GONE);
            selectionBottomNavi.setVisibility(View.GONE);
        });
        naCopy.setOnClickListener(v -> {
            shared.setFileTask(new HashSet<>(selectedFiles));
            selectedFiles.clear();
            isSelection = false;
            shared.setFileAction(1);
            getCurrentDirContent();
            naCopy.setVisibility(View.GONE);
            naCut.setVisibility(View.GONE);
            naRename.setVisibility(View.GONE);
            naDelete.setVisibility(View.GONE);
            naBackup.setVisibility(View.GONE);
            naShare.setVisibility(View.GONE);
            naHide.setVisibility(View.GONE);
            naOpen.setVisibility(View.GONE);
            naShow.setVisibility(View.GONE);
            naNewFolder.setVisibility(View.VISIBLE);
            naPaste.setVisibility(View.VISIBLE);
            naCancel.setVisibility(View.VISIBLE);
        });
        naCut.setOnClickListener(v -> {
            shared.setFileTask(new HashSet<>(selectedFiles));
            selectedFiles.clear();
            isSelection = false;
            shared.setFileAction(2);
            getCurrentDirContent();
            naCopy.setVisibility(View.GONE);
            naCut.setVisibility(View.GONE);
            naRename.setVisibility(View.GONE);
            naDelete.setVisibility(View.GONE);
            naBackup.setVisibility(View.GONE);
            naOpen.setVisibility(View.GONE);
            naShare.setVisibility(View.GONE);
            naHide.setVisibility(View.GONE);
            naShow.setVisibility(View.GONE);
            naNewFolder.setVisibility(View.VISIBLE);
            naPaste.setVisibility(View.VISIBLE);
            naCancel.setVisibility(View.VISIBLE);
        });
        naDelete.setOnClickListener(v -> {
           openMenu(5);
        });
        naRename.setOnClickListener(v -> {
            openMenu(6);
        });
        naHide.setOnClickListener(v -> {
            int fCount = 0, skipped = 0;
            for (int i = 0; i < selectedFiles.size(); i++){
                File f = new File(selectedFiles.get(i));
                if (!f.getName().startsWith(".")){
                    File f2 = new File(f.getParentFile().getAbsolutePath(),"." + f.getName());
                    if (f2.exists()){
                        skipped++;
                    } else {
                        if(f.renameTo(f2)){
                            fCount++;
                        }
                    }
                }
            }
            if (skipped > 0){
                ts("Some Files with same name are already in hidden files list. We had skipped those files to save data from any loss. " + fCount + " / " + selectedFiles.size() + " files hidden");
            } else {
                ts(fCount + " / " + selectedFiles.size() + " files hidden");
            }
            selectedFiles.clear();
            isSelection = false;
            getCurrentDirContent();
            if(shared.getFileAction() > 0){
                naCopy.setVisibility(View.GONE);
                naCut.setVisibility(View.GONE);
                naRename.setVisibility(View.GONE);
                naDelete.setVisibility(View.GONE);
                naBackup.setVisibility(View.GONE);
                naShare.setVisibility(View.GONE);
                naHide.setVisibility(View.GONE);
                naShow.setVisibility(View.GONE);
                naNewFolder.setVisibility(View.VISIBLE);
                naPaste.setVisibility(View.VISIBLE);
                naCancel.setVisibility(View.VISIBLE);
            } else {
                selectionBottomNavi.setVisibility(View.GONE);
            }
        });
        naShow.setOnClickListener(v -> {
            int fCount = 0, skipped = 0;
            for (int i = 0; i < selectedFiles.size(); i++) {
                File f = new File(selectedFiles.get(i));
                if (f.getName().startsWith(".")) {
                    File f2 = new File(f.getParentFile().getAbsolutePath(), f.getName().replaceFirst(".", ""));
                    if (f2.exists()){
                        skipped++;
                    } else {
                        if (f.renameTo(f2)) {
                            fCount++;
                        }
                    }
                }
            }
            if (skipped > 0){
                ts("Some Files with same name are already in visible files list. We had skipped those files to save data from any loss. " + fCount + " / " + selectedFiles.size() + " files hidden" );
            } else {
                ts(fCount + " / " + selectedFiles.size() + " files hidden");
            }
            selectedFiles.clear();
            isSelection = false;
            getCurrentDirContent();
            if(shared.getFileAction() > 0){
                naCopy.setVisibility(View.GONE);
                naCut.setVisibility(View.GONE);
                naRename.setVisibility(View.GONE);
                naDelete.setVisibility(View.GONE);
                naBackup.setVisibility(View.GONE);
                naShare.setVisibility(View.GONE);
                naHide.setVisibility(View.GONE);
                naShow.setVisibility(View.GONE);
                naNewFolder.setVisibility(View.VISIBLE);
                naPaste.setVisibility(View.VISIBLE);
                naCancel.setVisibility(View.VISIBLE);
            } else {
                selectionBottomNavi.setVisibility(View.GONE);
            }
        });
    }

    private void deleteSelectedFiles(){
        shared.setFileTask(new HashSet<>(selectedFiles));
        shared.setFileAction(3);
//        Intent i =  new Intent(FileViewerActivity.this, FileOperationsActivity.class);
//        startActivity(i);
        doFileTask();
        selectedFiles.clear();
        isSelection = false;
        naCopy.setVisibility(View.VISIBLE);
        naCut.setVisibility(View.VISIBLE);
        naRename.setVisibility(View.VISIBLE);
        naDelete.setVisibility(View.VISIBLE);
        naBackup.setVisibility(View.VISIBLE);
        naShare.setVisibility(View.VISIBLE);
        naHide.setVisibility(View.VISIBLE);
        naShow.setVisibility(View.VISIBLE);
        naNewFolder.setVisibility(View.GONE);
        naPaste.setVisibility(View.GONE);
        naCancel.setVisibility(View.GONE);
        selectionBottomNavi.setVisibility(View.GONE);
    }

    private void setUpRoots() {
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFOTask();
        if(isFirst){
            getCurrentDirContent();
        }
    }

    private ArrayList<ListItem> items = new ArrayList<>();
    private boolean receiverRegistered = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            Runnable r = () -> {
                try {
                    listRoots();
                } catch (Exception e) {
                    FileLog.e(e);
                }
            };
            r.run();
        }
    };

    private class ListItem {
        int icon;
        int per;
        String title;
        String subtitle = "";
        String thumb;
        File file;
        long date;
    }

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
        checkLists();
    }

    private void checkLists() {
        boolean isInserted = false;
        for (int i = 0; i < items.size(); i++){
            if(items.get(i).file.getAbsolutePath().equals(defaultStorage))
                isInserted = true;
        }
        if (!isInserted){
            ts(heading.getText().toString().trim() + " has been removed.");
            defaultStorage = currentDir = items.get(0).file.getAbsolutePath();
            subHeading.setText(currentDir);
            heading.setText(items.get(0).title);
            getCurrentDirContent();
        }
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
        findViewById(R.id.fv_info).setOnClickListener(v -> {
//            Intent i = new Intent(FileViewerActivity.this, BackupHelpActivity.class);
//            startActivity(i);
        });
        findViewById(R.id.fv_close).setOnClickListener(v -> {
            finishIt();
        });
        findViewById(R.id.fv_menu).setOnClickListener(v -> {
            openMenu(0);
        });
    }

    private void openMenu(int type) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(FileViewerActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.popup_file_viewer_menu, null);
        TextView newTab = mView.findViewById(R.id.pfv_add);
        TextView refreshTab = mView.findViewById(R.id.pfv_refresh);
        TextView newHead = mView.findViewById(R.id.pfv_new);
        TextView newFolderTab = mView.findViewById(R.id.pfv_new_folder);
        TextView newFileTab = mView.findViewById(R.id.pfv_new_file);
        EditText name = mView.findViewById(R.id.pfv_name);
        Button bCancel = mView.findViewById(R.id.pfv_b_cancel);
        Button bOk = mView.findViewById(R.id.pfv_b_okk);

        if (type == 0){
            name.setVisibility(View.GONE);
            bCancel.setVisibility(View.GONE);
            bOk.setVisibility(View.GONE);
            newHead.setVisibility(View.GONE);
            newFileTab.setVisibility(View.GONE);
            newFolderTab.setVisibility(View.GONE);
            newTab.setVisibility(View.VISIBLE);
            refreshTab.setVisibility(View.VISIBLE);
        } else if (type == 1){
            name.setVisibility(View.GONE);
            bCancel.setVisibility(View.GONE);
            bOk.setVisibility(View.GONE);
            newTab.setVisibility(View.GONE);
            refreshTab.setVisibility(View.GONE);
            newHead.setVisibility(View.VISIBLE);
            newFileTab.setVisibility(View.VISIBLE);
            newFolderTab.setVisibility(View.VISIBLE);
        } else if (type == 3 || type == 4){
            newTab.setVisibility(View.GONE);
            refreshTab.setVisibility(View.GONE);
            newFileTab.setVisibility(View.GONE);
            newFolderTab.setVisibility(View.GONE);
            newHead.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            bCancel.setVisibility(View.VISIBLE);
            bOk.setVisibility(View.VISIBLE);
        } else if (type == 5){
            newTab.setVisibility(View.GONE);
            refreshTab.setVisibility(View.GONE);
            newFileTab.setVisibility(View.VISIBLE);
            newFolderTab.setVisibility(View.GONE);
            name.setVisibility(View.GONE);
            newHead.setVisibility(View.VISIBLE);
            bCancel.setVisibility(View.VISIBLE);
            bOk.setVisibility(View.VISIBLE);
            newHead.setText("Delete");
            if (selectedFiles.size() < 2){
                File f = new File(selectedFiles.get(0));
                String message;
                if (f.isDirectory()){
                    message = "Are you sure to delete the folder " + f.getName();
                } else {
                    message = "Are you sure to delete " + f.getName();
                }
                newFileTab.setText(message);
            } else {
                File file = new File(selectedFiles.get(0));
                String message = "Are you sure to delete the "
                        + file.getName().substring(0,4)
                        + "..." + " ("
                        + selectedFiles.size()
                        + ")?";
                newFileTab.setText(message);
            }
        } else if (type == 6){
            newTab.setVisibility(View.GONE);
            refreshTab.setVisibility(View.GONE);
            newFileTab.setVisibility(View.GONE);
            newFolderTab.setVisibility(View.GONE);
            newHead.setVisibility(View.VISIBLE);
            name.setVisibility(View.VISIBLE);
            bCancel.setVisibility(View.VISIBLE);
            bOk.setVisibility(View.VISIBLE);
            newHead.setText("Rename");
        }

        mBuilder.setView(mView);
        final AlertDialog dialog = mBuilder.create();
        dialog.show();
        if (type == 6){
            if (selectedFiles.size() > 1){
                ts("Multiple Files Rename is not supported now and will be added in upcoming Feature Updates.");
                dialog.dismiss();
            }
            File f = new File(selectedFiles.get(0));
            name.setText(f.getName());
        }
        newTab.setOnClickListener(v -> {
            dialog.dismiss();
            openMenu(1);
        });
        refreshTab.setOnClickListener(v -> {
            dialog.dismiss();
            getCurrentDirContent();
        });
        newFolderTab.setOnClickListener(v -> {
            dialog.dismiss();
            openMenu(3);
        });
        newFileTab.setOnClickListener(v -> {
            if (type != 5){
                dialog.dismiss();
                openMenu(4);
            }
        });
        bCancel.setOnClickListener(v -> {
            if (type == 5){
                isSelection = false;
                selectedFiles.clear();
                if(shared.getFileAction() > 0){
                    naCopy.setVisibility(View.GONE);
                    naCut.setVisibility(View.GONE);
                    naRename.setVisibility(View.GONE);
                    naDelete.setVisibility(View.GONE);
                    naBackup.setVisibility(View.GONE);
                    naShare.setVisibility(View.GONE);
                    naHide.setVisibility(View.GONE);
                    naShow.setVisibility(View.GONE);
                    naNewFolder.setVisibility(View.VISIBLE);
                    naPaste.setVisibility(View.VISIBLE);
                    naCancel.setVisibility(View.VISIBLE);
                } else {
                    selectionBottomNavi.setVisibility(View.GONE);
                }
                getCurrentDirContent();
            }
            dialog.dismiss();
        });
        bOk.setOnClickListener(v -> {
            if (type == 5){
                deleteSelectedFiles();
                dialog.dismiss();
            } else {
                String nameText = name.getText().toString().trim();
                if(nameText.length() > 0
                        && !nameText.contains("|")
                        && !nameText.contains("*")
                        && !nameText.contains("\\")
                        && !nameText.contains("?")
                        && !nameText.contains("<")
                        && !nameText.contains(">")
                        && !nameText.contains("\"")
                        && !nameText.contains(":")
                        && !nameText.contains("/")
                ) {
                    dialog.dismiss();
                    if (type == 3){
                        createFile(true, nameText);
                    } else if (type == 4){
                        createFile(false, nameText);
                    } else if (type == 6){
                       File f = new File(selectedFiles.get(0));
                       File f2 = new File(f.getParentFile().getAbsolutePath(), nameText);
                       if (!f2.exists()){
                           if(f.renameTo(f2)){
                               ts ("Renamed...");
                           } else {
                               ts ("Unable to rename File");
                           }
                       } else {
                           ts("File with same name already exists.");
                       }
                       isSelection = false;
                       selectedFiles.clear();
                        if(shared.getFileAction() > 0){
                            naCopy.setVisibility(View.GONE);
                            naCut.setVisibility(View.GONE);
                            naRename.setVisibility(View.GONE);
                            naDelete.setVisibility(View.GONE);
                            naBackup.setVisibility(View.GONE);
                            naShare.setVisibility(View.GONE);
                            naHide.setVisibility(View.GONE);
                            naShow.setVisibility(View.GONE);
                            naNewFolder.setVisibility(View.VISIBLE);
                            naPaste.setVisibility(View.VISIBLE);
                            naCancel.setVisibility(View.VISIBLE);
                        } else {
                            selectionBottomNavi.setVisibility(View.GONE);
                        }
                       getCurrentDirContent();
                    }
                } else {
                    if (type == 3)
                        ts("The folder name could not contain the char * \\ / \" : ? | < >");
                    else if (type == 4)
                        ts("The file name could not contain the char * \\ / \" : ? | < >");
                    else if (type == 6)
                        ts("The file name could not contain the char * \\ / \" : ? | < >");
                }
            }
        });

    }

    private void createFile(boolean isFolder, String name) {
        File file = new File(currentDir, name);
        boolean isExist = file.exists();
        if (isExist){
            if (isFolder)
                ts ("Folder with same name already exists.");
            else
                ts("File with same name already exists.");
        } else {
            boolean isCreated;
            try {
                if (isFolder){
                    isCreated = file.mkdirs();
                } else {
                    isCreated = file.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
                isCreated = false;
            }

            if (isCreated){
                ts(name + " is Created.");
            } else {
                ts("Unable to create " + name);
            }
        }
        getCurrentDirContent();
    }

    @Override
    public void onBackPressed() {
        if (isSelection){
            selectedFiles.clear();
            isSelection = false;
            if(shared.getFileAction() > 0){
                naCopy.setVisibility(View.GONE);
                naCut.setVisibility(View.GONE);
                naRename.setVisibility(View.GONE);
                naDelete.setVisibility(View.GONE);
                naBackup.setVisibility(View.GONE);
                naShare.setVisibility(View.GONE);
                naHide.setVisibility(View.GONE);
                naShow.setVisibility(View.GONE);
                naNewFolder.setVisibility(View.VISIBLE);
                naPaste.setVisibility(View.VISIBLE);
                naCancel.setVisibility(View.VISIBLE);
            } else {
                selectionBottomNavi.setVisibility(View.GONE);
            }
            getCurrentDirContent();
        } else {
            if(!currentDir.equals(defaultStorage)){
                currentDir = currentDir.substring(0,currentDir.lastIndexOf("/"));
                subHeading.setText(currentDir);
                getCurrentDirContent();
            } else {
                finishIt();
                super.onBackPressed();
            }
        }
    }

    private void finishIt() {
        finish();
    }

    private void findViewIds() {
        heading = findViewById(R.id.fv_heading);
        selectionBottomNavi = findViewById(R.id.fv_selection_navigation);
        subHeading = findViewById(R.id.fv_sub_heading);
        mProgressBar = findViewById(R.id.fv_p_bar);
        ListView mFileViewerListView = findViewById(R.id.fv_list_view);
        selectedFiles = new ArrayList<>();
        List<FileObject> files = new ArrayList<>();
        mAdapter = new FilesAdapter(FileViewerActivity.this, R.layout.item_file, files);
        mFileViewerListView.setAdapter(mAdapter);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void getCurrentDirContent(){
        ArrayList<File> files = new ArrayList<>();
        mAdapter.clear();
        mProgressBar.setVisibility(View.VISIBLE);
        File f = new File(currentDir);
        if(!f.exists()){
            Toast.makeText(FileViewerActivity.this,"Read Permission Denied...",Toast.LENGTH_LONG).show();
            finishIt();
        }
        File[] file = f.listFiles();
        if(file != null){
            if (file.length > 1){
                Arrays.sort(file, (o1, o2) -> o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase()));
            }
            for (File aFile : file) {
                if(aFile.isFile()){
                    files.add(aFile);
                } else {
                    mAdapter.add(new FileObject(aFile.getName(), aFile.listFiles().length + " item", aFile.getPath(), TYPE_FOLDER));
                }
            }
            if (files.size() > 0){
                for (File aFile: files){
                    mAdapter.add(new FileObject(aFile.getName(),formatSize(aFile.length()), aFile.getPath(), getFileType(aFile.getName())));
                }
            }
        } else {
            ts("Empty Folder...");
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

    private ArrayList<String> selectedFiles;
    private boolean isSelection = false;

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
                LinearLayout  main = convertView.findViewById(R.id.if_main);
                TextView tHead = convertView.findViewById(R.id.if_title);
                TextView tSubHead = convertView.findViewById(R.id.if_sub_title);
                ImageView icon = convertView.findViewById(R.id.if_icon);
                ImageView cloud = convertView.findViewById(R.id.if_status);
                LinearLayout hidden = convertView.findViewById(R.id.if_hidden);
                switch (fO.type){
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

                if(fO.title.startsWith(".")){ hidden.setVisibility(View.VISIBLE); } else { hidden.setVisibility(View.GONE); }

                tHead.setText(fO.title);
                tSubHead.setText(fO.subTitle);

                if (fO.isSelected){
                    main.setBackgroundColor(Color.parseColor("#00B7FF"));
                } else {
                    main.setBackgroundColor(Color.parseColor("#ffffff"));
                }

                icon.setOnClickListener(v -> {
                    isSelection = true;
                    if(shared.getFileAction() > 0){
                        naCopy.setVisibility(View.VISIBLE);
                        naCut.setVisibility(View.VISIBLE);
                        naRename.setVisibility(View.VISIBLE);
                        naDelete.setVisibility(View.VISIBLE);
                        naBackup.setVisibility(View.VISIBLE);
                        naShare.setVisibility(View.VISIBLE);
                        naHide.setVisibility(View.VISIBLE);
                        naShow.setVisibility(View.VISIBLE);
                        naNewFolder.setVisibility(View.GONE);
                        naPaste.setVisibility(View.GONE);
                        naCancel.setVisibility(View.GONE);
                    } else {
                        selectionBottomNavi.setVisibility(View.VISIBLE);
                    }
                    if (selectedFiles.contains(fO.path)){
                        selectedFiles.remove(fO.path);
                        setUpOpenAs();
                        main.setBackgroundColor(Color.parseColor("#ffffff"));
                        fO.isSelected = false;
                        if (selectedFiles.size() == 0){
                            if(shared.getFileAction() > 0){
                                naCopy.setVisibility(View.GONE);
                                naCut.setVisibility(View.GONE);
                                naRename.setVisibility(View.GONE);
                                naDelete.setVisibility(View.GONE);
                                naBackup.setVisibility(View.GONE);
                                naShare.setVisibility(View.GONE);
                                naHide.setVisibility(View.GONE);
                                naShow.setVisibility(View.GONE);
                                naNewFolder.setVisibility(View.VISIBLE);
                                naPaste.setVisibility(View.VISIBLE);
                                naCancel.setVisibility(View.VISIBLE);
                            } else {
                                selectionBottomNavi.setVisibility(View.GONE);
                            }
                            isSelection = false;
                        }
                    } else {
                        selectedFiles.add(fO.path);
                        setUpOpenAs();
                        fO.isSelected = true;
                        main.setBackgroundColor(Color.parseColor("#00B7FF"));
                    }
                });

                main.setOnLongClickListener(v -> {
                    isSelection = true;
                    if(shared.getFileAction() > 0){
                        naCopy.setVisibility(View.VISIBLE);
                        naCut.setVisibility(View.VISIBLE);
                        naRename.setVisibility(View.VISIBLE);
                        naDelete.setVisibility(View.VISIBLE);
                        naBackup.setVisibility(View.VISIBLE);
                        naShare.setVisibility(View.VISIBLE);
                        naHide.setVisibility(View.VISIBLE);
                        naShow.setVisibility(View.VISIBLE);
                        naNewFolder.setVisibility(View.GONE);
                        naPaste.setVisibility(View.GONE);
                        naCancel.setVisibility(View.GONE);
                    } else {
                        selectionBottomNavi.setVisibility(View.VISIBLE);
                    }
                    if (!selectedFiles.contains(fO.path)){
                        selectedFiles.add(fO.path);
                        setUpOpenAs();
                        fO.isSelected = true;
                        main.setBackgroundColor(Color.parseColor("#00B7FF"));
                    }
                    return true;
                });

                main.setOnClickListener(v -> {
                    if (isSelection){
                        if (selectedFiles.contains(fO.path)){
                            selectedFiles.remove(fO.path);
                            setUpOpenAs();
                            main.setBackgroundColor(Color.parseColor("#ffffff"));
                            fO.isSelected = false;
                            if (selectedFiles.size() == 0){
                                isSelection = false;
                                if(shared.getFileAction() > 0){
                                    naCopy.setVisibility(View.GONE);
                                    naCut.setVisibility(View.GONE);
                                    naRename.setVisibility(View.GONE);
                                    naDelete.setVisibility(View.GONE);
                                    naBackup.setVisibility(View.GONE);
                                    naShare.setVisibility(View.GONE);
                                    naHide.setVisibility(View.GONE);
                                    naShow.setVisibility(View.GONE);
                                    naNewFolder.setVisibility(View.VISIBLE);
                                    naPaste.setVisibility(View.VISIBLE);
                                    naCancel.setVisibility(View.VISIBLE);
                                } else {
                                    selectionBottomNavi.setVisibility(View.GONE);
                                }
                            }
                        } else {
                            selectedFiles.add(fO.path);
                            setUpOpenAs();
                            main.setBackgroundColor(Color.parseColor("#00B7FF"));
                            fO.isSelected = true;
                        }
                    } else {
                        if (fO.type > 0){
                            openFile(fO.path, fO.type);
                        } else {
                            currentDir = fO.path;
                            subHeading.setText(currentDir);
                            getCurrentDirContent();
                        }
                    }
                });

                cloud.setOnClickListener(v -> {

                });

            } else {
                convertView.findViewById(R.id.if_main).setVisibility(View.GONE);
            }
            return convertView;
        }
    }

    private void setUpOpenAs() {
        if (selectedFiles.size() == 1){
            File f = new File(selectedFiles.get(0));
            if (!f.isDirectory()){
                naOpen.setVisibility(View.VISIBLE);
            } else {
                naOpen.setVisibility(View.GONE);
            }
        } else {
            naOpen.setVisibility(View.GONE);
        }
    }

    class FileObject {
        private String title, path, subTitle;
        private int type = 0;
        private boolean isSelected = false;
        FileObject(String mTitle, String mSubTitle, String mPath, int mType) {
            this.title = mTitle;
            this.path = mPath;
            this.subTitle = mSubTitle;
            this.type = mType;
        }
    }

    private void addItemToBackup(String path){
        String filename = "filesToBackup";
        String oldFileContents = getBackupFileContent();
        String fileContents;
                if(oldFileContents.length() > 0){
            fileContents = getBackupFileContent() + "\n" + path;
        } else {
            fileContents = path;
        }
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(fileContents.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBackupFileContent() {
        String filename = "filesToBackup";
        String fileContents = "";
        FileInputStream inputStream;
        try{
            inputStream = openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            fileContents = sb.toString();
            return fileContents;
        } catch (Exception e){
            return "";
        }
    }

    private void openFile(String path, int type) {
        File f = new File(path);
        Uri uri = FileProvider.getUriForFile(FileViewerActivity.this,
                FileViewerActivity.this.getApplicationContext().getPackageName() + ".provider", f);
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            switch (type) {
                case TYPE_VIDEO:
                    intent.setDataAndType(uri, "video/*");
                    break;
                case TYPE_ZIP:
                    intent.setDataAndType(uri, "application/x-rar-compressed");
                    break;
                case TYPE_TXT:
                    intent.setDataAndType(uri, "text/plain");
                    break;
                case TYPE_PPT:
                    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                    break;
                case TYPE_PDF:
                    intent.setDataAndType(uri, "application/pdf");
                    break;
                case TYPE_MUSIC:
                    intent.setDataAndType(uri, "audio/*");
                    break;
                case TYPE_HTM:
                    intent.setDataAndType(uri, "text/html");
                    break;
                case TYPE_EXCEL:
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                    break;
                case TYPE_DOC:
                    intent.setDataAndType(uri, "application/msword");
                    break;
                case TYPE_IMAGE:
                    intent.setDataAndType(uri, "image/*");
                    break;
                case TYPE_APK:
                    intent.setDataAndType(uri, "application/vnd.android.package-archive");
                    break;
                case TYPE_FILE:
                    intent.setDataAndType(uri, "*/*");
                    break;
                default:
                    intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            FileViewerActivity.this.startActivity(intent);
            Intent chooser = Intent.createChooser(intent, "Open With");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        } catch (ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "*/*");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//            FileViewerActivity.this.startActivity(intent);
            Intent chooser = Intent.createChooser(intent, "Open With");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(chooser);
            }
        }
    }

    private boolean isFilesUnderLimit = true;
    private void shareSelection(){
        isFilesUnderLimit = true;
        ArrayList<Uri> uris = new ArrayList<>();
        for (int i = 0; i < selectedFiles.size(); i++){
            File f = new File(selectedFiles.get(i));
            if (f.isDirectory()) {
                ArrayList<Uri> arrayList = getFilesFromLocation(f.getAbsolutePath());
                if (arrayList != null){
                    uris.addAll(arrayList);
                }
            } else {
                uris.add(FileProvider.getUriForFile(FileViewerActivity.this,
                        FileViewerActivity.this.getApplicationContext().getPackageName() + ".provider", f));
            }
            if (uris.size() > 100){
                isFilesUnderLimit = false;
                ts ("You can't share more than 100.");
                break;
            }
        }
        if (uris.size() > 0 && isFilesUnderLimit){
            if (uris.size() == 1){
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
                Intent chooser = Intent.createChooser(intent, "Share");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                intent.setType("*/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
//                startActivity(intent);
                Intent chooser = Intent.createChooser(intent, "Share");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }
            }
        } else {
            if (!isFilesUnderLimit){
                tss("Unable to share Files.");
            } else {
                ts("These folders are empty.");
            }
        }
    }

    private void tss(String s) {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Toast.makeText(FileViewerActivity.this, s, Toast.LENGTH_SHORT).show();
        }, 2000);
    }

    private ArrayList<Uri> getFilesFromLocation(String path){
        ArrayList<Uri> uris  = new ArrayList<>();
        File f = new File(path);
        File[] files = f.listFiles();
        if (files.length > 100){
            ts ("You can't share more than 100.");
            isFilesUnderLimit = false;
            return null;
        } else {
            for (File file: files){
                if (file.isDirectory()){
                    ArrayList<Uri> arrayList = getFilesFromLocation(file.getAbsolutePath());
                    if (arrayList != null){
                        uris.addAll(arrayList);
                        if (uris.size() > 100){
                            ts ("You can't share more than 100.");
                            isFilesUnderLimit = false;
                            uris = null;
                            break;
                        }
                    }
                } else {
                    uris.add(FileProvider.getUriForFile(FileViewerActivity.this,
                            FileViewerActivity.this.getApplicationContext().getPackageName() + ".provider", f));
                    if (uris.size() > 100){
                        ts ("You can't share more than 100.");
                        isFilesUnderLimit = false;
                        uris = null;
                        break;
                    }
                }
            }
            return uris;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // File Operations

    private void doFileTask(){
        new Thread(() -> {
            ArrayList<String> work = new ArrayList<>();
            if (shared.getFileAction() == 1 || shared.getFileAction() == 2){
                // Copy Task
                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
                    for(String task: shared.getFileTask()){
                        work.add(shared.getFileAction()   //  File Action
                                + PICS_STRING_BREAK                     //
                                + task                                  //  From Path
                                + PICS_STRING_BREAK                     //
                                + shared.getFileDestination()           //  To Path
                                + PICS_STRING_BREAK                     //
                                + 0);
                    }
                } else {
                    shared.setFileAction(0);
                }
            } else if (shared.getFileAction() == 3){
                // Delete Task
                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
                    for (String task : shared.getFileTask()){
                        work.add(shared.getFileAction()   //  File Action
                                + PICS_STRING_BREAK                       //
                                + task                                    //  From Path
                                + PICS_STRING_BREAK                       //
                                + 0);                                     //  Progress
                    }
                } else {
                    shared.setFileAction(0);
                }
            }
            shared.setCFileAction(shared.getFileAction());
            shared.setFileAction(0);
            shared.setFileDestination("");
            shared.setFileTask(null);
            new DoTask().execute(work.toArray(new String[0]));
            showFODialog();
        }).start();
    }

    private Handler mHandler = new Handler();
    Runnable mStateUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                refreshDialog();
            } finally {
                mHandler.postDelayed(mStateUpdater, 1000);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        hideFODialog();
    }

    private void startRefresh() {
        new Thread(() -> {
            mStateUpdater.run();
        }).start();
    }

    private void stopRefresh() {
        mHandler.removeCallbacks(mStateUpdater);
    }

    private void hideFODialog() {
        stopRefresh();
        FODialog.dismiss();
        FODialog = null;
    }

    private TextView t;
    private AlertDialog FODialog;
    private TextView title, subT1, subT2, subA;
    private ProgressBar pBar,pBar2,pBar3;
    private Button FOCancel;
    private void showFODialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(FileViewerActivity.this);
        View view = getLayoutInflater().inflate(R.layout.item_fo, null);
        title = view.findViewById(R.id.ifo_title);
        subA = view.findViewById(R.id.ifo_sub_title_arrow);
        subT1 = view.findViewById(R.id.ifo_sub_title1);
        subT2 = view.findViewById(R.id.ifo_sub_title2);
        pBar = view.findViewById(R.id.ifo_pbar);
        pBar2 = view.findViewById(R.id.ifo_pbar2);
        pBar3 = view.findViewById(R.id.ifo_pbar3);
        FOCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shared.setCFileAction(0);
                hideFODialog();
            }
        });
        mBuilder.setView(view);
        FODialog = mBuilder.create();
        FODialog.setCancelable(false);
        FODialog.show();
        startRefresh();
    }

    private void refreshDialog() {
        try {
            pBar.setProgress(shared.getCFileProgressThree());
            pBar2.setProgress(shared.getCFileProgressTwo());
            pBar3.setProgress(shared.getCFileProgress());
            subT1.setText(shared.getCFileAction());
            switch (shared.getCFileAction()){
                case -1:
                case -2:
                    // Error
                    title.setText("Error...");
                    subA.setVisibility(View.GONE);
                    subT2.setVisibility(View.GONE);
                    break;
                case 0:
                    // Cancel
                    title.setText("Canceled...");
                    subA.setVisibility(View.GONE);
                    subT2.setVisibility(View.GONE);
                    break;
                case 1:
                    // Copy
                    title.setText("Copying...");
                    subA.setVisibility(View.VISIBLE);
                    subT2.setVisibility(View.VISIBLE);
                    subT2.setText(shared.getCFileDestination());
                    break;
                case 2:
                    // Cut
                    title.setText("Moving...");
                    subA.setVisibility(View.VISIBLE);
                    subT2.setVisibility(View.VISIBLE);
                    subT2.setText(shared.getCFileDestination());
                    break;
                case 3:
                    // Delete
                    title.setText("Deleting...");
                    subA.setVisibility(View.GONE);
                    subT2.setVisibility(View.GONE);
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static class DoTask extends AsyncTask<String[],Integer,String> {
        private int cFile = 0, tFile = 0, cTask, tTask, time = 0;
        private Handler mHandler = new Handler();
        private boolean isTaskError = false;

        protected String doInBackground(String[]...tasks){
            // For Copy, Cut:
            // 0 - Action
            // 1 - Source
            // 2 - Destination
            // 3 - Progress
            // For Delete:
            // 0 - Action
            // 1 - Source
            // 2 - Progress
            cTask = 0;
            tTask = tasks[0].length;
            try{
                startRefresh();
                for(String work: tasks[0]){
                    if(shared.getCFileAction() == 0){
                        isTaskError = true;
                        break;
                    }
                    String[] task = work.split(PICS_STRING_BREAK);
                    int isError = 0, cTask = Integer.parseInt(task[0]);
                    if(cTask == 0){
                        isError = 1;
                    } else if(cTask == 1){
                        // Copy Task
                        File source = new File(task[1]);
                        File destination = new File(task[2] , source.getName());
                        shared.setCFilePath(task[1]);
                        shared.setCFileDestination(task[2]);
                        shared.setCFileProgress(0);
                        tFile = getTotalFiles(task[1]);
                        cFile = 0;
                        isError = copyDir(source, destination);
                    } else if (cTask == 2){
                        // Cut Task
                        File source = new File(task[1]);
                        File destination = new File(task[2] , source.getName());
                        shared.setCFilePath(task[1]);
                        shared.setCFileDestination(task[2]);
                        shared.setCFileProgress(0);
                        tFile = getTotalFiles(task[1]);
                        cFile = 0;
                        isError = copyDir(source, destination);
                        if(isError == 0){
                            boolean isE = deleteMyFile(task[1]);
                            if(!isE){ isError = 3; }
                        }
                    } else if (cTask == 3){
                        // Delete Task
                        shared.setCFilePath(task[1]);
                        shared.setCFileProgress(40);
                        boolean isE = deleteMyFile(task[1]);
                        if(!isE){ isError = 2; }
                    }

                    if (isError > 0) {
                        isTaskError = true;
                    }

                    cTask++;
                    shared.setCFileProgressThree((cTask*100)/tTask);
                }
            } finally {
                stopRefresh();
                if(isTaskError){
                    shared.setIsFOR(-1);
                } else {
                    shared.setIsFOR(0);
                }
            }
            return "Done";
        }

        Runnable mStateUpdater = new Runnable() {
            @Override
            public void run() {
                try {
                    shared.setIsFOR(time);
                } finally {
                    mHandler.postDelayed(mStateUpdater, 1000);
                }
            }
        };

        private void startRefresh() {
            new Thread(() -> {
                mStateUpdater.run();
            }).start();
        }

        private void stopRefresh() {
            mHandler.removeCallbacks(mStateUpdater);
        }

        private int getTotalFiles(String path) {
            int total = 0;
            File f = new File(path);
            if(f.isDirectory()){
                String[] children = f.list();
                for (String child : children) {
                    total += getTotalFiles(child);
                }
            } else {
                total++;
            }
            return total;
        }

        private int copyDir(File source, File destination) {
            int isError = 0;
            if(source.isDirectory()){
                if (!destination.exists() && !destination.mkdirs()) {
                    isError = 2;
                }
                String[] children = source.list();
                for (String child : children) {
                    isError = copyDir(new File(source, child), new File(destination, child));
                }
            } else {
                try {
                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(destination);
                    long lengthOfFile = source.length();
                    byte[] buf = new byte[1024];
                    int len;
                    long total = 0;
                    while ((len = in.read(buf)) != -1) {
                        total += len;
                        cFile++;
                        shared.setCFileProgress((int)((total*100)/lengthOfFile));
                        if (shared.getCFileAction() < 1){
                            isError = 1;
                            break;
                        }
                        out.write(buf, 0, len);
                    }
                    cFile++;
                    shared.setCFileProgressTwo(((cFile)*100) /tFile);
                    in.close();
                    out.close();
                } catch (IOException e){
                    isError = 2;
                    e.printStackTrace();
                }
            }
            return isError;
        }

        private boolean deleteMyFile(String s) {
            File f = new File(s);
            return delete(f);
        }

        private boolean delete(File path){
            if(path.isDirectory()){
                File[] files = path.listFiles();
                if(files == null){
                    return false;
                } else {
                    for (File f: files){
                        delete(f);
                    }
                }
                return path.delete();
            } else {
                return path.delete();
            }
        }

    }

}