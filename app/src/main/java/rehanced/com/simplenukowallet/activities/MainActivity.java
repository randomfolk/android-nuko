package rehanced.com.simplenukowallet.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.kobakei.ratethisapp.RateThisApp;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Security;

import okhttp3.Response;
import rehanced.com.simplenukowallet.R;
import rehanced.com.simplenukowallet.data.WatchWallet;
import rehanced.com.simplenukowallet.fragments.FragmentPrice;
import rehanced.com.simplenukowallet.fragments.FragmentTransactionsAll;
import rehanced.com.simplenukowallet.fragments.FragmentWallets;
import rehanced.com.simplenukowallet.interfaces.NetworkUpdateListener;
import rehanced.com.simplenukowallet.services.NotificationLauncher;
import rehanced.com.simplenukowallet.services.WalletGenService;
import rehanced.com.simplenukowallet.utils.AddressNameConverter;
import rehanced.com.simplenukowallet.utils.Dialogs;
import rehanced.com.simplenukowallet.utils.ExchangeCalculator;
import rehanced.com.simplenukowallet.utils.ExternalStorageHandler;
import rehanced.com.simplenukowallet.utils.OwnWalletUtils;
import rehanced.com.simplenukowallet.utils.Settings;
import rehanced.com.simplenukowallet.utils.TxCache;
import rehanced.com.simplenukowallet.utils.WalletStorage;

public class MainActivity extends SecureAppCompatActivity implements NetworkUpdateListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public Fragment[] fragments;
    private TabLayout tabLayout;
    private CoordinatorLayout coord;
    private SharedPreferences preferences;
    private AppBarLayout appbar;
    private int generateRefreshCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // App Intro
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (preferences.getLong("APP_INSTALLED", 0) == 0) {
            Intent intro = new Intent(this, rehanced.com.simplenukowallet.activities.AppIntroActivity.class);
            startActivityForResult(intro, rehanced.com.simplenukowallet.activities.AppIntroActivity.REQUEST_CODE);
        }

        Settings.displayAds = getPreferences().getBoolean("showAd", false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        // ------------------------- Material Drawer ---------------------------------
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.ethereum_bg)
                .build();

        DrawerBuilder wip = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .withSelectedItem(-1)

                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.drawer_import)).withIcon(R.drawable.ic_action_wallet3),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.action_settings)).withIcon(R.drawable.ic_setting),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.drawer_about)).withIcon(R.drawable.ic_about),
                        new PrimaryDrawerItem().withName(getResources().getString(R.string.website)).withIcon(R.drawable.ic_reddit)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        selectItem(position);
                        return false;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {

                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        //changeStatusBarColor();
                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        //changeStatusBarTranslucent();
                    }
                });

        if (!((AnalyticsApplication) this.getApplication()).isGooglePlayBuild()) {
            wip.addDrawerItems(new PrimaryDrawerItem().withName(getResources().getString(R.string.drawer_donate)).withIcon(R.drawable.ic_action_donate));
        }

        Drawer result = wip.build();

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        result.getActionBarDrawerToggle().setDrawerIndicatorEnabled(true);

        // ------------------------------------------------------------------------

        coord = (CoordinatorLayout) findViewById(R.id.main_content);
        appbar = (AppBarLayout) findViewById(R.id.appbar);

        fragments = new Fragment[3];
        fragments[0] = new FragmentPrice();
        fragments[1] = new FragmentWallets();
        fragments[2] = new FragmentTransactionsAll();


        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_price);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_wallet);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_transactions);

        try {
            ExchangeCalculator.getInstance().updateExchangeRates(preferences.getString("maincurrency", "USD"), this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        TxCache.getInstance().setFilepath(this.getFilesDir());
        Log.d("TxCache", "before loadfile");
        TxCache.getInstance().loadFromFile();
        Settings.initiate(this);
        NotificationLauncher.getInstance().start(this);

        if (getIntent().hasExtra("STARTAT")) { //  Click on Notification, show Transactions
            Log.d("StartAt",tabLayout.toString());

            if (tabLayout != null) {
                tabLayout.getTabAt(getIntent().getIntExtra("STARTAT", 2)).select();

            }
            broadCastDataSetChanged();
        } else if (Settings.startWithWalletTab) { // if enabled in setting select wallet tab instead of price tab
            if (tabLayout != null)
                tabLayout.getTabAt(1).select();
        }

        mViewPager.setOffscreenPageLimit(3);

        // Rate Dialog (only show on google play builds)
        if (((AnalyticsApplication) this.getApplication()).isGooglePlayBuild()) {
            try {
                RateThisApp.onCreate(this);
                RateThisApp.Config config = new RateThisApp.Config(3, 5);
                RateThisApp.init(config);
                RateThisApp.showRateDialogIfNeeded(this, R.style.AlertDialogTheme);
            } catch (Exception e) {
            }
        }
        //Security.removeProvider("BC");
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);

    }

    public void setSelectedPage(int i){
        if(mViewPager != null)
            mViewPager.setCurrentItem(i, true);
    }

    // Spongy Castle Provider
    static {
        Security.insertProviderAt(new org.spongycastle.jce.provider.BouncyCastleProvider(), 1);
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ExternalStorageHandler.REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (fragments != null && fragments[1] != null)
                        ((FragmentWallets) fragments[1]).export();
                } else {
                    snackError(getString(R.string.main_grant_permission_export));
                }
                return;
            }
            case ExternalStorageHandler.REQUEST_READ_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        WalletStorage.getInstance(this).importingWalletsDetector(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    snackError(getString(R.string.main_grant_permission_import));
                }
                return;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        broadCastDataSetChanged();

        // Update wallets if activity resumed and a new wallet was found (finished generation or added as watch only address)
        if (fragments != null && fragments[1] != null && WalletStorage.getInstance(this).get().size() != ((FragmentWallets) fragments[1]).getDisplayedWalletCount()) {
            try {
                ((FragmentWallets) fragments[1]).update();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
       /* if(preferences == null)
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("APP_PAUSED", true);
        editor.apply();*/
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == rehanced.com.simplenukowallet.activities.QRScanActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                byte type = data.getByteExtra("TYPE", rehanced.com.simplenukowallet.activities.QRScanActivity.SCAN_ONLY);
                if (type == rehanced.com.simplenukowallet.activities.QRScanActivity.SCAN_ONLY) {
                    if (data.getStringExtra("ADDRESS").length() != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
                        snackError("Invalid address!");
                        return;
                    }
                    Intent watch = new Intent(this, rehanced.com.simplenukowallet.activities.AddressDetailActivity.class);
                    watch.putExtra("ADDRESS", data.getStringExtra("ADDRESS"));
                    startActivity(watch);
                } else if (type == rehanced.com.simplenukowallet.activities.QRScanActivity.ADD_TO_WALLETS) {
                    if (data.getStringExtra("ADDRESS").length() != 42 || !data.getStringExtra("ADDRESS").startsWith("0x")) {
                        snackError("Invalid address!");
                        return;
                    }
                    final boolean suc = WalletStorage.getInstance(this).add(new WatchWallet(data.getStringExtra("ADDRESS")), this);
                    new Handler().postDelayed(
                            new Runnable() {
                                @Override
                                public void run() {
                                    if (fragments != null && fragments[1] != null) {
                                        try {
                                            ((FragmentWallets) fragments[1]).update();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    if (tabLayout != null)
                                        tabLayout.getTabAt(1).select();
                                    Snackbar mySnackbar = Snackbar.make(coord,
                                            MainActivity.this.getResources().getString(suc ? R.string.main_ac_wallet_added_suc : R.string.main_ac_wallet_added_er), Snackbar.LENGTH_SHORT);
                                    if (suc)
                                        AddressNameConverter.getInstance(MainActivity.this).put(data.getStringExtra("ADDRESS"), "Watch " + data.getStringExtra("ADDRESS").substring(0, 6), MainActivity.this);

                                    mySnackbar.show();

                                }
                            }, 100);
                } else if (type == rehanced.com.simplenukowallet.activities.QRScanActivity.REQUEST_PAYMENT) {
                    if (WalletStorage.getInstance(this).getFullOnly().size() == 0) {
                        Dialogs.noFullWallet(this);
                    } else {
                        Intent watch = new Intent(this, rehanced.com.simplenukowallet.activities.SendActivity.class);
                        watch.putExtra("TO_ADDRESS", data.getStringExtra("ADDRESS"));
                        watch.putExtra("AMOUNT", data.getStringExtra("AMOUNT"));
                        startActivity(watch);
                    }
                } else if (type == rehanced.com.simplenukowallet.activities.QRScanActivity.PRIVATE_KEY) {
                    if (OwnWalletUtils.isValidPrivateKey(data.getStringExtra("ADDRESS"))) {
                        importPrivateKey(data.getStringExtra("ADDRESS"));
                    } else {
                        this.snackError("Invalid private key!");
                    }
                }
            } else {
                Snackbar mySnackbar = Snackbar.make(coord,
                        MainActivity.this.getResources().getString(R.string.main_ac_wallet_added_fatal), Snackbar.LENGTH_SHORT);
                mySnackbar.show();
            }
        } else if (requestCode == rehanced.com.simplenukowallet.activities.WalletGenActivity.REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent generatingService = new Intent(this, WalletGenService.class);
                generatingService.putExtra("PASSWORD", data.getStringExtra("PASSWORD"));
                if (data.hasExtra("PRIVATE_KEY"))
                    generatingService.putExtra("PRIVATE_KEY", data.getStringExtra("PRIVATE_KEY"));
                startService(generatingService);

                final Handler handler = new Handler();
                generateRefreshCount = 0;
                final int walletcount = WalletStorage.getInstance(this).getFullOnly().size();
                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            if(walletcount < WalletStorage.getInstance(MainActivity.this).getFullOnly().size()) {
                                ((FragmentWallets) fragments[1]).update();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (generateRefreshCount++ < 8)
                            handler.postDelayed(this, 3000);
                    }
                };
                handler.postDelayed(runnable, 4000);
            }
        } else if (requestCode == rehanced.com.simplenukowallet.activities.SendActivity.REQUEST_CODE) {
            Log.d("from send","before check");
            if (resultCode == RESULT_OK) {
                if (fragments == null || fragments[2] == null) return;
                Log.d("from send","ok");
                ((FragmentTransactionsAll) fragments[2]).addUnconfirmedTransaction(data.getStringExtra("FROM_ADDRESS"), data.getStringExtra("TO_ADDRESS"), new BigDecimal("-" + data.getStringExtra("AMOUNT")).multiply(new BigDecimal("1000000000000000000")).toBigInteger());
                if (tabLayout != null)
                    tabLayout.getTabAt(2).select();
            }
        } else if (requestCode == rehanced.com.simplenukowallet.activities.AppIntroActivity.REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                finish();
            } else {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("APP_INSTALLED", System.currentTimeMillis());
                editor.commit();
            }
        } else if (requestCode == rehanced.com.simplenukowallet.activities.SettingsActivity.REQUEST_CODE) {
            if (!preferences.getString("maincurrency", "USD").equals(ExchangeCalculator.getInstance().getMainCurreny().getName())) {
                try {
                    ExchangeCalculator.getInstance().updateExchangeRates(preferences.getString("maincurrency", "USD"), this);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                new Handler().postDelayed(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (fragments != null) {
                                    if (fragments[0] != null)
                                        ((FragmentPrice) fragments[0]).update(true);
                                    if (fragments[1] != null) {
                                        ((FragmentWallets) fragments[1]).updateBalanceText();
                                        ((FragmentWallets) fragments[1]).notifyDataSetChanged();
                                    }
                                    if (fragments[2] != null)
                                        ((FragmentTransactionsAll) fragments[2]).notifyDataSetChanged();
                                }
                            }
                        }, 950);
            }

        }else{
            Log.d("return",String.valueOf(requestCode) + data.getDataString());

        }
    }

    public void importPrivateKey(String privatekey) {
        Intent genI = new Intent(this, rehanced.com.simplenukowallet.activities.WalletGenActivity.class);
        genI.putExtra("PRIVATE_KEY", privatekey);
        startActivityForResult(genI, rehanced.com.simplenukowallet.activities.WalletGenActivity.REQUEST_CODE);
    }

    public void snackError(String s, int length) {
        if (coord == null) return;
        Snackbar mySnackbar = Snackbar.make(coord, s, length);
        mySnackbar.show();
    }

    public void snackError(String s) {
        snackError(s, Snackbar.LENGTH_SHORT);
    }

    private void selectItem(int position) {
        switch (position) {
            case 1: {
                try {
                    WalletStorage.getInstance(this).importingWalletsDetector(this);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case 2: {
                Intent settings = new Intent(this, rehanced.com.simplenukowallet.activities.SettingsActivity.class);
                startActivityForResult(settings, rehanced.com.simplenukowallet.activities.SettingsActivity.REQUEST_CODE);
                break;
            }
            case 3: {
                new AlertDialog.Builder(this)
                        .setTitle("About Nekonium Lunary")
                        .setMessage("Nekonium wallet is published under GPL3\n" +
                                 "\n" +
                                "\nCredits:\n" +
                                "MaterialDrawer by Mike Penz\n" +
                                "MPAndroidChart by Philipp Jahoda\n" +
                                "Mobile Vision Barcode Scanner by KingsMentor\n" +
                                "XZING\n" +
                                "FloatingActionButton by Dmytro Tarianyk\n" +
                                "RateThisApp by Keisuke Kobayashi\n" +
                                "AppIntro by Maximilian Narr\n" +
                                "Material Dialogs by Aidan Michael Follestad\n" +
                                "Poloniex for price data\n" +
                                "Web3j by Conor Svensson\n" +
                                "PatternLock by Zhang Hai\n" +
                                "UI suggestion by @cs discord\n" +
                                "Lunary is published under GPL3, by Manuel S. C. for Rehanced\n" +
                                "Nekonium wallet is based on Lunary wallet app.")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .show();
                break;
            }
            case 4: {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://nekonium.github.io/"));
                startActivity(i);
                break;
            }
            case 5: {
                if (WalletStorage.getInstance(this).getFullOnly().size() == 0) {
                    Dialogs.noFullWallet(this);
                } else {
                    Intent donate = new Intent(this, SendActivity.class);
                    donate.putExtra("TO_ADDRESS", "0x7201bc1de01Ca412b6106dF436b524F872fd54b1");
                    startActivity(donate);
                }
                break;
            }
            default: {
                return;
            }
        }
    }

    public void broadCastDataSetChanged() {
        if (fragments != null && fragments[1] != null && fragments[2] != null) {
            ((FragmentWallets) fragments[1]).notifyDataSetChanged();
            ((FragmentTransactionsAll) fragments[2]).notifyDataSetChanged();
        }
    }

    @Override
    public void onUpdate(Response s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                broadCastDataSetChanged();
                if (fragments != null && fragments[0] != null) {
                    ((FragmentPrice) fragments[0]).update(true);
                }
            }
        });
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    public AppBarLayout getAppBar() {
        return appbar;
    }
}
