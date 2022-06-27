package com.yollpoll.nmb

import android.app.Application
import com.yollpoll.framework.fast.FastActivity
import com.yollpoll.framework.fast.FastViewModel
import com.yollpoll.nmb.databinding.ActivityMainBinding

/**
 * Created by spq on 2022/6/23
 */
class MainActivity :FastActivity<ActivityMainBinding,MainVM>(){

}
class MainVM(app:Application):FastViewModel(app){

}