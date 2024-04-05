# API contract
```json
// Get location
GET /v1/api/locations?lat=89.898&long=123.43&radius=50&max_count=30

HTTP 1.1 Response Code: 200

{
  "locations": [
    {
      "id": 7910837291,
      "title": "Toit",
      "desceription": "Best brewary in the area",
      "lat": 89.33,
      "long": 123.222
    },

    {
      "id": 1697290292,
      "title": "English Craft",
      "desceription": "Best brews hands down",
      "lat": 88.214,
      "long": 122.908
    }
  ]
}
```

* Worth clarifying whether a bulk API will be needed

```json
// Create location
POST v1/api/locations

{
  "locations": [
    {
      "title": "Toit",
      "desceription": "Best brewary in the area",
      "lat": 89.33,
      "long": 123.222
    },

    {
      "title": "English Craft",
      "desceription": "Best brews hands down",
      "lat": 88.214,
      "long": 122.908
    }
  ]
}

Response: 202 ACCEPTED
```