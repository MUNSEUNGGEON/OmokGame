package ui;

import core.Client;
import core.EmoticonManager;

import static core.MessageType.*;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;
import javax.swing.text.*;


public class GroupChatRoom extends JFrame {
    private final Client client;
    private final String roomName;
    private final JTextPane chatPane;
    private final JTextField inputField;
    private final DefaultListModel<String> userListModel;
    private final JList<String> userList;
    private final ThemeManager themeManager;
    private final StyledDocument doc;
    private final SimpleDateFormat timeFormat;
    private Color myMessageBubbleColor = new Color(255, 255, 153);
    private Color otherMessageBubbleColor = new Color(255, 255, 255);
    private String lastSender = "";
    private EmoticonManager emoticonManager = EmoticonManager.getInstance();
    private JPanel userListPanel;

    public GroupChatRoom(Client client, String roomName) {
        this.client = client;
        this.roomName = roomName;
        this.themeManager = client.mf.themeManager;
        this.timeFormat = new SimpleDateFormat("HH:mm");
        
        // 메인 패널 설정
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        themeManager.styleComponent(mainPanel);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // 상단 패널 설정
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        themeManager.styleComponent(topPanel);
        
        // 제목 레이블
        JLabel titleLabel = new JLabel(roomName);
        titleLabel.setFont(themeManager.getMainFont().deriveFont(16f));
        themeManager.styleComponent(titleLabel);
        
        // 상단 우측 버튼 패널
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        themeManager.styleComponent(topRightPanel);
        
        // 참여자 목록 토글 버튼 추가
        JButton userListButton = new JButton("참여자 목록");
        userListButton.addActionListener(e -> toggleUserList());
        themeManager.styleComponent(userListButton);
        
        // 설정 버튼
        JButton settingsButton = new JButton("설정");
        settingsButton.setToolTipText("채팅방 설정");
        settingsButton.addActionListener(e -> openSettings());
        themeManager.styleComponent(settingsButton);
        
        // 나가기 버튼 추가
        JButton leaveButton = new JButton("채팅방 나가기");
        leaveButton.setBackground(new Color(255, 100, 100));
        leaveButton.setForeground(Color.WHITE);
        leaveButton.addActionListener(e -> exitChatRoom());
        topRightPanel.add(leaveButton);
        
        topRightPanel.add(userListButton);
        topRightPanel.add(settingsButton);
        
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        // 채팅 영역
        chatPane = new JTextPane();
        chatPane.setEditable(false);
        doc = chatPane.getStyledDocument();
        JScrollPane chatScroll = new JScrollPane(chatPane);
        themeManager.styleComponent(chatPane);
        themeManager.styleComponent(chatScroll);

        // 입력 패널
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        themeManager.styleComponent(inputPanel);
        
        // 이모티콘 버튼
        JButton emoticonButton = new JButton("이모티콘");
        emoticonButton.setPreferredSize(new Dimension(50, 30));
        emoticonButton.addActionListener(e -> showEmoticonPanel());
        themeManager.styleComponent(emoticonButton);
        
        // 입력 필드
        inputField = new JTextField(20);
        inputField.addActionListener(e -> sendMessage());
        themeManager.styleComponent(inputField);
        
        // 전송 버튼
        JButton sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(60, 30));
        sendButton.addActionListener(e -> sendMessage());
        themeManager.styleComponent(sendButton);
        
        inputPanel.add(emoticonButton);
        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        // 사용자 목록 패널
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userListPanel = createUserListPanel();
        userListPanel.setVisible(false);

        // 레이아웃 구성
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(chatScroll, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(userListPanel, BorderLayout.EAST);

        setContentPane(mainPanel);
        setTitle("그룹 채팅: " + roomName);
        setSize(400, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        initializeStyles();
        setVisible(true);
    }

    private void initializeStyles() {
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
        Style mainStyle = chatPane.addStyle("MainStyle", defaultStyle);
        StyleConstants.setFontFamily(mainStyle, "맑은 고딕");
        StyleConstants.setFontSize(mainStyle, 12);
    }

    private JPanel createUserListPanel() {
        JPanel userListPanel = new JPanel(new BorderLayout(5, 5));
        userListPanel.setPreferredSize(new Dimension(140, 0));
        themeManager.styleComponent(userListPanel);

        // 헤더 패널 수정
        JPanel headerPanel = new JPanel(new BorderLayout(5, 0));
        themeManager.styleComponent(headerPanel);

        // 초대 버튼 생성
        JButton inviteButton = new JButton("초대");
        inviteButton.addActionListener(e -> showInviteDialog());
        themeManager.styleComponent(inviteButton);

        // 갱신 버튼
        JButton refreshButton = new JButton("갱신");
        refreshButton.setToolTipText("새로고침");
        refreshButton.addActionListener(e -> refreshUserList());
        themeManager.styleComponent(refreshButton);

        headerPanel.add(inviteButton, BorderLayout.CENTER);
        headerPanel.add(refreshButton, BorderLayout.EAST);

        userListPanel.add(headerPanel, BorderLayout.NORTH);
        userListPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        return userListPanel;
    }

    private JPanel createMessageBubble(String message, boolean isMine) {
        JPanel bubblePanel = new JPanel(new BorderLayout(5, 0));
        bubblePanel.setBackground(chatPane.getBackground());

        JPanel messagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(isMine ? myMessageBubbleColor : otherMessageBubbleColor);
                g2d.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2d.dispose();
            }
        };
        messagePanel.setOpaque(false);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        messagePanel.add(messageLabel);

        JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
        timeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 10));
        timeLabel.setForeground(Color.GRAY);

        JPanel wrapperPanel = new JPanel(new BorderLayout(5, 0));
        wrapperPanel.setBackground(chatPane.getBackground());

        if (isMine) {
            wrapperPanel.add(timeLabel, BorderLayout.WEST);
            wrapperPanel.add(messagePanel, BorderLayout.EAST);
        } else {
            wrapperPanel.add(messagePanel, BorderLayout.WEST);
            wrapperPanel.add(timeLabel, BorderLayout.EAST);
        }

        bubblePanel.add(wrapperPanel, isMine ? BorderLayout.EAST : BorderLayout.WEST);
        return bubblePanel;
    }

    private void showEmoticonPanel() {
        JPopupMenu popup = new JPopupMenu();
        JPanel emoticonPanel = new JPanel(new GridLayout(0, 4, 5, 5));
        emoticonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        for (String emoticonName : emoticonManager.getEmoticonNames()) {
            ImageIcon emoticon = emoticonManager.getScaledEmoticon(emoticonName, 30, 30);
            if (emoticon != null) {
                JButton btn = new JButton(emoticon);
                btn.setBorderPainted(false);
                btn.setContentAreaFilled(false);
                btn.addActionListener(e -> {
                    client.sendGroupChatMessage(roomName, EMOTICON + "//" + emoticonName);
                    popup.setVisible(false);
                });
                emoticonPanel.add(btn);
            }
        }

        popup.add(emoticonPanel);
        popup.show(inputField, 0, -popup.getPreferredSize().height);
    }

    private void openSettings() {
        new GroupChatSettings(this).setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            client.sendGroupChatMessage(roomName, message);
            inputField.setText("");
        }
    }

    public void receiveMessage(String sender, String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith(EMOTICON + "//")) {
                String emoticonName = message.substring((EMOTICON + "//").length());
                appendEmoticon(sender, emoticonName);
            } else {
                boolean isMine = sender.equals(client.nickname);
                appendMessage(message, isMine, sender);
            }
        });
    }

    private void appendEmoticon(String sender, String emoticonName) {
        try {
            boolean isMine = sender.equals(client.nickname);
            
            // 발신자가 바뀌었을 때만 닉네임 표시
            if (!sender.equals(lastSender)) {
                doc.insertString(doc.getLength(), "\n", null);
                JLabel nicknameLabel = new JLabel(sender);
                nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                StyleConstants.setComponent(chatPane.addStyle("NicknameStyle", null), nicknameLabel);
                doc.insertString(doc.getLength(), " ", chatPane.getStyle("NicknameStyle"));
            }

            // 이모티콘 표시
            ImageIcon emoticon = emoticonManager.getScaledEmoticon(emoticonName, 30, 30);
            if (emoticon != null) {
                JLabel emoticonLabel = new JLabel(emoticon);
                StyleConstants.setComponent(chatPane.addStyle("EmoticonStyle", null), emoticonLabel);
                doc.insertString(doc.getLength(), "\n", null);
                doc.insertString(doc.getLength(), " ", chatPane.getStyle("EmoticonStyle"));
            }

            lastSender = sender;
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String message, boolean isMine, String sender) {
        try {
            // 발신자가 바뀌었을 때만 닉네임 표시
            if (!sender.equals(lastSender)) {
                JPanel nicknamePanel = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
                nicknamePanel.setBackground(chatPane.getBackground());
                
                JLabel nicknameLabel = new JLabel(sender);
                nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                nicknameLabel.setForeground(new Color(100, 100, 100));
                nicknamePanel.add(nicknameLabel);
                
                doc.insertString(doc.getLength(), "\n", null);
                StyleConstants.setComponent(
                    chatPane.addStyle("NicknameStyle", null),
                    nicknamePanel
                );
                doc.insertString(doc.getLength(), " ", chatPane.getStyle("NicknameStyle"));
            }

            // 메시지 버블 추가
            JPanel bubblePanel = createMessageBubble(message, isMine);
            doc.insertString(doc.getLength(), "\n", null);
            StyleConstants.setComponent(
                chatPane.addStyle("MessageStyle", null),
                bubblePanel
            );
            doc.insertString(doc.getLength(), " ", chatPane.getStyle("MessageStyle"));
            
            lastSender = sender;
            chatPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.trim().isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    private void showInviteDialog() {
        String inviteeNickname = JOptionPane.showInputDialog(this,
            "초대할 사용자의 닉네임을 입력하세요:",
            "사용자 초대",
            JOptionPane.PLAIN_MESSAGE);
            
        if (inviteeNickname != null && !inviteeNickname.trim().isEmpty()) {
            client.sendGroupChatInvite(roomName, inviteeNickname.trim());
        }
    }

    private void refreshUserList() {
        client.requestGroupChatUserList(roomName);
    }

    public void updateBackgroundColor(Color color) {
        chatPane.setBackground(color);
        SwingUtilities.invokeLater(() -> {
            updateComponentBackgrounds(chatPane, color);
            chatPane.repaint();
        });
    }

    private void updateComponentBackgrounds(Container container, Color color) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel && !(comp instanceof JTextPane)) {
                if (!comp.getClass().getName().contains("messageBubble")) {
                    comp.setBackground(themeManager.getCurrentMainBgColor());
                }
            }
            if (comp instanceof Container) {
                updateComponentBackgrounds((Container) comp, color);
            }
        }
    }

    public void updateTextColor(Color color) {
        chatPane.setForeground(color);
        SwingUtilities.invokeLater(() -> {
            updateComponentColors(chatPane, color);
            chatPane.repaint();
        });
    }

    private void updateComponentColors(Container container, Color color) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(color);
            }
            if (comp instanceof Container) {
                updateComponentColors((Container) comp, color);
            }
        }
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public Color getMyBubbleColor() {
        return myMessageBubbleColor;
    }

    public Color getOtherBubbleColor() {
        return otherMessageBubbleColor;
    }

    public void updateFont(Font font) {
        chatPane.setFont(font);
        SwingUtilities.invokeLater(() -> {
            Style style = chatPane.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
            StyleConstants.setFontFamily(style, font.getFamily());
            StyleConstants.setFontSize(style, font.getSize());
            chatPane.repaint();
        });
    }

    public void updateMyBubbleColor(Color color) {
        this.myMessageBubbleColor = color;
        chatPane.repaint();
    }

    public void updateOtherBubbleColor(Color color) {
        this.otherMessageBubbleColor = color;
        chatPane.repaint();
    }

    public void applyTheme(ThemeManager.Theme theme) {
        themeManager.applyTheme(theme);
        updateBackgroundColor(themeManager.getCurrentMainBgColor());
        updateTextColor(themeManager.getCurrentTextColor());
    }

    private void exitChatRoom() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "정말로 채팅방을 나가시겠습니까?",
            "채팅방 나가기",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            client.sendMsg(LEAVE_GROUP_CHAT + "//" + roomName);
            dispose();
        }
    }

    // 토글 메서드 추가
    private void toggleUserList() {
        userListPanel.setVisible(!userListPanel.isVisible());
        revalidate();
        repaint();
    }
} 