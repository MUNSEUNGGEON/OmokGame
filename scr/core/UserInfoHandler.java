package core;

import DB.Database;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserInfoHandler {
    private Server server;
    private Database db;

    public UserInfoHandler(Server server) {
        this.server = server;
        this.db = new Database();
    }

    public void handleUserInfoRequest(CCUser requester, String targetNickname) {
        try {
            ResultSet userInfo = db.getUserInfo(targetNickname);
            if (userInfo != null && userInfo.next()) {
                sendUserInfo(requester.dos, targetNickname, userInfo);
                System.out.println("[Server] 사용자 정보 전송 성공: " + targetNickname);
            } else {
                sendEmptyUserInfo(requester.dos, targetNickname);
                System.out.println("[Server] 사용자 정보 없음: " + targetNickname);
            }
        } catch (SQLException e) {
            System.out.println("[Server] 사용자 정보 조회 실패: " + e.getMessage());
            sendEmptyUserInfo(requester.dos, targetNickname);
        }
    }

    private void sendUserInfo(DataOutputStream dos, String nickname, ResultSet userInfo) {
        try {
            dos.writeUTF(MessageType.USER_INFO_RESPONSE);
            dos.writeUTF(nickname);
            dos.writeInt(userInfo.getInt("win"));
            dos.writeInt(userInfo.getInt("lose"));
            
            byte[] profileImage = userInfo.getBytes("profile_picture");
            if (profileImage != null) {
                dos.writeInt(profileImage.length);
                dos.write(profileImage);
            } else {
                dos.writeInt(0);
            }
            dos.flush();
        } catch (IOException | SQLException e) {
            System.out.println("[Server] 사용자 정보 전송 실패: " + e.getMessage());
            sendEmptyUserInfo(dos, nickname);
        }
    }

    private void sendEmptyUserInfo(DataOutputStream dos, String nickname) {
        try {
            dos.writeUTF(MessageType.USER_INFO_RESPONSE);
            dos.writeUTF(nickname);
            dos.writeInt(0);  // wins
            dos.writeInt(0);  // losses
            dos.writeInt(0);  // 이미지 크기 0
            dos.flush();
        } catch (IOException e) {
            System.out.println("[Server] 오류 응답 전송 실패: " + e.getMessage());
        }
    }
} 