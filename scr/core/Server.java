package core;

import DB.*;
import core.Room.MoveRecord;

import static core.MessageType.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

// 클라이언트의 연결 요청 및 입출력을 상시 관리하는 클래스.
public class Server {
    private ServerSocket ss = null;
    private boolean running = false;
    private Thread acceptThread;
    
    /* 각 객체들을 Vector로 관리 */
    private Vector<CCUser> alluser;        // 연결된 모든 클라이언트
    private Vector<CCUser> waituser;       // 대기실에 있는 클라이언트
    private Vector<Room> room;             // 생성된 Room
    
    static Map<String, byte[]> profilePictures = new HashMap<>();

    GroupChatHandler groupChatHandler;
    private UserInfoHandler userInfoHandler;
    private Database db;

    public static void main(String[] args) {
        Server server = new Server();
        server.alluser = new Vector<>();
        server.waituser = new Vector<>();
        server.room = new Vector<>();
        
        // GroupChatHandler 초기화 추가
        server.groupChatHandler = new GroupChatHandler(server);

        try {
            // 서버 소켓 준비
            server.ss = new ServerSocket(1228);
            System.out.println("[Server] 서버 소켓 준비 완료");
            
            // 클라이언트의 연결 요청을 상시 대기
            while (!server.ss.isClosed()) {
                try {
                    Socket socket = server.ss.accept();
                    CCUser c = new CCUser(socket, server);
                    c.start();
                } catch (IOException e) {
                    if (!server.ss.isClosed()) {
                        System.out.println("[Server] 오류 발생: " + e.toString());
                    }
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 오류 발생: " + e.toString());
        } finally {
            // 자원 해제
            try {
                if (server.ss != null && !server.ss.isClosed()) {
                    server.ss.close();
                }
            } catch (IOException e) {
                System.out.println("[Server] 서버 소켓 종료 오류: " + e.toString());
            }
        }
    }

    // 자 메서드 가
    public Vector<CCUser> getAllUsers() {
        return alluser;
    }

    public Vector<CCUser> getWaitUsers() {
        return waituser;
    }

    public Vector<Room> getRooms() {
        return room;
    }

    // ServerSocket getter 추가
    public ServerSocket getServerSocket() {
        return ss;
    }

    // 서버 상태 확인 메서드 추가
    public boolean isRunning() {
        return running && ss != null && !ss.isClosed();
    }

    public void initialize() throws IOException {
        // UTF-8 인코딩 설정
        System.setProperty("file.encoding", "UTF-8");
        Charset.defaultCharset(); // 기본 문자셋 초기화

        alluser = new Vector<>();
        waituser = new Vector<>();
        room = new Vector<>();
        running = true;
        
        // 서버 소켓 준비
        ss = new ServerSocket(1228);
        System.out.println(new String("[Server] 서버 소켓 준비 완료".getBytes("UTF-8"), "UTF-8"));
        
        // 클라이언트 수락 스레드 시작
        acceptThread = new Thread(() -> {
            while (running && !ss.isClosed()) {
                try {
                    Socket socket = ss.accept();
                    CCUser c = new CCUser(socket, this);
                    c.start();
                } catch (IOException e) {
                    if (running) {
                        System.out.println("[Server] 오류 발생: " + e.toString());
                    }
                }
            }
        });
        acceptThread.start();

        groupChatHandler = new GroupChatHandler(this);
        this.userInfoHandler = new UserInfoHandler(this);
    }

    public void stopServer() {
        running = false;
        try {
            // 모든 클라이언트 연결 종료
            if (alluser != null) {
                for (CCUser user : alluser) {
                    try {
                        if (user != null && user.getSocket() != null) {
                            user.getSocket().close();
                        }
                    } catch (IOException e) {
                        System.out.println("[Server] 클라이언트 연결 종료 중 오류: " + e.getMessage());
                    }
                }
                alluser.clear();
            }

            // 대기실 유저 목록 초기화
            if (waituser != null) {
                waituser.clear();
            }

            // 방 목록 초기화
            if (room != null) {
                room.clear();
            }

            // 서버 소켓 종료
            if (ss != null && !ss.isClosed()) {
                ss.close();
            }

            System.out.println("[Server] 서버가 정상적으로 종료었습니다.");
        } catch (IOException e) {
            System.out.println("[Server] 서버 종료 중 오류 발생: " + e.getMessage());
        }
    }

    // System.out.println() 사용하는 모든 부분 수정
    private void printMessage(String message) {
        try {
            System.out.println(new String(message.getBytes("UTF-8"), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public CCUser findUserByNickname(String nickname) {
        for (CCUser user : alluser) {
            if (user.nickname.equals(nickname)) {
                return user;
            }
        }
        return null;
    }

    public GroupChatHandler getGroupChatHandler() {
        return groupChatHandler;
    }

    public void addWaitUser(CCUser user) {
        waituser.add(user);
        System.out.println("[Server] 대기실 입장: " + user.nickname);
        
        // 대기실 입장 시 즉시 방 목록 전송
        groupChatHandler.broadcastGroupChatList();
    }

    public void handleMessage(CCUser user, String msg) {
        String[] m = msg.split("//");
        String tag = m[0];
        
        switch (tag) {
            case LOGIN:
                // 로그인 성공 후 즉시 방 목록 전송
                if (groupChatHandler != null) {
                    groupChatHandler.broadcastGroupChatList();
                }
                break;
            // ... other cases ...
        }
    }

    private void logMessage(String message) {
        LogManager.getInstance().log("Server", message);
    }

    // getter 추가
    public UserInfoHandler getUserInfoHandler() {
        return userInfoHandler;
    }

    // Database getter 추가
    public Database getDatabase() {
        return db;
    }
}

// 서버에 접속한 유저와의 메시지 송수신을 관리하는 클래스.
class CCUser extends Thread {
    private Server server;
    private Socket socket;
    public Room myRoom = null;
    

    public Socket getSocket() {
        return socket;
    }

    /* 프로필 이미지 관련 태그 */

	private static final int PAGE_SIZE = 20;

    // 필드
    private OutputStream os;
    DataOutputStream dos;
    private InputStream is;
    private DataInputStream dis;
    
    String nickname;    // 클라이언트의 닉네임을 저장할 필드
    private static Vector<CCUser> auser; // 연결된 모든 클라이언트
    private static Vector<CCUser> wuser; // 대기실에 는 클라이언트
    private static Vector<Room> room; // 생성된 Room

    Database db = new Database();

    public CCUser(Socket _s, Server _ss) {
        this.socket = _s;
        this.server = _ss;

        // Server 클스의 접근자 메서드를 통해 필드에 접근
        auser = server.getAllUsers();
        wuser = server.getWaitUsers();
        room = server.getRooms();
    }

    public void run() {
        try {
            System.out.println("[Server] 클라이언트 접속 > " + this.socket.toString());

            os = this.socket.getOutputStream();
            dos = new DataOutputStream(os);
            is = this.socket.getInputStream();
            dis = new DataInputStream(is);
            
            while (true) {
                String msg = dis.readUTF();
                String[] m = msg.split("//");
                
                switch (m[0]) {
                    case LOGIN:
                        handleLogin(m);
                        break;
                    case JOIN:
                        handleJoin(m);
                        break;
                    case OVER:
                        handleOverlapCheck(m);
                        break;
                    case VIEW:
                        handleViewInfo();
                        break;
                    case CHANGE:
                        handleChangeInfo(m);
                        break;
                    case RANK:
                        handleViewRank();
                        break;
                    case CREATEROOM:
                        handleCreateRoom(m);
                        break;
                    case EROOM:
                        handleEnterRoom(m);
                        break;
                    case ROOM_CHAT:
                        handleRoomChat(m);
                        break;
                    case LOBBY_CHAT:
                        handleLobbyChat(m);
                        break;
                    case SEARCH:
                        handleSearchRank(m);
                        break;
                    case PEXIT:
                        handleExit();
                        break;
                    case REXIT:
                        handleRoomExit();
                        break;
                    case OMOK:
                        handleOmok(m);
                        break;
                    case PROFILE_PICTURE:
                        handleProfilePicture();
                        break;
                    case WIN:
                        handleWin();
                        break;
                    case LOSE:
                        handleLose();
                        break;
                    case INVITE:
                        handleInvite(m);
                        break;
                    case INVITE_ACCEPT:
                        handleInviteAccept(m);
                        break;
                    case INVITE_REJECT:
                        handleInviteReject(m);
                        break;
                    case PRIVATE_CHAT:
                        handlePrivateChat(m);
                        break;
                    case LOAD_CHAT:
                        handleLoadChatHistory(m);
                        break;
                    case CHAT_EXIT:
                        handleChatExit(m);
                        break;
                    case EMOTICON:
                        handleEmoticonMessage(m[1], m[2]);  // receiver, emoticonName
                        break;
                    case FIND_ID:
                        handleFindId(m);
                        break;
                    case FIND_PW:
                        handleFindPw(m);
                        break;
                    case VERIFY_USER:
                        handleVerifyUser(m);
                        break;
                    case RESET_PW:
                        handleResetPassword(m);
                        break;
                    case CREATE_GROUP_CHAT:
                        server.groupChatHandler.handleCreateGroupChat(this, m[1]);
                        break;
                    case JOIN_GROUP_CHAT:
                        server.groupChatHandler.handleJoinGroupChat(this, m[1]);
                        break;
                    case GROUP_CHAT_MESSAGE:
                        server.groupChatHandler.handleGroupChatMessage(this, m[1], m[2]);
                        break;
                    case LEAVE_GROUP_CHAT:
                        server.groupChatHandler.handleLeaveGroupChat(this, m[1]);
                        break;
                    case GROUP_CHAT_USERS:
                        if (m[1].equals("REQUEST")) {
                            server.groupChatHandler.handleUserListRequest(this, m[2]);  // m[2]는 roomName
                        }
                        break;
                    case GROUP_CHAT_INVITE:
                        if (m.length >= 3) {
                            String roomName = m[1];
                            String invitee = m[2];
                            System.out.println("[Server] 그룹 채팅 초대 요청: " + nickname + " -> " + invitee + " (방: " + roomName + ")");
                            server.groupChatHandler.handleGroupChatInvite(this, roomName, invitee);
                        }
                        break;
                    case GROUP_CHAT_INVITE_RESPONSE:
                        if (m.length >= 4) {
                            String roomName = m[1];
                            String response = m[3];
                            server.groupChatHandler.handleInviteResponse(this, roomName, response);
                        }
                        break;
                    case REQUEST_REPLAY:
                        handleReplayRequest();
                        break;
                    case READY:
                        handleReady(m);
                        break;
                    case START:
                        handleStart();
                        break;
                    case USER_INFO:
                        String targetNickname = m[1];
                        server.getUserInfoHandler().handleUserInfoRequest(this, targetNickname);
                        break;
                    case SPECTATE_REQUEST:
                        if (m.length > 1) {
                            handleSpectateRequest(m[1]);
                        }
                        break;
                    case PLACE_PIECE:
                        handlePlacePiece(m);
                        break;
                    case GAME_STATE_UPDATE:
                        if (myRoom != null) {
                            String gameState = m[1];
                            dos.writeUTF(GAME_STATE_UPDATE + "//" + gameState);
                        }
                        break;
                    case REQUEST_MOVE_HISTORY:
                        if (myRoom != null) {
                            myRoom.handleMoveHistoryRequest(this);
                        }
                        break;
                    case FILE_TRANSFER:
                        handleFileTransfer(m);
                        break;
                    case FILE_TRANSFER_START:
                        System.out.println("[Server] 파일 전송 시작: " + Arrays.toString(m));
                        handleFileTransferStart(m);
                        break;
                    case FILE_TRANSFER_DATA:
                        System.out.println("[Server] 파일 데이터 수신: " + m[1] + ", " + m[2] + ", 청크: " + m[3] + "/" + m[4]);
                        handleFileTransferData(m);
                        break;
                    case FILE_TRANSFER_END:
                        System.out.println("[Server] 파일 전송 완료: " + Arrays.toString(m));
                        handleFileTransferEnd(m);
                        break;
                }
            }
        } catch (IOException e) {
            server.groupChatHandler.handleUserDisconnect(this);
            handleClientDisconnect();
        }
    }

    private void handleClientDisconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println("[Server] 소켓 종료 오류: " + e.toString());
        }
    }

    private void handleLogin(String[] m) throws IOException {
        String mm = db.loginCheck(m[1], m[2]);
        
        if (mm.equals("DELETED")) {
            dos.writeUTF(LOGIN + "//DELETED");
        }
        else if (!mm.equals("null")) {
            nickname = mm;  // 닉네임 설정
            
            // 전 정보 조회
            ResultSet rs = db.getStats(nickname);
            int win = 0;
            int lose = 0;
            try {
                if (rs.next()) {
                    win = rs.getInt("win");
                    lose = rs.getInt("lose");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            
            // admin 계정은 별도 처리
            if (m[1].equals("admin")) {
                dos.writeUTF(LOGIN + "//OKAY");
                return;
            }
            
            // 로그인 성공 및 전적 정보 전송 (닉네임도 함께 송)
            dos.writeUTF(LOGIN + "//OKAY//" + nickname + "//" + win + "//" + lose);
            
            auser.add(this);    // 모든 접속 인원에 추가
            wuser.add(this);    // 대기실 접속 인원에 추가
            
            sendWait(connectedUser());
            if (!room.isEmpty()) {
                sendWait(roomInfo());
            }
        } else {    // 로그인 실패
            dos.writeUTF(LOGIN + "//FAIL");
        }
    }

    private void handleJoin(String[] m) throws IOException {
        if (m.length >= 12) { // 배열의 길이를 검사하여 필요한 정보가 모 있는지 확인
            byte[] profilePicture = null;

            // 프로필 사 를 수신하고 이미지를 별도로 수신
            int pictureLength = dis.readInt();
            if (pictureLength > 0) {
                profilePicture = new byte[pictureLength];
                dis.readFully(profilePicture);
            }

            // 회원가입 처리
            if (db.joinCheck(m[1], m[2], m[3], m[4], m[5], m[6], m[7], m[8], m[9], m[10], m[11], profilePicture)) {
                dos.writeUTF(JOIN + "//OKAY");
            } else {
                dos.writeUTF(JOIN + "//FAIL");
            }
        } else {
            System.out.println("[Server] 회원가입 데이터가 부족합니다.");
            dos.writeUTF(JOIN + "//FAIL");
        }
    }
    
    private void handleOverlapCheck(String[] m) throws IOException {
        if (db.overCheck(m[1], m[2])) {    // 사용 가능
            dos.writeUTF(OVER + "//OKAY");
        } else {    // 사용 불가능
            dos.writeUTF(OVER + "//FAIL");
        }
    }

    private void handleViewInfo() throws IOException {
        if (!db.viewInfo(nickname).equals("null")) {    // 조회 성공
            dos.writeUTF(VIEW + "//" + db.viewInfo(nickname));    // 태그와 조회한 내용을 같이 전송
        } else {    // 조회 실패
            dos.writeUTF(VIEW + "//FAIL");
        }
    }

    private void handleChangeInfo(String[] m) throws IOException {
        if (db.changeInfo(nickname, m[1], m[2])) {    // 변경 성공
            dos.writeUTF(CHANGE + "//OKAY");
        } else {    // 변 실패
            dos.writeUTF(CHANGE + "//FAIL");
        }
    }

    private void handleViewRank() throws IOException {
        if (!db.viewRank().equals("")) {    // 조회 성공
            dos.writeUTF(RANK + "//" + db.viewRank());    // 태그 조회한 내용을 같이 전
        } else {    // 조회 실
            dos.writeUTF(RANK + "//FAIL");
        }
    }

    private void handleCreateRoom(String[] m) throws IOException {
        myRoom = new Room(CHANGE);    // 새로운 Room 객체 생성 후 myRoom에 초기화
        myRoom.title = m[1];    // 방 제목을 m[1]로 설정
        myRoom.count++;            // 방의 인원수 하 추가
        
        room.add(myRoom);        // room 배열에 myRoom을 추가
        
        myRoom.ccu.add(this);    // myRoom의 접속인원에 클라이언트 추가
        wuser.remove(this);        // 대기실 접속 인원에서 클라이언트 삭제
        
        dos.writeUTF(CREATEROOM + "//OKAY");

        System.out.println("[Server] " + nickname + " : 방 '" + m[1] + "' 생성");

        // 1. 방 생성자의 프로필 미지를 저장
        byte[] hostProfileImage = getProfilePictureFromMemory(nickname);
        if (hostProfileImage != null) {
            // 방 생성자의 프로필을 방에 저장
            myRoom.hostProfileImage = hostProfileImage;
            System.out.println("[Server] 방 생성자 프로필 저장: " + nickname);
        }

        // 2. 방 생자에게도 자신의 프로필 이미지를 전송
        if (hostProfileImage != null) {
            sendProfileImageToClient(nickname, hostProfileImage);
            System.out.println("[Server] 방 생자에게 프로필 전송: " + nickname);
        }

        sendWait(roomInfo());    
        sendRoom(roomUser());    
    }

    private void handleEnterRoom(String[] m) throws IOException {
        boolean roomFound = false;

        for (Room r : room) {
            if (r.title.equals(m[1])) {
                roomFound = true;

                // 방의 인원이 2명 미만인 경우 입장 허용
                if (r.count < 2) {
                    myRoom = r;
                    myRoom.count++;  // 방의 인원 수 증가

                    wuser.remove(this);        
                    myRoom.ccu.add(this);   

                    sendWait(roomInfo());    // 대기실 보 업데이트
                    sendRoom(roomUser());   // 방 사용자 정보 업데이트
                    
                    dos.writeUTF(EROOM + "//OKAY");
                    System.out.println("[1][Server] " + nickname + " : 방 '" + m[1] + "' 입장");

                    // 1. 방의 프로필 이미지를 입장자에게 전송
                    CCUser hostUser = myRoom.ccu.get(0);
                    byte[] hostProfileImage = getProfilePictureFromMemory(hostUser.nickname);
                    if (hostProfileImage != null) {
                        dos.writeUTF(HOST_PROFILE_TAG);
                        dos.writeInt(hostProfileImage.length);
                        dos.write(hostProfileImage);
                        dos.flush();
                        System.out.println("[2][Server] 방장 프로필 전송: " + hostUser.nickname + " -> " + nickname);
                    }

                    // 2. 입장자의 프로필 이미지를 방장에게 전송
                    byte[] joinerProfileImage = getProfilePictureFromMemory(nickname);
                    if (joinerProfileImage != null) {
                        // 방장에게 입장자 프로필 전송
                        for (CCUser user : myRoom.ccu) {
                            if (!user.nickname.equals(nickname)) {
                                user.dos.writeUTF(JOINER_PROFILE_TAG);
                                user.dos.writeInt(joinerProfileImage.length);
                                user.dos.write(joinerProfileImage);
                                user.dos.flush();
                                System.out.println("[3][Server] 입장자 프로필 전송: " + nickname + " -> " + user.nickname);
                            }
                        }
                        
                        // 3. 입장자 자신에게도 자신의 프로필 전송
                        dos.writeUTF(JOINER_PROFILE_TAG);
                        dos.writeInt(joinerProfileImage.length);
                        dos.write(joinerProfileImage);
                        dos.flush();
                        System.out.println("[4][Server] 입장자 자신의 프로필 전송: " + nickname);
                    }
                    break;
                } else {
                    dos.writeUTF(EROOM + "//FAIL");
                    System.out.println("[5][Server] 인원 초과. 입장 불가능");
                    break;
                }
            }
        }
        if (!roomFound) {
            dos.writeUTF(EROOM + "//FAIL");
            System.out.println("[6][Server] " + nickname + " : 방 '" + m[1] + "' 입장 실패");
        }
    }


    private void handleRoomChat(String[] m) {
        sendRoom(ROOM_CHAT + "//" + nickname + ": " + m[1]);
    }

    private void handleLobbyChat(String[] m) {
        sendWait(LOBBY_CHAT + "//" + nickname + ": " + m[1]);
    }

    private void handleSearchRank(String[] m) throws IOException {
        String mm = db.searchRank(m[1]);
        
        if (!mm.equals("null")) {    // 조회 성공
            dos.writeUTF(SEARCH + "//" + mm);    // 태그와 조회한 내용을 같이 전송
        } else {    // 조회 실패
            dos.writeUTF(SEARCH + "//FAIL");
        }
    }

    private void handleExit() {
        // 방에 있는 경우 방 먼저 나가기
        if (myRoom != null) {
            handleRoomExit();
        }
        
        // 서버에서 완전히 나가
        auser.remove(this);        // 전체 접속 인원에서 클라이언트 삭제
        wuser.remove(this);        // 대기실 접속 인원에서 클라이언트 삭제
        
        sendWait(connectedUser());    // 대기실 접속 인원에 전체 접속 인원을 전송
        System.out.println("[Server] " + nickname + " : 서버 퇴장");
    }

    private void handleRoomExit() {
        if (myRoom == null) return;
        
        // 1. 방에 있는 다른 사용자들에게 알림
        for (CCUser user : myRoom.ccu) {
            if (!user.nickname.equals(nickname)) {
                try {
                    user.dos.writeUTF(REXIT + "//" + nickname);
                    user.dos.flush();
                } catch (IOException e) {
                    System.out.println("[Server] 알림 전송 실패: " + e.getMessage());
                }
            }
        }

        // 2. 방 정보 업데이트
        myRoom.ccu.remove(this);    // 방 접속 인원에서 제거
        myRoom.count--;            // 방 인원수 감소
        wuser.add(this);          // 대기실로 이동
        
        System.out.println("[Server] " + nickname + " : 방 '" + myRoom.title + "' 퇴장");
        
        // 3. 빈 방 처리
        if (myRoom.count == 0) {
            room.remove(myRoom);
        }
        
        // 4. 게임 상태 초기화
        myRoom.resetGame();
        
        // 5. 방 목록 및 유저 목록 업데이트
        if (!room.isEmpty()) {
            sendRoom(roomUser());
        }
        sendWait(roomInfo());
        sendWait(connectedUser());
        
        // 6. 방 참조 제거
        myRoom = null;
    }

    private void handleOmok(String[] m) throws IOException {
        String x = m[1];
        String y = m[2];
        String color = m[3];
        
        // 수 기록 처리 추가
        if (myRoom != null) {
            myRoom.recordMove(Integer.parseInt(x), Integer.parseInt(y), color);
            System.out.println("[Server] 수 기록 완료: " + x + "," + y + "," + color);
        }

        // 상대방에게 돌 정보 전달
        for (CCUser cu : myRoom.ccu) {
            if (cu != this) {
                cu.dos.writeUTF(OMOK + "//" + x + "//" + y + "//" + color);
            }
        }
    }

    private void handleProfilePicture() throws IOException {
        // 프로필 사진 데이터 길이 수신
        int length = dis.readInt();
        
        // 프로필 사진 데이터 수신
        byte[] profilePicture = new byte[length];
        dis.readFully(profilePicture);
        
        // 프로필 사진 데이터 처리 (메리에 저장)
        saveProfilePictureInMemory(nickname, profilePicture);
        
        // 클라이언트에게 전송 성공 메시지
        dos.writeUTF(PROFILE_PICTURE + "//OKAY");
    }

    private void handleWin() throws IOException {
        System.out.println("[Server] " + nickname + " 승리");
        
        if (db.winRecord(nickname)) {    // 전적 업이트가 성공하면 업데이트 성공을 전송
            dos.writeUTF(RECORD + "//OKAY");
        } else {    // 전적 업데이트가 실패하면 업데이트 실패를 전송
            dos.writeUTF(RECORD + "//FAIL");
        }

        for (CCUser user : myRoom.ccu) {    // myRoom의 인원수만큼 반복
            if (!user.nickname.equals(nickname)) {    // 방 접속 인원 중 클라이언트 다른 닉네임의 클라이언트에게만 전송
                user.dos.writeUTF(LOSE + "//");
                
                if (db.loseRecord(user.nickname)) {    // 전적 업데이트가 성공하면 업데이트 성공을 전송
                    user.dos.writeUTF(RECORD + "//OKAY");
                } else {    // 전적 데이트가 실패하면 업데이트 실패를 전송
                    user.dos.writeUTF(RECORD + "//FAIL");
                }
            }
        }
    }

    private void handleLose() throws IOException {
        if (myRoom == null || myRoom.count < 2) {
            dos.writeUTF(RECORD + "//NO");
            return;
        }

        // 기권자 처리
        dos.writeUTF(LOSE + "//");
        if (db.loseRecord(nickname)) {
            dos.writeUTF(RECORD + "//OKAY");
        } else {
            dos.writeUTF(RECORD + "//FAIL");
        }

        // 승리자 처리
        for (CCUser user : myRoom.ccu) {
            if (!user.nickname.equals(nickname)) {
                user.dos.writeUTF(WIN + "//");
                if (db.winRecord(user.nickname)) {
                    user.dos.writeUTF(RECORD + "//OKAY");
                } else {
                    user.dos.writeUTF(RECORD + "//FAIL");
                }
            }
        }

        // 게임 상태 초기화
        myRoom.resetGame();
        
        // 모든 플레이어에게 게임 종료 림
        for (CCUser user : myRoom.ccu) {
            user.dos.writeUTF(READY_STATUS + "//GAME_END");
        }
    }

    private void handleInvite(String[] m) throws IOException {
        String targetUser = m[1];
        for (CCUser user : auser) {
            if (user.nickname.equals(targetUser)) {
                user.dos.writeUTF(INVITE + "//" + this.nickname);
                break;
            }
        }
    }

    private void handleInviteAccept(String[] m) throws IOException {
        String inviter = m[1];
        for (CCUser user : auser) {
            if (user.nickname.equals(inviter)) {
                // 대한 사람에게 수락 메시지 전송
                user.dos.writeUTF(INVITE_ACCEPT + "//" + this.nickname);
                // 1대1 채팅방 열림
                createPrivateRoom(user, this);
                break;
            }
        }
    }

    private void handleInviteReject(String[] m) throws IOException {
        String inviter = m[1];
        for (CCUser user : auser) {
            if (user.nickname.equals(inviter)) {
                user.dos.writeUTF(INVITE_REJECT + "//" + this.nickname);
                break;
            }
        }
    }

    private void handlePrivateChat(String[] m) {
        try {
            String receiver = m[1];
            String message = m[2];
            
            // 메시지를 데이터베이스에 저장
            boolean saved = db.saveChatMessage(nickname, receiver, message, "PRIVATE");
            if (!saved) {
                System.out.println("[Server] 채팅 메시지 저장 실패: " + nickname + " -> " + receiver);
            }

            // 수신자에게 메시지 전송
            for (CCUser user : auser) {
                if (user.nickname.equals(receiver)) {
                    user.dos.writeUTF(PRIVATE_CHAT + "//" + nickname + "//" + message);
                    break;
                }
            }
            
            // 발신자에게 성공 응답
            dos.writeUTF(PRIVATE_CHAT + "//SUCCESS");
            
        } catch (IOException e) {
            System.out.println("[Server] 개인 메시지 전송 실패: " + e.getMessage());
        }
    }
    
    private void createPrivateRoom(CCUser user1, CCUser user2) throws IOException {
        user1.dos.writeUTF(PRIVATE_ROOM_CREATED + "//" + user2.nickname);
        user2.dos.writeUTF(PRIVATE_ROOM_CREATED + "//" + user1.nickname);
    }

    private void sendProfileImageToClient(String nickname, byte[] profileImageData) {
        try {
            for (CCUser client : this.auser) {
                if (client.nickname.equals(nickname)) {
                    client.dos.writeUTF(PROFILE_PICTURE);
                    client.dos.writeInt(profileImageData.length);
                    client.dos.write(profileImageData);
                    client.dos.flush();
                    
                    System.out.println("[Server] PROFILE_PICTURE 태그와 함께 " + nickname + "에게 프로필 이미지 전송 완료");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 프로필 이미지 전송 오류: " + e.getMessage());
        }
    }

    private void sendProfileImageToJoiner(String joinerNickname, byte[] hostProfileImageData) {
        try {
            for (CCUser client : this.auser) {
                if (client.nickname.equals(joinerNickname)) {
                    client.dos.writeUTF(JOINER_PROFILE_TAG);
                    client.dos.writeInt(hostProfileImageData.length);
                    client.dos.write(hostProfileImageData);
                    client.dos.flush();  // 반드시 flush 호출
                    
                    System.out.println("[Server] JOINER_PROFILE_TAG 전송 완료: " + joinerNickname);
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 입장자 프로필 이지 전송 류: " + e.getMessage());
        }
    }

    /* 프로필 사진을 메모리에 저장하 메서드 */
    private void saveProfilePictureInMemory(String nickname, byte[] profilePicture) {
        Server.profilePictures.put(nickname, profilePicture);
        System.out.println("[Server] " + nickname + "의 프로필 사진 메모리에 저장 완료");
    }

    /* 프로필 진 메모리에서 가져는 메서드 */
    public byte[] getProfilePictureFromMemory(String nickname) {
        return Server.profilePictures.get(nickname);
    }

    /* 현재 존재하는 방의 목록을 조회 */
    String roomInfo() {
        StringBuilder msg = new StringBuilder(VROOM + "//");
        
        for (Room r : room) {
            msg.append(r.title).append(" : ").append(r.count).append("@");
        }
        return msg.toString();
    }

    /* 클라이언트가 입장한 방의 인원을 조회 */
    String roomUser() {
        StringBuilder msg = new StringBuilder(UROOM + "//");
        
        for (CCUser user : myRoom.ccu) {
            msg.append(user.nickname).append("@");
        }
        return msg.toString();
    }

    /* 접속한 모든 회원 목록을 조회 */
    String connectedUser() {
        StringBuilder msg = new StringBuilder(CUSER + "//");
        
        for (CCUser user : auser) {
            msg.append(user.nickname).append("@");
        }
        return msg.toString();
    }

    /* 대기실에 있는 모든 회원에게 메시지 전송 */
    void sendWait(String m) {
        for (CCUser user : wuser) {
            try {
                user.dos.writeUTF(m);
            } catch (IOException e) {
                wuser.remove(user);
            }
        }
    }

    /* 방에 입장 모 회원에게 메시지 전송 */
    void sendRoom(String m) {
        for (CCUser user : myRoom.ccu) {
            try {
                user.dos.writeUTF(m);
            } catch (IOException e) {
                myRoom.ccu.remove(user);
            }
        }
    }

    private void handleLoadChatHistory(String[] m) {
        try {
            String otherUser = m[1];
            int page = Integer.parseInt(m[2]);
            
            ResultSet rs = db.loadChatMessages(nickname, otherUser, page, PAGE_SIZE);
            StringBuilder history = new StringBuilder();
            
            while (rs.next()) {
                String sender = rs.getString("sender");
                String message = rs.getString("message_text");
                Timestamp time = rs.getTimestamp("timestamp");
                
                System.out.println("[Server] 메시지 조회: " + sender + " -> " + message + " @ " + time.getTime());
                
                if (history.length() > 0) {
                    history.append("||");  // 메시지 구분자 추가
                }
                
                // 각 메시지의 형식을 수정
                history.append(sender)
                      .append("//")
                      .append(message)
                      .append("//")
                      .append(time.getTime());
            }
            
            // 전송 전 로그
            System.out.println("[Server] 전송할 채팅 내역: " + history.toString());
            
            // 채팅 내역이 있을 때만 전송
            if (history.length() > 0) {
                dos.writeUTF("LOAD_CHAT//" + otherUser + "//" + history.toString());
                System.out.println("[Server] 채팅 내역 전송 완료");
            } else {
                System.out.println("[Server] 채팅 내역 없음");
                dos.writeUTF("LOAD_CHAT//" + otherUser + "//EMPTY");
            }
            
        } catch (SQLException | IOException e) {
            System.out.println("[Server] 채팅 내역 로드 실패 > " + e.toString());
        }
    }

    private void handleChatExit(String[] msgArr) {
        String otherUser = msgArr[1];
        String exitingUser = msgArr[2];
        
        // 상대방에게 나가 메시지 전송
        for (CCUser user : server.getAllUsers()) {
            if (user.nickname.equals(otherUser)) {
                try {
                    user.dos.writeUTF("CHAT_PARTNER_EXIT//" + exitingUser);
                    System.out.println("[Server] 채팅방 나가기 알림 전송: " + exitingUser + " -> " + otherUser);
                } catch (IOException e) {
                    System.out.println("[Server] 채팅 나가기 알림 전송 실패: " + e.getMessage());
                }
                break;
            }
        }
    }

    private void handleEmoticonMessage(String receiver, String emoticonName) {
        try {
            // 수신자 찾기
            for (CCUser user : auser) {
                if (user.nickname.equals(receiver)) {
                    // 이모티콘 메시지 전송
                    user.dos.writeUTF(EMOTICON + "//" + this.nickname + "//" + emoticonName);
                    user.dos.flush();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 이모콘 전송 실패: " + e.getMessage());
        }
    }

    private void handleFindId(String[] m) throws IOException {
        String name = m[1];
        String email = m[2];
        String birthDate = m[3];
        
        String foundId = db.findId(name, email, birthDate);
        if (foundId != null) {
            dos.writeUTF(FIND_ID + "//SUCCESS//" + foundId);
        } else {
            dos.writeUTF(FIND_ID + "//FAIL");
        }
    }

    private void handleFindPw(String[] m) throws IOException {
        String id = m[1];
        String email = m[2];
        String birthDate = m[3];

        if (db.sendTempPasswordAndUpdate(id, email, birthDate)) {
            dos.writeUTF(FIND_PW + "//SUCCESS");
        } else {
            dos.writeUTF(FIND_PW + "//FAIL");
        }
    }

    private void handleVerifyUser(String[] m) throws IOException {
        String id = m[1];
        String email = m[2];
        String birthDate = m[3];
        
        if (db.verifyUser(id, email, birthDate)) {
            dos.writeUTF(VERIFY_USER + "//SUCCESS");
        } else {
            dos.writeUTF(VERIFY_USER + "//FAIL");
        }
    }

    private void handleResetPassword(String[] m) throws IOException {
        String userId = m[1];  // 사용자 ID (nickname)
        String newPassword = m[2];
        
        // 데이터베이스에 사용자 ID로 실제 아이디(로그인 ID) 조회
        String loginId = db.getLoginIdByNickname(userId);
        if (loginId != null) {
            if (db.resetPassword(loginId, newPassword, false)) {
                dos.writeUTF(RESET_PW + "//SUCCESS");
                System.out.println("[Server] 비밀번호 변경 성공: " + userId);
            } else {
                dos.writeUTF(RESET_PW + "//FAIL");
                System.out.println("[Server] 비밀번호 변경 실패: " + userId);
            }
        } else {
            dos.writeUTF(RESET_PW + "//FAIL");
            System.out.println("[Server] 사용자 ID 조회 실패: " + userId);
        }
    }

    private void handleReplayRequest() {
        try {
            if (myRoom != null) {
                List<String> replayData = myRoom.getMoveHistory();
                if (replayData.isEmpty()) {
                    System.out.println("[Server] 복기 데이터가 없습니다.");
                    dos.writeUTF(REPLAY_DATA + "//NO_DATA");
                } else {
                    System.out.println("[Server] 복기 데이터 전송: " + replayData);
                    dos.writeUTF(REPLAY_DATA + "//" + replayData);
                }
            } else {
                System.out.println("[Server] 복기 요청 처리 실패: 방이 없음");
                dos.writeUTF(REPLAY_DATA + "//NO_ROOM");
            }
        } catch (IOException e) {
            System.out.println("[Server] 복기 데이터 전송 실패: " + e.getMessage());
        }
    }

    private void handleReady(String[] m) throws IOException {
        if (myRoom != null && m.length >= 2) {  // 배열 길이 체크 추가
            try {
                boolean isReady = Boolean.parseBoolean(m[1]);
                myRoom.setReady(this, isReady);
                System.out.println("[Server] " + nickname + "님의 준비상태: " + isReady);
                
                // 모든 플레이어가 준비되었는지 확인하고 방장에게 알림
                if (myRoom.isAllReady()) {
                    CCUser host = myRoom.ccu.get(0);
                    host.dos.writeUTF(READY_STATUS + "//ALL_READY");
                }
            } catch (Exception e) {
                System.out.println("[Server] 준비 상태 처리 오류: " + e.getMessage());
            }
        }
    }

    private void handleStart() throws IOException {
        if (myRoom != null && myRoom.ccu.get(0) == this) { // 방장 확인
            if (myRoom.ccu.size() < 2) {
                dos.writeUTF(START + "//FAIL//인원부족");
                return;
            }
            
            if (!myRoom.isAllReady()) {
                dos.writeUTF(START + "//FAIL//준비안됨");
                return;
            }

            // 모든 플레이어에게 게임 시작 메시 전송
            for (CCUser user : myRoom.ccu) {
                user.dos.writeUTF(START + "//SUCCESS");
                user.myRoom.setGameInProgress(true);
            }
            
            // 준비 상태 초기화
            myRoom.clearReadyStatus();
            System.out.println("[Server] 게임 시작: " + myRoom.title);
        }
    }
    private void handleSpectateRequest(String roomTitle) throws IOException {
        Room targetRoom = null;
        for (Room r : room) {
            if (r.title.equals(roomTitle)) {
                targetRoom = r;
                break;
            }
        }

        if (targetRoom == null) {
            dos.writeUTF(SPECTATE_RESPONSE + "//FAIL//존재하지 않는 방입니다.");
            return;
        }

        // 관전자 추가
        targetRoom.addSpectator(this);
        
        // 현재 게임 상태와 수 기록을 함께 전송
        String currentState = targetRoom.getCurrentGameState();
        String moveHistory = targetRoom.getMoveHistoryAsString();
        
        dos.writeUTF(SPECTATE_RESPONSE + "//SUCCESS//" + currentState + "//" + moveHistory);
        System.out.println("[Server] 관전자 추가 및 게임 정보 전송 완료: " + nickname);
    }

    private void handlePlacePiece(String[] m) throws IOException {
        if (myRoom != null && m.length >= 3) {
            int x = Integer.parseInt(m[1]);
            int y = Integer.parseInt(m[2]);
            int color = (nickname.equals(myRoom.ccu.get(0).nickname)) ? 1 : 2;
            
            myRoom.placePiece(x, y, color);
        }
    }

    private void handleFileTransfer(String[] message) {
        try {
            String receiver = message[1];
            String fileName = message[2];
            String chunkIndex = message[3];
            String totalChunks = message[4];
            String fileData = message[5];
            // 수신자에게 파일 데이터 전송
            for (CCUser user : auser) {
                if (user.nickname.equals(receiver)) {
                    user.dos.writeUTF(FILE_TRANSFER + "//" + 
                        nickname + "//" + 
                        fileName + "//" +
                        chunkIndex + "//" +
                        totalChunks + "//" +
                        fileData);
                    break;
                }
            }
            
        } catch (IOException e) {
            System.out.println("[Server] 파일 전송 실패: " + e.getMessage());
        }
    }

    private void handleFileTransferStart(String[] m) throws IOException {
        String receiver = m[1];
        String fileName = m[2];
        String totalChunks = m[3];
        
        // 수신자에게 파일 전송 시작 알림
        for (CCUser user : auser) {
            if (user.nickname.equals(receiver)) {
                user.dos.writeUTF(FILE_TRANSFER_START + "//" + nickname + "//" + 
                                fileName + "//" + totalChunks);
                break;
            }
        }
    }

    private void handleFileTransferData(String[] m) throws IOException {
        String receiver = m[1];
        String fileName = m[2];
        String chunkIndex = m[3];
        String totalChunks = m[4];
        String base64Data = m[5];
        
        // 수신자에게 파일 데이터 전송
        for (CCUser user : auser) {
            if (user.nickname.equals(receiver)) {
                user.dos.writeUTF(FILE_TRANSFER_DATA + "//" + nickname + "//" + 
                                fileName + "//" + chunkIndex + "//" + 
                                totalChunks + "//" + base64Data);
                break;
            }
        }
    }

    private void handleFileTransferEnd(String[] m) throws IOException {
        String receiver = m[1];
        String fileName = m[2];
        
        // 수신자에게 파일 전송 완료 알림
        for (CCUser user : auser) {
            if (user.nickname.equals(receiver)) {
                user.dos.writeUTF(FILE_TRANSFER_END + "//" + nickname + "//" + fileName);
                break;
            }
        }
    }
}

