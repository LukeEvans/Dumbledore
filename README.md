Dumbledore - Prime API 2.0
==========

#### URL: http://ec2-23-22-89-44.compute-1.amazonaws.com:8080/


# ![Screenshot](http://static.fjcdn.com/gifs/Every_d28684_2645501.gif)



#####Notification Cards

######Endpoint: /notifications
######Request Type: POST
######Request Parameters: 
 - timezone_offset: Timezone Offset value
 - udid: User ID
 - lat: Latitude Value
 - long: Longitude Value
 - dev: Development Flag (All cards included in response)
 - service_request: Array of Services to include with ids of response cards to ommit
   - id: Service ID
   - cards: Array of card IDs to remove from response

######Sample Request:
```json
{
    "timezone_offset": "-25200",
    "udid": "SAMPLE_UDID",
    "lat": 40.016215,
    "long": -105.269146,
    "dev": false,
    "service_request":[
      {
        "id":"facebook_notifications",
        "cards": [
          "card-id-1",
          "card-id-2"
        ]
      },
      {
        "id":"facebook_birthdays",
        "cards": null
      },
      {
        "id":"facebook_messages",
        "cards": null
      },
      {
        "id":"nearby_places",
        "cards": null
      },
      {
        "id":"stocks",
        "cards": null
      },
      {
        "id":"weather",
        "cards": null
      },
      {
        "id":"nearby_photos",
        "cards": null
      }
    ]
}
```

######Schedule:
https://docs.google.com/a/winstonnetwork.com/spreadsheet/ccc?key=0AiBnAs7_nHDHdHlxZ2pNVW9TOW80N00zaGx0VUlmZVE&usp=drive_web#gid=0

######Cards: 
 - Weather
   - forecast - ID: "weather"
 - Facebook
   - Messages - ID: "facebook_messages"
   - Notifications - ID: "facebook_notifications"
   - Birthdays - ID: "facebook_birthdays"
 - Nearby
   - Photographs - ID: "nearby_photos"
   - Locations - ID: "nearby_places"
 - Stocks
   - value updates - ID: "stocks"
 - Traffic
   - drive times *Not yet implemented

   
#####Legacy Spring Endpoints

######* All existing endpoints
