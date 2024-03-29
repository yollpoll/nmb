package com.yollpoll.nmb.view.activity

import android.animation.ObjectAnimator
import android.app.Application
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.PopupMenu
import androidx.activity.viewModels
import androidx.core.util.Pair
import androidx.databinding.Bindable
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import com.yollpoll.annotation.annotation.OnMessage
import com.yollpoll.annotation.annotation.Route
import com.yollpoll.arch.annotation.Extra
import com.yollpoll.arch.log.LogUtils
import com.yollpoll.base.CommonDialog
import com.yollpoll.base.NMBActivity
import com.yollpoll.base.logI
import com.yollpoll.floweventbus.FlowEventBus
import com.yollpoll.framework.dispatch.DispatchRequest
import com.yollpoll.framework.extensions.*
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.*
import com.yollpoll.nmb.R
import com.yollpoll.nmb.databinding.ActivityNewthreadBinding
import com.yollpoll.nmb.model.bean.CookieBean
import com.yollpoll.nmb.model.bean.DraftBean
import com.yollpoll.nmb.model.repository.ArticleDetailRepository
import com.yollpoll.nmb.model.repository.CookieRepository
import com.yollpoll.nmb.model.repository.DraftRepository
import com.yollpoll.nmb.model.repository.ForumRepository
import com.yollpoll.nmb.router.DispatchClient
import com.yollpoll.nmb.router.ROUTE_NEW_THREAD
import com.yollpoll.nmb.view.widgets.REQ_CAMERA
import com.yollpoll.nmb.view.widgets.REQ_CROP_PHOTO
import com.yollpoll.nmb.view.widgets.REQ_PHOTO
import com.yollpoll.nmb.view.widgets.emoji.ChooseEmojiDialogFragment
import com.yollpoll.nmb.view.widgets.showChoosePicDialog
import com.yollpoll.nmb.view.widgets.tag.gotoChooseTag
import com.yollpoll.utils.compressBitmap
import com.yollpoll.utils.getPathByUri
import com.yollpoll.utils.saveBitmapToSd
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URLEncoder
import javax.inject.Inject

const val ACTION_NEW = "new"
const val ACTION_REPLAY = "replay"
const val ACTION_LINK = "link"
const val ACTION_REPORT = "report"

suspend fun gotoNewThreadWithDraft(context: Context, draft: DraftBean) {
    val action = draft.reply?.let {
        ACTION_REPLAY
    } ?: ACTION_NEW

    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "draft" to URLEncoder.encode(draft.toJson(), "UTF-8"),
            "action" to action
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

suspend fun gotoNewThreadActivity(context: Context, forumId: String) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_NEW,
            "forumId" to forumId,
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

suspend fun gotoRelyThreadActivity(context: Context, replyTo: String) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_REPLAY,
            "replyTo" to replyTo
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

suspend fun gotoLinkActivity(context: Context, threadId: String, linkIds: List<String>) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_LINK,
            "replyTo" to threadId,
            "linkIds" to URLEncoder.encode(linkIds.toListJson(), "UTF-8")
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

suspend fun gotoReportActivity(context: Context, linkIds: List<String>) {
    val req = DispatchRequest.RequestBuilder().host("nmb").module("new_thread").params(
        hashMapOf(
            "action" to ACTION_REPORT,
            "linkIds" to URLEncoder.encode(linkIds.toListJson(), "UTF-8")
        )
    ).build()
    DispatchClient.manager?.dispatch(context, req)
}

@AndroidEntryPoint
@Route(url = ROUTE_NEW_THREAD)
class NewThreadActivity : NMBActivity<ActivityNewthreadBinding, NewThreadVm>() {
    private var cameraUri: Uri? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_PHOTO -> {
                //相册
                if (resultCode != RESULT_OK) {
                    return
                }
                val uri = data?.data
                if (null == uri) {
                    "获取图片失败".shortToast()
                    return
                }
                vm.selectPhoto(uri)
            }
            REQ_CAMERA -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                val intent = Intent("com.android.camera.action.CROP")
                intent.setDataAndType(cameraUri, "image/*")
                intent.putExtra("scale", true)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                this@NewThreadActivity.startActivityForResult(intent, REQ_CROP_PHOTO)
            }
            REQ_CROP_PHOTO -> {
                if (resultCode != RESULT_OK) {
                    return
                }
                if (null == cameraUri) {
                    "图片裁剪失败".shortToast()
                    return
                }
                vm.selectPhoto(cameraUri!!)
            }
            REQUEST_DRAWING -> {
                if (resultCode == RESULT_OK) {
                    val path = data?.getStringExtra("path")
                    val uri = Uri.parse(path)
                    vm.selectPhoto(uri, height = 4096)
                }
            }
        }
    }

    @Extra
    var action: String = ""

    @Extra
    var forumId: String = ""

    @Extra
    var replyTo: String = ""

    @Extra
    var linkIds: String = ""

    @Extra
    var draft: String? = ""
    private val vm: NewThreadVm by viewModels()
    override fun getLayoutId() = R.layout.activity_newthread
    override fun getMenuLayout() = R.menu.menu_new_thread
    override fun initViewModel() = vm
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_draft -> {
                lifecycleScope.launch {
                    gotoDraftActivity(context, ACTION_SELECT)
                }
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    action = ACTION_NEW
                    forumId = "4"
                    initView()
                    intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
                        // Update UI to reflect text being shared
                        vm.threadContent = it
                    }
                } else if (intent.type?.startsWith("image/") == true) {
                    action = ACTION_NEW
                    forumId = "4"
                    initView()
                    (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
                        // Update UI to reflect image being shared
                        vm.selectPhoto(it)
                    }
                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
                initView()
                initData()
            }
        }

    }

    private fun initData() {
        vm.action = action
        if (!draft.isNullOrEmpty()) {
            vm.draftBean = draft!!.toJsonBean()
            forumId = vm.draftBean!!.fid
            replyTo = vm.draftBean?.reply ?: ""
        }
        when (action) {
            ACTION_NEW -> {
                vm.fid = forumId
                mDataBinding.tvTag.transitionName = forumId
            }
            ACTION_REPLAY -> {
                vm.replyTo = replyTo
            }
            ACTION_LINK -> {
                vm.replyTo = replyTo
                linkIds.toListBean<String>()?.let {
                    vm.addLinkIds(it)
                    mDataBinding.edtContent.setText(vm.threadContent)
                    mDataBinding.edtContent.setSelection(vm.threadContent.length)
                    mDataBinding.edtContent.requestFocus()
                }
            }
            ACTION_REPORT -> {
                vm.title = "举报"
                linkIds.toListBean<String>()?.let {
                    vm.addLinkIds(it)
                    mDataBinding.edtContent.setSelection(mDataBinding.edtContent.text.toString().length)
                }
            }
        }
        FlowEventBus.getFlow<String>(ACTION_TAG_NAME).asLiveData().observe(this) {
//            mDataBinding.tvTag.text = it
            vm.forumName = it
        }
        FlowEventBus.getFlow<String>(ACTION_TAG_ID).asLiveData().observe(this) {
            vm.fid = it
            forumId = it
        }
    }

    /**
     * 保存到草稿箱
     */
    private fun showDraftDialog() {
        CommonDialog("保存到草稿箱", "希望将内容保存到草稿箱吗?", context, onCancel = {
            this@NewThreadActivity.finish()
        }) {
            lifecycleScope.launch {
                vm.saveToDraft()
                this@NewThreadActivity.finish()
            }
        }.show()
    }

    override fun onBackPressed() {
        if (vm.threadContent.isNotEmpty()) {
            showDraftDialog()
        } else {
            super.onBackPressed()
        }
    }

    private fun initView() {
        initTitle(mDataBinding.layoutTitle.toolbar, true) {
            if (vm.threadContent.isNotEmpty()) {
                showDraftDialog()
            } else {
                this.finish()
            }
        }
        when (action) {
            ACTION_NEW -> {
                vm.title = "新串"
            }
            ACTION_REPLAY -> {
                vm.title = "回复: >>NO.$replyTo"
                mDataBinding.imgShowMoreTitle.visibility = View.GONE
                mDataBinding.inputTitle.visibility = View.GONE
            }
        }
        vm.currentImgLD.observe(this) {
            it?.let {
                mDataBinding.rlImg.visibility = View.VISIBLE
                mDataBinding.ivSelected.setImageBitmap(it)
            } ?: kotlin.run {
                mDataBinding.rlImg.visibility = View.GONE
            }
        }
        mDataBinding.edtContent.requestFocus()
    }

    /**
     * 选择表情
     */
    fun chooseEmoji() {
        ChooseEmojiDialogFragment { word: String?, id: Int, fragment: DialogFragment? ->
            if (word?.isEmpty() != false) {
                fragment?.dismiss()
                //选择了图片
                if (id == 0) return@ChooseEmojiDialogFragment
                lifecycleScope.launch(Dispatchers.IO) {
                    val cacheBitmap = BitmapFactory.decodeResource(resources, id)
                    vm.selectImg(cacheBitmap)
                }
            } else {
                val nSection = mDataBinding.edtContent.selectionStart
                mDataBinding.edtContent.text?.insert(nSection, word)
                // 可以更新光标位置
                mDataBinding.edtContent.setSelection(nSection + word.length)
                //选择了文字
//                vm.threadContent += word
                fragment?.dismiss()
            }
        }.show(supportFragmentManager, "chooseEmoji")
    }

    /**
     * 选择图片
     */
    fun choosePic() {
        showChoosePicDialog(this) {
            this.cameraUri = it
        }
    }

    /**
     * 选择板块标签
     */
    fun chooseTag() {
        lifecycleScope.launch {
            val pair1: Pair<View, String> = Pair(mDataBinding.tvTag, forumId)
            val pair2: Pair<View, String> = Pair(mDataBinding.rlChooseTag, "tag_group")
            gotoChooseTag(this@NewThreadActivity, forumId, pair1, pair2)
        }
    }

    /**
     * 显示更多输入项目
     */
    fun showOrHideTitle() {
        if (mDataBinding.llMoreTitle.visibility == View.VISIBLE) {
            dismissMoreTitle()
        } else {
            showMoreTitle()
        }
    }

    /**
     * 更多标题参数
     */
    private fun showMoreTitle() {
        mDataBinding.llMoreTitle.visibility = View.VISIBLE
        val anim = ObjectAnimator.ofFloat(mDataBinding.imgShowMoreTitle, "rotation", 0f, 180f)
        anim.duration = 400
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

        //layoutAnimation
        val animation = AnimationUtils.loadAnimation(this, R.anim.new_thread_anim)
        val layoutAnimationController = LayoutAnimationController(animation)
        layoutAnimationController.delay = .1f
        layoutAnimationController.interpolator = AccelerateDecelerateInterpolator()
        layoutAnimationController.order = LayoutAnimationController.ORDER_REVERSE
        mDataBinding.llMoreTitle.layoutAnimation = layoutAnimationController
        mDataBinding.llMoreTitle.startLayoutAnimation()
//        mDataBinding.edtContent.startAnimation(animation)
//        mDataBinding.rlImg.startAnimation(animation)
    }

    private fun dismissMoreTitle() {
        //动画监听
        val mAnimationListener: Animation.AnimationListener = object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationEnd(animation: Animation) {
                mDataBinding.llMoreTitle.visibility = View.GONE

            }

            override fun onAnimationRepeat(animation: Animation) {}
        }

        //img anim
        val anim = ObjectAnimator.ofFloat(mDataBinding.imgShowMoreTitle, "rotation", 180f, 0f)
        anim.duration = 400
        anim.interpolator = AccelerateDecelerateInterpolator()
        anim.start()

        //layoutAnimation
        val animation = AnimationUtils.loadAnimation(this, R.anim.new_thread_anim_close)
        animation.setAnimationListener(mAnimationListener)
        val layoutAnimationController = LayoutAnimationController(animation)
        layoutAnimationController.delay = .1f
        layoutAnimationController.interpolator = AccelerateDecelerateInterpolator()
        layoutAnimationController.order = LayoutAnimationController.ORDER_NORMAL
        mDataBinding.llMoreTitle.layoutAnimation = layoutAnimationController
        mDataBinding.llMoreTitle.startLayoutAnimation()

//        edt anim
//        mDataBinding.edtContent.startAnimation(animation)
//        mDataBinding.rlImg.startAnimation(animation)
    }

    lateinit var popupMenu: PopupMenu

    fun showCookies(view: View) {
        lifecycleScope.launch {
            val popupMenu = PopupMenu(context, view)

            vm.cookies.forEachIndexed() { pos, item ->
                val use = if (item.used == 1)
                    "(使用中)"
                else
                    ""
                popupMenu.menu.add("${item.name}$use")
            }
            popupMenu.setOnMenuItemClickListener {
                val selected = vm.cookies.filter { cookie ->
                    cookie.name == it.title
                }
                if (selected.isNotEmpty()) {
                    vm.selectCookie(selected[0])
                }
                true
            }
            popupMenu.show()
        }
    }

    /**
     * 绘图
     */
    fun gotoDraw() {
        lifecycleScope.launch {
            gotoDrawing(context)
        }
    }

    @OnMessage(stick = false)
    fun onDraftSelect(draft: DraftBean) {
        lifecycleScope.launch {
            vm.threadContent = draft.content
            val bitmap = compressBitmap(draft.img, 1024, 1024)
            vm.selectImg(bitmap)
        }
    }

    fun showImage(view: View) {
        lifecycleScope.launch {
            vm.currentImgLD.value?.run {
                saveBitmapToSd(
                    this,
                    "new_thread_temp_img.jpg",
                    context.filesDir.absolutePath
                )?.run {
                    gotoImageActivityByFile(context, this)
                }
            }
        }
    }

}

@HiltViewModel
class NewThreadVm @Inject constructor(
    val app: Application,
    val repository: ArticleDetailRepository,
    val cookieRepository: CookieRepository,
    val draftRepository: DraftRepository,
    val forumRepository: ForumRepository
) : FastViewModel(app) {
    init {
        viewModelScope.launch {
            cookies = cookieRepository.queryCookies()
            cookies.forEach {
                if (it.used == 1) {
                    "use cookie: ${it.name}".logI()
                    curCookie = it
                }

            }
        }
    }

    lateinit var cookies: List<CookieBean>

    //当前选择图片
    private val currentImg = MutableLiveData<Bitmap?>(null)
    val currentImgLD: LiveData<Bitmap?> = currentImg

    @Bindable
    var forumName: String = "综合板"
        set(value) {
            field = value
            notifyPropertyChanged(BR.forumName)
        }

    @Bindable
    var curCookie: CookieBean? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.curCookie)
        }

    @Bindable
    var title: String = "新串"
        set(value) {
            field = value
            notifyPropertyChanged(BR.title)
        }

    @Bindable
    var threadTitle: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.threadTitle)
        }

    @Bindable
    var threadContent: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.threadContent)
        }

    @Bindable
    var waterMask: Boolean = true
        set(value) {
            field = value
            notifyPropertyChanged(BR.waterMask)
        }

    @Bindable
    var name: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }

    @Bindable
    var email: String = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.email)
        }

    var imgPath: String = ""

    var action: String = ""

    var fid: String = ""
        set(value) {
            field = value
            viewModelScope.launch {
                forumRepository.queryById(value)?.also {
                    forumName = it.name
                }
            }
        }

    var replyTo: String = ""

    var draftBean: DraftBean? = null
        set(value) {
            field = value
            value?.let {
                email = it.email ?: ""
                fid = it.fid
                name = it.fName
                waterMask = it.mask == "1"
                threadContent = it.content
                threadTitle = it.title ?: ""
                title = "回复: >>NO.${it.reply}"
                viewModelScope.launch {
                    val bitmap = compressBitmap(it.img, 1024, 1024)
                    selectImg(bitmap)
                }
            }

        }

    /**
     * 提交
     */
    fun submit() {
        if (threadContent.isEmpty())
            "文本内容为空".shortToast()
        when (action) {
            ACTION_NEW -> {
                newThread()
            }
            ACTION_REPLAY -> {
                reply()
            }
            ACTION_LINK -> {
                reply()
            }
            ACTION_REPORT -> {
                report()
            }
        }
    }

    /**
     * 选择照片
     * @param uri Uri
     * @param width Int
     * @param height Int
     */
    fun selectPhoto(uri: Uri, width: Int = 1024, height: Int = 1024) {
        viewModelScope.launch(Dispatchers.IO) {
            val path = getPathByUri(uri, app)
            val bitmap = compressBitmap(path, width, height)
            selectImg(bitmap)
        }
    }


    /**
     * 选择图片
     * @param bitmap Bitmap?
     */
    suspend fun selectImg(bitmap: Bitmap?) {
        withContext(Dispatchers.Main) {
            currentImg.value = bitmap
        }
    }

    /**
     * 删除图片
     */
    fun delImg() {
        viewModelScope.launch(Dispatchers.Main) {
            currentImg.value = null
        }
    }

    /**
     * 发表新串
     */
    private fun newThread() {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val bitmap = currentImg.value
            val path = if (null != bitmap)
                saveBitmapToSd(bitmap, "img.jpg", app.filesDir.absolutePath)
            else null
            val file = if (null != path) File(path) else null
            repository.newThread(
                fid,
                name,
                threadTitle,
                email,
                threadContent,
                if (waterMask) "1" else "0",
                file
            )
            finish()
            hideLoading()
        }
    }

    /**
     * 回复
     */
    private fun reply() {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            val bitmap = currentImg.value
            val path = if (null != bitmap)
                saveBitmapToSd(bitmap, "img.jpg", app.filesDir.absolutePath)
            else null
            val file = if (null != path) File(path) else null
            val res = repository.reply(
                replyTo,
                name,
                threadTitle,
                email,
                threadContent,
                if (waterMask) "1" else "0",
                file
            )
//            LogUtils.e("res: ${res.string()}")
            finish()
            hideLoading()
            sendEmptyMessage(MR.ThreadDetailActivity_refresh)
        }
    }

    /**
     * 举报
     */
    fun report() {
        viewModelScope.launch(Dispatchers.IO) {
            showLoading()
            repository.newThread(
                "18",
                name,
                threadTitle,
                name,
                threadContent,
                "0",
                null
            )
            hideLoading()
            finish()
        }
    }

    /**
     * 添加引用链接
     * @param linkIds List<String>
     */
    fun addLinkIds(linkIds: List<String>) {
        var content = ""
        linkIds.forEach {
            content += ">>No.${it}\n"
        }
        threadContent = content
    }

    /**
     * 选择饼干
     * @param cookie CookieBean
     */
    fun selectCookie(cookie: CookieBean) {
        viewModelScope.launch {
            curCookie?.let {
                it.used = 0
                cookieRepository.updateCookie(it)
            }
            cookie.used = 1
            App.INSTANCE.cookie = cookie
            curCookie = cookie
            cookieRepository.insertCookie(cookie)
        }
    }

    /**
     * 保存到草稿
     */
    suspend fun saveToDraft() {
        showLoading()
        val bitmap = currentImg.value
        val path = bitmap?.let {
            return@let saveBitmapToSd(
                bitmap,
                "${System.currentTimeMillis()}_draft.jpg",
                app.filesDir.absolutePath
            )
        }
        val draft = DraftBean(
            id = draftBean?.id,
            reply = if (replyTo.isEmpty()) null else replyTo,
            fid = fid,
            fName = forumName,
            mask = if (waterMask) "1" else "0",
            email = email,
            title = threadTitle,
            content = threadContent,
            updateTime = System.currentTimeMillis(),
            img = path
        )
        draftRepository.insetDraft(draft)
        hideLoading()
    }
}