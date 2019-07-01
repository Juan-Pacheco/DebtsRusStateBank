import java.util.*;

public class DebtsRus {

	static Customer activeUser;
	static Inputs inputs;
	static DBConnection DBconn;

	public static void main(String[] args) {
	    activeUser = new Customer();
	    inputs = new Inputs();
	    DBconn = new DBConnection();
	    printBanner();	    

	    String input = "";
	    while (!input.equals("exit")) {
	      input = getUserInput();
	      if (inputs.contains(input)) {
	        switch (input) {
	          
	        case "login":
	        	activeUser.login();
	        	break;
	        	
	        case "bank teller":
	        	activeUser.bank_teller();
	        	break;
	        	
	        case "register":
	        	activeUser.register();
	        	break;
	        	
	        case "deposit":
	        	activeUser.deposit();
	        	break;
	        	
	        case "top up":
	        	activeUser.topUp();
	        	break;
	        	
	        case "withdrawal":
	        	activeUser.withdrawal();
	        	break;
	        	
	        case "purchase":
	        	activeUser.purchase();
	        	break;
	        	
	        case "transfer":
	        	activeUser.transfer();
	        	break;
	        	
	        case "collect":
	        	activeUser.collect();
	        	break;
	        	
	        case "pay friend":
	        	activeUser.payFriend();
	        	break;
	        	
	        case "wire":
	        	activeUser.wire();
	        	break;
	        	
	        case "write check":
	        	activeUser.writeCheck();
	        	break;
	        	
	        case "check transaction":
	        	activeUser.checkTransaction();
	        	break;
	        	
	        case "monthly statement":
	        	activeUser.monthlyStatement();
	        	break;
	        	
	        case "list closed accounts":
	        	activeUser.listClosedAccounts();
	        	break;
	        	
	        case "DTER":
	        	activeUser.DTER();
	        	break;
	        	
	        case "customer report":
	        	activeUser.listCR();
	        	break;
	        	
	        case "add interest":
	        	activeUser.accrueInterest();
	        	break;
	        	
	        case "create account":
	        	activeUser.createAccount();
	        	break;
	        	
	        case "delete accounts and customers":
	        	activeUser.deleteAC();
	        	break;
	        	
	        case "delete transactions":
	        	activeUser.deleteTransactions();
	        	break;
	        
	        case "set date":
	            activeUser.setDate();
	            break;
	        	
	        case "logout":
	            activeUser.logout();
	            break;
	        
	        case "help":
	        	inputs.print();	          
	        	break;
	        	
	        case "list customer accounts":
	    		activeUser.getCustomerAccounts();
        		break;
        		
	        case "verify pin":
	            activeUser.verifyPin();
	            break;
	            
	        case "add customer to account":
	        	activeUser.connectCtoA();
	        	break;
	            
	        ////////////////////////////////
	        	
	        case "list customers":
	        	activeUser.listAllC();
	        	break;
	        
	      	case "list transactions":
	        	activeUser.listAllT();
	        	break;
	      
	    	case "list accounts":
	    		activeUser.listAllA();
        		break;
	    
			case "list owns":
				activeUser.listAllO();
				break;
				
			case "list linked":
				activeUser.listAllL();
				break;
				
			case "list db":
				activeUser.listAllD();
				break;	
			
			case "date":
				activeUser.printDate();
				break;
				
	        }	
				
	      } else {
	    	  System.out.println(String.format("'%s' is not a valid input", input)); 
	      }
	    }

	    DBconn.closeConnection();
	    System.exit(0);
	  }

	  private static void printBanner() {
	    System.out.println("");
	    System.out.println("");
	    System.out.println("===================================");
	    System.out.println("Welcome to Debts R Us State Bank!");
		System.out.println("===================================");
		activeUser.printDate();
	    System.out.println("Type 'help' for help");
	    System.out.println("");  
	  }

	  private static String getUserInput() {
	    System.out.print(" > ");
	    Scanner in = new Scanner(System.in);
	    return(in.nextLine());
	  }  
}
