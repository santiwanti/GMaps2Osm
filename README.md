# GMaps 2 Osm

An app to convert from Google Maps links to links that OSM can understand. The app will automatically
attempt to open any Google Maps link in your OSM client when you click them.

**Note: This app requires that you don't run Google Play Services**

## How it Works

Google Maps links like "https://maps.app.goo.gl" are usually not handled by OSM clients because the link
requires an API call.

To work around this API call this app uses a WebView to open the link which bypasses the API call which
generates the "real" link. Once the "real" link is opened the app extracts the coordinates and creates
a new link with the coordinates which OSM clients can parse and then attempts to open an OSM app.

All the code is in the MainActivity and can be reviewed quite easily considering it is just 200 lines
of code.
