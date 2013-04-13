db.customers.drop();
db.customers.save([
    {
        "firstName":"Jan",
        "lastName":"Machacek",
        "email":"janm@cakesolutions.net",
        "id":JUUID("122fa630-92fd-11e2-9e96-0800200c9a66"),
        "addresses":[
            {"line1":"Magdalen Centre", "line2":"Robert Robinson Avenue", "line3":"Oxford"},
            {"line1":"Houldsworth Mill", "line2":"Houldsworth Street", "line3":"Reddish"}
        ]
    }
]);
