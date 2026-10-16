package org.example;

import org.example.dao.GenericDAO;
import org.example.entities.*;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   DEMONSTRATION : REVISION GESTION COMMERCIALE AVEC HIBERNATE & JPA");
        System.out.println("================================================================================");

        // =====================================================================
        // 1. ETAPE 4 & 6 : Insertion et gestion des Categories via DAO et Service
        // =====================================================================
        System.out.println("\n[ETAPE 4 & 6] Creation des categories via GenericDAO et CrudService...");
        CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);

        Categorie catElectronique = new Categorie("Electronique");
        Categorie catInformatique = new Categorie("Informatique");
        Categorie catBureau = new Categorie("Fournitures de Bureau");

        catService.create(catElectronique);
        catService.create(catInformatique);
        catService.create(catBureau);

        System.out.println("-> Categorie creee : " + catElectronique);
        System.out.println("-> Categorie creee : " + catInformatique);
        System.out.println("-> Categorie creee : " + catBureau);

        // =====================================================================
        // 2. ETAPE 7 : Creation des Produits et relation ManyToOne / OneToMany
        // =====================================================================
        System.out.println("\n[ETAPE 7] Creation des produits avec liaison ManyToOne / OneToMany...");
        ProduitService produitService = new ProduitService();

        Produit p1 = new Produit("Ordinateur Portable Dell", new BigDecimal("899.99"), catInformatique);
        Produit p2 = new Produit("Souris Sans Fil Logitech", new BigDecimal("29.99"), catInformatique);
        Produit p3 = new Produit("Smartphone Samsung Galaxy", new BigDecimal("649.50"), catElectronique);
        Produit p4 = new Produit("Ecran 27 pouces 4K", new BigDecimal("349.00"), catElectronique);
        Produit p5 = new Produit("Chaise Ergonomique", new BigDecimal("199.99"), catBureau);

        produitService.create(p1);
        produitService.create(p2);
        produitService.create(p3);
        produitService.create(p4);
        produitService.create(p5);

        System.out.println("-> Produit 1 : " + p1);
        System.out.println("-> Produit 2 : " + p2);
        System.out.println("-> Produit 3 : " + p3);
        System.out.println("-> Produit 4 : " + p4);
        System.out.println("-> Produit 5 : " + p5);

        // =====================================================================
        // 3. ETAPE 8 : Requetes avancees (Named Queries & Criteria API)
        // =====================================================================
        System.out.println("\n[ETAPE 8] Execution des Named Queries & Criteria API...");

        System.out.println("\n--- [NamedQuery] Produit.findByCategorie (Informatique) ---");
        List<Produit> prodsInfo = produitService.findByCategorie(catInformatique.getId());
        for (Produit p : prodsInfo) {
            System.out.println("  * " + p.getLibelle() + " | Prix: " + p.getPrix() + " EUR");
        }

        System.out.println("\n--- [Criteria API] Recherche dynamique (term='Ecran', minPrix=200, maxPrix=500) ---");
        List<Produit> resultatsCrit = produitService.search("Ecran", new BigDecimal("200.00"), new BigDecimal("500.00"));
        for (Produit p : resultatsCrit) {
            System.out.println("  * Trouve : " + p.getLibelle() + " (" + p.getPrix() + " EUR)");
        }

        // =====================================================================
        // 4. ETAPE 9 : Pagination et Heritage JPA (Strategie JOINED)
        // =====================================================================
        System.out.println("\n[ETAPE 9] Pagination JPQL et Heritage JOINED (Users, Clients, Fournisseurs)...");

        System.out.println("--- Pagination (Page 1, Taille 2) ---");
        List<Produit> page1 = produitService.findPage(1, 2);
        for (Produit p : page1) {
            System.out.println("  [Page 1] " + p.getLibelle());
        }

        System.out.println("--- Pagination (Page 2, Taille 2) ---");
        List<Produit> page2 = produitService.findPage(2, 2);
        for (Produit p : page2) {
            System.out.println("  [Page 2] " + p.getLibelle());
        }

        System.out.println("\n--- Heritage JOINED : Enregistrement de Clients et Fournisseurs ---");
        CrudService<User, Long> userService = new CrudService<>(User.class);
        Client client1 = new Client("Youness Bakka", LocalDate.of(2024, 1, 15));
        Client client2 = new Client("Amine Alami", LocalDate.of(2024, 3, 20));
        Fournisseur fournisseur1 = new Fournisseur("Tech Distributor SA", "Casablanca, Maroc");

        userService.create(client1);
        userService.create(client2);
        userService.create(fournisseur1);

        List<User> users = userService.getAll();
        for (User u : users) {
            System.out.println("  * Polymorphisme User: " + u);
        }

        // =====================================================================
        // 5. ETAPE 10 : Relations N:N avec CommandeProduit (Cle composee)
        // =====================================================================
        System.out.println("\n[ETAPE 10] Creation de Commandes avec relation N:N (Cle composee CommandeProduitPK)...");
        EntityManager em = GenericDAO.getEmf().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        Produit managedP1 = em.find(Produit.class, p1.getId());
        Produit managedP2 = em.find(Produit.class, p2.getId());

        Commande cmd1 = new Commande(LocalDate.now());
        em.persist(cmd1);

        cmd1.addProduit(managedP1, 2); // 2x Dell Laptops
        cmd1.addProduit(managedP2, 5); // 5x Souris
        for (CommandeProduit cp : cmd1.getLignes()) {
            em.persist(cp);
        }
        tx.commit();
        em.close();

        System.out.println("-> Commande persistee avec succes (ID=" + cmd1.getId() + ") :");
        for (CommandeProduit cp : cmd1.getLignes()) {
            System.out.println("   - Produit: " + cp.getProduit().getLibelle() + " | Quantite: " + cp.getQuantite());
        }

        // =====================================================================
        // 6. ETAPE 11 : Gestion des Transactions et Contexte de Persistance
        // =====================================================================
        System.out.println("\n[ETAPE 11] Test de Rollback Transactionnel sur regle metier...");
        EntityManager emTx = GenericDAO.getEmf().createEntityManager();
        EntityTransaction txEx = emTx.getTransaction();
        try {
            txEx.begin();
            Produit produitInvalide = new Produit("Produit Erreur", new BigDecimal("-10.00"), catBureau);
            emTx.persist(produitInvalide);

            if (produitInvalide.getPrix().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Validation rejetee : Le prix ne peut pas etre negatif !");
            }
            txEx.commit();
        } catch (Exception e) {
            if (txEx.isActive()) {
                txEx.rollback();
            }
            System.out.println("  [Succes du Rollback] Transaction annulee conformement aux regles : " + e.getMessage());
        } finally {
            emTx.close();
        }

        // =====================================================================
        // 7. ETAPE 12 : Exercice de synthese - Rapport de ventes par categorie
        // =====================================================================
        System.out.println("\n[ETAPE 12] Exercice de synthese : Calcul du CA par Categorie via Criteria API (sum/prod)...");
        RapportService rapportService = new RapportService();
        Map<String, BigDecimal> rapportCA = rapportService.getChiffreAffairesParCategorie();

        for (Map.Entry<String, BigDecimal> entry : rapportCA.entrySet()) {
            System.out.println("  ==> Categorie: " + entry.getKey() + " | Chiffre d'Affaires Total: " + entry.getValue() + " EUR");
        }

        System.out.println("\n================================================================================");
        System.out.println("   EXECUTION DU TP TERMINEE AVEC SUCCES - TOUTES LES ETAPES VALIDEES !");
        System.out.println("================================================================================");

        GenericDAO.getEmf().close();
    }
}
