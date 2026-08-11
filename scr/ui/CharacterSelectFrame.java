package ui;

import javax.swing.*;
import core.Client;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class CharacterSelectFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private Client client;

    public CharacterSelectFrame(Client client, JLabel profileLabel) {
        this.client = client;
        setTitle("캐릭터 선택");
        setSize(700, 400);
        setLayout(new GridLayout(2, 3));

        // 캐릭터 이미지 파일 경로 설정file:///C:/Users/82104/.Javaomok/Omok1107/scr/Character/동물의숲.png
        String[] imageFiles = {"./img/동물의숲.png", "./img/몬스터헌터.png", "./img/슈퍼마리오.png", 
        		"./img/스타듀밸리.png", "./img/커비.png","./img/에이펙스.png","./img/젤다의전설.png","./img/오버워치.png"};

        // 각 이미지 파일을 버튼에 추가
        for (String imageFile : imageFiles) {
            JButton imageButton = createImageButton(imageFile, profileLabel);
            add(imageButton);
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    private JButton createImageButton(String imageFile, JLabel profileLabel) {
        // 이미지 파일 존재 확인
        File file = new File(imageFile);
        if (!file.exists()) {
            System.out.println("[Error] 이미지 파일을 찾을 수 없음: " + imageFile);
            return new JButton("이미지 없음");
        }

        // 이미지 로딩
        ImageIcon icon = new ImageIcon(imageFile);
        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.out.println("[Error] 이미지 로딩 실패: " + imageFile);
            return new JButton("이미지 로딩 실패");
        }

        Image scaledImage = icon.getImage().getScaledInstance(120 ,120, Image.SCALE_SMOOTH);
        JButton imageButton = new JButton(new ImageIcon(scaledImage));
        
        // 버튼 클릭 시 동작 설정
        imageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // UI 업데이트는 EDT에서 실행
                SwingUtilities.invokeLater(() -> profileLabel.setIcon(new ImageIcon(scaledImage)));
                System.out.println("[CharacterSelectFrame] '" + imageFile + "' 캐릭터 선택됨.");

                // 이미지 전송을 비동기로 실행
                new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() {
                        try (FileInputStream fis = new FileInputStream(new File(imageFile))) {
                            byte[] imageBytes = fis.readAllBytes();
                            client.sendProfilePicture(imageBytes);
                            System.out.println("[CharacterSelectFrame] 프로필 사진 전송 성공: " + imageFile);
                        } catch (IOException ex) {
                            System.out.println("[CharacterSelectFrame] 프로필 사진 전송 실패: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        // 전송이 완료된 후 창 닫기
                        SwingUtilities.invokeLater(() -> dispose());
                    }
                }.execute();
            }
        });
        return imageButton;
    }
}
