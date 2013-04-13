module ApplicationHelper

  def flash_class(level)
    case level
      when :notice  then "alert alert-info"
      when :success then "alert alert-success"
      when :error   then "alert alert-error"
      when :alert   then "alert alert-error"
    end
  end

  def signed_in?
    session[:token].present?
  end

  def active_li_with_link_to(link_name, path, opts={}, &block)
    in_path = false
    if path == "/"
      in_path = request.fullpath == "/"
    else
      in_path = request.fullpath.starts_with? path
    end

    html_opts = opts.merge(class: "#{'active' if in_path }") { |k, x, y| [x, y].join(' ') }
    capture_haml do
      haml_tag :li, html_opts do
        haml_concat link_to link_name, path
      end
    end
  end

end
