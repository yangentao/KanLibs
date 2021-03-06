@file:Suppress("unused")

package dev.entao.kan.version

import android.app.Activity
import androidx.annotation.Keep
import dev.entao.kan.appbase.App
import dev.entao.kan.appbase.Task
import dev.entao.kan.appbase.sql.MapTable
import dev.entao.kan.base.*
import dev.entao.kan.dialogs.DialogX
import dev.entao.kan.http.HttpGet
import dev.entao.json.YsonObject
import java.io.File

/**
 * Created by entaoyang@163.com on 2017-06-13.
 */

@Keep
class YetVersion(val jo: YsonObject) {
    val versionCode: Int by jo
    val versionName: String by jo
    val msg: String by jo
    val resId: Int by jo


    fun great(): Boolean {
        return versionCode > App.versionCode
    }

    val downloadUrl: String
        get() {
            return HttpGet(SERVER_DOWN).arg("id", resId).buildGetUrl()
        }

    companion object {
        var SERVER = "http://app800.cn"
        val SERVER_CHECK: String get() = SERVER.trimEnd('/') + "/apps/check"
        val SERVER_DOWN: String get() = SERVER.trimEnd('/') + "/apps/res/download"
        var CHECK_HOURS = 4 //最多每4小时检查一次

        private val ignoreMap = MapTable("ver_ignore")

        private var lastCheckUpdate: Long by MapTable.config

        private fun isIgnored(verCode: Int): Boolean {
            return ignoreMap[verCode.toString()] != null
        }

        fun checkByUser(page: BasePage) {
            page.openActivity(UpgradeActivity::class)
        }

        fun checkAuto(page: BasePage) {
            val last = lastCheckUpdate
            val now = System.currentTimeMillis()
            if (now - last < CHECK_HOURS * 60 * 60 * 1000) {
                return
            }
            Task.back {
                val v = check()
                Task.fore {
                    if (v != null) {
                        lastCheckUpdate = now
                        if (!isIgnored(v.versionCode)) {
                            if (page.activity != null) {
                                confirmInstall(page.act, v, true)
                            }
                        }
                    }

                }
            }
        }

        private fun confirmInstall(act: Activity, v: YetVersion, quiet: Boolean) {
            if (!v.great()) {
                try {
                    if (!quiet) {
                        act.toast( "已经是最新版本")
                    }
                } catch (ex: Exception) {
                }
                return
            }

            DialogX(act).apply {
                title("检查升级")
                bodyText("发现新版本${v.versionName}")
                buttons {
                    ok("升级") {
                        act.openActivity(UpgradeActivity::class)
                    }
                    normal("忽略此版本") {
                        ignoreMap.put(v.versionCode.toString(), v.versionName)
                    }
                    cancel("取消")
                }
                show()
            }
        }


        fun check(): YetVersion? {
            val r = HttpGet(SERVER_CHECK).arg("pkg", App.packageName).request()
            if (r.OK) {
                val jo = r.ysonObject() ?: return null
                if (jo.int("code") != 0) {
                    return null
                }
                val jdata = jo.obj("data") ?: return null
                return YetVersion(jdata)
            }
            return null
        }

        fun download(resId: Int, progress: Progress?): File? {
            val url = HttpGet(SERVER_DOWN).arg("id", resId).buildGetUrl()
            val f = App.files.ex.temp(App.appName + ".apk")
            val r = HttpGet(url).download(f, progress)
            if (r.OK) {
                return f
            }
            return null
        }

    }
}