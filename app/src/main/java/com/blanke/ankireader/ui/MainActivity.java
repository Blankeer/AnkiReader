package com.blanke.ankireader.ui;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.afollestad.materialcab.MaterialCab;
import com.blanke.ankireader.R;
import com.blanke.ankireader.bean.Deck;
import com.blanke.ankireader.config.PlayConfig;
import com.blanke.ankireader.data.AnkiManager;
import com.blanke.ankireader.event.StartPlayEvent;
import com.blanke.ankireader.event.StopPlayEvent;
import com.blanke.ankireader.play.PlayerService;
import com.blanke.ankireader.ui.settings.SettingsActivity;
import com.mylhyl.acp.Acp;
import com.mylhyl.acp.AcpListener;
import com.mylhyl.acp.AcpOptions;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements DeckAdapter.Listener, MaterialCab.Callback {
    private Toolbar toolbar;
    private DragSelectRecyclerView mainRv;
    private FloatingActionButton fab;

    private DeckAdapter mAdapter;
    private MaterialCab cab;
    private PlayConfig playConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EventBus.getDefault().register(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        playConfig = PlayConfig.loadConfig(this);

        initRecyclerView();
        initFab();

        Acp.getInstance(this).request(new AcpOptions.Builder()
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .build(),
                new AcpListener() {
                    @Override
                    public void onGranted() {
                        prepareLoadData();
                    }

                    @Override
                    public void onDenied(List<String> permissions) {
                        Toast.makeText(MainActivity.this, "权限申请失败，程序无法运行",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void initFab() {
        fab = (FloatingActionButton) findViewById(R.id.main_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (PlayerService.isRunning()) {
                    stopPlayService();
                } else {
                    PlayConfig.saveDeckIds(MainActivity.this
                            , mAdapter.getSelectDeckIds());
                    startPlayService();
                }
                fab.setEnabled(false);
            }
        });
        initPlayFabDrawable();
    }

    private void initPlayFabDrawable() {
        fab.setEnabled(true);
        if (PlayerService.isRunning()) {
            fab.setImageResource(R.drawable.ic_stop);
        } else {
            fab.setImageResource(R.drawable.ic_play);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void stopPlay(StopPlayEvent event) {
        initPlayFabDrawable();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void startPlay(StartPlayEvent event) {
        initPlayFabDrawable();
    }

    private void initRecyclerView() {
        mainRv = (DragSelectRecyclerView) findViewById(R.id.main_rv);
        mainRv.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new DeckAdapter(this);
        mAdapter.setListener(this);
        mainRv.setAdapter(mAdapter);
    }

    private void toggleFab(boolean show) {
        if (!show && PlayerService.isRunning()) {
            return;
        }
        if (show) {
            fab.show();
        } else {
            fab.hide();
        }
    }

    private void prepareLoadData() {
        List<Deck> decks;
        try {
            decks = AnkiManager.getAllDecks();
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_anki_not_install, Toast.LENGTH_LONG).show();
            return;
        }
        if (decks == null || decks.size() == 0) {
            Toast.makeText(this, R.string.msg_deck_empty, Toast.LENGTH_LONG).show();
        }
        mAdapter.setDatas(decks);
        mAdapter.selectDecks(playConfig.getPlayDeckIds());//已经选择的
    }

    private void startPlayService() {
        Intent intent2 = new Intent(this, PlayerService.class);
        startService(intent2);
    }

    private void stopPlayService() {
        Intent intent2 = new Intent(this, PlayerService.class);
        stopService(intent2);
    }

    @Override
    public void onClick(int index) {
//        if (mAdapter.isSelected()) {
//            mAdapter.toggleSelected(index);
//        }
        // 点击转发给长按事件
        onLongClick(index);
    }

    @Override
    public void onLongClick(int index) {
        if (mAdapter.isSelected()) {
            mAdapter.toggleSelected(index);
        } else {
            mainRv.setDragSelectActive(true, index);
        }
    }

    @Override
    public void onSelectionChanged(int count) {
        if (count > 0) {
            if (cab == null) {
                cab = new MaterialCab(this, R.id.cab_stub)
                        .setMenu(R.menu.menu_selected_deck)
                        .setCloseDrawableRes(R.drawable.ic_arrow_back)
                        .start(this);
                cab.getToolbar().setTitleTextColor(Color.WHITE);
            }
            cab.setTitle(mAdapter.getSelectedCount() + "");
            toggleFab(true);//show fab
        } else if (cab != null && cab.isActive()) {
            cab.reset().finish();
            cab = null;
            toggleFab(false);//hide fab
        }
    }

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        if (item.getItemId() == R.id.select_deck_done_all) {
            mAdapter.selectAll();
        }
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mAdapter.clearSelected();
        this.cab = null;
        toggleFab(false);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mAdapter.isSelected()) {
            mAdapter.clearSelected();
            cab.reset().finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void start(Class activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    //test
    private void getFile() {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar
                + "AnkiDroid" + File.separatorChar + "collection.media" + File.separatorChar;
        Logger.d("path=" + path);
        File file = new File(path);
        Logger.d("exists=" + file.exists());
        Logger.d("canread=" + file.canRead());
        File[] files = file.listFiles();
        for (File f : files) {
            Logger.d(f.getName());
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_about:
                start(AboutActivity.class);
                break;
            case R.id.menu_main_settings:
                start(SettingsActivity.class);
                break;
        }
        return true;
    }
}
