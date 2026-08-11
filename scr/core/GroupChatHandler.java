package core;

import static core.MessageType.*;
import java.io.IOException;
import java.util.Set;

public class GroupChatHandler {
    private final GroupChatManager chatManager;
    private final Server server;

    public GroupChatHandler(Server server) {
        this.server = server;
        this.chatManager = GroupChatManager.getInstance();
    }

    public void handleCreateGroupChat(CCUser creator, String roomName) {
        try {
            if (chatManager.createRoom(creator, roomName)) {
                creator.dos.writeUTF(CREATE_GROUP_CHAT + "//SUCCESS//" + roomName);
                broadcastGroupChatList();
                System.out.println("[Server] 그룹 채팅방 생성: " + roomName);
            } else {
                creator.dos.writeUTF(CREATE_GROUP_CHAT + "//FAIL//이미 존재하는 채팅방 이름입니다.");
            }
        } catch (IOException e) {
            System.out.println("[Server] 그룹 채팅방 생성 오류: " + e.getMessage());
            try {
                creator.dos.writeUTF(CREATE_GROUP_CHAT + "//FAIL//서버 오류가 발생했습니다.");
            } catch (IOException ex) {
                System.out.println("[Server] 오류 응답 전송 실패: " + ex.getMessage());
            }
        }
    }

    public void handleJoinGroupChat(CCUser user, String roomName) {
        try {
            if (chatManager.joinRoom(user, roomName)) {
                user.dos.writeUTF(JOIN_GROUP_CHAT + "//SUCCESS//" + roomName);
                System.out.println("[Server] 그룹 채팅방 입장 성공: " + user.nickname + " -> " + roomName);
                
                String joinMessage = GROUP_CHAT_MESSAGE + "//" + roomName + "//SYSTEM//" 
                    + user.nickname + "님이 입장하셨습니다.";
                chatManager.broadcastToRoom(roomName, joinMessage);
                
                updateGroupChatUserList(roomName);
            } else {
                user.dos.writeUTF(JOIN_GROUP_CHAT + "//FAIL//존재하지 않는 채팅방입니다.");
                System.out.println("[Server] 그룹 채팅방 입장 실패: " + user.nickname);
            }
        } catch (IOException e) {
            System.out.println("[Server] 그룹 채팅방 입장 처리 오류: " + e.getMessage());
            try {
                user.dos.writeUTF(JOIN_GROUP_CHAT + "//FAIL//서버 오류가 발생했습니다.");
            } catch (IOException ex) {
                System.out.println("[Server] 오류 응답 전송 실패: " + ex.getMessage());
            }
        }
    }

    public void handleGroupChatMessage(CCUser sender, String roomName, String message) {
        if (chatManager.isUserInRoom(sender, roomName)) {
            String broadcastMessage = GROUP_CHAT_MESSAGE + "//" + 
                                    roomName + "//" + 
                                    sender.nickname + "//" + 
                                    message;
            chatManager.broadcastToRoom(roomName, broadcastMessage);
        }
    }

    public void handleLeaveGroupChat(CCUser user, String roomName) {
        try {
            if (chatManager.removeUserFromRoom(roomName, user)) {
                user.dos.writeUTF(LEAVE_GROUP_CHAT + "//SUCCESS//" + roomName);
                
                String exitMessage = GROUP_CHAT_MESSAGE + "//" + roomName + "//SYSTEM//" 
                                 + user.nickname + "님이 채팅방을 나갔습니다.";
                
                Set<CCUser> roomUsers = chatManager.getRoomUsers(roomName);
                if (roomUsers != null) {
                    for (CCUser roomUser : roomUsers) {
                        if (roomUser != null && roomUser.dos != null) {
                            roomUser.dos.writeUTF(exitMessage);
                        }
                    }
                }
                
                updateGroupChatUserList(roomName);
                broadcastGroupChatList();
                
                System.out.println("[Server] 사용자 채팅방 퇴장: " + user.nickname + " -> " + roomName);
            }
        } catch (IOException e) {
            System.out.println("[Server] 채팅방 퇴장 처리 오류: " + e.getMessage());
        }
    }

    public void broadcastGroupChatList() {
        try {
            Set<String> rooms = chatManager.getRoomList();
            StringBuilder roomList = new StringBuilder(GROUP_CHAT_LIST + "//");
            
            if (!rooms.isEmpty()) {
                for (String roomName : rooms) {
                    if (!roomName.trim().isEmpty()) {
                        roomList.append(roomName).append("@");
                    }
                }
                if (roomList.charAt(roomList.length() - 1) == '@') {
                    roomList.setLength(roomList.length() - 1);
                }
            }
            
            String message = roomList.toString();
            for (CCUser user : server.getAllUsers()) {
                try {
                    if (user != null && user.dos != null) {
                        user.dos.writeUTF(message);
                        System.out.println("[Server] 방 목록 전송 to " + user.nickname + ": " + message);
                    }
                } catch (IOException e) {
                    System.out.println("[Server] 방 목록 전송 실패 (" + user.nickname + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("[Server] 방 목록 브로드캐스트 오류: " + e.getMessage());
        }
    }

    private void updateGroupChatUserList(String roomName) {
        try {
            Set<CCUser> users = chatManager.getRoomUsers(roomName);
            if (users != null) {
                StringBuilder userList = new StringBuilder(GROUP_CHAT_USERS + "//" + roomName + "//");
                for (CCUser user : users) {
                    if (user != null && user.nickname != null) {
                        userList.append(user.nickname).append("@");
                    }
                }
                
                for (CCUser user : users) {
                    if (user != null && user.dos != null) {
                        user.dos.writeUTF(userList.toString());
                    }
                }
                System.out.println("[Server] 유저 목록 업데이트 완료: " + roomName);
            }
        } catch (IOException e) {
            System.out.println("[Server] 유저 목록 업데이트 오류: " + e.getMessage());
        }
    }

    public void handleUserDisconnect(CCUser user) {
        chatManager.removeUserFromAllRooms(user);
        broadcastGroupChatList();
    }

    public void handleGroupChatInvite(CCUser sender, String roomName, String invitee) {
        try {
            CCUser inviteeUser = server.findUserByNickname(invitee);
            if (inviteeUser != null) {
                String inviteMessage = GROUP_CHAT_INVITE + "//" + roomName + "//" + sender.nickname;
                inviteeUser.dos.writeUTF(inviteMessage);
                System.out.println("[Server] 그룹 채팅 초대 메시지 전송 완료: " + sender.nickname + " -> " + invitee + " (방: " + roomName + ")");
            } else {
                sender.dos.writeUTF(GROUP_CHAT_INVITE + "//FAIL//사용자를 찾을 수 없습니다.");
                System.out.println("[Server] 초대 실패: 사용자를 찾을 수 없음 - " + invitee);
            }
        } catch (IOException e) {
            System.out.println("[Server] 초대 처리 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void handleInviteResponse(CCUser responder, String roomName, String response) {
        try {
            if (response.equals("ACCEPT")) {
                if (chatManager.joinRoom(responder, roomName)) {
                    responder.dos.writeUTF(JOIN_GROUP_CHAT + "//SUCCESS//" + roomName);
                    updateGroupChatUserList(roomName);
                    System.out.println("[Server] 초대 수락 처리 완료: " + responder.nickname);
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] 초대 응답 처리 오류: " + e.getMessage());
        }
    }

    public void handleUserListRequest(CCUser requester, String roomName) {
        try {
            if (chatManager.isUserInRoom(requester, roomName)) {
                updateGroupChatUserList(roomName);
                System.out.println("[Server] 유저 목록 업데이트 전송: " + roomName);
            }
        } catch (Exception e) {
            System.out.println("[Server] 유저 목록 요청 처리 오류: " + e.getMessage());
        }
    }

    // 특정 사용자에게 현재 방 목록 전송
    public void sendGroupChatList(CCUser user) {
        try {
            Set<String> rooms = chatManager.getRoomList();
            StringBuilder roomList = new StringBuilder(GROUP_CHAT_LIST + "//");
            
            if (!rooms.isEmpty()) {
                for (String roomName : rooms) {
                    if (!roomName.trim().isEmpty()) {
                        roomList.append(roomName).append("@");
                    }
                }
                // 마지막 @ 제거
                if (roomList.charAt(roomList.length() - 1) == '@') {
                    roomList.setLength(roomList.length() - 1);
                }
            }
            
            String message = roomList.toString();
            if (user != null && user.dos != null) {
                user.dos.writeUTF(message);
                System.out.println("[Server] 신규 접속자에게 방 목록 전송 (" + user.nickname + "): " + message);
            }
        } catch (IOException e) {
            System.out.println("[Server] 방 목록 전송 실패: " + e.getMessage());
        }
    }
} 