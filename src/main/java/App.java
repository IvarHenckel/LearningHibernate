import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.NaturalIdCacheStatistics;
import org.hibernate.stat.QueryStatistics;
import org.hibernate.stat.Statistics;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class App {
    public static void main(String[] args) {
        //insertAndSelectAlien();
        //laptopStudentRelations();
        jpaInsteadOfHibernate();
    }

    private static void insertAndSelectAlien() {
        AlienName an = new AlienName();
        an.setFname("Burkin");
        an.setMname("Muhammed");
        an.setLname("Gunke");
        Alien telusko = new Alien();
        telusko.setAid(103);
        telusko.setAname(an);
        telusko.setColor("Brown");

        Configuration con = new Configuration().configure().addAnnotatedClass(Alien.class);
        ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(con.getProperties()).buildServiceRegistry();
        SessionFactory sf = con.buildSessionFactory(reg);

        Session session = sf.openSession();

        Transaction tx = session.beginTransaction();

        session.save(telusko); // -> INSERT in sql

        tx.commit();

        Alien select_alien = (Alien) session.get(Alien.class, 103);// SELECT in SQL
        System.out.println(select_alien);
        Statistics statistics = session.getSessionFactory().getStatistics();
        System.out.println(statistics.isStatisticsEnabled());
        System.out.println(statistics.getQueryExecutionCount());
        statistics.logSummary();
        session.close();
    }

    private static void laptopStudentRelations() {
        Laptop laptop = new Laptop();
        laptop.setLid(101);
        laptop.setLname("Dell");
        Student s = new Student();
        s.setName("Kalle");
        s.setRollno(1);
        s.setMarks(50);
        List<Laptop> l = new ArrayList<>();
        l.add(laptop);
        s.setLaptop(l);
        List<Student> sList = new ArrayList<>();
        sList.add(s);
        laptop.setStudent(sList);

        Configuration con = new Configuration().configure().addAnnotatedClass(Student.class).addAnnotatedClass(Laptop.class);
        ServiceRegistry reg = new ServiceRegistryBuilder().applySettings(con.getProperties()).buildServiceRegistry();
        SessionFactory sf = con.buildSessionFactory(reg);

        Session session = sf.openSession();

        Transaction tx = session.beginTransaction();

        session.save(laptop);
        session.save(s);
        tx.commit();
    }

    private static void jpaInsteadOfHibernate() {
        AlienName an = new AlienName();
        an.setFname("Jesper");
        an.setMname("Cykelbanebyggarn");
        an.setLname("Karlsson");
        Alien a1 = new Alien();
        a1.setColor("Brown");
        a1.setAid(2);
        a1.setAname(an);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pu");
        EntityManager em = emf.createEntityManager();
        Session session2 = em.unwrap(Session.class);
        Statistics statistics2 = session2.getSessionFactory().getStatistics();

        //This is for statistics
//        Session session = em.unwrap(Session.class);
//        Statistics statistics = session.getSessionFactory().getStatistics();
//        System.out.print("Is statistics enabled? "); System.out.println(statistics.isStatisticsEnabled());
//        System.out.println("Here's one log summary");
//        statistics.logSummary();


        Alien a = em.find(Alien.class, 103);

        Query query = em.createQuery("From Alien");
        List results = query.getResultList(); // Yes -> detta leder till att getQueryExecutionCount ??kas med 1
        em.getTransaction().begin();
        em.persist(a1);
        em.getTransaction().commit();

        System.out.println(a);

        //System.out.println("Here's another log summary");
        //statistics2.logSummary();
        System.out.println("Why is this 0? ");
        System.out.println(statistics2.getQueryExecutionCount());
        System.out.println(statistics2.getPrepareStatementCount()); // Denna ger 1 dock. Queries kanske ??r mer native Queries eller HQL??

        //Hade ju varit intressant att f?? tid per query faktiskt och antalet queries.
        System.out.print("Is statistics enabled? "); System.out.println(statistics2.isStatisticsEnabled());
        statistics2.logSummary();
        System.out.println(statistics2.getClass().getSimpleName());
        System.out.println(statistics2);

        String[] entities = statistics2.getEntityNames();
        for (String e: entities) {
            //NaturalIdStatistics n;
            NaturalIdCacheStatistics naturalIdStatistics =	statistics2.getNaturalIdCacheStatistics(e);
            if (naturalIdStatistics == null) {
                System.out.println("There were no info for entity (I guess means no query?) " + e);
            } else {
                // Obs NaturalIdCacheStatistics ??r deprecated man ska ist??llet anv??nda NaturalIdStatistics men tror jag har f??r gammal versoin
                // Obs det framg??r av namnet att detta skulle vara cache men som jag tolkar det av docs s??
                // inneh??ll NaturalIdCacheStatistics info f??r b??de cacheinfo och icke cacheinfo, man har numera brytit upp det i tv?? klasser.
                System.out.println("Entity is " + e + " mean time for querying is " + naturalIdStatistics.getExecutionAvgTime());
            }
        }

        String[] queries = statistics2.getQueries();
        for (String q: queries) {
            QueryStatistics qs = statistics2.getQueryStatistics(q);
            if (qs == null) {
                System.out.println("There were no info for query (I guess means no query?) " + qs);
            } else {
                System.out.println("Query is " + q + ", mean time for querying is " + qs.getExecutionAvgTime());
            }
        }
        // Det hade varit j??vligt intressant att bara k??ra detta f??r att se vad som tar tid. Har vi tur s?? funkar ??ven NaturalIdStatistics p?? Nasdaq!
        // Om det som inte r??knas som queries bara ??r enstaka .get(id) s?? kan man v??l anta bara att de inte ??r dem som tar tiden.
        // Man kan ocks?? f?? ut antal queries med EntityStatistics (summa ihop) och p?? s??  sett kolla hur stor andel som ??r queries.
        // Om vi har en stor andel queries kan vi bortse fr??n det andra, iaf tills vidare.

        System.out.println(statistics2.getNaturalIdQueryExecutionCount());
        System.out.println(statistics2.getQueryExecutionCount());
        em.close();
    }
}
