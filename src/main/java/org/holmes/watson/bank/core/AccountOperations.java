/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.holmes.watson.bank.core;

import org.android.json.JSONObject;
import org.holmes.watson.bank.core.client.Client;

/**
 *
 * @author Olayinka
 */
public interface AccountOperations extends Operations {

    public Message createAccount(JSONObject newClient);

    public Message deleteAccount(Client newClient);

    public Message modifyAccount(Client newClient);
}
