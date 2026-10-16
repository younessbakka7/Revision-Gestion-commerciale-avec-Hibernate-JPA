package org.example.service;

import org.example.dao.GenericDAO;
import org.example.entities.Produit;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProduitService extends CrudService<Produit, Long> {

    public ProduitService() {
        super(Produit.class);
    }

    public List<Produit> findByCategorie(Long catId) {
        EntityManager em = GenericDAO.getEmf().createEntityManager();
        try {
            return em.createNamedQuery("Produit.findByCategorie", Produit.class)
                     .setParameter("catId", catId)
                     .getResultList();
        } finally {
            em.close();
        }
    }

    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        EntityManager em = GenericDAO.getEmf().createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);
            List<Predicate> predicates = new ArrayList<>();

            if (term != null && !term.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.toLowerCase() + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.ge(root.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.le(root.get("prix"), maxPrix));
            }

            cq.where(predicates.toArray(new Predicate[0]));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    public List<Produit> findPage(int pageNumber, int pageSize) {
        EntityManager em = GenericDAO.getEmf().createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Produit p ORDER BY p.id", Produit.class)
                     .setFirstResult((pageNumber - 1) * pageSize)
                     .setMaxResults(pageSize)
                     .getResultList();
        } finally {
            em.close();
        }
    }
}
