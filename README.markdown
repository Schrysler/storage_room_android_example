# StorageRoom Android Example

This is an Android example on how to use the StorageRoom API ( <http://storageroomapp.com> ) provided by [Till Simon](http://www.tillsimon.com/).  

Find the code for the actual JSON parsing from the StorageRoom API in  

+ model / webconnect / RestaurantsDownloader.java -> downloadRestaurants() { ... }
+ model / webconnect / AnnouncementsDownloader.java -> downloadAnnouncements() { ... }

**To get Google Maps working on your device, insert your own Google Maps API Key in res/layout/restaurantsmap.xml !** 

## Screenshots

<img src="http://farm4.static.flickr.com/3057/5852900499_bb292d6fb8.jpg" width="216" height="360"> &nbsp;&nbsp;&nbsp;
<img src="http://farm4.static.flickr.com/3074/5852899963_8da71c437d.jpg" width="216" height="360"> &nbsp;&nbsp;&nbsp;
<img src="http://farm3.static.flickr.com/2513/5853454018_f50fb092f1.jpg" width="216" height="360"> &nbsp;&nbsp;&nbsp;

## Basics

Restaurant data and simple announcements are loaded from the StorageRoom CMS via the provided JSON-API. Data is saved locally in a SQLite database and displayed in a Activity with a ListView and a Activity with a MapView. Details can be accessed in a seperate activity.
The images in the ListView and in the DetailsView are downloaded in the background and cached locally for the application life-time.  

This example is kept simple on purpose and focuses on the downloading and parsing of the JSON data.

## Maintainers

Till Simon

## Bugs and Feedback

If you discover any bugs, please create an [issue on GitHub](http://github.com/tillsimon/storage_room_android_example/issues)

## License

MIT License  
Copyright &copy; 2011 [Till Simon](http://www.tillsimon.com)  
<mail@tillsimon.com>

