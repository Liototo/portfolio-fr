using UnityEngine;
using UnityEngine.UI;
using TMPro;

/**
	This class is the implementation of the timer used in the game and how it is handled in it
*/
public class Timer : MonoBehaviour
{
    public GameObject endScreen;
    public Slider displaySlider;
    public static float test;
    private TextMeshProUGUI timerText;
    public float maxMinutes;
    private float timeSpent;

    private bool gameOver;

    // Start is called before the first frame update
    public void Start() {
        gameOver = false;
        timerText = GetComponent<TextMeshProUGUI>();
        setMaxMinutes(maxMinutes);
    }

    // Update is called once per frame
    public void Update() {
        if (gameOver) {
            return;
        }
        if (timeSpent < maxMinutes) {
            timeSpent += Time.deltaTime;
            DisplayTime(timeSpent);
        } else {
            gameOver = true;
            Time.timeScale = 0;
            DisplayEnd();
        }
    }

    void DisplayEnd() {
        endScreen.SetActive(true);
    }

    void DisplayTime(float timeToDisplay) {
        displaySlider.value = timeToDisplay;
        float minutes = Mathf.FloorToInt(timeToDisplay / 60);
        float seconds = Mathf.FloorToInt(timeToDisplay % 60);

        string timeString = string.Format("{0:00}:{1:00}", minutes, seconds);

        timerText.text = "TIME " + timeString;
    }

    public void setMaxMinutes(float minutes) {
        maxMinutes = minutes * 60;
        displaySlider.minValue = 0;
        displaySlider.maxValue = maxMinutes;
        timeSpent = 0;
        DisplayTime(timeSpent);
    }
}
