using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class LongTouchManager : MonoBehaviour
{
    private int longTouches;
    public GameManager manager;
    public GameObject startButton;
    public GameObject customButton;
    public GameObject celluloManager;


    // Start is called before the first frame update
    void Start()
    {
        longTouches = 0;
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    public void addLongTouch()
    {
        longTouches++;
        if (longTouches >= 2)
        {
            manager.StartGame();
            startButton.SetActive(false);
            customButton.SetActive(false);
            celluloManager.SetActive(false);
        }
    }
}
