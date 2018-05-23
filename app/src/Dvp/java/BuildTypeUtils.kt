package cn.lt.android

import android.app.Application

/**
 * Created by Jone on 2018/3/21.
 */
internal object BuildTypeUtils
{
    fun installStetho(application: Application)
    {
    }

    fun installRefWatcher(application: Application): RefWatcherUtil?
    {
        return null
    }

    internal class RefWatcherUtil(application: Application)
    {
        fun watch(watchedReference: Any?)
        {
        }
    }
}