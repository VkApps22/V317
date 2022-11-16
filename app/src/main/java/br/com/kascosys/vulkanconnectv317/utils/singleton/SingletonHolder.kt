package br.com.kascosys.vulkanconnectv317.utils.singleton

import android.util.Log

open class SingletonHolder<out T : Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(): T? {
        Log.i("SingletonHolder","get existent instance ${instance?.javaClass}")

        return instance
    }

    fun getInstance(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null) {
            Log.i("SingletonHolder","get existent instance ${instance?.javaClass}")

            return checkInstance
        }

        return synchronized(this) {
            Log.i("SingletonHolder","get new instance---------------------")

            val checkInstanceAgain = instance
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}