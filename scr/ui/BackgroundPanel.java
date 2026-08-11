package ui;

import javax.swing.*;
import java.awt.*;

class BackgroundPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private Image backgroundImage;

    public BackgroundPanel(String filename) {
        // 이미지를 로드합니다.
        backgroundImage = new ImageIcon(filename).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // 컴포넌트 크기에 맞게 이미지를 그립니다.
        g.drawImage(backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
    }
}
