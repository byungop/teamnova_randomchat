package kr.chirokyel.test;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    int RC_SIGN_IN = 999;

    TextView main_textview_uid;
    Button main_button_chatting;

    String UID;
    RequestQueue requestQueue;
    String url = "http://192.168.0.222/test/uid.php";

    private Socket mSocket;
    boolean isConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLogin();

    }
    // 로그인 여부 확인
    public void isLogin() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            // 로그인 되 있으면 바로 화면 뿌리기
            UID = auth.getCurrentUser().getUid();
            setContentView(R.layout.activity_main);
            onCreateView();
        } else {
            // 안되있으면 ui 호출
            login();
        }
    }

    // 로그인 메서드
    public void login() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()); // 어떤 로그인 사용할지 선택

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.FirebaseLoginTheme)
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN); // 로그인 UI 실행
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // uid 받아서 db 저장
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                UID = user.getUid();
                requestQueue = Volley.newRequestQueue(MainActivity.this);
                isSetUID(UID);
                // 화면 뿌리기
                setContentView(R.layout.activity_main);
                onCreateView();
            } else {
                // 백버튼 누르면
                if (response == null) {
                    finish();
                }
            }
        }
    }

    // 로그아웃 메서드
    private void logout() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(), "로그아웃", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    // UID DB 연결
    private void isSetUID(final String uid) {

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // DB에 UID존재 않으면
                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // 연결 실패하면 작동할 코드
                System.out.println("error");
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                // 웹으로 보낼 데이터
                Map<String, String> params = new HashMap<>();
                params.put("code", "isSetUID");
                params.put("uid", uid);
                return params;
            }
        };
        // 웹 연결 실행
        requestQueue.add(stringRequest);
    }

    // 화면 뿌리기
    private void onCreateView() {

        // 앱바
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("메인");

        // 소켓 객체화
        MSocket ms = (MSocket) getApplication();
        mSocket = ms.getSocket();

        // 소켓 연결하고
        mSocket.connect();
        // 바로 로그인 발신
        mSocket.emit("login", UID);

        main_textview_uid = findViewById(R.id.main_textview_uid);
        main_textview_uid.setText("UID : " + UID);

        main_button_chatting = findViewById(R.id.main_button_chatting);
        main_button_chatting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isConnect = mSocket.connected();
                if (isConnect) {
                    // 서버 연결 되있으면 채팅방으로 이동
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra("UID", UID);
                    startActivity(intent);
                    // 안되있으면 인터넷 확인 경고
                } else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 앱바 설정
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Toast toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_LONG);

        switch(item.getItemId())
        {
            case R.id.main_logout:
                logout();
                toast.setText("로그아웃");
                break;
            case R.id.main_myInfo:
                toast.setText("내정보");
                break;
            case R.id.main_chat:
                toast.setText("대화보관");
                break;
            case R.id.main_message:
                toast.setText("메세지");
                break;
        }
        toast.show();
        return super.onOptionsItemSelected(item);
    }
}
