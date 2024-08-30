using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.UI;

public class PlayerMovementDropdown : MonoBehaviour
{
    public TMPro.TMP_Dropdown player1;
    public TMPro.TMP_Dropdown player2;

    public void ChangeDropdown(TMPro.TMP_Dropdown dropdown) {
        if (dropdown == player1) {
            if (dropdown.value == 0) {
                player2.value = 1;
            }
            else {
                player2.value = 0;
            }
        }
        else {
            if (dropdown.value == 0) {
                player1.value = 1;
            }
            else {
                player1.value = 0;
            }
        }
    }
}
