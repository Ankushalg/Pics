package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;


//import com.google.android.material.tabs.TabLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;

// <> Pics

//public class MIntroActivity extends AppCompatActivity {

public class MIntroActivity extends AppCompatActivity implements NotificationCenter.NotificationCenterDelegate{
    private TextView skip,next;

    private int currentAccount = UserConfig.selectedAccount;
    private int lastPage = 0;
    private boolean justCreated = false;
    private boolean startPressed = false;
    private boolean dragging;
    private boolean destroyed;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mintro);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        if(getSupportActionBar() != null)
            getSupportActionBar().hide();
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        preferences.edit().putLong("intro_crashed_time", System.currentTimeMillis()).apply();

        viewPager = findViewById(R.id.m_intro_viewpager);
        skip = findViewById(R.id.m_intro_skip);
        next = findViewById(R.id.m_intro_next);
        MIntroCategoryAdapter adapter = new MIntroCategoryAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int i) {
                if(i == 4){
                    skip.setVisibility(View.GONE);
                    next.setText(LocaleController.getString("verify",R.string.verify));
                } else {
                    skip.setVisibility(View.VISIBLE);
                    next.setText(LocaleController.getString("next",R.string.next));
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                    dragging = true;
                } else if (i == ViewPager.SCROLL_STATE_IDLE || i == ViewPager.SCROLL_STATE_SETTLING) {
                    if (dragging) {
                        dragging = false;
                    }
                    if (lastPage != viewPager.getCurrentItem()) {
                        lastPage = viewPager.getCurrentItem();
                    }
                }
            }
        });

        next.setOnClickListener(v -> {
            if(viewPager.getCurrentItem() == 4) {
                startLaunchActivity();
            } else
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        });
        skip.setOnClickListener(v -> {
            viewPager.setCurrentItem(4);
        });

        LocaleController.getInstance().loadRemoteLanguages(currentAccount);
        checkContinueText();
        justCreated = true;
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.suggestedLangpack);

        AndroidUtilities.handleProxyIntent(this, getIntent());

    }

    void startLaunchActivity(){
        if (startPressed) {
            return;
        }
        startPressed = true;
        Intent intent2 = new Intent(MIntroActivity.this, LaunchActivity.class);
        intent2.putExtra("fromIntro", true);
        startActivity(intent2);
        destroyed = true;
        finish();
    }

//    private void setUpTheme() {
//        Theme.ThemeInfo theme = new Theme.ThemeInfo();
//        theme.name = "Arctic Blue";
//        theme.assetName = "arctic.attheme";
//        theme.pathToFile = null;
//        Theme.applyTheme(theme);
//    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.suggestedLangpack) {
            checkContinueText();
        }
    }

    private void checkContinueText() {
        LocaleController.LocaleInfo englishInfo = null;
        LocaleController.LocaleInfo systemInfo = null;
        LocaleController.LocaleInfo currentLocaleInfo = LocaleController.getInstance().getCurrentLocaleInfo();
        String systemLang = LocaleController.getSystemLocaleStringIso639().toLowerCase();
        String arg = systemLang.contains("-") ? systemLang.split("-")[0] : systemLang;
        String alias = LocaleController.getLocaleAlias(arg);
        for (int a = 0; a < LocaleController.getInstance().languages.size(); a++) {
            LocaleController.LocaleInfo info = LocaleController.getInstance().languages.get(a);
            if (info.shortName.equals("en")) {
                englishInfo = info;
            }
            if (info.shortName.replace("_", "-").equals(systemLang) || info.shortName.equals(arg) || alias != null && info.shortName.equals(alias)) {
                systemInfo = info;
            }
            if (englishInfo != null && systemInfo != null) {
                break;
            }
        }
        if (englishInfo == null || systemInfo == null || englishInfo == systemInfo) {
            return;
        }
        TLRPC.TL_langpack_getStrings req = new TLRPC.TL_langpack_getStrings();
        LocaleController.LocaleInfo localeInfo;
        if (systemInfo != currentLocaleInfo) {
            req.lang_code = systemInfo.shortName.replace("_", "-");
            localeInfo = systemInfo;
        } else {
            req.lang_code = englishInfo.shortName.replace("_", "-");
            localeInfo = englishInfo;
        }
        req.keys.add("ContinueOnThisLanguage");
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> {
            if (response != null) {
                TLRPC.Vector vector = (TLRPC.Vector) response;
                if (vector.objects.isEmpty()) {
                    return;
                }
                final TLRPC.LangPackString string = (TLRPC.LangPackString) vector.objects.get(0);
                if (string instanceof TLRPC.TL_langPackString) {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (!destroyed) {
                            SharedPreferences preferences = MessagesController.getGlobalMainSettings();
                            preferences.edit().putString("language_showed2", LocaleController.getSystemLocaleStringIso639().toLowerCase()).apply();
                        }
                    });
                }
            }
        }, ConnectionsManager.RequestFlagWithoutLogin);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (justCreated) {
            if (LocaleController.isRTL) {
                viewPager.setCurrentItem(5);
                lastPage = 5;
            } else {
                viewPager.setCurrentItem(0);
                lastPage = 0;
            }
            justCreated = false;
        }
        AndroidUtilities.checkForCrashes(this);
        AndroidUtilities.checkForUpdates(this);
        ConnectionsManager.getInstance(currentAccount).setAppPaused(false, false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        AndroidUtilities.unregisterUpdates();
        ConnectionsManager.getInstance(currentAccount).setAppPaused(true, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyed = true;
        NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.suggestedLangpack);
        SharedPreferences preferences = MessagesController.getGlobalMainSettings();
        preferences.edit().putLong("intro_crashed_time", 0).apply();
    }

    public class MIntroCategoryAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = 5;
        private String[] tabTitles = new String[]{"", "", "", "", ""};

        MIntroCategoryAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new MIntroFrag1();
            } else if (position == 1) {
                return new MIntroFrag2();
            } else if (position == 2){
                return new MIntroFrag4();
            } else if (position == 3){
                return new MIntroFrag5();
            } else {
                return new MIntroFragFinal();
            }
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }
    }
    public static class MIntroFrag1 extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.m_intro_frag_1, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
    public static class MIntroFrag2 extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.m_intro_frag_2, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
    public static class MIntroFrag4 extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.m_intro_frag_4, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
    public static class MIntroFrag5 extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.m_intro_frag_5, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
    public static class MIntroFragFinal extends Fragment {
        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.m_intro_frag_final, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }

// </> Pics

}
