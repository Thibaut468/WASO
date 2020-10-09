package fr.insalyon.waso.sma;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.insalyon.waso.util.JsonHttpClient;
import fr.insalyon.waso.util.JsonServletHelper;
import fr.insalyon.waso.util.exception.ServiceException;
import fr.insalyon.waso.util.exception.ServiceIOException;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author WASO Team
 */
public class ServiceMetier {

    protected final String somClientUrl;
    protected final String somPersonneUrl;
    protected final String somContactUrl;
    protected final String somStructureUrl;
    protected final String somProduitUrl;
    protected final String somContratUrl;
    protected final JsonObject container;

    protected JsonHttpClient jsonHttpClient;

    public ServiceMetier(String somClientUrl, String somPersonneUrl, String somContactUrl, String somStructureUrl, String somProduitUrl, String somContratUrl, JsonObject container) {
        this.somClientUrl = somClientUrl;
        this.somPersonneUrl = somPersonneUrl;
        this.somContactUrl = somContactUrl;
        this.somStructureUrl = somStructureUrl;
        this.somProduitUrl = somProduitUrl;
        this.somContratUrl = somContratUrl;
        this.container = container;

        this.jsonHttpClient = new JsonHttpClient();
    }

    public void release() {
        try {
            this.jsonHttpClient.close();
        } catch (IOException ex) {
            // Ignorer
        }
    }

    public void getListeClient() throws ServiceException {
        try {

            // 1. Obtenir la liste des Clients
            
            JsonObject clientContainer = null;
            try {
                clientContainer = this.jsonHttpClient.post(
                        this.somClientUrl,
                        new JsonHttpClient.Parameter("SOM", "getListeClient")
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "getListeClient", ex);
            }

            JsonArray jsonOutputClientListe = clientContainer.getAsJsonArray("clients");


            // 2. Obtenir la liste des Personnes
            
            JsonObject personneContainer = null;
            try {
                personneContainer = this.jsonHttpClient.post(
                        this.somPersonneUrl,
                        new JsonHttpClient.Parameter("SOM", "getListePersonne")
                );
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somPersonneUrl, "Personne", "getListePersonne", ex);
            }


            // 3. Indexer la liste des Personnes
            
            HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();

            for (JsonElement p : personneContainer.getAsJsonArray("personnes")) {

                JsonObject personne = p.getAsJsonObject();

                personnes.put(personne.get("id").getAsInt(), personne);
            }


            // 4. Construire la liste des Personnes pour chaque Client (directement dans le JSON)

            for (JsonElement clientJsonElement : jsonOutputClientListe.getAsJsonArray()) {

                JsonObject client = clientJsonElement.getAsJsonObject();

                JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

                JsonArray outputPersonnes = new JsonArray();

                for (JsonElement personneID : personnesID) {
                    JsonObject personne = personnes.get(personneID.getAsInt());
                    outputPersonnes.add(personne);
                }

                client.add("personnes", outputPersonnes);

            }


            // 5. Ajouter la liste de Clients au conteneur JSON

            this.container.add("clients", jsonOutputClientListe);

        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("getListeClient", ex);
        }
    }

    public void rechercherClientParDenomination(String denomination, String ville) throws ServiceException {
        try {

            // 1. Obtenir la liste des Clients
            
            JsonObject clientContainer = null;
            try {
                clientContainer = this.jsonHttpClient.post(
                    this.somClientUrl,
                    new JsonHttpClient.Parameter("SOM", "rechercherClientParDenomination"),
                    new JsonHttpClient.Parameter("denomination", denomination),
                    new JsonHttpClient.Parameter("ville", ville)
                );               
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "rechercherClientParDenomination", ex);
            }

            JsonArray jsonOutputClientListe = clientContainer.getAsJsonArray("clients");
            
            HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();
            
            for (JsonElement c : jsonOutputClientListe){
                JsonObject client = c.getAsJsonObject();
                JsonArray listePersonneId = client.get("personnes-ID").getAsJsonArray();
                
                for(JsonElement personne_id : listePersonneId){
                    // 2. Obtenir la liste des Personnes depuis les clients
            
                    JsonObject personneContainer = null;
                    try {
                        personneContainer = this.jsonHttpClient.post(
                                this.somPersonneUrl,
                                new JsonHttpClient.Parameter("SOM", "getPersonneParId"),
                                new JsonHttpClient.Parameter("id-personne",personne_id.getAsString())
                        );
                    }
                    catch (ServiceIOException ex) {
                        throw JsonServletHelper.ServiceObjectMetierCallException(this.somPersonneUrl, "Personne", "getPersonneParId", ex);
                    }

                    System.out.println(personneContainer);
                    // 3. Indexer la personne à la liste
                    
                    JsonElement p = personneContainer.getAsJsonObject("personne");
                    JsonObject personne = p.getAsJsonObject();
                    personnes.put(personne.get("id").getAsInt(), personne);

                 
                }
            }
            
            // 4. Construire la liste des Personnes pour chaque Client (directement dans le JSON)

            for (JsonElement clientJsonElement : jsonOutputClientListe.getAsJsonArray()) {

                JsonObject client = clientJsonElement.getAsJsonObject();

                JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

                JsonArray outputPersonnes = new JsonArray();

                for (JsonElement personneID : personnesID) {
                    JsonObject personne = personnes.get(personneID.getAsInt());
                    outputPersonnes.add(personne);
                    System.out.println(personne);
                }

                client.add("personnes", outputPersonnes);

            }


            // 5. Ajouter la liste de Clients au conteneur JSON

            this.container.add("clients", jsonOutputClientListe);

        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("rechercherClientParDenomination", ex);
        }
    } 
    
    public void rechercherClientParNumero(Integer numero) throws ServiceException {
        try {

            // 1. Obtenir le client
            
            JsonObject clientContainer = null;
            try {
                clientContainer = this.jsonHttpClient.post(
                    this.somClientUrl,
                    new JsonHttpClient.Parameter("SOM", "rechercherClientParNumero"),
                    new JsonHttpClient.Parameter("numero", numero.toString())
                );               
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "rechercherClientParNumero", ex);
            }

            JsonElement jsonOutputClient = clientContainer.get("client");
            
            HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();
            
            JsonObject client = jsonOutputClient.getAsJsonObject();
            JsonArray listePersonneId = client.get("personnes-ID").getAsJsonArray();

            for(JsonElement personne_id : listePersonneId){
                // 2. Obtenir la liste des Personnes depuis les clients

                JsonObject personneContainer = null;
                try {
                    personneContainer = this.jsonHttpClient.post(
                            this.somPersonneUrl,
                            new JsonHttpClient.Parameter("SOM", "getPersonneParId"),
                            new JsonHttpClient.Parameter("id-personne",personne_id.getAsString())
                    );
                }
                catch (ServiceIOException ex) {
                    throw JsonServletHelper.ServiceObjectMetierCallException(this.somPersonneUrl, "Personne", "getPersonneParId", ex);
                }

                System.out.println(personneContainer);
                // 3. Indexer la personne à la liste

                JsonElement p = personneContainer.getAsJsonObject("personne");
                JsonObject personne = p.getAsJsonObject();
                personnes.put(personne.get("id").getAsInt(), personne);


            }
            
            // 4. Construire la liste des Personnes pour le client (directement dans le JSON)

            JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

            JsonArray outputPersonnes = new JsonArray();

            for (JsonElement personneID : personnesID) {
                JsonObject personne = personnes.get(personneID.getAsInt());
                outputPersonnes.add(personne);
                System.out.println(personne);
            }

            client.add("personnes", outputPersonnes);

            // 5. Ajouter la liste de Clients au conteneur JSON
            JsonArray jsonOutputArray = new JsonArray();
            jsonOutputArray.add(jsonOutputClient);
            this.container.add("clients", jsonOutputArray);

        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("rechercherClientParDenomination", ex);
        }
    }
    
    public void rechercherClientParNomPersonne(String nomPersonne, String ville) throws ServiceException {
        try {

            // 1. Obtenir la liste des idClient depuis le nom de la personne
            JsonObject personneIdContainer = null;
            try {
                personneIdContainer = this.jsonHttpClient.post(
                    this.somPersonneUrl,
                    new JsonHttpClient.Parameter("SOM", "rechercherPersonneParNom"),
                    new JsonHttpClient.Parameter("nom-personne", nomPersonne)
                );               
            }
            catch (ServiceIOException ex) {
                throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "rechercherClientParDenomination", ex);
            }
            
            JsonArray jsonPersonneIdListe = personneIdContainer.getAsJsonArray("personnes-ID");
            
            // 2. Récupérer dans le SOM Client les entités clients et idPersonne liéss aux idClients de l'étape 1
            JsonArray jsonOutputClientListe = new JsonArray();
            
            for(JsonElement personne_id : jsonPersonneIdListe){
                JsonObject clientContainer = null;
                try {
                    clientContainer = this.jsonHttpClient.post(
                        this.somClientUrl,
                        new JsonHttpClient.Parameter("SOM", "rechercherClientParPersonne"),
                        new JsonHttpClient.Parameter("id-personne", personne_id.getAsString()),
                        new JsonHttpClient.Parameter("ville", ville)
                    );               
                }
                catch (ServiceIOException ex) {
                    throw JsonServletHelper.ServiceObjectMetierCallException(this.somClientUrl, "Client", "rechercherClientParDenomination", ex);
                }

                for(JsonElement c : clientContainer.getAsJsonArray("clients")){
                    jsonOutputClientListe.add(c);
                }
               
            }
  
            // 3. Récupérer enfin les Personnes dans le SOM Personne de manière habituelle
            HashMap<Integer, JsonObject> personnes = new HashMap<Integer, JsonObject>();
            
            for (JsonElement c : jsonOutputClientListe){
                JsonObject client = c.getAsJsonObject();
                JsonArray listePersonneId = client.get("personnes-ID").getAsJsonArray();
                
                for(JsonElement personne_id : listePersonneId){
                                
                    JsonObject personneContainer = null;
                    try {
                        personneContainer = this.jsonHttpClient.post(
                                this.somPersonneUrl,
                                new JsonHttpClient.Parameter("SOM", "getPersonneParId"),
                                new JsonHttpClient.Parameter("id-personne",personne_id.getAsString())
                        );
                    }
                    catch (ServiceIOException ex) {
                        throw JsonServletHelper.ServiceObjectMetierCallException(this.somPersonneUrl, "Personne", "getPersonneParId", ex);
                    }

                    // 3. Indexer la personne à la liste
                    
                    JsonElement p = personneContainer.getAsJsonObject("personne");
                    JsonObject personne = p.getAsJsonObject();
                    personnes.put(personne.get("id").getAsInt(), personne);
                }
            }
            
            // 4. Construire la liste des Personnes pour le client (directement dans le JSON)

            for (JsonElement clientJsonElement : jsonOutputClientListe.getAsJsonArray()) {

                JsonObject client = clientJsonElement.getAsJsonObject();

                JsonArray personnesID = client.get("personnes-ID").getAsJsonArray();

                JsonArray outputPersonnes = new JsonArray();

                for (JsonElement personneID : personnesID) {
                    JsonObject personne = personnes.get(personneID.getAsInt());
                    outputPersonnes.add(personne);
                }

                client.add("personnes", outputPersonnes);

            }

            // 5. Ajouter la liste de Clients au conteneur JSON

            this.container.add("clients", jsonOutputClientListe);
            
        } catch (Exception ex) {
            throw JsonServletHelper.ServiceMetierExecutionException("rechercherClientParDenomination", ex);
        }
    }
}
