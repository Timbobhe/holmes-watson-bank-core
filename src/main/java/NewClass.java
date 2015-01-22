/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.math.BigDecimal;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.holmes.watson.bank.core.entity.Client;
import org.holmes.watson.bank.core.entity.ClientJpaController;

/**
 *
 * @author Olayinka
 */
public class NewClass {

    public static void main(String[] args) throws Exception {
        EntityManager entityManager = Persistence.createEntityManagerFactory("HolmesWatsonPU1").createEntityManager();
        ClientJpaController cjc =  new ClientJpaController(entityManager.getEntityManagerFactory());
        cjc.create(new Client(BigDecimal.ONE, "folorunso", "olayinka", "gueliz"));
    }
}
