//package org.telegram.ui;
//
//import android.annotation.SuppressLint;
//import android.content.res.Configuration;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.CountDownTimer;
//import android.os.Handler;
//import android.util.ArraySet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import org.telegram.messenger.R;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//public class FileOperationsActivity extends AppCompatActivity {
//    private RecyclerView mTaskRecyclerView;
//    private static final String PICS_STRING_BREAK = "<<!PicsBreak12846>>:";
//    private static final String PICS_ERROR = "<<!PicsError12846>>:";
//    private static SharedMemory shared;
//    private LinearLayout pBar;
//    private boolean toClean = false;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_file_operations);
//        mHandler = new Handler();
//        setUpViews();
//        doPendingWork();
//    }
//
//    private void doPendingWork() {
//        new Thread(() -> {
//            if (shared.getFileAction() == 1 || shared.getFileAction() == 2){
//                // Copy Task
//                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
//                    Set<String> old;
//                    if(shared.getRunningFileTask() == null){
//                        old =  new HashSet<>();
//                    } else {
//                        old =  shared.getRunningFileTask();
//                    }
//                    for(String task: shared.getFileTask()){
//                        old.add(shared.getFileAction()   //  File Action
//                                + PICS_STRING_BREAK                     //
//                                + task                                  //  From Path
//                                + PICS_STRING_BREAK                     //
//                                + shared.getFileDestination()           //  To Path
//                                + PICS_STRING_BREAK                     //
//                                + 0);
//                    }
//                    synchronized (this){
//                        shared.setRunningFileTask(old);
//                    }
//                } else {
//                    shared.setFileAction(0);
//                }
//            } else if (shared.getFileAction() == 3){
//                // Delete Task
//                Set<String> old;
//                if(shared.getRunningFileTask() == null){
//                    old =  new HashSet<>();
//                } else {
//                    old =  shared.getRunningFileTask();
//                }
//                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
//                    for (String task : shared.getFileTask()){
//                        old.add(shared.getFileAction()   //  File Action
//                                + PICS_STRING_BREAK                       //
//                                + task                                    //  From Path
//                                + PICS_STRING_BREAK                       //
//                                + 0);                                     //  Progress
//                    }
//                    synchronized (this){
//                        shared.setRunningFileTask(old);
//                    }
//                } else {
//                    shared.setFileAction(0);
//                }
//            }
//            shared.setFileAction(0);
//            shared.setFileDestination("");
//            shared.setFileTask(null);
//            startRefresh();
//            showProgressDialog(false);
//            isFirstTimeLoaded = true;
//            synchronized (this){
//                if(!shared.getAsyncTaskRunning()){
//                    doWork();
//                }
//            }
//        }).start();
//    }
//
//    private void doWork() {
//        synchronized (this){
//            shared.setAsyncTaskRunning(true);
//        }
//        ArrayList<String> works = new ArrayList<>(shared.getRunningFileTask());
//        if(works.size() > 0) {
//            String s = works.get(0);
//            works.remove(0);
//            synchronized (this) {
//                shared.setRunningFileTask(new HashSet<>(works));
//                shared.setCFileProgress(0);
//            }
//            new DoTask().execute(s);
//        } else {
//            synchronized (this){
//                shared.setAsyncTaskRunning(false);
//            }
//            synchronized (this) {
//                shared.setCFileAction(101);
//                if(toClean){
//                    shared.setErrorFileTask(null);
//                }
//            }
//            ts("Finished.");
//            if(shared.getErrorFileTask() != null){
//                if(shared.getErrorFileTask().size() == 0){
//                    finish();
//                }
//            } else {
//                finish();
//            }
//        }
//    }
//
//    // isError == 0 - No Error
//    // == 1 - Cancelled
//    // == 2 - Error
//    // == 3 - Move Source Write Error;
//    @SuppressLint("StaticFieldLeak")
//    private class DoTask extends AsyncTask<String,Integer,String> {
//        private int cFile = 0, tFile = 0;
//
//        protected String doInBackground(String...tasks){
//            // For Copy, Cut:
//            // 0 - Action
//            // 1 - Source
//            // 2 - Destination
//            // 3 - Progress
//            // For Delete:
//            // 0 - Action
//            // 1 - Source
//            // 2 - Progress
//            String[] task = tasks[0].split(PICS_STRING_BREAK);
//            int isError = 0, cTask = Integer.parseInt(task[0]);
//            shared.setCFileAction(cTask);
//            if(cTask == 0){
//                isError = 1;
//            } else if(cTask == 1){
//                // Copy Task
//                File source = new File(task[1]);
//                File destination = new File(task[2] , source.getName());
//                shared.setCFilePath(task[1]);
//                shared.setCFileDestination(task[2]);
//                shared.setCFileProgress(0);
//                shared.setCFileWork(cTask);
//                tFile = getTotalFiles(task[1]);
////                try{
////                    tFile = getTotalSize(source);
//                    cFile = 0;
////                } catch (Exception e){
////                    e.printStackTrace();
////                }
//                isError = copyDir(source, destination);
//            } else if (cTask == 2){
//                // Cut Task
//                File source = new File(task[1]);
//                File destination = new File(task[2] , source.getName());
//                shared.setCFilePath(task[1]);
//                shared.setCFileDestination(task[2]);
//                shared.setCFileProgress(0);
//                shared.setCFileWork(cTask);
//                tFile = getTotalFiles(task[1]);
////                try{
////                    tFile = getTotalSize(source);
//                    cFile = 0;
////                } catch (Exception e){
////                    e.printStackTrace();
////                }
//                isError = copyDir(source, destination);
//                if(isError == 0){
//                    boolean isE = deleteMyFile(task[1]);
//                    if(!isE){ isError = 3; }
//                }
//            } else if (cTask == 3){
//                // Delete Task
//                shared.setCFilePath(task[1]);
//                shared.setCFileProgress(40);
//                shared.setCFileWork(cTask);
//                boolean isE = deleteMyFile(task[1]);
//                if(!isE){ isError = 2; }
//            }
//
//            if (isError > 0) {
//                // Return the task list for post execute
//                addError(PICS_ERROR + isError + PICS_STRING_BREAK
//                        + task[0] + PICS_STRING_BREAK
//                        + task[1] + PICS_STRING_BREAK
//                        + task[2] + PICS_STRING_BREAK);
//            }
//            doWork();
//            return "Done";
//        }
//
////        private long getTotalSize(File path){
////            long total = 0;
////            if(path.isDirectory()){
////                String[] children = path.list();
////                for (String child : children) {
////                    total += getTotalSize(new File(path.getAbsolutePath(),child));
////                }
////            } else {
////                total += path.length();
////            }
////            return total;
////        }
//
//        private int getTotalFiles(String path) {
//            int total = 0;
//            File f = new File(path);
//            if(f.isDirectory()){
//                String[] children = f.list();
//                for (String child : children) {
//                    total += getTotalFiles(child);
//                }
//            } else {
//                total++;
//            }
//            return total;
//        }
//
//        private int copyDir(File source, File destination) {
//            int isError = 0;
//            if(source.isDirectory()){
//                if (!destination.exists() && !destination.mkdirs()) {
//                    isError = 2;
//                }
//                String[] children = source.list();
//                for (String child : children) {
//                    isError = copyDir(new File(source, child), new File(destination, child));
//                }
//            } else {
//                try {
//                    cFile++;
//                    shared.setCFileProgressTwo(((cFile - 1)*100) /tFile);
//                    InputStream in = new FileInputStream(source);
//                    OutputStream out = new FileOutputStream(destination);
//                    long lengthOfFile = source.length();
//                    byte[] buf = new byte[1024];
//                    int len;
//                    long total = 0;
//                    while ((len = in.read(buf)) != -1) {
//                        total += len;
//                        cFile++;
////                        try {
//                            shared.setCFileProgress((int)((total*100)/lengthOfFile));
////                        }catch (Exception e){
////                            e.printStackTrace();
////                        }
//                        if (shared.getCFileAction() < 1){
//                            isError = 1;
//                            break;
//                        }
//                        out.write(buf, 0, len);
//                    }
//                    in.close();
//                    out.close();
//                } catch (IOException e){
//                    isError = 2;
//                    e.printStackTrace();
//                }
//            }
//            return isError;
//        }
//
//        private boolean deleteMyFile(String s) {
//            File f = new File(s);
//            return delete(f);
//        }
//
//        private boolean delete(File path){
//            if(path.isDirectory()){
//                File[] files = path.listFiles();
//                if(files == null){
//                    return false;
//                } else {
//                    for (File f: files){
//                        delete(f);
//                    }
//                }
//                return path.delete();
//            } else {
//                return path.delete();
//            }
//        }
//
//        private void addError(String id){
//            synchronized (this) {
//                Set<String> myArray;
//                if(shared.getErrorFileTask() == null){
//                    myArray = new HashSet<>();
//                } else {
//                    myArray = shared.getErrorFileTask();
//                }
//                myArray.add(id);
//                shared.setErrorFileTask(myArray);
//            }
//        }
//    }
//
//    private void showProgressDialog(boolean isShow) {
//        runOnUiThread(() -> {
//            if(isShow){
//                pBar.setVisibility(View.VISIBLE);
//            } else {
//                pBar.setVisibility(View.GONE);
//            }
//        });
//    }
//
//    private void setUpViews() {
//        pBar = findViewById(R.id.fo_pbar);
//        pBar.setVisibility(View.VISIBLE);
//        shared = new SharedMemory(this);
//        mTaskRecyclerView = findViewById(R.id.fo_recycle);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//        mTaskRecyclerView.setLayoutManager(layoutManager);
//        findViewById(R.id.fo_ok).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                toClean = true;
//                cleanErrors();
//                finish();
//            }
//        });
//    }
//
//    private void cleanErrors() {
//        shared.setErrorFileTask(null);
//    }
//
//    private Handler mHandler;
//
//    private boolean isFirstTimeLoaded = false;
//
//    Runnable mStateUpdater = new Runnable() {
//        @Override
//        public void run() {
//            try {
//                updateRecycleView();
//            } finally {
//                mHandler.postDelayed(mStateUpdater, 5*100);
//            }
//        }
//    };
//
//    private void startRefresh() {
//        new Thread(() -> {
//            mStateUpdater.run();
//        }).start();
//    }
//
//    private void stopRefresh() {
//        mHandler.removeCallbacks(mStateUpdater);
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        stopRefresh();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (isFirstTimeLoaded)
//            startRefresh();
//    }
//
//    private void updateRecycleView() {
//        List<TaskItem> currentTasks = getCurrentTasks();
//        if (currentTasks.size() > 0){
//            TaskObjectsAdapter adapter = new TaskObjectsAdapter(currentTasks);
//            runOnUiThread(() -> {
//                mTaskRecyclerView.setAdapter(adapter);
//            });
//        }
//    }
//
//    private List<TaskItem> getCurrentTasks() {
//        List<TaskItem> tasks = new ArrayList<>();
//        if(shared.getCFileAction() != 101){
//            TaskItem cI = new TaskItem(shared.getCFileAction(),shared.getCFilePath(), shared.getCFileDestination(), shared.getCFileProgress(), shared.getCFileProgressTwo());
//            tasks.add(cI);
//        }
//        if (shared.getRunningFileTask() != null){
//            for (String task: shared.getRunningFileTask()){
//                TaskItem item = new TaskItem();
//                String[] t = task.split(PICS_STRING_BREAK);
//                item.title = Integer.parseInt(t[0]);
//                item.per = 0;
//                item.subTitlePre = t[1];
//                if (Integer.parseInt(t[0]) != 3){
//                    item.subTitlePost = t[2];
//                }
//                tasks.add(item);
//            }
//        }
//        if (shared.getErrorFileTask() != null){
//            for (String task: shared.getErrorFileTask()){
//                TaskItem item = new TaskItem();
//                String[] t = task.split(PICS_STRING_BREAK);
//                int i = Integer.parseInt(t[0].replace(PICS_ERROR, ""));
//                item.title = i - 2;
//                item.per = 0;
//                item.subTitlePre = t[2];
//                if (Integer.parseInt(t[1]) != 3){
//                    item.subTitlePost = t[3];
//                }
//                tasks.add(item);
//            }
//        }
//        return tasks;
//    }
//
//    public class TaskObjectsAdapter extends RecyclerView.Adapter<TaskObjectsAdapter.MyViewHolder> {
//
//        private List<TaskItem> taskList;
//
//        private class MyViewHolder extends RecyclerView.ViewHolder {
//            private TextView title, subT1, subT2, subA;
//            private ProgressBar pBar,pBar2;
//            MyViewHolder(View view) {
//                super(view);
//                title = view.findViewById(R.id.ifo_title);
//                subA = view.findViewById(R.id.ifo_sub_title_arrow);
//                subT1 = view.findViewById(R.id.ifo_sub_title1);
//                subT2 = view.findViewById(R.id.ifo_sub_title2);
//                pBar = view.findViewById(R.id.ifo_pbar);
//                pBar2 = view.findViewById(R.id.ifo_pbar2);
//            }
//        }
//
//        TaskObjectsAdapter(List<TaskItem> taskList) {
//            this.taskList = taskList;
//        }
//
//        @NonNull
//        @Override
//        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View itemView = LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_fo, parent, false);
//            return new MyViewHolder(itemView);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//            TaskItem taskObject = taskList.get(position);
//            int per = taskObject.per;
//            holder.pBar.setProgress(per);
//            holder.subT1.setText(taskObject.subTitlePre);
//            holder.pBar2.setVisibility(View.GONE);
//            switch (taskObject.title){
//                case -1:
//                case -2:
//                    // Error
//                    holder.title.setText("Error...");
//                    holder.subA.setVisibility(View.GONE);
//                    holder.pBar2.setVisibility(View.GONE);
//                    holder.subT2.setVisibility(View.GONE);
//                    break;
//                case 0:
//                    // Cancel
//                    holder.title.setText("Canceled...");
//                    holder.subA.setVisibility(View.GONE);
//                    holder.pBar2.setVisibility(View.GONE);
//                    holder.subT2.setVisibility(View.GONE);
//                    break;
//                case 1:
//                    // Copy
//                    holder.title.setText("Copying...");
//                    holder.pBar2.setProgress(taskObject.per2);
//                    holder.subA.setVisibility(View.VISIBLE);
//                    holder.subT2.setVisibility(View.VISIBLE);
//                    holder.pBar2.setVisibility(View.VISIBLE);
//                    holder.subT2.setText(taskObject.subTitlePost);
//                    break;
//                case 2:
//                    // Cut
//                    holder.title.setText("Moving...");
//                    holder.pBar2.setProgress(taskObject.per2);
//                    holder.subA.setVisibility(View.VISIBLE);
//                    holder.subT2.setVisibility(View.VISIBLE);
//                    holder.pBar2.setVisibility(View.VISIBLE);
//                    holder.subT2.setText(taskObject.subTitlePost);
//                    break;
//                case 3:
//                    // Delete
//                    holder.title.setText("Deleting...");
//                    holder.subA.setVisibility(View.GONE);
//                    holder.subT2.setVisibility(View.GONE);
//                    holder.pBar2.setVisibility(View.GONE);
//                    break;
//            }
//        }
//
//        @Override
//        public int getItemCount() {
//            return taskList.size();
//        }
//    }
//
//    // title == 1 for copy
//    // title == 2 for cut
//    // title == 3 for delete
//    private class TaskItem {
//        int per = 0,per2 = 0;
//        int title;
//        String subTitlePre;
//        String subTitlePost;
//        TaskItem(int action, String fileSource, String fileDestination, int percentage, int perTwo) {
//            super();
//            this.per = percentage;
//            this.subTitlePost = fileDestination;
//            this.subTitlePre = fileSource;
//            this.title = action;
//            this.per2 = perTwo;
//        }
//        TaskItem(){}
//    }
//
//    private void ts(int time, String message){
//        Handler handler = new Handler();
//        handler.postDelayed(() -> {
//            Toast.makeText(FileOperationsActivity.this, message, Toast.LENGTH_SHORT).show();
//        }, 2000*time);
//    }
//
//    private void ts(String message){
//        runOnUiThread(() -> {
//            Toast.makeText(FileOperationsActivity.this, message, Toast.LENGTH_SHORT).show();
//        });
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//    }
//
//}