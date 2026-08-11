package DB;

import core.*;
import ui.signup.*;
import ui.*;

import java.sql.*;
import config.AppConfig;

// 클라이언트가 요청한 데이터베이스 업데이트 및 쿼리 작업을 수행하는 클래스.
public class Database {
    public Connection con;
    public Statement stmt;
    String url = AppConfig.optional("OMOK_DB_URL", "jdbc:oracle:thin:@localhost:1521:testdb");
    String user = AppConfig.require("OMOK_DB_USER");
    String passwd = AppConfig.require("OMOK_DB_PASSWORD");

    public Database() { // Database 객체 생성 시 데이터베이스 서버와 연결한다.
        try { // 데이터베이스 연결은 try-catch문으로 예외를 잡아준다.
            // 데이터베이스와 연결한다.
            Class.forName("oracle.jdbc.driver.OracleDriver"); // Oracle 드라이버 로드
            con = DriverManager.getConnection(url, user, passwd);
            stmt = con.createStatement();
            System.out.println("[Server] Oracle 서버 연동 성공"); // 데이터베이스 연결에 성공하면 성공을 콘솔로 알린다.
        } catch (Exception e) { // 데이터베이스 연결에 예외가 발생했을 때 패를 콘��로 알린다.
            System.out.println("[Server] Oracle 서버 연동 실패 > " + e.toString());
        }
    }

    // 로그인 여부를 확인하는 메소드. 서버에 닉네임을 String 형식으로 반환한다.
    public String loginCheck(String _i, String _p) {
        String nickname = "null";
        String id = _i;
        String pw = _p;
        
        try {
            // admin 계정 체크
            if (id.equals(AppConfig.optional("OMOK_ADMIN_ID", "admin"))
                    && pw.equals(AppConfig.require("OMOK_ADMIN_PASSWORD"))) {
                return "admin";
            }
            
            String checkingStr = "SELECT password, nickname, is_temp_password, deleted FROM Users WHERE id=?";
            PreparedStatement pstmt = con.prepareStatement(checkingStr);
            pstmt.setString(1, _i);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                if (result.getInt("deleted") == 1) {
                    return "DELETED"; // 삭제된 계정
                }
                if (pw.equals(result.getString("password"))) {
                    nickname = result.getString("nickname");
                    // 임시 비밀번호인 경우 "nickname//TEMP" 형식으로 반환
                    if (result.getBoolean("is_temp_password")) {
                        nickname += "//TEMP";
                    }
                    
                    System.out.println("[Server] 로그인 성공");
                } else {
                    nickname = "null";
                    System.out.println("[Server] 로그인 실패: 비밀번호 불일치");
                }
            }
        } catch (Exception e) {
            nickname = "null";
            System.out.println("[Server] 로그인 실패 > " + e.toString());
        }
        return nickname;
    }

    // 회원가입을 수행하는 메소드. 회원가입에 성공하면 true, 실패하면 false를 반환한다.
    public boolean joinCheck(String _n, String _nn, String _i, String _p, String _e, String _g, String _ph, String _postal, String _addr, String _detailAddr, String _birthDate, byte[] profilePicture) {
        boolean flag = false;

        String insertStr = "INSERT INTO Users (name, nickname, id, password, email, gender, phone, postal, address, detail_address, birth_date, profile_picture, win, lose) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, 0)";
        try (PreparedStatement pstmt = con.prepareStatement(insertStr)) {
            pstmt.setString(1, _n);
            pstmt.setString(2, _nn);
            pstmt.setString(3, _i);
            pstmt.setString(4, _p);
            pstmt.setString(5, _e);
            pstmt.setString(6, _g);
            pstmt.setString(7, _ph);
            pstmt.setString(8, _postal);
            pstmt.setString(9, _addr);
            pstmt.setString(10, _detailAddr);
            pstmt.setString(11, _birthDate);
            if (profilePicture != null && profilePicture.length > 0) {
                pstmt.setBytes(12, profilePicture);
            } else {
                pstmt.setNull(12, java.sql.Types.BLOB);
            }

            pstmt.executeUpdate();
            flag = true; // 업데이트문이 정상적으로 수행되면 flag를 true로 초기화하고 성공을 콘솔로 알린다.
            System.out.println("[Server] 회원가입 성공");
        } catch (SQLException e) {
            flag = false;
            System.out.println("[Server] 회원가입 실패 > " + e.toString());
        }

        return flag;
    }

    // 중복 검사 메서드
    public boolean overCheck(String fieldName, String value) {
        if (!java.util.Set.of("id", "nickname", "email", "phone").contains(fieldName)) {
            throw new IllegalArgumentException("Unsupported duplicate-check field: " + fieldName);
        }
        String sql = "SELECT COUNT(*) FROM users WHERE " + fieldName + " = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, value);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.out.println(fieldName + " 중복 검사 오류: " + e.getMessage());
        }
        return false;
    }

    // 아이디 중복 검사 메서드
    public boolean isIdTaken(String id) {
        return overCheck("id", id);
    }

    // 이메일 중복 검사 메서드
    public boolean isEmailTaken(String email) {
        return overCheck("email", email);
    }

    // 닉네임 중복 검사 메서드
    public boolean isNicknameTaken(String nickname) {
        return overCheck("nickname", nickname);
    }

    // 데이터베이스에 저장된 자신의 정보를 조회하는 메소드. 조회한 정보들을 String 형태로 반환한다.
    public String viewInfo(String _nn) {
        String msg = "null"; // 반환할 문자열 변수를 "null"로 초기화.

        // 매개변수로 받은 닉네임을 nick에 초기화한다.
        String nick = _nn;

        try {
            // Users 테이블에서 nick이라는 닉네임을 가진 회원의 ��름과 이메일 정보를 조회한다.
            String viewStr = "SELECT name, email FROM Users WHERE nickname=?";
            PreparedStatement pstmt = con.prepareStatement(viewStr);
            pstmt.setString(1, nick);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                // msg에 "이름//닉네임//이메일" 형태로 초기화한다.
                msg = result.getString("name") + "//" + nick + "//" + result.getString("email");
            }
            System.out.println("[Server] 회원정보 조회 성공"); // 정상적으로 수행되면 성공을 콘솔로 알린다.
        } catch (Exception e) { // 정상적으로 수행하지 못하면 실패를 콘솔로 알린다.
            System.out.println("[Server] 회원정보 조회 실패 > " + e.toString());
        }

        return msg; // msg 반환
    }

    // 회원정보를 변경을 수행하는 메소드. 변경에 성공하면 true, 실패하면 false를 반환한다.
    public boolean changeInfo(String _nn, String _a, String _v) {
        boolean flag = false; // 참거짓을 반환할 flag 변수. 초기값은 false.

        // 매개변수로 받은 정보들을 초기화한다. att는 속성(이름, 이메일, 비밀번호) 구분용이고 val은 바꿀 값.
        String nick = _nn;
        String att = _a;
        String val = _v;

        if (!java.util.Set.of("name", "email", "password", "phone", "address", "detail_address")
                .contains(att)) {
            throw new IllegalArgumentException("Unsupported profile field: " + att);
        }

        try {
            // Users 테이블에서 nick이라는 닉네임을 가진 회원의 att(이름, 이메일, 비밀번호)를 val로 변경한다.
            String changeStr = "UPDATE Users SET " + att + "=? WHERE nickname=?";
            PreparedStatement pstmt = con.prepareStatement(changeStr);
            pstmt.setString(1, val);
            pstmt.setString(2, nick);
            pstmt.executeUpdate();

            flag = true; // 정상적으로 수행되면 flag를 true로 바꾸고 성공을 콘솔로 알린다.
            System.out.println("[Server] 회원정보 변경 ");
        } catch (Exception e) { // 정상적으로 수행하지 못하면 flag를 false로 바꾸고 실패를 콘솔로 알린다.
            flag = false;
            System.out.println("[Server] 회원정보 변경 실패 > " + e.toString());
        }

        return flag; // flag 반환
    }

    // 전체 회원의 전적을 조회하는 메소드. 모든 회원의 전적을 String 형태로 반환한다.
    public String viewRank() {
        String msg = "";
        
        try {
            // 승률 계산 및 정렬하여 조회
            String viewStr = "SELECT nickname, win, lose, " +
                            "ROUND(DECODE(win + lose, 0, 0, (win / (win + lose)) * 100), 2) as winrate " +
                            "FROM Users " +
                            "WHERE deleted = 0 " +  // 삭제되지 않은 사용자만
                            "ORDER BY winrate DESC, win DESC";
            ResultSet result = stmt.executeQuery(viewStr);

            while (result.next()) {
                // "닉네임 : n승 n패 (승률 n%)@" 태로 포맷팅
                msg = msg + result.getString("nickname") + " : " + 
                      result.getInt("win") + "승 " + 
                      result.getInt("lose") + "패 (" + 
                      result.getDouble("winrate") + "%)@";
            }
            System.out.println("[Server] 전적 랭킹 조회 성공");
        } catch (Exception e) {
            System.out.println("[Server] 전적 랭킹 조회 실패 > " + e.toString());
        }

        return msg;
    }

    // 한 명의 회원의 전적을 조회하는 메소드. 해당 회원의 전적을 String 형태로 반환한다.
    public String searchRank(String _nn) {
        String msg = "null"; // 전적을 받을 문자열. 초기값은 "null"로 한다.

        // 매개변수로 받은 닉네임을 초기화한다.
        String nick = _nn;

        try {
            // Users 테이블에서 nick이라는 닉네임을 가진 ���원의 승, 패를 조회한다.
            String searchStr = "SELECT win, lose FROM Users WHERE nickname=?";
            PreparedStatement pstmt = con.prepareStatement(searchStr);
            pstmt.setString(1, nick);
            ResultSet result = pstmt.executeQuery();

            if (result.next()) {
                // msg에 "닉네임 : n승 n패" 형태로 초기화한다.
                msg = nick + " : " + result.getInt("win") + "승 " + result.getInt("lose") + "패";
            }
            System.out.println("[Server] 전적 조 성공"); // 정상적으로 수행되면 성공을 콘솔로 알린다.
        } catch (Exception e) { // 정상적으로 수행하지 못하면 실패를 콘솔로 알린다.
            System.out.println("[Server] 전적 조회 실패 > " + e.toString());
        }

        return msg; // msg 반환
    }

    // 게임 승리 시 전적 업데이트하는 메소드. 조회 및 업데이트에 성공하면 true, 실패하면 false를 반환한다.
    public boolean winRecord(String _nn) {
        boolean flag = false; // 참거짓을 반환할 flag 변수. 초기값은 false.

        // 매개변수로 받은 닉네임과 조회한 승리 횟수를 저장할 변수. num의 초기값은 0.
        String nick = _nn;
        int num = 0;

        try {
            // Users 테이블에서 nick이라는 닉네임을 가진 회원의 승리 횟수를 ��회한다.
            String searchStr = "SELECT win FROM Users WHERE nickname=?";
            PreparedStatement searchStatement = con.prepareStatement(searchStr);
            searchStatement.setString(1, nick);
            ResultSet result = searchStatement.executeQuery();

            if (result.next()) {
                // num에 조회한 승리 횟수를 초기화.
                num = result.getInt("win");
                num++; // 승리 횟수를 올림

                // Users 테이블에서 nick이라는 닉네임을 가진 회의 승리 횟수를 num으로 업데이트한다.
                String changeStr = "UPDATE Users SET win=? WHERE nickname=?";
                PreparedStatement updateStatement = con.prepareStatement(changeStr);
                updateStatement.setInt(1, num);
                updateStatement.setString(2, nick);
                updateStatement.executeUpdate();
                flag = true; // 조회 및 업데이트 성공 시 flag를 true로 바꾸고 성공을 콘솔로 알린다.
                System.out.println("[Server] 전적 업데이트 성공");
            }
        } catch (Exception e) { // 조회 및 업데이트 실패 시 flag를 false로 바꾸고 실패를 콘솔로 알린다.
            flag = false;
            System.out.println("[Server] 전적 업데이트 실패 > " + e.toString());
        }

        return flag; // flag 반환
    }

    // 게임 패배 시 전적을 업데이트하는 메소드. 조회 및 업데이트에 성공하면 true, 실패하면 false를 반환한다.
    public boolean loseRecord(String _nn) {
        boolean flag = false; // 참거짓을 반환할 flag 변수. 초기값은 false.

        // 매개변수로 받은 닉네임과 조회한 패배 횟수를 저장할 변수. num의 초기값은 0.
        String nick = _nn;
        int num = 0;

        try {
            // Users 테이블에서 nick이라는 닉네임을 가진 회원의 패배 횟수를 조회한다.
            String searchStr = "SELECT lose FROM Users WHERE nickname=?";
            PreparedStatement searchStatement = con.prepareStatement(searchStr);
            searchStatement.setString(1, nick);
            ResultSet result = searchStatement.executeQuery();

            if (result.next()) {
                // num에 조회한 패배 횟수를 초기화.
                num = result.getInt("lose");
                num++; // 패배 횟수를 올림

                // Users 테이블에서 nick이라는 닉네임을 가진 회원의 패배 횟수를 num으로 업데이트한다.
                String changeStr = "UPDATE Users SET lose=? WHERE nickname=?";
                PreparedStatement updateStatement = con.prepareStatement(changeStr);
                updateStatement.setInt(1, num);
                updateStatement.setString(2, nick);
                updateStatement.executeUpdate();
                flag = true; // 조회 및 업데이트 성공 시 flag를 true로 바꾸고 성공을 콘솔로 알린다.
                System.out.println("[Server] 전적 업데이트 성공");
            }
        } catch (Exception e) { // 조회 및 업데이트 실패 시 flag를 false로 바꾸고 실패를 콘솔로 알린다.
            flag = false;
            System.out.println("[Server] Error: > " + e.toString());
        }

        return flag; // flag 반환
    }

    // 채팅 메시지 저장
    public boolean saveChatMessage(String sender, String receiver, String message, String messageType) {
        String sql = "INSERT INTO ChatMessages (message_id, sender, receiver, message_text, message_type, timestamp) " +
                    "VALUES (chat_message_seq.NEXTVAL, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            pstmt.setString(3, message);
            pstmt.setString(4, messageType);
            pstmt.executeUpdate();
            System.out.println("[Server] 채팅 메시지 저장 성공: " + sender + " -> " + receiver);
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 2291) { // 외래키 제약조건 위반 에러 코드
                System.out.println("[Server] 존재하지 않는 사용자와의 채팅 시도");
            } else {
                System.out.println("[Server] 채팅 메시지 저장 실패 > " + e.toString());
            }
            return false;
        }
    }

    // 채팅 내역 불러오기
    public ResultSet loadChatMessages(String user1, String user2, int page, int pageSize) throws SQLException {
        String sql = "SELECT * FROM (" +
                    "    SELECT a.*, ROWNUM rnum FROM (" +
                    "        SELECT * FROM ChatMessages " +
                    "        WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?) " +
                    "        ORDER BY timestamp DESC" +
                    "    ) a WHERE ROWNUM <= ?" +
                    ") WHERE rnum > ?";
        
        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, user1);
        pstmt.setString(2, user2);
        pstmt.setString(3, user2);
        pstmt.setString(4, user1);
        pstmt.setInt(5, (page + 1) * pageSize);
        pstmt.setInt(6, page * pageSize);
        
        return pstmt.executeQuery();
    }

    // 읽음 상태 업데이트
    public boolean updateReadStatus(String sender, Object nickname) {
        String sql = "UPDATE ChatMessages SET is_read = 1 " +
                    "WHERE sender = ? AND receiver = ? AND is_read = 0";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, (String) nickname);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("[Server] 읽음 상태 업데이트 실패 > " + e.toString());
            return false;
        }
    }

    // 안 읽은 메시지 수 확인
    public int getUnreadMessageCount(String sender, String receiver) {
        String sql = "SELECT COUNT(*) FROM ChatMessages " +
                    "WHERE sender = ? AND receiver = ? AND is_read = 0";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, sender);
            pstmt.setString(2, receiver);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("[Server] 안 읽은 메시지 수 확인 실패 > " + e.toString());
        }
        return 0;
    }

    // 사용자 삭제 시 채팅 기록 처리 메서드 추가
    public boolean deleteUserAndChats(String nickname) {
        try {
            // 트랜잭션 시작loadChatMessages
            con.setAutoCommit(false);
            
            // 해당 사용자의 채팅 기록 삭제
            String deleteChatsSql = "DELETE FROM ChatMessages WHERE sender = ? OR receiver = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteChatsSql)) {
                pstmt.setString(1, nickname);
                pstmt.setString(2, nickname);
                pstmt.executeUpdate();
            }
            
            // 사용자 삭제
            String deleteUserSql = "DELETE FROM Users WHERE nickname = ?";
            try (PreparedStatement pstmt = con.prepareStatement(deleteUserSql)) {
                pstmt.setString(1, nickname);
                pstmt.executeUpdate();
            }
            
            // 트랜잭션 커밋
            con.commit();
            System.out.println("[Server] 사용자 및 채팅 기록 삭제 성공: " + nickname);
            return true;
            
        } catch (SQLException e) {
            try {
                // 오류 발생 시 롤백
                con.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("[Server] 롤백 실패 > " + rollbackEx.toString());
            }
            System.out.println("[Server] 사용자 삭제 실패 > " + e.toString());
            return false;
            
        } finally {
            try {
                // auto-commit 모드 복구
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("[Server] auto-commit 복구 실패 > " + e.toString());
            }
        }
    }

    // 닉네임 변경 시 채팅 기록 업데이트 메서드 추가
    public boolean updateUserNickname(String oldNickname, String newNickname) {
        try {
            // 트랜잭션 시작
            con.setAutoCommit(false);
            
            // ChatMessages 테이블의 sender 업데이트
            String updateSenderSql = "UPDATE ChatMessages SET sender = ? WHERE sender = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateSenderSql)) {
                pstmt.setString(1, newNickname);
                pstmt.setString(2, oldNickname);
                pstmt.executeUpdate();
            }
            
            // ChatMessages 테이블의 receiver 업데이트
            String updateReceiverSql = "UPDATE ChatMessages SET receiver = ? WHERE receiver = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateReceiverSql)) {
                pstmt.setString(1, newNickname);
                pstmt.setString(2, oldNickname);
                pstmt.executeUpdate();
            }
            
            // Users 테이블의 nickname 업데이트
            String updateUserSql = "UPDATE Users SET nickname = ? WHERE nickname = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateUserSql)) {
                pstmt.setString(1, newNickname);
                pstmt.setString(2, oldNickname);
                pstmt.executeUpdate();
            }
            
            // 트랜잭션 커밋
            con.commit();
            System.out.println("[Server] 닉네임 변경 성공: " + oldNickname + " -> " + newNickname);
            return true;
            
        } catch (SQLException e) {
            try {
                // 오류 발생 시 롤백
                con.rollback();
            } catch (SQLException rollbackEx) {
                System.out.println("[Server] 롤백 실패 > " + rollbackEx.toString());
            }
            System.out.println("[Server] 닉네임 변경 실패 > " + e.toString());
            return false;
            
        } finally {
            try {
                // auto-commit 모드 복구
                con.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("[Server] auto-commit 복구 실패 > " + e.toString());
            }
        }
    }

    public ResultSet getStats(String nickname) {
        ResultSet rs = null;
        try {
            String sql = "SELECT win, lose FROM Users WHERE nickname = ?";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, nickname);
            rs = pstmt.executeQuery();
            
            System.out.println("[Database] " + nickname + "님의 전적 조회 성공");
            return rs;
        } catch (SQLException e) {
            System.out.println("[Database] 전적 조회 실패 > " + e.toString());
            return null;
        }
    }

    // 아이디 찾기
    public String findId(String name, String email, String birthDate) {
        String id = null;
        String sql = "SELECT id FROM Users WHERE name=? AND email=? AND birth_date=? AND deleted=0";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, birthDate);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                id = rs.getString("id");
                System.out.println("[Server] 아이디 찾기 성공");
            }
        } catch (SQLException e) {
            System.out.println("[Server] 아이디 찾기 실패 > " + e.toString());
        }
        return id;
    }

    // 비밀번호 찾기
    public String findPassword(String id, String email, String birthDate) {
        String password = null;
        String sql = "SELECT password FROM Users WHERE id=? AND email=? AND birth_date=? AND deleted=0";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, email);
            pstmt.setString(3, birthDate);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                password = rs.getString("password");
                System.out.println("[Server] 비밀번호 찾기 성공");
            }
        } catch (SQLException e) {
            System.out.println("[Server] 비밀번호 찾기 실패 > " + e.toString());
        }
        return password;
    }

    public boolean verifyUser(String id, String email, String birthDate) {
        String sql = "SELECT * FROM Users WHERE id=? AND email=? AND birth_date=? AND deleted=0";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.setString(2, email);
            pstmt.setString(3, birthDate);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.out.println("[Server] 사자 확인 실패 > " + e.toString());
            return false;
        }
    }

    public boolean resetPassword(String id, String newPassword) {
        return resetPassword(id, newPassword, false);  // 기본적으로 임시 비밀번호가 아님
    }

    public boolean resetPassword(String id, String newPassword, boolean isTemp) {
        String sql = "UPDATE Users SET password=?, is_temp_password=? WHERE id=? AND deleted=0";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, isTemp ? 1 : 0);
            pstmt.setString(3, id);
            
            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            System.out.println("[Server] 비밀번호 재설정 실패 > " + e.toString());
            return false;
        }
    }

    public boolean sendTempPasswordAndUpdate(String id, String email, String birthDate) {
        if (!verifyUser(id, email, birthDate)) {
            return false;
        }

        String tempPassword = EmailUtil.generateTempPassword();
        if (!resetPassword(id, tempPassword, true)) {
            return false;
        }

        return EmailUtil.sendTempPassword(email, tempPassword);
    }

    public String getLoginIdByNickname(String nickname) {
        String sql = "SELECT id FROM Users WHERE nickname = ? AND deleted = 0";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, nickname);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id");
            }
        } catch (SQLException e) {
            System.out.println("[Database] 로그인 ID 조회 실패: " + e.getMessage());
        }
        return null;
    }

    public ResultSet getUserInfo(String nickname) {
        try {
            String sql = "SELECT nickname, win, lose, profile_picture " +
                        "FROM Users WHERE nickname = ? AND deleted = 0";
            PreparedStatement pstmt = con.prepareStatement(sql);
            pstmt.setString(1, nickname);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("[Server] 사용자 정보 조회 실패 > " + e.toString());
            return null;
        }
    }
}
