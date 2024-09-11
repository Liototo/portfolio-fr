using System.Collections;
using System.Collections.Generic;
using UnityEngine;

// The behaviour of a player holding an item
public class HoldingBehavior : MonoBehaviour
{

    public Item held;
    private bool cleared;
    public GameObject checkMark;

    // Start is called before the first frame update
    void Start()
    {
        held = Item.none;
    }

    // Update is called once per frame
    void Update()
    {
        
    }

    // Make the player grab an item
    public void GrabItem(Item i, bool sound)
    {
        held = i;
        if (sound) playSound("GemCollected");
    }

    // Make the player drop their item and give them points
    public void DropItem()
    {
        if (!cleared) return;
        switch(held)
        {
            case Item.green:
                addPoints(1);
                playSound("winPoint");
                break;
            case Item.pink:
                addPoints(2);
                playSound("winPoint");
                break;
            case Item.gold:
                addPoints(3);
                playSound("winPoint");
                break;
            default:
                break;
        }
        held = Item.none;
        setClear(false);
    }

    // Give points to the player
    public void addPoints(int i)
    {
        for (int j = 1; j <= i; ++j)
            gameObject.GetComponent<Movement>().addPoint();
    }

    public void setClear(bool clear)
    {
        cleared = clear;
        checkMark.SetActive(clear);
    }

    public bool isCleared() { return cleared; }

    public void playSound(string sound)
    {
        AudioSource audio = gameObject.AddComponent<AudioSource>();
        audio.PlayOneShot((AudioClip)Resources.Load(sound));
    }
}
