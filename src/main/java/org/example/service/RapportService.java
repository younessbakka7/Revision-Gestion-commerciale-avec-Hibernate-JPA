package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Categorie;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RapportService {

    public Map<String, BigDecimal> getChiffreAffairesParCategorie() {
        EntityManager em = GenericDAO.getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
            Root<CommandeProduit> cp = cq.from(CommandeProduit.class);

            Join<CommandeProduit, Produit> produitJoin = cp.join("produit");
            Join<Produit, Categorie> categorieJoin = produitJoin.join("categorie");

            // Calcul du chiffre d'affaires: prix * quantite
            Expression<BigDecimal> caLigne = cb.prod(produitJoin.get("prix"), cp.get("quantite"));
            Expression<BigDecimal> sumCA = cb.sum(caLigne);

            cq.multiselect(categorieJoin.get("nom"), sumCA);
            cq.groupBy(categorieJoin.get("nom"));

            List<Object[]> results = em.createQuery(cq).getResultList();
            Map<String, BigDecimal> rapport = new LinkedHashMap<>();

            for (Object[] row : results) {
                String catNom = (String) row[0];
                BigDecimal total = (BigDecimal) row[1];
                rapport.put(catNom, total != null ? total : BigDecimal.ZERO);
            }

            return rapport;
        } finally {
            em.close();
        }
    }
}
