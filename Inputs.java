import java.util.*;

public class Inputs {	
	 ArrayList<String> state;	 
	 ArrayList<String> noUser = new ArrayList<>(Arrays.asList("help","login","register","bank teller","exit"));
	 ArrayList<String> user = new ArrayList<>(Arrays.asList("help","deposit","top up","withdrawal","purchase","transfer","collect","pay friend","wire","write check","create account","verify pin","logout","exit"));	 
	 ArrayList<String> bank_teller = new ArrayList<>(Arrays.asList("help","deposit","top up","withdrawal","purchase","transfer","collect","pay friend","wire","check transaction","monthly statement","list closed accounts","DTER","customer report","add interest","create account","delete accounts and customers","delete transactions","set date","add customer to account","logout","exit","list customers","list accounts","list transactions","list owns","list linked","list db","date","list customer accounts"));

	 public Inputs() {
		 state = noUser;
	 }

	 public void setState(String currstate) {
		    switch (currstate) {
		      case "noUser":
		    	  state = noUser;
		    	  break;
		      case "user":
		    	  state = user;
		    	  break;
		      case "bank teller":
		    	  state = bank_teller;
		    	  break;
		    }
	 }

	 public Boolean contains(String input) {
		    return state.contains(input);	  
	 }

	 public void print() {
		 System.out.println("The following inputs are available:");
		 for (String input : state) {
			 System.out.println("     "+input);
		 } 
	 }
}
