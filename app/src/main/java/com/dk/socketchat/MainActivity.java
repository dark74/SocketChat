package com.dk.socketchat;

import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.dk.socketchat.view.ChatFragment;
import com.dk.socketchat.view.LoginFragment;

import static com.dk.socketchat.view.ChatFragment.CHAT_ARGMENTS;

public class MainActivity extends FragmentActivity implements View.OnClickListener{

    private ViewPager mViewPager;
    private TableLayout mTablayout;
    private TextView mHint;
    private String user;
    private FragmentManager fragmentManager;
    private LoginFragment loginFragment;
    private ChatFragment chatFragment;
    private LoginFragment.ILoginToMainListener loginToMainListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    private void initData() {
        onListenLogin();
    }

    private void initView() {
        mHint = (TextView) findViewById(R.id.hint);
        mHint.setOnClickListener(this);
        fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        loginFragment = new LoginFragment();
        transaction.replace(R.id.content_fragment, loginFragment);
        transaction.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hint:
                sentMsgToFragment();
                break;
        }
    }

    private void onListenLogin() {
        if (loginFragment != null) {
            loginFragment.setLoginToMainListener(new LoginFragment.ILoginToMainListener() {
                @Override
                public void sendToMain(String msg, String name) {
                    onRecvLoginMsg(msg, name);
                }
            });
        }
    }

    private void onRecvLoginMsg(String msg, String name) {
        if (msg != null && !msg.isEmpty()) {
            if (msg.equals("success")) {
                //切换fragment
                switchToChat(name);
            }
        }
    }

    private void switchToChat(String name) {
        if (fragmentManager != null) {
            chatFragment = new ChatFragment();
            Bundle sendBundle = new Bundle();
            sendBundle.putString(CHAT_ARGMENTS,name);
            chatFragment.setArguments(sendBundle);
            fragmentManager.beginTransaction().replace(R.id.content_fragment, chatFragment).commit();
        }
    }

    private void sentMsgToFragment() {
        if (loginFragment != null) {
            loginFragment.onRecvMain("DongKe");
        }
    }

    public interface IMainToLoginListener {
        void onRecvMain(String msg);
    }
}
