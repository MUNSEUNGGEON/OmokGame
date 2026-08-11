package ui.GameFrame;

import ui.ThemeManager;
import ui.ThemeManager.Theme;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ChatSettingsDialog extends JDialog {
    private ThemeManager themeManager;
    private Color backgroundColor;
    private Color textColor;
    private String selectedFont;
    private int selectedFontSize;
    private GameFrame gameFrame;
    
    private JButton bgColorBtn;
    private JButton fgColorBtn;
    private JButton applyBtn;
    private JLabel fontLabel;
    private JLabel themeLabel;
    private JComboBox<String> fontComboBox;
    private JComboBox<String> fontSizeComboBox;
    private JComboBox<String> themeComboBox;
    private JToggleButton themeToggleBtn;
    private JPanel themePanel;
    private JPanel chatPanel;

    public ChatSettingsDialog(GameFrame parent, JTextArea chatArea) {
        super(parent, "설정", true);
        this.gameFrame = parent;
        this.themeManager = parent.themeManager;
        
        setSize(400, 450);
        setLayout(new BorderLayout(10, 10));
        
        // 메인 패널 생성
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(themeManager.getCurrentMainBgColor());
        
        // 테마 설정 패널
        themePanel = new JPanel(new BorderLayout(10, 10));
        themePanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        themePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(themeManager.getCurrentAccentColor()),
            "테마 설정",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            null,
            themeManager.getCurrentTextColor()
        ));
        
        // 테마 토글 버튼 생성
        themeToggleBtn = new JToggleButton("테마 설정 보기");
        themeToggleBtn.setBackground(themeManager.getCurrentAccentColor());
        themeToggleBtn.setForeground(themeManager.getCurrentTextColor());
        themeToggleBtn.setFocusPainted(false);
        
        // 테마 콤보박스 패널
        JPanel themeSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeSelectionPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        themeLabel = new JLabel("테마:");
        themeLabel.setForeground(themeManager.getCurrentTextColor());
        String[] themeNames = {"아침", "밤", "봄", "여름", "가을", "겨울"};
        themeComboBox = new JComboBox<>(themeNames);
        themeComboBox.setSelectedItem(getCurrentThemeName());
        styleComboBox(themeComboBox);
        
        themeSelectionPanel.add(themeLabel);
        themeSelectionPanel.add(themeComboBox);
        themeSelectionPanel.setVisible(false);
        
        themePanel.add(themeToggleBtn, BorderLayout.NORTH);
        themePanel.add(themeSelectionPanel, BorderLayout.CENTER);
        
        // 채팅 설정 패널
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        chatPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(themeManager.getCurrentAccentColor()),
            "채팅 설정",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            null,
            themeManager.getCurrentTextColor()
        ));
        
        // 폰트 설정 패널
        JPanel fontPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        fontPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        fontLabel = new JLabel("글꼴:");
        fontLabel.setForeground(themeManager.getCurrentTextColor());
        
        ArrayList<String> koreanFonts = getKoreanFonts();
        fontComboBox = new JComboBox<>(koreanFonts.toArray(new String[0]));
        fontComboBox.setSelectedItem("맑은 고딕");
        styleComboBox(fontComboBox);
        
        fontSizeComboBox = new JComboBox<>(new String[]{"8", "9", "10", "11", "12", "13", "14", "16", "18", "20"});
        fontSizeComboBox.setSelectedItem("12");
        styleComboBox(fontSizeComboBox);
        
        fontPanel.add(fontLabel);
        fontPanel.add(fontComboBox);
        fontPanel.add(fontSizeComboBox);
        
        // 색상 설정 패널
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorPanel.setBackground(themeManager.getCurrentSecondaryBgColor());
        
        bgColorBtn = new JButton("채팅창 배경색");
        fgColorBtn = new JButton("채팅 글씨색");
        styleButton(bgColorBtn);
        styleButton(fgColorBtn);
        
        colorPanel.add(bgColorBtn);
        colorPanel.add(fgColorBtn);
        
        // 적용 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(themeManager.getCurrentMainBgColor());
        applyBtn = new JButton("설정 완료");
        styleButton(applyBtn);
        buttonPanel.add(applyBtn);
        
        // 패널 조립
        chatPanel.add(fontPanel);
        chatPanel.add(Box.createVerticalStrut(10));
        chatPanel.add(colorPanel);
        
        mainPanel.add(themePanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(chatPanel);
        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // 이벤트 리스너 설정
        themeToggleBtn.addActionListener(e -> {
            themeSelectionPanel.setVisible(themeToggleBtn.isSelected());
            pack();
        });
        
        bgColorBtn.addActionListener(ev -> {
            Color selectedColor = JColorChooser.showDialog(this, "채팅창 배경색 선택", chatArea.getBackground());
            if (selectedColor != null) {
                backgroundColor = selectedColor;
            }
        });
        
        fgColorBtn.addActionListener(ev -> {
            Color selectedColor = JColorChooser.showDialog(this, "채팅 글씨색 선택", chatArea.getForeground());
            if (selectedColor != null) {
                textColor = selectedColor;
            }
        });
        
        applyBtn.addActionListener(ev -> {
            // 테마 변경
            Theme selectedTheme = getThemeFromName((String) themeComboBox.getSelectedItem());
            if (selectedTheme != themeManager.getCurrentTheme()) {
                themeManager.applyTheme(selectedTheme);
                gameFrame.updateTheme();
                if (gameFrame.c != null) {
                    gameFrame.c.currentTheme = selectedTheme;
                }
            }
            
            // 채팅 설정 적용
            selectedFont = (String) fontComboBox.getSelectedItem();
            selectedFontSize = Integer.parseInt((String) fontSizeComboBox.getSelectedItem());
            
            if (backgroundColor != null) {
                chatArea.setBackground(backgroundColor);
            }
            if (textColor != null) {
                chatArea.setForeground(textColor);
            }
            if (selectedFont != null) {
                chatArea.setFont(new Font(selectedFont, Font.PLAIN, selectedFontSize));
            }
            
            dispose();
        });
        
        setLocationRelativeTo(parent);
        pack();
    }
    
    private String getCurrentThemeName() {
        return switch (themeManager.getCurrentTheme()) {
            case MORNING -> "아침";
            case NIGHT -> "밤";
            case SPRING -> "봄";
            case SUMMER -> "여름";
            case AUTUMN -> "가을";
            case WINTER -> "겨울";
        };
    }
    
    private Theme getThemeFromName(String name) {
        return switch (name) {
            case "아침" -> Theme.MORNING;
            case "밤" -> Theme.NIGHT;
            case "봄" -> Theme.SPRING;
            case "여름" -> Theme.SUMMER;
            case "가을" -> Theme.AUTUMN;
            case "겨울" -> Theme.WINTER;
            default -> Theme.MORNING;
        };
    }
    
    private void styleButton(JButton button) {
        button.setBackground(themeManager.getCurrentAccentColor());
        button.setForeground(themeManager.getCurrentTextColor());
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(themeManager.getCurrentSecondaryBgColor());
        comboBox.setForeground(themeManager.getCurrentTextColor());
        ((JComponent) comboBox.getRenderer()).setBackground(themeManager.getCurrentSecondaryBgColor());
    }
    
    private ArrayList<String> getKoreanFonts() {
        String[] availableFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        ArrayList<String> koreanFonts = new ArrayList<>();
        for (String font : availableFonts) {
            if (font.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣].*")) {
                koreanFonts.add(font);
            }
        }
        return koreanFonts;
    }
}
