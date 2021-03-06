package test.jpa;

import java.net.URI;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.junit.Test;

import thewebsemantic.jpa.JBFactory;
import static junit.framework.Assert.*;


public class TestTransactions {
	
	@Test
	public void basic() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		em.getTransaction().begin();
		Man m = new Man(URI.create("http://example.org/joe"));
		m.setName("Joseph");
		m.setDescription("had a nice coat.");
		em.persist(m);
		em.getTransaction().commit();
		
		Man m2 = em.find(Man.class, "http://example.org/joe");
		assertEquals("Joseph", m2.getName());
		JBFactory jbfactory = (JBFactory)factory;
		//jbfactory.getModel().write(System.out, "N3");
		
	}

	@Test
	public void rollback() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		EntityTransaction ta = em.getTransaction();
		ta.begin();
		Man m = new Man(URI.create("http://example.org/mark"));
		m.setName("Mark");
		m.setDescription("A disciple of Jesus.");
		em.persist(m);
		ta.rollback();
		
		Man m2 = em.find(Man.class, "http://example.org/mark");
		assertNull(m2);
		
		boolean caught = false;
		try {
			m2 = em.getReference(Man.class, "http://example.org/mark");
		} catch (EntityNotFoundException e) {
			caught = true;
		}
		assertTrue(caught);		
	}
	
	@Test
	public void illegalStateBegin() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		EntityTransaction ta = em.getTransaction();
		ta.begin();
		
		try {
			ta.begin();
		} catch (IllegalStateException e) {
			return;
		} finally {
			ta.rollback();
		}
		fail();
	}
	
	@Test
	public void  illegalStateClommit1() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		EntityTransaction ta = em.getTransaction();
		ta.begin();
		ta.commit();
		try {
			ta.commit();
		} catch (IllegalStateException e) {
			return;
		}
		fail();
	}
	
	@Test
	public void illegalStateClommit2() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		EntityTransaction ta = em.getTransaction();
		try {
			ta.commit();
		} catch (IllegalStateException e) {
			return;
		}
		fail();
	}
	
	@Test
	public void illegalStateRollback() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:filemodel");
		EntityManager em = factory.createEntityManager();
		EntityTransaction ta = em.getTransaction();
		try {
			ta.rollback();
		} catch (IllegalStateException e) {
			return;
		}
		fail();
	}
}
