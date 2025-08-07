package com.foo;

import dev.morphia.annotations.CappedAt;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import org.bson.types.ObjectId;

@Entity(cap = @CappedAt(count = 100))
public class CappedEntity {
    @Id
    public ObjectId id;
}
