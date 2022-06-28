package com.yollpoll.nmb.net


const val BASE_URL = "https://www.nmbxd1.com/"
const val DIRECT_BASE_URL = "https://adnmb3.com/"//重定向以后的url，应该动态获取

const val ROOT_URL = "Api/backupUrl"//重定向获取根url

const val CDN_URL = "Api/getCdnPath"//图片cdn地址
const val COVER = "http://cover.acfunwiki.org/cover.php"//封面地址,会重定向
var realCover = ""//封面图片重定向以后的真实地址
const val ANNOUNCEMENT = "http://cover.acfunwiki.org/nmb-notice.json"//公告
const val IMG_THUMB_URL = "/thumb/"
const val IMG_URL = "/image/"

//获取板块列表
const val FORUM_LIST = "Api/getForumList"

//获取串
const val GET_ARTICLE = "Api/showf/"
const val GET_CHILD_ARTICLE = "Api/thread"
const val GET_COOKIE = "/Api/getCookie"

const val NEW_THREAD: String = "/Home/Forum/doPostThread.html"
const val REPLY_THREAD: String = "/Home/Forum/doReplyThread.html"

//查看订阅
const val COLLECTION: String = "/Api/feed"
const val ADD_COLLECTION: String = "/Api/addFeed"
const val DEL_COLLECTION: String = "/Api/delFeed"

//时间线
//    public static final String TIME_LINE = "Api/timeline";
const val TIME_LINE: String = "Api/timeline/"
const val TIME_LINE_ID = "-1"

var realUrl: String? = null//重定向以后获取到的根url

var imagHead = "https://nmbimg.fastmirror.org/"//应该动态获取
var imgUrl = imagHead + IMG_URL
var imgThumbUrl = imagHead + IMG_THUMB_URL