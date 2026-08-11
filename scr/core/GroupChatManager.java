package core;

import java.io.IOException;
import java.util.*;

public class GroupChatManager {
    private static GroupChatManager instance;
    private Map<String, Set<CCUser>> groupChatRooms;
    private Server server;
    
    private GroupChatManager() {
        groupChatRooms = new HashMap<>();
    }
    
    public void setServer(Server server) {
        this.server = server;
    }
    
    public static synchronized GroupChatManager getInstance() {
        if (instance == null) {
            instance = new GroupChatManager();
        }
        return instance;
    }

    public boolean createRoom(CCUser creator, String roomName) {
        if (groupChatRooms.containsKey(roomName)) {
            return false;
        }

        Set<CCUser> users = new HashSet<>();
        users.add(creator);
        groupChatRooms.put(roomName, users);
        
        // 방 생성 후 즉시 목록 변경 알림
        notifyRoomListChanged();
        
        System.out.println("[Server] 새로운 그룹 채팅방 생성: " + roomName);
        return true;
    }

    public boolean joinRoom(CCUser user, String roomName) {
        Set<CCUser> roomUsers = groupChatRooms.get(roomName);
        if (roomUsers != null) {
            roomUsers.add(user);
            return true;
        }
        return false;
    }

    public void leaveRoom(CCUser user, String roomName) {
        Set<CCUser> roomUsers = groupChatRooms.get(roomName);
        if (roomUsers != null) {
            roomUsers.remove(user);
            if (roomUsers.isEmpty()) {
                groupChatRooms.remove(roomName);
            }
        }
    }

    public void broadcastToRoom(String roomName, String message) {
        Set<CCUser> roomUsers = groupChatRooms.get(roomName);
        if (roomUsers != null) {
            for (CCUser user : roomUsers) {
                try {
                    user.dos.writeUTF(message);
                } catch (IOException e) {
                    System.out.println("[Server] 그룹 채팅 메시지 전송 오류: " + e.getMessage());
                }
            }
        }
    }

    public Set<String> getRoomList() {
        return new HashSet<>(groupChatRooms.keySet());
    }

    public Set<CCUser> getRoomUsers(String roomName) {
        return groupChatRooms.get(roomName);
    }

    public void removeUserFromAllRooms(CCUser user) {
        List<String> roomsToCheck = new ArrayList<>(groupChatRooms.keySet());
        for (String roomName : roomsToCheck) {
            leaveRoom(user, roomName);
        }
    }

    public boolean isUserInRoom(CCUser user, String roomName) {
        Set<CCUser> roomUsers = groupChatRooms.get(roomName);
        return roomUsers != null && roomUsers.contains(user);
    }

    public boolean inviteUser(String roomName, CCUser inviter, CCUser invitee) {
        Set<CCUser> users = groupChatRooms.get(roomName);
        if (users != null && users.contains(inviter)) {
            return true;
        }
        return false;
    }

    public boolean acceptInvitation(String roomName, CCUser user) {
        Set<CCUser> users = groupChatRooms.get(roomName);
        if (users != null) {
            users.add(user);
            return true;
        }
        return false;
    }

    public boolean removeUserFromRoom(String roomName, CCUser user) {
        Set<CCUser> users = groupChatRooms.get(roomName);
        if (users != null) {
            users.remove(user);
            
            // 채팅방이 비었을 때 삭제
            if (users.isEmpty()) {
                groupChatRooms.remove(roomName);
                System.out.println("[Server] 빈 채팅방 삭제: " + roomName);
            }
            return true;
        }
        return false;
    }

    // 방 목록 변경 알림 메서드 추가
    private void notifyRoomListChanged() {
        if (server != null && server.getGroupChatHandler() != null) {
            server.getGroupChatHandler().broadcastGroupChatList();
        } else {
            System.out.println("[Server] 방 목록 업데이트 실패: 서버 또는 핸들러가 null입니다.");
        }
    }
} 