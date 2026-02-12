package com.lab.demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import com.lab.entity.Product;
import com.lab.util.HibernateUtil;

import java.util.Arrays;
import java.util.List;

public class HQLDemo {

    public static void main(String[] args) {
        // Get Hibernate session
        SessionFactory factory = HibernateUtil.getSessionFactory();
        Session session = factory.openSession();

        try {
            // Load sample products
            loadSampleProducts(session);

            // Sort products
            sortProductsByPriceAscending(session);
            sortProductsByPriceDescending(session);
            sortProductsByQuantityDescending(session);

            // Pagination
            getFirstThreeProducts(session);
            getNextThreeProducts(session);

            // Aggregates
            countTotalProducts(session);
            countProductsInStock(session);
            countProductsByDescription(session);
            findMinMaxPrice(session);

            // Group by description
            groupProductsByDescription(session);
            groupProductsWithAggregation(session);

            // Filter by price
            filterProductsByPriceRange(session, 20.0, 100.0);

            // LIKE queries
            findProductsStartingWith(session, "D");
            findProductsEndingWith(session, "p");
            findProductsContaining(session, "Desk");
            findProductsByNameLength(session, 5);
            findProductsByExactLengthPattern(session, 7);

        } finally {
            session.close();
            factory.close();
        }
    }

    // =========================
    // Load sample products
    // =========================
    public static void loadSampleProducts(Session session) {
        Transaction tx = session.beginTransaction();

        List<Product> products = Arrays.asList(
            new Product("Laptop", 899.99, 10, "Electronics"),
            new Product("Mouse", 25.50, 50, "Electronics"),
            new Product("Monitor", 179.99, 15, "Electronics"),
            new Product("Desk Chair", 85.00, 5, "Furniture"),
            new Product("Desk Lamp", 45.75, 20, "Furniture"),
            new Product("Notebook", 5.99, 100, "Stationery"),
            new Product("Pen Set", 12.50, 75, "Stationery"),
            new Product("Keyboard", 49.99, 25, "Electronics")
        );

        for (Product p : products) {
            session.save(p);
        }

        tx.commit();
        System.out.println("Sample products loaded into database.");
    }

    // =========================
    // Sort Products
    // =========================
    public static void sortProductsByPriceAscending(Session session) {
        String hql = "FROM Product p ORDER BY p.price ASC";
        List<Product> products = session.createQuery(hql, Product.class).list();
        System.out.println("\nProducts by Price Ascending:");
        products.forEach(p -> System.out.println(p.getName() + " - $" + p.getPrice()));
    }

    public static void sortProductsByPriceDescending(Session session) {
        String hql = "FROM Product p ORDER BY p.price DESC";
        List<Product> products = session.createQuery(hql, Product.class).list();
        System.out.println("\nProducts by Price Descending:");
        products.forEach(p -> System.out.println(p.getName() + " - $" + p.getPrice()));
    }

    public static void sortProductsByQuantityDescending(Session session) {
        String hql = "FROM Product p ORDER BY p.quantity DESC";
        List<Product> products = session.createQuery(hql, Product.class).list();
        System.out.println("\nProducts by Quantity Descending:");
        products.forEach(p -> System.out.println(p.getName() + " - Qty: " + p.getQuantity()));
    }

    // =========================
    // Pagination
    // =========================
    public static void getFirstThreeProducts(Session session) {
        String hql = "FROM Product p";
        Query<Product> query = session.createQuery(hql, Product.class);
        query.setFirstResult(0);
        query.setMaxResults(3);
        List<Product> products = query.list();
        System.out.println("\nFirst 3 Products:");
        products.forEach(p -> System.out.println(p.getName()));
    }

    public static void getNextThreeProducts(Session session) {
        String hql = "FROM Product p";
        Query<Product> query = session.createQuery(hql, Product.class);
        query.setFirstResult(3);
        query.setMaxResults(3);
        List<Product> products = query.list();
        System.out.println("\nNext 3 Products:");
        products.forEach(p -> System.out.println(p.getName()));
    }

    // =========================
    // Aggregates
    // =========================
    public static void countTotalProducts(Session session) {
        Long count = session.createQuery("SELECT COUNT(p) FROM Product p", Long.class).uniqueResult();
        System.out.println("\nTotal Products: " + count);
    }

    public static void countProductsInStock(Session session) {
        Long count = session.createQuery("SELECT COUNT(p) FROM Product p WHERE p.quantity > 0", Long.class).uniqueResult();
        System.out.println("\nProducts in Stock: " + count);
    }

    public static void countProductsByDescription(Session session) {
        List<Object[]> results = session.createQuery("SELECT p.description, COUNT(p) FROM Product p GROUP BY p.description", Object[].class).list();
        System.out.println("\nProducts by Description:");
        for (Object[] row : results) {
            System.out.println(row[0] + " - " + row[1] + " products");
        }
    }

    public static void findMinMaxPrice(Session session) {
        Object[] result = session.createQuery("SELECT MIN(p.price), MAX(p.price) FROM Product p", Object[].class).uniqueResult();
        System.out.println("\nPrice Range:");
        System.out.println("Min: $" + result[0] + ", Max: $" + result[1]);
    }

    // =========================
    // Group by Description
    // =========================
    public static void groupProductsByDescription(Session session) {
        List<Object[]> results = session.createQuery("SELECT p.description, p.name, p.price FROM Product p ORDER BY p.description", Object[].class).list();
        System.out.println("\nProducts Grouped by Description:");
        String current = "";
        for (Object[] row : results) {
            String desc = (String) row[0];
            String name = (String) row[1];
            Double price = (Double) row[2];
            if (!desc.equals(current)) {
                System.out.println("\n" + desc + ":");
                current = desc;
            }
            System.out.println("  - " + name + " ($" + price + ")");
        }
    }

    public static void groupProductsWithAggregation(Session session) {
        List<Object[]> results = session.createQuery("SELECT p.description, COUNT(p), AVG(p.price), SUM(p.quantity) FROM Product p GROUP BY p.description", Object[].class).list();
        System.out.println("\nProduct Stats by Category:");
        for (Object[] row : results) {
            System.out.println(row[0] + ": Count=" + row[1] + ", Avg=$" + String.format("%.2f", row[2]) + ", TotalQty=" + row[3]);
        }
    }

    // =========================
    // Filter by Price
    // =========================
    public static void filterProductsByPriceRange(Session session, double min, double max) {
        Query<Product> query = session.createQuery("FROM Product p WHERE p.price BETWEEN :min AND :max", Product.class);
        query.setParameter("min", min);
        query.setParameter("max", max);
        List<Product> products = query.list();
        System.out.println("\nProducts Between $" + min + " and $" + max + ":");
        products.forEach(p -> System.out.println(p.getName() + " - $" + p.getPrice()));
    }

    // =========================
    // LIKE Queries
    // =========================
    public static void findProductsStartingWith(Session session, String prefix) {
        Query<Product> query = session.createQuery("FROM Product p WHERE p.name LIKE :pattern", Product.class);
        query.setParameter("pattern", prefix + "%");
        List<Product> products = query.list();
        System.out.println("\nProducts starting with '" + prefix + "':");
        products.forEach(p -> System.out.println(p.getName()));
    }

    public static void findProductsEndingWith(Session session, String suffix) {
        Query<Product> query = session.createQuery("FROM Product p WHERE p.name LIKE :pattern", Product.class);
        query.setParameter("pattern", "%" + suffix);
        List<Product> products = query.list();
        System.out.println("\nProducts ending with '" + suffix + "':");
        products.forEach(p -> System.out.println(p.getName()));
    }

    public static void findProductsContaining(Session session, String substring) {
        Query<Product> query = session.createQuery("FROM Product p WHERE p.name LIKE :pattern", Product.class);
        query.setParameter("pattern", "%" + substring + "%");
        List<Product> products = query.list();
        System.out.println("\nProducts containing '" + substring + "':");
        products.forEach(p -> System.out.println(p.getName()));
    }

    public static void findProductsByNameLength(Session session, int length) {
        Query<Product> query = session.createQuery("FROM Product p WHERE LENGTH(p.name) = :len", Product.class);
        query.setParameter("len", length);
        List<Product> products = query.list();
        System.out.println("\nProducts with Name Length " + length + ":");
        products.forEach(p -> System.out.println(p.getName()));
    }

    public static void findProductsByExactLengthPattern(Session session, int length) {
        StringBuilder pattern = new StringBuilder();
        for (int i = 0; i < length; i++) pattern.append("_");
        Query<Product> query = session.createQuery("FROM Product p WHERE p.name LIKE :pattern", Product.class);
        query.setParameter("pattern", pattern.toString());
        List<Product> products = query.list();
        System.out.println("\nProducts with exactly " + length + " characters:");
        products.forEach(p -> System.out.println(p.getName()));
    }
}