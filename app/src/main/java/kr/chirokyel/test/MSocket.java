package kr.chirokyel.test;

import android.app.Application;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

public class MSocket extends Application {

    private Socket mSocket;
    {
        try {
            mSocket = IO.socket("http://172.30.1.44:3000/");
        } catch (URISyntaxException e) {}
    }
    public Socket getSocket() {
        return mSocket;
    }
}
