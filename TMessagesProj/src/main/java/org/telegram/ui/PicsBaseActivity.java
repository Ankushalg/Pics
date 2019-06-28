package org.telegram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBarLayout;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.DrawerLayoutContainer;
import org.telegram.ui.ActionBar.Theme;

public class PicsBaseActivity extends Activity {
    private ActionBarLayout actionBarLayout;
    protected DrawerLayoutContainer drawerLayoutContainer;

    private int currentAccount;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Loading Telegram Application
        ApplicationLoader.postInitApplication();
        AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());

        // Getting Current Account.
        currentAccount = UserConfig.selectedAccount;

        super.onCreate(savedInstanceState);

        if (!UserConfig.getInstance(currentAccount).isClientActivated()) {
            // User Not Logged in...
            Toast.makeText(this, "User Not Logged In", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // <> Pics Comment :: Initialise Text (Like Fonts).
            Theme.createChatResources(this, false);

            // <> Pics Comment :: Creating ActionBar.
            actionBarLayout = new ActionBarLayout(this);

            // <> Pics Comment :: This is the home layout. We will add diff. layout inside it later.
            drawerLayoutContainer = new DrawerLayoutContainer(this);
            drawerLayoutContainer.setBehindKeyboardColor(Theme.getColor(Theme.key_windowBackgroundWhite));
            setContentView(drawerLayoutContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            Bundle args = new Bundle();
            args.putInt("user_id", UserConfig.getInstance(currentAccount).getClientUserId());
            presentFragment(new ChatActivity(args));

            // <> Pics Comment :: Setting action bar layout as action bar.
            drawerLayoutContainer.setParentActionBarLayout(actionBarLayout);
            actionBarLayout.setDrawerLayoutContainer(drawerLayoutContainer);

        }
    }

    public void presentFragment(BaseFragment fragment) {
        actionBarLayout.presentFragment(fragment);
    }

    public boolean presentFragment(final BaseFragment fragment, final boolean removeLast, boolean forceWithoutAnimation) {
        return actionBarLayout.presentFragment(fragment, removeLast, forceWithoutAnimation, true, false);
    }

}