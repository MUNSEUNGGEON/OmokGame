package ui;

import javax.swing.*;
import core.*;
import ui.signup.PasswordPanel;

import static core.MessageType.*;  // 상수를 static import
import java.awt.*;
import java.awt.event.*;

// 회원정보 변경 기능을 수행하는 인터페이스
public class CInfoFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    /* Panel */
    JPanel panel = new JPanel();

    /* Button */
    JButton nicknameBtn = new JButton("닉네임 변경하기");
    JButton pwBtn = new JButton("비밀번호 변경하기");
    JButton emailBtn = new JButton("이메일 변경하기");
    JButton phoneBtn = new JButton("전화번호 변경하기");
    JButton addressBtn = new JButton("주소 변경하기");
    JButton exitBtn = new JButton("나가기");

    String nickname;    // 변경할 닉네임
    String pw;          // 변경할 비밀번호
    String email;       // 변경할 이메일
    String phone;       // 변경할 전화번호
    String address;     // 변경할 주소

    Client c = null;

    public CInfoFrame(Client _c) {
        c = _c;

        setTitle("회원정보 수정");

        /* Button 크기 작업 */
        nicknameBtn.setPreferredSize(new Dimension(250, 30));
        pwBtn.setPreferredSize(new Dimension(250, 30));
        emailBtn.setPreferredSize(new Dimension(250, 30));
        phoneBtn.setPreferredSize(new Dimension(250, 30));
        addressBtn.setPreferredSize(new Dimension(250, 30));
        exitBtn.setPreferredSize(new Dimension(250, 30));

        /* panel 추가 작업 */
        setContentPane(panel);    // panel을 기본 컨테이너로 설정
        panel.add(nicknameBtn);
        panel.add(pwBtn);
        panel.add(emailBtn);
        panel.add(phoneBtn);
        panel.add(addressBtn);
        panel.add(exitBtn);

        /* 버튼 이벤트 리스너 추가 */
        ButtonListener bl = new ButtonListener();
        nicknameBtn.addActionListener(bl);
        pwBtn.addActionListener(bl);
        emailBtn.addActionListener(bl);
        phoneBtn.addActionListener(bl);
        addressBtn.addActionListener(bl);
        exitBtn.addActionListener(bl);

        setSize(280, 300);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /* Button 이벤트 리스너 */
    public class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JButton b = (JButton) e.getSource();

            /* 닉네임 변경하기 버튼 이벤트 */
            if (b.getText().equals("닉네임 변경하기")) {
                System.out.println("[Client] 닉네임 변경 시도");
                nickname = JOptionPane.showInputDialog(null, "변경할 닉네임을 입력하시오", "닉네임변경", JOptionPane.QUESTION_MESSAGE);

                if (nickname != null) {
                    c.sendMsg(CHANGE + "//nickname//" + nickname);
                }
            }
            
            /* 비밀번호 변경하기 버튼 이벤트 */
            else if (b.getText().equals("비밀번호 변경하기")) {
                System.out.println("[Client] 비밀번호 변경 시도");
                PasswordPanel passwordPanel = new PasswordPanel(); // PasswordPanel 인스턴스 생성
                int option = JOptionPane.showConfirmDialog(null, passwordPanel, "비밀번호 변경", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

                if (option == JOptionPane.OK_OPTION) {
                    if (passwordPanel.isPasswordValid()) {
                        String newPassword = passwordPanel.getPassword();
                        c.sendMsg(CHANGE + "//password//" + newPassword);
                    } else {
                        JOptionPane.showMessageDialog(null, "비밀번호 요구사항을 충족하지 않습니다.", "입력 오류", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

            /* 이메일 변경하기 버튼 이벤트 */
            else if (b.getText().equals("이메일 변경하기")) {
                System.out.println("[Client] 이메일 변경 시도");
                email = JOptionPane.showInputDialog(null, "변경할 이메일을 입력하시오", "이메일변경", JOptionPane.QUESTION_MESSAGE);

                if (email != null) {
                    c.sendMsg(CHANGE + "//email//" + email);
                }
            }

            /* 전화번호 변경하기 버튼 이벤트 */
            else if (b.getText().equals("전화번호 변경하기")) {
                System.out.println("[Client] 전화번호 변경 시도");
                phone = JOptionPane.showInputDialog(null, "변경할 전화번호를 입력하시오", "전화번호변경", JOptionPane.QUESTION_MESSAGE);

                if (phone != null) {
                    c.sendMsg(CHANGE + "//phone//" + phone);
                }
            }

            /* 주소 변경하기 버튼 이벤트 */
            else if (b.getText().equals("주소 변경하기")) {
                System.out.println("[Client] 주소 변경 시도");
                address = JOptionPane.showInputDialog(null, "변경할 주소를 입력하시오", "주소변경", JOptionPane.QUESTION_MESSAGE);

                if (address != null) {
                    c.sendMsg(CHANGE + "//address//" + address);
                }
            }

            /* 나가기 버튼 이벤트 */
            else if (b.getText().equals("나가기")) {
                System.out.println("[Client] 회원 정보 변경 인터페이스 종료");
                dispose();    // 인터페이스 닫음
            }
        }
    }
}