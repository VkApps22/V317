package br.com.kascosys.vulkanconnectv317

import android.app.Application
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import java.util.*

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val locale = Locale.getDefault()
        val store = PreferenceLocaleStore(this, locale)
        Lingver.init(this, store)
    }
}