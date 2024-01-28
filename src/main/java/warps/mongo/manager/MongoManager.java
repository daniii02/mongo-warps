package warps.mongo.manager;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.util.Strings;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bukkit.configuration.ConfigurationSection;
import warps.mongo.MongoWarps;

import java.util.concurrent.CompletableFuture;

public final class MongoManager {
    private MongoClient client;
    private MongoDatabase database;

    public MongoManager() {
        // Desactivo los logs de MongoDB
        Configurator.setAllLevels("org.mongodb.driver", Level.WARN);

        loadDatabase();
    }

    private void loadDatabase() {
        ConfigurationSection config = MongoWarps.get().getConfig().getConfigurationSection("database");

        String connectionString = config.getString("mongo-uri");
        String databaseName = config.getString("database-name");

        if (Strings.isBlank(connectionString)) {
            String host = config.getString("host");
            String username = config.getString("username");
            String password = config.getString("password");

            connectionString = "mongodb://" + host;
            if (!Strings.isBlank(username) && !Strings.isBlank(password)) {
                connectionString = "mongodb://" + username + ":" + password + "@" + host;
            }
        }

        MongoClientSettings settings = MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .retryWrites(true)
                .applyConnectionString(new ConnectionString(connectionString))
                .build();

        client = MongoClients.create(settings);
        database = client.getDatabase(databaseName);
    }

    /**
     * Reconecta la base de datos
     * Uso interno
     * @return {@link CompletableFuture} de la reconexión
     */
    public CompletableFuture<Void> reconnect() {
        return MongoWarps.get().getTaskManager().runAsync(() -> {
            stop();
            loadDatabase();
        });
    }

    /**
     * Obtiene una colección de la base de datos.
     * @param name nombre de la colección
     * @return colección
     */
    public MongoCollection<Document> getCollection(String name) {
        return database.getCollection(name);
    }

    /**
     * Cierra la conexión con la base de datos.
     * Uso interno
     */
    public void stop() {
        if (client != null) client.close();
    }
}
