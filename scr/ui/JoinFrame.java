package ui;

import DB.*;
import core.*;
import ui.signup.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import static core.MessageType.*;  // 상수를 static import
import java.awt.*;

public class JoinFrame extends JFrame {
	private static final long serialVersionUID = 1L;
    private Client c = null;
    private Database database = new Database();
    
    // 컴포넌트 선언
    private JPanel contentPanel;
    public NamePanel namePanel;
    public NicknamePanel nicknamePanel;
    private PhonePanel phonePanel;
    private GenderPanel genderPanel;
    private AddressPanel addressPanel;
    private BirthPanel birthPanel;
    private EmailPanel emailPanel;
    private IdPanel idPanel;
    private PasswordPanel passwordPanel;
    private ProfilePicturePanel profilePicturePanel;
    private JButton joinBtn;
    private JButton cancelBtn;
    
    public JTextComponent nickname;
	
    /* TextField */
    public JTextField name = new JTextField();
    public JoinFrame(Client _c) {
        c = _c;
        initialize();
    }

    private void initialize() {
        // 프레임 기본 설정
        setTitle("회원가입");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 650, 670);
        setLocationRelativeTo(null);
        
        // 메인 패널
        contentPanel = new JPanel();
        contentPanel.setLayout(null);
        contentPanel.setBackground(Color.WHITE);
        setContentPane(contentPanel);
        
        // 컴포넌트 초기화
        initComponents();
        
        // 이벤트 리스너 설정
        setEventListeners();
    }

    private void initComponents() {
        // 각 패널 초기화
        profilePicturePanel = new ProfilePicturePanel();
        profilePicturePanel.setBounds(20, 20, 171, 210);
        profilePicturePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        namePanel = new NamePanel();
        namePanel.setBounds(200, 20, 374, 40);
        namePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        nicknamePanel = new NicknamePanel(database);
        nicknamePanel.setBounds(200, 70, 374, 40);
        nicknamePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        idPanel = new IdPanel(database);
        idPanel.setBounds(200, 120, 374, 40);
        idPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        passwordPanel = new PasswordPanel();
        passwordPanel.setBounds(200, 170, 374, 82);
        passwordPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        birthPanel = new BirthPanel();
        birthPanel.setBounds(200, 262, 374, 40);
        birthPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        phonePanel = new PhonePanel();
        phonePanel.setBounds(200, 312, 374, 40);
        phonePanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        genderPanel = new GenderPanel();
        genderPanel.setBounds(200, 362, 374, 40);
        genderPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        emailPanel = new EmailPanel();
        emailPanel.setBounds(200, 412, 374, 40);
        emailPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        addressPanel = new AddressPanel();
        addressPanel.setBounds(200, 462, 374, 120);
        addressPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        // 버튼 초기화
        joinBtn = new JButton("가입하기");
        joinBtn.setBounds(200, 592, 100, 30);
        
        cancelBtn = new JButton("가입취소");
        cancelBtn.setBounds(312, 592, 100, 30);
        
        // 컴포넌트 추가
        contentPanel.add(profilePicturePanel);
        contentPanel.add(namePanel);
        contentPanel.add(nicknamePanel);
        contentPanel.add(idPanel);
        contentPanel.add(passwordPanel);
        contentPanel.add(birthPanel);
        contentPanel.add(phonePanel);
        contentPanel.add(genderPanel);
        contentPanel.add(emailPanel);
        contentPanel.add(addressPanel);
        contentPanel.add(joinBtn);
        contentPanel.add(cancelBtn);
    }

    private void setEventListeners() {
        // 가입하기 버튼 이벤트
        joinBtn.addActionListener(e -> handleJoinButton());
        
        // 가입취소 버튼 이벤트
        cancelBtn.addActionListener(e -> dispose());
    }

    private void handleJoinButton() {
        // 입력값 가져오기
        String uname = namePanel.getName();
        String unick = nicknamePanel.getNickname();
        String uid = idPanel.getId();
        String upass = passwordPanel.getPassword();
        String uemail = emailPanel.getEmail();
        String ugender = genderPanel.getSelectedGender();
        String uphone = phonePanel.getPhone();
        String postal = addressPanel.getPostal();
        String address = addressPanel.getAddress();
        String detailAddress = addressPanel.getDetailAddress();
        String birthDate = birthPanel.getBirthDate();
        byte[] profilePicture = profilePicturePanel.getProfilePictureBytes();

        // 입력 검증
        if(uname.isEmpty() || unick.isEmpty() || uid.isEmpty() || upass.isEmpty() || 
           uemail.isEmpty() || ugender.isEmpty() || uphone.isEmpty() || 
           postal.isEmpty() || address.isEmpty() || detailAddress.isEmpty() || 
           birthDate.isEmpty() || profilePicture == null) {
            
            JOptionPane.showMessageDialog(this, 
                "모든 정보를 기입해주세요", 
                "회원가입 실패", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 비밀번호 유효성 검사 추가
        if (!passwordPanel.isPasswordValid()) {
            JOptionPane.showMessageDialog(this, 
                "비밀번호가 요구사항을 충족하지 않습니다.", 
                "회원가입 실패", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            if (c != null) {
                // 클라이언트를 통한 회가입 처리
                c.dos.writeUTF(JOIN + "//" + uname + "//" + unick + "//" + uid + "//" + 
                              upass + "//" + uemail + "//" + ugender + "//" + uphone + "//" + 
                              postal + "//" + address + "//" + detailAddress + "//" + birthDate);
                
                c.dos.writeInt(profilePicture.length);
                c.dos.write(profilePicture);
                c.dos.flush();
                
                System.out.println("[Client] 회원가입 정보 및 프로필 사진 전송 완료");
            } else {
                // Database를 통한 직접 회원가입 처리
                boolean success = database.joinCheck(
                    uname, unick, uid, upass, uemail, ugender, uphone,
                    postal, address, detailAddress, birthDate, profilePicture
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, 
                        "회원가입이 완료되었습니다.", 
                        "회원가입 성공", 
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "회원가입에 실패했습니다.", 
                        "회원가입 실패", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            System.out.println("[Error] 회원가입 처리 중 오류 발생: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, 
                "회원가입 처리 중 오류가 발생했습니다.", 
                "오류", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
