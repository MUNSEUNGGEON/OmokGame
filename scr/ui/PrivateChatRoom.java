package ui;

// 필요한 패키지 및 클래스 임포트
import core.Client; // 클라이언트 기능을 위한 클래스 임포트
import core.EmoticonManager;

import static core.MessageType.*;  // 상수를 static import
import javax.swing.*; // 스윙 컴포넌트 임포트
import javax.swing.border.AbstractBorder;
import javax.swing.text.*; // 텍스트 스타일링을 위한 패키지 임포트
import java.awt.*; // AWT 패키지 임포트 (레이아웃 및 색상 관련)
import java.awt.event.*; // 이벤트 처리를 위한 패키지 임포트
import java.text.SimpleDateFormat; // 날짜 포맷을 위한 클래스 임포트
import java.util.Date; // 날짜 클래스 임포트
import java.util.Arrays; // 배열 조작을 위한 클래스 임포트
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.nio.file.Files;
import java.awt.Desktop;

// 1:1 채팅방 UI 클래스 정의, JFrame을 상속받아 GUI 창으로 사용
public class PrivateChatRoom extends JFrame {
    private Client client; // 클라이언트 객체 (채팅 서버와 통신)
    private String otherUser; // 상대방 사용자 이름
    private JTextPane chatPane; // 채팅 내역을 표시하는 JTextPane
    private JTextField inputField; // 메시지 입력 필드
    private StyledDocument doc; // JTextPane의 스타일링 문서 모델
    private SimpleDateFormat timeFormat; // 시간 포맷 (시/분 표시)
    private JButton exitButton; // 채팅방 나가기 버튼
    
    // 페이징 관련 변수 (과거 채팅 메시지 로드)
    private int currentPage = 0; // 현재 표시 중인 페이지 번호
    private static final int PAGE_SIZE = 20; // 페이지당 표시할 메시지 수
    
    // 메시지 스타일 관련 상수
    private static final String STYLE_MY_MESSAGE = "MyMessage"; // 내 메시지 스타일 이름
    private static final String STYLE_OTHER_MESSAGE = "OtherMessage"; // 상대 메시지 스타일 이름
    private static final Color MY_MESSAGE_COLOR = new Color(255, 255, 153); // 내 메시지 배경색
    private static final Color OTHER_MESSAGE_COLOR = new Color(255, 255, 255); // 상대 메시지 배경
    
    // 날짜 포맷 및 현재 날짜 문자열
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy년 MM월 dd일"); // 날짜 포맷
    private String currentDateString = ""; // 현재 날짜 문자열 (날짜 구분선 표시를 위한 변수)
    
    // 버블 색상 변수를 static final에서 일반 변수로 변경
    private Color myMessageBubbleColor = new Color(255, 255, 153);  // 기본 내 메시지 버블 색상
    private Color otherMessageBubbleColor = new Color(255, 255, 255);  // 기본 상대방 메시지 버블 색상
    
    // 클래스 멤버 변수로 추가
    private String lastSender = "";  // 마지막 메시지 발신자
    private long lastMessageTime = 0;  // 마지막 메시지 시간
    
    // 메시지 그룹을 관리하기 위한 맵 추가
    private Map<String, List<Component>> messageGroups = new HashMap<>();
    private String currentGroup = null;
    
    // 새로운 채팅방 인스턴스 생성 메서드
    public static PrivateChatRoom create(Client client, String otherUser) {
        return new PrivateChatRoom(client, otherUser); // 인스턴스 생성 후 반환
    }

    // 두 사용자 이름을 정렬하여 고유한 채팅방 키 생성
    public static String generateRoomKey(String user1, String user2) {
        String[] users = {user1, user2}; // 사용자 이름 열 생성
        Arrays.sort(users); // 이름 배열 정렬 (사전순)
        return String.join("-", users); // 정렬된 이름을 "-"로 연결하여 반환
    }

    // 생성자: PrivateChatRoom 객체 생성 및 초기화
    public PrivateChatRoom(Client client, String otherUser) {
        this.client = client; // 클라이언트 객체 초기화
        this.otherUser = otherUser; // 상대방 이름 초기화
        this.lastSender = "";  // 명시적으로 초기화
        this.timeFormat = new SimpleDateFormat("HH:mm"); // 시간 포맷 (시:분)
        
        setTitle("11 채팅 - " + otherUser); // 창 제목 설정
        setSize(415, 600); // 창 크기 설정 (너비 400, 높이 600)
        setLocationRelativeTo(null); // 창을 화면 중앙에 표시
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 닫기 버튼 클릭 시 창 닫기
        
        initializeComponents(); // GUI 컴포넌트 초기화 메서드 호출
        initializeStyles(); // 채팅 메시지 스타일 초기화
        addLoadMoreButton(); // "전 메시지 보기" 버튼 추가
        loadInitialChatHistory(); // 초기 채팅 내역 로드
        
        // 창 닫기 이벤트 처리 (채팅방 나가기 처리)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                exitChatRoom(); // 채팅방 나가기 메서드 호출
            }
        });
        
        setVisible(true); // 창 표시
    }
    
    private void initializeComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topPanel = new JPanel(new BorderLayout(5, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 상단 패널에 버튼들을 담을 우측 패널 추가
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        // 설정 버튼 추가
        JButton settingsButton = new JButton("⚙️");
        settingsButton.setToolTipText("채팅방 설정");
        settingsButton.setFocusPainted(false);
        settingsButton.addActionListener(e -> openSettings());

        exitButton = new JButton("채팅방 나가기");
        exitButton.setBackground(new Color(255, 100, 100));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> exitChatRoom());

        topRightPanel.add(settingsButton);
        topRightPanel.add(exitButton);

        JLabel titleLabel = new JLabel(otherUser + "님과의 대화");
        titleLabel.setFont(new Font("은 고딕", Font.BOLD, 14));

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(topRightPanel, BorderLayout.EAST);

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        doc = chatPane.getStyledDocument();
        appendDateSeparator(new Date());

        JScrollPane scrollPane = new JScrollPane(chatPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5)); // FlowLayout으로 설정
        JButton emoticonButton = new JButton("😊");
        emoticonButton.setPreferredSize(new Dimension(50, 30)); // 버튼 크기 설정
        emoticonButton.addActionListener(e -> showEmoticonPanel());

        inputField = new JTextField(20); // 적절한 길이 설정
        inputField.addActionListener(e -> sendMessage());

        JButton sendButton = new JButton("전송");
        sendButton.setPreferredSize(new Dimension(60, 30)); // 버튼 크기 설정
        sendButton.addActionListener(e -> sendMessage());
        
        inputPanel.add(emoticonButton);
        
                JButton fileButton = new JButton("📎");
                fileButton.setToolTipText("파일 전송");
                fileButton.addActionListener(e -> selectAndSendFile());
                inputPanel.add(fileButton);
        inputPanel.add(inputField);
        inputPanel.add(sendButton);

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    
    // 채팅창의 메시지 스타일 초기화 메서드
    private void initializeStyles() {
        Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE); // 기본 스타일 가져오
        
        // 내 메시지 스타일 설정
        Style myMessageStyle = chatPane.addStyle(STYLE_MY_MESSAGE, defaultStyle); // 내 메시지 스타일 추가
        StyleConstants.setAlignment(myMessageStyle, StyleConstants.ALIGN_RIGHT); // 오른쪽 정렬
        StyleConstants.setBackground(myMessageStyle, MY_MESSAGE_COLOR); // 배경색 설정
        
        // 상대방 메시지 스타일 설정
        Style otherMessageStyle = chatPane.addStyle(STYLE_OTHER_MESSAGE, defaultStyle); // 상대 메시지 스타일 추가
        StyleConstants.setAlignment(otherMessageStyle, StyleConstants.ALIGN_LEFT); // 왼쪽 정렬
        StyleConstants.setBackground(otherMessageStyle, OTHER_MESSAGE_COLOR); // 배경색 설정
    }
    
    // 메시지 전송 메서드
    private void sendMessage() {
        String message = inputField.getText().trim(); // 입력 필드의 메시지 가져와 공백 제거
        if (!message.isEmpty()) { // 메시지가 비어있지 않다면
            client.sendMsg(PRIVATE_CHAT +"//" + otherUser + "//" + message); // 클라이언트를 통해 서버로 메시지 전송
            appendMessage(message, true); // 내 메시지로 채팅창에 추가
            inputField.setText(""); // 입력 필드 초기화
        }
    }
    
    // 메시지 수신 시 호출되는 메서드
    public void receiveMessage(String message) {
        appendMessage(message, false); // 상대방의 메시지로 표시
    }
    
    // 채팅창에 메시지를 추가하는 메서드
    public void appendMessage(String message, boolean isMine) {
        SwingUtilities.invokeLater(() -> {
            try {
                String sender = isMine ? client.nickname.toString() : otherUser;
                
                // 새로운 발신자인 경우에만 닉네임 표시
                if (!sender.equals(lastSender)) {
                    // 닉네임 패널 추가
                    JPanel nicknamePanel = new JPanel(new FlowLayout(isMine ? FlowLayout.RIGHT : FlowLayout.LEFT));
                    nicknamePanel.setBackground(chatPane.getBackground());
                    
                    JLabel nicknameLabel = new JLabel(sender);
                    nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                    nicknameLabel.setForeground(new Color(100, 100, 100));
                    nicknamePanel.add(nicknameLabel);
                    
                    // 닉네임 추가
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
                
                // 마지막 발신자 업데이트
                lastSender = sender;
                
                chatPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    // 메시지 버블만 ���성하는 메서드로 분리
    private JPanel createMessageBubble(String message, boolean isMine) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setBackground(chatPane.getBackground());

        JPanel bubblePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isMine ? myMessageBubbleColor : otherMessageBubbleColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
            }
        };
        bubblePanel.setLayout(new BorderLayout());
        bubblePanel.setOpaque(false);

        JLabel messageLabel = new JLabel(message);
        messageLabel.setForeground(chatPane.getForeground());
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        bubblePanel.add(messageLabel);

        JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
        timeLabel.setFont(timeLabel.getFont().deriveFont(10f));
        timeLabel.setForeground(new Color(150, 150, 150));

        if (isMine) {
            panel.add(timeLabel, BorderLayout.WEST);
            panel.add(bubblePanel, BorderLayout.EAST);
        } else {
            panel.add(bubblePanel, BorderLayout.WEST);
            panel.add(timeLabel, BorderLayout.EAST);
        }

        return panel;
    }


    // 둥근 모양의 테두리를 설정하는 클래스
    class RoundedBorder extends AbstractBorder {
        private int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground().darker());
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    // 이전 메시지 로드 버튼을 추가하는 메서드
    private void addLoadMoreButton() {
        JButton loadMoreBtn = new JButton("이전 메시지 보기"); // 버튼 생성
        loadMoreBtn.addActionListener(e -> { // 버튼 클릭 시 동작 설정
            currentPage++; // 페이지 증가
            loadChatHistory(currentPage); // 해당 페이지의 채팅 내역 로드
        });
    }

    // 채팅 내역을 서버로부터 요청하는 메서드
    private void loadChatHistory(int page) {
        client.sendMsg(LOAD_CHAT + "//" + otherUser + "//" + page); // 채팅 내역 요청 메시지 전송
        System.out.println("[Client] 채팅 내역 요청: " + otherUser + ", 페이지: " + page); // 로그 출력
    }

    // 초기 채팅 역 로드 메서드 (첫 페이지 로드)
    private void loadInitialChatHistory() {
        currentPage = 0; // 현재 페이지를 0로 설정
        loadChatHistory(currentPage); // 첫 페이지 로드 요청
    }

    // 채팅방 나가기 메서드
    private void exitChatRoom() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "채팅방을 나가시겠습니까?", // 확인 메시지
            "채팅방 나가기", // 이얼로그 제목
            JOptionPane.YES_NO_OPTION // 예/아니오 옵션 제공
        );
        
        if (confirm == JOptionPane.YES_OPTION) { // "예" 선택 시
            client.sendMsg("CHAT_EXIT//" + otherUser + "//" + client.nickname); // 클라이언트에 나가기 메시지 전송
            dispose(); // 창 닫기
            client.removeChatRoom(otherUser); // 클라이언트의 채팅방 목록에서 제거
        }
    }

    // 상대방이 채팅방에서 나갔을 때 호출되는 메서드
    public void partnerExited(String nickname) {
        SwingUtilities.invokeLater(() -> { // UI 업데이트를 위해 이벤트 큐에 추가
            appendSystemMessage(nickname + "님이 채팅방을 나갔습니다."); // 시스템 메시지 추가
        });
    }

    // 시스템 메시를 채팅창에 가하는 서드
    private void appendSystemMessage(String message) {
        try {
            JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER)); // 시스템 메시지 패널 생성
            messagePanel.setBackground(chatPane.getBackground()); // 배경색 설정
            
            JLabel messageLabel = new JLabel(message); // 메시지를 표시할 레이블 생성
            messageLabel.setForeground(Color.BLACK); // 글자색 설정
            messageLabel.setFont(messageLabel.getFont().deriveFont(Font.PLAIN, 12f)); // 폰트 스타일 설정
            
            messagePanel.add(messageLabel); // 패널에 메시지 레이블 추가
            
            doc.insertString(doc.getLength(), "\n", null); // 문서에 줄바꿈 추가
            StyleConstants.setComponent(
                chatPane.addStyle("SystemStyle", null), // 스타일 생성 및 설정
                messagePanel // 생성된 패널을 컴포넌트로 추가
            );
            doc.insertString(doc.getLength(), " ", chatPane.getStyle("SystemStyle")); // 공백 추가
            chatPane.setCaretPosition(doc.getLength()); // 커서를 문서 끝으로 이동
            
        } catch (BadLocationException e) {
            e.printStackTrace(); // 예외 출력
        }
    }

    // 버블로부터 수신한 채 내역을 표시하는 메서드
    public void displayChatHistory(String messageData) {
        try {
            String[] parts = messageData.split("//"); // 수신한 데이터 분할 (발신자, 메시지, 타임스탬프)
            if (parts.length >= 3) {
                String sender = parts[0]; // 발신자 이름
                String message = parts[1]; // 메시지 내용
                long timestamp = Long.parseLong(parts[2]); // 타임스탬프
                
                boolean isMine = sender.equals(client.nickname); // 발신자가 나인지 확인
                createMessageBubble(message, isMine, new Date(timestamp), true); // 메시지 버블 생성
            }
        } catch (Exception e) {
            System.out.println("[Client] 메시지 표시 실패: " + e.toString()); // 실패 로그 출력
            e.printStackTrace(); // 예외 출력
        }
    }

    // 메시지 버블 생성 및 표시 메서드
    private void createMessageBubble(String message, boolean isMine, Date timestamp, boolean isHistorical) {
        try {
            JPanel messagePanel = new JPanel(); // 메시지 패널 생성
            messagePanel.setLayout(new BorderLayout(5, 2)); // 레이아웃 설
            messagePanel.setBackground(chatPane.getBackground()); // 배경색 설정
            
            JPanel bubblePanel = new JPanel(); // 메시지 버블 패널 생성
            bubblePanel.setLayout(new BorderLayout()); // 레아웃 설정
            bubblePanel.setBackground(isMine ? MY_MESSAGE_COLOR : OTHER_MESSAGE_COLOR); // 내/상대 메시지에 따라 배경색 설정
            bubblePanel.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(15), // 모서리 반경 15로 둥근 테두리 설정
                BorderFactory.createEmptyBorder(8, 12, 8, 12) // 여백 설정
            ));
            
            JLabel messageLabel = new JLabel(message); // 메시지 내용 레이블 생성
            messageLabel.setForeground(Color.BLACK); // 글자색 설정
            bubblePanel.add(messageLabel, BorderLayout.CENTER); // 메시지 레이블 추가
            
            JLabel timeLabel = new JLabel(timeFormat.format(timestamp)); // 시�� 레이블 생성
            timeLabel.setFont(timeLabel.getFont().deriveFont(10f)); // 폰트 크기 설정
            timeLabel.setForeground(Color.BLACK); // 글자색 설정
            
            // 내 메시지일 때 오른쪽, 상대 메시지일 때 왼쪽에 표시
            if (isMine) {
                messagePanel.add(timeLabel, BorderLayout.WEST); // 시간 왼쪽 정렬
                messagePanel.add(bubblePanel, BorderLayout.EAST); // 메시지 오른쪽 정렬
            } else {
                messagePanel.add(bubblePanel, BorderLayout.WEST); // 메지 왼쪽 정렬
                messagePanel.add(timeLabel, BorderLayout.EAST); // 시간은 오른쪽 정렬
            }
            
            if (isHistorical) {
                doc.insertString(0, "\n", null); // 이전 메시지는 문서 시작에 추가
                StyleConstants.setComponent(
                    chatPane.addStyle("MessageStyle", null),
                    messagePanel
                );
                doc.insertString(0, " ", chatPane.getStyle("MessageStyle"));
            } else {
                doc.insertString(doc.getLength(), "\n", null); // 새 메시지는 문서 끝에 추가
                StyleConstants.setComponent(
                    chatPane.addStyle("MessageStyle", null),
                    messagePanel
                );
                doc.insertString(doc.getLength(), " ", chatPane.getStyle("MessageStyle"));
                chatPane.setCaretPosition(doc.getLength()); // 커서를 문서 끝으로 이동
            }
            
        } catch (BadLocationException e) {
            System.out.println("[Client] 메시지 버블 생성 실패: " + e.toString()); // 실패 로그 출력
            e.printStackTrace(); // 예외 출력
        }
    }

    // 날짜 구분선을 채팅창에 추가하는 메서드
    private void appendDateSeparator(Date messageDate) {
        String messageDateString = dateFormat.format(messageDate); // 날짜 포맷 설정
        
        SwingUtilities.invokeLater(() -> { // UI 갱신을 위해 이벤트 큐에 추가
            try {
                JPanel separatorPanel = new JPanel(new BorderLayout(10, 0)); // 구선 패널 생성
                separatorPanel.setBackground(chatPane.getBackground()); // 배경색 설정
                
                JSeparator leftLine = new JSeparator(); // 좌측 라인 생성
                leftLine.setForeground(new Color(200, 200, 200)); // 색상 설정
                
                JSeparator rightLine = new JSeparator(); // 우측 라인 생성
                rightLine.setForeground(new Color(200, 200, 200)); // 색상 설정
                
                JLabel dateLabel = new JLabel(messageDateString); // 날짜 레이블 생성
                dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12)); // 폰트 설정
                dateLabel.setForeground(new Color(130, 130, 130)); // 자색 설정
                dateLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10)); // 여백 설정
                dateLabel.setHorizontalAlignment(SwingConstants.CENTER); // 가운데 렬
                
                separatorPanel.add(leftLine, BorderLayout.WEST); // 패널에 좌측 라인 추가
                separatorPanel.add(dateLabel, BorderLayout.CENTER); // 패널에 날짜 레이블 추가
                separatorPanel.add(rightLine, BorderLayout.EAST); // 패널에 우측 라인 추가
                
                separatorPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20)); // 패널 여백 설정
                
                doc.insertString(doc.getLength(), "\n", null); // 문서에 줄바꿈 추가
                StyleConstants.setComponent(
                    chatPane.addStyle("DateSeparator", null),
                    separatorPanel
                );
                doc.insertString(doc.getLength(), " ", chatPane.getStyle("DateSeparator"));
                
            } catch (BadLocationException e) {
                e.printStackTrace(); // 예외 출력
            }
        });
    }

    private void showEmoticonPanel() {
        JPopupMenu emoticonMenu = new JPopupMenu();
        JPanel emoticonPanel = new JPanel(new GridLayout(0, 4, 5, 5)); // 4열로 이모티콘 배치
        
        EmoticonManager emoticonManager = EmoticonManager.getInstance();
        String[] emoticonNames = emoticonManager.getEmoticonNames();
        
        for (String name : emoticonNames) {
            ImageIcon scaledIcon = emoticonManager.getScaledEmoticon(name, 40, 40); // 미리보기 크기
            if (scaledIcon != null) {
                JButton button = new JButton(scaledIcon);
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
                button.setFocusPainted(false);
                button.addActionListener(e -> sendEmoticon(name));
                emoticonPanel.add(button);
            }
        }
        
        JScrollPane scrollPane = new JScrollPane(emoticonPanel);
        scrollPane.setPreferredSize(new Dimension(200, 200));
        emoticonMenu.add(scrollPane);
        
        // 입력 필드 위에 이모티콘 패널 표시
        emoticonMenu.show(inputField, 0, -emoticonMenu.getPreferredSize().height);
    }

    private void sendEmoticon(String emoticonName) {
        // 서버로 이모티콘 메시지 전송
        client.sendMsg(EMOTICON + "//" + otherUser + "//" + emoticonName);
        // 내 채팅창에도 이모티콘 표시
        appendEmoticon(emoticonName, true);
    }

    // JLabel용 메시지 패널 생성 메서드 오버로딩
    private JPanel createMessagePanel(JLabel content, boolean isMine) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 0));
        panel.setBackground(chatPane.getBackground());

        // 시지를 포함하는 패널 생성
        JPanel messageBubble = new JPanel(new BorderLayout());
        messageBubble.setBackground(isMine ? MY_MESSAGE_COLOR : OTHER_MESSAGE_COLOR);
        messageBubble.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        
        content.setForeground(chatPane.getForeground());
        messageBubble.add(content, BorderLayout.CENTER);
        
        JLabel timeLabel = new JLabel(timeFormat.format(new Date()));
        timeLabel.setFont(timeLabel.getFont().deriveFont(10f));
        timeLabel.setForeground(chatPane.getForeground());
        
        JPanel contentPanel = new JPanel(new BorderLayout(5, 0));
        contentPanel.setBackground(chatPane.getBackground());
        
        if (isMine) {
            contentPanel.add(timeLabel, BorderLayout.WEST);
            contentPanel.add(messageBubble, BorderLayout.EAST);
            panel.add(contentPanel, BorderLayout.EAST);
        } else {
            contentPanel.add(messageBubble, BorderLayout.WEST);
            contentPanel.add(timeLabel, BorderLayout.EAST);
            panel.add(contentPanel, BorderLayout.WEST);
        }
        
        return panel;
    }

    // appendEmoticon 메서드를 public으로 변경
    public void appendEmoticon(String emoticonName, boolean isMine) {
        SwingUtilities.invokeLater(() -> {
            try {
                ImageIcon scaledIcon = EmoticonManager.getInstance().getScaledEmoticon(emoticonName, 100, 100);
                if (scaledIcon != null) {
                    JLabel emoticonLabel = new JLabel(scaledIcon);
                    JPanel emoticonPanel = createMessagePanel(emoticonLabel, isMine);
                    
                    doc.insertString(doc.getLength(), "\n", null);
                    StyleConstants.setComponent(
                        chatPane.addStyle("EmoticonStyle", null),
                        emoticonPanel
                    );
                    doc.insertString(doc.getLength(), " ", chatPane.getStyle("EmoticonStyle"));
                    chatPane.setCaretPosition(doc.getLength());
                }
            } catch (Exception e) {
                System.out.println("[Client] 이모티콘 표시 실패: " + e.getMessage());
            }
        });
    }

    // 설정 창을 여는 메서드
    private void openSettings() {
        ChatRoomSettings settings = new ChatRoomSettings(this);
        settings.setLocationRelativeTo(this);
        settings.setVisible(true);
    }

    // 채팅방 스타일 업데이트 메서드들
    public void updateBackgroundColor(Color color) {
        // 채팅창 배경색 설정
        chatPane.setBackground(color);
        
        // 모든 컴포넌트의 배경색을 재귀적으로 업데이트
        SwingUtilities.invokeLater(() -> {
            updateComponentBackgrounds(chatPane, color);
            chatPane.repaint();
        });
    }

    // 새로 추가할 메서드: 모든 하위 컴포넌트의 배경색을 재귀적으로 업데이트
    private void updateComponentBackgrounds(Container container, Color color) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel && !(comp instanceof JTextPane)) {
                // 메시지 버블 패널은 제외하고 배경색 업��이트
                if (!comp.getClass().getName().contains("messageBubble")) {
                    comp.setBackground(color);
                }
            }
            if (comp instanceof Container) {
                updateComponentBackgrounds((Container) comp, color);
            }
        }
    }

    public void updateTextColor(Color color) {
        // 전체 채팅창의 기본 텍스트 색상 설정
        chatPane.setForeground(color);
        
        // 모든 컴포넌트를 순회하면서 텍스트 색상 업데이트
        SwingUtilities.invokeLater(() -> {
            updateComponentColors(chatPane, color);
            chatPane.repaint();
        });
    }

    // 새로 추가할 메서드: 모든 하위 컴포넌트의 색상을 재귀적으로 업데이트
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

    public void updateFont(Font font) {
        chatPane.setFont(font);
        // 모든 메시지에 새 폰트 적용
        Style style = chatPane.getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(style, font.getFamily());
        StyleConstants.setFontSize(style, font.getSize());
        
        chatPane.repaint();
    }

    // 버블 색상 변경 메서드 추가
    public void updateMyBubbleColor(Color color) {
        this.myMessageBubbleColor = color;
        chatPane.repaint();
    }

    public void updateOtherBubbleColor(Color color) {
        this.otherMessageBubbleColor = color;
        chatPane.repaint();
    }

    // 파일 선택 및 전송 메서드
    private void selectAndSendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                // 파일 크기 체크 (예: 100MB 제한)
                if (file.length() > 100 * 1024 * 1024) {
                    JOptionPane.showMessageDialog(this, 
                        "파일 크기가 너무 큽니다 (100MB 이하만 가능)", 
                        "전송 실패", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                byte[] fileData = Files.readAllBytes(file.toPath());
                int chunkSize = 8192; // 8KB chunks
                int totalChunks = (int) Math.ceil(fileData.length / (double) chunkSize);

                // 전송 시작 메시지
                client.sendMsg(FILE_TRANSFER_START + "//" + otherUser + "//" + 
                             file.getName() + "//" + totalChunks);

                // 청크 단위로 전송
                for (int i = 0; i < totalChunks; i++) {
                    int start = i * chunkSize;
                    int end = Math.min(start + chunkSize, fileData.length);
                    byte[] chunk = Arrays.copyOfRange(fileData, start, end);
                    String base64Chunk = Base64.getEncoder().encodeToString(chunk);

                    client.sendMsg(FILE_TRANSFER_DATA + "//" + otherUser + "//" + 
                                 file.getName() + "//" + i + "//" + 
                                 totalChunks + "//" + base64Chunk);
                    
                    // 진행상황 표시
                    appendMessage(String.format("파일 전송 중: %d/%d", i+1, totalChunks), true);
                }

                // 전송 완료 메시지
                client.sendMsg(FILE_TRANSFER_END + "//" + otherUser + "//" + file.getName());
                appendMessage("파일 전송 완료: " + file.getName(), true);

            } catch (IOException e) {
                appendMessage("파일 전송 실패: " + e.getMessage(), true);
                e.printStackTrace();
            }
        }
    }

    // 파일 수신 메서드
    public void receiveFile(String senderNickname, String fileName, String base64Data) {
        try {
            // downloads 폴더 생성
            File downloadDir = new File("downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdir();
            }
            
            // 파일 저장
            File outputFile = new File(downloadDir, fileName);
            byte[] fileData = Base64.getDecoder().decode(base64Data);
            Files.write(outputFile.toPath(), fileData);
            
            // 파일 수신 완료 메시지 표시
            appendMessage("파일 수신 완료: " + fileName, false);
            
            // 파일 열기 옵션 제공
            int option = JOptionPane.showConfirmDialog(this,
                fileName + " 파일을 여시겠습니까?",
                "파일 수신 완료",
                JOptionPane.YES_NO_OPTION);
                
            if (option == JOptionPane.YES_OPTION) {
                // 시스템 기본 프로그램으로 파일 열기
                Desktop.getDesktop().open(outputFile);
            }
            
        } catch (IOException e) {
            appendMessage("파일 수신 실패: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    // 파일 청크 수신 메서드
    public void receiveFileChunk(String sender, String fileName, String base64Data, 
                            int chunkIndex, int totalChunks) {
        try {
            // downloads 폴더 생성
            File downloadDir = new File("downloads");
            if (!downloadDir.exists()) {
                downloadDir.mkdir();
            }

            // 파일 저장
            File outputFile = new File(downloadDir, fileName);
            byte[] chunk = Base64.getDecoder().decode(base64Data);
            
            // 파일에 데이터 추가 (append 모드)
            try (FileOutputStream fos = new FileOutputStream(outputFile, true)) {
                fos.write(chunk);
            }

            // 마지막 청크인 경우
            if (chunkIndex == totalChunks - 1) {
                appendMessage("파일 수신 완료: " + fileName, false);
            }
        } catch (IOException e) {
            appendMessage("파일 수신 실패: " + e.getMessage(), false);
            e.printStackTrace();
        }
    }

    // 메시지 처리 메서드 (기존 handleMessage 메서드 수정)
    public void processMessage(String msg) {  // handleMessage를 processMessage로 변경
        String[] tokens = msg.split("//");
        String messageType = tokens[0];

        switch (messageType) {
            case FILE_TRANSFER_START:
                // 파일 전송 시작 메시지
                String senderStart = tokens[1];
                String fileNameStart = tokens[2];
                appendMessage(senderStart + "님이 파일을 전송합니다: " + fileNameStart, false);
                break;
            
            case FILE_TRANSFER_DATA:
                // 파일 데이터 수신
                String senderData = tokens[1];
                String fileNameData = tokens[2];
                String chunkIndex = tokens[3];
                String totalChunks = tokens[4];
                String base64Data = tokens[5];
                
                // 파일 데이터 저장
                receiveFileChunk(senderData, fileNameData, base64Data, 
                               Integer.parseInt(chunkIndex), 
                               Integer.parseInt(totalChunks));
                
                // 진행 상황 표시
                appendMessage("파일 수신 중: " + fileNameData + 
                            " (" + (Integer.parseInt(chunkIndex) + 1) + "/" + 
                            totalChunks + ")", false);
                break;
                
            case FILE_TRANSFER_END:
                // 파일 전송 완료 메시지
                String senderEnd = tokens[1];
                String fileNameEnd = tokens[2];
                appendMessage(senderEnd + "님의 파일 전송이 완료되었습니다: " + fileNameEnd, false);
                
                // 파일 열기 옵션 제공
                showFileOpenDialog(fileNameEnd);
                break;
                
            default:
                // 일반 채팅 메시지 처리
                String sender = tokens[1];
                String content = tokens[2];
                appendMessage(content, sender.equals(client.getNickname()));
                break;
        }
    }

    // 파일 열기 대화상자 표시
    public void showFileOpenDialog(String fileName) {
        int option = JOptionPane.showConfirmDialog(this,
            fileName + " 파일을 여시겠습니까?",
            "파일 수신 완료",
            JOptionPane.YES_NO_OPTION);
            
        if (option == JOptionPane.YES_OPTION) {
            try {
                File file = new File("downloads/" + fileName);
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                }
            } catch (IOException e) {
                appendMessage("파일 열기 실패: " + e.getMessage(), false);
            }
        }
    }
}
