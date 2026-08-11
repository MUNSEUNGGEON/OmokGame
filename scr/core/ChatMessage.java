package core;

import java.sql.Timestamp;

public class ChatMessage {
    private long messageId;
    private String sender;
    private String receiver;
    private String message;
    private Timestamp sendTime;
    private boolean isRead;
    private boolean isDeleted;
    private String messageType;

    public ChatMessage(long messageId, String sender, String receiver, 
                      String message, Timestamp sendTime, 
                      boolean isRead, boolean isDeleted,
                      String messageType) {
        this.messageId = messageId;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.sendTime = sendTime;
        this.isRead = isRead;
        this.isDeleted = isDeleted;
        this.messageType = messageType;
    }

    // Getters and Setters
    public long getMessageId() { return messageId; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getMessage() { return message; }
    public Timestamp getSendTime() { return sendTime; }
    public boolean isRead() { return isRead; }
    public boolean isDeleted() { return isDeleted; }
    public void setRead(boolean read) { isRead = read; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public String getMessageType() { return messageType; }
}