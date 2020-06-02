package controller;

import java.sql.*;

public class Driver {
	public static class DB{
		
		//DB connection
		
		public static Connection myConn;
		public static Statement myStmt;
		public static ResultSet myRs;
		
		 public DB(){
			myConn = null;
			myStmt = null;
			myRs = null;
		}
	 
	public static void make_connection() throws SQLException {
		// 1. Get a connection to database
		myConn = DriverManager.getConnection("jdbc:mysql://localhost/indexer_DB","put your database user name here","put your database password here");
	}
	public static void execute_update_quere(String query) throws SQLException {
		// 2. Create a statement
		myStmt = myConn.createStatement();
		
		// 3. Execute SQL query
		myStmt.executeUpdate(query);
	}
	public static void execute_insert_quere(String query) throws SQLException {
		// 2. Create a statement
			myStmt = myConn.createStatement();
			
			// 3. Execute SQL query
			myStmt.executeUpdate(query);
	}
	public  static ResultSet execute_select_query(String query) throws SQLException {
		// 2. Create a statement
		myStmt = myConn.createStatement();
		
		// 3. Execute SQL query
		myRs = myStmt.executeQuery(query);
		return myRs;
	}
//	private void test_connection() throws SQLException {
//			try {
//				// 1. Get a connection to database
//				myConn = DriverManager.getConnection("jdbc:mysql://localhost/demo","root","01021592414ahmed");
//				// 2. Create a statement
//				myStmt = myConn.createStatement();
//				
//				// 3. Execute SQL query
////				myRs = myStmt.executeQsuery("select * from `employees`");
////				String q="INSERT INTO `employees`(`last_name`, `first_name`, `email`, `department`, `salary`) VALUES (\"hamdy\",\"ahmed\",\"jfnvdvnf\",\"jnvnfv\",5000);";
//				String q="UPDATE `employees` SET `first_name`=\"mohamed\" WHERE last_name=\"hamdy\"";
//				System.out.println(q);
//				myStmt.executeUpdate(q);
//				// 4. Process the result set
////				while (myRs.next()) {
////					System.out.println(myRs.getString("last_name") + ", " + myRs.getString("first_name"));
////				}
//			}
//			catch (Exception exc) {
//				exc.printStackTrace();
//			}
//			finally {
//				if (myRs != null) {
//					myRs.close();
//				}
//				
//				if (myStmt != null) {
//					myStmt.close();
//				}
//				
//				if (myConn != null) {
//					myConn.close();
//				}
//			}
//	 }
//	}
	}
//	public static void main(String[] args) throws SQLException {
//		
////		Driver d=new Driver();
//		DB tst= new DB();
////		DB.make_connection();
//		tst.test_connection();
//	}

}
