package DB;
import java.sql.*;
import config.AppConfig;

// 프로그램 첫 실행 시 프로그램에 필요한 테이블을 생성하는 클래스.
public class DBTable {
	
	public static void main(String[] args) { // 클래스 실행 시 main 메소드가 바로 시작한다.
		/* 데이터베이스와의 연결에 사용할 변수들 */
		Connection con = null;
		Statement stmt = null;
		String url = AppConfig.optional("OMOK_DB_URL", "jdbc:oracle:thin:@localhost:1521:testdb");
		String user = AppConfig.require("OMOK_DB_USER");
		String passwd = AppConfig.require("OMOK_DB_PASSWORD");
		
		try { // 데이터베이스 연결은 try-catch문으로 예외를 잡아준다.
			// Oracle JDBC 드라이버 로드
			Class.forName("oracle.jdbc.driver.OracleDriver");
			con = DriverManager.getConnection(url, user, passwd);
			stmt = con.createStatement();
			
			// member라는 테이블 생성하고 테이블 안의 칼럼은 name, nickname, id, password, email, win, lose가 존재.
			// nickname과 id를 기본키로 설정한다.
			String createStr = "CREATE TABLE Users ("
							   + "name VARCHAR2(20) NOT NULL, "
							   + "nickname VARCHAR2(20) NOT NULL, "
							   + "id VARCHAR2(20) NOT NULL, "
							   + "password VARCHAR2(20) NOT NULL, "
							   + "email VARCHAR2(40) NOT NULL, "
							   + "win NUMBER NOT NULL, "
							   + "lose NUMBER NOT NULL, "
							   + "PRIMARY KEY (nickname, id))";
			
			stmt.executeUpdate(createStr); // 업데이트 문을 수행한다.
			System.out.println("[Server] 테이블 생성 성공"); // 업데이트 문이 성공하면 테이블 생성 성공을 콘솔로 알린다.
		} catch (Exception e) { // 데이터베이스 연결 및 테이블 생성에 예외가 발생했을 때 실패를 콘솔로 알린다.
			System.out.println("[Server] 데이터베이스 연결 혹은 테이블 생성에 문제 발생 > " + e.toString());
		} finally {
			// 자원 해제
			try {
				if (stmt != null) stmt.close();
				if (con != null) con.close();
			} catch (SQLException e) {
				System.out.println("[Server] 자원 해제 중 문제 발생 > " + e.toString());
			}
		}
	}
}
