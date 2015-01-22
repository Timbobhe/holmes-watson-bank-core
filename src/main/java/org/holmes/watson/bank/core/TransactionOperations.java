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
public interface TransactionOperations extends Operations {

    public Message transferMoney(Client donor, Client recipient, BigDecimal amounut);
    
    public Message withdrawMoney(Client donor, BigDecimal amounut);
    
    public Message depositMoney(Client donor, BigDecimal amounut);
}
