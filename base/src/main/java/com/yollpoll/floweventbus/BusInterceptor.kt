package com.aispeech.framework.floweventbus

import android.content.Context
import com.aispeech.framework.dispatch.DispatchRequest
import com.aispeech.framework.dispatch.DispatchResponse
import com.aispeech.framework.dispatch.RealDispatchChain
import java.util.concurrent.Flow

/**
 * Created by spq on 2022/1/18
 */
interface BusInterceptor {
    suspend fun intercept(chain: Chain): Flow

    interface Chain {
        suspend fun proceed(): Flow
    }
}

class RealBusChain(private val interceptors: List<BusInterceptor>, private val index: Int) :
    BusInterceptor.Chain {
    override suspend fun proceed(): Flow {
        if (index >= interceptors.size) {
            throw Exception("Bus拦截器数量错误: size${interceptors.size} index:$index")
        }
        val nextChain =
            RealBusChain(interceptors, index + 1)
        val interceptor = interceptors[index]
        return interceptor.intercept(nextChain)
    }

}