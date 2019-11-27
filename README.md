# [DEPRECATED] MjolnirRecyclerView - This library is no longer maintained and it will not receive any more updates. 

==========

[![CircleCI](https://circleci.com/gh/infinum/MjolnirRecyclerView/tree/master.svg?style=svg&circle-token=d7d31554a2af2654f26885397e9dda150cc07428)](https://circleci.com/gh/infinum/MjolnirRecyclerView/tree/master)
[![JCenter](https://img.shields.io/badge/JCenter-2.2.0-red.svg?style=flat)](https://bintray.com/infinum/android/mjolnirrecyclerview/view)
[![Method count](https://img.shields.io/badge/Methods%20count-145-e91e63.svg)](http://www.methodscount.com/?lib=co.infinum%3Amjolnirrecyclerview%3A2.2.0)
[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-MjolnirRecyclerView-brightgreen.svg?style=flat)](https://android-arsenal.com/details/1/4643)

Provides a simple way to extend the default RecyclerView behaviour with support for headers, footers, empty view, DiffUtil and ArrayAdapter like methods.

## Usage


Add the library as a dependency to your ```build.gradle```

```groovy
compile 'co.infinum:mjolnirrecyclerview:version@aar'
```

Check the latest version [here](https://bintray.com/search?query=mjolnirrecyclerview).

## Features

### 1. Header & footer support

Add a custom number of headers and footers to [MjolnirRecyclerView](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerView.java) by using 2 simple methods from [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java):

```java
    adapter.setHeader(View view)
    adapter.setHeader(View view)
```    

### 2. Empty view

Empty view support for [MjolnirRecyclerView](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerView.java). View is automatically hidden when adapter is populated with some data, and is automatically shown once again when adapter becomes empty.

```java
    recyclerView.setEmptyView(View view)
```  
 
You can also show empty view while adapter is not set to the MjolnirRecyclerView, which is handy if you want to intialize adapter at some later point in the time.

```java  
    // show empty view if adapter is not set
    recyclerView.setEmptyView(View view, true)
```   
### 3. DiffUtil

DiffUtil support for [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java). Simply add [DiffUtil.Callback](https://developer.android.com/reference/android/support/v7/util/DiffUtil.Callback.html) in adapters update method:

```java
    adapter.update(new ItemDiffUtilResult())
```    

As [DiffUtil](https://developer.android.com/reference/android/support/v7/util/DiffUtil.html) is a blocking sync action, it's executed on the background thread inside the  [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java) by using a [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html). As a result of this approach, you need to call [cancel()](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java#L126) method on your adapter when your activity or fragment is about to be destroyed, so that the adapter is not updated if the screen has been destroyed.

```java
    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.cancel();
    }
``` 

### 4. ArrayAdapter like methods

[MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java) has full support for [ArrayAdapter](https://developer.android.com/reference/android/widget/ArrayAdapter.html) methods, like add(), addAll(), reset(), remove(), set()...

## Contributing

Feedback and code contributions are very much welcome. Just make a pull request with a short description of your changes. By making contributions to this project you give permission for your code to be used under the same [license](LICENSE).

## Credits

Maintained and sponsored by
[Infinum](http://www.infinum.co).

<img src="https://infinum.co/infinum.png" width="264">
