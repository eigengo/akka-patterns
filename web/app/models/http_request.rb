class HttpRequest

  include HTTParty

  base_uri Rails.application.config.server_url

  headers 'Content-Type'  => 'application/json'

  disable_rails_query_string_format

  def initialize(token)
    self.class.headers.merge!('x-token' => token.nil? ? '' : token)
  end

  def get(url, opts={})
    if url.present?
      response = self.class.get(url, :query => opts)
      authorized response
      response
    end
  end

  def post(url, opts = {})
    self.class.post(url, :body => opts.to_json) if url.present?
  end

  def put(url, opts = {})
    self.class.put(url, :body => opts.to_json) if url.present?
  end

  def delete(url, opts = {})
    self.class.delete(url, :body => opts.to_json) if url.present?
  end

  private
  def authorized(response)
    if response.code == 401 then
      raise UnauthorizedException.new("Unauthorized request. Response content: #{response}")
    end
  end

end
