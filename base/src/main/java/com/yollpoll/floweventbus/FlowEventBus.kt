package com.yollpoll.floweventbus

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.*

/**
 * Created by spq on 2022/1/17
 */
object FlowEventBus {
    private val bus: HashMap<String, MutableSharedFlow<out Any>> = hashMapOf()

    private fun <T : Any> with(key: String): MutableSharedFlow<T> {
        if (!bus.containsKey(key)|| bus[key]==null) {
            val flow = MutableSharedFlow<T>()
            bus[key] = flow
        }
        return bus[key] as MutableSharedFlow<T>
    }

    /**
     * 对外只暴露SharedFlow
     * @param action String
     * @return SharedFlow<T>
     */
    fun <T> getFlow(action: String): SharedFlow<T> {
        return with(action)
    }


    /**
     * 挂起函数
     * @param action String
     * @param data T
     */
    suspend fun <T : Any> post(action: String, data: T) {
        with<T>(action).emit(data)
    }

    /**
     * 详见tryEmit和emit的区别
     * @param action String
     * @param data T
     * @return Boolean
     */
    fun <T : Any> tryPost(action: String, data: T): Boolean {
        return with<T>(action).tryEmit(data)
    }

    /**
     * sharedFlow会长久持有，所以要加声明周期限定，不然会出现内存溢出
     * @param lifecycle Lifecycle
     * @param action String
     * @param block Function1<T, Unit>
     */
    suspend fun <T : Any> subscribe(lifecycle: Lifecycle, action: String, block: (T) -> Unit) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
            with<T>(action).collect {
                block(it)
            }
        }
    }

    /**
     * 注意，使用这个方法需要将协程在合适的时候取消，否则会导致内存溢出
     * @param action String
     * @param block Function1<T, Unit>
     */
    suspend fun <T : Any> subscribe(action: String, block: (T) -> Unit) {
        with<T>(action).collect {
            block(it)
        }
    }

}
