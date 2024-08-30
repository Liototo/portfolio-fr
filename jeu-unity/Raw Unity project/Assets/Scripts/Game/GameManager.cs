using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using TMPro;
using UnityEngine.SceneManagement;

public class GameManager : MonoBehaviour
{
    // Start is called before the first frame update
    void Start()
    {
        Time.timeScale = 0;
    }

    public void StartGame() {
        Time.timeScale = 1;
    }

    public void RestartGame() {
        Time.timeScale = 1;
        SceneManager.LoadScene("WelcomeScene");
    }

    // Update is called once per frame
    void Update()
    {
                
    }

    public static void PauseGame()
    {
        if (Time.timeScale == 0) Time.timeScale = 1;
        else Time.timeScale = 0;
    }



}
