package core;

import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.awt.Window;
import java.io.*;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import DB.Database;
import ui.*;
import ui.GameFrame.GameFrame;

import static core.MessageType.*;  // 상수를 static import

// 서버와의 연결과 각 인터페이스를 관리하는 클래스.
public class Client {
    Socket mySocket = null;

    /* 메시지 송신을 위한 필드 */
    OutputStream os = null;
    public DataOutputStream dos = null;

    /* 각 프레임을 관리할 필드 */
    public MainFrame mf = null;
    public LoginFrame lf = null;
    public JoinFrame jf = null;
    public RankingFrame rf = null;
    public InfoFrame inf = null;
    public CInfoFrame cinf = null;
    public GameFrame gf = null;
    public SRankFrame srf = null;
    public SpectatorFrame spectatorFrame;  // 필드 추가

	public Object nickname;

    private Map<String, PrivateChatRoom> chatRooms = new HashMap<>();
    private Map<String, GroupChatRoom> groupChatRooms = new HashMap<>();

	public Object database;

	public Database db;

    public ThemeManager.Theme currentTheme; // 현재 테마 저장

    private Room currentRoom;  // 현재 방을 저장할 변수 추가
    
    // 현재 방 설정 메서드
    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }
    
    // 현재 방 반환 메서드
    public Room getCurrentRoom() {
        return this.currentRoom;
    }

    public static void main(String[] args) {
        // 인코딩 설정
        System.setProperty("file.encoding", "UTF-8");
        try {
            java.lang.reflect.Field charset = java.nio.charset.Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Client client = new Client();

        try {
            // 서버에 연결
            client.mySocket = new Socket("localhost", 1228);
            System.out.println(new String("[Client] 서버 연결 성공".getBytes("UTF-8"), "UTF-8"));

            client.os = client.mySocket.getOutputStream();
            client.dos = new DataOutputStream(client.os);

            /* 프레임 생성 */
            client.mf = new MainFrame(client);
            client.lf = new LoginFrame(client);
            client.jf = new JoinFrame(client);
            client.rf = new RankingFrame(client);
            client.inf = new InfoFrame(client);
            client.cinf = new CInfoFrame(client);
            client.gf = new GameFrame(client);
            client.srf = new SRankFrame(client);

            // 인 프레임을 보이게 한다.
            client.lf.setVisible(true);

            MessageListener msgListener = new MessageListener(client, client.mySocket);
            msgListener.start(); // 스레드 시작
        } catch (Exception e) {
            try {
                System.out.println(new String(("[Client] 오류 발생: " + e.toString()).getBytes("UTF-8"), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
    }

    // System.out.println() 사용하는 모든 부분 수정
    private void printMessage(String message) {
        LogManager.getInstance().log("Client", message);
    }

    // 현재 방의 사용자 수를 반환하는 메서드 추가
    public int getUserCount() {
        if (gf != null && gf.userList != null) {
            return gf.userList.getModel().getSize();
        }
        return 0;
    }

    // 방 생성자인지 확인하는 서드 추가
    public boolean isRoomOwner() {
        if (gf != null) {
            System.out.println("[Client] 돌 색상: " + gf.dc);  // 디버깅용
            return gf.dc.equals("BLACK");  // BLACK이면 방장
        }
        return false;
    }

    /* 서버에 메시지 전송 */
    public void sendMsg(String _m) {
        try {
            dos.writeUTF(new String(_m.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            printMessage("[Client] 메시지 전송 오류 > " + e.toString());
        }
    }

    public void sendLobbyChatMessage(String message) {
        try {
            dos.writeUTF("LOBBYCHAT//" + message);
        } catch (IOException e) {
            System.out.println("[Client] 로비 채팅 메시지 전송 오류: " + e.getMessage());
        }
    }

    public void sendRoomChatMessage(String message) {
        try {
            dos.writeUTF("ROOMCHAT//" + message);
        } catch (IOException e) {
            System.out.println("[Client] 게임방 채팅 메시지 전송 오류: " + e.getMessage());
        }
    }

    // 특정 사용자의 프로필 이미지를 서버에 요청하는 메서드
    public void requestProfilePicture(String targetNickname) {
        try {
            dos.writeUTF(REQUEST_PROFILE_PIC + targetNickname);
            dos.flush();
            System.out.println("[Client] " + targetNickname + "의 프로필 이미지 요청을 서버로 송했습니다.");
        } catch (IOException e) {
            System.out.println("[Client] 프로필 이미지 요청 실패: " + e.getMessage());
        }
    }

    // 프로필 사진 전송 메서드
    public void sendProfilePicture(byte[] profilePicture) {
        try {
            dos.writeUTF(PROFILE_PICTURE);
            dos.writeInt(profilePicture.length);
            dos.write(profilePicture);
            dos.flush();
            System.out.println("[Client] 프로필 사진 전송 성공");
        } catch (IOException e) {
            System.out.println("[Client] 프로필 사진 전송 실패: " + e.getMessage());
        }
    }

    public void receiveProfileImages(byte[] hostImage, byte[] opponentImage) {
        if (isRoomOwner()) {
            // 방 생성자인 경우
            gf.updateHostProfileImage(hostImage);
            gf.updateOpponentProfileImage(opponentImage);
        } else {
            // 방 입장자인 경우
            gf.updateHostProfileImage(opponentImage);
            gf.updateOpponentProfileImage(hostImage);
        }
    }

    public void processMessage(String msg) {
        String[] m = msg.split("//");
        switch (m[0]) {
            case INVITE:
                handleChatInvite(m[1]); // 채팅 요청 다이얼로그 표시
                break;
            case INVITE_ACCEPT:
                openPrivateChatRoom(m[1]); // 채팅방 열기
                break;
            case INVITE_REJECT:
                showRejectionMessage(m[1]); // 거절 메시지 표시
                break;
            case PRIVATE_ROOM_CREATED:
                openPrivateChatRoom(m[1]); // 채팅방 열기
                break;
        }
    }

    private void handleChatInvite(String inviter) {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(
                null,
                inviter + "님이 1:1 채팅을 요청하였습니다.\n수락하시겠습니까?",
                "채팅 요청",
                JOptionPane.YES_NO_OPTION
            );
            
            if (response == JOptionPane.YES_OPTION) {
                sendMsg(INVITE_ACCEPT + "//" + inviter);
            } else {
                sendMsg(INVITE_REJECT + "//" + inviter);
            }
        });
    }

    public void openPrivateChatRoom(String otherUser) {
        SwingUtilities.invokeLater(() -> {
            PrivateChatRoom chatRoom = findOrCreateChatRoom(otherUser);
            chatRoom.setVisible(true);
        });
    }

    private void showRejectionMessage(String rejector) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                rejector + "님이 채팅 요청을 거절하였습니다.",
                "채팅 요청 거절",
                JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    PrivateChatRoom findOrCreateChatRoom(String otherUser) {
        return chatRooms.computeIfAbsent(otherUser, k -> new PrivateChatRoom(this, k));
    }

    public void removeChatRoom(String otherUser) {
        chatRooms.remove(otherUser);
    }

    public void openChatHistory(String otherUser) {
        SwingUtilities.invokeLater(() -> {
            ChatHistoryDialog historyDialog = new ChatHistoryDialog(
                (JFrame) SwingUtilities.getWindowAncestor(
                    findOrCreateChatRoom(otherUser)
                ),
                this,
                otherUser
            );
            historyDialog.setVisible(true);
        });
    }

    public void sendChatHistoryRequest(String otherUser, int page) {
        try {
            dos.writeUTF(LOAD_CHAT + "//" + otherUser + "//" + page);
            System.out.println("[Client] 채팅 내역 요청: " + otherUser + ", 페이지: " + page);
        } catch (IOException e) {
            System.out.println("[Client] 채팅 역 요청 실패: " + e.getMessage());
        }
    }

    public void createGroupChat(String roomName) {
        try {
            dos.writeUTF(CREATE_GROUP_CHAT + "//" + roomName);
        } catch (IOException e) {
            System.out.println("[Client] 그룹 채팅방 생성 오류: " + e.getMessage());
        }
    }

    public void joinGroupChat(String roomName) {
        try {
            dos.writeUTF(JOIN_GROUP_CHAT + "//" + roomName);
            System.out.println("[Client] 그룹 채팅방 입장 요청 전송: " + roomName);
        } catch (IOException e) {
            System.out.println("[Client] 그룹 채팅방 입장 요청 실패: " + e.getMessage());
        }
    }

    public void sendGroupChatMessage(String roomName, String message) {
        try {
            dos.writeUTF(GROUP_CHAT_MESSAGE + "//" + roomName + "//" + message);
        } catch (IOException e) {
            System.out.println("[Client] 그룹 채팅 메시지 전송 오류: " + e.getMessage());
        }
    }

    public void leaveGroupChat(String roomName) {
        try {
            // 서버에 방 나가기 메시지 전송
            dos.writeUTF(LEAVE_GROUP_CHAT + "//" + roomName);
            System.out.println("[Client] 그룹 채팅방 나기 요청: " + roomName);
            
            // 채팅방 창 닫기
            GroupChatRoom chatRoom = findGroupChatRoom(roomName);
            if (chatRoom != null) {
                chatRoom.dispose();
                groupChatRooms.remove(roomName);
            }
        } catch (IOException e) {
            System.out.println("[Client] 방 나가기 요청 실패: " + e.getMessage());
        }
    }

    public GroupChatRoom findGroupChatRoom(String roomName) {
        return groupChatRooms.get(roomName);
    }

    public void createGroupChatRoom(String roomName) {
        SwingUtilities.invokeLater(() -> {
            if (!groupChatRooms.containsKey(roomName)) {
                GroupChatRoom chatRoom = new GroupChatRoom(this, roomName);
                groupChatRooms.put(roomName, chatRoom);
                chatRoom.setVisible(true);
                System.out.println("[Client] 그룹 채팅방 생성됨: " + roomName);
            } else {
                groupChatRooms.get(roomName).setVisible(true);
                System.out.println("[Client] 기존 그룹 채팅방 열기: " + roomName);
            }
        });
    }

    public void removeGroupChatRoom(String roomName) {
        groupChatRooms.remove(roomName);
    }

    public void sendGroupChatInvite(String roomName, String invitee) {
        try {
            dos.writeUTF(GROUP_CHAT_INVITE + "//" + roomName + "//" + invitee);
            System.out.println("[Client] 그룹 초대 전송: " + invitee + " -> " + roomName);
        } catch (IOException e) {
            System.out.println("[Client] 그룹 초대 전송 실패: " + e.getMessage());
        }
    }

    public void handleGroupChatInvite(String roomName, String inviter) {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(
                mf,
                inviter + "님이 '" + roomName + "' 채팅방에 초대하였습니다.\n참여하시겠습니까?",
                "그룹 채팅 초대",
                JOptionPane.YES_NO_OPTION
            );
            
            try {
                if (response == JOptionPane.YES_OPTION) {
                    dos.writeUTF(GROUP_CHAT_INVITE_RESPONSE + "//" + roomName + "//" + nickname + "//ACCEPT");
                    // 수락 시 채팅 입장
                    joinGroupChat(roomName);
                } else {
                    dos.writeUTF(GROUP_CHAT_INVITE_RESPONSE + "//" + roomName + "//" + nickname + "//REJECT");
                }
            } catch (IOException e) {
                System.out.println("[Client] 초대 응답 전송 실패: " + e.getMessage());
            }
        });
    }

    public void requestGroupChatUserList(String roomName) {
        try {
            dos.writeUTF(GROUP_CHAT_USERS + "//REQUEST//" + roomName);
            System.out.println("[Client] 그룹 채팅방 유저 목록 요청: " + roomName);
        } catch (IOException e) {
            System.out.println("[Client] 유저 목록 요청 실패: " + e.getMessage());
        }
    }

    public void requestGroupChatList() {
        try {
            dos.writeUTF(REQUEST_GROUP_CHAT_LIST);
            System.out.println("[Client] 그룹 채팅방 목록 요청");
        } catch (IOException e) {
            System.out.println("[Client] 그룹 채팅방 목록 요청 실패: " + e.getMessage());
        }
    }

    private void handleLoginSuccess(String[] m) {
        // 기존 로그인 성공 처리 코드
        
        // 로인 성공 후 즉시 방 목록 요청
        requestGroupChatList();
    }

    public void sendPasswordChangeRequest(String newPassword) {
        try {
            dos.writeUTF(RESET_PW + "//" + nickname + "//" + newPassword);
            System.out.println("[Client] 비밀번호 변경 요청 전송");
        } catch (IOException e) {
            System.out.println("[Client] 비밀번호 변경 요청 전송 실패: " + e.getMessage());
        }
    }

    // 생성자에 인코딩 설정 추가
    public Client() {
        try {
            // UTF-8 인딩 설정
            System.setProperty("file.encoding", "UTF-8");
            Charset.defaultCharset(); // 기본 문자셋 기화
        } catch (Exception e) {
            System.out.println("인코 설정 실패: " + e.getMessage());
        }
    }

    private Map<String, UserInfoFrame> userInfoFrames = new HashMap<>();

    public void showUserInfo(String nickname) {
        UserInfoFrame frame = userInfoFrames.computeIfAbsent(nickname, k -> new UserInfoFrame());
        frame.setVisible(true);
    }

    public void updateUserInfoFrame(String nickname, byte[] profileImage, int wins, int losses) {
        UserInfoFrame frame = userInfoFrames.get(nickname);
        if (frame != null) {
            SwingUtilities.invokeLater(() -> {
                frame.updateUserInfo(nickname, profileImage, wins, losses);
            });
        }
    }

    public void updateUserInfoStats(String nickname, int wins, int losses) {
        SwingUtilities.invokeLater(() -> {
            UserInfoFrame frame = userInfoFrames.get(nickname);
            if (frame != null) {
                frame.updateUserInfo(nickname, null, wins, losses);
            }
        });
    }

    public void requestUserInfo(String nickname) {
        try {
            // USER_INFO 요청 메시지 전송
            dos.writeUTF(USER_INFO + "//" + nickname);
            System.out.println("[Client] 사용자 정보 요청: " + nickname);
        } catch (IOException e) {
            System.out.println("[Client] 사용자 정 요청 실패: " + e.getMessage());
        }
    }

    public void showUserInfo(String nickname, byte[] profileImage, int wins, int losses) {
        UserInfoFrame frame = userInfoFrames.computeIfAbsent(nickname, k -> new UserInfoFrame());
        frame.updateUserInfo(nickname, profileImage, wins, losses);
        frame.setVisible(true);
    }

    private void handleSpectateResponse(String[] message) {
        if (message[1].equals("SUCCESS")) {
            SwingUtilities.invokeLater(() -> {
                if (spectatorFrame == null) {
                    spectatorFrame = new SpectatorFrame(this);
                }
                spectatorFrame.setVisible(true);
                mf.setVisible(false);
                
                // 초기 게임 상태 설정
                if (message.length > 2) {
                    spectatorFrame.updateGameState(message[2]);
                }
                
                // 수 기록 설정
                if (message.length > 3) {
                    spectatorFrame.setMoveHistory(message[3]);
                }
            });
        } else {
            String errorMsg = message.length > 2 ? message[2] : "알 수 없는 오류";
            JOptionPane.showMessageDialog(null, 
                "관전 실패: " + errorMsg, 
                "관전 오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleGameStateUpdate(String[] m) {
        System.out.println("[Client] 게임 상태 업데이트 수신");
        if (m.length > 1 && gf != null) {
            SwingUtilities.invokeLater(() -> {
                gf.updateGameState(m[1]);
            });
        }
    }

	public Object getNickname() {
		// TODO Auto-generated method stub
		return nickname;
	}
}

// 서버의 메시지 송수신을 관리하는 클래스.
// 스레드를 속받아  기능과 독립적으로 동작할 수 있도록 한다.
class MessageListener extends Thread {
    Socket socket;
    Client client;

    /* 메시지 수신을 위한 필드 */
    InputStream is;
    DataInputStream dis;
    private SpectatorFrame spectatorFrame;  // 필드 추가
    MessageListener(Client _c, Socket _s) {
        this.client = _c;
        this.socket = _s;
    }

    public void run() {
        try {
            is = this.socket.getInputStream();
            dis = new DataInputStream(is);

            while (true) {
                // 태그 수신
                String tag = dis.readUTF();

                // 프로필 이미지 태그 처리 통합
                if (tag.equals("PROFILE_IMAGE") || tag.equals(JOINER_PROFILE_TAG)) {
                    receiveAndUpdateProfileImage(tag);
                } else {
                    receiveTextMessage(tag); // 나머지 텍스트 메시지 처리
                }
            }
        } catch (EOFException e) {
            System.out.println("[Client] 서버와의 연결이 종료되었습니다.");
        } catch (IOException e) {
            System.out.println("[Client] 메시 받기 오류 > " + e.toString());
        }
    }

    /* 텍스트 메시 처리 메서드 */
    private void receiveTextMessage(String tag) {
        try {
            String[] m = tag.split("//");
            
            /* 로그인 */
            if (m[0].equals(LOGIN)) {
            	loginCheck(m);
            }

            /* 회원가입 */
            else if (m[0].equals(JOIN)) {
                joinCheck(m[1]);
            }

            /* 중복인 */
            else if (m[0].equals(OVER)) {
                overlapCheck(m[1]);
            }

            /* 회원정보 조회 */
            else if (m[0].equals(VIEW)) {
                viewMyInfo(m[1], m[2], m[3]);
            }

            /* 전체 전적 조 */
            else if (m[0].equals(RANK)) {
                viewRank(m[1]);
            }

            /* 회원정보 변경 */
            else if (m[0].equals(CHANGE)) {
                changeInfo(m[1]);
            }

            /* 방 생성 */
            else if (m[0].equals(CREATEROOM)) {
                createRoom(m[1]);
            }

            /* 접속 유저 */
            else if (m[0].equals(CUSER)) {
                viewCUser(m[1]);
            }

            /* 방 목록 */
            else if (m[0].equals(VROOM)) {
                if (m.length > 1) { // 배열 크기가 1다 클 때
                    roomList(m[1]);
                } else { // 배열 크기가 1다 작다 == 방이 없다
                    String[] room = {""}; // 방 목이 비도록 함
                    client.mf.rList.setListData(room);
                }
            }

            /* 방 입장 */
            else if (m[0].equals(EROOM)) {
                enterRoom(m[1]);
            }

            /* 방 인원 */
            else if (m[0].equals(UROOM)) {
                roomUser(m[1]);
            }

            /* 전적 조회 */
            else if (m[0].equals(SEARCH)) {
                searchRank(m[1]);
            }

            /* 오목 */
            else if (m[0].equals(OMOK)) {
                inputOmok(m[1], m[2], m[3]);
            }

            /* 패배 */
            else if (m[0].equals(LOSE)) {
                loseGame();
            }

            /* 승리 */
            else if (m[0].equals(WIN)) {
                winGame();
            }

            /* 전적 업데이트 */
            else if (m[0].equals(RECORD)) {
                dataRecord(m[1]);
            }

            /* 로 채팅 */
            else if (m[0].equals(LOBBY_CHAT)) {
                client.mf.addChatMessage(m[1]);
            }

            /* 게임방 채팅 */
            else if (m[0].equals(ROOM_CHAT)) {
                client.gf.addChatMessage(m[1]);
            }

            else if (m[0].equals(INVITE)) {
                client.mf.showInviteDialog(m[1]);
            }
            else if (m[0].equals(INVITE_ACCEPT)) {
                JOptionPane.showMessageDialog(null, m[1] + "님이 초대를 수락했습니다.");
            }
            else if (m[0].equals(INVITE_REJECT)) {
                JOptionPane.showMessageDialog(null, m[1] + "님이 초대를 거절했습니다.");
            }
            else if (m[0].equals(PRIVATE_ROOM_CREATED)) {
                client.openPrivateChatRoom(m[1]);
            }
            else if (m[0].equals(PRIVATE_CHAT)) {
                handlePrivateChat(m[1], m[2]);
            }

            if (tag.equals(HOST_PROFILE_TAG)) {
                int length = dis.readInt();
                byte[] profileImage = new byte[length];
                dis.readFully(profileImage);
                client.gf.updateHostProfileImage(profileImage);
            }
            else if (tag.equals(JOINER_PROFILE_TAG)) {
                int length = dis.readInt();
                byte[] profileImage = new byte[length];
                dis.readFully(profileImage);
                client.gf.updateOpponentProfileImage(profileImage);
            }

            else if (m[0].equals(REXIT)) {
                if (client.gf != null) {
                    client.gf.clearProfileImages();
                }
            }

            else if (m[0].equals(CHAT_HISTORY)) {
                handleChatHistory(m[1], m[2]);
            }

            else if (m[0].equals(EMOTICON)) {
                handleEmoticon(m[1], m[2]);  // sender, emoticonName
            }
            
            /* 그룹 채팅 초대 */
            else if (m[0].equals(GROUP_CHAT_INVITE)) {
                if (m.length >= 3) {  // roomName과 inviter가 모두 있는지 확인
                    String roomName = m[1];
                    String inviter = m[2];
                    handleGroupChatInvite(roomName, inviter);
                }
            }

            /* 그룹 채팅 초대 응답 */
            else if (m[0].equals(GROUP_CHAT_INVITE_RESPONSE)) {
                handleGroupChatInviteResponse(m[1], m[2], m[3]);
            }

            /* 아이디 찾기 결과 */
            else if (m[0].equals(FIND_ID)) {
                if (m[1].equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null, 
                        "찾은 아이디: " + m[2], 
                        "아이디 찾기 성공", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "입력하신 정보와 일치하는 아이디를 찾을 수 없다.", 
                        "아이디 찾기 실패", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            
            /* 비밀번 찾기 결과 */
            else if (m[0].equals(FIND_PW)) {
                if (m[1].equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null, 
                        "임시 비밀번호가 이메일로 전송되었습니다.\n로그인 후 비밀번호를 변경해주세요.", 
                        "비밀번호 찾기", 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "입력하신 정보와 일치하는 계정을 찾을 수 없습니다.", 
                        "비밀번호 찾기 실", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            else if (m[0].equals(VERIFY_USER)) {
                if (m[1].equals("SUCCESS")) {
                    // FindPwDialog 찾기
                    for (Window window : Window.getWindows()) {
                        if (window instanceof FindPwDialog) {
                            SwingUtilities.invokeLater(() -> 
                                ((FindPwDialog) window).showResetPanel()
                            );
                            break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "입력하신 정보와 일치하는 계정을 찾 수 없습니다.", 
                        "사용자 확인 실패", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (m[0].equals(RESET_PW)) {
                if (m[1].equals("SUCCESS")) {
                    JOptionPane.showMessageDialog(null, 
                        "비밀번호가 성공적으로 재설정되었습니다.", 
                        "비밀번호 재설정 성공", 
                        JOptionPane.INFORMATION_MESSAGE);
                    // FindPwDialog 찾아서 닫기
                    for (Window window : Window.getWindows()) {
                        if (window instanceof FindPwDialog) {
                            window.dispose();
                            break;
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "비밀번호 재설정 실패했습니다.", 
                        "비번호 재설정 실패", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            // 그룹 채팅 관련 메시지 처리 추가
            if (m[0].equals(GROUP_CHAT_MESSAGE)) {
                String roomName = m[1];
                String sender = m[2];
                String message = m[3];
                // GroupChatRoom 찾아서 메시지 전달
                handleGroupChatMessage(roomName, sender, message);
            } 
            else if (m[0].equals(GROUP_CHAT_LIST)) {
                handleGroupChatList(m);
            }
            else if (m[0].equals(GROUP_CHAT_USERS)) {
                String roomName = m[1];
                String[] users = m[2].split("@");
                handleGroupChatUsers(roomName, users);
                System.out.println("[Client] 그룹 채팅 유저 목록 업데이트: " + roomName);
            }

            else if (m[0].equals(CREATE_GROUP_CHAT)) {
                if (m[1].equals("SUCCESS")) {
                    System.out.println("[Client] 그룹 채팅방 생성 성공");
                    SwingUtilities.invokeLater(() -> {
                        handleGroupChatCreated(m[2]); // m[2]는 방 이름
                    });
                } else {
                    System.out.println("[Client] 그룹 채팅방 생성 실패: " + m[2]); // m[2]는 실패 사유
                    JOptionPane.showMessageDialog(null, 
                        "채팅방 생성 실패: " + m[2], 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            if (m[0].equals(JOIN_GROUP_CHAT)) {
                if (m[1].equals("SUCCESS")) {
                    String roomName = m[2];
                    SwingUtilities.invokeLater(() -> {
                        client.createGroupChatRoom(roomName);  // client 인스턴스를 통해 메서드 호출
                        System.out.println("[Client] 그룹 채팅방 입장 성공: " + roomName);
                    });
                } else {
                    System.out.println("[Client] 그룹 채팅방 입장 실패: " + m[2]);
                    JOptionPane.showMessageDialog(null, 
                        "채팅방 입장 실패: " + m[2], 
                        "오류", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            else if (m[0].equals(REPLAY_DATA)) {
                if (m.length > 1) {
                    handleReplayData(m[1]);
                }
            }

            /* 준비 상태 업데이트 */
            else if (m[0].equals(READY_STATUS)) {
                String nickname = m[1];
                boolean ready = Boolean.parseBoolean(m[2]);
                client.gf.updateReadyStatus(nickname, ready);
            }

            /* 게임 시작 */
            else if (m[0].equals(START)) {
                if (m[1].equals("SUCCESS")) {
                    client.gf.startGame();
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "모든 플레이어가 준비되지 않았습니다.", 
                        "게임 시작 실패", 
                        JOptionPane.WARNING_MESSAGE);
                }
            }

            else if (m[0].equals(USER_INFO)) {
                String nickname = m[1];
                int wins = Integer.parseInt(m[2]);
                int losses = Integer.parseInt(m[3]);
                client.updateUserInfoStats(nickname, wins, losses);
            }

            else if (m[0].equals(USER_INFO_RESPONSE)) {
                handleUserInfoResponse();
            }

            else if (m[0].equals(SPECTATE_REQUEST)) {
                handleSpectateRequest(m);
            }

            else if (m[0].equals(SPECTATE_RESPONSE)) {
                handleSpectateResponse(m);
            }

            else if (m[0].equals(GAME_STATE_UPDATE)) {
                handleGameStateUpdate(m);
            }

            else if (m[0].equals(MOVE_UPDATE)) {
                if (client.spectatorFrame != null && m.length >= 4) {
                    try {
                        int x = Integer.parseInt(m[1]);
                        int y = Integer.parseInt(m[2]);
                        int color = Integer.parseInt(m[3]);
                        SwingUtilities.invokeLater(() -> {
                            client.spectatorFrame.addNewMove(x, y, color);
                        });
                    } catch (NumberFormatException e) {
                        System.out.println("[Client] 수 업데이트 처리 실패: " + e.getMessage());
                    }
                }
            }

            else if (m[0].equals(MOVE_HISTORY_UPDATE)) {
                if (client.spectatorFrame != null && m.length >= 2) {
                    SwingUtilities.invokeLater(() -> {
                        client.spectatorFrame.setMoveHistory(m[1]);
                    });
                }
            }
            
            
            else if (m[0].equals(FILE_TRANSFER)) {
            	handleFileReceive(m);
            }
            else if (m[0].equals(FILE_TRANSFER_START)) {
            	handleFileTransferStart(m);
            }
            else if (m[0].equals(FILE_TRANSFER_DATA)) {
            	handleFileTransferData(m);
            }
            else if (m[0].equals( FILE_TRANSFER_END)) {
            	handleFileTransferEnd(m);
            }
            
        } catch (Exception e) {
            System.out.println("[Client] 메시지 처리 오류: " + e.getMessage());
        }
    }
    
    void loginCheck(String[] m) {
        if ("OKAY".equals(m[1])) {
            if ("admin".equals(client.lf.id.getText())) {
                // 관리자 계정 로인 처리
                handleAdminLogin();
            } else {
                // 일반 사용자 로그인 처리
                handleUserLogin(m);
            }
        } else if ("DELETED".equals(m[1])) {
            // 삭제된 계정 처리
            handleDeletedAccount();
        } else {
            // 로그인 실패 처리
            handleLoginFailure();
        }
    }

    // 관리자 로그인 처리 메서드
    private void handleAdminLogin() {
        System.out.println("[Client] 관리자 로그인 성공: 관리자 창 열림");
        SwingUtilities.invokeLater(() -> {
            new AdminFrame(); // 관리자 창 열기
            client.lf.dispose(); // 로그인 창 닫기
        });
    }

    // 일반 사용자 로그인 처리 메서드
    private void handleUserLogin(String[] m) {
        client.nickname = m[2]; // 닉네임 설정

        boolean isTemporaryPassword = false;
        final int[] stats = new int[2]; // stats[0] = win, stats[1] = lose

        try {
            // 전적 정보가 있는 경우 처리
            stats[0] = Integer.parseInt(m[3]); // win
            stats[1] = Integer.parseInt(m[4]); // lose

            // 임시 비밀번호 여 확인
            if (m.length > 5 && "TEMP".equals(m[5])) {
                isTemporaryPassword = true;
            }
        } catch (NumberFormatException e) {
            // 임시 비밀번호 상태로 처리
            if ("TEMP".equals(m[3])) {
                isTemporaryPassword = true;
                stats[0] = 0; // 기본 값으로 설정 (win)
                stats[1] = 0; // 기본 값으로 설정 (lose)
            } else {
                System.out.println("[Client] 데이터 처리 오류: " + e.getMessage());
                return; // 데이터 오류 시 함수 종료
            }
        }

        boolean finalIsTemporaryPassword = isTemporaryPassword;

        SwingUtilities.invokeLater(() -> {
            client.mf.updateProfileInfo(client.nickname.toString(), stats[0], stats[1]);
            client.mf.setTitle(client.nickname + "님의 게임");
            client.mf.setVisible(true);
            client.lf.setVisible(false);
            client.lf.id.setText("");
            client.lf.pw.setText("");
            System.out.println("[Client] 일반 사용자 로그인 성공");

            if (finalIsTemporaryPassword) {
                JOptionPane.showMessageDialog(
                    null,
                    "임시 비밀번호로 로그인하셨습니다.\n비밀번호를 즉시 변경해주세요.",
                    "비밀번호 변경 안내",
                    JOptionPane.WARNING_MESSAGE
                );
                openPasswordChangeDialog();
            }
        });
    }

 // 비밀번 변경 화면을 열어주는 메서드
    private void openPasswordChangeDialog() {
        SwingUtilities.invokeLater(() -> {
            PasswordChangeDialog passwordChangeDialog = new PasswordChangeDialog(client.mf, client);
            passwordChangeDialog.setVisible(true);
        });
    }

	// 삭제된 계정 처리 메서드
    private void handleDeletedAccount() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                "삭제된 계정입니다.",
                "로그인 실패",
                JOptionPane.ERROR_MESSAGE
            );
            resetLoginFields();
        });
        System.out.println("[Client] 로그인 실패: 삭제된 계정");
    }

    // 로그인 실패 처리 메서드
    private void handleLoginFailure() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                null,
                "아이디나 비밀번호가 일치하지 않습니다.",
                "로그인 실패",
                JOptionPane.ERROR_MESSAGE
            );
            resetLoginFields();
        });
        System.out.println("[Client] 로그인 실패: 정보 불일치");
    }

    // 로그인 필드 초기화 메서드
    private void resetLoginFields() {
        client.lf.id.setText("");
        client.lf.pw.setText("");
    }

    /* 회원가입 공 여부 확인하는 메소드 */
    void joinCheck(String _m) {
        if (_m.equals("OKAY")) { // 회원가입 성공
            JOptionPane.showMessageDialog(null, "회원가입에 성공하였습니다", "회원가입 성공", JOptionPane.INFORMATION_MESSAGE);
            client.jf.dispose();
            System.out.println("[Client] 회원가입 성공 : 회원가입 인터페이스 종료");
        } else { // 회원가입 패
            JOptionPane.showMessageDialog(null, "닉네임이나 이름이 중복되었는지 확인하세요", "회원가입 실패", JOptionPane.ERROR_MESSAGE);
            System.out.println("[Client] 회원가입 실패");
            client.jf.name.setText("");
            ((JTextComponent) client.jf.nickname).setText("");
        }
    }

    /* 복 여부를 확인하는 메소드 */
    void overlapCheck(String _m) {
        if (_m.equals("OKAY")) { // 사용 가능
            System.out.println("[Client] 사용 가능");
            JOptionPane.showMessageDialog(null, "사용 가능한 닉네임/아디 입니다", "중복 확인", JOptionPane.INFORMATION_MESSAGE);
        } else { // 사용 불가능
            System.out.println("[Client] 사용 불가능");
            JOptionPane.showMessageDialog(null, "이미 존재하는 임/아이디 입니다", "중 확인", JOptionPane.ERROR_MESSAGE);
            ((JTextComponent) client.jf.nickname).setText("");
        }
    }

    /* 내 정보를 확인하는 메소드 */
    void viewMyInfo(String m1, String m2, String m3) {
        if (!m1.equals("FAIL")) { // 회원정보 조회 성공
            System.out.println("[Client] 회원 정보 조회 성공");
            client.inf.name.setText(m1);
            client.inf.nickname.setText(m2);
            client.inf.email.setText(m3);
        } else { // 회원정보 조회 실패
            System.out.println("[Client] 회원 정보 회 실패");
        }
    }

    /* 전적을 출력하는 메소드 */
    void viewRank(String _m) {
        if (!_m.equals("FAIL")) { // 전적 조회 공
            System.out.println("[Client] 전적 조회 성공");
            client.rf.tableModel.setRowCount(0); // 기존 데이터 초기화
            
            String[] users = _m.split("@");
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    try {
                        String[] data = user.split(" : ");
                        String nickname = data[0];
                        String[] stats = data[1].split(" ");
                        int wins = Integer.parseInt(stats[0].replace("승", ""));
                        int losses = Integer.parseInt(stats[1].replace("패", ""));
                        double winRate = 0.0;
                        if (wins + losses > 0) {
                            winRate = (double) wins / (wins + losses) * 100;
                        }
                        
                        client.rf.tableModel.addRow(new Object[]{
                        	client.rf.tableModel.getRowCount() + 1, // 순위
                            nickname,                        // 닉네임
                            wins,                           // 승
                            losses,                         // 패
                            String.format("%.2f%%", winRate) // 률
                        });
                    } catch (Exception e) {
                        System.out.println("[Client] 전적 데이터 파싱 오류: " + e.getMessage());
                    }
                }
            }
        } else {
            System.out.println("[Client] 전적 조회 실패");
        }
    }

    /* 회원정보 변 여부를 확인는 메소드 */
    void changeInfo(String _m) {
        if (_m.equals("OKAY")) { // 회원정보 변경 성공
            System.out.println("[Client] 이름 변경 성공");
            JOptionPane.showMessageDialog(null, "정적로 변경되었습다", "회원정보변경", JOptionPane.INFORMATION_MESSAGE);
        } else { // 회원정보 변경 실패
            System.out.println("[Client] 름 변경 실패");
            JOptionPane.showMessageDialog(null, "정상적으로 변경 실패하였습니다", "회원정보변경", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* 방 생성 여부를 인하는 메소드 */
    void createRoom(String _m) throws IOException {
        if (_m.equals("OKAY")) { // 방 생성 성공
            System.out.println("[Client] 방 생성 성공");
            client.gf.setVisible(true);
            client.mf.setVisible(false);
            client.gf.reset();  // 수정된 enterRoom 메서드 호출
            client.gf.setTitle(client.mf.roomName);
            client.gf.dc = blackTag; // 방을 생성한 사람은 검은 돌
            client.gf.enable = true; // 돌 놓기 가능하게 설정
            
            // 서버로부터 방 생성자의 프로필 이미지 수신
            String tag = dis.readUTF(); // 태그 수신
            if (tag.equals(PROFILE_PICTURE)) { // 올바른 태그인지 확인
                int hostImageLength = dis.readInt(); // 이미지 길이 수신
                byte[] hostImage = new byte[hostImageLength];
                dis.readFully(hostImage); // 이미지 데이터 수신

                // GameFrame의 프로필 미지 데이트
                if (client.gf != null) {
                    client.gf.updateHostProfileImage(hostImage);
                    System.out.println("[Client] 방 생성자 프로필 이미지 수신 후 업데이트 완료");
                }
            } else {
                System.out.println("[Client] 프로필 이미지 태그를 수신하 못함: " + tag);
            }
        } else { // 방 입장 실패
            System.out.println("[Client] 방 입장 실패");
            JOptionPane.showMessageDialog(null, "이미 2명이 찬 방이므로 입장할 수 없습니다", "방입장", JOptionPane.ERROR_MESSAGE);
        }
    }


    // 프로필 이미지 수신 및 업데이트 메서드
    private void receiveAndUpdateProfileImage(String tag) {
        try {
            int imageLength = dis.readInt();
            byte[] profileImage = new byte[imageLength];
            dis.readFully(profileImage);

            if (client.gf != null) {
                if (tag.equals(HOST_PROFILE_TAG)) {
                    client.gf.updateHostProfileImage(profileImage);
                    System.out.println("[Client]  성자 프로필 이미지 수신 후 업데이트 완료");
                } else if (tag.equals(JOINER_PROFILE_TAG)) {
                    client.gf.updateOpponentProfileImage(profileImage);
                    System.out.println("[Client] 입장자 프로필 이미지 수신 후 업데트 완료");
                }
            }
        } catch (IOException e) {
            System.out.println("[Client] 프로필 이미지 수신 실패: " + e.getMessage());
        }
    }

    /* 접속 인원을 출력하는 메소드 */
    void viewCUser(String _m) {
        if (!_m.equals("")) {
            String[] user = _m.split("@");
            client.mf.cuList.setListData(user);
        }
    }

    /* 방 목록을 출력하는 메서드 */
    void roomList(String _m) {
        if (!_m.equals("")) {
            String[] room = _m.split("@");
            client.mf.rList.setListData(room);
        }
    }

    /* 게임 시작 메소드 */
    void startGame() {
        System.out.println("[Client] 게임 시작");
        client.gf.enable = true;
        client.gf.enableL.setText("게임 시작합니다. 돌을 둘 수 있습니다.");
    }

    /* 준비 메소드 */
    void ready() {
        System.out.println("[Client] 준비 완료");
        client.gf.enableL.setText("준비 완료. 상대를 기다리는 중...");
    }

    void enterRoom(String _m) throws IOException {
        if (_m.equals("OKAY")) { // 방 입장 성공
            System.out.println("[6][Client] 방 입장 성공");
            client.gf.setVisible(true);
            client.mf.setVisible(false);
            client.gf.reset();  // 수정된 enterRoom 메서드 호출
            client.gf.setTitle(client.mf.selRoom);
            client.gf.dc = whiteTag;
            client.gf.enable = false;

            // 방 생성자의 프로필 이미지 수신
            String hostTag = dis.readUTF();
            if (hostTag.equals(HOST_PROFILE_TAG)) {
                int hostImageLength = dis.readInt();
                byte[] hostImage = new byte[hostImageLength];
                dis.readFully(hostImage);
                
                if (client.gf != null) {
                    client.gf.updateHostProfileImage(hostImage);
                    System.out.println("[7][Client] 방 생성자 프로필 이미지 수신 후 GameFrame으로송");
                }
            } else {
                System.out.println("[8][Client] 방 성자 프로필 이미지 태그를 수신하지 못함: " + hostTag);
            }

            try {
                String joinTag = dis.readUTF();
                if (joinTag.equals(JOINER_PROFILE_TAG)) {
                    int joinImageLength = dis.readInt();
                    byte[] joinImage = new byte[joinImageLength];
                    dis.readFully(joinImage);
                    client.sendMsg(JOINER_PROFILE_TAG);
                    
                    if (client.gf != null) {
                        client.gf.updateOpponentProfileImage(joinImage);
                        System.out.println("[9][Client] 입장자 프로필 ���미지 GameFrame으로 전송");
                    }
                } else {
                    System.out.println("[10][Client] 입장자 프로필 이미지 태그 수신하지 못함: " + joinTag);
                }
            } catch (Exception e) {
                System.out.println("[11][Client] 입장자 프로필 이미지 전송 실패: " + e.getMessage());
            }

        } else { // 방 입장 실패
            System.out.println("[12][Client] 방 입장 실패");
            JOptionPane.showMessageDialog(null, "이미 2명이 찬 방이므로 입장 수 없습니다", "방입장", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* 방 인원 목록을 출력하는 메소드 */
    void roomUser(String _m) {
        if (!_m.equals("")) {
            String[] user = _m.split("@");
            client.gf.userList.setListData(user);
        }
    }

    /* 전적 조회 메소 */
    void searchRank(String _m) {
        if (!_m.equals("FAIL")) { // 전적 조회 성공
            client.srf.setVisible(true);
            client.srf.l.setText(_m);
        }
    }

    /* 상대 오목을 두는 메소드 */
    void inputOmok(String m1, String m2, String m3) {
        if (!m1.equals("") && !m2.equals("") && !m3.equals("")) {
            int n1 = Integer.parseInt(m1);
            int n2 = Integer.parseInt(m2);

            if (m3.equals(blackTag)) {
                client.gf.omok[n2][n1] = 1;
            } else {
                client.gf.omok[n2][n1] = 2;
            }

            client.gf.repaint();
            client.gf.enable = true;
            client.gf.enableL.setText("당신의 차례입니다.");
        }
    }

    void winGame() {
        SwingUtilities.invokeLater(() -> {
            client.gf.winGame();
        });
    }

    void loseGame() {
        SwingUtilities.invokeLater(() -> {
            client.gf.loseGame();
        });
    }

    /* 전적 업데이트 여부를 알려는 메서드 */
    void dataRecord(String _m) {
        if (_m.equals("NO")) { // 전적 업데이트 함
            System.out.println("[Client] 데이터 미반영 : 상대가 없음");
            JOptionPane.showMessageDialog(null, "게임 상대가 없어 전적을 반영하지 않았습니다", "전적반영", JOptionPane.INFORMATION_MESSAGE);
        } else if (_m.equals("OKAY")) { // 전적 데이트 성공
            System.out.println("[Client] 데이터 반영 성공");
            JOptionPane.showMessageDialog(null, "전적 반영이 정상적으로 완료되습니", "전적반��", JOptionPane.INFORMATION_MESSAGE);
        } else if (_m.equals("FAIL")) { // 전적 업데이트 실패
            System.out.println("[Client] 데이터 반영 실");
            JOptionPane.showMessageDialog(null, "시스템 장애로 인해 전적 반영에 실패하였습니다", "전적반영", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handlePrivateChat(String sender, String message) {
        // sender: 메시지를 낸 사용자
        // message: 전달된 메시지 내용

        // Client 인스턴스의 findOrCreateChatRoom 메서드를 호출하여
        // sender와의 1대1 채팅방을 찾거나 없으면 새로 생성
        PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);

        // 성된 혹은 찾은 채팅방에 메시지를 전달하여 표시
        chatRoom.receiveMessage(message);
    }


    private void handleChatHistory(String otherUser, String historyData) {
        SwingUtilities.invokeLater(() -> {
            Window[] windows = Window.getWindows();
            for (Window window : windows) {
                if (window instanceof ChatHistoryDialog) {
                    ChatHistoryDialog dialog = (ChatHistoryDialog) window;
                    if (dialog.getOwner().equals(otherUser)) {
                        dialog.displayChatHistory(historyData);
                        break;
                    }
                }
            }
        });
    }

    private void handleEmoticon(String sender, String emoticonName) {
        PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);
        chatRoom.appendEmoticon(emoticonName, false);
    }

    // 그룹 채팅 메시지 처리 메서드 추가
    private void handleGroupChatMessage(String roomName, String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            GroupChatRoom chatRoom = client.findGroupChatRoom(roomName);
            if (chatRoom != null) {
                chatRoom.receiveMessage(sender, message);
            }
        });
    }

    private void handleGroupChatList(String[] m) {
        System.out.println("[Client] 방 목록 메시지 수신: " + String.join("//", m));  // 디버깅용
        
        SwingUtilities.invokeLater(() -> {
            if (client.mf != null) {
                if (m.length > 1 && !m[1].trim().isEmpty()) {
                    String[] rooms = m[1].split("@");
                    System.out.println("[Client] 처리할 방 목록: " + String.join(", ", rooms));  // 디버깅용
                    client.mf.updateGroupChatList(rooms);
                } else {
                    System.out.println("[Client] 빈 방 목록 수신");
                    client.mf.updateGroupChatList(new String[0]);
                }
            }
        });
    }

    private void handleGroupChatUsers(String roomName, String[] users) {
        SwingUtilities.invokeLater(() -> {
            GroupChatRoom chatRoom = client.findGroupChatRoom(roomName);
            if (chatRoom != null) {
                chatRoom.updateUserList(users);
            }
        });
    }

    private void handleGroupChatCreated(String roomName) {
        SwingUtilities.invokeLater(() -> {
            client.createGroupChatRoom(roomName);
            System.out.println("[Client] 그룹 채팅방 생성됨: " + roomName);
        });
    }
    
    private void handleGroupChatInvite(String roomName, String inviter) {
        SwingUtilities.invokeLater(() -> {
            client.handleGroupChatInvite(roomName, inviter);
        });
    }

    private void handleGroupChatInviteResponse(String roomName, String invitee, String response) {
        SwingUtilities.invokeLater(() -> {
            if (response.equals("ACCEPT")) {
                System.out.println("[Client] " + invitee + "님이 " + roomName + " 초대를 수락했습니다.");
                // 초대 수락 시 채팅방 생성 및 입장
                client.createGroupChatRoom(roomName);
                // 유 목록 업데이트 청
                client.requestGroupChatUserList(roomName);
            } else {
                System.out.println("[Client] " + invitee + "님이 " + roomName + " 초대를 거절했습니다.");
                JOptionPane.showMessageDialog(
                    client.mf,
                    invitee + "님이 초대를 거절했습니다.",
                    "초대 거절",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        });
    }

    private void handleReplayData(String replayData) {
        System.out.println("[Client] 복기 데이터 수신: " + replayData);
        
        if (replayData.equals("NO_DATA")) {
            JOptionPane.showMessageDialog(null, 
                "복기할 수 있는 데이터가 없습니다.", 
                "복기 불가", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (replayData.equals("NO_ROOM")) {
            JOptionPane.showMessageDialog(null, 
                "현재 방이 재하지 않습니다.", 
                "복기 불가", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            ReplayFrame replayFrame = new ReplayFrame(replayData);
            replayFrame.setVisible(true);
        });
    }

    private void handleUserInfoResponse() {
        try {
            String nickname = dis.readUTF();
            int wins = dis.readInt();
            int losses = dis.readInt();
            int imageLength = dis.readInt();
            byte[] profileImage = null;
            
            if (imageLength > 0) {
                profileImage = new byte[imageLength];
                dis.readFully(profileImage);
            }
            
            System.out.println("[Client] 사용자 정보 수신: " + nickname);
            client.showUserInfo(nickname, profileImage, wins, losses);
        } catch (IOException e) {
            System.out.println("[Client] 사용자 정보 처리 실패: " + e.getMessage());
        }
    }

    private void handleSpectateRequest(String[] m) {
        System.out.println("[Client] 관전 요청 처리");
        try {
            client.dos.writeUTF(SPECTATE_REQUEST + "//" + m[1]);
        } catch (IOException e) {
            System.out.println("[Client] 관전 요청 전송 실패: " + e.getMessage());
        }
    }

    private void handleGameStateUpdate(String[] m) {
        System.out.println("[Client] 게임 상태 업데이트 수신");
        if (m.length > 1 && client.gf != null) {
            SwingUtilities.invokeLater(() -> {
                client.gf.updateGameState(m[1]);
            });
        }
    }

    // MessageListener 클래스 내부에 추가
    private void handleSpectateResponse(String[] message) {
        if (message[1].equals("SUCCESS")) {
            SwingUtilities.invokeLater(() -> {
                if (client.spectatorFrame == null) {
                    client.spectatorFrame = new SpectatorFrame(client);
                }
                client.spectatorFrame.setVisible(true);
                client.mf.setVisible(false);
                
                // 초기 게임 상태 설정
                if (message.length > 2) {
                    client.spectatorFrame.updateGameState(message[2]);
                }
                
                // 수 기록 설정
                if (message.length > 3) {
                    client.spectatorFrame.setMoveHistory(message[3]);
                }
            });
        } else {
            String errorMsg = message.length > 2 ? message[2] : "알 수 없는 오류";
            JOptionPane.showMessageDialog(null, 
                "관전 실패: " + errorMsg, 
                "관전 오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // 파일 전송 처리 메서드
    private void handleFileReceive(String[] message) {
        try {
            String sender = message[1];
            String fileName = message[2];
            String chunkIndex = message[3];
            String totalChunks = message[4];
            String fileData = message[5];

            // client 인스턴스를 통해 메서드 호출
            PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);
            System.out.println("[Client] 파일 청크 수신 - " + fileName + 
                             " (" + chunkIndex + "/" + totalChunks + ")");
            
            chatRoom.receiveFileChunk(sender, fileName, fileData, 
                                    Integer.parseInt(chunkIndex), 
                                    Integer.parseInt(totalChunks));
                                    
        } catch (Exception e) {
            System.out.println("[Client] 파일 수신 처리 오류: " + e.getMessage());
        }
    }

    // 파일 전송 시작 처리
    private void handleFileTransferStart(String[] message) {
        try {
            String sender = message[1];
            String fileName = message[2];
            String totalChunks = message[3];
            
            // client 인스턴스를 통해 메서드 호출
            PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);
            chatRoom.appendMessage(sender + "님이 파일을 전송합니다: " + fileName, false);
            
        } catch (Exception e) {
            System.out.println("[Client] 파일 전송 시작 처리 오류: " + e.getMessage());
        }
    }

    // 2. 파일 데이터 수신 처리
    private void handleFileTransferData(String[] message) {
        try {
            String sender = message[1];
            String fileName = message[2];
            String chunkIndex = message[3];
            String totalChunks = message[4];
            String fileData = message[5];
            
            // client 인스턴스를 통해 메서드 호출
            PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);
            chatRoom.receiveFileChunk(sender, fileName, fileData, 
                                    Integer.parseInt(chunkIndex), 
                                    Integer.parseInt(totalChunks));
                                    
        } catch (Exception e) {
            System.out.println("[Client] 파일 데이터 처리 오류: " + e.getMessage());
        }
    }

    // 3. 파일 전송 완료 처리
    private void handleFileTransferEnd(String[] message) {
        try {
            String sender = message[1];
            String fileName = message[2];
            
            // client 인스턴스를 통해 메서드 호출
            PrivateChatRoom chatRoom = client.findOrCreateChatRoom(sender);
            chatRoom.showFileOpenDialog(fileName);
            
        } catch (Exception e) {
            System.out.println("[Client] 파일 전송 완료 처리 오류: " + e.getMessage());
        }
    }
}



