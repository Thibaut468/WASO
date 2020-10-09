package fr.insalyon.waso.som.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.DBConnection;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.DBException;
import fr.insalyon.waso.util.exception.ServiceException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author WASO Team
 */
public class ServiceObjetMetier {

    protected DBConnection dBConnection;
    protected JsonObject container;

    public ServiceObjetMetier(DBConnection dBConnection, JsonObject container) {
        this.dBConnection = dBConnection;
        this.container = container;
    }
    
    public void release() {
        this.dBConnection.close();
    }

    public void getListeClient() throws ServiceException {
        try {
            JsonArray jsonListe = new JsonArray();

            List<Object[]> listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT ORDER BY ClientID");

            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);

        } catch (DBException ex) {
            throw JsonServletHelper.ServiceObjectMetierExecutionException("Client","getListeClient", ex);
        }
    }
    
    public void rechercherClientParDenomination(String denomination, String ville) {
        try {
            JsonArray jsonListe = new JsonArray();
            List<Object[]> listeClients;
            if (!ville.isEmpty()) {
                listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE Denomination = ? and Ville = ?", denomination, ville);
            } else {
                listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE Denomination = ?", denomination);
            }
            
            for (Object[] row : listeClients) {
                JsonObject jsonItem = new JsonObject();

                Integer clientId = (Integer) row[0];
                jsonItem.addProperty("id", clientId);
                jsonItem.addProperty("type", (String) row[1]);
                jsonItem.addProperty("denomination", (String) row[2]);
                jsonItem.addProperty("adresse", (String) row[3]);
                jsonItem.addProperty("ville", (String) row[4]);

                List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                JsonArray jsonSousListe = new JsonArray();
                for (Object[] innerRow : listePersonnes) {
                    jsonSousListe.add((Integer) innerRow[1]);
                }

                jsonItem.add("personnes-ID", jsonSousListe);

                jsonListe.add(jsonItem);
            }

            this.container.add("clients", jsonListe);
        } catch (DBException ex) {
            Logger.getLogger(ServiceObjetMetier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void rechercherClientParNumero(Integer numero) {
        try {
            JsonArray jsonListe = new JsonArray();
            List<Object[]> listeClients;
            listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE ClientID = ?", numero);
                   
            Object[] row = listeClients.get(0);
            JsonObject jsonItem = new JsonObject();
            
            Integer clientId = (Integer) row[0];
            jsonItem.addProperty("id", clientId);
            jsonItem.addProperty("type", (String) row[1]);
            jsonItem.addProperty("denomination", (String) row[2]);
            jsonItem.addProperty("adresse", (String) row[3]);
            jsonItem.addProperty("ville", (String) row[4]);

            List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
            JsonArray jsonSousListe = new JsonArray();
            for (Object[] innerRow : listePersonnes) {
                jsonSousListe.add((Integer) innerRow[1]);
            }

            jsonItem.add("personnes-ID", jsonSousListe);

            this.container.add("client", jsonItem);
        } catch (DBException ex) {
            Logger.getLogger(ServiceObjetMetier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void rechercherClientParPersonne(Integer idPersonne, String ville) {
        try {
            JsonArray jsonListe = new JsonArray();
            List<Object[]> listeIdClient = this.dBConnection.launchQuery("SELECT ClientID FROM COMPOSER WHERE PersonneID = ? ORDER BY ClientID", idPersonne);
            
            for(Object[] rowId : listeIdClient) {
                
                List<Object[]> listeClients;
                if (!ville.isEmpty()) {
                    listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE ClientID = ? and Ville = ?", (Integer)rowId[0], ville);
                } else {
                    listeClients = this.dBConnection.launchQuery("SELECT ClientID, TypeClient, Denomination, Adresse, Ville FROM CLIENT WHERE ClientID = ?", (Integer)rowId[0]);
                }
                
                for (Object[] row : listeClients) {
                    JsonObject jsonItem = new JsonObject();

                    Integer clientId = (Integer) row[0];
                    jsonItem.addProperty("id", clientId);
                    jsonItem.addProperty("type", (String) row[1]);
                    jsonItem.addProperty("denomination", (String) row[2]);
                    jsonItem.addProperty("adresse", (String) row[3]);
                    jsonItem.addProperty("ville", (String) row[4]);

                    List<Object[]> listePersonnes = this.dBConnection.launchQuery("SELECT ClientID, PersonneID FROM COMPOSER WHERE ClientID = ? ORDER BY ClientID,PersonneID", clientId);
                    JsonArray jsonSousListe = new JsonArray();
                    for (Object[] innerRow : listePersonnes) {
                        jsonSousListe.add((Integer) innerRow[1]);
                    }

                    jsonItem.add("personnes-ID", jsonSousListe);

                    jsonListe.add(jsonItem);
                }
                
            }
            
            this.container.add("clients", jsonListe);
        } catch (DBException ex) {
            Logger.getLogger(ServiceObjetMetier.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
