package webapp

class RestResponse {
  def status
  def json
  def text

  def boolean isOk() { status == 200 }

  def ok() {
    if (status != 200) throw new Exception("Response is not OK. " + text)
    json
  }

  def errorJson() {
    if (status == 200) throw new Exception("Respose is OK. ")
    if (json == null) throw new Exception("No JSON payload.")
    json
  }

  def errorText() {
    if (status == 200) throw new Exception("Respose is OK. ")
    text
  }

  @Override
  public String toString() {
    return "RestResponse{" +
            "status=" + status +
            ", json=" + json +
            ", text=" + text +
            '}';
  }
}
