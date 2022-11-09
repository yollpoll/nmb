X岛Android客户端
语言: kotlin
最低系统版本: android 5.0
采用最新Material You，根据系统定制app配色，快去修改系统颜色试试吧。
操作说明：首页右下角按钮分别支持左滑（弹出板块选择）、右滑（弹出设置）、上滑（回到列表顶部）、下滑（刷新）、点击（发新串）
后续计划：准备搞一个跑团功能，我观察到跑团比较容易出现各种分支和各说各的，所以准备加入树状图和强制收缩时间线的功能，目前还没有什么头绪。

技术说明:
整体项目采用MVVM+APT动态代码生成+组件化+依赖注入。
其中基础架构全部由我自己实现，参考主页中的其他项目(已发布MavenCenter,接入方式具体看wiki)：
基础架构:https://github.com/yollpoll/framework/wiki/%E4%BD%BF%E7%94%A8%E6%96%B9%E6%B3%95
路由组建:https://github.com/yollpoll/sRouter
依赖注入采用hilt。
出于练手的目的，项目大量采用jetpack库内容，欢迎交流学习，这年头还在深入学习android的不多了好寂寞

联动：
好多年前写的A岛客户端(肥宅岛)(停止维护):
https://github.com/yollpoll/MyApp
另有一个采用compose开发的客户端（鸽中）：
https://github.com/yollpoll/nmbAndroidCompose
另有一个electron的桌面客户端（鸽中）：
https://github.com/yollpoll/nmb_electron