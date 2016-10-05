MjolnirRecyclerView
==========

[![CircleCI](https://circleci.com/gh/infinum/MjolnirRecyclerView/tree/master.svg?style=svg&circle-token=d7d31554a2af2654f26885397e9dda150cc07428)](https://circleci.com/gh/infinum/MjolnirRecyclerView/tree/master)

Provides a simple way to extend the default RecyclerView behaviour with support for headers, footers, empty view, DiffUtil and ArrayAdapter like methods.

## Features

### 1. Header & footer support

Add a custom number of headers and footers to [MjolnirRecyclerView](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerView.java) by using 2 simple methods from [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java):

```java
    adapter.addHeader(View view)
    adapter.addFooter(View view)
```    

### 2. Empty view

Empty view support for [MjolnirRecyclerView](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerView.java). View is automatically hidden when adapter is populated with some data, and is automatically shown once again when adapter becomes empty.

```java
    recyclerView.setEmptyView(View view)
```  
 
 
### 3. DiffUtil

DiffUtil support for [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java). Simply add [DiffUtil.Callback](https://developer.android.com/reference/android/support/v7/util/DiffUtil.Callback.html) in adapters reset method:

```java
    adapter.reset(items, new ItemDiffUtilResult())
```    

As [DiffUtil](https://developer.android.com/reference/android/support/v7/util/DiffUtil.html) is a blocking sync action, it's executed on the background thread inside the  [MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java) by using a [AsyncTask](https://developer.android.com/reference/android/os/AsyncTask.html). As a result of this approach, you need to call [cancel()](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java#L126) method on your adapter when your activity or fragment is about to be destroyed, so that the adapter is not updated if the screen has been destroyed.

### 4. ArrayAdapter like methods

[MjolnirRecyclerAdapter](https://github.com/infinum/MjolnirRecyclerView/blob/master/mjolnirrecyclerview/src/main/java/co/infinum/mjolnirrecyclerview/MjolnirRecyclerAdapter.java) has full support for [ArrayAdapter](https://developer.android.com/reference/android/widget/ArrayAdapter.html) methods, like add(), addAll(), reset(), remove(), set()...
