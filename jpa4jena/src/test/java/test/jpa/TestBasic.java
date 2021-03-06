package test.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import org.junit.Test;


import thewebsemantic.binding.Persistable;
import thewebsemantic.jpa.JBFactory;
import thewebsemantic.jpa.JBProvider;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TestBasic {

	@Test
	public void basic() throws IOException {     
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:test");
		EntityManager em = factory.createEntityManager();
		assertTrue(em.isOpen());
	}
	
	@Test
	public void missing() throws IOException {
		boolean caught = false;
		try {
			Persistence.createEntityManagerFactory("tws:bad");
		} catch (PersistenceException e) {
			caught = true;
		}
		assertTrue(caught);
	}

	@Test
	public void foundButNotModel() throws IOException {
		boolean caught = false;
		try {
			Persistence.createEntityManagerFactory("tws:notamodel");
		} catch (PersistenceException e) {
			caught = true;
		}
		assertTrue(caught);
	}
	
	@Test
	public void simple() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:test");
		EntityManager em = factory.createEntityManager();
		Yin yin = new Yin();
		yin.id = 0;
		em.persist(yin);		
		Yin yin2 = em.find(Yin.class, 0);
		assertNotNull(yin2);
	}
	
	@Test
	public void testFindAssembly() throws InstantiationException, IllegalAccessException, IOException {
		JBProvider p = JBProvider.class.newInstance();
		Model m = null;
		m  = p.findAssembly("bad");
		assertNotNull(m);
	}
	
	@Test
	public void testContainerUnsupported() throws InstantiationException, IllegalAccessException, IOException {
		JBProvider p = JBProvider.class.newInstance();
		boolean caught = false;
		try {
			p.createContainerEntityManagerFactory(null, null);
		} catch (UnsupportedOperationException e) {
			caught = true;
		}
		assertTrue(caught);
	}
	
	@Test
	public void testForceAssembler() {
		Model m = ModelFactory.createDefaultModel();
		URL uri = getClass().getResource("/testassembler.n3");
		m.read(uri.toString(), "N3");
		JBProvider p = new JBProvider(m);
		JBFactory f =  p.createEntityManagerFactory("tws:test", null);
		EntityManager em =  f.createEntityManager();
		Man jesse = em.find(Man.class, "http://semanticbible.org/ns/2006/NTNames#Jesse");
		assertNotNull(jesse);
		assertEquals("the father of King David", jesse.getDescription());

		Human human = em.find(Human.class, "http://semanticbible.org/ns/2006/NTNames#Jesse");
		assertNotNull(human);
		assertEquals("the father of King David", human.getDescription());

		boolean caught = false;
		try {
			em.find(Woman.class, "http://semanticbible.org/ns/2006/NTNames#Jesse");
		} catch (PersistenceException e) {
			caught = true;
		}
		assertTrue(caught);
		Query q = em.createNamedQuery("Woman.hasChildren");
		List<Woman> haveChildren = q.getResultList();
		assertNotNull(haveChildren);
		assertTrue(haveChildren.size() > 0);
		q = em.createNamedQuery("Woman.noChildren");
		List<Woman> withoutChildren = q.getResultList();
		assertNotNull(withoutChildren);
		assertTrue(withoutChildren.size() > 0);
		for (Woman woman2 : withoutChildren) {
			assertFalse(haveChildren.contains(woman2));
		}

		f = p.createEntityManagerFactory("tws:test2", null);
		em = f.createEntityManager();
		em.find(UnkownThing.class, "http://semanticbible.org/ns/2006/NTNames#Jesse");		
	}
	
	@Test
	public void testBad() {
		Model m = ModelFactory.createDefaultModel();
		URL uri = getClass().getResource("/testassembler.n3");
		m.read(uri.toString(), "N3");
		JBProvider p = new JBProvider(m);
		JBFactory f =  p.createEntityManagerFactory("tws:test", null);
		EntityManager em =  f.createEntityManager();
		Query q = em.createNamedQuery("Human.hasChildren");
		List<Human> people = q.getResultList();
		for (Human human : people) {
			assertTrue(human instanceof Persistable);
			Class clz = human.getClass().getSuperclass();
			assertTrue(clz == Man.class || clz == Woman.class); 
		}
	}
	
	@Test
	public void idURIasField() throws IOException {     
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();
		MusicGenre jazz = new MusicGenre();
		jazz.id = URI.create("http://example.org/genre/jazz");
		jazz.description = "Jazz Music";
		em.persist(jazz);
		Model m = (Model)em.getDelegate();
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
		Individual I = om.getIndividual("http://example.org/genre/jazz");
		assertNotNull(I);
		//m.write(System.out, "N3");
	}
	
	@Test
	public void idURIreadField() throws IOException {     
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();
		MusicGenre jazz = new MusicGenre();
		jazz.id = URI.create("http://example.org/genre/jazz");
		jazz.description = "Jazz Music";
		em.persist(jazz);
		jazz = em.find(MusicGenre.class, "http://example.org/genre/jazz");
		assertNotNull(jazz.id);
	}
	
	@Test
	public void idURIonMethod() throws IOException {     
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();
		MusicalInstrument trombone = new MusicalInstrument();
		trombone.setId(URI.create("http://example.org/instruments/trombone"));
		trombone.setDescription("a large brass instrument used in jazz, clasical, and broadway.");
		
		em.persist(trombone);
		Model m = (Model)em.getDelegate();
		//m.write(System.out, "N3");
		OntModel om = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM, m);
		Individual I = om.getIndividual("http://example.org/instruments/trombone");
		assertNotNull(I);
		
		trombone = em.find(MusicalInstrument.class, "http://example.org/instruments/trombone");
		assertNotNull(trombone.getId());
		assertEquals("http://example.org/instruments/trombone", trombone.getId().toString());
		assertNotNull(trombone.getDescription());		
	}
	
	@Test
	public void testSets() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();
		Group g = new Group("administrators");
		em.persist(g);
		g.getUsers().add(new User("tom"));
		em.persist(g);
		
		em = factory.createEntityManager();
		g = em.find(Group.class, "administrators");
		assertNotNull(g);
		assertEquals(g.getUsers().size(), 1);
		Model m = (Model)em.getDelegate();
		//m.write(System.out, "N3");
	}
	
	@Test
	public void testList() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();

		Hotel h = new Hotel();
		h.hotelid = 1234;
		em.persist(h);
		h.getAmmenities().add(Ammenity.POOL);
		em.persist(h);
		
		em = factory.createEntityManager();
		Hotel h1 = em.find(Hotel.class, 1234);
		assertNotNull(h1);
		assertEquals(1, h1.getAmmenities().size());
		Model m = (Model)em.getDelegate();
		//m.write(System.err, "N3");
	}
	
	@Test
	public void testGeneratedId() {
		EntityManagerFactory factory =  Persistence.createEntityManagerFactory("tws:blank");
		EntityManager em = factory.createEntityManager();		
	}
}
