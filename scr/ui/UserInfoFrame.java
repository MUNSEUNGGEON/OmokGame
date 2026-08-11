package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class UserInfoFrame extends JFrame {
    private JLabel profileImageLabel;
    private JLabel nicknameLabel;
    private JLabel statsLabel;
    private JLabel winRateLabel;
    private Color backgroundColor = new Color(248, 248, 248);
    private Color textColor = new Color(51, 51, 51);
    private Color accentColor = new Color(70, 130, 180);

    public UserInfoFrame() {
        setTitle("사용자 정보");
        setSize(300, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);
        
        // 메인 패널 생성
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        mainPanel.setBackground(backgroundColor);

        // 상단 프로필 패널
        JPanel profilePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        profilePanel.setBackground(backgroundColor);

        // 프로필 이미지 레이블
        profileImageLabel = new JLabel();
        profileImageLabel.setPreferredSize(new Dimension(180, 180));
        profileImageLabel.setBackground(Color.WHITE);
        profileImageLabel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        profileImageLabel.setOpaque(true);
        profilePanel.add(profileImageLabel);

        // 정보 패널
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(backgroundColor);
        infoPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        // 닉네임 레이블
        nicknameLabel = createStyledLabel("", 18, Font.BOLD);
        
        // 구분선 추가
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(200, 2));
        separator.setForeground(accentColor);

        // 전적 레이블
        statsLabel = createStyledLabel("", 15, Font.PLAIN);
        
        // 승률 레이블
        winRateLabel = createStyledLabel("", 15, Font.PLAIN);

        // 컴포넌트 추가
        mainPanel.add(profilePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createCenteredPanel(nicknameLabel));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(separator);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(createCenteredPanel(statsLabel));
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createCenteredPanel(winRateLabel));

        add(mainPanel);
        
        // 프레임 스타일 설정
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getRootPane().setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, accentColor));
    }

    private JLabel createStyledLabel(String text, int fontSize, int fontStyle) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("맑은 고딕", fontStyle, fontSize));
        label.setForeground(textColor);
        return label;
    }

    private JPanel createCenteredPanel(JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(backgroundColor);
        panel.add(component);
        return panel;
    }

    public void updateUserInfo(String nickname, byte[] profileImage, int wins, int losses) {
        SwingUtilities.invokeLater(() -> {
            // 프로필 이미지 업데이트
            if (profileImage != null) {
                ImageIcon imageIcon = new ImageIcon(profileImage);
                Image scaledImage = imageIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                profileImageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                // 기본 이미지 설정
                ImageIcon defaultIcon = new ImageIcon("./img/기본사진.png");
                if (defaultIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                    Image scaledImage = defaultIcon.getImage().getScaledInstance(180, 180, Image.SCALE_SMOOTH);
                    profileImageLabel.setIcon(new ImageIcon(scaledImage));
                }
            }

            // 닉네임 업데이트
            nicknameLabel.setText(nickname);

            // 전적 업데이트
            statsLabel.setText(String.format("%d승 %d패", wins, losses));

            // 승률 계산 및 업데이트
            double winRate = (wins + losses) > 0 ? (double)wins/(wins + losses) * 100 : 0;
            winRateLabel.setText(String.format("승률: %.1f%%", winRate));
        });
    }
} 