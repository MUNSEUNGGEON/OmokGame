package core;

public class MessageType {
    public static final String LOGIN = "LOGIN";
    public static final String JOIN = "JOIN";
    public static final String OVER = "OVER";
    public static final String VIEW = "VIEW";
    public static final String CHANGE = "CHANGE";
    public static final String RANK = "RANK";
    public static final String CREATEROOM = "CREATEROOM";
    public static final String VROOM = "VROOM";
    public static final String UROOM = "UROOM";
    public static final String EROOM = "EROOM";
    public static final String CUSER = "CUSER";
    public static final String SEARCH = "SEARCH";
    public static final String PEXIT = "PEXIT";
    public static final String REXIT = "REXIT";
    public static final String OMOK = "OMOK";
    public static final String WIN = "WIN";
    public static final String LOSE = "LOSE";
    public static final String RECORD = "RECORD";
    public static final String LOBBY_CHAT = "LOBBYCHAT";
    public static final String ROOM_CHAT = "ROOMCHAT";
    public static final String USER_INFO = "USER_INFO";

    public static final String REQUEST_PROFILE_PIC = "REQUEST_PROFILE_PIC";
    public static final String INVITE = "INVITE";
    public static final String INVITE_ACCEPT = "INVITE_ACCEPT";
    public static final String INVITE_REJECT = "INVITE_REJECT";
    public static final String PRIVATE_ROOM_CREATED = "PRIVATE_ROOM_CREATED";
    public static final String PRIVATE_CHAT = "PRIVATE_CHAT";
    public static final String LOAD_CHAT = "LOAD_CHAT";
    public static final String CHAT_HISTORY = "CHAT_HISTORY";
    public static final String CHAT_EXIT = "CHAT_EXIT";
    public static final String EMOTICON = "EMOTICON";
    public static final String PROFILE_PICTURE = "PROFILE_PICTURE";
    public static final String HOST_PROFILE_TAG = "HOST_PROFILE";    // 방 생성자의 프로필 이미지
    public static final String JOINER_PROFILE_TAG = "JOINER_PROFILE";  // 방 입장자의 프로필 이미지
    
    public static final String blackTag = "BLACK";    // 검은색 돌
    public static final String whiteTag = "WHITE";    // 흰색 돌

    public static final String FIND_ID = "FIND_ID";
    public static final String FIND_PW = "FIND_PW";
    public static final String VERIFY_USER = "VERIFY_USER";
    public static final String RESET_PW = "RESET_PW";
    public static final String TEMP_PASSWORD_SENT = "TEMP_PASSWORD_SENT";

    public static final String CREATE_GROUP_CHAT = "CREATE_GROUP_CHAT";
    public static final String JOIN_GROUP_CHAT = "JOIN_GROUP_CHAT";
    public static final String GROUP_CHAT_MESSAGE = "GROUP_CHAT_MESSAGE";
    public static final String GROUP_CHAT_LIST = "GROUP_CHAT_LIST";
    public static final String GROUP_CHAT_USERS = "GROUP_CHAT_USERS";
    public static final String LEAVE_GROUP_CHAT = "LEAVE_GROUP_CHAT";
    public static final String GROUP_CHAT_INVITE = "GROUP_CHAT_INVITE";
    public static final String GROUP_CHAT_INVITE_RESPONSE = "GROUP_CHAT_INVITE_RESPONSE";
    public static final String REQUEST_GROUP_CHAT_LIST = "REQUEST_GROUP_CHAT_LIST";

    public static final String REQUEST_REPLAY = "REQUEST_REPLAY";
    public static final String REPLAY_DATA = "REPLAY_DATA";

    public static final String READY = "READY";
    public static final String START = "START";
    public static final String READY_STATUS = "READY_STATUS";

    public static final String USER_INFO_RESPONSE = "USER_INFO_RESPONSE";

    public static final String SPECTATE_REQUEST = "SPECTATE_REQUEST";
    public static final String SPECTATE_RESPONSE = "SPECTATE_RESPONSE";
    public static final String GAME_STATE_UPDATE = "GAME_STATE_UPDATE";
    public static final String PLACE_PIECE = "PLACE_PIECE";
    public static final String MOVE_UPDATE = "MOVE_UPDATE";  // 새로운 수 업데이트용

    public static final String REQUEST_MOVE_HISTORY = "REQUEST_MOVE_HISTORY";
    public static final String MOVE_HISTORY_UPDATE = "MOVE_HISTORY_UPDATE";

    public static final String FILE_TRANSFER = "FILE_TRANSFER";
    public static final String FILE_TRANSFER_START = "FILE_TRANSFER_START";
    public static final String FILE_TRANSFER_DATA = "FILE_TRANSFER_DATA";
    public static final String FILE_TRANSFER_END = "FILE_TRANSFER_END";
} 