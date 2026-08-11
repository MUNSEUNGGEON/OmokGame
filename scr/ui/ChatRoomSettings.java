package ui;

import java.awt.*;
import javax.swing.*;

public class ChatRoomSettings extends JDialog {
    private PrivateChatRoom chatRoom;
    private JColorChooser colorChooser;
    private JComboBox<String> fontFamilyCombo;
    private JComboBox<Integer> fontSizeCombo;

    public ChatRoomSettings(PrivateChatRoom chatRoom) {
        super((Frame) SwingUtilities.getWindowAncestor(chatRoom), "채팅방 설정", true);
        this.chatRoom = chatRoom;
        
        setSize(400, 300);
        initializeComponents();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();

        // 색상 설정 패널
        JPanel colorPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        colorPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton bgColorButton = new JButton("배경색 변경");
        JButton textColorButton = new JButton("글자색 변경");
        JButton myBubbleColorButton = new JButton("내 메시지 버블 색상");
        JButton otherBubbleColorButton = new JButton("상대방 메시지 버블 색상");

        bgColorButton.addActionListener(e -> changeBackgroundColor());
        textColorButton.addActionListener(e -> changeTextColor());
        myBubbleColorButton.addActionListener(e -> changeMyBubbleColor());
        otherBubbleColorButton.addActionListener(e -> changeOtherBubbleColor());

        colorPanel.add(bgColorButton);
        colorPanel.add(textColorButton);
        colorPanel.add(myBubbleColorButton);
        colorPanel.add(otherBubbleColorButton);

        // 폰트 설정 패널
        JPanel fontPanel = new JPanel(new FlowLayout());
        
        // 폰트 패밀리 선택
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilies = ge.getAvailableFontFamilyNames();
        fontFamilyCombo = new JComboBox<>(fontFamilies);
        
        // 폰트 크기 선택
        Integer[] fontSizes = {8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24, 26, 28, 36};
        fontSizeCombo = new JComboBox<>(fontSizes);
        fontSizeCombo.setSelectedItem(12); // 기본 크기

        JButton applyFontButton = new JButton("폰트 적용");
        applyFontButton.addActionListener(e -> changeFont());

        fontPanel.add(new JLabel("폰트: "));
        fontPanel.add(fontFamilyCombo);
        fontPanel.add(new JLabel("크기: "));
        fontPanel.add(fontSizeCombo);
        fontPanel.add(applyFontButton);

        // 탭 추가
        tabbedPane.addTab("색상 설정", colorPanel);
        tabbedPane.addTab("폰트 설정", fontPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 확인 버튼
        JButton closeButton = new JButton("닫기");
        closeButton.addActionListener(e -> dispose());
        add(closeButton, BorderLayout.SOUTH);
    }

    private void changeBackgroundColor() {
        Color color = JColorChooser.showDialog(this, "배경색 선택", Color.WHITE);
        if (color != null) {
            chatRoom.updateBackgroundColor(color);
        }
    }

    private void changeTextColor() {
        Color color = JColorChooser.showDialog(this, "글자색 선택", Color.BLACK);
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
        Color color = JColorChooser.showDialog(
            this,
            "내 메시지 버블 색상 선택",
            Color.YELLOW
        );
        if (color != null) {
            chatRoom.updateMyBubbleColor(color);
        }
    }

    private void changeOtherBubbleColor() {
        Color color = JColorChooser.showDialog(
            this,
            "상대방 메시지 버블 색상 선택",
            Color.WHITE
        );
        if (color != null) {
            chatRoom.updateOtherBubbleColor(color);
        }
    }
} 