import java.util.*;

public class Customer {

	public String name;
	public String taxid;	
	public Boolean admin;
	public String password = "wasd1234";
	
	public void login() {
	    // set current user
		System.out.println("Please Insert TaxId and PIN");
	    Scanner in = new Scanner(System.in);
	    System.out.print("TaxId:     ");
	    String taxID = in.nextLine();
	    System.out.print("PIN:     ");
	    String pin = in.nextLine();

	    if (DebtsRus.DBconn.login(pin, taxID)) {
	    	DebtsRus.inputs.setState("user");
	    	taxid = taxID;
	    	admin = false;
	        System.out.println("Login Successful");
	        System.out.println("Hello, "+DebtsRus.activeUser.name);	      
	        return;
	    }
	    System.out.println("Login Failed");
	}
	
	public void bank_teller() {
		Scanner in = new Scanner(System.in);
	    System.out.println("Enter password for bank teller");
	    System.out.print("Password: ");
	    String pw = in.nextLine();
    	if (pw.equals(password)) {
    		DebtsRus.inputs.setState("bank teller");
    		admin = true;
    		System.out.println("Logged in successfully as bank teller");
    	}
    	else {
    		System.out.println("Login Failed");
    	}
	}
	
	public void register(){
		Scanner in = new Scanner(System.in);
	    System.out.print("Name: \t \t");
	    String name = in.nextLine();
	    System.out.print("Address: \t \t");
	    String addr = in.nextLine();
	    System.out.print("Tax ID: \t \t");
	    String taxID = in.nextLine();
	    System.out.print("Choose a 4 digit PIN:  ");
	    String pin = in.nextLine();
	    while(pin.length() != 4) {
		      System.out.println("Your PIN should be four digits");
		      System.out.print("Please choose a 4 digit PIN:  ");
		      pin = in.nextLine();
	    }
	    if(DebtsRus.DBconn.register(name,addr,taxID,pin)){
	      System.out.println("Registration Sucessful!");
	      System.out.println("Your PIN is:" + pin);
	      System.out.println("Upon registering a Account is automatically opened for you.");
	      System.out.println("What type of account would you like to open, Checkings or Savings? 0 for Checkings, 1 for Savings");
	      String atype = in.nextLine();
	      int aitype = Integer.parseInt(atype);
	      while (aitype < 0 || aitype > 1) {
	    	  System.out.println("Please pick Checkings or Savings");
	    	  atype = in.nextLine();
	    	  aitype = Integer.parseInt(atype);
	      }
	      System.out.println("How much would you like to deposit? Min($1)");
	      String deposit = in.nextLine();
	      double value = Double.parseDouble(deposit.replaceAll("[^\\d.]", ""));
	      while(value < 1){
	        System.out.println("You must make an initial deposit of at least $1");
	        deposit = in.nextLine();
	        value = Double.parseDouble(deposit.replaceAll("[^\\d.]", ""));
	      }
	      //int aid = (int)(Math.random() * 99999 + 1); /////////// rerandomized if duplicate aid
	      System.out.println("Please enter the account id that will be associated with this account: ");	    
	      int aid = in.nextInt();	
	      in.nextLine();
	      System.out.println("What branch are you at?");
	      String branch = in.nextLine();
	      if (aitype == 0) {
	    	  System.out.println("What type of Checkings account would you like to open, Interest or Student? 0 for Interest, 1 for Student");
	    	  String ctype = in.nextLine();
	    	  int citype = Integer.parseInt(ctype);
		      while (citype < 0 || citype > 1) {
		    	  System.out.println("Please pick Interest or Student");
		    	  ctype = in.nextLine();
		    	  citype = Integer.parseInt(ctype);
		      }
		      if(!DebtsRus.DBconn.createCheckingsAccount(aid, branch, value, citype)) {
		    	  //delete customer
		    	  DebtsRus.DBconn.deleteCustomers(taxID);
		    	  return;
		      }
	      }
	      else {
	    	  if(!DebtsRus.DBconn.createSavingsAccount(aid, branch, value)) {
	    		  //delete
	    		  DebtsRus.DBconn.deleteCustomers(taxID);
	    		  return;
	    	  }
	      }
	      DebtsRus.DBconn.connectCtoA(taxID, aid, true);	        
	      System.out.println("Account connection made");
	      DebtsRus.DBconn.newTransaction("deposit", value, -1, aid, -1, taxID);
	    }
	    else{
	      System.out.println("Registration unsucessful please try again.");
	    }
	}
	
	
	public void deposit(){
		String taxID = "";
    	Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("Amount:  ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("Available accounts:");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxid);
	    }
	    System.out.println("Which account would you like to deposit into?");
	    int acco = in.nextInt();
	    if(avAccounts.contains(acco)) {
	    	if(admin) {	    	
	    		if (DebtsRus.DBconn.updateBal(amount, acco, taxID)) {
	    			DebtsRus.DBconn.newTransaction("deposit", amount, -1, acco, -1, taxID);
	    		}	    	
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateBal(amount, acco, taxid)) {
	    			DebtsRus.DBconn.newTransaction("deposit", amount, -1, acco, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("Invalid account");
	    }
	}
	
	public void topUp(){
		String taxID = "";
		Set<Set<Integer>> avAccounts = new HashSet<Set<Integer>>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("How much would you like to move? ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("From which account would you like to make this transaction into?");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getLAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getLAccounts(taxid);
	    }
	    System.out.println("Which account would you like to move from?");
	    int acco = in.nextInt();
	    in.nextLine();
	    System.out.println("Which account would you like to move into?");
	    int accos = in.nextInt();
	    in.nextLine();
	    Set<Integer> avAccount = new HashSet<Integer>();
	    avAccount.add(acco);
	    avAccount.add(accos);
	    if(avAccounts.contains(avAccount)) {
	    	if(admin) {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxID, accos, taxID)) {
	    			DebtsRus.DBconn.newTransaction("top up", amount, acco, accos, -1, taxID);
	    		}
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxid, accos, taxid)) {
	    			DebtsRus.DBconn.newTransaction("top up", amount, acco, accos, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("These accounts are not linked");
	    }
	}
	
	public void withdrawal(){
		String taxID = "";
    	Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("Amount:  ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("Available accounts:");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxid);
	    }
	    System.out.println("Which account would you like to withdraw from?");
	    int acco = in.nextInt();
	    if(avAccounts.contains(acco)) {
	    	if(admin) {	    	
	    		if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxID)) {
	    			DebtsRus.DBconn.newTransaction("withdrawal", amount, acco, -1, -1, taxID);
	    		}	    	
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxid)) {
	    			DebtsRus.DBconn.newTransaction("withdrawal", amount, acco, -1, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("Invalid account");
	    }
	}
	
	public void purchase(){
		String taxID = "";
    	Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("Amount:  ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("Available accounts:");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getPAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getPAccounts(taxid);
	    }
	    System.out.println("Which account would you like to purchase from?");
	    int acco = in.nextInt();
	    if(avAccounts.contains(acco)) {
	    	if(admin) {	    	
	    		if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxID)) {
	    			DebtsRus.DBconn.newTransaction("purchase", amount, acco, -1, -1, taxID);
	    		}	    	
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxid)) {
	    			DebtsRus.DBconn.newTransaction("purchase", amount, acco, -1, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("Invalid account");
	    }
	}
	
	public void transfer(){
		String taxID = "";
		Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0 || amount > 2000) {
	      System.out.print("How much would you like to move? ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0 || amount > 2000) {
	        System.out.println("Amount cannot be negative or exceed $2000");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("From which account would you like to make this transaction into?");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxid);
	    }
	    System.out.println("Which account would you like to transfer from?");
	    int acco = in.nextInt();
	    in.nextLine();
	    System.out.println("Which account would you like to transfer into?");
	    int accos = in.nextInt();
	    in.nextLine();
	    if(avAccounts.contains(acco) && avAccounts.contains(accos)) {
	    	if(admin) {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxID, accos, taxID)) {
	    			DebtsRus.DBconn.newTransaction("transfer", amount, acco, accos, -1, taxID);
	    		}
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxid, accos, taxid)) {
	    			DebtsRus.DBconn.newTransaction("transfer", amount, acco, accos, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("These accounts are not owned by the same person");
	    }
	}
	
	public void collect(){
		String taxID = "";
		Set<Set<Integer>> avAccounts = new HashSet<Set<Integer>>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("How much would you like to move? ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    amount -= amount * .03;
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("From which account would you like to make this transaction into?");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getLAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getLAccounts(taxid);
	    }
	    System.out.println("Which account would you like to move from?");
	    int acco = in.nextInt();
	    in.nextLine();
	    System.out.println("Which account would you like to move into?");
	    int accos = in.nextInt();
	    in.nextLine();
	    Set<Integer> avAccount = new HashSet<Integer>();
	    avAccount.add(acco);
	    avAccount.add(accos);
	    if(avAccounts.contains(avAccount)) {
	    	if(admin) {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxID, accos, taxID)) {
	    			DebtsRus.DBconn.newTransaction("collect", amount, acco, accos, -1, taxID);
	    		}
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxid, accos, taxid)) {
	    			DebtsRus.DBconn.newTransaction("collect", amount, acco, accos, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("These accounts are not linked");
	    }
	}

	public void payFriend(){
		String taxID = "";
		Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0 || amount > 2000) {
	      System.out.print("How much would you like to move? ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0 || amount > 2000) {
	        System.out.println("Amount cannot be negative or exceed $2000");
	      }
	    }
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("Please enter the tax id of the person you are paying");
	    String taxID1 = in.nextLine();
	    //System.out.println("From which account would you like to make this transaction on?");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getPAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getPAccounts(taxid);
	    }
	    System.out.println("Which Pocket account would you like to pay from?");
	    int acco = in.nextInt();
	    in.nextLine();
	    Set<Integer> avAccounts1 = DebtsRus.DBconn.getPAccounts(taxID1);
	    System.out.println("Which Pocket account would you like to pay into?");
	    int accos = in.nextInt();
	    in.nextLine();
	    if(avAccounts.contains(acco) && avAccounts1.contains(accos)) {
	    	if(admin) {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxID, accos, taxID)) {
	    			DebtsRus.DBconn.newTransaction("pay friend", amount, acco, accos, -1, taxID);
	    		}
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxid, accos, taxid)) {
	    			DebtsRus.DBconn.newTransaction("pay friend", amount, acco, accos, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("Transaction failed. Invalid accounts");
	    }
	}
	
	public void wire(){
		String taxID = "";
		Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    while (amount < 0) {
	      System.out.print("How much would you like to move? ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    amount -= amount * .02;
	    if(admin) {
	        System.out.println("Please enter the desired person's tax id");
	        taxID = in.nextLine();
	        if(!DebtsRus.DBconn.isInDB(taxID)) {
	        	return;
	        }
	    }
	    System.out.println("Please enter the tax id of the person you are wiring to");
	    String taxID1 = in.nextLine();
	    System.out.println("From which account would you like to make this transaction on?");
	    if(admin) {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxID);
	    }
	    else {
	    	avAccounts = DebtsRus.DBconn.getCSAccounts(taxid);
	    }
	    System.out.println("Which account would you like to transfer from?");
	    int acco = in.nextInt();
	    in.nextLine();
	    DebtsRus.DBconn.getCSAccounts(taxID1);
	    System.out.println("Which account would you like to transfer into?");
	    int accos = in.nextInt();
	    in.nextLine();
	    if(avAccounts.contains(acco)) {
	    	if(admin) {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxID, accos, taxID)) {
	    			DebtsRus.DBconn.newTransaction("wire", amount, acco, accos, -1, taxID);
	    		}
	    	}
	    	else {
	    		if (DebtsRus.DBconn.updateTwoBal(-1 * amount, acco, taxid, accos, taxid)) {
	    			DebtsRus.DBconn.newTransaction("wire", amount, acco, accos, -1, taxid);
	    		}
	    	}
	    }
	    else {
	    	System.out.println("You are not the owner of this account");
	    }
	}

	public void writeCheck(){
    	Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    int checkno = (int)(Math.random() * 9999 + 1);
	    while (amount < 0) {
	      System.out.print("Amount:  ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }
	    System.out.println("Available accounts:");
	    avAccounts = DebtsRus.DBconn.getCAccounts(taxid);
	    System.out.println("Which account would you like to write a check from?");
	    int acco = in.nextInt();
	    if(avAccounts.contains(acco)) {	    	
	    	if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxid)) {
	    		DebtsRus.DBconn.newTransaction("write check", amount, acco, -1, checkno, taxid);
	    	}	    	
	    }
	    else {
	    	System.out.println("Invalid account");
	    }
	}
	
	public void accrueInterest(){
		DebtsRus.DBconn.accrueInt();
		System.out.println("Interest has been applied to all applicable accounts.");
	}
	
	public void checkTransaction(){
    	Set<Integer> avAccounts = new HashSet<Integer>();
		Scanner in = new Scanner(System.in);
	    double amount = -1;
	    int checkno = (int)(Math.random() * 9999 + 1);
	    while (amount < 0) {
	      System.out.print("Amount:  ");
	      amount = in.nextDouble();
	      in.nextLine();
	      if (amount < 0) {
	        System.out.println("Amount cannot be negative");
	      }
	    }	    
	    System.out.println("Please enter the desired person's tax id");
	    String taxID = in.nextLine();
	    if(!DebtsRus.DBconn.isInDB(taxID)) {
	    	return;
	    }	    
	    System.out.println("Available accounts:");	    
	    avAccounts = DebtsRus.DBconn.getCAccounts(taxID);	    	    
	    System.out.println("Which account would you like to withdraw from?");
	    int acco = in.nextInt();
	    if(avAccounts.contains(acco)) {	    	
	    	if (DebtsRus.DBconn.updateBal(-1 * amount, acco, taxID)) {
	    		DebtsRus.DBconn.newTransaction("write check", amount, acco, -1, checkno, taxID);
	    	}	    		    		    	
	    }
	    else {
	    	System.out.println("Invalid account");
	    }
	}

	public void monthlyStatement(){
		Scanner in = new Scanner(System.in);
		System.out.println("Insert the tax id of the desired customer");
		String taxID = in.nextLine();	    
		DebtsRus.DBconn.monthlyStatement(taxID);
	}
	
	public void listCR(){
		Scanner in = new Scanner(System.in);
		System.out.println("Insert the tax id of the desired customer");
		String taxID = in.nextLine();	    
		DebtsRus.DBconn.listCR(taxID);
	}
	
	public void DTER(){
		// use group by aid on trans where account_to, group by taxid on owns
		DebtsRus.DBconn.DTER();
	}

	public void listClosedAccounts(){
		DebtsRus.DBconn.listClosedAccounts();
	}
	
	public void createAccount(){
		Scanner in = new Scanner(System.in);
		if (admin){
			System.out.println("Is the customer in the system? 1 for yes, 0 for no");
			int res = in.nextInt();
			in.nextLine();		
			if (res == 0) {
				register();
				return;
			}
		}
		System.out.println("What type of account would you like to open, Checkings, Savings, or Pocket? 0 for Checkings, 1 for Savings, 2 for Pocket");
		String atype = in.nextLine();	    
		int aitype = Integer.parseInt(atype);	    
		while (aitype < 0 || aitype > 2) {
			System.out.println("Please pick Checkings, Savings, or Pocket");	    	
			atype = in.nextLine();	    	
			aitype = Integer.parseInt(atype);	      
		}
		System.out.println("How much would you like to deposit? Min($1)");	    
		String deposit = in.nextLine();	    
		double value = Double.parseDouble(deposit.replaceAll("[^\\d.]", ""));	    
		while(value < 1){    
			System.out.println("You must make an initial deposit of at least $1");	        
			deposit = in.nextLine();	        
			value = Double.parseDouble(deposit.replaceAll("[^\\d.]", ""));	   
		}
		//int aid = (int)(Math.random() * 99999 + 1); /////////// rerandomized if duplicate aid
		System.out.println("Please enter the account id that will be associated with this account: ");	    
		int aid = in.nextInt();	
		in.nextLine();	
		int link = 0;
		System.out.println("What branch are you at?"); 
		String branch = in.nextLine();    
		if (aitype == 0) {	    
			System.out.println("What type of Checkings account would you like to open, Interest or Student? 0 for Interest, 1 for Student");	    	
			String ctype = in.nextLine();	    	
			int citype = Integer.parseInt(ctype);		    
			while (citype < 0 || citype > 1) {		    
				System.out.println("Please pick Interest or Student");		    	
				ctype = in.nextLine();		    	
				citype = Integer.parseInt(ctype);		      
			}		    	
			if(!DebtsRus.DBconn.createCheckingsAccount(aid, branch, value, citype)) {
				return;
			}
		}
		else if (aitype == 1) {	    	
			if(!DebtsRus.DBconn.createSavingsAccount(aid, branch, value)) {
				return;
			}
		}
		else {
			System.out.println("Please pick which one of the following account(s) to link the Pocket account to");
			DebtsRus.DBconn.getCSAccounts(taxid);
			String slink = in.nextLine();
			link = Integer.parseInt(slink);	
			if(!DebtsRus.DBconn.createPocketAccount(link, branch, value, aid, taxid)) {
				return;
			}
		}
		
		if (aitype == 2) {
			DebtsRus.DBconn.newTransaction("top up", value - 5, link, aid, -1, taxid);
		}
		else {
			DebtsRus.DBconn.newTransaction("deposit", value, -1, aid, -1, taxid);
		}
		DebtsRus.DBconn.connectCtoA(taxid, aid, true);
		System.out.println("Account connection made");
	}
	
	public void deleteAC(){
		DebtsRus.DBconn.deleteClosedAccounts();
		DebtsRus.DBconn.deleteCustomers();
	}
	
	public void deleteTransactions() {
		DebtsRus.DBconn.deleteTransactions();
	}
	
	public void setDate() {
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter the date on the format 'yyyy-mm-dd'");
		String date = in.nextLine();
		DebtsRus.DBconn.setDate(date);
		//DebtsRus.DBconn.appDate(date);
	}
	
	public void verifyPin() {
		System.out.println("Please Insert PIN");
	    Scanner in = new Scanner(System.in);
	    System.out.print("PIN:     ");
	    String pin = in.nextLine();

	    if (DebtsRus.DBconn.verifyPin(pin, taxid)) {
	        System.out.println("PIN verified");
	        return;
	    }
	    System.out.println("PIN not verified");
	}
	public void connectCtoA() {
	    Scanner in = new Scanner(System.in);
		System.out.println("Insert the tax id of the desired customer");
		String taxID = in.nextLine();
		System.out.println("Insert the account id of the accont you want to be added to");
		int aid = in.nextInt();
		in.nextLine();
		DebtsRus.DBconn.connectCtoA(taxID, aid, false);
	}
	
	///////////////////////////////////
	
	public void getCustomerAccounts(){
		Scanner in = new Scanner(System.in);
		System.out.println("Enter the person's taxid");
		String value = in.nextLine();
		DebtsRus.DBconn.getAccountsSet(value);
	}
	
    public void listAllA(){
    	DebtsRus.DBconn.listAllA();
    }
    
    public void listAllC(){
    	DebtsRus.DBconn.listAllC();
    }

    public void listAllT(){
    	DebtsRus.DBconn.listAllT();
    }

    public void listAllO(){
    	DebtsRus.DBconn.listAllO();
    }
    
    public void listAllL(){
    	DebtsRus.DBconn.listAllL();
    }
    
    public void listAllD(){
    	DebtsRus.DBconn.listAllD();
    }
    
    public void printDate() {
    	String date = DebtsRus.DBconn.getDate();
    	System.out.println("Today's date is " + date);
    }
    
	public void logout() {
		DebtsRus.activeUser = new Customer();
		DebtsRus.inputs.setState("noUser");
	    System.out.println("Logout Successful");
	}
}
