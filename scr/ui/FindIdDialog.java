package ui;

import java.awt.*;
import javax.swing.*;

import core.MessageType;
import ui.signup.*;

public class FindIdDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private NamePanel namePanel;
    private EmailPanel emailPanel;
    private BirthPanel birthPanel;
    private JButton findButton;
    private JButton cancelButton;
    private LoginFrame loginFrame;
    private ThemeManager themeManager;

    public FindIdDialog(LoginFrame parent) {
        super(parent, "아이디 찾기", true);
        this.loginFrame = parent;
        this.themeManager = parent.themeManager;
        
        setLayout(new BorderLayout());
        
        // 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 1, 0, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        themeManager.styleComponent(inputPanel);
        
        namePanel = new NamePanel();
        emailPanel = new EmailPanel();
        birthPanel = new BirthPanel();
        
        themeManager.styleComponent(namePanel);
        themeManager.styleComponent(emailPanel);
        themeManager.styleComponent(birthPanel);
        
        inputPanel.add(namePanel);
        inputPanel.add(emailPanel);
        inputPanel.add(birthPanel);
        
        // 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        themeManager.styleComponent(buttonPanel);
        
        findButton = new JButton("찾기");
        cancelButton = new JButton("취소");
        
        findButton.setBounds(130, 10, 80, 30);
        cancelButton.setBounds(220, 10, 80, 30);
        findButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.setPreferredSize(new Dimension(80, 30));
        
        themeManager.styleComponent(findButton);
        themeManager.styleComponent(cancelButton);
        
        findButton.addActionListener(e -> findId());
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(findButton);
        buttonPanel.add(cancelButton);
        
        inputPanel.add(buttonPanel);
        
        add(inputPanel, BorderLayout.CENTER);
        
        setSize(393, 227);
        setLocationRelativeTo(parent);
        
        getContentPane().setBackground(themeManager.getCurrentMainBgColor());
        applyTheme();
    }
    
    private void applyTheme() {
        getContentPane().setBackground(themeManager.getCurrentMainBgColor());
        
        for (Component comp : getContentPane().getComponents()) {
            if (comp instanceof JPanel) {
                applyThemeToPanel((JPanel) comp);
            }
        }
    }
    
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
    
    private void findId() {
        String name = namePanel.getName();
        String email = emailPanel.getEmail();
        String birthDate = birthPanel.getBirthDate();
        
        if (name.isEmpty() || !emailPanel.isValidEmail() || !birthPanel.isValidBirthDate()) {
            JOptionPane.showMessageDialog(this, "모든 정보를 올바르게 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 서버로 메시지 전송
        loginFrame.c.sendMsg(MessageType.FIND_ID + "//" + name + "//" + email + "//" + birthDate);
    }
} 