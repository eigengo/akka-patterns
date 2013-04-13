class ApplicationController < ActionController::Base
  # Prevent CSRF attacks by raising an exception.
  # For APIs, you may want to use :null_session instead.
  protect_from_forgery with: :exception
  rescue_from Exception, :with =>:handle_exception

  protected
  def handle_api response
    case response.code
      when 200
        yield response if block_given?
        response
      when 401
        raise UnauthorizedException.new("Unauthorized request. Response content: #{response}")
      else
        raise Exception.new("Error sent from server: #{response.code}, response content: #{response}")
    end
  end

  def api
    HttpRequest.new(session[:token])
  end

  def handle_exception(exception)
    wrapper = wrapper(exception)
    log_exception(exception, wrapper)
    # self.api.post('/error', {message: exception.message, trace: wrapper.application_trace})
    @exception = exception.message
    @trace = wrapper.application_trace
    render template: 'fail'
  end

  def unauthorized_exception(exception)
    wrapper = wrapper(exception)
    log_exception(exception, wrapper)
    redirect_to login_url
  end

  private
  def wrapper(exception)
    ActionDispatch::ExceptionWrapper.new(self.env, exception)
  end

  def log_exception(exception, wrapper)
    logger.debug "message => #{exception.message}"
    logger.debug "trace => #{wrapper.application_trace}"
  end
end
