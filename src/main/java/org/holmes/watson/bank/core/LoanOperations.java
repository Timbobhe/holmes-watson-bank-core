/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core;

import java.math.BigDecimal;
import org.holmes.watson.bank.core.client.Client;

/**
 *
 * @author Olayinka
 */
public interface LoanOperations extends Operations {

    public Message demandLoan(Client client, BigDecimal amount);

    public Message terminateLoan(Client client, int loanId);

    public Message payLoan(Client client, Integer loanId);
}
