package com.example.thu.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.thu.taxinhanhdriver.R;
import com.example.thu.utils.Enums;
import com.github.nkzawa.emitter.Emitter;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

/**
 * Created by thu on 6/12/2017.
 */

public class ChatFragment extends Fragment {
    private View root = null;
    public static Fragment newInstance(Context context) {
        BookFragment f = new BookFragment();
        return f;
    }

    protected FragmentActivity mActivity;

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://thesisk13.ddns.net:3001/");
            //mSocket = IO.socket(getResources().getString(R.string.socket_url));
        } catch (URISyntaxException e) {}
    }

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity =(FragmentActivity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        root = (ViewGroup) inflater.inflate(R.layout.activity_chat, null);

        ImageView btnSend = (ImageView)root.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText etMessage = (EditText)root.findViewById(R.id.etMessage);
                addMessage(etMessage.getText().toString(), Enums.MessageType.SELF_MESSAGE);

                JSONObject objData = new JSONObject();
                try {
                    objData.put("sender", mAuth.getCurrentUser().getUid());
                    objData.put("receiver", null);
                    objData.put("messageType", 1);
                    objData.put("message", etMessage.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                etMessage.setText("");
                mSocket.emit("MESSAGE", objData);
            }
        });


        //mSocket = IO.socket(getResources().getString(R.string.socket_chat_url));
        mSocket.connect();
        mSocket.on("MESSAGE", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject messageObj = (JSONObject)args[0];
                            addMessage(messageObj.getString("message"), Enums.MessageType.OTHER_MESSAGE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        mSocket.emit("JOIN_CHAT", mAuth.getCurrentUser().getUid());
        return root;
    }

    private void addMessage(String messageContent, Enums.MessageType messageType) {
        if (messageContent.equals("")) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) mActivity.getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        View viewMessage = null;

        //styling this message
        switch (messageType) {
            case SELF_MESSAGE:
                viewMessage = inflater.inflate(R.layout.layout_message_self, null);
                viewMessage.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

                ((LinearLayout.LayoutParams)viewMessage.getLayoutParams()).
                        setMargins(dpToPx(50), dpToPx(10), dpToPx(10), dpToPx(10));
                break;
            case OTHER_MESSAGE:
                viewMessage = inflater.inflate(R.layout.layout_message, null);
                viewMessage.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                ((LinearLayout.LayoutParams)viewMessage.getLayoutParams())
                        .setMargins(dpToPx(10), dpToPx(10), dpToPx(50), dpToPx(10));
                break;
            default:
                return;
        }

        TextView tvMessage = (TextView)viewMessage.findViewById(R.id.tvMessage);
        tvMessage.setText(messageContent);

        //find scrollview and add message to the scrollview
        LinearLayout llChatContent = (LinearLayout)root.findViewById(R.id.llChatContent);
        llChatContent.addView(viewMessage);

        //scroll message to end - NOT WORKING
        ScrollView svChatContent = (ScrollView)root.findViewById(R.id.svChatContent);
        svChatContent.fullScroll(View.FOCUS_DOWN);
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
