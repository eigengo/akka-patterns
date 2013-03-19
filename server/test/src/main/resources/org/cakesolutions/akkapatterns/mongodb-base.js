db.getCollectionNames().forEach(function (name) {
	if (name.indexOf("system.") == -1) db.getCollection(name).remove()
});

db.customers.save({

});

db.users.save({
        id: UUID("994fc1f090a911e29e960800200c9a66"),
        username: "guest",
        hashedPassword: "",
        email: "johndoe@example.com",
        firstName: "John",
        lastName: "Doe",
        kind: {kind: "guest"}
    }, {
        id: UUID("7370f98090aa11e29e960800200c9a66"),
        username: "customer",
        hashedPassword: "",
        email: "johndoe@example.com",
        mobile: "07777777777",
        firstName: "John",
        lastName: "Doe",
        kind: {kind: "customer", value: UUID("82c6e89090aa11e29e960800200c9a66")}
    },{
        id: UUID("c0a9319090aa11e29e960800200c9a66"),
        username: "root",
        hashedPassword: "",
        email: "johndoe@example.com",
        firstName: "John",
        lastName: "Doe",
        kind: {kind: "guest"}
    }
)