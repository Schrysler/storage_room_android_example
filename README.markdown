# StorageRoom Android Example

This is an Android example on how to use the StorageRoom API ( <http://storageroomapp.com> ) provided by [Till Simon](http://www.tillsimon.com/).  

Find the code for the actual JSON parsing from the StorageRoom API in  

+ model / webconnect / RestaurantsDownloader.java -> downloadRestaurants() { ... }
+ model / webconnect / AnnouncementsDownloader.java -> downloadAnnouncements() { ... }


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

