SlidingListViewPlus
===================

A custom ListView that its item view can been sliding left. Reference to the app Wechat which was released by Tencent Inc. 
<h1>Show</h1>
![image](https://github.com/YoungLeeForeverBoy/SlidingListViewPlus/blob/master/about_this_widget.gif?raw=true)

<h1>Setup</h1>
* In Eclipse, import this project as a Android library project.
* Then add this project as a dependency to your project.
* This widget support Android 2.x and later version which I had tested.

<h1>Usage</h1>
* In your activity layout file, use like follow, the SwipeListView has one attribute "swipeItemViewID" temporarily(I just thought one attribute now :P), and this attribute must been set, it's the resource ID of the SwipeItemView:
  ![image](https://github.com/YoungLeeForeverBoy/SlidingListViewPlus/blob/master/usage_about_swipelistview.jpg?raw=true)
* Then in your custom BaseAdapter's layout file, you should do like follow, and the SwipeItemView has three attributes, "primaryView", "slidingView" and "enableSliding", attribute "primaryView" declare the layout file reference of the primary view which show at normal state of the ListView's item, and it must been set, attribute "slidingView" declare layout file reference of the item's sliding part, "enableSliding" means if the SwipeItemView should enable sliding or not:
  ![image](https://github.com/YoungLeeForeverBoy/SlidingListViewPlus/blob/master/usage_about_swipeitemview.jpg?raw=true)
* Then this widget use like the ListView, it's ok to set AdapterView.OnItemClickListener and AdapterView.OnItemLongClickListener
