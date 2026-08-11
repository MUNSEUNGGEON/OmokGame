package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class PhonePanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField phoneField, phone1Field;
    private JComboBox<String> phonePrefixComboBox;
    private JLabel phoneLabel;
    private JLabel hyphen1, hyphen2;

    public PhonePanel() {
        initialize();
    }

    private void initialize() {
        setLayout(null);
        setBounds(0, 0, 400, 40);

        // 전화번호 라벨
        phoneLabel = new JLabel("전화번호");
        phoneLabel.setFont(new Font("굴림", Font.BOLD, 12));
        phoneLabel.setBounds(10, 10, 70, 25);
        add(phoneLabel);

        // 전화번호 앞자리 콤보박스
        phonePrefixComboBox = new JComboBox<>(new String[]{"010", "011", "016"});
        phonePrefixComboBox.setFont(new Font("굴림", Font.PLAIN, 12));
        phonePrefixComboBox.setBounds(90, 10, 55, 25);
        add(phonePrefixComboBox);

        // 첫 번째 하이픈
        hyphen1 = new JLabel("-");
        hyphen1.setFont(new Font("굴림", Font.PLAIN, 12));
        hyphen1.setHorizontalAlignment(SwingConstants.CENTER);
        hyphen1.setBounds(150, 10, 15, 25);
        add(hyphen1);

        // 중간 번호 필드
        phoneField = new JTextField();
        phoneField.setFont(new Font("굴림", Font.PLAIN, 12));
        phoneField.setBounds(170, 10, 45, 25);
        add(phoneField);

        // 두 번째 하이픈
        hyphen2 = new JLabel("-");
        hyphen2.setFont(new Font("굴림", Font.PLAIN, 12));
        hyphen2.setHorizontalAlignment(SwingConstants.CENTER);
        hyphen2.setBounds(220, 10, 15, 25);
        add(hyphen2);

        // 마지막 번호 필드
        phone1Field = new JTextField();
        phone1Field.setFont(new Font("굴림", Font.PLAIN, 12));
        phone1Field.setBounds(240, 10, 45, 25);
        add(phone1Field);
    }

    public String getPhone() {
        return phonePrefixComboBox.getSelectedItem() + "-" + phoneField.getText() + "-" + phone1Field.getText();
    }

    public void reset() {
        phoneField.setText("");
        phone1Field.setText("");
        phonePrefixComboBox.setSelectedIndex(0);
    }
}