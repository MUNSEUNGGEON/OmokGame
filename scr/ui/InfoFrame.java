package ui;

import core.*;
import static core.MessageType.*;  // 상수를 static import
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
 
//조회한 회원정보를 출력하는 인터페이스
public class InfoFrame extends JFrame{
	private static final long serialVersionUID = 1L;
	/* Panel */
	JPanel panel = new JPanel();
	
	/* Label */
	JLabel nameL = new JLabel("이름");
	JLabel nicknameL = new JLabel("닉네임");
	JLabel emailL = new JLabel("이메일");
	
	/* TextField */
	public  JTextField name = new JTextField();
	public JTextField nickname = new JTextField();
	public JTextField email = new JTextField();
	
	/* Button */
	JButton viewBtn = new JButton("조회하기");
	JButton exitBtn = new JButton("나가기");
	
	Client c = null;
	
	public InfoFrame(Client _c) {
		c = _c;
		
		setTitle("내 정보");
		
		/* Label 크기 작업 */
		nameL.setPreferredSize(new Dimension(40, 30));
		nicknameL.setPreferredSize(new Dimension(40, 30));
		emailL.setPreferredSize(new Dimension(40, 30));
		
		/* TextField 크기 작업 */
		name.setPreferredSize(new Dimension(200, 30));
		nickname.setPreferredSize(new Dimension(200, 30));
		email.setPreferredSize(new Dimension(200, 30));

		name.setEditable(false);
		nickname.setEditable(false);
		email.setEditable(false);
		
		/* Button 크기 작업 */
		viewBtn.setPreferredSize(new Dimension(250, 25));
		exitBtn.setPreferredSize(new Dimension(250, 25));
		
		/* panel 추가 작업 */
		setContentPane(panel);	//panel을 기본 컨테이너로 설정
		
		panel.add(nameL);
		panel.add(name);
		
		panel.add(nicknameL);
		panel.add(nickname);
		
		panel.add(emailL);
		panel.add(email);
		
		panel.add(viewBtn);
		panel.add(exitBtn);
		
		/* Button 이벤트 작업 */
		viewBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[Client] 회원 정보 조회 시도");
				c.sendMsg(VIEW + "//");	//서버에 회원 정보 조회 전송
			}
		});
		
		exitBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("[Client] 회원 정보 조회 인터페이스 종료");
				dispose();
			}
		});
		
		setSize(280, 210);
		setLocationRelativeTo(null);
		setResizable(false);
	}
}