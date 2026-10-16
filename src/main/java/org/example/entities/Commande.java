package org.example.entities;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
public class Commande {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_commande")
    private LocalDate dateCommande;

    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommandeProduit> lignes = new ArrayList<>();

    // Constructeurs
    public Commande() {}

    public Commande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDateCommande() {
        return dateCommande;
    }

    public void setDateCommande(LocalDate dateCommande) {
        this.dateCommande = dateCommande;
    }

    public List<CommandeProduit> getLignes() {
        return lignes;
    }

    public void setLignes(List<CommandeProduit> lignes) {
        this.lignes = lignes;
    }

    // Helper
    public void addProduit(Produit p, int quantite) {
        CommandeProduit cp = new CommandeProduit(this, p, quantite);
        lignes.add(cp);
    }

    @Override
    public String toString() {
        return "Commande{id=" + id + ", dateCommande=" + dateCommande + ", nbLignes=" + lignes.size() + "}";
    }
}
