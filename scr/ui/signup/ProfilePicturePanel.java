package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

public class ProfilePicturePanel extends JPanel {
    private JLabel profilePictureLabel;
    private JButton uploadButton;
    private String profilePicturePath;
    private BufferedImage img;
    private final int DEFAULT_IMAGE_SIZE = 150;

    public ProfilePicturePanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 200));  // 높이를 200으로 설정

        // 프로필 사진 라벨
        JLabel titleLabel = new JLabel("프로필 사진");
        titleLabel.setFont(new Font("굴림", Font.BOLD, 11));
        titleLabel.setBounds(55, 0, 70, 25);
        add(titleLabel);

        // 프로필 이미지를 표시할 패널
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(null);
        imagePanel.setBounds(10, 25, DEFAULT_IMAGE_SIZE, DEFAULT_IMAGE_SIZE);
        imagePanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(imagePanel);
        
                // 프로필 사진 레이블
                profilePictureLabel = new JLabel("이미지 없음", SwingConstants.CENTER);
                profilePictureLabel.setBounds(0, 0, 150, 150);
                imagePanel.add(profilePictureLabel);
                profilePictureLabel.setFont(new Font("굴림", Font.PLAIN, 12));

        // 업로드 버튼
        uploadButton = new JButton("이미지 선택");
        uploadButton.setFont(new Font("굴림", Font.PLAIN, 12));
        uploadButton.setBounds(35, 175, 100, 25);
        uploadButton.addActionListener(e -> uploadProfilePicture());
        add(uploadButton);
    }

    private void uploadProfilePicture() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".png");
            }
            public String getDescription() {
                return "PNG 이미지 (*.png)";
            }
        });

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile.getName().toLowerCase().endsWith(".png")) {
                showPreview(selectedFile);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "PNG 형식의 파일만 선택하세요.", 
                    "잘못된 파일 형식", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showPreview(File imageFile) {
        try {
            img = ImageIO.read(imageFile);
            JDialog previewDialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "미리보기", true);
            previewDialog.setSize(400, 500);
            previewDialog.setLocationRelativeTo(this);
            previewDialog.getContentPane().setLayout(new BorderLayout(0, 10));

            // 미리보기 이미지 패널
            JPanel previewPanel = new JPanel();
            previewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JLabel previewLabel = new JLabel(new ImageIcon(
                img.getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
            previewPanel.add(previewLabel);
            previewDialog.getContentPane().add(previewPanel, BorderLayout.CENTER);

            // 크기 조절 슬라이더
            JPanel sliderPanel = new JPanel(new BorderLayout());
            sliderPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            JSlider sizeSlider = new JSlider(100, 300, DEFAULT_IMAGE_SIZE);
            sizeSlider.setMajorTickSpacing(50);
            sizeSlider.setPaintTicks(true);
            sizeSlider.setPaintLabels(true);
            sliderPanel.add(new JLabel("이미지 크기"), BorderLayout.NORTH);
            sliderPanel.add(sizeSlider, BorderLayout.CENTER);
            
            // 버튼 패널
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            JButton confirmButton = new JButton("확인");
            JButton cancelButton = new JButton("취소");

            confirmButton.addActionListener(e -> {
                updateProfilePicture(imageFile, sizeSlider.getValue());
                previewDialog.dispose();
            });

            cancelButton.addActionListener(e -> previewDialog.dispose());

            buttonPanel.add(confirmButton);
            buttonPanel.add(cancelButton);

            // 하단 패널
            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            bottomPanel.add(sliderPanel, BorderLayout.CENTER);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);
            previewDialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

            // 슬라이더 이벤트
            sizeSlider.addChangeListener(e -> {
                int size = sizeSlider.getValue();
                previewLabel.setIcon(new ImageIcon(
                    img.getScaledInstance(size, size, Image.SCALE_SMOOTH)));
            });

            previewDialog.setVisible(true);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                "이미지를 불러오는 데 실패했습니다.", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProfilePicture(File imageFile, int size) {
        profilePicturePath = imageFile.getAbsolutePath();
        Image scaledImage = img.getScaledInstance(size, size, Image.SCALE_SMOOTH);
        profilePictureLabel.setIcon(new ImageIcon(scaledImage));
        profilePictureLabel.setText(null);
    }

    public byte[] getProfilePictureBytes() {
        if (profilePicturePath == null) {
            return null;
        }
        try {
            return Files.readAllBytes(new File(profilePicturePath).toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void reset() {
        profilePictureLabel.setIcon(null);
        profilePictureLabel.setText("이미지 없음");
        profilePicturePath = null;
        img = null;
    }
}