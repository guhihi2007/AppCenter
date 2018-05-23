package cn.lt.android

import android.app.Application
import com.facebook.stetho.Stetho
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
/**
 * Created by Jone on 2018/3/21.
 */
internal object BuildTypeUtils
{
    fun installStetho(application: Application)
    {
        com.facebook.stetho.Stetho.initializeWithDefaults(application);
    }

    fun installRefWatcher(application: Application): RefWatcherUtil?
    {
//        val refWatcherUtil = RefWatcherUtil(application)
        return null
    }

    internal class RefWatcherUtil(application: Application)
    {
        private val refWatcher: RefWatcher?

        init
        {
            refWatcher = LeakCanary.install(application);
        }

        fun watch(watchedReference: Any?)
        {
            refWatcher?.watch(watchedReference)
        }
    }
}