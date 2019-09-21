package org.telegram.ui;

//  @author Ankush Kumar (ankushalg@gmail.com)
//  ~ Methods:
//  ~ Saved Messages Chat ID =>
//    UserConfig.getInstance(currentAccount).getClientUserId()
//
//
//  ~ Photo Sending Method:
//
//  ~ Adding photo uri to arrayList.
//    if (photoPathsArray == null) {
//        photoPathsArray = new ArrayList<>();
//    }
//    SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
//    info.uri = uri;
//    photoPathsArray.add(info);
//
//  ~ Adding #Tags with Images:
//    photoPathsArray.get(0).caption = sendingText; // Here get(0) is for first image, you may set it for more images also.
//
//  ~ Preparing Sending Message:
//    SendMessagesHelper.prepareSendingMedia(photoPathsArray, did, null, null, false, false, null);  // Here did is id of chat.
//

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;

import java.util.ArrayList;

public class PicsMediaBackupActivity  extends AppCompatActivity {
    private boolean isSending = false;
    private final int PICK_IMAGE = 101;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pics_media_backup);
        setUpApp();
        findViewById(R.id.pmb_send).setOnClickListener(v -> {
            findViewById(R.id.pmb_send).setVisibility(View.GONE);
            if (!isSending){
                isSending = true;
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });
        findViewById(R.id.pmb_reset).setOnClickListener(v -> {
            findViewById(R.id.pmb_send).setVisibility(View.VISIBLE);
            isSending = false;
        });
    }

    private long did;
    private ArrayList<SendMessagesHelper.SendingMediaInfo> photoPathsArray;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri dataUri = data.getData();
                String text = "This is just a testing... Here #Tags Are #ALLTesting";
//                sendPhoto(dataUri, text);
            }
        }
    }
    private void setUpApp() {
        int currentAccount = UserConfig.selectedAccount;
        if (!UserConfig.getInstance(currentAccount).isClientActivated()) {
            Toast.makeText(this, "User Not Logged In", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            did = UserConfig.getInstance(currentAccount).getClientUserId();
        }
    }
//    private void sendPhoto(Uri uri, String title) {
//        if (photoPathsArray == null) {
//            photoPathsArray = new ArrayList<>();
//        }
//        SendMessagesHelper.SendingMediaInfo info = new SendMessagesHelper.SendingMediaInfo();
//        info.uri = uri;
//        photoPathsArray.add(info);
//        photoPathsArray.get(photoPathsArray.indexOf(info)).caption = title; // Here get(0) is for first image, you may set it for more images also.
//        SendMessagesHelper.prepareSendingMedia(photoPathsArray, did, null, null, false, false, null);  // Here did is id of chat.
//    }
}
