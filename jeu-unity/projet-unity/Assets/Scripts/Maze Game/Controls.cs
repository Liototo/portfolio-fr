using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using System;

public class Controls : MonoBehaviour {

    public TextMeshProUGUI player1;
    public TextMeshProUGUI player2;

    public TMPro.TMP_Dropdown movementDropdown1;
    public TMPro.TMP_Dropdown colorDropdown1;
    public TMPro.TMP_Dropdown colorDropdown2;

    private string movement1;
    private string movement2;
    private string color1;
    private string color2;

    public enum PlayerColor {
        magenta = 0,
        blue = 1,
        yellow = 2,
        cyan = 3,
        black = 4
    }

    public void setControls () {
        if (movementDropdown1.value == 0) {
            movement1 = "Arrows";
            movement2 = "WASD";
        } else {
            movement1 = "WASD";
            movement2 = "Arrows";
        }

        color1 = Enum.GetName(typeof(PlayerColor), colorDropdown1.value);
        color2 = Enum.GetName(typeof(PlayerColor), colorDropdown2.value);

        player1.text = "Top left corner\nMovement: "+movement1+"\nColor: " + color1;
        player2.text = "Bottom right corner\nMovement: "+movement2+"\nColor: " + color2;
    }


}