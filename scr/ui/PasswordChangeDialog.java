package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import core.Client;
import ui.signup.PasswordPanel;

public class PasswordChangeDialog extends JDialog {
    private Client client;
    private PasswordPanel passwordPanel;

    public PasswordChangeDialog(JFrame parent, Client client) {
        super(parent, "비밀번호 변경", true);
        this.client = client;

        // PasswordPanel 생성
        passwordPanel = new PasswordPanel();

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton changeButton = new JButton("변경");
        JButton cancelButton = new JButton("취소");

        buttonPanel.add(changeButton);
        buttonPanel.add(cancelButton);

        // 이벤트 리스너
        changeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePasswordChange();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 다이얼로그 닫기
            }
        });

        // 다이얼로그 레이아웃 설정
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(passwordPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(408, 160);
        setLocationRelativeTo(parent);
    }

    private void handlePasswordChange() {
        String newPassword = passwordPanel.getPassword();
        if (passwordPanel.isPasswordValid()) {
            client.sendPasswordChangeRequest(newPassword);
            // 서버 응답을 기다리지 않고 바로 종료하지 않도록 수정
            setVisible(false);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "비밀번호가 유효하지 않습니다. 요구사항을 확인해주세요.",
                "변경 실패",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
