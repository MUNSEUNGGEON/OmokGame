package ui.signup;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.toedter.calendar.JDateChooser;

public class BirthPanel extends JPanel implements Serializable {
    private static final long serialVersionUID = 1L;
    private JDateChooser birthDateChooser;

    public BirthPanel() {
        setLayout(null);
        setPreferredSize(new Dimension(400, 50));

        // 생년월일 라벨
        JLabel birthLabel = new JLabel("생년월일");
        birthLabel.setFont(new Font("굴림", Font.BOLD, 11));
        birthLabel.setBounds(10, 10, 70, 25);
        add(birthLabel);

        // 날짜 선택기
        birthDateChooser = new JDateChooser();
        birthDateChooser.setFont(new Font("굴림", Font.PLAIN, 12));
        birthDateChooser.setBounds(90, 10, 150, 25);
        birthDateChooser.setDateFormatString("yyyy-MM-dd");
        
        // JTextFields 폰트 설정
        try {
            Component[] components = birthDateChooser.getComponents();
            for (Component component : components) {
                if (component instanceof JTextField) {
                    ((JTextField) component).setFont(new Font("굴림", Font.PLAIN, 12));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        add(birthDateChooser);
    }

    public String getBirthDate() {
        Date date = birthDateChooser.getDate();
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }

    public boolean isValidBirthDate() {
        Date date = birthDateChooser.getDate();
        if (date == null) {
            JOptionPane.showMessageDialog(this, 
                "생년월일을 선택해주세요.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // 현재 날짜와 비교
        Date currentDate = new Date();
        if (date.after(currentDate)) {
            JOptionPane.showMessageDialog(this, 
                "미래의 날짜는 선택할 수 없습니다.", 
                "입력 오류", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public void reset() {
        birthDateChooser.setDate(null);
    }
}
