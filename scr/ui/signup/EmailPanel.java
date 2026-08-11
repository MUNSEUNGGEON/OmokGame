package ui.signup;

import java.awt.*;
import java.io.Serializable;
import java.util.regex.Pattern;
import javax.swing.*;

public class EmailPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField emailField;
    private JComboBox<String> domainComboBox;
    private JTextField customDomainField;

    public EmailPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 이메일 라벨
        JLabel emailLabel = new JLabel("이메일");
        emailLabel.setFont(new Font("굴림", Font.BOLD, 11));
        emailLabel.setBounds(10, 10, 70, 25);
        add(emailLabel);

        // 이메일 입력 필드
        emailField = new JTextField();
        emailField.setFont(new Font("굴림", Font.PLAIN, 12));
        emailField.setBounds(90, 10, 100, 25);
        add(emailField);

        // @ 레이블
        JLabel atLabel = new JLabel("@");
        atLabel.setFont(new Font("굴림", Font.PLAIN, 12));
        atLabel.setHorizontalAlignment(SwingConstants.CENTER);
        atLabel.setBounds(195, 10, 20, 25);
        add(atLabel);

        // 도메인 직접 입력 필드
        customDomainField = new JTextField();
        customDomainField.setFont(new Font("굴림", Font.PLAIN, 12));
        customDomainField.setBounds(220, 10, 120, 25);
        customDomainField.setVisible(false);  // 초기에는 숨김
        add(customDomainField);

        // 도메인 선택 콤보박스
        String[] domains = {"선택", "직접입력", "gmail.com", "naver.com", "daum.net", "kakao.com", "nate.com"};
        domainComboBox = new JComboBox<>(domains);
        domainComboBox.setFont(new Font("굴림", Font.PLAIN, 12));
        domainComboBox.setBounds(220, 10, 120, 25);
        add(domainComboBox);

        // 콤보박스 이벤트 리스너
        domainComboBox.addActionListener(e -> {
            String selected = (String) domainComboBox.getSelectedItem();
            if ("직접입력".equals(selected)) {
                domainComboBox.setVisible(false);
                customDomainField.setVisible(true);
                customDomainField.requestFocus();
            }
        });

        // 직접 입력 필드에서 포커스를 잃었을 때 처리
        customDomainField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (customDomainField.getText().trim().isEmpty()) {
                    domainComboBox.setSelectedIndex(0);  // "선택"으로 되돌림
                    customDomainField.setVisible(false);
                    domainComboBox.setVisible(true);
                }
            }
        });

        // 초기값 설정
        domainComboBox.setSelectedIndex(0);
    }

    // Getter 메소드
    public String getEmail() {
        String email = emailField.getText().trim();
        String domain;
        
        if (customDomainField.isVisible()) {
            domain = customDomainField.getText().trim();
        } else {
            String selected = (String) domainComboBox.getSelectedItem();
            domain = "선택".equals(selected) || "직접입력".equals(selected) ? "" : selected;
        }
        
        if (email.isEmpty() || domain.isEmpty()) {
            return "";
        }
        return email + "@" + domain;
    }

    // 입력 필드 초기화
    public void reset() {
        emailField.setText("");
        customDomainField.setText("");
        customDomainField.setVisible(false);
        domainComboBox.setVisible(true);
        domainComboBox.setSelectedIndex(0);
    }

    // 이메일 유효성 검사
    public boolean isValidEmail() {
        String email = getEmail();
        if (email.isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return Pattern.matches(emailRegex, email);
    }
}
