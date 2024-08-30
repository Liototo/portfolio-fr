using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class ItemBox : MonoBehaviour
{
    public GameObject player;

    public GameObject green;
    public GameObject pink;
    public GameObject gold;

    // Start is called before the first frame update
    void Start()
    {
        
    }

    // Update is called once per frame
    void Update()
    {
        UpdateBox();
    }

    void UpdateBox()
    {
        switch (player.GetComponent<HoldingBehavior>().held)
        {
            case Item.green:
                green.SetActive(true);
                pink.SetActive(false);
                gold.SetActive(false);
                break;
            case Item.pink:
                green.SetActive(false);
                pink.SetActive(true);
                gold.SetActive(false);
                break;
            case Item.gold:
                green.SetActive(false);
                pink.SetActive(false);
                gold.SetActive(true);
                break;
            default:
                green.SetActive(false);
                pink.SetActive(false);
                gold.SetActive(false);
                break;
        }
    }

}
