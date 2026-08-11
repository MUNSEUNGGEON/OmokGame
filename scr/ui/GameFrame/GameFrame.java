package ui.GameFrame;

import core.*;
import ui.ThemeManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import static core.MessageType.*;  // 상수를 static import

public class GameFrame extends JFrame {

    /* UI 구성 요소 */

	private static final long serialVersionUID = 1L;
	// 패널들: UI 레이아웃을 구성하기 위해 사용
    JPanel basePanel = new JPanel(new BorderLayout());
    JPanel centerPanel = new JPanel();
    JPanel eastPanel = new JPanel();
    JPanel chatPanel = new JPanel();
    JPanel chatInputPanel = new JPanel(new BorderLayout());

    // 방에 참가한 사용자 목록을 보여주는 리스트
    public JList<String> userList = new JList<>();

    // 다양한 UI 정보를 표시하는 라벨
    public JLabel la1 = new JLabel();
    public JLabel la2 = new JLabel();
    public JLabel userListL = new JLabel("참가자 목록");
    public JLabel enableL = new JLabel();
    public JLabel hostProfileLabel = new JLabel(); // 방 생성자의 프로필 라벨
    public JLabel opponentProfileLabel = new JLabel(); // 입장 사람의 프로필 라벨

    // 사용자 액션을 위한 버튼들
    JButton searchBtn = new JButton("전적검색");
    JButton loseBtn = new JButton("기권하기");
    JButton sendChatBtn = new JButton("전송");
    JButton settingsBtn = new JButton("채팅방 설정");
    JButton startGameBtn = new JButton("게임 시작");
    JButton readyBtn = new JButton("준비");
    JButton exitRoomBtn = new JButton("방 나가기");
    JComboBox<String> emojiComboBox; // 이모티콘 선택을 위한 콤보박스
    JButton replayBtn = new JButton("복기");

    // 채팅 구성 요소
    JTextArea chatArea = new JTextArea(); // 채팅 메시지 표시
    JTextField chatInput = new JTextField(); // 채팅 메시지 입력 필드

    String selUser; // 선택된 사용자
    public String dc = ""; // 돌 색깔
    int col; // 돌 색깔 코드 (1=검정, 2=흰색)
    
    // 오목 게임 판 배열 (20x20 크기)
    public int omok[][] = new int[20][20]; 
    public boolean enable = false; // 현재 돌을 둘 수 있는지 여부

    Client c = null; // 서버와의 연결을 위한 클라이언트 객체

    ThemeManager themeManager;

    private Room myRoom; // Room 객체 참조 추가
    
    private boolean isReady = false;  // 준비 상태 추가
    
    private boolean isGameStarted = false; // 게임 시작 상태를 추적하는 변수 추가
    
    private boolean isSpectator = false;
    
    private MouseListener mouseListener; // 필드 추가

    // 마지막 수의 위치를 저장하기 위한 변수 추가
    private Point lastMove = null;

    // Room 객체 설정을 위한 메서드 추가
    public void setRoom(Room room) {
        this.myRoom = room;
    }

    // GameFrame 생성자
    public GameFrame(Client _c) {
        c = _c; // 클라이언트 객체 초기화

        this.themeManager = c.mf.themeManager;
        
        // 기본 프레임 설정
        setTitle("오목 게임");
        setSize(900, 620);
        setResizable(false);
        setLocationRelativeTo(null);

        /* 버튼 이벤트 리스너 설정 */
        ButtonListener bl = new ButtonListener();
        searchBtn.setBounds(12, 265, 110, 30);
        searchBtn.addActionListener(bl);
        sendChatBtn.addActionListener(bl);
        loseBtn.setBounds(12, 300, 110, 30);
        loseBtn.addActionListener(bl);
        settingsBtn.setBounds(127, 300, 110, 30);
        settingsBtn.addActionListener(bl);
        startGameBtn.addActionListener(bl);
        readyBtn.addActionListener(bl);
        replayBtn.setBounds(127, 230, 110, 30);
        replayBtn.addActionListener(bl);

        /* 이모티콘 콤보박스 설정 */
        emojiComboBox = new JComboBox<>(new String[]{"😃", "😄", "😊", "😉", "😍", "😘", "😜", "😎", "😇", "😂", "😭", "😱", "😡"});
        emojiComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setHorizontalAlignment(JLabel.CENTER);
                label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                return label;
            }
        });
        emojiComboBox.setPreferredSize(new Dimension(60, 30));
        emojiComboBox.addActionListener(e -> chatInput.setText(chatInput.getText() + emojiComboBox.getSelectedItem().toString()));

        /* 사용자 리스트 크기 설정 */
        userList.setBounds(53, 143, 140, 50);
        userList.setPreferredSize(new Dimension(140, 50));

        /* 라벨 크기 설정 */
        la1.setBounds(0, 5, 250, 30);
        la1.setPreferredSize(new Dimension(250, 30));
        userListL.setBounds(53, 120, 80, 20);
        userListL.setPreferredSize(new Dimension(80, 20));
        userListL.setHorizontalAlignment(JLabel.LEFT);
        la2.setBounds(90, 40, 155, 20);
        la2.setPreferredSize(new Dimension(155, 20));
        
        /* 프로필 라벨 설정 */
        hostProfileLabel.setBounds(12, 10, 100, 100);
        hostProfileLabel.setPreferredSize(new Dimension(100, 100));
        hostProfileLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        opponentProfileLabel.setBounds(137, 10, 100, 100);
        opponentProfileLabel.setPreferredSize(new Dimension(100, 100));
        opponentProfileLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        /* 버튼 크기 설정 */
        searchBtn.setPreferredSize(new Dimension(110, 30));
        sendChatBtn.setPreferredSize(new Dimension(60, 30));
        loseBtn.setPreferredSize(new Dimension(110, 30));
        settingsBtn.setPreferredSize(new Dimension(110, 30));
        startGameBtn.setPreferredSize(new Dimension(110, 30));
        readyBtn.setPreferredSize(new Dimension(110, 30));
        replayBtn.setPreferredSize(new Dimension(110, 30));

        /* 채팅 컴포넌트 설정 */
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBounds(0, 0, 240, 190);
        chatScrollPane.setPreferredSize(new Dimension(240, 190));
        chatInput.setPreferredSize(new Dimension(150, 30));

        /* 패널 구성 */
        setContentPane(basePanel);
        centerPanel.setPreferredSize(new Dimension(600, 620));
        centerPanel.setLayout(new FlowLayout());
        eastPanel.setPreferredSize(new Dimension(260, 620));
        centerPanel.setBackground(new Color(206, 167, 61));
        centerPanel.setLayout(null);
        basePanel.add(centerPanel, BorderLayout.CENTER);
        basePanel.add(eastPanel, BorderLayout.EAST);
        eastPanel.setLayout(null);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        /* 동쪽 패널에 컴포넌트 추가 */
        eastPanel.add(readyBtn);
        eastPanel.add(startGameBtn);
        eastPanel.add(replayBtn);
        readyBtn.setBounds(12, 230, 110, 30);
        startGameBtn.setBounds(127, 230, 110, 30);
        replayBtn.setBounds(127, 230, 110, 30);
        eastPanel.add(hostProfileLabel);
        eastPanel.add(opponentProfileLabel);
        eastPanel.add(la1);
        eastPanel.add(userListL);
        eastPanel.add(la2);
        eastPanel.add(userList);
        
        enableL.setBounds(53, 192, 150, 30);
        enableL.setPreferredSize(new Dimension(150, 30));
        enableL.setHorizontalAlignment(JLabel.CENTER);
        enableL.setForeground(Color.RED);
        eastPanel.add(enableL);
        enableL.setText("게임을 시작합니다. 돌 둘 수 있습니다.");
        enableL.setText("준비 완료. 상대를 기다리는 중...");
        eastPanel.add(searchBtn);
        
        /* 방 나가 버튼 설정 */
        exitRoomBtn.setBounds(127, 265, 110, 30);
        exitRoomBtn.addActionListener(bl);
        exitRoomBtn.setPreferredSize(new Dimension(110, 30));
        eastPanel.add(exitRoomBtn);
        exitRoomBtn.addActionListener(e -> {
            c.sendMsg(REXIT + "//");
            clearProfileImages();
            this.setVisible(false);
            c.mf.setVisible(true);
        });

        eastPanel.add(loseBtn);
        eastPanel.add(settingsBtn);
        chatPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        chatPanel.setBounds(10, 344, 240, 235);
        chatPanel.setPreferredSize(new Dimension(260, 300));
        chatPanel.setLayout(null);
        chatPanel.add(chatScrollPane);
        chatScrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        chatInputPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        chatInputPanel.setBounds(0, 190, 240, 40);
        chatInputPanel.setPreferredSize(new Dimension(240, 40));
        chatInputPanel.add(chatInput, BorderLayout.CENTER);
        chatInputPanel.add(sendChatBtn, BorderLayout.EAST);
        chatPanel.add(chatInputPanel);
        chatInputPanel.add(emojiComboBox, BorderLayout.WEST);
        eastPanel.add(chatPanel);
        
        /* 준비 버튼 액션 리스너 설정 */
        readyBtn.addActionListener(e -> {
            isReady = !isReady;  // 준비 상태 토글
            readyBtn.setText(isReady ? "준비 취소" : "준비");
            c.sendMsg(READY + "//" + isReady);
            enableL.setText(isReady ? "준비 완료. 상대를 기다리는 중..." : "준비해 주세요.");
        });

        /* 시작 버튼 액션 리스너 설정 */
        startGameBtn.addActionListener(e -> {
            if (c.getUserCount() < 2) {
                JOptionPane.showMessageDialog(null, "게임을 시작하려면 2명의 플레이어가 필요합니다.", 
                    "게임 시작 불가", JOptionPane.WARNING_MESSAGE);
                return;
            }
            c.sendMsg(START + "//");
        });

        // 초기에 방장만 시작 버튼 보이게 설정
        startGameBtn.setVisible(c.isRoomOwner());
        readyBtn.setVisible(!c.isRoomOwner());

        /* Enter 키로 메시지 전송 */
        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendChatMessage();
                }
            }
        });

        /* 오목 게임 판에서 마우스 이벤트 리스너 추가 */
        DolAction da = new DolAction();
        centerPanel.addMouseListener(da);

        /* 사용자 목록에서 마우스 이벤트 리스너 추가 */
        userList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!userList.isSelectionEmpty()) {
                    String[] m = userList.getSelectedValue().split(" : ");
                    selUser = m[0];
                }
            }
            public void mousePressed(MouseEvent e) {}
            public void mouseReleased(MouseEvent e) {}
            public void mouseEntered(MouseEvent e) {}
            public void mouseExited(MouseEvent e) {}
        });

        /* 채팅 입력 패널 개선 */
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));

        /* 전체 프레임 크기 조정 */
        setSize(900, 620);
        setResizable(false);
        setLocationRelativeTo(null);

        // 테마 적용
        applyTheme();

        // Client에 저장된 현재 테마 적용
        if (c.currentTheme != null) {
            themeManager.applyTheme(c.currentTheme);
            updateTheme();
        }

        // 마우스 리스너 초기화
        mouseListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 기존 마우스 이벤트 처리 코드
            }
        };
        centerPanel.addMouseListener(mouseListener);
    }

    private void applyTheme() {
        // 패널 색상 설정
        basePanel.setBackground(themeManager.getCurrentMainBgColor());
        centerPanel.setBackground(themeManager.getCurrentMainBgColor());
        eastPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatInputPanel.setBackground(themeManager.getCurrentSecondaryBgColor());

        // 라벨 스타일링
        JLabel[] labels = {la1, la2, userListL, enableL, hostProfileLabel, opponentProfileLabel};
        for (JLabel label : labels) {
            label.setForeground(themeManager.getCurrentTextColor());
            label.setFont(themeManager.getMainFont());
        }

        // 버튼 링
        JButton[] buttons = {
            searchBtn, loseBtn, sendChatBtn, settingsBtn, 
            startGameBtn, readyBtn, exitRoomBtn, replayBtn
        };
        for (JButton button : buttons) {
            button.setBackground(themeManager.getCurrentAccentColor());
            button.setForeground(themeManager.getCurrentTextColor());
            button.setFont(themeManager.getMainFont());
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
        }

        // 채팅 영역 스타일링
        chatArea.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatArea.setForeground(themeManager.getCurrentTextColor());
        chatArea.setFont(themeManager.getSubFont());
        chatArea.setCaretColor(themeManager.getCurrentTextColor());

        // 채팅 입력 필드 스타일링
        chatInput.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatInput.setForeground(themeManager.getCurrentTextColor());
        chatInput.setCaretColor(themeManager.getCurrentTextColor());
        chatInput.setFont(themeManager.getSubFont());

        // 유저 리스트 스타일링
        userList.setBackground(themeManager.getCurrentSecondaryBgColor());
        userList.setForeground(themeManager.getCurrentTextColor());
        userList.setFont(themeManager.getSubFont());

        // 이모티콘 콤보박스 스타일링
        if (emojiComboBox != null) {
            emojiComboBox.setBackground(themeManager.getCurrentSecondaryBgColor());
            emojiComboBox.setForeground(themeManager.getCurrentTextColor());
        }

        // 프로필 라벨 테두리 설정
        hostProfileLabel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor()));
        opponentProfileLabel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor()));

        // 컴포넌트 리페인트
        SwingUtilities.updateComponentTreeUI(this);
    }

    // 테마 변경 시 호출할 메서드
    public void updateTheme() {
        SwingUtilities.invokeLater(() -> {
            // 패널 색상 설정
            basePanel.setBackground(themeManager.getCurrentMainBgColor());
            centerPanel.setBackground(themeManager.getCurrentMainBgColor());
            eastPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
            chatPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
            chatInputPanel.setBackground(themeManager.getCurrentSecondaryBgColor());

            // 라벨 스타일링
            la1.setForeground(themeManager.getCurrentTextColor());
            la2.setForeground(themeManager.getCurrentTextColor());
            userListL.setForeground(themeManager.getCurrentTextColor());
            enableL.setForeground(themeManager.getCurrentTextColor());

            // 버튼 스타일링
            styleButtons(searchBtn, loseBtn, sendChatBtn, settingsBtn, startGameBtn, readyBtn, exitRoomBtn, replayBtn);

            // 채 역 스타일링
            chatArea.setBackground(themeManager.getCurrentSecondaryBgColor());
            chatArea.setForeground(themeManager.getCurrentTextColor());
            chatArea.setCaretColor(themeManager.getCurrentTextColor());

            // 채팅 입력 필드 스타일링
            chatInput.setBackground(themeManager.getCurrentSecondaryBgColor());
            chatInput.setForeground(themeManager.getCurrentTextColor());
            chatInput.setCaretColor(themeManager.getCurrentTextColor());

            // 저 리스트 스타일링
            userList.setBackground(themeManager.getCurrentSecondaryBgColor());
            userList.setForeground(themeManager.getCurrentTextColor());

            // 프로필 라벨 테두리 설정
            hostProfileLabel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2));
            opponentProfileLabel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2));

            // 컴넌트 새로침
            SwingUtilities.updateComponentTreeUI(this);
            repaint();
            revalidate();
        });
    }

    /* 버튼 이���트 리스너 */
    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton) e.getSource();

            /* 전적검색 버튼 이벤트 */
            if (b.getText().equals("전적검색")) {
                if (selUser != null) { 
                    c.sendMsg(SEARCH + "//" + selUser);
                } else { 
                    JOptionPane.showMessageDialog(null, "검색할 닉네임을 선택해주세요", "검색 실패", JOptionPane.ERROR_MESSAGE);
                }
            }

            /* 기권하기 버튼 이벤트 */
            else if (b.getText().equals("기권하기")) {
                int response = JOptionPane.showConfirmDialog(
                    GameFrame.this,
                    "정말 기권하시겠습니까?",
                    "기권 확인",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (response == JOptionPane.YES_OPTION) {
                    c.sendMsg(LOSE + "//");  // 기권 메시지 전송
                    resetGameState();        // 게임 상태 초기화
                    loseBtn.setEnabled(false);
                    enableL.setText("게임이 종료되었습니다.");
                }
            }

            /* 채팅 전송 버튼 이벤트 */
            else if (b.getText().equals("전송")) {
                sendChatMessage();
            }

            /* 채팅방 설정 버튼 이벤트 */
            else if (b.getText().equals("채팅방 설정")) {
                ChatSettingsDialog settingsDialog = new ChatSettingsDialog(GameFrame.this, chatArea);
                settingsDialog.setVisible(true);
            }

            /* 복기 버튼 이벤트 */
            else if (b == replayBtn) {
                System.out.println("[GameFrame] 복기 요청 전송");
                replayBtn.setEnabled(false); // 중복 클릭 방지
                c.sendMsg(REQUEST_REPLAY + "//");
                Timer enableTimer = new Timer(2000, ev -> replayBtn.setEnabled(true));
                enableTimer.setRepeats(false);
                enableTimer.start();
            }

            /* 나가 버튼 이벤트 */
            else if (b == exitRoomBtn) {
                // 게임이 진행 중일 때는 나가기 불가
                if (enable) {
                    return;
                }
                c.sendMsg(REXIT + "//");
                clearProfileImages();
                GameFrame.this.setVisible(false);
                c.mf.setVisible(true);
            }
        }
    }

    /* 채팅 메시지를 채팅창에 추가 */
    public void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }
    
    /* 채팅 메시지를 서버로 전송 */
    private void sendChatMessage() {
        String message = chatInput.getText().trim();
        if (!message.isEmpty()) {
            c.sendRoomChatMessage(message);
            chatInput.setText(""); 
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paintComponents(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // 안티앨리어싱 설정
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // 바둑판 배경 그리기
        g2d.setColor(new Color(206, 167, 61));
        g2d.fillRect(30, 35, 600, 600);
        
        // 격자 선 그리기
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.0f));
        
        // 세로선
        for (int i = 1; i <= 19; i++) {
            g2d.drawLine(30 + i * 30, 50, 30 + i * 30, 590);
        }
        
        // 가로선
        for (int i = 1; i <= 19; i++) {
            g2d.drawLine(60, 20 + i * 30, 600, 20 + i * 30);
        }
        
        // 화점(바둑판의 점) 그리기
        g2d.setColor(Color.BLACK);
        int[] starPoints = {4, 10, 16};  // 화점 위치
        for (int i : starPoints) {
            for (int j : starPoints) {
                g2d.fillOval(26 + i * 30, 16 + j * 30, 8, 8);
            }
        }
        
        // 돌 그리기
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                if (omok[j][i] > 0) {
                    // 그림자 효과
                    g2d.setColor(new Color(0, 0, 0, 50));
                    g2d.fillOval((i + 1) * 30 - 10, (j) * 30 + 39, 25, 25);
                    
                    // 돌 그리기
                    if (omok[j][i] == 1) {  // 흑돌
                        GradientPaint gp = new GradientPaint(
                            (i + 1) * 30 - 12, (j) * 30 + 35, Color.DARK_GRAY,
                            (i + 1) * 30 + 13, (j) * 30 + 60, Color.BLACK
                        );
                        g2d.setPaint(gp);
                    } else {  // 백돌
                        GradientPaint gp = new GradientPaint(
                            (i + 1) * 30 - 12, (j) * 30 + 35, Color.WHITE,
                            (i + 1) * 30 + 13, (j) * 30 + 60, Color.LIGHT_GRAY
                        );
                        g2d.setPaint(gp);
                    }
                    g2d.fillOval((i + 1) * 30 - 12, (j) * 30 + 37, 25, 25);
                    
                    // 마지막 수 표시
                    if (lastMove != null && lastMove.x == i && lastMove.y == j) {
                        g2d.setColor(Color.RED);
                        g2d.setStroke(new BasicStroke(2.0f));
                        g2d.drawOval((i + 1) * 30 - 14, (j) * 30 + 35, 29, 29);
                    }
                }
            }
        }
    }

    /* 돌을 그리는 메소드 */
    void drawdol(Graphics g) {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (omok[j][i] == 1) {
                    g.setColor(Color.BLACK);
                    g.fillOval((i + 1) * 30 - 12, (j) * 30 + 37, 25, 25);
                } else if (omok[j][i] == 2) {
                    g.setColor(Color.WHITE);
                    g.fillOval((i + 1) * 30 - 12, (j) * 30 + 37, 25, 25);
                }
            }
        }
    }

    /* 돌 초기화 메소드 */
    public void remove() {
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                omok[i][j] = 0;
            }
        }
        lastMove = null;  // 마지막 수 초기화
        repaint();
    }
    /* 목 게임 돌 위치 설정 */
    class DolAction implements MouseListener {
        @Override
        public void mousePressed(MouseEvent e) {
            if (!isGameStarted) {
                JOptionPane.showMessageDialog(
                    GameFrame.this,
                    "게임이아직 시작되지 않았습니다.\n게임 시작 후에 돌을 둘 수 있습니다.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }
            
            if (!enable) {
                JOptionPane.showMessageDialog(
                    GameFrame.this,
                    "상대방의 차례입니다.\n자신의 차례가 될 때까지 기다려주세요.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // 마우스 클릭 위치 계산
            int x = (e.getX() - 10) / 30;
            int y = (e.getY() - 10) / 30;
            
            // 이미 돌이 있는 경우 리턴
            if (omok[y][x] != 0) {
                JOptionPane.showMessageDialog(
                    GameFrame.this,
                    "이미 돌이 놓여있는 위치입니다.\n다른 위치를 선택해주세요.",
                    "알림",
                    JOptionPane.INFORMATION_MESSAGE
                );
                return;
            }

            // 돌 놓기
            if (dc.equals(blackTag)) {
                omok[y][x] = 1;
            } else {
                omok[y][x] = 2;
            }

            // 마지막 수 위치 업데이트
            lastMove = new Point(x, y);

            repaint();

            // 마지막 수를 상대방에게 전송
            c.sendMsg(OMOK + "//" + x + "//" + y + "//" + dc);
            enable = false;
            enableL.setText("상대방의 차례입니다.");

            // 승리 조건 체크
            Point p = new Point(x, y);
            if (check(p, omok[y][x])) {
                c.sendMsg(WIN + "//");  // 승리 메시지 전송
                winGame();  // 게임 종료 리
            }
        }

        public void mouseClicked(MouseEvent e) {}
        public void mouseReleased(MouseEvent e) {}
        public void mouseEntered(MouseEvent e) {}
        public void mouseExited(MouseEvent e) {}
    }

    /* 승리 여부 확인 메소드 */
    boolean check(Point p, int c) {
        // 가로 방향 확인
        if (count(p, 1, 0, c) + count(p, -1, 0, c) == 4) { 
            return true;
        }
        // 세로 방향 확인
        if (count(p, 0, 1, c) + count(p, 0, -1, c) == 4) {
            return true;
        }
        // 대각선 방향 확인 (좌상단-우하단)
        if (count(p, -1, -1, c) + count(p, 1, 1, c) == 4) {
            return true;
        }
        // 대각선 방향 확인 (우상단-좌하단)
        if (count(p, 1, -1, c) + count(p, -1, 1, c) == 4) {
            return true;
        }
        return false;
    }

    /* 특정 방향에 같은 색 돌이 몇 개 있는지 확인 */
    int count(Point p, int _x, int _y, int c) {
        int i = 0;
        try {
            for (i = 0; omok[p.y + (i + 1) * _y][p.x + (i + 1) * _x] == c; i++);
        } catch (ArrayIndexOutOfBoundsException e) {
            // 배열 범위를 벗어나면 현재까지 계산된 개수 반환
        }
        return i;
    }

    /* 호스트 프로필 이미지 업데이트 */
    public void updateHostProfileImage(byte[] profileImage) {
        SwingUtilities.invokeLater(() -> {
            try {
                ImageIcon icon = new ImageIcon(profileImage);
                Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                hostProfileLabel.setIcon(new ImageIcon(image));
                System.out.println("[GameFrame] 방장 프로필 이미지 업데이트 완료");
            } catch (Exception e) {
                System.out.println("[GameFrame] 방장 프로필 이미지 업데이트 실패: " + e.getMessage());
            }
        });
    }

    /* 상대방 프로필 이미지 업데이트 */
    public void updateOpponentProfileImage(byte[] profileImage) {
        SwingUtilities.invokeLater(() -> {
            try {
                ImageIcon icon = new ImageIcon(profileImage);
                Image image = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                opponentProfileLabel.setIcon(new ImageIcon(image));
                System.out.println("[GameFrame] 상대방 프로필 이미 업데이트 완료");
            } catch (Exception e) {
                System.out.println("[GameFrame] 상대방 프로필 이미지 업데이트 실패: " + e.getMessage());
            }
        });
    }

    /* 프로필 이미지 초기화 메서드 추가 */
    public void clearProfileImages() {
        SwingUtilities.invokeLater(() -> {
            hostProfileLabel.setIcon(null);
            opponentProfileLabel.setIcon(null);
            System.out.println("[GameFrame] 프로필 이미지 초기화 완료");
        });
    }

    private void styleButtons(JButton... buttons) {
        for (JButton button : buttons) {
            if (button != null) {
                button.setBackground(themeManager.getCurrentAccentColor());
                button.setForeground(themeManager.getCurrentTextColor());
                button.setFocusPainted(false);
                button.setBorderPainted(false);
                button.setOpaque(true);
            }
        }
    }
    
    // 테마 변경 시 호출되는 메서드
    public void applyCurrentTheme() {
        SwingUtilities.invokeLater(() -> {
            themeManager.applyTheme(themeManager.getCurrentTheme());
        });
    }

    public void winGame() {
        System.out.println("[Client] 게임 승리");
        JOptionPane.showMessageDialog(null, "게임에 승리하였습니다", "승리", JOptionPane.INFORMATION_MESSAGE);
        
        // 게임 종료 후 조작 불가능하게 설정
        enable = false;
        enableL.setText("게임이 종료되었습니다.");
        
        // 게임 종료 후 버튼 상태 변경
        loseBtn.setEnabled(false);
        readyBtn.setVisible(false);
        startGameBtn.setVisible(false);
        replayBtn.setEnabled(true);
        
        // 게임판 초기화
        remove();  // 현재 GameFrame의 오목판 초기화
        
        // 기존 ActionListener 제거 후 새로운 리스너 추가
        for (ActionListener al : replayBtn.getActionListeners()) {
            replayBtn.removeActionListener(al);
        }
        replayBtn.addActionListener(e -> {
            c.sendMsg(REQUEST_REPLAY + "//");
            // 서버로부터 응답을 받으면 Client 클래스의 MessageListener에서 
            // ReplayFrame을 생성하고 표시하도록 수정
        });
    }

    public void loseGame() {
        System.out.println("[Client] 게임 패배");
        JOptionPane.showMessageDialog(null, "게임 패배하였습니다", "패배", JOptionPane.INFORMATION_MESSAGE);
        
        // 게임 종료 후 조작 불가능하게 설정
        enable = false;
        enableL.setText("게임이 종료되었습니다.");
        
        // 게임 종료 후 버튼 상태 변경
        loseBtn.setEnabled(false);
        readyBtn.setVisible(false);
        startGameBtn.setVisible(false);
        replayBtn.setEnabled(true);
        
        // 게임판 초기화
        remove();  // 현재 GameFrame의 오목판 초기화
        
        // 기존 ActionListener 제거 후 새로운 리스너 추가
        for (ActionListener al : replayBtn.getActionListeners()) {
            replayBtn.removeActionListener(al);
        }
        replayBtn.addActionListener(e -> {
            c.sendMsg(REQUEST_REPLAY + "//");
        });
    }

    private void handleRoomExit() {
        // ... existing code ...
        
        if (myRoom != null) {
            myRoom.resetGame();  // 방의 게임 상태 초기화
        }
        remove();  // 현재 GameFrame의 오목판 초기화
        enable = false;  // 게 진행 상태 초기화
        enableL.setText("");  // 상태 메시지 초화
        
        // ... rest of existing code ...
    }

    // 버튼 상태 초기화 메서드 추가
    private void resetButtonStates() {
        loseBtn.setEnabled(true);
        readyBtn.setVisible(true);
        startGameBtn.setVisible(true);
        replayBtn.setEnabled(false);
        
        // 게임 상태 초기화
        enable = false;
        enableL.setText("");
    }

    // enterRoom 메서드에서 호출
    public void reset() {
        setVisible(true);
        resetButtonStates();  // 버튼 상태 초기화
        remove();  // 오목판 초기화
        
        // 준비 상태 초기화 추가
        isReady = false;
        readyBtn.setText("준비");
        enableL.setText("준비해 주세요.");
        
        // 게임 상태 초기화
        isGameStarted = false;
        enable = false;
    }

    // 준비 상태 업데이트 메서드
    public void updateReadyStatus(String nickname, boolean ready) {
        SwingUtilities.invokeLater(() -> {
            ListModel<?> currentModel = userList.getModel();
            DefaultListModel<String> newModel = new DefaultListModel<>();
            
            // 기존 모델의 내용을 새 모델로 복사
            for (int i = 0; i < currentModel.getSize(); i++) {
                String user = currentModel.getElementAt(i).toString();
                if (user.startsWith(nickname)) {
                    newModel.addElement(nickname + (ready ? " (준비완료)" : ""));
                } else {
                    newModel.addElement(user);
                }
            }
            
            userList.setModel(newModel);
        });
    }

    /* 게임 시작 메서드 */
    public void startGame() {
        System.out.println("[GameFrame] 게임 시작");
        isGameStarted = true;
        
        // 방 생성자(BLACK)인 경우에만 처음에 enable을 true로 설정
        if (dc.equals(blackTag)) {
            enable = true;
            enableL.setText("게임이 시작되었습니다. 첫 번째 돌을 둘 수 있습니다.");
            JOptionPane.showMessageDialog(
                this,
                "게임 시작.\n방장님이 첫 번째 돌을 둘 수 있습니다.",
                "게임 시작",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            enable = false;
            enableL.setText("게임이 시작되습니다. 상대방의 차례를 기다려주세요.");
            JOptionPane.showMessageDialog(
                this,
                "게임 시작.\n상대방(방장)의 첫 수를 기다려주세요.",
                "게임 시작",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
        
        // 게임 시작 시 버튼 상태 변경
        readyBtn.setVisible(false);
        startGameBtn.setVisible(false);
        loseBtn.setEnabled(true);
    }

    /* 준비 상태 설정 메서드 */
    public void setReady(boolean isReady) {
        this.isReady = isReady;
        readyBtn.setText(isReady ? "준비 취소" : "준비");
        enableL.setText(isReady ? "준비 완료. 상대를 기다리는 중..." : "준비해 주세요.");
    }

    // 게임 종료나 방 나가기 시 게임 상태 초기화를 위한 메서드
    public void resetGameState() {
        isGameStarted = false;
        enable = false;
        enableL.setText("게임이 시작되지 않았습니다.");
    }

    private void logMessage(String message) {
        LogManager.getInstance().log("GameFrame", message);
    }


    // 게임판 업데이트 메서드 수정
    public void updateGameState(String gameState) {
        String[] states = gameState.split(",");
        int index = 0;
        
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                if (index < states.length) {
                    omok[i][j] = Integer.parseInt(states[index++]);
                    if (omok[i][j] > 0) {
                        repaint(); // 화면 갱신
                    }
                }
            }
        }
    }

    // 상대방의 수를 받았을 때 호출될 메소드 추가
    public void updateOpponentMove(int x, int y, int color) {
        omok[y][x] = color;
        lastMove = new Point(x, y);
        repaint();
    }
}
