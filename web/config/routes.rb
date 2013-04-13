Web::Application.routes.draw do
  root to: 'home#index'

  controller :recog do
    get  'recog'               => :index,  :as => :recog
    get  'recog/begin/:method' => :begin,  :as => :recog_begin
    get  'recog/:id/webcam'    => :webcam, :as => :recog_webcam
    get  'recog/:id/mobile'    => :mobile, :as => :recog_mobile
  end

end
