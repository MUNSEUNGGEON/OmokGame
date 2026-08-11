package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import DB.Database;

public class NicknamePanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField nicknameField;
    private JButton checkNicknameButton;

    public NicknamePanel(Database database) {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 닉네임 라벨
        JLabel nicknameLabel = new JLabel("닉네임");
        nicknameLabel.setFont(new Font("굴림", Font.BOLD, 11));
        nicknameLabel.setBounds(10, 10, 70, 25);
        add(nicknameLabel);

        // 닉네임 입력 필드
        nicknameField = new JTextField();
        nicknameField.setFont(new Font("굴림", Font.PLAIN, 12));
        nicknameField.setBounds(90, 10, 150, 25);
        add(nicknameField);

        // 중복 확인 버튼
        checkNicknameButton = new JButton("중복 확인");
        checkNicknameButton.setFont(new Font("굴림", Font.PLAIN, 12));
        checkNicknameButton.setBounds(250, 10, 100, 25);
        add(checkNicknameButton);

        // 중복 확인 버튼 이벤트
        checkNicknameButton.addActionListener(e -> {
            String nickname = getNickname();
            if (database.isNicknameTaken(nickname)) {
                JOptionPane.showMessageDialog(this, "이미 사용 중인 닉네임입니다.", "중복 확인", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 닉네임입니다.", "중복 확인", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public String getNickname() {
        return nicknameField.getText();
    }

    public JButton getCheckNicknameButton() {
        return checkNicknameButton;
    }

    public void reset() {
        nicknameField.setText("");
    }
}
