package thewebsemantic.jpa;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import com.hp.hpl.jena.graph.TransactionHandler;

public class JBEntityTransaction implements EntityTransaction {

	private TransactionHandler ta;
	private boolean isActive = false;
	private boolean isRollBackOnly = false;
	
	public JBEntityTransaction(TransactionHandler ta) {
		this.ta = ta;
	}
	
	
	public void begin() {
		if ( isActive)
			throw new IllegalStateException("Transaction is already active.  Nested transactions are not supported.");
		ta.begin();
		isActive = true;
	}

	
	public void commit() {
		if (! isActive)
			throw new IllegalStateException("transaction is not active");

		try {
			ta.commit();
			isActive = false;
		} catch (Exception e) {
			throw new RollbackException(e);
		}
	}

	
	public boolean getRollbackOnly() {
		if (! isActive)
			throw new IllegalStateException("transaction is not active");
		return isRollBackOnly;
	}

	
	public boolean isActive() {
		return isActive;
	}

	
	public void rollback() {
		if (! isActive)
			throw new IllegalStateException("transaction is not active");
		ta.abort();
		isActive = false;

	}

	
	public void setRollbackOnly() {
		isRollBackOnly = true;
	}

}