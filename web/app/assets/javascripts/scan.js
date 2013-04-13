function feedbackUI(elementId, text, clazz) {
    var element = $('#' + elementId);
    element.text(text);
    element.attr('class', clazz);
}

function feedback(data) {

    function nothingChecked() {
        feedbackUI('histogram', 'Light and colours', 'not_checked');
        feedbackUI('focus', 'Focus', 'not_checked');
        feedbackUI('corners', 'Corners', 'not_checked');
        feedbackUI('face', 'Face', 'not_checked');
    }

    if (!data) { nothingChecked(); return false; }
    if (!data['preflight']) { nothingChecked(); return false; }
    var result = data['preflight'];

    var histogram = result.histogram;
    var face = result.face;
    var corners = result.corners;
    var focus = result['focus'];

    if (histogram) {
        if (histogram.tooDark) feedbackUI('histogram', 'Too dark', 'fail');
        else if (histogram.tooLight) feedbackUI('histogram', 'Too light', 'fail');
        else feedbackUI('histogram', 'Light and colours', 'ok');
    } else feedbackUI('histogram', 'Light and colours', 'not_checked');
    if (focus) {
        if (focus.notInFocus) feedbackUI('focus', 'In focus', 'fail');
        else feedbackUI('focus', 'In focus', 'ok');
    } else feedbackUI('focus', 'Focus', 'not_checked');
    if (corners) {
        var text = [];
        if (corners.topMissing) text.push("Top missing");
        if (corners.bottomMissing) text.push("Bottom missing");
        if (corners.leftMissing) text.push("Left missing");
        if (corners.rightMissing) text.push("Right missing");
        if (text.length > 0) feedbackUI('corners', text.join(', '), 'fail');
        else feedbackUI('corners', 'Corners', 'ok');
    } else feedbackUI('corners', 'Corners', 'not_checked');
    if (face) {
        if (face.faceMissing)      feedbackUI('face', 'Face missing', 'fail');
        else if (face.faceTooBig)  feedbackUI('face', 'Face too big', 'fail');
        else if (face.faceTooSmall)feedbackUI('face', 'Face too small', 'fail');
        else feedbackUI('face', 'Face', 'ok');
    } else feedbackUI('face', 'Face', 'not_checked');

    return data.preflightSucceeded;

}

function startWebcamScan(opts) {
//    var lastImageCount = 10;
    var url = opts.preflightUrl;
//    var lastImages = [];
    var process = true;

    function restart() {
        process = true;
    }

    function base64_to_image() {
        var imageData = $.scriptcam.getFrameAsBase64();
//        if (lastImages.length > lastImageCount) lastImages.shift();
//        lastImages.push(imageData);

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
                         if (opts.onPreflightComplete) opts.onPreflightComplete(imageData, [], restart);
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
