package com.foo;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.aggregation.stages.Lookup;
import dev.morphia.aggregation.stages.Unwind;
import org.bson.UuidRepresentation;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MongoDBContainer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.mongodb.MongoClientSettings.builder;
import static dev.morphia.query.filters.Filters.eq;

public class ReproducerTest {

    private MongoDBContainer mongoDBContainer;
    private String connectionString;

    private Datastore datastore;

    @Test
    public void reproduce() {
        Book book = new Book("The Eye of the World");
        book.author = new Author("Robert Jordan");
        datastore.save(book.author);
        datastore.save(book);

        Book book1 = datastore.find(Book.class).iterator().toList().get(0);
        Author author1 = datastore.find(Author.class).iterator().toList().get(0);

        System.out.println(book1.getAuthor().getId());
        System.out.println(author1.getId());

        datastore.aggregate(Book.class)
                .lookup(Lookup.lookup(Author.class)
                        .localField("author")
                        .foreignField("_id")
                        .as("author"))
                .unwind(Unwind.unwind("author"))
                .match(eq("author.name", "Robert Jordan"))
                .execute(Book.class)
                .toList();
    }

    @Test
    public void capTest() {
        datastore.save(new CappedEntity());
        datastore.ensureCaps();
    }

    @NotNull
    public String databaseName() {
        return "morphia_repro";
    }

    @NotNull
    public String dockerImageName() {
        return "mongo:7";
    }

    @BeforeClass
    private void setup() {
        mongoDBContainer = new MongoDBContainer(dockerImageName());
        mongoDBContainer.start();
        connectionString = mongoDBContainer.getReplicaSetUrl(databaseName());

        MongoClient mongoClient = MongoClients.create(builder()
                                                  .uuidRepresentation(UuidRepresentation.STANDARD)
                                                  .applyConnectionString(new ConnectionString(connectionString))
                                                  .build());

        datastore = Morphia.createDatastore(mongoClient, databaseName());
    }
}
