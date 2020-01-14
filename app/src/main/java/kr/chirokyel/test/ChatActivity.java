package kr.chirokyel.test;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private Socket mSocket;
    private String UID;

    Button chat_button_send;
    EditText chat_edittext_input;
    RecyclerView chat_recyclerview_output;
    private Adapter adapter;
    private List<Message> chatList = new ArrayList<>();

    private boolean isTyping = false;
    private Handler handler = new Handler();
    private static final int TYPING_LENGTH = 600;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        setAppBar();
        getUID(); // 메인에서 보낸 UID 받기
        setRecyclerview(); // 채팅 시작 로그도 포함

        setSocket();

        // 입력버튼
        chat_button_send = findViewById(R.id.chat_button_send);
        chat_button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        // 입력란
        chat_edittext_input = findViewById(R.id.chat_edittext_input);
        chat_edittext_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                sendTyping();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // 뒤로가기 키
    @Override
    public void onBackPressed() {
        // 대화종료 다시 묻는 다이얼로그
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("대화종료");
        builder.setMessage("대화를 종료하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        builder.show();
    }

    // 앱바 세팅
    private void setAppBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("채팅");
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG);

        switch(item.getItemId())
        {
            // 뒤로가기 버튼
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.chat_menu1:
                toast.setText("신고");
                break;
            case R.id.chat_menu2:
                toast.setText("내정보");
                break;
            case R.id.chat_menu3:
                toast.setText("대화보관");
                break;
        }

        toast.show();

        return super.onOptionsItemSelected(item);
    }

    // UID 가져오기
    private void getUID() {
        Intent intent = getIntent();
        UID = intent.getStringExtra("UID");
    }

    // 리사이클러뷰 조립
    private void setRecyclerview() {
        chat_recyclerview_output = findViewById(R.id.chat_recyclerview_output);
        chat_recyclerview_output.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new Adapter(chatList);
        chat_recyclerview_output.setAdapter(adapter);

        // 채팅 시작 로그
        addLog("채팅을 시작합니다.");
    }
    // 스크롤 내리기
    private void scrollToBottom() {
        chat_recyclerview_output.scrollToPosition(adapter.getItemCount() - 1);
    }

    // 소켓 세팅
    private void setSocket() {
        MSocket ms = (MSocket) getApplication();
        mSocket = ms.getSocket();
        mSocket.on("message", onMessage);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.emit("join");
    } // 리스너 가득 담겨 있음

    // 메시지 보내기
    private void sendMessage() {
        // 내용 받기
        String message = chat_edittext_input.getText().toString();
        // 비었으면 포커스
        if (TextUtils.isEmpty(message)) {
            chat_edittext_input.requestFocus();
            return;
        }

        // 입력란 비울 때 입력중 메시지 안뜨도록
        isTyping = true;
        chat_edittext_input.setText("");
        isTyping = false;

        // 혹시 떠 있는 입력중 메시지 있다면 내리고
        mSocket.emit("stop typing", message);
        // 다른 사람들에 메시지 전송
        mSocket.emit("message", message);
        // 내 메세지 올리기
        addMyMessage(message);
    }

    // 타이핑 보낵
    private void sendTyping() {
        if (!mSocket.connected()) return;

        // 현재 입력중 떠있지 않을 때만
        if (!isTyping) {
            // 입력중 떠있다고 표시
            isTyping = true;
            // 내 입력중 표시 보내기
            mSocket.emit("typing");
        }

        // 이전 입력중 제한시간 걸려있다면 제거하고
        handler.removeCallbacks(typingTimeout);
        // 다시 제한시간 새로 걸기
        handler.postDelayed(typingTimeout, TYPING_LENGTH);
    }

    // 타이핑 제한시간
    private Runnable typingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!isTyping) return;
            // 입력중 안 떠있다 표시
            isTyping = false;
            // 시간 지나면 입력중 제거 발신
            mSocket.emit("stop typing");
        }
    };

    // message 리스너
    private Emitter.Listener onMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message;
                    try {
                        // 상대 message 올리고
                        message = data.getString("message");
                        addYourMessage(message);
                        // 상대 typing 지우고
                        removeTyping(UID);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    // typing 리스너
    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String mUID;
                    try {
                        // 상대 typing 띠우기
                        mUID = data.getString("mUID");
                        addTyping(mUID);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };
    // stop typing 리스너
    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String mUID;
                    try {
                        // 상대 typing 지우기
                        mUID = data.getString("mUID");
                        removeTyping(mUID);
                    } catch (JSONException e) {
                        return;
                    }
                }
            });
        }
    };

    // log 입력
    private void addLog(String message) {
        chatList.add(new Message.MessageBuilder(Message.TYPE_LOG)
                .message(message).build());
        adapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
    }
    // my message 입력
    private void addMyMessage(String message) {
        chatList.add(new Message.MessageBuilder(Message.TYPE_MY_MESSAGE)
                .message(message).build());
        adapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
    }
    // your message 입력
    private void addYourMessage(String message) {
        chatList.add(new Message.MessageBuilder(Message.TYPE_YOUR_MESSAGE)
                .message(message).build());
        adapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
    }
    // typing 입력
    private void addTyping(String mUID) {
        chatList.add(new Message.MessageBuilder(Message.TYPE_TYPING)
                .message("is Typing").UID(mUID).build());
        adapter.notifyItemInserted(chatList.size() - 1);
        scrollToBottom();
    }
    // typing 제거
    private void removeTyping(String mUID) {
        // 들어온지 얼마 안되는 아이템부터 뒤져서
        for (int i = chatList.size() - 1; i >= 0; i--) {
            Message message = chatList.get(i);
            // 아이템 타입과 내용이 일치하면
            if (message.getType() == Message.TYPE_TYPING && message.getUID().equals(mUID)) {
                // 리스트에서 지우기
                chatList.remove(i);
                adapter.notifyItemRemoved(i);
            }
        }
    }

}
