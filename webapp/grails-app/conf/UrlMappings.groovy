class UrlMappings {

	static mappings = {
		"/customers"(controller: "customer", action: [GET: "list", POST: "doAdd", PUT: "doSave"])
    "/customers/add"(controller: "customer", action: [GET: "add"])
    "/customers/$customerId"(controller: "customer", action: [GET: "show", DELETE: "doDelete"]) {
      constraints {
        customerId(minSize: 36, maxSize: 36)
      }
    }

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
