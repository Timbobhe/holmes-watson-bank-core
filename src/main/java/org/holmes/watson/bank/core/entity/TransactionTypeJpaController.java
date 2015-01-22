/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core.entity;

import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import org.holmes.watson.bank.core.entity.exceptions.IllegalOrphanException;
import org.holmes.watson.bank.core.entity.exceptions.NonexistentEntityException;
import org.holmes.watson.bank.core.entity.exceptions.PreexistingEntityException;

/**
 *
 * @author Olayinka
 */
public class TransactionTypeJpaController implements Serializable {

    public TransactionTypeJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TransactionType transactionType) throws PreexistingEntityException, Exception {
        if (transactionType.getTransactionList() == null) {
            transactionType.setTransactionList(new ArrayList<Transaction>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Transaction> attachedTransactionList = new ArrayList<Transaction>();
            for (Transaction transactionListTransactionToAttach : transactionType.getTransactionList()) {
                transactionListTransactionToAttach = em.getReference(transactionListTransactionToAttach.getClass(), transactionListTransactionToAttach.getTransactionid());
                attachedTransactionList.add(transactionListTransactionToAttach);
            }
            transactionType.setTransactionList(attachedTransactionList);
            em.persist(transactionType);
            for (Transaction transactionListTransaction : transactionType.getTransactionList()) {
                TransactionType oldTransactiontypeOfTransactionListTransaction = transactionListTransaction.getTransactiontype();
                transactionListTransaction.setTransactiontype(transactionType);
                transactionListTransaction = em.merge(transactionListTransaction);
                if (oldTransactiontypeOfTransactionListTransaction != null) {
                    oldTransactiontypeOfTransactionListTransaction.getTransactionList().remove(transactionListTransaction);
                    oldTransactiontypeOfTransactionListTransaction = em.merge(oldTransactiontypeOfTransactionListTransaction);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findTransactionType(transactionType.getTransactiontype()) != null) {
                throw new PreexistingEntityException("TransactionType " + transactionType + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TransactionType transactionType) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TransactionType persistentTransactionType = em.find(TransactionType.class, transactionType.getTransactiontype());
            List<Transaction> transactionListOld = persistentTransactionType.getTransactionList();
            List<Transaction> transactionListNew = transactionType.getTransactionList();
            List<String> illegalOrphanMessages = null;
            for (Transaction transactionListOldTransaction : transactionListOld) {
                if (!transactionListNew.contains(transactionListOldTransaction)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transaction " + transactionListOldTransaction + " since its transactiontype field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Transaction> attachedTransactionListNew = new ArrayList<Transaction>();
            for (Transaction transactionListNewTransactionToAttach : transactionListNew) {
                transactionListNewTransactionToAttach = em.getReference(transactionListNewTransactionToAttach.getClass(), transactionListNewTransactionToAttach.getTransactionid());
                attachedTransactionListNew.add(transactionListNewTransactionToAttach);
            }
            transactionListNew = attachedTransactionListNew;
            transactionType.setTransactionList(transactionListNew);
            transactionType = em.merge(transactionType);
            for (Transaction transactionListNewTransaction : transactionListNew) {
                if (!transactionListOld.contains(transactionListNewTransaction)) {
                    TransactionType oldTransactiontypeOfTransactionListNewTransaction = transactionListNewTransaction.getTransactiontype();
                    transactionListNewTransaction.setTransactiontype(transactionType);
                    transactionListNewTransaction = em.merge(transactionListNewTransaction);
                    if (oldTransactiontypeOfTransactionListNewTransaction != null && !oldTransactiontypeOfTransactionListNewTransaction.equals(transactionType)) {
                        oldTransactiontypeOfTransactionListNewTransaction.getTransactionList().remove(transactionListNewTransaction);
                        oldTransactiontypeOfTransactionListNewTransaction = em.merge(oldTransactiontypeOfTransactionListNewTransaction);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = transactionType.getTransactiontype();
                if (findTransactionType(id) == null) {
                    throw new NonexistentEntityException("The transactionType with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TransactionType transactionType;
            try {
                transactionType = em.getReference(TransactionType.class, id);
                transactionType.getTransactiontype();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The transactionType with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Transaction> transactionListOrphanCheck = transactionType.getTransactionList();
            for (Transaction transactionListOrphanCheckTransaction : transactionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This TransactionType (" + transactionType + ") cannot be destroyed since the Transaction " + transactionListOrphanCheckTransaction + " in its transactionList field has a non-nullable transactiontype field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(transactionType);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TransactionType> findTransactionTypeEntities() {
        return findTransactionTypeEntities(true, -1, -1);
    }

    public List<TransactionType> findTransactionTypeEntities(int maxResults, int firstResult) {
        return findTransactionTypeEntities(false, maxResults, firstResult);
    }

    private List<TransactionType> findTransactionTypeEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TransactionType.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public TransactionType findTransactionType(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TransactionType.class, id);
        } finally {
            em.close();
        }
    }

    public int getTransactionTypeCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TransactionType> rt = cq.from(TransactionType.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
