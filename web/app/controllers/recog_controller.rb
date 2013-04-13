class RecogController < ApplicationController

  def index

  end

  def begin
    handle_api(api.post('/recog')) do |id|
      case params[:method]
        when 'webcam' then redirect_to recog_webcam_path(id)
        when 'mobile' then redirect_to recog_mobile_path(id)
        else               redirect_to recog_path
      end
    end
  end

  def webcam

  end

  def mobile
    @qr_code = "A#{params[:id]}akka 192.168.200.101:8080"
  end

  private
  def check_active
    active = handle_api(api.get("/recog/#{params[:id]}/active"))
    if active.eql? 'false'
      redirect_to recog_url, :flash => {error: "The recognition #{params[:id]} is not active."}
    end
  end

end