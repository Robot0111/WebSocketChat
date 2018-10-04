package webgroupchat.androidhive.info.chat.database;

public class ChatDbSchema {
    public static final class ChatTable{
        public static final String NAME = "chats";
        public static final class Cols{
            public static final String CONTT = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String FROMENAME = "fromename";
        }
    }
}
