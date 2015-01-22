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
public class AgencyJpaController implements Serializable {

    public AgencyJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Agency agency) throws PreexistingEntityException, Exception {
        if (agency.getAccountList() == null) {
            agency.setAccountList(new ArrayList<Account>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Account> attachedAccountList = new ArrayList<Account>();
            for (Account accountListAccountToAttach : agency.getAccountList()) {
                accountListAccountToAttach = em.getReference(accountListAccountToAttach.getClass(), accountListAccountToAttach.getAccountnum());
                attachedAccountList.add(accountListAccountToAttach);
            }
            agency.setAccountList(attachedAccountList);
            em.persist(agency);
            for (Account accountListAccount : agency.getAccountList()) {
                Agency oldAgencyidOfAccountListAccount = accountListAccount.getAgencyid();
                accountListAccount.setAgencyid(agency);
                accountListAccount = em.merge(accountListAccount);
                if (oldAgencyidOfAccountListAccount != null) {
                    oldAgencyidOfAccountListAccount.getAccountList().remove(accountListAccount);
                    oldAgencyidOfAccountListAccount = em.merge(oldAgencyidOfAccountListAccount);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findAgency(agency.getAgencyid()) != null) {
                throw new PreexistingEntityException("Agency " + agency + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Agency agency) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Agency persistentAgency = em.find(Agency.class, agency.getAgencyid());
            List<Account> accountListOld = persistentAgency.getAccountList();
            List<Account> accountListNew = agency.getAccountList();
            List<String> illegalOrphanMessages = null;
            for (Account accountListOldAccount : accountListOld) {
                if (!accountListNew.contains(accountListOldAccount)) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("You must retain Account " + accountListOldAccount + " since its agencyid field is not nullable.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            List<Account> attachedAccountListNew = new ArrayList<Account>();
            for (Account accountListNewAccountToAttach : accountListNew) {
                accountListNewAccountToAttach = em.getReference(accountListNewAccountToAttach.getClass(), accountListNewAccountToAttach.getAccountnum());
                attachedAccountListNew.add(accountListNewAccountToAttach);
            }
            accountListNew = attachedAccountListNew;
            agency.setAccountList(accountListNew);
            agency = em.merge(agency);
            for (Account accountListNewAccount : accountListNew) {
                if (!accountListOld.contains(accountListNewAccount)) {
                    Agency oldAgencyidOfAccountListNewAccount = accountListNewAccount.getAgencyid();
                    accountListNewAccount.setAgencyid(agency);
                    accountListNewAccount = em.merge(accountListNewAccount);
                    if (oldAgencyidOfAccountListNewAccount != null && !oldAgencyidOfAccountListNewAccount.equals(agency)) {
                        oldAgencyidOfAccountListNewAccount.getAccountList().remove(accountListNewAccount);
                        oldAgencyidOfAccountListNewAccount = em.merge(oldAgencyidOfAccountListNewAccount);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = agency.getAgencyid();
                if (findAgency(id) == null) {
                    throw new NonexistentEntityException("The agency with id " + id + " no longer exists.");
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
            Agency agency;
            try {
                agency = em.getReference(Agency.class, id);
                agency.getAgencyid();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The agency with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            List<Account> accountListOrphanCheck = agency.getAccountList();
            for (Account accountListOrphanCheckAccount : accountListOrphanCheck) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Agency (" + agency + ") cannot be destroyed since the Account " + accountListOrphanCheckAccount + " in its accountList field has a non-nullable agencyid field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            em.remove(agency);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Agency> findAgencyEntities() {
        return findAgencyEntities(true, -1, -1);
    }

    public List<Agency> findAgencyEntities(int maxResults, int firstResult) {
        return findAgencyEntities(false, maxResults, firstResult);
    }

    private List<Agency> findAgencyEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Agency.class));
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

    public Agency findAgency(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Agency.class, id);
        } finally {
            em.close();
        }
    }

    public int getAgencyCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Agency> rt = cq.from(Agency.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
