package ui.signup;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.swing.*;

public class PasswordPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JProgressBar passwordStrengthBar;
    private JLabel confirmPasswordFeedbackLabel;
    private JLabel passwordRequirementLabel;
    
    public PasswordPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(401, 101));

        // JLayeredPane 생성
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 400, 100);
        add(layeredPane);

        // 먼저 모든 기본 컴포넌트들을 DEFAULT_LAYER에 추가
        JLabel passwordLabel = new JLabel("비밀번호");
        passwordLabel.setFont(new Font("굴림", Font.BOLD, 11));
        passwordLabel.setBounds(10, 10, 70, 25);
        layeredPane.add(passwordLabel, JLayeredPane.DEFAULT_LAYER);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("굴림", Font.PLAIN, 12));
        passwordField.setBounds(90, 10, 150, 25);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            passwordField.getBorder(),
            BorderFactory.createEmptyBorder(0, 5, 0, 25)
        ));
        layeredPane.add(passwordField, JLayeredPane.DEFAULT_LAYER);

        JLabel confirmPasswordLabel = new JLabel("비밀번호 확인");
        confirmPasswordLabel.setFont(new Font("굴림", Font.BOLD, 11));
        confirmPasswordLabel.setBounds(10, 45, 75, 25);
        layeredPane.add(confirmPasswordLabel, JLayeredPane.DEFAULT_LAYER);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setFont(new Font("굴림", Font.PLAIN, 10));
        confirmPasswordField.setBounds(90, 45, 150, 25);
        confirmPasswordField.setBorder(BorderFactory.createCompoundBorder(
            confirmPasswordField.getBorder(),
            BorderFactory.createEmptyBorder(0, 5, 0, 25)
        ));
        layeredPane.add(confirmPasswordField, JLayeredPane.DEFAULT_LAYER);

        passwordStrengthBar = new JProgressBar(0, 100);
        passwordStrengthBar.setStringPainted(true);
        passwordStrengthBar.setBounds(250, 10, 100, 25);
        layeredPane.add(passwordStrengthBar, JLayeredPane.DEFAULT_LAYER);

        passwordRequirementLabel = new JLabel("8자 이상 입력해주세요");
        passwordRequirementLabel.setFont(new Font("굴림", Font.PLAIN, 10));
        passwordRequirementLabel.setForeground(Color.RED);
        passwordRequirementLabel.setBounds(250, 45, 200, 25);
        layeredPane.add(passwordRequirementLabel, JLayeredPane.DEFAULT_LAYER);

        // 이모지를 POPUP_LAYER(더 높은 레이어)에 추가
        JLabel showPasswordIcon = new JLabel("👁");
        showPasswordIcon.setForeground(Color.BLACK);
        showPasswordIcon.setBounds(215, 14, 20, 17);
        showPasswordIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        layeredPane.add(showPasswordIcon, JLayeredPane.POPUP_LAYER);

        JLabel showConfirmIcon = new JLabel("👁");
        showConfirmIcon.setForeground(Color.BLACK);
        showConfirmIcon.setBounds(215, 49, 20, 17);
        showConfirmIcon.setCursor(new Cursor(Cursor.HAND_CURSOR));
        layeredPane.add(showConfirmIcon, JLayeredPane.POPUP_LAYER);

        // 이벤트 리스너 설정
        showPasswordIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                passwordField.setEchoChar((char) 0);
                showPasswordIcon.setText("👀");
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                passwordField.setEchoChar('●');
                showPasswordIcon.setText("👁");
            }
        });

        showConfirmIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                confirmPasswordField.setEchoChar((char) 0);
                showConfirmIcon.setText("👀");
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                confirmPasswordField.setEchoChar('●');
                showConfirmIcon.setText("👁");
            }
        });

        // DocumentListener 설정은 그대로 유지
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validatePassword(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validatePassword(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validatePassword(); }
        });

        confirmPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateConfirmPassword(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateConfirmPassword(); }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateConfirmPassword(); }
        });
    }

    public void validatePassword() {
        String password = new String(passwordField.getPassword());
        int score = 0;

        // 순차적으로 각 조건 체크
        if (password.length() < 8) {
            passwordRequirementLabel.setText("8자 이상 입력해주세요");
            score = 20;
        } 
        else if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            passwordRequirementLabel.setText("대문자를 포함해주세요");
            score = 40;
        }
        else if (!Pattern.compile("[a-z]").matcher(password).find()) {
            passwordRequirementLabel.setText("소문자를 포함해주세요");
            score = 60;
        }
        else if (!Pattern.compile("\\d").matcher(password).find()) {
            passwordRequirementLabel.setText("숫자를 포함해주세요");
            score = 80;
        }
        else if (!Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find()) {
            passwordRequirementLabel.setText("특수문자를 포함해주세요");
            score = 90;
        }
        else {
            passwordRequirementLabel.setText("사용 가능한 비밀번호입니다");
            passwordRequirementLabel.setForeground(Color.GREEN);
            score = 100;
        }

        // 프로그레스 바 업데이트
        passwordStrengthBar.setValue(score);
        passwordStrengthBar.setString(getPasswordFeedback(score));
        passwordStrengthBar.setForeground(getStrengthColor(score));
    }

    private void validateConfirmPassword() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        if (!password.equals(confirmPassword)) {
            confirmPasswordField.setBackground(Color.PINK);
            passwordRequirementLabel.setText("비밀번호가 일치하지 않습니다");
            passwordRequirementLabel.setForeground(Color.RED);
        } else {
            confirmPasswordField.setBackground(Color.WHITE);
            validatePassword(); // 원래의 비밀번호 요구사항 메시지로 돌아감
        }
    }

    private String getPasswordFeedback(int score) {
        if (score == 100) return "매우 강함";
        if (score >= 80) return "강함";
        if (score >= 60) return "보통";
        if (score >= 40) return "약함";
        return "매우 약함";
    }

    private Color getStrengthColor(int score) {
        if (score < 40) return new Color(255, 0, 0);     // 빨강
        if (score < 60) return new Color(255, 128, 0);   // 주황
        if (score < 80) return new Color(255, 255, 0);   // 노랑
        if (score < 100) return new Color(128, 255, 0);  // 연두
        return new Color(0, 255, 0);                     // 초록
    }

    public boolean isPasswordValid() {
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // 모든 조건 검사
        boolean isLengthValid = password.length() >= 8;
        boolean hasUppercase = Pattern.compile("[A-Z]").matcher(password).find();
        boolean hasLowercase = Pattern.compile("[a-z]").matcher(password).find();
        boolean hasNumber = Pattern.compile("\\d").matcher(password).find();
        boolean hasSpecialChar = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]").matcher(password).find();
        boolean passwordsMatch = password.equals(confirmPassword);

        return isLengthValid && hasUppercase && hasLowercase && 
               hasNumber && hasSpecialChar && passwordsMatch;
    }

    // Getter 메서드
    public String getPassword() {
        return new String(passwordField.getPassword());
    }

    // Reset 메서드
    public void reset() {
        passwordField.setText("");
        confirmPasswordField.setText("");
        passwordRequirementLabel.setText("8자 이상 입력해주세요");
        passwordRequirementLabel.setForeground(Color.RED);
        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setString("매우 약함");
        
        // 배경색 초기화
        passwordField.setBackground(Color.WHITE);
        confirmPasswordField.setBackground(Color.WHITE);
    }
}
