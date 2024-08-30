using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.SceneManagement;

public class MainMenu : MonoBehaviour
{
    public void PlayGame()
    {
        SceneManager.LoadScene("GameScene");
    }

    public void PlayMaze()
    {
        SceneManager.LoadScene("MazeGameScene");
    }

    public void PlayHardMode()
    {
        SceneManager.LoadScene("HardGameScene");
    }

    public void QuitGame()
    {
        Application.Quit();
        Debug.Log("Quit!");
    }
    public void BackToMenu()
    {
        SceneManager.LoadScene(SceneManager.GetActiveScene().buildIndex - 1);
    }
}
