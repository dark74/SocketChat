package com.dk.socketchat.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dk.socketchat.MainActivity;
import com.dk.socketchat.R;

public class LoginFragment extends Fragment implements View.OnClickListener, MainActivity.IMainToLoginListener {
    public static final String LOGIN_ARGAMS = "login_argams";
    private Activity mContext;
    private String args;
    private EditText userName;
    private EditText pwd;
    private Button btnLogin;
    private Button btnRetain;
    private ILoginToMainListener loginToMainListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (Activity) context;
        args = getArguments() != null ? getArguments().getString(LOGIN_ARGAMS) : "";
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        userName = view.findViewById(R.id.user);
        pwd = view.findViewById(R.id.pwd);
        btnLogin = view.findViewById(R.id.btn_login);
        btnRetain = view.findViewById(R.id.btn_forget);
        btnLogin.setOnClickListener(this);
        btnRetain.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (loginToMainListener != null) {
                    loginToMainListener.sendToMain("success", userName.getText().toString());
                }
                break;
            case R.id.btn_forget:

                break;
        }
    }

    public void setLoginToMainListener(ILoginToMainListener listener) {
        loginToMainListener = listener;
    }

    @Override
    public void onRecvMain(String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        if (userName != null) {
            userName.setText(msg);
        }
    }


    public interface ILoginToMainListener {
        void sendToMain(String msg, String name);
    }
}
