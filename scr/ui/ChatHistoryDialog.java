package ui;

import core.Client;
import static core.MessageType.*;
import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatHistoryDialog extends JDialog {
	private static final long serialVersionUID = 1L;
    private JTextPane historyPane;
    private Client client;
    private String otherUser;
    private int currentPage = 0;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    
    public ChatHistoryDialog(JFrame parent, Client client, String otherUser) {
        super(parent, "대화 내역 - " + otherUser, true);
        this.client = client;
        this.otherUser = otherUser;
        
        setSize(400, 500);
        setLocationRelativeTo(parent);
        initComponents();
        loadChatHistory(currentPage);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        
        // 대화 내역을 표시할 텍스트 영역
        historyPane = new JTextPane();
        historyPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(historyPane);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton loadMoreButton = new JButton("이전 메시지 더 보기");
        JButton closeButton = new JButton("닫기");
        
        loadMoreButton.addActionListener(e -> {
            currentPage++;
            loadChatHistory(currentPage);
        });
        
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(loadMoreButton);
        buttonPanel.add(closeButton);
        
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadChatHistory(int page) {
        client.sendMsg(LOAD_CHAT + "//" + otherUser + "//" + page);
    }
    
    public void displayChatHistory(String messageData) {
        SwingUtilities.invokeLater(() -> {
            try {
                String[] parts = messageData.split("//");
                if (parts.length >= 3) {
                    String sender = parts[0];
                    String message = parts[1];
                    long timestamp = Long.parseLong(parts[2]);
                    
                    String timeStr = dateFormat.format(new Date(timestamp));
                    String displayName = sender.equals(client.nickname) ? "나" : sender;
                    
                    // 메시지 포맷팅
                    String formattedMessage = String.format("[%s] %s: %s\n", 
                        timeStr, displayName, message);
                    
                    // 기존 텍스트의 앞에 새 메시지 추가
                    historyPane.setText(formattedMessage + historyPane.getText());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}