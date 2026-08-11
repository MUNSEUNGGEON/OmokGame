package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class AddressPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField postalField, addressField, detailAddressField;

    public AddressPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 120));

        // 우편번호 라벨
        JLabel postalLabel = new JLabel("우편번호");
        postalLabel.setFont(new Font("굴림", Font.BOLD, 11));
        postalLabel.setBounds(10, 10, 70, 25);
        add(postalLabel);

        // 우편번호 입력 필드 (읽기 전용)
        postalField = new JTextField();
        postalField.setFont(new Font("굴림", Font.PLAIN, 12));
        postalField.setBounds(90, 10, 150, 25);
        postalField.setEditable(false);  // 읽기 전용으로 설정
        postalField.setBackground(Color.WHITE);  // 배경색은 흰색 유지
        add(postalField);

        // 우편번호 검색 버튼
        JButton searchButton = new JButton("우편번호");
        searchButton.setFont(new Font("굴림", Font.PLAIN, 12));
        searchButton.setBounds(250, 10, 100, 25);
        searchButton.setBackground(new Color(64, 64, 64));  // 버튼 색상 설정
        searchButton.setForeground(Color.WHITE);  // 버튼 텍스트 색상
        searchButton.setFocusPainted(false);  // 포커스 테두리 제거
        add(searchButton);

        // 주소 라벨
        JLabel addressLabel = new JLabel("주소");
        addressLabel.setFont(new Font("굴림", Font.BOLD, 11));
        addressLabel.setBounds(10, 45, 70, 25);
        add(addressLabel);

        // 주소 입력 필드
        addressField = new JTextField();
        addressField.setFont(new Font("굴림", Font.PLAIN, 12));
        addressField.setBounds(90, 45, 260, 25);
        addressField.setEditable(false);  // 읽기 전용으로 설정
        addressField.setBackground(Color.WHITE);
        add(addressField);

        // 상세주소 라벨
        JLabel detailLabel = new JLabel("상세주소");
        detailLabel.setFont(new Font("굴림", Font.BOLD, 11));
        detailLabel.setBounds(10, 80, 70, 25);
        add(detailLabel);

        // 상세주소 입력 필드
        detailAddressField = new JTextField();
        detailAddressField.setFont(new Font("굴림", Font.PLAIN, 12));
        detailAddressField.setBounds(90, 80, 260, 25);
        add(detailAddressField);

        // 버튼 호버 효과
        searchButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(new Color(96, 96, 96));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                searchButton.setBackground(new Color(64, 64, 64));
            }
        });

        // 우편번호 검색 버튼 이벤트
        searchButton.addActionListener(e -> {
            AddressSearchFrame searchFrame = new AddressSearchFrame(postalField, addressField);
            searchFrame.setVisible(true);
        });
    }

    // Getter 메서드들
    public String getPostal() {
        return postalField.getText();
    }

    public String getAddress() {
        return addressField.getText();
    }

    public String getDetailAddress() {
        return detailAddressField.getText();
    }

    // Reset 메서드
    public void reset() {
        postalField.setText("");
        addressField.setText("");
        detailAddressField.setText("");
    }
}
