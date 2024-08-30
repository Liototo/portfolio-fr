using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Pause : MonoBehaviour
{
    GameObject menu;

    private bool hasStarted;

    // Start is called before the first frame update
    void Start()
    {
        menu = GameObject.Find("PauseMenu");
        menu.SetActive(false);
        hasStarted = false;
    }

    public void startGame() 
    {
        hasStarted = true;
    }

    // Update is called once per frame

    void Update()
    {
        // Don't do anything if the game hasn't started
        if (!hasStarted) 
        {
            return;
        }
        
        if (Input.GetKeyDown(KeyCode.Escape))
        {
            GameManager.PauseGame();
            if (menu.activeSelf) menu.SetActive(false);
            else menu.SetActive(true);
        }
    }
}
