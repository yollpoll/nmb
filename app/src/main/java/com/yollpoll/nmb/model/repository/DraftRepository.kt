package com.yollpoll.nmb.model.repository

import com.yollpoll.nmb.db.MainDB
import com.yollpoll.nmb.model.bean.DraftBean
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Created by spq on 2022/12/6
 */
class DraftRepository @Inject constructor(val db: MainDB) : IRepository {
    suspend fun getDrafts(): List<DraftBean> {
        return db.getDraftDao().query()
    }

    fun getDraftFlow(): Flow<List<DraftBean>> {
        return db.getDraftDao().queryFlow()
    }

    suspend fun delDraft(bean: DraftBean) {
        db.getDraftDao().delete(bean)
    }

    suspend fun insetDraft(vararg bean: DraftBean) {
        db.getDraftDao().insert(*bean)
    }

    suspend fun updateDraft(vararg bean: DraftBean) {
        db.getDraftDao().update(*bean)
    }

}