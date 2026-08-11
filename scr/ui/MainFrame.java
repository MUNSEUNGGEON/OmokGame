package ui;

import core.*;
import ui.ThemeManager.Theme;
import sound.BackgroundMusic;

import static core.MessageType.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public ThemeManager themeManager;
    private BackgroundMusic bgm;
    
    /* MainFrame에서 가져온 변수들 */
    public Client c = null;
    public String selRoom;
    public String roomName;
    private JPopupMenu userPopupMenu;
    private JMenuItem inviteMenuItem;

    /* Panel */
    private JPanel mainPanel;
    private JPanel sidePanel;
    private JPanel roomListPanel;
    private JPanel chatPanel;
    private JPanel profilePanel;
    private JPanel eastPanel;
    private JPanel groupChatListPanel;
    private JList<String> groupChatList;
    private DefaultListModel<String> groupChatListModel;
    private JButton createGroupChatButton;
    
    /* Label */
    private JLabel roomListL = new JLabel("방 목록");
    private JLabel cuListL = new JLabel("접속 인원");
    private JLabel profileLabel = new JLabel();
    private JLabel nicknameLabel = new JLabel("닉네임", SwingConstants.CENTER);
    private JLabel statsLabel = new JLabel("전적: 0승 0패", SwingConstants.CENTER);

    /* ScrollPane */
    public JScrollPane rL_sp;
    public JScrollPane cL_sp;
    private JScrollPane chatScrollPane;

    /* List */
    public JList<String> rList = new JList<>();
    public JList<String> cuList = new JList<>();
    private JTextArea chatArea = new JTextArea();

    /* Menu */
    private JMenuBar mb = new JMenuBar();
    private JMenu infoMenu = new JMenu("내정보");
    private JMenuItem viewInfo = new JMenuItem("내 정보 보기");
    private JMenuItem changeInfo = new JMenuItem("내 정보 바꾸기");

    /* Button */
    private JButton viewRanking = new JButton("랭킹");
    private JButton createRoom = new JButton("방 만들기");
    private JButton enterRoom = new JButton("방 입장하기");
    private JButton exitGame = new JButton("게임 종료");
    private JButton sendChat = new JButton("전송");
    private JButton selectCharacterButton = new JButton("캐릭터 선택");
    private JButton spectateButton = new JButton("관전하기");
    private JButton toggleThemeButton = new JButton("테마 설정");
    private JButton toggleMusicButton = new JButton("배경음악");

    /* TextField */
    private JTextField chatInput = new JTextField();
    private final JLabel lblNewLabel_1 = new JLabel("프로필");

    private JPanel themeSelectionPanel;
    private JComboBox<String> themeComboBox;
    private boolean isThemePanelVisible = false;
    private boolean isAdjusting = false;
    private boolean isTemporaryPassword = false;

    public MainFrame(Client _c) {
        c = _c;
        themeManager = new ThemeManager(this);
        
        bgm = new BackgroundMusic("scr/sound/BackgroundMusic.wav");
        bgm.play();
        
        setTitle("오목 게임 로비");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        initializeUI();
        initializeMenuBar();
        addEventListeners();
        addChatInputKeyListener();
        
        themeManager.applyTheme(ThemeManager.Theme.MORNING);
    }

    private void styleAllLabels() {
        JLabel[] labels = {
            roomListL, cuListL, profileLabel, nicknameLabel, statsLabel
        };
        
        for (JLabel label : labels) {
            themeManager.styleComponent(label);
        }
    }

    private void styleAllButtons() {
        JButton[] buttons = {
            viewRanking, createRoom, enterRoom, exitGame, 
            sendChat, selectCharacterButton, spectateButton, 
            toggleThemeButton, toggleMusicButton
        };
        
        for (JButton button : buttons) {
            themeManager.styleComponent(button);
        }
    }

    private void initializeUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null);
        themeManager.styleComponent(mainPanel);
        
        ImageIcon titleIcon = new ImageIcon("./img/title.png");
        Image titleImage = titleIcon.getImage();
        Image scaledTitle = titleImage.getScaledInstance(200, 40, Image.SCALE_SMOOTH);
        JLabel titleLabel = new JLabel(new ImageIcon(scaledTitle));
        titleLabel.setBounds(10, 10, 200, 40);
        mainPanel.add(titleLabel);
        
        if (toggleMusicButton == null) {
            toggleMusicButton = createStyledButton("배경음악");
        }
        if (toggleThemeButton == null) {
            toggleThemeButton = createStyledButton("테마 설정");
        }
        if (exitGame == null) {
            exitGame = createStyledButton("게임 종료");
        }
        
        // 상단 버튼들의 위치와 크기 조정
        toggleMusicButton.setBounds(470, 10, 90, 25);  // x좌표를 왼쪽으로, y좌표를 위로
        toggleThemeButton.setBounds(575, 10, 95, 25);  // x좌표를 왼쪽으로, y좌표를 위로
        exitGame.setBounds(680, 10, 95, 25);
        
        mainPanel.add(toggleMusicButton);
        mainPanel.add(toggleThemeButton);
        mainPanel.add(exitGame);
        
        createSidePanel();
        createRoomListPanel();
        createGroupChatPanel();
        createUserListPanel();
        createChatPanel();
        
        getContentPane().add(mainPanel);

        themeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        themeSelectionPanel.setBounds(582, 33, 80, 30);
        themeSelectionPanel.setBackground(themeManager.getCurrentMainBgColor());
        
        String[] themeNames = {"아침", "밤", "봄", "여름", "가을", "겨울"};
        themeComboBox = new JComboBox<>(themeNames);
        themeComboBox.setSelectedItem(getCurrentThemeName());
        styleComboBox(themeComboBox);
        
        themeComboBox.addActionListener(e -> {
            if (!isAdjusting && e.getActionCommand().equals("comboBoxChanged")) {
                Theme selectedTheme = getThemeFromName((String) themeComboBox.getSelectedItem());
                if (selectedTheme != themeManager.getCurrentTheme()) {
                    themeManager.applyTheme(selectedTheme);
                    styleAllComponents();
                    SwingUtilities.updateComponentTreeUI(this);
                    toggleThemeButton.setText("변경 완료");
                    repaint();
                    revalidate();
                }
            }
        });
        
        themeSelectionPanel.add(themeComboBox);
        themeSelectionPanel.setVisible(false);
        mainPanel.add(themeSelectionPanel);

        toggleThemeButton.addActionListener(e -> {
            isThemePanelVisible = !isThemePanelVisible;
            themeSelectionPanel.setVisible(isThemePanelVisible);
            
            if (isThemePanelVisible) {
                toggleThemeButton.setText("적용");
            } else {
                toggleThemeButton.setText("테마");
            }
        });

        // 사용자 목록에 우클릭 메뉴 추가
        userPopupMenu = new JPopupMenu();
        JMenuItem viewInfoItem = new JMenuItem("정보 보기");
        viewInfoItem.addActionListener(e -> {
            String selectedUser = cuList.getSelectedValue();
            if (selectedUser != null) {
                c.requestUserInfo(selectedUser);
            }
        });
        userPopupMenu.add(viewInfoItem);

        // 사용자 목록에 마우스 리스너 추가
        cuList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            private void showPopupMenu(MouseEvent e) {
                int index = cuList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    cuList.setSelectedIndex(index);
                    userPopupMenu.show(cuList, e.getX(), e.getY());
                }
            }
        });
        
    }

    private void createSidePanel() {
        sidePanel = new JPanel();
        sidePanel.setLayout(null);
        sidePanel.setBounds(10, 60, 150, 480);
        sidePanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        themeManager.styleComponent(sidePanel);

        profileLabel.setBounds(15, 30, 120, 120);
        profileLabel.setOpaque(true);
        profileLabel.setBackground(themeManager.getCurrentSecondaryBgColor());
        profileLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));
        
        nicknameLabel.setBounds(15, 160, 120, 25);
        nicknameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nicknameLabel.setForeground(themeManager.getCurrentTextColor());
        
        statsLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));
        statsLabel.setBounds(15, 190, 120, 20);
        statsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statsLabel.setForeground(themeManager.getCurrentTextColor());

        if (viewRanking == null) {
            viewRanking = createStyledButton("전적 보기");
        }
        if (createRoom == null) {
            createRoom = createStyledButton("방 만들기");
        }
        if (enterRoom == null) {
            enterRoom = createStyledButton("방 입장하기");
        }
        if (selectCharacterButton == null) {
            selectCharacterButton = createStyledButton("캐릭터 선택");
        }
        if (spectateButton == null) {
            spectateButton = createStyledButton("관전하기");
        }

        viewRanking.setBounds(15, 230, 120, 30);
        createRoom.setBounds(15, 270, 120, 30);
        enterRoom.setBounds(15, 310, 120, 30);
        selectCharacterButton.setBounds(15, 350, 120, 30);
        spectateButton.setBounds(15, 390, 120, 30);

        sidePanel.add(profileLabel);
        lblNewLabel_1.setFont(new Font("굴림", Font.PLAIN, 14));
        lblNewLabel_1.setBounds(55, 10, 50, 15);
        lblNewLabel_1.setForeground(themeManager.getCurrentTextColor());
        
        sidePanel.add(lblNewLabel_1);
        sidePanel.add(nicknameLabel);
        sidePanel.add(statsLabel);
        sidePanel.add(viewRanking);
        sidePanel.add(createRoom);
        sidePanel.add(enterRoom);
        sidePanel.add(selectCharacterButton);
        sidePanel.add(spectateButton);
        
        sidePanel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 1));
        
        mainPanel.add(sidePanel);
    }

    private void createProfilePanel() {
        profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(themeManager.getCurrentMainBgColor());
        profilePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        profileLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileLabel.setPreferredSize(new Dimension(150, 150));
        profileLabel.setBorder(BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2));
        
        nicknameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nicknameLabel.setForeground(themeManager.getCurrentTextColor());
        nicknameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        statsLabel.setForeground(themeManager.getCurrentTextColor());
        statsLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 9));

        profilePanel.add(profileLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        profilePanel.add(nicknameLabel);
        profilePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        profilePanel.add(statsLabel);
        
        sidePanel.add(profilePanel);
    }

    private void createRoomListPanel() {
        roomListPanel = new JPanel();
        roomListPanel.setLayout(null);
        roomListPanel.setBounds(170, 60, 300, 300);
        roomListPanel.setBackground(themeManager.getCurrentMainBgColor());

        roomListL.setBounds(120, 10, 60, 25);
        roomListL.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        roomListL.setForeground(themeManager.getCurrentTextColor());
        
        rList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        rList.setBackground(themeManager.getCurrentSecondaryBgColor());
        rList.setForeground(themeManager.getCurrentTextColor());
        rL_sp = new JScrollPane(rList);
        rL_sp.setBounds(10, 40, 280, 250);
        
        roomListPanel.add(roomListL);
        roomListPanel.add(rL_sp);
        mainPanel.add(roomListPanel);
    }

    private void createUserListPanel() {
        eastPanel = new JPanel();
        eastPanel.setLayout(null);
        eastPanel.setBounds(640, 60, 140, 300);
        eastPanel.setBackground(themeManager.getCurrentMainBgColor());

        cuListL.setBounds(37, 10, 75, 25);
        cuListL.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        cuListL.setForeground(themeManager.getCurrentTextColor());
        
        cuList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        cuList.setBackground(themeManager.getCurrentSecondaryBgColor());
        cuList.setForeground(themeManager.getCurrentTextColor());
        cL_sp = new JScrollPane(cuList);
        cL_sp.setBounds(10, 40, 120, 250);
        
        eastPanel.add(cuListL);
        eastPanel.add(cL_sp);
        mainPanel.add(eastPanel);
    }

    private void createChatPanel() {
        chatPanel = new JPanel();
        chatPanel.setLayout(null);
        chatPanel.setBounds(170, 370, 610, 170);
        themeManager.styleComponent(chatPanel);

        chatArea.setEditable(false);
        themeManager.styleComponent(chatArea);
        
        chatScrollPane = new JScrollPane(chatArea);
        chatScrollPane.setBounds(10, 10, 590, 110);
        themeManager.styleComponent(chatScrollPane);

        chatInput.setBounds(10, 130, 500, 30);
        themeManager.styleComponent(chatInput);
        
        sendChat.setBounds(520, 130, 80, 30);
        
        chatPanel.add(chatScrollPane);
        chatPanel.add(chatInput);
        chatPanel.add(sendChat);
        mainPanel.add(chatPanel);
    }

    private void initializeMenuBar() {
        mb.setBackground(themeManager.getCurrentSecondaryBgColor());
        infoMenu.setForeground(themeManager.getCurrentTextColor());
        
        styleMenuItems();
        
        infoMenu.add(viewInfo);
        infoMenu.addSeparator();
        infoMenu.add(changeInfo);
        mb.add(infoMenu);
        setJMenuBar(mb);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        themeManager.styleComponent(button);
        return button;
    }

    private void initializePopupMenu() {
        userPopupMenu = new JPopupMenu();
        JMenuItem viewInfoItem = new JMenuItem("정보 보기");
        
        viewInfoItem.addActionListener(e -> {
            String selectedUser = cuList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(c.nickname)) {
                c.showUserInfo(selectedUser);
                c.sendMsg(USER_INFO + "//" + selectedUser);
            }
        });
        
        userPopupMenu.add(viewInfoItem);
        inviteMenuItem = new JMenuItem("1:1 채팅 초대");
        JMenuItem groupChatInviteItem = new JMenuItem("그룹 채팅 초대");
        
        inviteMenuItem.addActionListener(e -> {
            String selectedUser = cuList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(c.nickname)) {
                c.sendMsg(INVITE + "//" + selectedUser);
                System.out.println("[Client] " + selectedUser + "에게 1대1 대화 초대 전송");
            }
        });

        groupChatInviteItem.addActionListener(e -> {
            String selectedUser = cuList.getSelectedValue();
            if (selectedUser != null && !selectedUser.equals(c.nickname)) {
                // 그룹 채팅방 목록 가져오기
                int size = groupChatListModel.getSize();
                String[] rooms = new String[size];
                for (int i = 0; i < size; i++) {
                    rooms[i] = groupChatListModel.getElementAt(i);
                }
                
                if (rooms.length > 0) {
                    String selectedRoom = (String) JOptionPane.showInputDialog(
                        this,
                        "초대할 그룹 채팅방을 선택하세요:",
                        "그룹 채팅 초대",
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        rooms,
                        rooms[0]
                    );
                    
                    if (selectedRoom != null) {
                        c.sendMsg(GROUP_CHAT_INVITE + "//" + selectedRoom + "//" + selectedUser);
                        System.out.println("[Client] " + selectedUser + "님을 " + selectedRoom + "방으로 초대");
                    }
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "초대할 그룹 채팅방이 없습니다.\n먼저 그룹 채팅방을 생성해주세요.",
                        "알림",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }
            }
        });

        userPopupMenu.add(inviteMenuItem);
        userPopupMenu.addSeparator();  // 구분선 추가
        userPopupMenu.add(groupChatInviteItem);
    }

    private void addEventListeners() {
        ButtonListener bl = new ButtonListener();
        MenuItemListener mil = new MenuItemListener();
        
        viewInfo.addActionListener(mil);
        changeInfo.addActionListener(mil);
        viewRanking.addActionListener(bl);
        createRoom.addActionListener(bl);
        enterRoom.addActionListener(bl);
        exitGame.addActionListener(bl);
        sendChat.addActionListener(bl);
        selectCharacterButton.addActionListener(bl);
        spectateButton.addActionListener(bl);
        
        toggleMusicButton.addActionListener(e -> {
            if (bgm.isPlaying()) {
                bgm.stop();
                toggleMusicButton.setText("♪OFF");
            } else {
                bgm.play();
                toggleMusicButton.setText("♪ON");
            }
        });
        
        rList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!rList.isSelectionEmpty()) {
                    String[] m = rList.getSelectedValue().split(" : ");
                    selRoom = m[0];
                }
            }
        });
        
        initializePopupMenu();
        addUserListPopupMenu();
    }

    private void addUserListPopupMenu() {
        cuList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) handlePopupTrigger(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) handlePopupTrigger(e);
            }
        });
    }

    private void handlePopupTrigger(MouseEvent e) {
        if (cuList.getSelectedIndex() != -1) {
            userPopupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton) e.getSource();

            if (b == exitGame) {
                if (bgm != null) {
                    bgm.close();
                }
                System.out.println("[Client] 게임 종료");
                c.sendMsg(PEXIT + "//");
                System.exit(0);
            }
            else if (b == viewRanking) {
                c.rf.setVisible(true);
                System.out.println("[Client] 전적 조회 인터페이스 열림");
                c.sendMsg(RANK + "//");
            }
            else if (b == createRoom) {
                roomName = JOptionPane.showInputDialog(null, "생성할 방 제목을 입력하시오", "방 생성", JOptionPane.QUESTION_MESSAGE);
                if (roomName != null && !roomName.trim().isEmpty()) {
                    c.sendMsg(CREATEROOM + "//" + roomName);
                } else {
                    JOptionPane.showMessageDialog(null, "방 제목이 입력되지 않았습니다", "생성 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (b == enterRoom) {
                if (selRoom != null) {
                    c.sendMsg(EROOM + "//" + selRoom);
                } else {
                    JOptionPane.showMessageDialog(null, "입장할 방을 선택해주요", "입장 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (b == sendChat) {
                String chatMessage = chatInput.getText().trim();
                if (!chatMessage.isEmpty()) {
                    c.sendMsg(LOBBY_CHAT + "//" + chatMessage);
                    chatInput.setText("");
                }
            }
            else if (b == selectCharacterButton) {
                try {
                    new CharacterSelectFrame(c, profileLabel);
                    System.out.println("[Client] 캐릭터 선택 인터페이스 열림");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainFrame.this, "캐릭터 선택 창을 열 수 습니다. 오: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE);
                }
            }
            else if (b == spectateButton) {
                System.out.println("[MainFrame] 관전하기 버튼 클릭됨");
                if (selRoom != null) {
                    try {
                        c.sendMsg(SPECTATE_REQUEST + "//" + selRoom);
                        System.out.println("[Client] 관전 요청: " + selRoom);
                    } catch (Exception ex) {
                        System.out.println("[Client] 관전 요청 실패: " + ex.getMessage());
                        JOptionPane.showMessageDialog(null, 
                            "관전 요청 실패: " + ex.getMessage(), 
                            "오류", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, 
                        "관전할 방을 선택해주세요", 
                        "관전 실패", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public class MenuItemListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JMenuItem mi = (JMenuItem) e.getSource();
            if (mi == viewInfo) {
                c.inf.setVisible(true);
                System.out.println("[Client] 회원 정보 조회 인터페이스 열림");
            }
            else if (mi == changeInfo) {
                c.cinf.setVisible(true);
                System.out.println("[Client] 회원 정 변경 인터이스 열림");
            }
        }
    }

    private void switchTheme() {
        Theme currentTheme = themeManager.getCurrentTheme();
        Theme nextTheme = getNextTheme(currentTheme);
        themeManager.applyTheme(nextTheme);
        
        styleAllComponents();
        
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
        revalidate();
    }

    private Theme getNextTheme(Theme currentTheme) {
        switch (currentTheme) {
            case NIGHT: return Theme.MORNING;
            case MORNING: return Theme.SPRING;
            case SPRING: return Theme.SUMMER;
            case SUMMER: return Theme.AUTUMN;
            case AUTUMN: return Theme.WINTER;
            case WINTER: return Theme.NIGHT;
            default: return Theme.NIGHT;
        }
    }

    private void addChatInputKeyListener() {
        chatInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String chatMessage = chatInput.getText().trim();
                    if (!chatMessage.isEmpty()) {
                        c.sendMsg(LOBBY_CHAT + "//" + chatMessage);
                        chatInput.setText("");
                    }
                }
            }
        });
    }

    public void showInviteDialog(String inviter) {
        int response = JOptionPane.showConfirmDialog(
            this,
            inviter + "님이 1대1 대화 초대하셨습니다. 수락하시겠습니까?",
            "대화 초대",
            JOptionPane.YES_NO_OPTION
        );
        
        if (response == JOptionPane.YES_OPTION) {
            c.sendMsg(INVITE_ACCEPT + "//" + inviter);
        } else {
            c.sendMsg(INVITE_REJECT + "//" + inviter);
        }
    }

    public void setProfileImage(byte[] imageBytes) {
        SwingUtilities.invokeLater(() -> {
            if (imageBytes == null || imageBytes.length == 0) return;
            
            ImageIcon originalIcon = new ImageIcon(imageBytes);
            Image originalImage = originalIcon.getImage();
            
            // 실제 표시 영역 계산 (border와 padding 고려)
            int padding = 10; // border와 내부 여백의 총합
            int displayWidth = profileLabel.getWidth() - padding;
            int displayHeight = profileLabel.getHeight() - padding;
            
            if (displayWidth <= 0) displayWidth = 110; // 120 - 10
            if (displayHeight <= 0) displayHeight = 110; // 120 - 10
            
            // 이미지 비율 유지하면서 크기 조정
            double imageRatio = (double) originalImage.getWidth(null) / originalImage.getHeight(null);
            double displayRatio = (double) displayWidth / displayHeight;
            
            int scaledWidth, scaledHeight;
            if (imageRatio > displayRatio) {
                scaledWidth = displayWidth;
                scaledHeight = (int) (displayWidth / imageRatio);
            } else {
                scaledHeight = displayHeight;
                scaledWidth = (int) (displayHeight * imageRatio);
            }
            
            Image scaledImage = originalImage.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
            profileLabel.setIcon(new ImageIcon(scaledImage));
            profileLabel.setHorizontalAlignment(SwingConstants.CENTER);
            profileLabel.setVerticalAlignment(SwingConstants.CENTER);
        });
    }

    public void addChatMessage(String message) {
        chatArea.append(message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void updateProfileInfo(String nickname, int wins, int losses) {
        nicknameLabel.setText(String.format("닉네임 %s", nickname));
        double winRate = (wins + losses) > 0 ? (double)wins/(wins + losses) * 100 : 0;
        // %.1f를 %.0f로 변경하여 소수점 제거
        statsLabel.setText(String.format("%d승 %d패 승률%.0f%%", wins, losses, winRate));
    }

    private void styleMenuItems() {
        JMenuItem[] menuItems = {viewInfo, changeInfo};
        for (JMenuItem item : menuItems) {
            themeManager.styleComponent(item);
        }
    }

    private void styleAllComponents() {
        // 모든 패널 스타일링
        JPanel[] panels = {
            mainPanel, sidePanel, roomListPanel, chatPanel, 
            eastPanel, groupChatListPanel, themeSelectionPanel
        };
        for (JPanel panel : panels) {
            if (panel != null) {
                themeManager.styleComponent(panel);
                // 패널 내부의 모든 컴포넌트도 재귀적으로 스타일링
                styleContainerComponents(panel);
            }
        }
        
        // 스크롤팬 스타일링
        JScrollPane[] scrollPanes = {
            rL_sp, cL_sp, chatScrollPane
        };
        for (JScrollPane scrollPane : scrollPanes) {
            if (scrollPane != null) {
                themeManager.styleComponent(scrollPane);
                if (scrollPane.getViewport() != null) {
                    scrollPane.getViewport().setBackground(themeManager.getCurrentSecondaryBgColor());
                }
            }
        }
        
        // 스트 스타일링
        JList<?>[] lists = {rList, cuList, groupChatList};
        for (JList<?> list : lists) {
            if (list != null) {
                list.setBackground(themeManager.getCurrentSecondaryBgColor());
                list.setForeground(themeManager.getCurrentTextColor());
            }
        }
        
        // 텍스트 컴포넌트 스타일링
        chatArea.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatArea.setForeground(themeManager.getCurrentTextColor());
        chatInput.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatInput.setForeground(themeManager.getCurrentTextColor());
        
        // 메뉴 스타일링
        mb.setBackground(themeManager.getCurrentSecondaryBgColor());
        infoMenu.setForeground(themeManager.getCurrentTextColor());
        styleMenuItems();
        
        // 테마 선택 콤보박스 스타일링
        if (themeComboBox != null) {
            styleComboBox(themeComboBox);
        }
        
        // 프로필 관련 스타일링
        if (profileLabel != null) {
            profileLabel.setBackground(themeManager.getCurrentSecondaryBgColor());
            profileLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)
            ));
        }
    }

    // 컨테이너 내부의 모든 컴포넌를 재귀적으로 스일링하는 헬퍼 메서드
    private void styleContainerComponents(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                themeManager.styleComponent((JComponent) comp);
                
                // 특별한 처리가 필요한 컴포넌트들
                if (comp instanceof JButton) {
                    comp.setBackground(themeManager.getCurrentAccentColor());
                    comp.setForeground(themeManager.getCurrentTextColor());
                } else if (comp instanceof JLabel) {
                    comp.setForeground(themeManager.getCurrentTextColor());
                } else if (comp instanceof JList) {
                    ((JList<?>) comp).setBackground(themeManager.getCurrentSecondaryBgColor());
                    ((JList<?>) comp).setForeground(themeManager.getCurrentTextColor());
                }
            }
            
            // 재귀적으로 하위 컨테이너의 컴포넌트들도 스타일링
            if (comp instanceof Container) {
                styleContainerComponents((Container) comp);
            }
        }
    }

    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(themeManager.getCurrentSecondaryBgColor());
        comboBox.setForeground(themeManager.getCurrentTextColor());
        ((JComponent) comboBox.getRenderer()).setBackground(themeManager.getCurrentSecondaryBgColor());
    }

    private String getCurrentThemeName() {
        return switch (themeManager.getCurrentTheme()) {
            case MORNING -> "아침";
            case NIGHT -> "밤";
            case SPRING -> "봄";
            case SUMMER -> "여름";
            case AUTUMN -> "가을";
            case WINTER -> "겨울";
        };
    }

    private Theme getThemeFromName(String name) {
        return switch (name) {
            case "아침" -> Theme.MORNING;
            case "밤" -> Theme.NIGHT;
            case "봄" -> Theme.SPRING;
            case "여름" -> Theme.SUMMER;
            case "가을" -> Theme.AUTUMN;
            case "겨울" -> Theme.WINTER;
            default -> Theme.MORNING;
        };
    }

    @Override
    public void dispose() {
        if (bgm != null) {
            bgm.close();
        }
        super.dispose();
    }

    private void initializeGroupChatComponents() {
        groupChatListPanel = new JPanel();
        groupChatListPanel.setLayout(new BorderLayout());
        groupChatListPanel.setBounds(240, 100, 680, 300);
        themeManager.styleComponent(groupChatListPanel);

        JLabel groupChatLabel = new JLabel("그룹 채팅방");
        groupChatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        themeManager.styleComponent(groupChatLabel);

        groupChatListModel = new DefaultListModel<>();
        groupChatList = new JList<>(groupChatListModel);
        groupChatList.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        themeManager.styleComponent(groupChatList);

        JScrollPane scrollPane = new JScrollPane(groupChatList);
        themeManager.styleComponent(scrollPane);

        createGroupChatButton = new JButton("그룹 채팅방 만들기");
        themeManager.styleComponent(createGroupChatButton);
        createGroupChatButton.addActionListener(e -> createGroupChat());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(createGroupChatButton);
        themeManager.styleComponent(buttonPanel);

        groupChatListPanel.add(groupChatLabel, BorderLayout.NORTH);
        groupChatListPanel.add(scrollPane, BorderLayout.CENTER);
        groupChatListPanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(groupChatListPanel);

        // 그룹 채팅방 더블클릭 이벤트
        groupChatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    enterSelectedGroupChat();
                }
            }
        });
    }

    private void createGroupChat() {
        String roomName = JOptionPane.showInputDialog(this, 
            "채팅방 이름을 입력하세요:", 
            "그룹 채팅방 생성", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (roomName != null && !roomName.trim().isEmpty()) {
            try {
                c.sendMsg(CREATE_GROUP_CHAT + "//" + roomName.trim());
                System.out.println("[Client] 그룹 채팅방 생 요청: " + roomName);
            } catch (Exception e) {
                System.out.println("[Client] 그룹 채팅방 생성 요청 실패: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "채팅방 생성에 실패했습니다.", 
                    "오류", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 그룹 채팅방 목록 업데이트
    public void updateGroupChatList(String[] rooms) {
        SwingUtilities.invokeLater(() -> {
            groupChatListModel.clear();
            for (String room : rooms) {
                groupChatListModel.addElement(room);
            }
        });
    }

    private void createGroupChatPanel() {
        JPanel groupChatPanel = new JPanel();
        groupChatPanel.setLayout(null);
        groupChatPanel.setBounds(480, 60, 150, 300);
        groupChatPanel.setBackground(themeManager.getCurrentMainBgColor());

        // 그룹 채팅방 라벨
        JLabel groupChatLabel = new JLabel("그룹 채팅방");
        groupChatLabel.setBounds(37, 10, 85, 25);
        groupChatLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        groupChatLabel.setForeground(themeManager.getCurrentTextColor());

        // 리스트 설정
        groupChatListModel = new DefaultListModel<>();
        groupChatList = new JList<>(groupChatListModel);
        groupChatList.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        groupChatList.setBackground(themeManager.getCurrentSecondaryBgColor());
        groupChatList.setForeground(themeManager.getCurrentTextColor());
        
        JScrollPane groupChatScrollPane = new JScrollPane(groupChatList);
        groupChatScrollPane.setBounds(10, 40, 130, 210);

        // 버튼 패널 - FlowLayout 대신 직접 위치 지정
        JButton enterGroupChatBtn = new JButton("입장");
        JButton createGroupChatBtn = new JButton("생성");

        // 버튼 크기와 위치 조정
        enterGroupChatBtn.setBounds(10, 260, 60, 25);
        createGroupChatBtn.setBounds(80, 260, 60, 25);

        // 버튼 스타일 설정
        themeManager.styleComponent(enterGroupChatBtn);
        themeManager.styleComponent(createGroupChatBtn);

        // 버튼 이벤트 설정
        enterGroupChatBtn.addActionListener(e -> enterSelectedGroupChat());
        createGroupChatBtn.addActionListener(e -> createGroupChat());

        // 컴포넌트 추가
        groupChatPanel.add(groupChatLabel);
        groupChatPanel.add(groupChatScrollPane);
        groupChatPanel.add(enterGroupChatBtn);
        groupChatPanel.add(createGroupChatBtn);

        mainPanel.add(groupChatPanel);

        // 더블클릭 이벤트 유지
        groupChatList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    enterSelectedGroupChat();
                }
            }
        });
    }

    // Client 클래스의 MessageListener에서 성공 응답 시
    private void handleGroupChatCreated(String roomName) {
        c.createGroupChatRoom(roomName);
    }

    // 선택된 채팅방 입장 메서드
    private void enterSelectedGroupChat() {
        String selectedRoom = groupChatList.getSelectedValue();
        if (selectedRoom != null) {
            c.joinGroupChat(selectedRoom);
            System.out.println("[Client] 그룹 채팅방 입장 요청: " + selectedRoom);
        } else {
            JOptionPane.showMessageDialog(this,
                "입장할 채팅방을 선택해주세요",
                "알림",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void showPasswordChangeReminder() {
        if (isTemporaryPassword) {  // 임시 비밀번호로 로그인한 경우
            JOptionPane.showMessageDialog(this,
                "임시 비밀번호로 로그인하셨습니다.\n보안을 위해 비밀번호를 변경해주세요.",
                "비밀번호 변경 안내",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    public void setTemporaryPassword(boolean isTemp) {
        this.isTemporaryPassword = isTemp;
    }

    public boolean isTemporaryPassword() {
        return this.isTemporaryPassword;
    }
}
