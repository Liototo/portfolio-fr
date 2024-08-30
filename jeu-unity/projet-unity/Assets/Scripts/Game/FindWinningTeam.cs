using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;

public class FindWinningTeam : MonoBehaviour
{
    private TextMeshProUGUI text;

    public GameObject bluePlayer;
    public GameObject pinkPlayer;

    private static Color BLUE_COLOR = new Color(0f, 0f, 1.0f, 1.0f);
    private static Color PINK_COLOR = new Color(1.0f, 0.41f, 0.71f, 1.0f);
    private static Color WHITE_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);

    // Start is called before the first frame update
    void Awake()
    {
        text = GetComponent<TextMeshProUGUI>();
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    void OnEnable() 
    {
        int bluePoints = bluePlayer.GetComponent<MoveWithKeyboardBehavior>().points;
        int pinkPoints = pinkPlayer.GetComponent<MoveWithKeyboardBehavior>().points;

        if (bluePoints > pinkPoints) {
            text.color = BLUE_COLOR;
            text.text = "TEAM BLUE";
        } else if (pinkPoints > bluePoints) {
            text.color = PINK_COLOR;
            text.text = "TEAM PINK";
        } else {
            text.color = WHITE_COLOR;
            text.text = "BOTH";
        }
    }
}
