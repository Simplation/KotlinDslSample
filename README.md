# KotlinDslSample

### 1. 从 Groovy 迁移到 Kotlin DSL 的步骤

1.1. 语法的区分

   - Groovy 字符串可以用单引号 `'string'` 或双引号引起来 `"string"`，而 Kotlin 需要双引号 `"string"`。
   - Groovy 允许在调用函数时省略括号，而 Kotlin 始终需要括号。
   - Gradle Groovy DSL 允许 `=` 在分配属性时省略赋值运算符，而 Kotlin 始终需要赋值运算符。

1.2. 需要改造的文件

   - settings.gradle
   - project 目录下的 `build.gradle`
   - app 目录下的 build.gradle

1.3. 步骤(按照上述罗列的顺序依次进行)

   - settings.gradle (**Gradle Groovy DSL 允许 `=` 在分配属性时省略赋值运算符，而 Kotlin 始终需要赋值运算符。**)

     修改文件名称为 `settings.gradle.kts`，将原来的代码

     ```kotlin
     include ':app'
     rootProject.name = "ProjectName"
     ```

     替换为以下代码

     ```kotlin
     include(":app")
     rootProject.name = "ProjectName"
     ```

   - project / build.gradle

     同样，第一步是修改文件名称，把`build.gradle` 重命名为 `build.gradle.kts`，先罗列出原来的代码

     ```kotlin
     buildscript {
         val kotlin_version = "1.3.61" // #1
         repositories {
             google()
             jcenter()
         }
         dependencies {
             classpath("com.android.tools.build:gradle:4.0.0-beta01")  // #2
             classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
     
             // NOTE: Do not place your application dependencies here; they belong
             // in the individual module build.gradle files
         }
     }
     
     allprojects {
         repositories {
             google()
             jcenter()
         }
     }
     
     tasks.register("clean", Delete::class) {  // #3
         delete(rootProject.buildDir)
     }
     ```
   
     将上面代码标注的三个地方替换成以下代码

     ```kotlin
     buildscript {
     	val kotlinVersion = "1.3.72"
         repositories {
             google()
             jcenter()
         }
         dependencies {
             classpath("com.android.tools.build:gradle:4.0.0")
             classpath(kotlin("gradle-plugin", kotlinVersion))
         }
     }
     
     allprojects {
         repositories {
             google()
             jcenter()
     
             maven {
                 setUrl("https://jitpack.io")
             }
         }
     }
     
     tasks {
         val clean by registering(Delete::class) {
             delete(buildDir)
         }
     }
     ```

   - app / build.gradle

     同样，先修改名称，将 `build.gradle`重命名为 `build.gardle.kts`，该文件修改的较多，直接展示已完成的代码，然后点击 Sync now，同步构建以下代码。

     ```kotlin
     plugins {  // #1
         id("com.android.application")
         kotlin("android")
         kotlin("android.extensions")
     }
     
     android {
         compileSdkVersion(29)  // #2
         buildToolsVersion("29.0.3")
     
         defaultConfig {
             applicationId = "com.example.projectname"
             minSdkVersion(21)
             targetSdkVersion(29)
             versionCode = 1
             versionName = "1.0"
         }
     
         buildTypes {
             getByName("release") {   // #3
                 isMinifyEnabled = false
                 proguardFiles (getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
             }
         }
     }
     
     dependencies {
         implementation(
             fileTree(  // #4
                 mapOf("dir" to "libs", "include" to listOf("*.jar"))
             )
         )
         implementation (kotlin(
             "stdlib-jdk7",
             org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION
         ))
         implementation("androidx.core:core-ktx:1.2.0")
         implementation("androidx.appcompat:appcompat:1.1.0")
         implementation("androidx.constraintlayout:constraintlayout:1.1.3")
         testImplementation("junit:junit:4.12")
         androidTestImplementation("androidx.test.ext:junit:1.1.1")
         androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
     }
     ```

     看到以上代码，不知你是否存在这样的想法？（所有的版本号、依赖库等直接写在 build.gradle.kts 文件中，能不能有一个文件统一进行管理呢？?）答案当然是可以的（官方提供的解决方案：**buildSrc**），**最后再简单说一下 buildSrc 这个文件夹的作用：管理整个项目的依赖。**

### 2. 创建 buildSrc

2.1. 在项目的根目录下创建 buildSrc 文件夹，然后分别创建 src/main/kotlin 和 build.gradle.kts 文件就可以。

   ```kotlin
   buildSrc
   ├── build.gradle.kts
   └── src
       └── main
           └── kotlin
              └── AndroidConfig
   ```

2.2. 在 src/main/kotlin 目录下创建 AndroidConfig 文件，分别将版本信息罗列出来。

   ```kotlin
   object AndroidConfig {
       const val compileSdkVersion = 29
       const val buildToolsVersion = "29.0.3"
       const val minSdkVersion = 21
       const val targetSdkVersion = 29
       const val versionCode = 1
       const val versionName = "1.0.0"
   
       const val applicationID = "com.example.projectname"
   
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
   ```

  

2.3. 最后在 app/build.gradle.kts 文件中引用即可。

   ```kotlin
   plugins {
       id("com.android.application")
       kotlin("android")
       kotlin("android.extensions")
   }
   
   android {
       compileSdkVersion(AndroidConfig.compileSdkVersion)
       buildToolsVersion(AndroidConfig.buildToolsVersion)
   
       defaultConfig {
           applicationId = AndroidConfig.applicationID
   
           minSdkVersion(AndroidConfig.minSdkVersion)
           targetSdkVersion(AndroidConfig.targetSdkVersion)
           versionCode = AndroidConfig.versionCode
           versionName = AndroidConfig.versionName
   }
       
   dependencies {
       implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))  // ## 重点
       implementation(LibraryDependency.KOTLIN)
       implementation(LibraryDependency.COREKTX)
       testImplementation(LibraryDependency.JUNIT)
       androidTestImplementation(LibraryDependency.EXTJUNIT)
   }
   ```

   

   注：贴出的代码不完整，简单贴一下。

2.4. 完善一下 buildSrc 下的 build.gradle.kts 文件

   ```kotlin
   plugins {
       `kotlin-dsl`
   }
   
   repositories {
       jcenter()
   }
   ```

   
