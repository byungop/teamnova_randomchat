package kr.chirokyel.test;

public class Message {

    public static final int TYPE_MY_MESSAGE = 0;
    public static final int TYPE_YOUR_MESSAGE = 1;
    public static final int TYPE_LOG = 2;
    public static final int TYPE_TYPING = 3;

    private int mType;
    private String mMessage;
    private String mUID;

    public int getType() {
        return mType;
    };
    public String getMessage() {
        return mMessage;
    };
    public String getUID() {return mUID;};

    public static class MessageBuilder {
        private final int mType;
        private String mMessage;
        private String mUID;

        // mtype 넣는 메서드
        public MessageBuilder(int type) {
            mType = type;
        }

        // message 넣는 메서드
        public MessageBuilder message(String message) {
            mMessage = message;
            return this;
        }
        public MessageBuilder UID(String UID) {
            mUID = UID;
            return this;
        }

        // Message 만드는 메서드
        public Message build() {
            Message message = new Message();
            message.mType = mType;
            message.mMessage = mMessage;
            message.mUID = mUID;
            return message;
        }
    }

}
