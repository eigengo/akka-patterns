package webapp

class CustomerController extends RestClient {

  // GET /
  def list = {
    [customers: doGet("/customers").ok()]
  }

  // GET /UUID
  def show = {
    [customers: doGet("/customers/{customerId}", params).ok()]
  }

  // GET /add
  def add = {
    [customer: new CustomerForm()]
  }

  // DELETE /UUID
  def delete = {
    doDelete("/customers/{customerId}", params)
    redirect(url: "/customers")
  }

  // POST /customers
  def doSave(CustomerForm form) {

    redirect(url: "/customers")
  }
}
