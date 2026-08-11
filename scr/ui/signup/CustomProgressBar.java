package ui.signup;

import java.awt.Color;
import java.io.Serializable;
import javax.swing.JProgressBar;

class CustomProgressBar extends JProgressBar implements Serializable {
    private static final long serialVersionUID = 1L; // serialVersionUID 추가
    public CustomProgressBar() {
        super(0, 100);
        setStringPainted(true);
    }

    // 점수에 따라 색상 설정
    public void setColor(int score) {
        if (score < 50) {
            setForeground(Color.RED); // 약함
        } else if (score < 75) {
            setForeground(Color.ORANGE); // 보통
        } else {
            setForeground(Color.GREEN); // 강함
        }
    }
}