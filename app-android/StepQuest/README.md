# StepQuest

## App Architecture
![app architechture](images/APP_architecture_v2.png)

Figma project : https://www.figma.com/file/r8L5v1avlD0mruSlr6Akvt/StepQuest---Figma?type=design&node-id=0%3A1&mode=design&t=WWJ6baSKShap6a4s-1

### Map
Add Google map api key to local.properties file
```
MAPS_API_KEY=YOUR_API_KEY
```

### Maps Search
Go to https://console.cloud.google.com/. Enable the places API and Google Maps SDK for Android.
Maps search uses Google Places API, requiring building the Build.Config file.
In case there is an error indicating that Build.Config could not be found/resolved, go to Build -> Rebuild Project and it will be generated.


