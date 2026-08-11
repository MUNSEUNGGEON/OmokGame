

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;
import config.AppConfig;

public class EmailUtil {
    private static final String FROM_EMAIL = AppConfig.require("OMOK_SMTP_FROM");
    private static final String APP_PASSWORD = AppConfig.require("OMOK_SMTP_APP_PASSWORD");
    public static String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public static boolean sendTempPassword(String toEmail, String tempPassword) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        
        try {
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, APP_PASSWORD);
                }
            });

            session.setDebug(true);

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("임시 비밀번호 발급 안내");
            
            String htmlContent = String.format(
                "<div style='background-color: #f4f4f4; padding: 20px;'>" +
                "<h2 style='color: #333;'>임시 비밀번호 발급</h2>" +
                "<p>귀하의 임시 비밀번호는 다음과 같습니다:</p>" +
                "<div style='background-color: #fff; padding: 15px; margin: 10px 0; border-radius: 5px;'>" +
                "<strong style='color: #007bff; font-size: 18px;'>%s</strong>" +
                "</div>" +
                "<p style='color: #dc3545;'>※ 보안을 위해 로그인 후 반드시 비밀번호를 변경해주세요.</p>" +
                "</div>", tempPassword);

            message.setContent(htmlContent, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("[EmailUtil] 임시 비밀번호 이메일 전송 성공: " + toEmail);
            return true;

        } catch (MessagingException e) {
            System.out.println("[EmailUtil] 이메일 전송 실패: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

// 이메일 전송 테스트
public static void main(String[] args) {
    String testEmail = "ms1615@naver.com"; // 테스트할 이메일 주소
    String tempPassword = generateTempPassword();
    boolean result = sendTempPassword(testEmail, tempPassword);
    System.out.println("전송 결과: " + result);
    System.out.println("생성된 임시 비밀번호: " + tempPassword);
}
}
