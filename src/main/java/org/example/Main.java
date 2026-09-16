package org.example;

import org.example.model.Author;
import org.example.model.Book;
import org.example.model.Publisher;
import org.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        kreirajEntitete();

        ispisiAutore();

        azurirajNaslovKnjige();

        obrisiKnjigu();

        HibernateUtil.shutdown();
    }

    // KREIRANJE ENTITETA

    public static void kreirajEntitete() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Dva autora
            Author author1 = new Author("Ivo Andrić");
            Author author2 = new Author("Miroslav Krleža");

            // Dvije knjige
            Book book1 = new Book("Na Drini ćuprija");
            Book book2 = new Book("Povratak Filipa Latinovicza");

            // Dva izdavača
            Publisher publisher1 = new Publisher("Školska knjiga");
            Publisher publisher2 = new Publisher("Mladost");

            // Povezivanje autora i knjiga
            author1.addBook(book1);
            author2.addBook(book2);

            // Povezivanje knjiga i izdavača
            book1.addPublisher(publisher1);
            book1.addPublisher(publisher2);
            book2.addPublisher(publisher1);

            // Spremanje
            session.persist(author1);
            session.persist(author2);
            session.persist(publisher1);
            session.persist(publisher2);
            transaction.commit();
            System.out.println("Entiteti su uspješno spremljeni.");
        }
    }

    // DOHVAĆANJE AUTORA I KNJIGA

    public static void ispisiAutore() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Author> authors = session.createQuery("SELECT DISTINCT a FROM Author a LEFT JOIN FETCH a.books", Author.class).getResultList();
            System.out.println();
            System.out.println("=== AUTORI I NJIHOVE KNJIGE ===");
            for (Author author : authors) {
                System.out.println("Autor: " + author.getName());
                for (Book book : author.getBooks()) {
                    System.out.println("  Knjiga: " + book.getTitle());
                }
                System.out.println();
            }
        }
    }

    // AŽURIRANJE NASLOVA KNJIGE

    public static void azurirajNaslovKnjige() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Book book = session.get(Book.class, 1L);

            if (book != null) {
                System.out.println("Stari naslov: " + book.getTitle());
                book.setTitle("Na Drini ćuprija - novo izdanje");
                session.merge(book);
                System.out.println("Novi naslov: " + book.getTitle());
            }

            transaction.commit();
        }
    }

    // BRISANJE KNJIGE

    public static void obrisiKnjigu() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            Book book = session.get(Book.class, 2L);

            if (book != null) {

                // Uklanjamo vezu s autorom
                if (book.getAuthor() != null) {
                    book.getAuthor().removeBook(book);
                }

                // Uklanjamo veze s publisherima
                for (Publisher publisher : book.getPublishers()) {
                    publisher.getBooks().remove(book);
                }
                session.remove(book);
                System.out.println("Knjiga je obrisana.");
            }

            transaction.commit();
        }
    }
}
