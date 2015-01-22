/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
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
public class AccountJpaController implements Serializable {

    public AccountJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Account account) throws PreexistingEntityException, Exception {
        if (account.getTransactionList() == null) {
            account.setTransactionList(new ArrayList<Transaction>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Client clientid = account.getClientid();
            if (clientid != null) {
                clientid = em.getReference(clientid.getClass(), clientid.getClientid());
                account.setClientid(clientid);
            }
            Agency agencyid = account.getAgencyid();
            if (agencyid != null) {
                agencyid = em.getReference(agencyid.getClass(), agencyid.getAgencyid());
                account.setAgencyid(agencyid);
            }
            List<Transaction> attachedTransactionList = new ArrayList<Transaction>();
            for (Transaction transactionListTransactionToAttach : account.getTransactionList()) {
                transactionListTransactionToAttach = em.getReference(transactionListTransactionToAttach.getClass(), transactionListTransactionToAttach.getTransactionid());
                attachedTransactionList.add(transactionListTransactionToAttach);
            }
            account.setTransactionList(attachedTransactionList);
            em.persist(account);
            if (clientid != null) {
                clientid.getAccountList().add(account);
                clientid = em.merge(clientid);
            }
            if (agencyid != null) {
                agencyid.getAccountList().add(account);
                agencyid = em.merge(agencyid);
            }
            for (Transaction transactionListTransaction : account.getTransactionList()) {
                Account oldAccountnumOfTransactionListTransaction = transactionListTransaction.getAccountnum();
                transactionListTransaction.setAccountnum(account);
                transactionListTransaction = em.merge(transactionListTransaction);
                if (oldAccountnumOfTransactionListTransaction != null) {
                    oldAccountnumOfTransactionListTransaction.getTransactionList().remove(transactionListTransaction);
                    oldAccountnumOfTransactionListTransaction = em.merge(oldAccountnumOfTransactionListTransaction);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findAccount(account.getAccountnum()) != null) {
                throw new PreexistingEntityException("Account " + account + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Account account) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Account persistentAccount = em.find(Account.class, account.getAccountnum());
            Client clientidOld = persistentAccount.getClientid();
            Client clientidNew = account.getClientid();
            Agency agencyidOld = persistentAccount.getAgencyid();
            Agency agencyidNew = account.getAgencyid();
            List<Transaction> transactionListOld = persistentAccount.getTransactionList();
            List<Transaction> transactionListNew = account.getTransactionList();
            List<String> illegalOrphanMessages = null;
            for (Transaction transactionListOldTransaction : transactionListOld) {
                if (!transactionListNew.contains(transactionListOldTransaction)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Transaction " + transactionListOldTransaction + " since its accountnum field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (clientidNew != null) {
                clientidNew = em.getReference(clientidNew.getClass(), clientidNew.getClientid());
                account.setClientid(clientidNew);
            }
            if (agencyidNew != null) {
                agencyidNew = em.getReference(agencyidNew.getClass(), agencyidNew.getAgencyid());
                account.setAgencyid(agencyidNew);
            }
            List<Transaction> attachedTransactionListNew = new ArrayList<Transaction>();
            for (Transaction transactionListNewTransactionToAttach : transactionListNew) {
                transactionListNewTransactionToAttach = em.getReference(transactionListNewTransactionToAttach.getClass(), transactionListNewTransactionToAttach.getTransactionid());
                attachedTransactionListNew.add(transactionListNewTransactionToAttach);
            }
            transactionListNew = attachedTransactionListNew;
            account.setTransactionList(transactionListNew);
            account = em.merge(account);
            if (clientidOld != null && !clientidOld.equals(clientidNew)) {
                clientidOld.getAccountList().remove(account);
                clientidOld = em.merge(clientidOld);
            }
            if (clientidNew != null && !clientidNew.equals(clientidOld)) {
                clientidNew.getAccountList().add(account);
                clientidNew = em.merge(clientidNew);
            }
            if (agencyidOld != null && !agencyidOld.equals(agencyidNew)) {
                agencyidOld.getAccountList().remove(account);
                agencyidOld = em.merge(agencyidOld);
            }
            if (agencyidNew != null && !agencyidNew.equals(agencyidOld)) {
                agencyidNew.getAccountList().add(account);
                agencyidNew = em.merge(agencyidNew);
            }
            for (Transaction transactionListNewTransaction : transactionListNew) {
                if (!transactionListOld.contains(transactionListNewTransaction)) {
                    Account oldAccountnumOfTransactionListNewTransaction = transactionListNewTransaction.getAccountnum();
                    transactionListNewTransaction.setAccountnum(account);
                    transactionListNewTransaction = em.merge(transactionListNewTransaction);
                    if (oldAccountnumOfTransactionListNewTransaction != null && !oldAccountnumOfTransactionListNewTransaction.equals(account)) {
                        oldAccountnumOfTransactionListNewTransaction.getTransactionList().remove(transactionListNewTransaction);
                        oldAccountnumOfTransactionListNewTransaction = em.merge(oldAccountnumOfTransactionListNewTransaction);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                BigDecimal id = account.getAccountnum();
                if (findAccount(id) == null) {
                    throw new NonexistentEntityException("The account with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(BigDecimal id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Account account;
            try {
                account = em.getReference(Account.class, id);
                account.getAccountnum();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The account with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Transaction> transactionListOrphanCheck = account.getTransactionList();
            for (Transaction transactionListOrphanCheckTransaction : transactionListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Account (" + account + ") cannot be destroyed since the Transaction " + transactionListOrphanCheckTransaction + " in its transactionList field has a non-nullable accountnum field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Client clientid = account.getClientid();
            if (clientid != null) {
                clientid.getAccountList().remove(account);
                clientid = em.merge(clientid);
            }
            Agency agencyid = account.getAgencyid();
            if (agencyid != null) {
                agencyid.getAccountList().remove(account);
                agencyid = em.merge(agencyid);
            }
            em.remove(account);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Account> findAccountEntities() {
        return findAccountEntities(true, -1, -1);
    }

    public List<Account> findAccountEntities(int maxResults, int firstResult) {
        return findAccountEntities(false, maxResults, firstResult);
    }

    private List<Account> findAccountEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Account.class));
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

    public Account findAccount(BigDecimal id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Account.class, id);
        } finally {
            em.close();
        }
    }

    public int getAccountCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Account> rt = cq.from(Account.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
