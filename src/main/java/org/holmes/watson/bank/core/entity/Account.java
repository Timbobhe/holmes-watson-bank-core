/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Olayinka
 */
@Entity
@Table(name = "ACCOUNT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Account.findAll", query = "SELECT a FROM Account a"),
    @NamedQuery(name = "Account.findByAccountnum", query = "SELECT a FROM Account a WHERE a.accountnum = :accountnum"),
    @NamedQuery(name = "Account.findByAccountbalance", query = "SELECT a FROM Account a WHERE a.accountbalance = :accountbalance")})
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ACCOUNTNUM")
    private BigDecimal accountnum;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ACCOUNTBALANCE")
    private BigDecimal accountbalance;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "accountnum")
    private List<Transaction> transactionList;
    @JoinColumn(name = "CLIENTID", referencedColumnName = "CLIENTID")
    @ManyToOne(optional = false)
    private Client clientid;
    @JoinColumn(name = "AGENCYID", referencedColumnName = "AGENCYID")
    @ManyToOne(optional = false)
    private Agency agencyid;

    public Account() {
    }

    public Account(BigDecimal accountnum) {
        this.accountnum = accountnum;
    }

    public Account(BigDecimal accountnum, BigDecimal accountbalance) {
        this.accountnum = accountnum;
        this.accountbalance = accountbalance;
    }

    public BigDecimal getAccountnum() {
        return accountnum;
    }

    public void setAccountnum(BigDecimal accountnum) {
        this.accountnum = accountnum;
    }

    public BigDecimal getAccountbalance() {
        return accountbalance;
    }

    public void setAccountbalance(BigDecimal accountbalance) {
        this.accountbalance = accountbalance;
    }

    @XmlTransient
    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public Client getClientid() {
        return clientid;
    }

    public void setClientid(Client clientid) {
        this.clientid = clientid;
    }

    public Agency getAgencyid() {
        return agencyid;
    }

    public void setAgencyid(Agency agencyid) {
        this.agencyid = agencyid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (accountnum != null ? accountnum.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Account)) {
            return false;
        }
        Account other = (Account) object;
        if ((this.accountnum == null && other.accountnum != null) || (this.accountnum != null && !this.accountnum.equals(other.accountnum))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "org.holmes.watson.bank.core.entity.Account[ accountnum=" + accountnum + " ]";
    }
    
}
