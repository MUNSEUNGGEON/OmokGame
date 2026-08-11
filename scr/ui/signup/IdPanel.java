package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import DB.Database;

public class IdPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField idField;
    private JButton checkIdButton;

    public IdPanel(Database database) {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 아이디 라벨
        JLabel idLabel = new JLabel("아이디");
        idLabel.setFont(new Font("굴림", Font.BOLD, 11));
        idLabel.setBounds(10, 10, 70, 25);
        add(idLabel);

        // 아이디 입력 필드
        idField = new JTextField();
        idField.setFont(new Font("굴림", Font.PLAIN, 12));
        idField.setBounds(90, 10, 150, 25);
        add(idField);

        // 중복 확인 버튼
        checkIdButton = new JButton("중복 확인");
        checkIdButton.setFont(new Font("굴림", Font.PLAIN, 12));
        checkIdButton.setBounds(250, 10, 100, 25);
        add(checkIdButton);

        // 중복 확인 버튼 이벤트
        checkIdButton.addActionListener(e -> {
            String id = getId();
            if (database.isIdTaken(id)) {
                JOptionPane.showMessageDialog(this, "이미 사용 중인 아이디입니다.", "중복 확인", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "사용 가능한 아이디입니다.", "중복 확인", JOptionPane.INFORMATION_MESSAGE);
            }
        });
    }

    public String getId() {
        return idField.getText();
    }

    public JButton getCheckIdButton() {
        return checkIdButton;
    }

    public void reset() {
        idField.setText("");
    }
}
