import java.util.Comparator;
import java.sql.Date;

public class Transaction {

	String tid;
	String t_type;
	double amount;
	int account_from;
	int account_to;
	String checkno;
	String branch;
	Date transDate;
	
	// default constructor
	
	public Transaction(){	
		transDate = null;
		tid = "";
		t_type = "";
		amount = 0;
		account_from = 0;
		account_to = 0;
		checkno = "";
		branch = "";
	  }
	  
	  
	public Transaction(String tids, String t_types, double amounts, int account_froms, int account_tos, String checknos, String branchs, Date transDates){
		transDate = transDates;
		tid = tids;
		t_type = t_types;
		amount = amounts;
		account_from = account_froms;
		account_to = account_tos;
		checkno = checknos;
		branch = branchs;
	}	  
	  
			  // Getters
	  public String gettid(){
		  return tid;
	  }
			  
	  public Date gettransDate(){		
		  return transDate;			
	  }
		
	  public String gett_type(){		
		  return t_type;			
	  }
		
	  public double getamount(){		
		  return amount;			
	  }
		
	  public int getaccount_from(){		
		  return account_from;			
	  }
		
	  public int getaccount_to(){		
		  return account_to;			
	  }
		
	  public String getcheckno(){		
		  return checkno;			
	  }
		
	  public String getbranch(){		
		  return branch;			
	  }  
		
	  // Setters
		
	  public void settid(String value){		
		  tid = value;			
	  }
		
	  public void settransDate(Date value){		
		   transDate = value;			
	  }
		
	  public void sett_type(String value){		
		   t_type = value;			
	  }
		
	  public void setamount(double value){		
		   amount = value;			
	  }
		
	  public void setaccount_from(int value){		
		   account_from = value;			
	  }
		
	  public void setaccount_to(int value){		
		   account_to = value;			
	  }
		
	  public void setcheckno(String value){		
		   checkno = value;
	  }
		
	  public void setbranch(String value){		
		   branch = value;
	  }		
}



//comparator to sort by transDate

class SortByDate implements Comparator<Transaction>{
	public int compare(Transaction a, Transaction b){
		return a.gettransDate().compareTo(b.gettransDate());		
	}
}