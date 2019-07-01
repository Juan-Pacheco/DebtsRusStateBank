import java.sql.*;
import java.util.*;

public class DBConnection {

	static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
	static final String DB_URL = "jdbc:oracle:thin:@cloud-34-133.eci.ucsb.edu:1521:XE";
	//  Database credentials
	static final String USERNAME = "juan_pacheco";
	static final String PASSWORD = "9628355";

	Connection conn;

    public DBConnection(){
    	getDriver();
        getConnection();
    }

    private void getDriver() {
        try {
          Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
      
    private void getConnection() {
        try {
          conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    
    public Boolean login(String pin, String taxid) {
    	String query = String.format("SELECT * FROM Customers WHERE taxid='%s'", taxid);    	
    	try (Statement statement = conn.createStatement()) {
    	      ResultSet rs = statement.executeQuery(query);
    	      if (rs.next()) {
    	    	  String querypin = String.format("call customer_security.valid_customer('%s','%s')", taxid, pin);    	      		
    	    	  try (CallableStatement statement1 = conn.prepareCall(querypin)) {
    	    		  statement1.executeQuery();
    	    		  DebtsRus.activeUser.name = rs.getString("name");
    	    		  DebtsRus.activeUser.taxid = rs.getString("taxid");
    	    		  return true; 	    		  
    	    	  }catch (SQLException e) {}
    	      }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}    	 
    	return false;    	
    }
    
    public Boolean verifyPin(String pin, String taxid) {
    	String query = String.format("SELECT * FROM Customers WHERE taxid='%s'", taxid);    	
    	try (Statement statement = conn.createStatement()) {
    	      ResultSet rs = statement.executeQuery(query);
    	      if (rs.next()) {
    	    	  String querypin = String.format("call customer_security.valid_customer('%s','%s')", taxid, pin);    	      		
    	    	  try (CallableStatement statement1 = conn.prepareCall(querypin)) {
    	    		  statement1.executeQuery();
    	    		  return true; 	    		  
    	    	  }catch (SQLException e) {}
    	      }
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}    	 
    	return false;    	
    }
    
    public boolean register(String name, String address, String taxid, String pin) {
    	String queryCustomer = String.format("{call customer_security.add_customer('%s','%s','%s','%s')", name, address, taxid, pin);
    	try(CallableStatement statement = conn.prepareCall(queryCustomer)){
    		statement.executeQuery();
    		return true;
        }catch(SQLException e){
          e.printStackTrace();
        }
        return false;
    }
    
    public boolean createSavingsAccount(int aid, String branch, double deposit) {
    	String queryAccount = String.format("INSERT INTO Account(aid, closed, branch, a_type, balance, interest) VALUES(%d, 0, '%s', 'Savings', %f, 7.5)", aid, branch, deposit);
        try(Statement statement = conn.createStatement()){
          statement.executeUpdate(queryAccount);
          System.out.println("Success! Savings Account created with balance of " + deposit); 
          return true;
        }catch(SQLException e){
          e.printStackTrace();
        }
        return false;
    }
    
    public boolean createCheckingsAccount(int aid, String branch, double deposit, int type) {
    	String queryAccount;
    	if (type == 0) {
    		queryAccount = String.format("INSERT INTO Account(aid, closed, branch, a_type, balance, interest) VALUES(%d, 0, '%s', 'Interest-Checking', %f, 5.5)", aid, branch, deposit);
    	}
    	else {
    		queryAccount = String.format("INSERT INTO Account(aid, closed, branch, a_type, balance, interest) VALUES(%d, 0, '%s', 'Student-Checking', %f, 0)", aid, branch, deposit);
    	}
    	try(Statement statement = conn.createStatement()){
          statement.executeUpdate(queryAccount);
          System.out.println("Success! Checkings Account created with balance of " + deposit);
          return true;
        }catch(SQLException e){
          e.printStackTrace();
        }
    	return false;
    }
    
    public boolean createPocketAccount(int aid, String branch, double deposit, int link, String taxid) {
    	int closed = 0;
    	Set<Integer> avAccounts = new HashSet<Integer>();
    	String query = String.format("SELECT A.aid FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid AND A.a_type<>'Pocket'", taxid);
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
    		while (rs.next()) { 	      		
    			int account = rs.getInt("aid");  	
    			avAccounts.add(account);
    		}  	      	  	      	
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	
    	if (!avAccounts.contains(aid)) {
            System.out.println("You do not own this account: " + aid);
            return false;
    	}
    	
    	String queryClosed = String.format("SELECT closed FROM Account WHERE aid=%d", aid);
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(queryClosed);
    		if(rs.next()) { 			 		 	   		
    			closed = rs.getInt("closed");    	  	      	
    		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	
    	if (closed == 1) {
            System.out.println("This account is closed");
            return false;
    	}
    	
    	String queryAccount = String.format("INSERT INTO Account(aid, closed, branch, a_type, balance, interest) VALUES(%d, 0, '%s', 'Pocket', 0, 0)", link, branch);
        try(Statement statement = conn.createStatement()){
          statement.executeUpdate(queryAccount);
          updateTwoBal(-1 * deposit, aid, taxid, link, taxid);
          System.out.println("Success! Pocket Account created with balance of " + deposit);
        }catch(SQLException e){
          e.printStackTrace();
        }
        
        String queryLinked = String.format("INSERT INTO Linked(aid, linked) VALUES(%d, %d)", aid, link);
        try(Statement statement = conn.createStatement()){
          statement.executeUpdate(queryLinked);
          System.out.println("Accounts linked");
          return true;
        }catch(SQLException e){
          e.printStackTrace();
        }
        return false;
    }
    
    public void connectCtoA(String taxid, int aid, boolean primary) {
    	int powner;
    	if (primary) {
    		powner = 1;
    	}
    	else {
    		powner = 0;
    	}
    	String queryConnect = String.format("INSERT INTO Owns(taxid, aid, primary) VALUES('%s', %d, %d)", taxid, aid, powner);
        try(Statement statement = conn.createStatement()){
          statement.executeUpdate(queryConnect);
        }catch(SQLException e){
          e.printStackTrace();
        }
    }
    
    public Set<Integer> getAccountsSet(String taxid){
    	String query = String.format("SELECT aid FROM Owns WHERE taxid='%s'", taxid);
    	Set<Integer> avAccounts = new HashSet<Integer>();
    	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int accoun = rs.getInt("aid");  		       	      		
    			System.out.println(accoun);
    			avAccounts.add(accoun);
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  		       	      		
  	      			System.out.println(account); 
  	      			avAccounts.add(account);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }
    
    public Set<Integer> getCSAccounts(String taxid) {
    	String query = String.format("SELECT A.aid FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid AND A.a_type<>'Pocket'", taxid);
    	Set<Integer> avAccounts = new HashSet<Integer>();   	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int accoun = rs.getInt("aid");  		       	      		
  	      		System.out.println(accoun);
  	      		avAccounts.add(accoun);
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  		       	      		
  	      			System.out.println(account);  	      	
  	      			avAccounts.add(account);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }
    
    public Set<Integer> getCAccounts(String taxid) {
    	String query = String.format("SELECT A.aid FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid AND (A.a_type='Interest-Checking' OR A.a_type='Student-Checking')", taxid);
    	Set<Integer> avAccounts = new HashSet<Integer>();  	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int accoun = rs.getInt("aid");  		       	      		
  	      		System.out.println(accoun);
  	      		avAccounts.add(accoun);
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  		       	      		
  	      			System.out.println(account);
  	      			avAccounts.add(account);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }
    
    public Set<Integer> getSAccounts(String taxid) {
    	String query = String.format("SELECT A.aid FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid AND A.a_type='Savings'", taxid);
    	Set<Integer> avAccounts = new HashSet<Integer>();   	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int accoun = rs.getInt("aid");  		       	      		
  	      		System.out.println(accoun);
  	      		avAccounts.add(accoun);
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  		       	      		
  	      			System.out.println(account);  
  	      			avAccounts.add(account);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }
    
    public Set<Integer> getPAccounts(String taxid) {
    	String query = String.format("SELECT A.aid FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid AND A.a_type='Pocket'", taxid);
    	Set<Integer> avAccounts = new HashSet<Integer>();  	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int accoun = rs.getInt("aid");  		       	      		
  	      		System.out.println(accoun);
  	      		avAccounts.add(accoun);
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  		       	      		
  	      			System.out.println(account);  
  	      			avAccounts.add(account);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }

    public Set<Set<Integer>> getLAccounts(String taxid) {
    	String query = String.format("SELECT L.aid, L.linked, A.a_type FROM Owns O, Account A, Linked L WHERE O.taxid='%s' AND O.aid=A.aid AND L.aid=A.aid", taxid);
    	Set<Set<Integer>> avAccounts = new HashSet<Set<Integer>>();   	
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
  	      	if(rs.next()) {   		
  	      		int aid = rs.getInt("aid");
  	      		int linked = rs.getInt("linked");
  	      		String a_type = rs.getString("a_type");
  	      		Set<Integer> accPair = new HashSet<Integer>();
  	      		accPair.add(aid);
  	      		accPair.add(linked);
  	      		System.out.println("Pocket account: " + linked + " is linked to " + a_type + " account: " + aid);
  	      		avAccounts.add(accPair);
  	      		while (rs.next()) { 	
  	      			Set<Integer> accPairs = new HashSet<Integer>();
  	      			int aids = rs.getInt("aid");
  	      			int linkeds = rs.getInt("linked");
  	      			String a_types = rs.getString("a_type");
  	      			accPairs.add(aids);
  	      			accPairs.add(linkeds);
  	      			System.out.println("Pocket account: " + linkeds + " is linked to " + a_types + " account: " + aids);  
  	      			avAccounts.add(accPairs);
  	      		}  	      	
  	      	}
  	      	else {
  	      		System.out.println("You have no available accounts");
  	      	}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    	return avAccounts;
    }
    
    public void listCR(String taxid) {
    	String query = String.format("SELECT A.aid, closed FROM Owns O, Account A WHERE O.taxid='%s' AND O.aid=A.aid", taxid);
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query); 	      	  		
    		while (rs.next()) { 	      		
    			int account = rs.getInt("aid");
    			int closed = rs.getInt("closed");
    			if (closed == 1) {
    				System.out.println("Account " + account + ": closed");
  	      		}
    			else {
    				System.out.println("Account " + account + ": open");
    			}
    		}  	      	 	      	  	      	
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void newTransaction(String t_type, double amount, int account_from, int account_to, int checkno, String taxid) {
    	String query;    	
    	String date = formatDate(getDate());
    	if(t_type.equals("deposit")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Deposit', %f, NULL, %d, NULL, '%s', '%s')", amount, account_to, date, taxid);
    	}
    	else if(t_type.equals("top up")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Top-up', %f, %d, %d, NULL, '%s', '%s')", amount, account_from, account_to, date, taxid);
    	}
    	else if(t_type.equals("withdrawal")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Withdrawal', %f, %d, NULL, NULL, '%s', '%s')", amount, account_from, date, taxid);
    	}
    	else if(t_type.equals("purchase")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Purchase', %f, %d, NULL, NULL, '%s', '%s')", amount, account_from, date, taxid);
    	}
    	else if(t_type.equals("transfer")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Transfer', %f, %d, %d, NULL, '%s', '%s')", amount, account_from, account_to, date, taxid);
    	}
    	else if(t_type.equals("collect")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Collect', %f, %d, %d, NULL, '%s', '%s')", amount, account_from, account_to, date, taxid);
    	}
    	else if(t_type.equals("pay friend")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Pay-friend', %f, %d, %d, NULL, '%s', '%s')", amount, account_from, account_to, date, taxid);
    	}
    	else if(t_type.equals("wire")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Wire', %f, %d, %d, NULL, '%s', '%s')", amount, account_from, account_to, date, taxid);
    	}
    	else if(t_type.equals("write check")) {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Write-check', %f, %d, NULL, %d, '%s', '%s')", amount, account_from, checkno, date, taxid);
    	}
    	else {
    		query = String.format("INSERT INTO Transactions (t_type, amount, account_from, account_to, checkno, transdate, taxid) VALUES ('Accrue-interest', %f, NULL, %d, NULL, '%s', '%s')", amount, account_to, date, taxid);
    	}
    	
        try (Statement statement = conn.createStatement()) {
          statement.executeUpdate(query);
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }
    
    public String getDate() {
        String query = String.format("SELECT * FROM Bank_Date");
        String currdate = "";
        try (Statement statement = conn.createStatement()){
          ResultSet rs = statement.executeQuery(query);
          if(rs.next()){
            currdate = rs.getString("bdate");
          }
        } catch (SQLException e){
          e.printStackTrace();
        }
        String temp = currdate.substring(0,10);
        return temp;
    }
    
    public String formatDate(String currdate) {
    	String month = currdate.substring(5,7);
        String mt = "";
        if (month.equals("01")) {
        	mt = "JAN";
        }else if (month.equals("02")) {
        	mt = "FEB";
        }else if (month.equals("03")) {
        	mt = "MAR";
        }else if (month.equals("04")) {
        	mt = "APR";
        }else if (month.equals("05")) {
        	mt = "MAY";
        }else if (month.equals("06")) {
        	mt = "JUN";
        }else if (month.equals("07")) {
        	mt = "JUL";
        }else if (month.equals("08")) {
        	mt = "AUG";
        }else if (month.equals("09")) {
        	mt = "SEP";
        }else if (month.equals("10")) {
        	mt = "OCT";
        }else if (month.equals("11")) {
        	mt = "NOV";
        }else {
        	mt = "DEC";
        }
        String temp = currdate.substring(8,10)+"-"+mt+"-"+currdate.substring(0,4);

        return temp;
    }
    
    public void setDate(String date){
        int day = dayToInt(date);
        int todayday = dayToInt(getDate());
        int month = monthToInt(date);
        int todaymonth = monthToInt(getDate());
        int year = yearToInt(date);
        int todayyear = yearToInt(getDate());
                
        if(year < todayyear) {
        	System.out.println("Cannot go back in time1.");
            return;
		}
        
        if(month < todaymonth) {
    		if(year <= todayyear) {
    			System.out.println("Cannot go back in time.2");
    	          return;
    		}
    	}
        
        if(day < todayday) {
        	if(month <= todaymonth) {
        		if(year <= todayyear) {
        			System.out.println("Cannot go back in time3.");
        	          return;
        		}
        	}
        }

        if (todaymonth == 1 || todaymonth == 3 || todaymonth == 5 || todaymonth == 7 || todaymonth == 8 || todaymonth == 10 || todaymonth == 12) {
        	if (todayday == 31) {
        		todayday = 0;
        	}
        }else if(todaymonth == 4 || todaymonth == 6 || todaymonth == 9 || todaymonth == 10) {
        	if (todayday == 30) {
        		todayday = 0;
        	}
        }else {
        	if (todayday == 28) {
        		todayday = 0;
        	}
        }
        
        for (int i = 0; i < (day - todayday); i++) {
          advanceDate();
        }
    }
    
    public int dayToInt(String date){
        int temp = Integer.parseInt(date.substring(8,10));
        return temp;
    }
    
    public int monthToInt(String date){
        int temp = Integer.parseInt(date.substring(5,7));
        return temp;
    }
    
    public int yearToInt(String date){
        int temp = Integer.parseInt(date.substring(0,4));
        return temp;
    }
    
    public void advanceDate() {
        String today = getDate();
        int year = yearToInt(today);
        int month = monthToInt(today);
        int day = dayToInt(today)+1;
        
        if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
        	if (day == 32) {
        		day = 1;
        		month += 1;
        		if (month == 13) {
        			month = 1;
        			year += 1;
        		}
        	}
        }else if(month == 4 || month == 6 || month == 9 || month == 10) {
        	if (day == 31) {
        		day = 1;
        		month += 1;
        	}
        }else {
        	if (day == 29) {
        		day = 1;
        		month += 1;
        	}
        }
        
        String days = Integer.toString(day);
        String months = Integer.toString(month);
        if(day < 10) {
        	days = "0" + days;
        }
        if(month < 10) {
        	months = "0" + months;
        }
        String tomm = year + "-" + months + "-" + days;        
        insertDailyBal();        
        appDate(tomm);
        int day1 = dayToInt(getDate());
        if(day1 == 1) {
        	accrueInt();
        	deleteDailyBal();
        	deleteAllT();        	
        }
    }
    
    public void appDate(String date){
    	String fdate = formatDate(date);
        String query = String.format("UPDATE Bank_Date SET bdate = '%s'", fdate);
        try(Statement statement = conn.createStatement()){
          statement.executeUpdate(query);
          System.out.println("Date is now: " + date);
        } catch (SQLException e){
            e.printStackTrace();
        }
        return;
    }
    
    public boolean updateBal(double amount, int aid, String taxid) {	 	
    	String query = String.format("SELECT balance, closed FROM Account WHERE aid=%d", aid);
    	try (Statement statement = conn.createStatement()) {
    	      ResultSet rs = statement.executeQuery(query);
    	      if (rs.next()) {
    	        double balance = rs.getDouble("balance");
    	        int closed = rs.getInt("closed");
    	        if (closed == 1) {
    	        	System.out.println(String.format("Transaction failed. This account is closed"));
    	        	return false;
    	        }
    	        double newBalance1 = balance + amount;
    	        double newBalance = (double)Math.round(newBalance1 * 100d) / 100d;
    	        if (newBalance >= 0) {
    	        	if(newBalance > .01) {
    	        		query = String.format("UPDATE Account SET balance='%.2f' WHERE aid=%d", newBalance, aid);
    	        		statement.executeUpdate(query);
        	        	System.out.println(String.format("Success! Account " + aid + " balance is now $%.2f", newBalance));
    	        	}
    	        	else {
    	        		query = String.format("UPDATE Account SET balance='%.2f', closed=1 WHERE aid=%d", newBalance, aid);
    	        		statement.executeUpdate(query);
        	        	System.out.println(String.format("Account " + aid + " balance is now insufficient. This account is now closed"));
    	        	}
    	        	return true;
    	        } else {
    	        	System.out.println("Transaction failed. Account balance cannot fall below $0");
    	        	return false;	
    	        }
    	      }
    		} catch (SQLException e) {    	
    			e.printStackTrace();    	    
    		}
    		return false;
    }
    
    public boolean updateTwoBal(double amount, int aid, String taxid, int aid1, String taxid1) {		
    	String query = String.format("SELECT balance, closed FROM Account WHERE aid=%d", aid);    	
    	try (Statement statement = conn.createStatement()) {
    	      ResultSet rs = statement.executeQuery(query);
    	      if (rs.next()) {
    	        double balance = rs.getDouble("balance");
    	        int closed = rs.getInt("closed");
    	        if (closed == 1) {
    	        	System.out.println(String.format("Transaction failed. This account is closed"));
    	        	return false;
    	        }
    	        double newBalance = balance + amount;
    	        if (newBalance >= 0) {
    	        	if(newBalance > .01) {
    	        		if (updateBal(-1 * amount, aid1, taxid1)) {
    	        		query = String.format("UPDATE Account SET balance='%.2f' WHERE aid=%d", newBalance, aid);
    	        		statement.executeUpdate(query);
        	        	System.out.println(String.format("Success! Account " + aid +" balance is now $%.2f", newBalance));
    	        		}
    	        		else {
    	        			return false;
    	        		}
    	        	}
    	        	else {
    	        		if (updateBal(-1 * amount, aid1, taxid1)) {
    	        		query = String.format("UPDATE Account SET balance='%.2f', closed=1 WHERE aid=%d", newBalance, aid);
    	        		statement.executeUpdate(query);
        	        	System.out.println(String.format("Account " + aid +" balance is now insufficient. This account is now closed"));
    	        		}
    	        		else {
    	        			return false;
    	        		}
    	        	}
    	        	return true;
    	        } else {
    	        	System.out.println("Transaction failed. Account balance cannot fall below $0");
    	        	return false;	
    	        }
    	      }
    		} catch (SQLException e) {    	
    			e.printStackTrace();    	    
    		}
    		return false;   	
    }
    
    public boolean isInDB(String taxid) {
    	String query = String.format("SELECT 1 FROM Customers WHERE taxid='%s'", taxid);
    	try (Statement statement = conn.createStatement()){
    		ResultSet rs = statement.executeQuery(query);
  	      if (rs.next()) {
  	    	return true;  
  	      }
  	      else {
  	    	  System.out.println("This person does not appear in our database");
  	    	  return false;
  	      }
    	}catch (SQLException e) {    	
    		e.printStackTrace();    	    
		}
    	return false;
    }
    
    public boolean isInOwns(String taxid) {
    	String query = String.format("SELECT 1 FROM Owns WHERE taxid='%s'", taxid);
    	try (Statement statement = conn.createStatement()){
    		ResultSet rs = statement.executeQuery(query);
  	      if (rs.next()) {
  	    	return true;  
  	      }
  	      else {
  	    	  System.out.println("This person does not own any accounts. Will be deleted from database");
  	    	  return false;
  	      }
    	}catch (SQLException e) {    	
    		e.printStackTrace();    	    
		}
    	return false;
    }
    
    public void insertDailyBal() {
    	String query = "SELECT * FROM Account WHERE a_type<>'Pocket'";
        try (Statement statement = conn.createStatement()) {
          ResultSet rs = statement.executeQuery(query);
          String updateQuery;
          while (rs.next()) {
            int aid = rs.getInt("aid");
            double balance = rs.getDouble("balance");            
            String date = formatDate(getDate());
            updateQuery = String.format("INSERT INTO Daily_Bal VALUES (%d, '%s', %f)", aid, date, balance);
            try (Statement statement1 = conn.createStatement()) {
            	statement1.executeUpdate(updateQuery);
            } catch (SQLException e) {
            	e.printStackTrace();
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
    }
    
    public void accrueInt() {
    	String query = "SELECT aid, COUNT(bdate), SUM(balance) FROM Daily_Bal GROUP BY aid";   	
        try (Statement statement = conn.createStatement()){
          ResultSet rs = statement.executeQuery(query);
          while (rs.next()) {
            int aid = rs.getInt("aid");
	    	  System.out.println(aid);
            int count = rs.getInt("COUNT(bdate)");
            double sum = rs.getDouble("SUM(balance)");         
            String queryAccount = String.format("SELECT O.taxid, A.interest FROM Account A, Owns O WHERE A.aid=%d AND A.aid=O.aid", aid);           
            String taxid = "";
            double interest = 0;
            try (Statement statement1 = conn.createStatement()){
            	ResultSet rs1 = statement.executeQuery(queryAccount);
            	if(rs1.next()) {
            		taxid = rs1.getString("taxid");
            		interest = rs1.getFloat("interest") * .01;
            	}
            }catch (SQLException e) {
                e.printStackTrace();
            }
            double interests = (double)Math.round(interest * 1000d) / 1000d;
            double avg = sum / count;
            double BaltoAdd = avg * interests;
            if (BaltoAdd != 0) {
            	updateBal(BaltoAdd, aid, taxid);
            	newTransaction("accrue interest", BaltoAdd , -1, aid, -1, taxid);
            }
          }
        } catch (SQLException e) {
          e.printStackTrace();
        }
        return;
    }
    
    public void listClosedAccounts(){
    	String query = String.format("SELECT aid FROM Account WHERE closed=1");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  
  	      			System.out.println(account);
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
	}
    
    public void deleteClosedAccounts() {
    	String query = String.format("DELETE FROM Account WHERE closed=1");
    	try (Statement statement = conn.createStatement()) {
    		statement.executeQuery(query);   		
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void deleteCustomers(String taxid) {
    	String query = String.format("DELETE FROM Customers WHERE taxid='%s'", taxid);
    	try (Statement statement = conn.createStatement()) {
    		statement.executeQuery(query);   		
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void deleteCustomers() {   	
    	String query = String.format("SELECT taxid FROM Customers");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
    		while (rs.next()) { 	      		
    			String taxid = rs.getString("taxid");  
  	      		if(!isInOwns(taxid)) {
  	      			String queryCustomers = String.format("DELETE FROM Customers WHERE taxid='%s'", taxid);
  	      			try (Statement statement1 = conn.createStatement()) {
  	      				statement.executeQuery(queryCustomers);   		
  	      			} catch (SQLException e) {
  	      				e.printStackTrace();
  	      			}
  	      		}  	      		
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}    	
    }
    
    public void deleteTransactions() {
    	String date = getDate();
    	int month = monthToInt(date);
    	int year = yearToInt(date);   	
    	String queryTrans = "Select * From Transactions";
    	try(Statement statement = conn.createStatement()){
    		ResultSet rs = statement.executeQuery(queryTrans);   		
    		while (rs.next()) { 	      		
    			boolean flag = false;
    			int tid = rs.getInt("tid");
    			String tdate = rs.getString("transdate").substring(0,10);
    			int tmonth = monthToInt(tdate);
    			int tyear = yearToInt(tdate);
    			if (tyear < year) {
    				String query = String.format("DELETE FROM Transactions WHERE tid=%d", tid);
    				flag = true;
    				try (Statement statement1 = conn.createStatement()) {
    					statement1.executeQuery(query);   		
    				} catch (SQLException e) {
    					e.printStackTrace();
    				}
    			}
    			if(!flag) {
    				if (tmonth < month) {
    					String query = String.format("DELETE FROM Transactions WHERE tid=%d", tid);
        				try (Statement statement1 = conn.createStatement()) {
        					statement1.executeQuery(query);   		
        				} catch (SQLException e) {
        					e.printStackTrace();
        				}
    				}
    			}    			
    		}
    	}catch (SQLException e) {
    	      e.printStackTrace();
    	}    	
    }
    
    public void deleteDailyBal() {
    	String query = String.format("DELETE FROM Daily_Bal");
    	try (Statement statement = conn.createStatement()) {
    		statement.executeQuery(query);   		
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void monthlyStatement(String taxid) {
    	String queryAccounts = String.format("SELECT * FROM Owns WHERE taxid='%s'", taxid);
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(queryAccounts);
    		while (rs.next()) {
    			int aid = rs.getInt("aid");
    			System.out.println("Transactions of account " + aid + ":");
    			String queryTransactions = String.format("SELECT * FROM Transactions WHERE account_from=%d OR account_to=%d", aid, aid);
    			try (Statement statement1 = conn.createStatement()) {
    				ResultSet rs1 = statement1.executeQuery(queryTransactions);
    				System.out.println("TID \t \t T_Type \t \t Amount \t \t Account_from \t \t Account_to \t \t Checkno \t \t Transaction Date");
    				while (rs1.next()) {
    					int account_from = rs1.getInt("account_from");  
      	      			int account_to = rs1.getInt("account_to"); 
      	      			int tid = rs1.getInt("tid");
      	      			String date = rs1.getString("transdate");
      	      			String t_type = rs1.getString("t_type");
      	      			double amount = rs1.getFloat("amount");
      	      			int checkno = rs1.getInt("checkno");
      	      			String fdate = formatDate(date);
      	      			double amounts = (double)Math.round(amount * 100d) / 100d;      	      			
      	      			System.out.println(tid + " \t \t " + t_type + "      \t \t " + amounts + " \t \t \t " + account_from + " \t \t \t" + account_to + "\t \t \t " + checkno + "\t \t " + fdate);      	      			
    				}
    			}catch (SQLException e) {
    	    	      e.printStackTrace();
    	    	}   
    			System.out.println("Owners of account " + aid + ":");
    			String queryOwners = String.format("SELECT C.address, C.name FROM Owns O, Customers C WHERE O.aid=%d AND O.taxid=C.taxid", aid);
    			try (Statement statement1 = conn.createStatement()) {
    				ResultSet rs1 = statement1.executeQuery(queryOwners);
    				System.out.println("Name \t \t \t Address");
    				while (rs1.next()) {
    					String name = rs1.getString("name");
    					String address = rs1.getString("address");
        				System.out.println(name + "\t \t" + address);
    				}
    			}catch (SQLException e) {
  	    	      e.printStackTrace();
    			}
    		}    		
    	}catch (SQLException e) {
    	      e.printStackTrace();
    	}
    	
    	String query = String.format("SELECT SUM(balance) FROM Account A, Owns O WHERE O.taxid='%s' AND O.aid=A.aid AND O.primary=1", taxid);
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);
    		if (rs.next()) {
    			double sum = rs.getFloat("SUM(balance)");
    			double sums = (double)Math.round(sum * 100d) / 100d;
    			if(sums > 100000) {
    				System.out.println("The total sum of your accounts exceeds $100,000. The limit of the insurance has been reached");
    			}
    		}		
    	}catch (SQLException e) {
  	      e.printStackTrace();
    	}    	
    }

    public void DTER() {
    	String query = String.format("SELECT * FROM Customers");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
    		while (rs.next()) { 	      		
  	      			String taxid = rs.getString("taxid");
  	      			String name = rs.getString("name");
  	    			String queryTransactions = String.format("SELECT SUM(amount) FROM Transactions T, Owns O WHERE O.taxid='%s' AND O.aid=T.account_to AND T.taxid=O.taxid", taxid); 	      			
  	    			try (Statement statement1 = conn.createStatement()) {
  	    	    		ResultSet rs1 = statement1.executeQuery(queryTransactions);
  	    	    		if (rs1.next()) {
  	    	    			double sum = rs1.getFloat("SUM(amount)");
  	    	    			double sums = (double)Math.round(sum * 100d) / 100d;
  	    	    			if(sums > 10000) {
  	    	    				System.out.println(name);
  	    	    			}
  	    	    		}		
  	    	    	}catch (SQLException e) {
  	    	  	      e.printStackTrace();
  	    	    	} 	      			
    		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void deleteAllT() {
    	String query = String.format("DELETE FROM Transactions");
    	try (Statement statement = conn.createStatement()) {
    		statement.executeQuery(query);   		
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    //////////////////////////////////////
    
    public void listAllA(){
    	String query = String.format("SELECT * FROM Account");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int account = rs.getInt("aid");  
  	      			int closed = rs.getInt("closed");
  	      			String branch = rs.getString("branch");
  	      			String a_type = rs.getString("a_type");
  	      			double balance = rs.getFloat("balance");
  	      			double interest = rs.getFloat("interest");
  	      			double balances = (double)Math.round(balance * 100d) / 100d;
	      			double interests = (double)Math.round(interest * 100d) / 100d;
  	      			System.out.println(account + " " + closed + " " + branch + " " + a_type + " " + balances + " " + interests);  	      	
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void listAllC(){
    	String query = String.format("SELECT * FROM Customers");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			String name = rs.getString("name");   	      			
  	      			String address = rs.getString("address");
  	      			String taxid = rs.getString("taxid");
  	      			String pin = rs.getString("pin");
  	      			System.out.println(name + "\t" + address + "\t" + taxid + "\t" + pin);  	      	
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void listAllT(){
    	String query = String.format("SELECT * FROM Transactions");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int account_from = rs.getInt("account_from");  
  	      			int account_to = rs.getInt("account_to"); 
  	      			int tid = rs.getInt("tid");
  	      			String date = rs.getString("transdate");
  	      			String t_type = rs.getString("t_type");
  	      			double amount = rs.getFloat("amount");
  	      			int checkno = rs.getInt("checkno");
  	      			String fdate = date.substring(0,10);
  	      			double amounts = (double)Math.round(amount * 100d) / 100d;
  	      			System.out.println(tid + " " + t_type + " " + amounts + " " + account_from + " " + account_to + " " + checkno + " " + fdate);  	      	
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void listAllO(){
    	String query = String.format("SELECT * FROM Owns");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int aid = rs.getInt("aid");  
  	      			int primary = rs.getInt("primary");
  	      			String taxid = rs.getString("taxid");
  	      			System.out.println(taxid + " " + aid + " " + primary);  	      	
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void listAllL(){
    	String query = String.format("SELECT * FROM Linked");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int aid = rs.getInt("aid");  
  	      			int linked = rs.getInt("linked");
  	      			System.out.println(aid + " " + linked);  	      	
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void listAllD(){
    	String query = String.format("SELECT * FROM Daily_Bal");
    	try (Statement statement = conn.createStatement()) {
    		ResultSet rs = statement.executeQuery(query);   		
  	      		while (rs.next()) { 	      		
  	      			int aid = rs.getInt("aid");
  	      			String bdate = rs.getString("bdate");
  	      			double balance = rs.getFloat("balance");
  	      			double balances = (double)Math.round(balance * 100d) / 100d;
  	      			System.out.println(aid + " " + bdate.substring(0,10) + " " + balances);
  	      		}
  	    } catch (SQLException e) {
  	      e.printStackTrace();
  	    }
    }
    
    public void closeConnection() {
    	try {
    		if (conn != null) {
    			conn.close();
    		}
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
}
