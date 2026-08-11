package core;

import java.util.*;
import static core.MessageType.*;
import ui.*;
import DB.*;
import java.io.IOException;
import java.util.stream.Collectors;

public class Room {
    public String title;
    public int count = 0;
    private boolean gameInProgress;

    // 사용자 목록 (방에 있는 사용자들)
    public Vector<CCUser> ccu; // 현재 방에 접속한 사용자들
    
    // 오목 보드 상태 (20x20 크기)
    private int[][] omok = new int[20][20];
    
	public byte[] hostProfileImage;

    // 관전자 목록
    private Vector<CCUser> spectators; // 관전자 목록

    // 생성자: 방 이름 설정
    public Room(String title) {
        this.title = title;
        ccu = new Vector<>();
        spectators = new Vector<>(); // 관전자 Vector 초기화
    }

    // 게임 진행 중 상태 반환
    public boolean isGameInProgress() {
        return gameInProgress;
    }
    public void recordMove(int x, int y, String color) {
        // color 문자열을 정수로 변환 (BLACK -> 1, WHITE -> 2)
        int colorValue = color.equals("BLACK") ? 1 : 2;
        
        // MoveRecord 객체 생성 후 추가
        moveHistory.add(new MoveRecord(x, y, colorValue));
        
        System.out.println("[Room] 수 기록 완료: " + x + "," + y + "," + colorValue + " (현재 총 " + moveHistory.size() + "수)");
    }
    
    // 게임 진행 중 상태 설정
    public void setGameInProgress(boolean gameInProgress) {
        this.gameInProgress = gameInProgress;
    }

    // 사용자 추가 메서드
    public void addClient(CCUser client) {
        if (!ccu.contains(client)) {
            ccu.add(client);
            count++;
        }
    }

    // 사용자 제거 메서드
    public void removeClient(CCUser client) {
        if (ccu.contains(client)) {
            ccu.remove(client);
            count--;
            readyStatus.remove(client);  // 준비 상태도 제거
            
            // 남은 사용자들에게 준비 상태 업데이트 알림
            for (CCUser u : ccu) {
                u.sendRoom(READY_STATUS + "//" + client.toString() + "//false");
            }
        }
    }

    // 현재 사용자 수 반환
    public int getUserCount() {
        return count;
    }

    // 방이 비어있는지 확인
    public boolean isEmpty() {
        return count == 0;
    }

    // 방 제목 반환
    public String getTitle() {
        return title;
    }

    // 방의 현재 상를 문자열로 반환하는 메서드
    public String getGameState() {
        StringBuilder gameState = new StringBuilder();
        for (int y = 0; y < omok.length; y++) {
            for (int x = 0; x < omok[y].length; x++) {
                gameState.append(omok[y][x]);
                if (x < omok[y].length - 1) {
                    gameState.append(",");  // 열 구분
                }
            }
            if (y < omok.length - 1) {
                gameState.append(";");  // 행 구분
            }
        }
        return gameState.toString();
    }

    private List<MoveRecord> moveHistory = new ArrayList<>();
    
    // 내부 클래스로 MoveRecord 정의
    public static class MoveRecord {
        public final int x, y, color;
        
        public MoveRecord(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }
        
        public String serialize() {
            return String.format("%d,%d,%d", x, y, color);
        }
    }

    // 돌을 놓을 때 기록도 함께 저장
    public void placePiece(int x, int y, int color) {
        // 오목판에 돌 놓기
        omok[x][y] = color;
        
        // 수 기록에 추가
        moveHistory.add(new MoveRecord(x, y, color));
        
        try {
            // 플레이어들에게 전송
            for (CCUser user : ccu) {
                user.dos.writeUTF(PLACE_PIECE + "//" + x + "//" + y + "//" + color);
            }
            
            // 관전자들에게도 전송
            for (CCUser spectator : spectators) {
                spectator.dos.writeUTF(MOVE_UPDATE + "//" + x + "//" + y + "//" + color);
            }
        } catch (IOException e) {
            System.out.println("[Room] 수 전송 실패: " + e.getMessage());
        }
    }

    // 관전자를 위한 현재까지의 수 기록 반환
    public String getMoveHistoryAsString() {
        return moveHistory.stream()
            .map(MoveRecord::serialize)  // serialize() 메서드 사용
            .collect(Collectors.joining(";"));
    }

    // 게임 종료 시 복기 데이터 저장 로직 필요
    public void saveReplayData() {
        // DB나 파일에 저장
    }

    // 게임이 끝나고 새 게임을 시작할 때 기록 초기화
    public void clearMoveHistory() {
        moveHistory.clear();
        System.out.println("[Room] 복기 데이터 초기화");
    }

    // 오목 초기화 메서드 추가
    public void resetGame() {
        // 오목판 초기화
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                omok[i][j] = 0;
            }
        }
        
        // 게임 상태 초기화
        gameInProgress = false;
        
        // 복기 데이터 초기화
        clearMoveHistory();
        
        // 준비 상태 초기화
        clearReadyStatus();
        
        System.out.println("[Room] 게임 상태 완전 초기화 완료");
    }

    private Map<CCUser, Boolean> readyStatus = new HashMap<>();
    
    // 준비 상태 설정
    public void setReady(CCUser user, boolean ready) {
        readyStatus.put(user, ready);
        
        // 모든 사용자에게 준비 상태 변경을 알림
        for (CCUser u : ccu) {
            try {
                // 메시지 형식 변경: "READY_STATUS//닉네임//상태//추가정보"
                String readyMessage = String.format("%s//%s//%s//READY_UPDATE", 
                    READY_STATUS, 
                    user.nickname, 
                    ready ? "true" : "false"
                );
                u.dos.writeUTF(readyMessage);
            } catch (IOException e) {
                System.out.println("[Room] 준비 상태 전송 실패: " + e.getMessage());
            }
        }
        
        // 모든 플레이어가 준비되었는지 확인하고 방장에게 알림
        if (isAllReady()) {
            CCUser host = ccu.get(0);
            try {
                String allReadyMessage = String.format("%s//ALL_READY//true//STATUS_UPDATE", READY_STATUS);
                host.dos.writeUTF(allReadyMessage);
            } catch (IOException e) {
                System.out.println("[Room] 준비 상태 전송 실패: " + e.getMessage());
            }
        }
    }
    public void handleMoveHistoryRequest(CCUser spectator) {
        try {
            String history = getMoveHistoryAsString();
            spectator.dos.writeUTF(MOVE_HISTORY_UPDATE + "//" + history);
        } catch (IOException e) {
            System.out.println("[Room] 수 기록 전송 실패: " + e.getMessage());
        }
    }
    
    // 모든 플레이어가 준비되었는지 확인
    public boolean isAllReady() {
        if (ccu.size() < 2) return false;
        
        // 모든 플레이어(방장 포함)가 준비되었는지 확인
        for (CCUser user : ccu) {
            Boolean ready = readyStatus.get(user);
            if (ready == null || !ready) return false;
        }
        return true;
    }

    // 방 입장 시 준비 상태 초기화
    public void initializeClientStatus(CCUser client) {
        readyStatus.put(client, false);
    }

    public void clearReadyStatus() {
        readyStatus.clear();
        System.out.println("[Room] 준비 상태 초기화 완료");
    }

    // 관전자 추가 메서드
    public void addSpectator(CCUser spectator) {
        if (!spectators.contains(spectator)) {
            spectators.add(spectator);
            // 방 참조 설정
            spectator.myRoom = this;
        }
    }

    // 관전자 제거 메서드
    public void removeSpectator(CCUser spectator) {
        spectators.remove(spectator);
        // 방 참조 제거
        spectator.myRoom = null;
    }

    // 현재 게임 상태를 문자열로 반환하는 메서드 (관전자에게 전송용)
    public String getCurrentGameState() {
        StringBuilder state = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                state.append(omok[i][j]);
                if (i != 19 || j != 19) {
                    state.append(",");
                }
            }
        }
        return state.toString();
    }

    // 모든 관전자에게 게임 상태 업데이트 전송
    public void broadcastToSpectators(String gameState) {
        for (CCUser spectator : spectators) {
            try {
                spectator.dos.writeUTF(GAME_STATE_UPDATE + "//" + gameState);
            } catch (IOException e) {
                System.out.println("[Room] 관전자 업데이트 실패: " + spectator.nickname);
            }
        }
    }

    // 게임 상태를 모든 참가자와 관전자에게 전송하는 메서드
    private void broadcastGameState(String gameState) {
        // 플레이어들에게 전송
        for (CCUser user : ccu) {
            try {
                user.dos.writeUTF(GAME_STATE_UPDATE + "//" + gameState);
                user.dos.flush(); // flush 추가
            } catch (IOException e) {
                System.out.println("[Room] 플레이어 업데이트 실패: " + user.nickname);
            }
        }
        
        // 관전자들에게 전송
        for (CCUser spectator : spectators) {
            try {
                spectator.dos.writeUTF(GAME_STATE_UPDATE + "//" + gameState);
                spectator.dos.flush(); // flush 추가
                System.out.println("[Room] 관전자 업데이트 전송: " + spectator.nickname);
            } catch (IOException e) {
                System.out.println("[Room] 관전자 업데이트 실패: " + spectator.nickname);
            }
        }
    }

    public boolean isSpectator(CCUser user) {
        return spectators.contains(user);
    }

    // 관전자를 위한 현재까지의 수 기록 반환
    public List<String> getMoveHistory() {
        return moveHistory.stream()
            .map(move -> move.x + "," + move.y + "," + move.color)
            .collect(Collectors.toList());
    }
}
