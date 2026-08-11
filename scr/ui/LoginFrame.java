package ui;

import core.*;

import javax.swing.*;
import javax.swing.border.AbstractBorder;

import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    /* Panel */
    public JPanel mainPanel;
    public JPanel loginPanel = new JPanel();
    
    /* Label */
    public JLabel titleLabel;
    public JLabel idL = new JLabel("아이디");
    public JLabel pwL = new JLabel("비밀번호");

    /* TextField */
    public JTextField id = new JTextField();
    public JPasswordField pw = new JPasswordField();

    /* Button */
    public JButton loginBtn = new JButton("로그인");
    public JButton joinBtn = new JButton("회원가입");
    public JButton exitBtn = new JButton("게임종료");
    public JButton findIdBtn = new JButton("아이디 찾기");
    public JButton findPwBtn = new JButton("비밀번호 찾기");

    /* ComboBox */
    private JComboBox<String> themeSelector;

    /* Client */
    public Client c = null;
    final String loginTag = "LOGIN";

    ThemeManager themeManager;
    private WeatherWidget weatherWidget;

    public LoginFrame(Client _c) {
        c = _c;
        themeManager = new ThemeManager(this);

        setTitle("로그인");

        // 메인 패널 설정
        mainPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // 배경 그라데이션
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = themeManager.getCurrentMainBgColor();
                Color color2 = themeManager.getCurrentSecondaryBgColor();
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        // 타이틀 이미지 먼저 초기화
        ImageIcon titleIcon = new ImageIcon("./img/title.png");
        Image titleImage = titleIcon.getImage();
        Image scaledTitle = titleImage.getScaledInstance(400, 80, Image.SCALE_SMOOTH);
        titleLabel = new JLabel(new ImageIcon(scaledTitle));
        titleLabel.setBounds(100, 90, 400, 80);  // WeatherWidget을 고려한 위치
        mainPanel.add(titleLabel);

        // WeatherWidget 추가
        weatherWidget = new WeatherWidget();
        weatherWidget.setBounds(10, 10, 170, 80);
        mainPanel.add(weatherWidget);

        // 로그인 패널 설정 - 반투명 효과
        loginPanel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(255, 255, 255, 50));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        loginPanel.setBounds(100, 180, 400, 250);
        loginPanel.setLayout(null);
        loginPanel.setOpaque(false);
        mainPanel.add(loginPanel);

        setupComponents();
        setupThemeSelector();
        setupEventListeners();

        // 초기 테마 적
        themeManager.applyTheme(ThemeManager.Theme.MORNING);

        // 프레임 설정
        setSize(600, 510);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setupComponents() {    
        // 로그인 패널에 검정색 테두리와 굵기 설정
        loginPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 10));
        // 라벨 설정
        Font labelFont = new Font("맑은 고딕", Font.BOLD, 14);
        idL.setFont(labelFont);
        pwL.setFont(labelFont);
        
        // 라벨과 텍스트필드 위치 조정
        idL.setBounds(35, 40, 60, 30);
        pwL.setBounds(35, 80, 60, 30);
        
        id.setBounds(105, 40, 150, 30);  // 텍스트필드 너비 증가
        pw.setBounds(105, 80, 150, 30);  // 텍스트필드 너비 증가
        
        // 로그인 버튼 설정 - 둥근 모서리 적용
        loginBtn.setBounds(270, 40, 90, 70);  // 위치 조정
        styleMainButton(loginBtn);

        // 하단 버튼들 위치 조정 (20 아래로)
        findIdBtn.setBounds(70, 140, 125, 30);    
        findPwBtn.setBounds(210, 140, 125, 30);   
        joinBtn.setBounds(70, 180, 125, 30);     
        exitBtn.setBounds(210, 180, 125, 30);     

        // 컴포넌트 추가
        loginPanel.add(idL);
        loginPanel.add(pwL);
        loginPanel.add(id);
        loginPanel.add(pw);

        // 버튼 스타일링 및 추가
        JButton[] buttons = {loginBtn, joinBtn, exitBtn, findIdBtn, findPwBtn};
        for (JButton btn : buttons) {
            styleButton(btn);
            loginPanel.add(btn);
        }
    }

    private void styleTextField(JTextField textField) {
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(themeManager.getCurrentAccentColor(), 2),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        textField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        themeManager.styleComponent(textField);
    }

    private void styleMainButton(JButton button) {
        button.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(themeManager.getCurrentAccentColor());
        
        // 둥근 모서리 적용
        int radius = 15;  // 모서리 둥글기 정도
        button.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(radius, themeManager.getCurrentAccentColor()),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        button.setFocusPainted(false);
        
        // 마우스 호버 효과
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(button.getBackground().brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(themeManager.getCurrentAccentColor());
            }
        });
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        themeManager.styleComponent(button);
    }

    private void setupThemeSelector() {
        String[] themes = {
            "아침", "밤", "봄", "여름", "가을", "겨울"
        };
        themeSelector = new JComboBox<>(themes);
        // 테마 선택기를 메인 패널의 상단으로 이동
        themeSelector.setBounds(470, 10, 100, 25);
        mainPanel.add(themeSelector);  // loginPanel 대신 mainPanel에 추가
        themeManager.styleComponent(themeSelector);
        
        themeSelector.addActionListener(e -> {
            String selectedTheme = (String) themeSelector.getSelectedItem();
            switch (selectedTheme) {
                case "밤":
                    applyNewTheme(ThemeManager.Theme.NIGHT);
                    break;
                case "아침":
                    applyNewTheme(ThemeManager.Theme.MORNING);
                    break;
                case "봄":
                    applyNewTheme(ThemeManager.Theme.SPRING);
                    break;
                case "여름":
                    applyNewTheme(ThemeManager.Theme.SUMMER);
                    break;
                case "가을":
                    applyNewTheme(ThemeManager.Theme.AUTUMN);
                    break;
                case "겨울":
                    applyNewTheme(ThemeManager.Theme.WINTER);
                    break;
            }
        });
    }

    private void applyNewTheme(ThemeManager.Theme theme) {
        themeManager.applyTheme(theme);
        
        // 모든 컴포넌트 다시 스타일링
        themeManager.styleComponent(mainPanel);
        themeManager.styleComponent(loginPanel);
        
        // 라벨 다시 스타일링
        themeManager.styleComponent(idL);
        themeManager.styleComponent(pwL);
        
        // 텍스트필드 다시 스타일링
        styleTextField(id);
        styleTextField(pw);
        
        // 버튼들 다시 스타일링
        styleMainButton(loginBtn);
        JButton[] buttons = {joinBtn, exitBtn, findIdBtn, findPwBtn};
        for (JButton btn : buttons) {
            styleButton(btn);
        }
        
        // 테마 선택기 다시 스타일링
        themeManager.styleComponent(themeSelector);
        
        // 프레임 갱신
        SwingUtilities.updateComponentTreeUI(this);
        repaint();
    }

    private void setupEventListeners() {
        findIdBtn.addActionListener(new FindButtonListener());
        findPwBtn.addActionListener(new FindButtonListener());
        loginBtn.addActionListener(new ButtonListener());
        exitBtn.addActionListener(new ButtonListener());
        joinBtn.addActionListener(new ButtonListener());
        id.addKeyListener(new KeyBoardListener());
        pw.addKeyListener(new KeyBoardListener());
    }

    /* Button 이벤트 리스너 */
    class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton) e.getSource();
            String uid = id.getText();
            String upass = new String(pw.getPassword());
            
            if (b == exitBtn) {
                System.out.println("[Client] 게임 종료");
                System.exit(0);
            }
            else if (b == joinBtn) {
                System.out.println("[Client] 회원가입 인터페이스 림");
                c.jf.setVisible(true);
            }
            else if (b == loginBtn) {
                handleLogin(uid, upass);
            }
        }
    }

    /* Key 이벤트 리스너 */
    class KeyBoardListener implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                String uid = id.getText();
                String upass = new String(pw.getPassword());
                handleLogin(uid, upass);
            }
        }

        public void keyTyped(KeyEvent e) {}
        public void keyReleased(KeyEvent e) {}
    }

    /* 찾기 버튼 이벤트 리스너 */
    class FindButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton source = (JButton) e.getSource();
            if (source == findIdBtn) {
                FindIdDialog findIdDialog = new FindIdDialog(LoginFrame.this);
                findIdDialog.setVisible(true);
            } else if (source == findPwBtn) {
                FindPwDialog findPwDialog = new FindPwDialog(LoginFrame.this);
                findPwDialog.setVisible(true);
            }
        }
    }

    /* 로그인 처리 메소드 */
    private void handleLogin(String uid, String upass) {
        if (uid.isEmpty() && !upass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "아이디를 입력해주세요", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            System.out.println("[Client] 로그인 실패 : 아이디 미입력");
        }
        else if (!uid.isEmpty() && upass.isEmpty()) {
            JOptionPane.showMessageDialog(null, "비밀번호를 입력해주세요", "로그인 실패", JOptionPane.ERROR_MESSAGE);
            System.out.println("[Client] 로그인 실패 : 비밀번호 미입력");
        }
        else if (!uid.isEmpty() && !upass.isEmpty()) {
            c.sendMsg(loginTag + "//" + uid + "//" + upass);
        }
    }

    // 둥근 모서리를 위한 커스텀 Border 클래스
    private static class RoundedBorder extends AbstractBorder {
        private static final long serialVersionUID = 1L;
        private final int radius;
        private final Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(this.radius / 2, this.radius / 2, this.radius / 2, this.radius / 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }
    }
}
