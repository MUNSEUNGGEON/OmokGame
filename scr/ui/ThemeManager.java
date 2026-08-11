package ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ThemeManager {
    public enum Theme {
        NIGHT, MORNING, SPRING, SUMMER, AUTUMN, WINTER
    }
    
    // 테마별 색상 정의
    private final ColorScheme NIGHT = new ColorScheme(
        new Color(32, 34, 37),      // main background (밤하늘)
        new Color(47, 49, 54),      // secondary background (어두운 밤)
        new Color(103, 140, 177),   // accent (달빛)
        new Color(255, 255, 255)    // text (별빛)
    );
    
    private final ColorScheme MORNING = new ColorScheme(
        new Color(255, 248, 220),   // main background (아침 하늘)
        new Color(255, 235, 205),   // secondary background (새벽빛)
        new Color(255, 192, 103),   // accent (아침 햇살)
        new Color(70, 70, 70)       // text (그림자)
    );
    
    private final ColorScheme SPRING = new ColorScheme(
        new Color(255, 240, 245),   // main background (벚꽃)
        new Color(230, 230, 250),   // secondary background (연보라)
        new Color(255, 182, 193),   // accent (진한 벚꽃)
        new Color(32, 34, 37)      // text (올리브)
    );
    
    private final ColorScheme SUMMER = new ColorScheme(
        new Color(173, 216, 230),   // main background (하늘)
        new Color(135, 206, 235),   // secondary background (바다)
        new Color(255, 215, 0),     // accent (태양)
        new Color(0, 51, 102)       // text (깊은 바다)
    );
    
    private final ColorScheme AUTUMN = new ColorScheme(
        new Color(255, 228, 196),   // main background (낙엽)
        new Color(222, 184, 135),   // secondary background (마른 흙)
        new Color(205, 92, 92),     // accent (단풍)
        new Color(32, 34, 37)      // text (나무 줄기)
    );
    
    private final ColorScheme WINTER = new ColorScheme(
        new Color(240, 248, 255),   // main background (눈)
        new Color(230, 230, 250),   // secondary background (얼음)
        new Color(176, 196, 222),   // accent (겨울 하늘)
        new Color(25, 25, 112)      // text (겨울 밤)
    );

    private Theme currentTheme = Theme.MORNING;
    private final JFrame frame;
    private final Font MAIN_FONT = new Font("맑은 고딕", Font.BOLD, 12);
    private final Font SUB_FONT = new Font("맑은 고딕", Font.PLAIN, 12);

    private static class ColorScheme {
        final Color mainBg;
        final Color secondaryBg;
        final Color accent;
        final Color text;

        ColorScheme(Color mainBg, Color secondaryBg, Color accent, Color text) {
            this.mainBg = mainBg;
            this.secondaryBg = secondaryBg;
            this.accent = accent;
            this.text = text;
        }
    }

    public ThemeManager(JFrame frame) {
        this.frame = frame;
    }

    public void applyTheme(Theme theme) {
        ColorScheme scheme = getColorScheme(theme);
        updateColors(scheme);
        currentTheme = theme;
        
        switch (theme) {
            case MORNING:
                setLookAndFeel(new FlatLightLaf());
                break;
            case NIGHT:
                setLookAndFeel(new FlatDarkLaf());
                break;
//            case SPRING:
//                setLookAndFeel(new FlatDarkLaf());
//                break;
            default:
                // 다른 테마는 기본 Look&Feel 유지
                break;
        }
        
        SwingUtilities.updateComponentTreeUI(frame);
    }

    private void updateColors(ColorScheme scheme) {
        // 프레임의 모든 컴포넌트 업데이트
        updateComponentColors(frame.getRootPane(), scheme);
    }

    private void updateComponentColors(Container container, ColorScheme scheme) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                styleComponent((JComponent)comp);
            }
            if (comp instanceof Container) {
                updateComponentColors((Container)comp, scheme);
            }
        }
    }

    private ColorScheme getColorScheme(Theme theme) {
        switch (theme) {
            case MORNING: return MORNING;
            case SPRING: return SPRING;
            case SUMMER: return SUMMER;
            case AUTUMN: return AUTUMN;
            case WINTER: return WINTER;
            default: return NIGHT;
        }
    }

    private void setLookAndFeel(LookAndFeel laf) {
        try {
            UIManager.setLookAndFeel(laf);
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
    }

    public void styleComponent(JComponent component) {
        ColorScheme scheme = getColorScheme(currentTheme);
        
        if (component instanceof JButton) {
            styleButton((JButton)component, scheme);
        } else if (component instanceof JTextField) {
            styleTextField((JTextField)component, scheme);
        } else if (component instanceof JList) {
            styleList((JList<?>)component, scheme);
        } else if (component instanceof JPanel) {
            stylePanel((JPanel)component, scheme);
        } else if (component instanceof JLabel) {
            styleLabel((JLabel)component, scheme);
        } else if (component instanceof JTextArea) {
            styleTextArea((JTextArea)component, scheme);
        } else if (component instanceof JScrollPane) {
            styleScrollPane((JScrollPane)component, scheme);
        } else if (component instanceof JComboBox) {
            styleComboBox((JComboBox<?>)component, scheme);
        }
    }

    private void styleButton(JButton button, ColorScheme scheme) {
        if (button.getIcon() == null) {
            button.setBackground(scheme.accent);
            button.setForeground(scheme.text);
            button.setFont(MAIN_FONT);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
            
            // 이미 리스너가 있다면 제거
            for (MouseListener listener : button.getMouseListeners()) {
                if (listener instanceof MouseAdapter) {
                    button.removeMouseListener(listener);
                }
            }
            
            button.addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    button.setBackground(scheme.accent.darker());
                }
                public void mouseExited(MouseEvent e) {
                    button.setBackground(scheme.accent);
                }
            });
        }
    }

    private void styleTextField(JTextField textField, ColorScheme scheme) {
        textField.setBackground(scheme.secondaryBg);
        textField.setForeground(scheme.text);
        textField.setCaretColor(scheme.text);
        textField.setFont(SUB_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(scheme.accent, 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }

    private void styleList(JList<?> list, ColorScheme scheme) {
        list.setBackground(scheme.secondaryBg);
        list.setForeground(scheme.text);
        list.setFont(SUB_FONT);
        list.setSelectionBackground(scheme.accent);
        list.setSelectionForeground(scheme.text);
        list.setBorder(BorderFactory.createLineBorder(scheme.accent, 1));
    }

    private void stylePanel(JPanel panel, ColorScheme scheme) {
        panel.setBackground(scheme.mainBg);
        Border border = panel.getBorder();
        if (border != null) {
            panel.setBorder(BorderFactory.createLineBorder(scheme.accent, 1));
        }
    }

    private void styleLabel(JLabel label, ColorScheme scheme) {
        label.setForeground(scheme.text);
        label.setFont(MAIN_FONT);
    }

    private void styleTextArea(JTextArea textArea, ColorScheme scheme) {
        textArea.setBackground(scheme.secondaryBg);
        textArea.setForeground(scheme.text);
        textArea.setFont(SUB_FONT);
        textArea.setCaretColor(scheme.text);
    }

    private void styleScrollPane(JScrollPane scrollPane, ColorScheme scheme) {
        scrollPane.getViewport().setBackground(scheme.secondaryBg);
        scrollPane.setBorder(BorderFactory.createLineBorder(scheme.accent, 1));
    }

    private void styleComboBox(JComboBox<?> comboBox, ColorScheme scheme) {
        comboBox.setBackground(scheme.secondaryBg);
        comboBox.setForeground(scheme.text);
        comboBox.setFont(SUB_FONT);
        ((JComponent) comboBox.getRenderer()).setBackground(scheme.secondaryBg);
    }

    // Getter 메소드들
    public Color getCurrentMainBgColor() {
        return getColorScheme(currentTheme).mainBg;
    }

    public Color getCurrentSecondaryBgColor() {
        return getColorScheme(currentTheme).secondaryBg;
    }

    public Color getCurrentAccentColor() {
        return getColorScheme(currentTheme).accent;
    }

    public Color getCurrentTextColor() {
        return getColorScheme(currentTheme).text;
    }

    public Font getMainFont() {
        return MAIN_FONT;
    }

    public Font getSubFont() {
        return SUB_FONT;
    }

    public void switchTheme() {
        Theme[] themes = Theme.values();
        int nextThemeIndex = (currentTheme.ordinal() + 1) % themes.length;
        applyTheme(themes[nextThemeIndex]);
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }
}
