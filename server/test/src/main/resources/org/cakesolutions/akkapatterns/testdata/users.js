db.users.drop();
db.users.save([
{
    id: JUUID("994fc1f0-90a9-11e2-9e96-0800200c9a66"),
    username: "guest",
    hashedPassword: "",
    email: "johndoe@example.com",
    firstName: "John",
    lastName: "Doe",
    kind: {kind: "guest"}
}, {
    id: JUUID("7370f980-90aa-11e2-9e96-0800200c9a66"),
    username: "customer",
    hashedPassword: "",
    email: "johndoe@example.com",
    mobile: "07777777777",
    firstName: "John",
    lastName: "Doe",
    kind: {kind: "customer", value: {customerReference: JUUID("67c2b250-92f2-11e2-9e96-0800200c9a66")}}
},{
    id: JUUID("c0a93190-90aa-11e2-9e96-0800200c9a66"),
    username: "root",
    hashedPassword: "",
    email: "johndoe@example.com",
    firstName: "John",
    lastName: "Doe",
    kind: {kind: "guest"}
}
]);