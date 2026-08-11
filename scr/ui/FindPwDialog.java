package ui;

// 필요한 패키지와 클래스 임포트
import ui.signup.*;
import javax.swing.*;

import core.MessageType;

import java.awt.*;

// 비밀번호 재설정을 위한 JDialog 클래스 정의
public class FindPwDialog extends JDialog {
    private static final long serialVersionUID = 1L;  // 직렬화에 사용되는 고유 ID
    private JTextField idField;  // 사용자 ID 입력 필드
    private JLabel idLabel;  // ID 입력 필드에 대한 라벨
    private EmailPanel emailPanel;  // 이메일 입력 패널
    private BirthPanel birthPanel;  // 생년월일 입력 패널
    private PasswordPanel passwordPanel;  // 새 비밀번호 입력 패널
    private JButton resetButton;  // "다음" 버튼
    private JButton cancelButton;  // "취소" 버튼
    private LoginFrame loginFrame;  // 부모 프레임 참조
    private JPanel inputPanel;  // 사용자 입력 패널
    private CardLayout cardLayout;  // 카드 레이아웃 (패널 전환용)
    private JPanel cardPanel;  // 카드 레이아웃을 적용할 패널
    private ThemeManager themeManager;  // 테마 관리 객체
    private JTextField textField;

    // 생성자: 부모 프레임을 기반으로 다이얼로그 초기화
    public FindPwDialog(LoginFrame parent) {
        super(parent, "비밀번호 재설정", true);  // 모달 다이얼로그 생성
        this.loginFrame = parent;  // 부모 프레임 참조 설정
        this.themeManager = parent.themeManager;  // 부모의 테마 관리자 가져오기
        
        getContentPane().setLayout(new BorderLayout());  // 기본 레이아웃 설정
        
        cardLayout = new CardLayout();  // 카드 레이아웃 초기화
        cardPanel = new JPanel(cardLayout);  // 카드 패널 생성
        
        // 첫 번째 패널 설정 (사용자 확인용)
        setupVerificationPanel();
        
        // 두 번째 패널 생성 (비밀번호 재설정용)
        JPanel resetPanel = createResetPanel();
        
        // 카드 패널에 두 개의 패널 추가
        cardPanel.add(inputPanel, "verification");  // 첫 번째 패널 추가
        cardPanel.add(resetPanel, "reset");  // 두 번째 패널 추가
        
        getContentPane().add(cardPanel, BorderLayout.CENTER);  // 카드 패널을 다이얼로그에 추가
        
        setSize(393, 227);  // 다이얼로그 크기 설정
        setLocationRelativeTo(parent);  // 부모 프레임을 기준으로 다이얼로그 위치 설정
        
        // 테마 적용 메서드 호출
        applyTheme();
    }
    
    // 테마를 다이얼로그와 그 내부 컴포넌트에 적용
    private void applyTheme() {
        // 다이얼로그 배경색 설정
        getContentPane().setBackground(themeManager.getCurrentMainBgColor());
        cardPanel.setBackground(themeManager.getCurrentMainBgColor());
        
        // 입력 패널 스타일 적용
        themeManager.styleComponent(inputPanel);
        themeManager.styleComponent(cardPanel);
        
        // 버튼 스타일 적용
        themeManager.styleComponent(resetButton);
        themeManager.styleComponent(cancelButton);
        
        // 패널 내 모든 컴포넌트에 테마 적용
        for (Component comp : cardPanel.getComponents()) {
            if (comp instanceof JPanel) {
                applyThemeToPanel((JPanel) comp);
            }
        }
    }
    
    // 패널 내부의 모든 컴포넌트에 테마 적용
    private void applyThemeToPanel(JPanel panel) {
        themeManager.styleComponent(panel);
        for (Component comp : panel.getComponents()) {
            if (comp instanceof JComponent) {
                themeManager.styleComponent((JComponent) comp);
            }
            if (comp instanceof JPanel) {
                applyThemeToPanel((JPanel) comp);
            }
        }
    }
    
    // 사용자 확인 패널 설정 메서드
    private void setupVerificationPanel() {
        inputPanel = new JPanel();  // 사용자 입력 패널 생성
        inputPanel.setLayout(new GridLayout(4, 1, 0, 5));  // 그리드 레이아웃 설정
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  // 여백 추가
        
        // 아이디 입력 패널 생성
        JPanel idPanel = new JPanel();
        idLabel = new JLabel("아이디");
        idLabel.setBounds(10, 10, 70, 25);
        idLabel.setFont(new Font("굴림", Font.BOLD, 11));  // 폰트 설정
        idLabel.setPreferredSize(new Dimension(60, 25));  // 선호 크기 설정
        idField = new JTextField();
        idField.setBounds(90, 10, 150, 25);
        idPanel.setLayout(null);
        
        // 패널에 라벨과 필드 추가
        idPanel.add(idLabel);
        idPanel.add(idField);
        themeManager.styleComponent(idPanel);  // 테마 적용
        themeManager.styleComponent(idLabel);  // 테마 적용
        themeManager.styleComponent(idField);  // 테마 적용
        
        // 이메일 및 생년월일 패널 추가
        emailPanel = new EmailPanel();
        birthPanel = new BirthPanel();
        
        // 입력 패널에 추가
        inputPanel.add(idPanel);
        inputPanel.add(emailPanel);
        inputPanel.add(birthPanel);
        
        // 버튼 패널 생성 및 설정
        JPanel buttonPanel = new JPanel();
        themeManager.styleComponent(buttonPanel);  // 테마 적용
        
        resetButton = new JButton("다음");  // "다음" 버튼 생성
        resetButton.setBounds(130, 10, 80, 30);  // 위치 및 크기 설정
        cancelButton = new JButton("취소");  // "취소" 버튼 생성
        cancelButton.setBounds(220, 10, 80, 30);  // 위치 및 크기 설정
        resetButton.setPreferredSize(new Dimension(80, 30));  // 선호 크기 설정
        cancelButton.setPreferredSize(new Dimension(80, 30));  // 선호 크기 설정
        
        // 버튼에 테마 적용
        themeManager.styleComponent(resetButton);
        themeManager.styleComponent(cancelButton);
        
        // 버튼에 액션 리스너 추가
        resetButton.addActionListener(e -> verifyUser());  // "다음" 버튼 클릭 시 사용자 검증
        cancelButton.addActionListener(e -> dispose());  // "취소" 버튼 클릭 시 다이얼로그 닫기
        buttonPanel.setLayout(null);  // 자유 레이아웃 설정
        
        // 버튼 패널에 버튼 추가
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);
        
        inputPanel.add(buttonPanel);  // 버튼 패널을 입력 패널에 추가
    }
    
 // 비밀번호 재설정 패널 생성 메서드
    private JPanel createResetPanel() {
        JPanel resetPanel = new JPanel();  // 새 패널 생성
        resetPanel.setLayout(new BorderLayout());  // BorderLayout 설정
        resetPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));  // 여백 설정
        themeManager.styleComponent(resetPanel);  // 테마 적용
        
        // 비밀번호 패널 생성
        passwordPanel = new PasswordPanel();
        
        // 새로운 안내 패널 생성
        JPanel infoPanel = new JPanel();  // 안내 패널 생성
        infoPanel.setLayout(new FlowLayout(FlowLayout.CENTER));  // 중앙 정렬
        infoPanel.setPreferredSize(new Dimension(0, 50)); // 높이를 50픽셀로 설정
        JLabel infoLabel = new JLabel(" ");  // 안내 라벨 생성
        themeManager.styleComponent(infoLabel);  // 라벨에 테마 적용
        infoPanel.add(infoLabel);  // 패널에 라벨 추가
        themeManager.styleComponent(infoPanel);  // 패널에 테마 적용
        
        // resetPanel에 비밀번호 패널, 안내 패널과 버튼 패널 추가
        resetPanel.add(infoPanel, BorderLayout.NORTH);  // 안내 패널 추가
        resetPanel.add(passwordPanel, BorderLayout.CENTER);  // 비밀번호 패널 추가
        passwordPanel.setLayout(null);
        
        // 버튼 패널 생성 및 설정
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBounds(0, 95, 401, 60);
        passwordPanel.add(buttonPanel);
        themeManager.styleComponent(buttonPanel);  // 테마 적용
        
        JButton confirmButton = new JButton("변경");  // "변경" 버튼 생성
        confirmButton.setBounds(110, 15, 80, 30);
        JButton backButton = new JButton("이전");  // "이전" 버튼 생성
        backButton.setBounds(200, 15, 80, 30);
        confirmButton.setPreferredSize(new Dimension(80, 30));  // 선호 크기 설정
        backButton.setPreferredSize(new Dimension(80, 30));  // 선호 크기 설정
        
        // 버튼에 테마 적용
        themeManager.styleComponent(confirmButton);
        themeManager.styleComponent(backButton);
        
        // 버튼에 액션 리스너 추가
        confirmButton.addActionListener(e -> resetPassword());  // "변경" 버튼 클릭 시 비밀번호 재설정
        backButton.addActionListener(e -> cardLayout.show(cardPanel, "verification"));  // "이전" 버튼 클릭 시 패널 전환
        buttonPanel.setLayout(null);
        
        // 버튼 패널에 버튼 추가
        buttonPanel.add(confirmButton);
        buttonPanel.add(backButton);

        return resetPanel;  // 완성된 패널 반환
    }

    
    // 사용자 검증 메서드
    private void verifyUser() {
        String id = idField.getText().trim();
        String email = emailPanel.getEmail();
        String birthDate = birthPanel.getBirthDate();
        
        // 입력값 검증
        if (id.isEmpty() || !emailPanel.isValidEmail() || !birthPanel.isValidBirthDate()) {
            JOptionPane.showMessageDialog(this, 
                "모든 정보를 올바르게 입력해주세요.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 서버에 임시 비밀번호 발급 요청
        loginFrame.c.sendMsg(MessageType.FIND_PW + "//" + id + "//" + email + "//" + birthDate);
    }
    
    // 비밀번호 재설정 메서드
    private void resetPassword() {
        // 비밀번호 유효성 검사
        if (!passwordPanel.isPasswordValid()) {
            // 경고 메시지 표시
            JOptionPane.showMessageDialog(this, 
                "비밀번호 요구사항을 모두 충족해주세요.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return;  // 유효하지 않으면 종료
        }
        
        String id = idField.getText().trim();  // ID 값 가져오기
        String newPassword = passwordPanel.getPassword();  // 새 비밀번호 가져오기
        
        // 서버에 비밀번호 재설정 요청 메시지 전송
        loginFrame.c.sendMsg(MessageType.RESET_PW + "//" + id + "//" + newPassword);
    }
    
    // 사용자 검증이 성공했을 때 호출되는 메서드
    public void showResetPanel() {
        cardLayout.show(cardPanel, "reset");  // 비밀번호 재설정 패널로 전환
        
        // 비밀번호 패널의 필드를 초기화
        passwordPanel.getPassword();  // 비밀번호 필드 초기화
        passwordPanel.validatePassword();  // 비밀번호 유효성 검사 초기화
    }
}
