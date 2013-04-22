function feedbackUI(elementId, text, clazz) {
    var element = $('#' + elementId);
    element.text(text);
    element.attr('class', clazz);
}

function feedback(data) {

    function nothingChecked() {
        feedbackUI('accepted', 'Accepted', 'not_checked');
    }

    if (!data) { nothingChecked(); return false; }
    if (data.accepted) feedbackUI('accepted', 'Accepted', 'ok'); else feedbackUI('accepted', 'Accepted', 'fail');

    return data.accepted;

}

function startWebcamScan(opts) {
    var url = opts.preflightUrl;
    var process = true;

    function restart() {
        process = true;
    }

    function base64_to_image() {
        var imageData = $.scriptcam.getFrameAsBase64();

        if (process) {
            $.ajax({
                type: "POST",
                url: url,
                data: imageData,
                dataType: "JSON"
            }).done(function(data) {
                 process = false;
                 if (opts.onPreflight) {
                     if (opts.onPreflight(data)) {
                         if (opts.onPreflightComplete) opts.onPreflightComplete(imageData, restart);
                     } else {
                         process = true;
                         setTimeout(base64_to_image, 200);
                     }
                 }
            }).fail(function(jqXHR, textStatus, errorThrown) {
                 setTimeout(base64_to_image, 200);
            });
        } else {
            setTimeout(base64_to_image(), 200);
        }
    }

    function onError(errorId, errorMsg) {
        alert(errorMsg);
        process = false;
    }

    function onWebcamReady(cameraNames, camera, microphoneNames, microphone, volume) {
        base64_to_image();
    }

    opts.camera.scriptcam({
        path:'/assets/',
        showMicrophoneErrors:false,
        onError:onError,
        cornerRadius:0,
        width:640,
        height:480,
        onWebcamReady:onWebcamReady,
        onPictureAsBase64:base64_to_image
    });

}
