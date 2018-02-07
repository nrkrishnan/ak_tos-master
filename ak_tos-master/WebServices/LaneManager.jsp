<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <% response.addHeader("Refresh", "360000");%>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Matson Navigation Company</title>
    <bgsound id="soundeffect1" src="#" loop="1"/>
    <bgsound id="soundeffect2" src="#" loop="1"/>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.0/jquery.min.js"></script>
    <script type="text/javascript">
        setInterval(function () {
            $("#inlane-img").attr("src", "http://<%= request.getSession().getAttribute("INLANE1") %>/axis-cgi/jpg/image.cgi?camera=1&compression=30&resolution=fullsize?" + new Date());
            $("#outlane-img").attr("src", "http://<%= request.getSession().getAttribute("OUTLANE1") %>/axis-cgi/jpg/image.cgi?camera=1&compression=30&resolution=fullsize" + new Date());
        }, 3000);
    </script>
    <style>
        #span1 {
            background-color: aquamarine;
            line-height: 1
        }

        #span2 {
            background-color: #275da7;
            line-height: 1
        }

        #progress1 {
            width: 300px;
            height: 22px;
            color: #FFFFFF;
            text-align: right;
            position: relative;
            display: block;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            border: 1px solid #111;
            background-color: #292929;
        }

        #progress1 div {
            height: 100%;
            color: #000000;
            text-align: right;
            line-height: 22px; /* same as #progressBar height if we want text middle aligned */
            width: 0;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -webkit-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -moz-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -o-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            background: linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -o-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -moz-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -ms-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-gradient(
                    linear,
                    left top,
                    right top,
                    color-stop(0, rgb(255, 0, 0)),
                    color-stop(0.5, rgb(255, 255, 0)),
                    color-stop(1, rgb(16, 240, 0))
            );
            -webkit-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            -moz-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            border: 1px solid #4c8932;
        }

        #progress2 div {
            height: 100%;
            color: #000000;
            text-align: right;
            line-height: 22px; /* same as #progressBar height if we want text middle aligned */
            width: 0;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -webkit-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -moz-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -o-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            background: linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -o-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -moz-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -ms-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-gradient(
                    linear,
                    left top,
                    right top,
                    color-stop(0, rgb(255, 0, 0)),
                    color-stop(0.5, rgb(255, 255, 0)),
                    color-stop(1, rgb(16, 240, 0))
            );
            -webkit-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            -moz-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            border: 1px solid #4c8932;
        }

        #progress3 div {
            height: 100%;
            color: #000000;
            text-align: right;
            line-height: 22px; /* same as #progressBar height if we want text middle aligned */
            width: 0;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -webkit-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -moz-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -o-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            background: linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -o-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -moz-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -ms-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-gradient(
                    linear,
                    left top,
                    right top,
                    color-stop(0, rgb(255, 0, 0)),
                    color-stop(0.5, rgb(255, 255, 0)),
                    color-stop(1, rgb(16, 240, 0))
            );
            -webkit-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            -moz-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            border: 1px solid #4c8932;
        }

        #progress4 div {
            height: 100%;
            color: #000000;
            text-align: right;
            line-height: 22px; /* same as #progressBar height if we want text middle aligned */
            width: 0;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -webkit-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -moz-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -o-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            background: linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -o-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -moz-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -ms-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-gradient(
                    linear,
                    left top,
                    right top,
                    color-stop(0, rgb(255, 0, 0)),
                    color-stop(0.5, rgb(255, 255, 0)),
                    color-stop(1, rgb(16, 240, 0))
            );
            -webkit-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            -moz-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            border: 1px solid #4c8932;
        }

        #progress2 {
            width: 300px;
            height: 22px;
            color: #FFFFFF;
            text-align: right;
            border: 1px solid #111;
            background-color: #292929;
        }

        #progress3 {
            width: 300px;
            height: 22px;
            color: #FFFFFF;
            text-align: right;
            border: 1px solid #111;
            background-color: #292929;
        }

        #progress4 {
            width: 300px;
            height: 22px;
            color: #FFFFFF;
            text-align: right;
            border: 1px solid #111;
            background-color: #292929;
        }

        #container {
            margin: 0 auto;
            width: 460px;
            padding: 2em;
            background: #DCDDDF;

        }

        .ui-progress-bar {
            margin-top: 3em;
            margin-bottom: 3em;
        }

        .ui-progress span.ui-label {
            font-size: 1.2em;
            position: absolute;
            right: 0;
            line-height: 33px;
            padding-right: 12px;
            color: rgba(0, 0, 0, 0.6);
            text-shadow: rgba(255, 255, 255, 0.45) 0 1px 0px;
            white-space: nowrap;
        }

        .ui-progress {
            position: relative;
            display: block;
            height: 33px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            border-radius: 5px;
            -webkit-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -moz-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            -o-box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            box-shadow: 0 1px 5px #000 inset, 0 1px 0 #fff;
            background: linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -o-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -moz-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -ms-linear-gradient(left, rgb(255, 0, 0) 0%, rgb(255, 255, 0) 50%, rgb(16, 240, 0) 100%);
            background: -webkit-gradient(
                    linear,
                    left top,
                    right top,
                    color-stop(0, rgb(255, 0, 0)),
                    color-stop(0.5, rgb(255, 255, 0)),
                    color-stop(1, rgb(16, 240, 0))
            );
            -webkit-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            -moz-box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            box-shadow: inset 0px 1px 0px 0px #dbf383, inset 0px -1px 1px #58c43a;
            border: 1px solid #4c8932;
        }

        .progress4stripes {
            width: 100%;
            height: 100%;
            background-image: -webkit-repeating-linear-gradient(45deg, rgba(0, 0, 0, .08), rgba(0, 0, 0, .08) 35px, rgba(255, 255, 255, .05) 35px, rgba(255, 255, 255, .05) 70px);
            background-image: -moz-repeating-linear-gradient(45deg, rgba(0, 0, 0, .08), rgba(0, 0, 0, .08) 35px, rgba(255, 255, 255, .05) 35px, rgba(255, 255, 255, .05) 70px);
        }

        .red {
            background: red;
            background-image: radial-gradient(brown, transparent);
            background-size: 5px 5px;
            width: 100px;
            height: 100px;
            border-radius: 50%;
            position: absolute;
            top: 20px;
            left: 35px;
            animation: 1s red infinite;
            border: dotted 2px red;
            box-shadow: 0 0 20px #111 inset, 0 0 10px red;
        }

        .green {
            background: green;
            background-image: radial-gradient(lime, transparent);
            background-size: 5px 5px;
            width: 100px;
            height: 100px;
            border-radius: 50%;
            border: dotted 2px lime;
            position: absolute;
            top: 0px;
            left: 0px;
            box-shadow: 0 0 20px #111 inset,
            0 0 10px lime;
            animation: 1s green infinite;
        }


    </style>
    <script type="text/javascript">
        var imageGreen = './light_grn.JPG';
        var imageRed = './light_red.JPG';
        var imageYellow = './light_ylw.JPG';
        var webservicepath = '<%=request.getSession().getAttribute("localServerIp")%>';
        var delay = 5000;
        var soundDelay = 60000;
        var testwebservicepath1 = 'http://' + '<%=request.getRemoteHost()%>' + '/tos/rest';
        var waiting = 'WAITING';
        var ready = 'READY';
        var working = 'WORKING';
        var NA = 'NOT IN SERVICE';
        var cssWaitingLow = {"background-color": "rgba(255, 0, 0, 0.77)", "font-size": "100%"};
        var cssReady = {"background-color": "white", "font-size": "100%"};
        var cssWorking = {"background-color": "#ccf0c6", "font-size": "100%"};
        var cssNA = {"background-color": "yellow", "font-size": "100%"};
        var cssVisible = {/*"display": "block",*/ "visibility": "visible"};
        var cssHidden = {"display": "none"};
        var G = 'G';
        var Y = 'Y';
        var R = 'R';
        var playedLowOne = false;
        var playedLowTwo = false;
        var playedLowThree = false;
        var playedLowFour = false;
        var playedHighOne = false;
        var playedHighTwo = false;
        var playedHighThree = false;
        var playedHighFour = false;
        var highSoundTime;
        var cssRed = {
            "background": "red",
            "background-image": "radial-gradient(brown, transparent)",
            "background - size": " 5px 5px",
            "width": " 100px",
            "height": " 100px",
            "border-radius": " 50 % ",
            "position": " absolute",
            "top": " 20px",
            "left": " 35px",
            "animation": " 1s red infinite",
            "border": " dotted 2px red",
            "box-shadow": "0 0 20px #111 inset, 0 0 10px red"
        };
        var audiotypes = {
            "mp3": "audio/mpeg",
            "mp4": "audio/mp4",
            "ogg": "audio/ogg",
            "wav": "audio/wav"
        }
        var lowsound = ss_soundbits("./Store_Door.mp3", "./Store_Door.mp3");
        var highsound1 = ss_soundbits("./Ship_Bell.mp3", "./Ship_Bell.mp3");
        var highsound2 = ss_soundbits("./Ship_Bell.mp3", "./Ship_Bell.mp3");
        var highsound3 = ss_soundbits("./Ship_Bell.mp3", "./Ship_Bell.mp3");
        var highsound4 = ss_soundbits("./Ship_Bell.mp3", "./Ship_Bell.mp3");

        function setImages4( signal) {
            var lanegreen4 = document.getElementById("lanegreen4");
            var lanered4 = document.getElementById("lanered4");
            var laneoff4 = document.getElementById("laneoff4");
            if (signal === G) {
                lanegreen4.style.display = 'block';
                lanered4.style.display = 'none';
                laneoff4.style.display = 'none';
            } else if (signal === R) {
                lanegreen4.style.display = 'none';
                lanered4.style.display = 'block';
                laneoff4.style.display = 'none';
            } else if (signal === "O") {
                lanegreen4.style.display = 'none';
                lanered4.style.display = 'none';
                laneoff4.style.display = 'block';
            }
        }
        function setImages3( signal) {
            var lanegreen3 = document.getElementById("lanegreen3");
            var lanered3 = document.getElementById("lanered3");
            var laneoff3 = document.getElementById("laneoff3");
            if (signal === G) {
                lanegreen3.style.display = 'block';
                lanered3.style.display = 'none';
                laneoff3.style.display = 'none';
            } else if (signal === R) {
                lanegreen3.style.display = 'none';
                lanered3.style.display = 'block';
                laneoff3.style.display = 'none';
            } else if (signal === "O") {
                lanegreen3.style.display = 'none';
                lanered3.style.display = 'none';
                laneoff3.style.display = 'block';
            }
        }
        function setImages2( signal) {
            var lanegreen2 = document.getElementById("lanegreen2");
            var lanered2 = document.getElementById("lanered2");
            var laneoff2 = document.getElementById("laneoff2");
            if (signal === G) {
                lanegreen2.style.display = 'block';
                lanered2.style.display = 'none';
                laneoff2.style.display = 'none';
            } else if (signal === R) {
                lanegreen2.style.display = 'none';
                lanered2.style.display = 'block';
                laneoff2.style.display = 'none';
            } else if (signal === "O") {
                lanegreen2.style.display = 'none';
                lanered2.style.display = 'none';
                laneoff2.style.display = 'block';
            }
        }
        function setImages1( signal) {
            var lanegreen1 = document.getElementById("lanegreen1");
            var lanered1 = document.getElementById("lanered1");
            var laneoff1 = document.getElementById("laneoff1");
            if (signal === G) {
                lanegreen1.style.display = 'block';
                lanered1.style.display = 'none';
                laneoff1.style.display = 'none';
            } else if (signal === R) {
                lanegreen1.style.display = 'none';
                lanered1.style.display = 'block';
                laneoff1.style.display = 'none';
            } else if (signal === "O") {
                lanegreen1.style.display = 'none';
                lanered1.style.display = 'none';
                laneoff1.style.display = 'block';
            }
        }

        function ss_soundbits(sound) {
            var audio_element = document.createElement('audio')
            if (audio_element.canPlayType) {
                for (var i = 0; i < arguments.length; i++) {
                    var source_element = document.createElement('source')
                    source_element.setAttribute('src', arguments[i])
                    if (arguments[i].match(/\.(\w+)$/i))
                        source_element.setAttribute('type', audiotypes[RegExp.$1])
                    audio_element.appendChild(source_element)
                }
                audio_element.load()
                audio_element.playclip = function () {
                    audio_element.pause()
                    audio_element.currentTime = 0
                    audio_element.play()
                }
                return audio_element
            }
        }

        $.fn.equals = function (compareTo) {
            if (!compareTo || this.length != compareTo.length) {
                return false;
            }
            for (var i = 0; i < this.length; ++i) {
                if (this[i] !== compareTo[i]) {
                    return false;
                }
            }
            return true;
        };
        (function ($) {
            $.fn.animateProgress = function (progress, callback) {
                return this.each(function () {
                    $(this).animate({
                        width: progress + '%'
                    }, {
                        duration: 500,

                        easing: 'swing',

                        step: function (progress) {
                            var labelEl = $('.ui-label', this),
                                    valueEl = $('.value', labelEl);

                            if (Math.ceil(progress) < 20 && $('.ui-label', this).is(":visible")) {
                                labelEl.hide();
                            } else {
                                if (labelEl.is(":hidden")) {
                                    labelEl.fadeIn();
                                }
                                ;
                            }

                            if (Math.ceil(progress) == 100) {
                                labelEl.text('Done');
                                setTimeout(function () {
                                    labelEl.fadeOut();
                                }, 1000);
                            } else {
                                valueEl.text(Math.ceil(progress) + '%');
                            }
                        },
                        complete: function (scope, i, elem) {
                            if (callback) {
                                callback.call(this, i, elem);
                            }
                            ;
                        }
                    });
                });
            };

        })(jQuery);
        var animateprogress = function animateprogressbar(percent, progressbar, pagecapacity, pageremaining) {
            //$('.progressbar .ui-progress .ui-label').hide();
            $('.progressbar .ui-progress').css('width', '7%');

            $('.progressbar .ui-progress').animateProgress(percent, function () {
                $(this).animateProgress(percent, function () {
                    setTimeout(function () {
                        $('#progress_bar .ui-progress').animateProgress(percent, function () {
                            $('#main_content').slideDown();
                        });
                    }, 1000);
                });
            }).html(pageremaining + "/ " + pagecapacity);
            ;

        };
        var updateLane1Status = function refreshLane1Status() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/1',
                success: function (data) {
                    $("#queueStatus11").text(data.lanestatus);
                    if (!(data.workstation === null || data.workstation==="" || data.workstation==="Waiting"))
                        $("#workstation1").text('(' + data.workstation + ')');
                    else
                        $("#workstation1").text('');
                    var lastvalue = $("#queueStatus11").val();
                    $("#queueStatus11").val(data.lanestatus);

                    if (data.lanestatus === waiting) {
                        $('#row1col1').css(cssWaitingLow);
                        $('#row1col2').css(cssWaitingLow);
                        $('#row1col3').css(cssWaitingLow);
                        $('#row1col4').css(cssWaitingLow);
                        if (data.volume === "LOW" && !playedLowOne) {
                            lowsound.playclip();
                            playedLowOne = true;
                        }
                    } else if (data.lanestatus === ready) {
                        $('#row1col1').css(cssReady);
                        $('#row1col2').css(cssReady);
                        $('#row1col3').css(cssReady);
                        $('#row1col4').css(cssReady);
                        highsound1.pause();
                        playedLowOne = false;
                    } else if (data.lanestatus.indexOf(working) >= 0) {
                        $('#row1col1').css(cssWorking);
                        $('#row1col2').css(cssWorking);
                        $('#row1col3').css(cssWorking);
                        $('#row1col4').css(cssWorking);
                    } else if (data.lanestatus == NA) {
                        $('#row1col1').css(cssNA);
                        $('#row1col2').css(cssNA);
                        $('#row1col3').css(cssNA);
                        $('#row1col4').css(cssNA);
                    }
                }
            })
        }
        var updateLane2Status = function refreshLane2Status() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/2',
                success: function (data) {
                    $("#queueStatus12").text(data.lanestatus);
                    if (!(data.workstation === null || data.workstation==="" || data.workstation==="Waiting"))
                        $("#workstation2").text('(' + data.workstation + ')');
                    else
                        $("#workstation2").text('');
                    if (data.lanestatus === waiting) {
                        $('#row2col1').css(cssWaitingLow);
                        $('#row2col2').css(cssWaitingLow);
                        $('#row2col3').css(cssWaitingLow);
                        $('#row2col4').css(cssWaitingLow);
                        if (data.volume === "LOW" && !playedLowTwo) {
                           lowsound.playclip();
                           playedLowTwo = true;
                        }
                    } else if (data.lanestatus === ready) {
                        $('#row2col1').css(cssReady);
                        $('#row2col2').css(cssReady);
                        $('#row2col3').css(cssReady);
                        $('#row2col4').css(cssReady);
                        highsound2.pause();
                        playedLowTwo = false;
                    } else if (data.lanestatus.indexOf(working) >= 0) {
                        $('#row2col1').css(cssWorking);
                        $('#row2col2').css(cssWorking);
                        $('#row2col3').css(cssWorking);
                        $('#row2col4').css(cssWorking);
                    } else if (data.lanestatus == NA) {
                        $('#row2col1').css(cssNA);
                        $('#row2col2').css(cssNA);
                        $('#row2col3').css(cssNA);
                        $('#row2col4').css(cssNA);
                    }
                }
            })
        }
        var updateLane3Status = function refreshLane3Status() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/3',
                success: function (data) {
                    $("#queueStatus13").text(data.lanestatus);
                    if (!(data.workstation === null || data.workstation==="" || data.workstation==="Waiting"))
                        $("#workstation3").text('(' + data.workstation + ')');
                    else
                        $("#workstation3").text('');
                    if (data.lanestatus === waiting) {
                        $('#row3col1').css(cssWaitingLow);
                        $('#row3col2').css(cssWaitingLow);
                        $('#row3col3').css(cssWaitingLow);
                        $('#row3col4').css(cssWaitingLow);
                        if (data.volume === "LOW" && !playedLowThree) {
                            lowsound.playclip();
                            playedLowThree = true;
                        }
                    } else if (data.lanestatus === ready) {
                        $('#row3col1').css(cssReady);
                        $('#row3col2').css(cssReady);
                        $('#row3col3').css(cssReady);
                        $('#row3col4').css(cssReady);
                        highsound3.pause();
                        playedLowThree = false;
                    } else if (data.lanestatus.indexOf(working) >= 0) {
                        $('#row3col1').css(cssWorking);
                        $('#row3col2').css(cssWorking);
                        $('#row3col3').css(cssWorking);
                        $('#row3col4').css(cssWorking);
                    } else if (data.lanestatus == NA) {
                        $('#row3col1').css(cssNA);
                        $('#row3col2').css(cssNA);
                        $('#row3col3').css(cssNA);
                        $('#row3col4').css(cssNA);
                    }

                }
            })
        }
        var updateLane4Status = function refreshLane4Status() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/4',
                success: function (data) {
                    $("#queueStatus14").text(data.lanestatus);
                    if (!(data.workstation === null || data.workstation==="" || data.workstation==="Waiting"))
                        $("#workstation4").text('(' + data.workstation + ')');
                    else
                        $("#workstation4").text('');
                    if (data.lanestatus === waiting) {
                        $('#row4col1').css(cssWaitingLow);
                        $('#row4col2').css(cssWaitingLow);
                        $('#row4col3').css(cssWaitingLow);
                        $('#row4col4').css(cssWaitingLow);
                        if (data.volume === "LOW" && !playedLowFour) {
                            lowsound.playclip();
                            playedLowFour = true;
                        }
                    } else if (data.lanestatus === ready) {
                        $('#row4col1').css(cssReady);
                        $('#row4col2').css(cssReady);
                        $('#row4col3').css(cssReady);
                        $('#row4col4').css(cssReady);
                        playedLowFour = false;
                        highsound4.pause();
                    } else if (data.lanestatus.indexOf(working) >= 0) {
                        $('#row4col1').css(cssWorking);
                        $('#row4col2').css(cssWorking);
                        $('#row4col3').css(cssWorking);
                        $('#row4col4').css(cssWorking);
                    } else if (data.lanestatus == NA) {
                        $('#row4col1').css(cssNA);
                        $('#row4col2').css(cssNA);
                        $('#row4col3').css(cssNA);
                        $('#row4col4').css(cssNA);
                    }
                }
            })
        }

        var playHighSoundLane1 = function play4Lane1() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/1',
                success: function (data) {
                    if (data.lanestatus === waiting) {
                        if (data.volume === "HIGH") {
                            highsound1.onloadedmetadata = function() {
                                console.log("highsound1 loaded");
                                highsound1.playclip();
                            };
                            highsound1.playclip();
                        }
                    }
                }
            })
        }

        var playHighSoundLane2 = function play4Lane2() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/2',
                success: function (data) {
                    if (data.lanestatus === waiting) {
                        if (data.volume === "HIGH") {
                            highsound2.onloadedmetadata = function() {
                                console.log("highsound2 loaded");
                                highsound2.playclip();
                            };
                            highsound2.playclip();
                        }
                    }
                }
            })
        }

        var playHighSoundLane3 = function play4Lane3() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanestatus/3',
                success: function (data) {
                    if (data.lanestatus === waiting) {
                        if (data.volume === "HIGH") {
                            highsound3.onloadedmetadata = function() {
                                console.log("highsound3 loaded");
                                highsound3.playclip();
                            };
                            highsound3.playclip();
                        }
                    }
                }
            })
        }


         var playHighSoundLane4 = function play4Lane4() {
             $.ajax({
                 url: webservicepath + '/lanedetails/lanestatus/4',
                 success: function (data) {
                     if (data.lanestatus === waiting) {
                         if (data.volume === "HIGH") {
                            highsound4.onloadedmetadata = function() {
                                console.log("highsound4 loaded");
                                highsound4.playclip();
                            };
                            highsound4.playclip();
                         }
                     }
                 }
             })
         }


        var updateLane1WaitTime = function refreshLane1WaitTime() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanewaittime/1',
                success: function (data) {
                    $("#time11").text(data.lanewaittime);
                }
            })
        }
        var updateLane2WaitTime = function refreshLane2WaitTime() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanewaittime/2',
                success: function (data) {
                    $("#time12").text(data.lanewaittime);
                }
            })
        }
        var updateLane3WaitTime = function refreshLane3WaitTime() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanewaittime/3',
                success: function (data) {
                    $("#time13").text(data.lanewaittime);
                }
            })
        }
        var updateLane4WaitTime = function refreshLane4WaitTime() {
            $.ajax({
                url: webservicepath + '/lanedetails/lanewaittime/4',
                success: function (data) {
                    $("#time14").text(data.lanewaittime);
                }
            })
        }

        var updateLane1Others = function refreshLane1Others() {
            $.ajax({
                url: webservicepath + '/lanedetails/pages/1',
                success: function (data) {
                    $("#pageremaining11").text(data.pageremaining);
                    $("#pagecapacity11").text(data.pagecapacity);
                    var percentRemaining = (data.pageremaining / data.pagecapacity ) * 100;
                    progressWithData(percentRemaining, data.pageremaining, data.pagecapacity, $('#progress1'));
                    setImages1(data.lightstatus);
                }
            })
        }
        var updateLane2Others = function refreshLane2Others() {
            $.ajax({
                url: webservicepath+'/lanedetails/pages/2',
                success: function (data) {
                    $("#pageremaining12").text(data.pageremaining);
                    $("#pagecapacity12").text(data.pagecapacity);
                    var percentRemaining = (data.pageremaining / data.pagecapacity ) * 100;
                    progressWithData(percentRemaining, data.pageremaining, data.pagecapacity, $('#progress2'));
                    setImages2(data.lightstatus);
                }
            })
        }
        var updateLane3Others = function refreshLane3Others() {
            $.ajax({
                url: webservicepath + '/lanedetails/pages/3',
                success: function (data) {
                    $("#pageremaining13").text(data.pageremaining);
                    $("#pagecapacity13").text(data.pagecapacity);
                    var percentRemaining = (data.pageremaining / data.pagecapacity ) * 100;
                    progressWithData(percentRemaining, data.pageremaining, data.pagecapacity, $('#progress3'));
                    setImages3(data.lightstatus);
                }
            })
        }
        var updateLane4Others = function refreshLane4Others() {
            $.ajax({
                url: webservicepath + '/lanedetails/pages/4',
                success: function (data) {
                    $("#pageremaining14").text(data.pageremaining);
                    $("#pagecapacity14").text(data.pagecapacity);
                    var percentRemaining = (data.pageremaining / data.pagecapacity ) * 100;
                    progressWithData(percentRemaining, data.pageremaining, data.pagecapacity, $('#progress4'));
                    setImages4(data.lightstatus);
                }
            })
        }

        function progress(percent, $element) {
            var progressBarWidth = percent * $element.width() / 100;
            $element.find('div').animate({width: progressBarWidth}, 500).html(percent + "% ");
        }
        function progressWithData(percent, pageremaining, pagecapacity, $element) {
            var progressBarWidth = percent * $element.width() / 100;
            if (pageremaining < 100) {
                progressBarWidth = 12 * $element.width() / 100;
                $element.find('div').animate({width: progressBarWidth}, 500).html(pageremaining + "/ " + pagecapacity);
            }
            if (pageremaining >= 100)
                $element.find('div').animate({width: progressBarWidth}, 500).html(pageremaining + "/ " + pagecapacity);
        }

        setInterval(updateLane1Status, delay);
        setInterval(updateLane2Status, delay);
        setInterval(updateLane3Status, delay);
        setInterval(updateLane4Status, delay);
        setInterval(updateLane1WaitTime, delay);
        setInterval(updateLane2WaitTime, delay);
        setInterval(updateLane3WaitTime, delay);
        setInterval(updateLane4WaitTime, delay);
        setInterval(updateLane1Others, delay);
        setInterval(updateLane2Others, delay);
        setInterval(updateLane3Others, delay);
        setInterval(updateLane4Others, delay);
        setInterval(playHighSoundLane1, soundDelay);
        setInterval(playHighSoundLane2, soundDelay);
        setInterval(playHighSoundLane3, soundDelay);
        setInterval(playHighSoundLane4, soundDelay);

        $(updateLane1Status());
        $(updateLane2Status());
        $(updateLane3Status());
        $(updateLane4Status());
        $(updateLane1WaitTime());
        $(updateLane2WaitTime());
        $(updateLane3WaitTime());
        $(updateLane4WaitTime());
        $(updateLane1Others());
        $(updateLane2Others());
        $(updateLane3Others());
        $(updateLane4Others());
        $(playHighSoundLane1());
        $(playHighSoundLane2());
        $(playHighSoundLane3());
        $(playHighSoundLane4());

    </script>
    <script type="text/javascript">


        function reset1() {
            document.LaneLightManagerForm.submit();
        }


        function reset2() {
            document.LaneLightManagerForm.submit();
        }


        function reset3() {
            document.LaneLightManagerForm.submit();
        }


        function reset4() {
            document.LaneLightManagerForm.submit();
        }


        function RefreshMe() {
            var soundIdx
            var tmpSoundIdx
            var laneid
            var facilityID
            soundIdx = 0;
            tmpSoundIdx = 0;
            var Q_TIME_AN = '2:20';
            var Q_STATUS_IDX = 1;
            var time1 = '<%=request.getSession().getAttribute("time1")%>';
            var time2 = '<%=request.getSession().getAttribute("time2")%>';
            var time3 = '<%=request.getSession().getAttribute("time3")%>';
            var time4 = '<%=request.getSession().getAttribute("time4")%>';

            var status1 = '<%=request.getSession().getAttribute("queueStatus1")%>';
            var status2 = '<%=request.getSession().getAttribute("queueStatus2")%>';
            var status3 = '<%=request.getSession().getAttribute("queueStatus3")%>';
            var status4 = '<%=request.getSession().getAttribute("queueStatus4")%>';

            document.getElementById("soundeffect1").src = ""
            document.getElementById("soundeffect2").src = ""

            var facilityID = '<%=request.getSession().getAttribute("facilityID")%>';
            // var facilityID = '3';
            if (facilityID == '' || facilityID == 'null')
                return;

            if (facilityID == '3') {
                setLightStatus();

                var inlaneImg = document.getElementById('inlane-img');
                inlaneImg.src = 'http://<%= request.getSession().getAttribute("INLANE1") %>/axis-cgi/jpg/image.cgi?camera=1&compression=30&resolution=fullsize?' + new Date();

                var outlaneImg = document.getElementById('outlane-img');
                outlaneImg.src = 'http://<%= request.getSession().getAttribute("OUTLANE1") %>/axis-cgi/jpg/image.cgi?camera=1&compression=30&resolution=fullsize' + new Date();

            }

            //tmpSoundIdx = GetSoundIdx(laneid, Q_STATUS_IDX, Q_TIME_AN)
            if (status1 == 'READY')
                Q_STATUS_IDX = 0;
            else if (status1 == 'WAITING')
                Q_STATUS_IDX = 1;
            else
                Q_STATUS_IDX = 2;

            if (Q_STATUS_IDX != 0) {
                tmpSoundIdx = GetSoundIdx(laneid, Q_STATUS_IDX, time1)
                //alert (tmpSoundIdx)
                if (tmpSoundIdx > 0 && tmpSoundIdx != soundIdx) {
                    soundIdx += tmpSoundIdx;
                }
            }
            playSoundFile(tmpSoundIdx);
            soundIdx = 0;
            if (status2 == 'READY')
                Q_STATUS_IDX = 0;
            else if (status2 == 'WAITING')
                Q_STATUS_IDX = 1;
            else
                Q_STATUS_IDX = 2;

            if (Q_STATUS_IDX != 0) {

                tmpSoundIdx = GetSoundIdx(laneid, Q_STATUS_IDX, time2)
                //alert (tmpSoundIdx)
                if (tmpSoundIdx > 0 && tmpSoundIdx != soundIdx) {
                    soundIdx += tmpSoundIdx;
                }
            }
            playSoundFile(tmpSoundIdx);
            if (status3 == 'READY')
                Q_STATUS_IDX = 0;
            else if (status3 == 'WAITING')
                Q_STATUS_IDX = 1;
            else
                Q_STATUS_IDX = 2;

            if (Q_STATUS_IDX != 0) {

                tmpSoundIdx = GetSoundIdx(laneid, Q_STATUS_IDX, time3)
                //alert (tmpSoundIdx)
                if (tmpSoundIdx > 0 && tmpSoundIdx != soundIdx) {
                    soundIdx += tmpSoundIdx;
                }
            }
            playSoundFile(tmpSoundIdx);
            if (status4 == 'READY')
                Q_STATUS_IDX = 0;
            else if (status4 == 'WAITING')
                Q_STATUS_IDX = 1;
            else
                Q_STATUS_IDX = 2;

            if (Q_STATUS_IDX != 0) {

                tmpSoundIdx = GetSoundIdx(laneid, Q_STATUS_IDX, time4)
                //alert (tmpSoundIdx)
                if (tmpSoundIdx > 0 && tmpSoundIdx != soundIdx) {
                    soundIdx += tmpSoundIdx;
                }
            }
            playSoundFile(tmpSoundIdx);
        }

        function playSoundFile(soundId) {
            alert("play sound file : "+soundId);  //-- TODO: need to fix this numbering
            if (soundId == 1) {
                //playqsound('./Store_Door.mp3');
            }
            else if (soundId == 2) {
                //playqsound('./Store_Door.mp3');
                //window.setTimeout("playalertsound('./Ship_Bell.mp3')", 60000);
            }
            /*else if (soundId != 0) {
                //playalertsound('./alert.wav');
            }*/
        }
        function setLightStatus() {
            var lane1LightStatus = '<%= request.getSession().getAttribute("lightStatus1") %>';
            var lane1LightElem = document.getElementById("lane1LightImg");
            if (lane1LightElem) {
                if (lane1LightStatus == 'G') {
                    lane1LightElem.src = "/light_grn.JPG";
                }
                else if (lane1LightStatus == 'R') {
                    lane1LightElem.src = "light_red.JPG";
                }
                else if (lane1LightStatus == 'Y') {
                    lane1LightElem.src = "light_ylw.JPG";
                }
                else {
                    lane1LightElem.src = "";
                }
            }

            var lane2LightStatus = '<%= request.getSession().getAttribute("lightStatus2") %>';
            var lane2LightElem = document.getElementById("lane2LightImg");
            if (lane2LightElem) {
                if (lane2LightStatus == 'G') {
                    lane2LightElem.src = "./tos/light_grn.JPG"
                }
                else if (lane2LightStatus == 'R') {
                    lane2LightElem.src = "light_red.JPG";
                }
                else if (lane2LightStatus == 'Y') {
                    lane2LightElem.src = "light_ylw.JPG";
                }
                else {
                    lane2LightElem.src = "";
                }
            }

            var lane3LightStatus = '<%= request.getSession().getAttribute("lightStatus3") %>';
            var lane3LightElem = document.getElementById("lane3LightImg");
            if (lane3LightElem) {
                if (lane3LightStatus == 'G') {
                    lane3LightElem.src = "/tos/light_grn.JPG";
                }
                else if (lane3LightStatus == 'R') {
                    lane3LightElem.src = "light_red.JPG";
                }
                else if (lane3LightStatus == 'Y') {
                    lane3LightElem.src = "light_ylw.JPG";
                }
                else {
                    lane3LightElem.src = "";
                }
            }

            var lane4LightStatus = '<%= request.getSession().getAttribute("lightStatus4") %>';
            var lane4LightElem = document.getElementById("lane4LightImg");
            if (lane4LightElem) {
                if (lane4LightStatus == 'G') {
                    lane4LightElem.src = "./light_grn.JPG";
                }
                else if (lane4LightStatus == 'R') {
                    lane4LightElem.src = "light_red.JPG";
                }
                else if (lane4LightStatus == 'Y') {
                    lane4LightElem.src = "light_ylw.JPG";
                }
                else {
                    lane4LightElem.src = "";
                }
            }


        }

        //setInterval(RefreshMe,1000);

        function playqsound(soundfile) {

            document.getElementById("soundeffect1").src = "" //reset first in case of problems
            document.getElementById("soundeffect1").src = soundfile
        }

        function playalertsound(soundfile) {
            //var audio = new Audio('./alert.wav');
            //audio.play();

            document.getElementById("soundeffect2").src = "" //reset first in case of problems
            document.getElementById("soundeffect2").src = soundfile
        }

        function GetSoundIdx(LaneID, QStatusIdx, QTime) {
            alert('LaneID=' +LaneID+', QStatusIdx='+QStatusIdx+', QTime='+QTime)
            var sndIdx;
            var mins;
            var secs;
            mins = Minutes(QTime);
            secs = Seconds(QTime);
            sndIdx = 0;

            if ((QStatusIdx == "1") && (mins == 0) && (secs >= 0) && (secs < 7)) {
                sndIdx = 1;	//status = waiting, and 0-1 mins, play in q sound once
            }
            else if ((QStatusIdx == "1") && (mins >= 2) && (secs >= 0) && (secs < 7)) {
                sndIdx = 2; //status = waiting, and 2+ mins, play klaxon sound once per min
            }
            else if ((QStatusIdx == "2") && (mins = 5) && (secs >= 0) && (secs < 7)) {
                sndIdx = 0; //status = working, and 5 min mark, then play klaxon sound once
            }

            return sndIdx;

        }

        //Used to get the correct background color for the q time panel based on q time & q status during AJAX refresh
        function GetQColor(QStatusIdx, QTime) {
            var mins;
            mins = Minutes(QTime);

            //alert(mins)
            if (QStatusIdx == "0") {
                return "white";
            }
            else if (QStatusIdx == "1" && mins == 0) {
                return "green";
            }
            else if (QStatusIdx == "1" && mins == 1) {
                return "yellow";
            }
            else if (QStatusIdx == "1" && mins > 1) {
                return "red";
            }
            else if (QStatusIdx == "2" && mins >= 0 && mins < 2) {
                return "green";
            }
            else if (QStatusIdx == "2" && mins >= 2 && mins < 5) {
                return "yellow";
            }
            else if (QStatusIdx == "2" && mins >= 5) {
                return "red";
            }
            else {
                return "white";
            }
        }

        function Minutes(data) {

            for (var i = 0; i < data.length; i++) if (data.substring(i, i + 1) == ":") break;

            return (data.substring(0, i));
        }

        function Seconds(data) {

            for (var i = 0; i < data.length; i++) if (data.substring(i, i + 1) == ":") break;

            return (data.substring(i + 1, data.length));
        }
    </script>
</head>

<%
    boolean showRearCamera = false;
    String facilityID = (String) request.getSession().getAttribute("facilityID");
    if (facilityID != null && !"3".equals(facilityID)) {
        showRearCamera = true;
    }
%>

<!-- <body onload="javascript:setLightStatus();"> -->

<body <%--onload="window.setTimeout(RefreshMe,10000);"--%>>

<form id="form" name="LaneLightManagerForm" action="LaneManager" method="post">

    <table id="lanemanagertable" align="center" border="0" cellpadding="10" width="70%">
        <tr align="center">
            <% if (!showRearCamera) { %>
            <th align="left">IN LANES</th>
            <% } %>
            <% if (showRearCamera) { %>
            <th align="left">REAR CAMERA</th>
            <% } %>
            <th align="center">LANE</th>
            <th align="center">QUEUE</th>
            <th align="center">PRINTER</th>
            <th align="center">LIGHT</th>
        </tr>

        <!---------------------  LANE 1 ------------------>

        <tr id="row1" align="center">
            <!-- <% if (!showRearCamera) { %><td rowspan="2" align="left"><img src="" alt="matson" width="98%" id="inlane-img"></td><% } %> -->

            <% if (!showRearCamera) { %>
            <td rowspan="2" align="left"><img src="" alt="matson" width="520" HEIGHT="400" id="inlane-img"></td>
            <% } %>

            <% if (showRearCamera) { %>
            <td align="left"><img src="" alt="matson" width="50" height="50" id="imgCamera1"></td>
            <% } %>
            <td id="row1col1">ONE</td>
            <td id="row1col2">
                <p id="queueStatus11"></p><br>

                <p id="workstation1"></p><br>
                <p id="time11"></p>
            </td>
            <td id="row1col3">
                <div id="progress1" align="left">
                    <div align="left"></div>
                </div>
                <br>
                <input type="submit" name="lane1" value="Reset" onclick="reset1()">
                <br>
            </td>

            <td>
                <div id="lanegreen1">
                    <img alt="" src="light_grn.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="lanered1">
                    <img alt="" src="light_red.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="laneoff1">
                    <img alt="" src="light_OFF.JPG" width="50" height="50" border="0"/>
                </div>
            </td>
        </tr>


        <!---------------------  LANE 2 ------------------>


        <tr id="row2" align="center">
            <% if (showRearCamera) { %>
            <td align="left"><img src="" alt="matson" width="50" height="50" id="imgCamera2"></td>
            <% } %>
            <td id="row2col1">TWO</td>
            <td id="row2col2"><p id="queueStatus12"></p><br>

                <p id="workstation2"></p><br>
                <p id="time12"></p>
            </td>
            <td id="row2col3">
                <div id="progress2" align="left">
                    <div align="left"></div>
                </div>
                <br>
                <input type="submit" name="lane2" value="Reset" onclick="reset2()">
            </td>

            <td>
                <div id="lanegreen2">
                    <img alt="" src="light_grn.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="lanered2">
                    <img alt="" src="light_red.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="laneoff2">
                    <img alt="" src="light_OFF.JPG" width="50" height="50" border="0"/>
                </div>
            </td>
        </tr>


        <!---------------------  LANE 3 ------------------>


        <tr id="row3" align="center">
            <% if (!showRearCamera) { %>
            <td rowspan="2" align="left"><p style="font-weight: bold;">OUT LANES</p>
            <img src="" alt="matson" WIDTH="520" HEIGHT="400" id="outlane-img"></td>
            <% } %>
            <% if (showRearCamera) { %>
            <td align="left"><img src="" alt="matson" width="98%" id="imgCamera3"></td>
            <% } %>

            <td id="row3col1">THREE</td>
            <td id="row3col2"><p id="queueStatus13"></p><br>

                <p id="workstation3"></p><br>
                <p id="time13"></p>
            </td>
            <td id="row3col3">
                <div id="progress3" align="left">
                    <div align="left"></div>
                </div>
                <br>
                <input type="submit" name="lane3" value="Reset" onclick="reset3()">
            </td>
            <td>
                <div id="lanegreen3">
                    <img alt="" src="light_grn.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="lanered3">
                    <img alt="" src="light_red.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="laneoff3">
                    <img alt="" src="light_OFF.JPG" width="50" height="50" border="0"/>
                </div>
            </td>
        </tr>


        <!---------------------  LANE 4 ------------------>


        <tr id="row4" align="center">
            <% if (showRearCamera) { %>
            <td align="left"><img src="" alt="matson" width="50" height="50" id="imgCamera4"></td>
            <% } %>
            <td id="row4col1">FOUR</td>
            <td id="row4col2"><p id="queueStatus14"></p><br>

                <p id="workstation4"></p><br>
                <p id="time14"></p>
            </td>
            <td id="row4col3">
                <div id="progress4" align="left">
                    <div align="left">
                    </div>
                </div>
                <br>
                <input type="submit" name="lane4" value="Reset" onclick="reset4()">
            <td>
                <div id="lanegreen4">
                    <img alt="" src="light_grn.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="lanered4">
                    <img alt="" src="light_red.JPG" width="50" height="50" border="0"/>
                </div>
                <div id="laneoff4">
                    <img alt="" src="light_OFF.JPG" width="50" height="50" border="0"/>
                </div>
            </td>
        </tr>

    </table>
</form>
</body>
</html>

