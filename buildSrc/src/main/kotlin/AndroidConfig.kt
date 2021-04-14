/**
 * @作者: Simplation
 * @日期: 2021/4/8 15:22
 * @描述: Config
 * @更新:
 */
object AndroidConfig {
    const val compileSdkVersion = 30
    const val buildToolsVersion = "30.0.3"
    const val minSdkVersion = 21
    const val targetSdkVersion = 30
    const val versionCode = 1
    const val versionName = "1.0.0"

    const val applicationID = "com.simplation.dslsample"

    const val testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}

interface BuildType {
    companion object {
        const val Release = "release"
        const val Debug = "debug"
    }

    val isMinifyEnabled: Boolean
}

object BuildTypeDebug : BuildType {
    override val isMinifyEnabled = false
}

object BuildTypeRelease : BuildType {
    override val isMinifyEnabled = false
}

object TestOptions {
    const val isReturnDefaultValues = true
}
