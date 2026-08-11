package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class GenderPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JRadioButton male;
    private JRadioButton female;
    private ButtonGroup genderGroup;

    public GenderPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 성별 라벨
        JLabel genderLabel = new JLabel("성별");
        genderLabel.setFont(new Font("굴림", Font.BOLD, 11));
        genderLabel.setBounds(10, 10, 70, 25);
        add(genderLabel);

        // 라디오 버튼 초기화
        male = new JRadioButton("남성");
        female = new JRadioButton("여성");
        genderGroup = new ButtonGroup();

        // 라디오 버튼 스타일 설정
        male.setFont(new Font("굴림", Font.PLAIN, 12));
        female.setFont(new Font("굴림", Font.PLAIN, 12));
        
        // 라디오 버튼 위치 설정
        male.setBounds(90, 10, 60, 25);
        female.setBounds(160, 10, 60, 25);

        // 배경 설정
        male.setBackground(null);
        female.setBackground(null);

        // 버튼 그룹에 추가
        genderGroup.add(male);
        genderGroup.add(female);

        // 패널에 추가
        add(male);
        add(female);

        // 기본값 설정
        male.setSelected(true);
    }

    public String getSelectedGender() {
        if (male.isSelected()) {
            return "남성";
        } else if (female.isSelected()) {
            return "여성";
        }
        return "";
    }

    public void reset() {
        male.setSelected(true);
    }
}
