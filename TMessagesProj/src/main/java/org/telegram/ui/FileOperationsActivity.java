package org.telegram.ui;

import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FileOperationsActivity extends AppCompatActivity {
    private RecyclerView mTaskRecyclerView;
    private static final String PICS_STRING_BREAK = "<<!PicsBreak12846>>:";
    private static final String PICS_ERROR = "<<!PicsError12846>>:";
    private static SharedMemory shared;
    private LinearLayout pBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_operations);
        mHandler = new Handler();
        setUpViews();
        doPendingWork();
    }

    private void doPendingWork() {
        new Thread(() -> {
            if (shared.getFileAction() == 1){
                // Copy Task
                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
                    for (String task : shared.getFileTask()){
                        new CopyTask().execute(shared.getFileAction()   // 0 File Action
                                + PICS_STRING_BREAK                     //
                                + task                                  // 1 From Path
                                + PICS_STRING_BREAK                     //
                                + shared.getFileDestination()           // 2 To Path
                                + PICS_STRING_BREAK                     //
                                + 0);                                   // 3 Progress
                    }
                } else {
                    shared.setFileAction(0);
                }
            } else if (shared.getFileAction() == 2){
                // Cut Task
                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
                    for (String task : shared.getFileTask()){
                        new MoveTask().execute(shared.getFileAction()   //  File Action
                                + PICS_STRING_BREAK                     //
                                + task                                  //  From Path
                                + PICS_STRING_BREAK                     //
                                + shared.getFileDestination()           //  To Path
                                + PICS_STRING_BREAK                     //
                                + 0);                                   //  Progress
                    }
                } else {
                    shared.setFileAction(0);
                }
            } else if (shared.getFileAction() == 3){
                // Delete Task
                if (shared.getFileTask() != null && shared.getFileTask().size() > 0){
                    for (String task : shared.getFileTask()){
                        new DeleteTask().execute(shared.getFileAction()   //  File Action
                                + PICS_STRING_BREAK                       //
                                + task                                    //  From Path
                                + PICS_STRING_BREAK                       //
                                + 0);                                     //  Progress
                    }
                } else {
                    shared.setFileAction(0);
                }
            }
            shared.setFileAction(0);
            shared.setFileDestination("");
            shared.setFileTask(null);
            startRefresh();
            showProgressDialog(false);
        }).start();
        isFirstTimeLoaded = true;
    }

    private void showProgressDialog(boolean isShow) {
        runOnUiThread(() -> {
            if(isShow){
                pBar.setVisibility(View.VISIBLE);
            } else {
                pBar.setVisibility(View.GONE);
            }
        });
    }

    private static class DeleteTask extends AsyncTask<String,Integer,String> {
        // Do the task in background/non UI thread
        protected String doInBackground(String...tasks){
            // Get the number of task


            if(isCancelled()){ }

            // Return the task list for post execute
            return tasks[0];
        }

        // When all async task done
        protected void onPostExecute(String result){

        }
    }

    private static class MoveTask extends AsyncTask<String,Integer,String> {
        // Do the task in background/non UI thread
        protected String doInBackground(String...tasks){
            // Get the number of task


            if(isCancelled()){ }

            // Return the task list for post execute
            return tasks[0];
        }

        // When all async task done
        protected void onPostExecute(String result){

        }
    }

    private static class CopyTask extends AsyncTask<String,Integer,String> {
        // Do the task in background/non UI thread
        protected String doInBackground(String...tasks){
            // Get the task
            // 0 - Action
            // 1 - Source
            // 2 - Destination
            // 3 - Progress
            ArrayList<String> oldTask;
            if(shared.getRunningFileTask() != null){
                oldTask = new ArrayList<>(shared.getRunningFileTask());
            } else {
                oldTask = new ArrayList<>();
            }
            oldTask.add(tasks[0]);
            shared.setRunningFileTask(new HashSet<>(oldTask));
            String[] task = tasks[0].split(PICS_STRING_BREAK);
            int isError = 0;
            File source = new File(task[1]);
            File destination = new File(task[2] , source.getName());

            try{
                InputStream in = new FileInputStream(source);
                OutputStream out = new FileOutputStream(destination);
                long lengthOfFile = source.length();
                byte[] buf = new byte[1024];
                int len;
                long total = 0;
                while ((len = in.read(buf)) != -1) {
                    total += len;
                    boolean isTaskCanceled = setMyProgress(task[0] + PICS_STRING_BREAK
                            + task[1] + PICS_STRING_BREAK
                            + task[2] + PICS_STRING_BREAK
                            ,(int)((total*100)/lengthOfFile));
                    if (isTaskCanceled){
                        isError = 1;
                        break;
                    }
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (IOException e){
                isError = 2;
                e.printStackTrace();
            }

            if (isError > 0){
                // Return the task list for post execute
                finishWork(PICS_ERROR + isError + PICS_STRING_BREAK
                        + task[0] + PICS_STRING_BREAK
                        + task[1] + PICS_STRING_BREAK
                        + task[2] + PICS_STRING_BREAK);
            } else
                // Return the task list for post execute
                finishWork(task[0] + PICS_STRING_BREAK
                    + task[1] + PICS_STRING_BREAK
                    + task[2] + PICS_STRING_BREAK);
            return "Done";
        }

        private boolean setMyProgress(String id, int update) {
            boolean isCancelled = true;
            synchronized (this) {
                ArrayList<String> newArray = new ArrayList<>();
                if(shared.getRunningFileTask() != null){
                    for (String task : shared.getRunningFileTask()){
                        if(task.startsWith(id)){
                            isCancelled = false;
                            newArray.add(id + update);
                        } else {
                            newArray.add(task);
                        }
                    }
                    shared.setRunningFileTask(new HashSet<>(newArray));
                }
            }
            return isCancelled;
        }

        private void finishWork(String id){
            if (id.startsWith(PICS_ERROR)){
                synchronized (this) {
                    ArrayList<String> newArray = new ArrayList<>();
                    if(shared.getRunningFileTask() != null){
                        for (String task : shared.getRunningFileTask()){
                            if(!task.startsWith(id)){
                                newArray.add(task);
                            } else {
                                newArray.add(id);
                            }
                        }
                        shared.setRunningFileTask(new HashSet<>(newArray));
                    }
                }
            } else {
                synchronized (this) {
                    ArrayList<String> newArray = new ArrayList<>();
                    if(shared.getRunningFileTask() != null){
                        for (String task : shared.getRunningFileTask()){
                            if(!task.startsWith(id)){
                                newArray.add(task);
                            }
                        }
                        shared.setRunningFileTask(new HashSet<>(newArray));
                    }
                }
            }
        }
    }

    private void setUpViews() {
        pBar = findViewById(R.id.fo_pbar);
        pBar.setVisibility(View.VISIBLE);
        shared = new SharedMemory(this);
        mTaskRecyclerView = findViewById(R.id.fo_recycle);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mTaskRecyclerView.setLayoutManager(layoutManager);
    }

    private Handler mHandler;

    private boolean isFirstTimeLoaded = false;
    private int reUpdateRate = 0, timeRe = 5;

    Runnable mStateUpdater = new Runnable() {
        @Override
        public void run() {
            try {
                reUpdateRate++;
                updateRecycleView();
            } finally {
                if (reUpdateRate > 2 && timeRe < 2){
                    timeRe += 5;
                } else {
                    timeRe--;
                }
                mHandler.postDelayed(mStateUpdater, timeRe*100);
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

    @Override
    protected void onPause() {
        super.onPause();
        stopRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstTimeLoaded)
            startRefresh();
    }

    private void updateRecycleView() {
        List<TaskItem> currentTasks = getCurrentTasks();
        if (currentTasks.size() > 0){
            TaskObjectsAdapter adapter = new TaskObjectsAdapter(currentTasks);
            runOnUiThread(() -> {
                mTaskRecyclerView.setAdapter(adapter);
            });
        }
        reUpdateRate--;
    }

    private List<TaskItem> getCurrentTasks() {
        List<TaskItem> tasks = new ArrayList<>();
        synchronized (this) {
            if (shared.getRunningFileTask() != null){
                for (String task: shared.getRunningFileTask()){
                    TaskItem item = new TaskItem();
                    String[] t = task.split(PICS_STRING_BREAK);
                    if (task.startsWith(PICS_ERROR)){
                        int i = Integer.parseInt(t[0].replace(PICS_ERROR, ""));
                        item.title = i - 2;
                        item.per = 0;
                        item.subTitlePre = t[2];
                        if (Integer.parseInt(t[1]) != 3){
                            item.subTitlePost = t[3];
                        }
                    } else {
                        item.title = Integer.parseInt(t[0]);
                        item.per = Integer.parseInt(t[3]);
                        item.subTitlePre = t[1];
                        if (Integer.parseInt(t[0]) != 3){
                            item.subTitlePost = t[2];
                        }
                    }
                    tasks.add(item);
                }
            }
            return tasks;
        }
    }

    public class TaskObjectsAdapter extends RecyclerView.Adapter<TaskObjectsAdapter.MyViewHolder> {

        private List<TaskItem> taskList;

        private class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView title, subT1, subT2, subA;
            private ProgressBar pBar;
            MyViewHolder(View view) {
                super(view);
                title = view.findViewById(R.id.ifo_title);
                subA = view.findViewById(R.id.ifo_sub_title_arrow);
                subT1 = view.findViewById(R.id.ifo_sub_title1);
                subT2 = view.findViewById(R.id.ifo_sub_title2);
                pBar = view.findViewById(R.id.ifo_pbar);
            }
        }

        TaskObjectsAdapter(List<TaskItem> taskList) {
            this.taskList = taskList;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_fo, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            TaskItem taskObject = taskList.get(position);
            int per = taskObject.per;
            holder.pBar.setProgress(per);
            holder.subT1.setText(taskObject.subTitlePre);
            switch (taskObject.title){
                case -1:
                    // Error
                    break;
                case 0:
                    // Cancel
                    break;
                case 1:
                    // Copy
                    holder.title.setText("Copying...");
                    holder.subA.setVisibility(View.VISIBLE);
                    holder.subT2.setVisibility(View.VISIBLE);
                    holder.subT2.setText(taskObject.subTitlePost);
                    break;
                case 2:
                    // Cut
                    holder.title.setText("Moving...");
                    holder.subA.setVisibility(View.VISIBLE);
                    holder.subT2.setVisibility(View.VISIBLE);
                    holder.subT2.setText(taskObject.subTitlePost);
                    break;
                case 3:
                    // Delete
                    holder.title.setText("Deleting...");
                    holder.subA.setVisibility(View.GONE);
                    holder.subT2.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return taskList.size();
        }
    }

    // title == 1 for copy
    // title == 2 for cut
    // title == 3 for delete
    private class TaskItem {
        int per;
        int title;
        String subTitlePre;
        String subTitlePost;
    }

    private void ts(int time, String message){
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            Toast.makeText(FileOperationsActivity.this, message, Toast.LENGTH_SHORT).show();
        }, 2000*time);
    }

    private void ts(String message){
        Toast.makeText(FileOperationsActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}