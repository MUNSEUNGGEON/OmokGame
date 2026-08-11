package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class NamePanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField nameField;

    public NamePanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 이름 라벨
        JLabel nameLabel = new JLabel("이름");
        nameLabel.setFont(new Font("굴림", Font.BOLD, 11));
        nameLabel.setBounds(10, 10, 70, 25);
        add(nameLabel);

        // 이름 입력 필드
        nameField = new JTextField();
        nameField.setFont(new Font("굴림", Font.PLAIN, 12));
        nameField.setBounds(90, 10, 150, 25);
        add(nameField);
    }

    public String getName() {
        return nameField.getText().trim();
    }

    public boolean isValidName() {
        String name = getName();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "이름을 입력해주세요.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        return true;
    }
    
    public void reset() {
        nameField.setText("");
    }
}
