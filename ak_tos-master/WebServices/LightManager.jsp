<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <title>Matson Navigation Company</title>
    <LINK href="./overhead.css" type="text/css" rel="stylesheet">

    <script type="text/javascript">


        function validateForm(btn)
        {
            //alert(btnSelected)
            console.log("triggered submit");
            document.LaneLightManagerForm1.submit();

        }

        function SetLightImage(laneID, lamp)
        {
            var imgSrc;

            switch (lamp)
            {
                case "GREEN":
                    imgSrc = "light_grn.JPG";
                    break;
                case "RED":
                    imgSrc = "light_red.JPG";
                    break;
                case "OFF":
                    imgSrc = "light_OFF.JPG";
                    break;
            }

            if (!imgSrc)
                return;

            var imgElem = document.getElementById('laneLightImg' + laneID);
            imgElem.src = imgSrc;

            // Update value for lane parameter
            var radioElemId = 'lane' + laneID + lamp + 'RB';
            var radioElem = document.getElementById(radioElemId);
            if (radioElem) {

                switch (laneID)
                {
                    case 1:
                        document.getElementById('lane'+laneID).value = radioElem.value;
                        break;
                    case 2:
                        document.getElementById('lane'+laneID).value = radioElem.value;
                        break;
                    case 3:
                        document.getElementById('lane'+laneID).value = radioElem.value;
                        break;
                    case 4:
                        document.getElementById('lane'+laneID).value = radioElem.value;
                        break;
                }
            }

            //alert(document.getElementById('lane').value);
        }

        function initializeLaneLights() {

            var laneLight1 = '<%= request.getSession().getAttribute("laneLight1") %>';
            if (laneLight1 == 'G') {
                SetLightImage(1, 'GREEN');
                checkRadioButton(1, 'GREEN');
            }
            else if (laneLight1 == 'R') {
                SetLightImage(1, 'RED');
                checkRadioButton(1, 'RED');
            }
            else if (laneLight1 == 'O') {
                SetLightImage(1, 'OFF');
                checkRadioButton(1, 'OFF');
            }

            var laneLight2 = '<%= request.getSession().getAttribute("laneLight2") %>';
            if (laneLight2 == 'G') {
                SetLightImage(2, 'GREEN');
                checkRadioButton(2, 'GREEN');
            }
            else if (laneLight2 == 'R') {
                SetLightImage(2, 'RED');
                checkRadioButton(2, 'RED');
            }
            else if (laneLight2 == 'O') {
                SetLightImage(2, 'OFF');
                checkRadioButton(2, 'OFF');
            }

            var laneLight3 = '<%= request.getSession().getAttribute("laneLight3") %>';
            if (laneLight3 == 'G') {
                SetLightImage(3, 'GREEN');
                checkRadioButton(3, 'GREEN');
            }
            else if (laneLight3 == 'R') {
                SetLightImage(3, 'RED');
                checkRadioButton(3, 'RED');
            }
            else if (laneLight3 == 'O') {
                SetLightImage(3, 'OFF');
                checkRadioButton(3, 'OFF');
            }

            var laneLight4 = '<%= request.getSession().getAttribute("laneLight4") %>';
            if (laneLight4 == 'G') {
                SetLightImage(4, 'GREEN');
                checkRadioButton(4, 'GREEN');
            }
            else if (laneLight4 == 'R') {
                SetLightImage(4, 'RED');
                checkRadioButton(4, 'RED');
            }
            else if (laneLight4 == 'O') {
                SetLightImage(4, 'OFF');
                checkRadioButton(4, 'OFF');
            }
        }

        function checkRadioButton(laneID, lamp) {
            var radioElemId = 'lane' + laneID + lamp + 'RB';
            //alert(radioElemId);
            var radioElem = document.getElementById(radioElemId);
            if (radioElem) {
                //alert('checking radio button ' + radioElemId);
                radioElem.checked = true;
            }
        }
        function setAllGreen() {
            document.getElementById("lane1GREENRB").checked = true;
            document.getElementById("lane2GREENRB").checked = true;
            document.getElementById("lane3GREENRB").checked = true;
            document.getElementById("lane4GREENRB").checked = true;
            SetLightImage(1, 'GREEN');
            SetLightImage(2, 'GREEN');
            SetLightImage(3, 'GREEN');
            SetLightImage(4, 'GREEN');
        }

        function setAllRed() {
            document.getElementById("lane1REDRB").checked = true;
            document.getElementById("lane2REDRB").checked = true;
            document.getElementById("lane3REDRB").checked = true;
            document.getElementById("lane4REDRB").checked = true;
            SetLightImage(1, 'RED');
            SetLightImage(2, 'RED');
            SetLightImage(3, 'RED');
            SetLightImage(4, 'RED');
        }

        function setAllOff() {
            document.getElementById("lane1OFFRB").checked = true;
            document.getElementById("lane2OFFRB").checked = true;
            document.getElementById("lane3OFFRB").checked = true;
            document.getElementById("lane4OFFRB").checked = true;
            SetLightImage(1, 'OFF');
            SetLightImage(2, 'OFF');
            SetLightImage(3, 'OFF');
            SetLightImage(4, 'OFF');
        }
    </script>
</head>


<body onload="initializeLaneLights();">
<table border="0" cellpadding="0" cellspacing="0" width="430" align="center">
    <tr bgcolor="#275da7" width=50%>
        <td>
            &nbsp;&nbsp;<img src="MATSONBLUE_LO.jpg" width="150" />
        </td>
        <td style="color:#FFFFFF">
            <h2>Gate-Light Manager</h2>
        </td>
    </tr>
</table><br />
<form id="form" name="LaneLightManagerForm1" action="LightManager" method="post">

    <input type="hidden" name="lane1" id="lane1" />
    <input type="hidden" name="lane2" id="lane2" />
    <input type="hidden" name="lane3" id="lane3" />
    <input type="hidden" name="lane4" id="lane4" />

    <div id="ColorBox_container" style="width: 100%;">
        <div class="Your_orders" style=" height:25px; width: 100%; border: solid; background-color: #275da7; text-align: center;color: white;">ANC Gate-Light Manager
            <img style="float:right;" src="off_all.PNG" title="Set All Lights Off" onclick="setAllOff()">
            <img style="float:right;" src="red_all.PNG" title="Set All Lights Red" onclick="setAllRed()">
            <img style="float:right;" src="green_all.PNG" title="Set All Lights Green" onclick="setAllGreen()">
        </div>


        <table style="width:100%;margin-left: 5px;">


            <tr>
                <th style="width: 120px;">LANE</th>
                <th style="width: 140px;" >LIGHT</th>
                <th>CONTROL</th>
            </tr>

            <!------------------------------------ LANE1 ----------------------------->

            <tr>
                <td align="center">ONE</td>
                <td align="center"><img src="" id="laneLightImg1"></td>
                <td align="center">
                    <input type="radio" name="lane1Light" value="G1" onchange="SetLightImage(1, 'GREEN');" id="lane1GREENRB">Green</input>
                    <input type="radio" name="lane1Light" value="R1" onchange="SetLightImage(1, 'RED');" id="lane1REDRB">Red</input>
                    <input type="radio" name="lane1Light" value="O1" onchange="SetLightImage(1, 'OFF');" id="lane1OFFRB">Off </input>
                </td>
            </tr>

            <!----------------------------------------------- LNAE 2 ------------------------------------------------>


            <tr>
                <td align="center">TWO</td>
                <td align="center"><img src="" id="laneLightImg2" ></td>
                <td align="center">
                    <input type="radio" name="lane2Light" value="G2" onchange="SetLightImage(2, 'GREEN');" id="lane2GREENRB">Green</input>
                    <input type="radio" name="lane2Light" value="R2" onchange="SetLightImage(2, 'RED');" id="lane2REDRB">Red</input>
                    <input type="radio" name="lane2Light" value="O2" onchange="SetLightImage(2, 'OFF');" id="lane2OFFRB">Off </input>
                </td>
            </tr>


            <!---------------------------------------------------  LANE3 ------------------------------------------------->

            <tr>
                <td align="center">THREE</td>
                <td align="center"><img src="" id="laneLightImg3" ></td>
                <td align="center">
                    <input type="radio" name="lane3Light" value="G3" onchange="SetLightImage(3, 'GREEN');" id="lane3GREENRB">Green</input>
                    <input type="radio" name="lane3Light" value="R3" onchange="SetLightImage(3, 'RED');" id="lane3REDRB">Red</input>
                    <input type="radio" name="lane3Light" value="O3" onchange="SetLightImage(3, 'OFF');" id="lane3OFFRB">Off </input>
                </td>
            </tr>


            <!------------------------------------------------- LANE4 -------------------------------------------------->

            <tr>
                <td align="center">FOUR</td>
                <td align="center"><img src="" id="laneLightImg4" ></td>
                <td align="center">
                    <input type="radio" name="lane4Light" value="G4" onchange="SetLightImage(4, 'GREEN');" id="lane4GREENRB">Green</input>
                    <input type="radio" name="lane4Light" value="R4" onchange="SetLightImage(4, 'RED');" id="lane4REDRB">Red</input>
                    <input type="radio" name="lane4Light" value="O4" onchange="SetLightImage(4, 'OFF');" id="lane4OFFRB">Off </input>

                </td>
            </tr>
            <tr>
            <td/>
            <td/>
                <td align="center"><input type="submit" value="SUBMIT" onclick="validateForm(this)"></td>
            </tr>
        </table>

        <div class="Your_orders" style="height:15px;  width: 100%; border: solid; background-color: #275da7; text-align: center;color: white;  font-size: 10px;">Matson Alaska</div>
    </div>

</form>
</body>
</html>
