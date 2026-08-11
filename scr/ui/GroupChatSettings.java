package ui;

import java.awt.*;
import javax.swing.*;

public class GroupChatSettings extends JDialog {
    private GroupChatRoom chatRoom;
    private JColorChooser colorChooser;
    private JComboBox<String> fontFamilyCombo;
    private JComboBox<Integer> fontSizeCombo;
    private ThemeManager themeManager;

    public GroupChatSettings(GroupChatRoom chatRoom) {
        super((Frame) SwingUtilities.getWindowAncestor(chatRoom), "그룹 채팅방 설정", true);
        this.chatRoom = chatRoom;
        this.themeManager = chatRoom.getThemeManager();
        
        setSize(400, 300);
        initializeComponents();
        setLocationRelativeTo(chatRoom);
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        themeManager.styleComponent(tabbedPane);

        // 색상 설정 패널
        JPanel colorPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        themeManager.styleComponent(colorPanel);
        
        // 색상 버튼들
        JButton bgColorButton = new JButton("배경색 변경");
        JButton textColorButton = new JButton("글자색 변경");
        JButton myBubbleColorButton = new JButton("내 메시지 버블 색상");
        JButton otherBubbleColorButton = new JButton("상대방 메시지 버블 색상");

        bgColorButton.addActionListener(e -> changeBackgroundColor());
        textColorButton.addActionListener(e -> changeTextColor());
        myBubbleColorButton.addActionListener(e -> changeMyBubbleColor());
        otherBubbleColorButton.addActionListener(e -> changeOtherBubbleColor());

        // 버튼 스타일링
        for (JButton button : new JButton[]{bgColorButton, textColorButton, 
                                          myBubbleColorButton, otherBubbleColorButton}) {
            themeManager.styleComponent(button);
            colorPanel.add(button);
        }

        // 폰트 설정 패널
        JPanel fontPanel = new JPanel(new GridBagLayout());
        themeManager.styleComponent(fontPanel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 폰트 패밀리 선택
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilies = ge.getAvailableFontFamilyNames();
        fontFamilyCombo = new JComboBox<>(fontFamilies);
        fontFamilyCombo.setSelectedItem("맑은 고딕");
        themeManager.styleComponent(fontFamilyCombo);
        
        // 폰트 크기 선택
        Integer[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36};
        fontSizeCombo = new JComboBox<>(fontSizes);
        fontSizeCombo.setSelectedItem(12);
        themeManager.styleComponent(fontSizeCombo);

        // 폰트 패널 구성
        gbc.gridx = 0; gbc.gridy = 0;
        fontPanel.add(new JLabel("폰트:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1;
        fontPanel.add(fontFamilyCombo, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        fontPanel.add(new JLabel("크기:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        fontPanel.add(fontSizeCombo, gbc);
        
        JButton applyFontButton = new JButton("폰트 적용");
        themeManager.styleComponent(applyFontButton);
        applyFontButton.addActionListener(e -> changeFont());
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        fontPanel.add(applyFontButton, gbc);

        // 탭 추가
        tabbedPane.addTab("색상", colorPanel);
        tabbedPane.addTab("폰트", fontPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        themeManager.styleComponent(buttonPanel);
        JButton closeButton = new JButton("닫기");
        themeManager.styleComponent(closeButton);
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void changeBackgroundColor() {
        Color color = JColorChooser.showDialog(this, "배경색 선택", chatRoom.getBackground());
        if (color != null) {
            chatRoom.updateBackgroundColor(color);
        }
    }

    private void changeTextColor() {
        Color color = JColorChooser.showDialog(this, "글자색 선택", chatRoom.getForeground());
        if (color != null) {
            chatRoom.updateTextColor(color);
        }
    }

    private void changeFont() {
        String selectedFamily = (String) fontFamilyCombo.getSelectedItem();
        int selectedSize = (Integer) fontSizeCombo.getSelectedItem();
        Font newFont = new Font(selectedFamily, Font.PLAIN, selectedSize);
        chatRoom.updateFont(newFont);
    }

    private void changeMyBubbleColor() {
        Color color = JColorChooser.showDialog(this, "내 메시지 버블 색상 선택", 
                                             chatRoom.getMyBubbleColor());
        if (color != null) {
            chatRoom.updateMyBubbleColor(color);
        }
    }

    private void changeOtherBubbleColor() {
        Color color = JColorChooser.showDialog(this, "상대방 메시지 버블 색상 선택", 
                                             chatRoom.getOtherBubbleColor());
        if (color != null) {
            chatRoom.updateOtherBubbleColor(color);
        }
    }
} 