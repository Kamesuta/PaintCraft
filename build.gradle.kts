import com.github.jengelman.gradle.plugins.shadow.tasks.ConfigureShadowRelocation
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.ajoberstar.grgit.Grgit

plugins {
    // Javaプラグインを適用
    java
    // Kotlinを使用するためのプラグイン
    kotlin("multiplatform") version "1.7.10"
    // Kdoc(Kotlin用Javadoc)を生成するためのプラグイン
    id("org.jetbrains.dokka") version "1.7.10"
    // ShadowJar(依存関係埋め込み)を使用するためのプラグイン
    id("com.github.johnrengelman.shadow") version "6.0.0"
    // Gitに応じた自動バージョニングを行うためのプラグイン
    id("org.ajoberstar.grgit") version "4.1.1"
    // ベンチマーク
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.0"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
}

// グループ定義
group = "com.kamesuta"
// バージョン定義
version = run {
    // Gitに応じた自動バージョニングを行うための設定
    val grgit = runCatching { Grgit.open(mapOf("currentDir" to project.rootDir)) }.getOrNull()
        ?: return@run "unknown" // .gitがない
    // HEADがバージョンを示すタグを指している場合はそのタグをバージョンとする
    val versionStr = grgit.describe {
        longDescr = false
        tags = true
        match = listOf("v[0-9]*")
    } ?: "0.0.0" // バージョンを示すタグがない
    // GitHub Actionsでビルドする場合は環境変数からビルド番号を取得する
    val buildNumber = System.getenv("GITHUB_RUN_NUMBER") ?: "git"
    // コミットされていない変更がある場合は+dirtyを付与する
    val dirty = if (grgit.status().isClean) "" else "+dirty"
    // リリースバージョン以外は-SNAPSHOTを付与する
    val snapshot = if (versionStr.matches(Regex(".*-[0-9]+-g[0-9a-f]{7}"))) "-SNAPSHOT" else ""
    // バージョンを組み立てる
    "${versionStr}.${buildNumber}${snapshot}${dirty}"
}

repositories {
    mavenCentral()
    // Paperの依存リポジトリ
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    // ProtocolLibの依存リポジトリ
    maven("https://repo.dmulloy2.net/repository/public/")
}

// 特定のsourceSetのみにallOpenを適用する方法がわからないので全てに適用
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

kotlin {
    // Bukkit使用部分と非使用部分を分離する
    jvm("bukkit") {
        // Java8互換
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    // Bukkit部分のみ依存関係を追加する
    sourceSets {
        all {
            // expect/actualを使用するための設定
            languageSettings.enableLanguageFeature("MultiPlatformProjects")
        }

        val commonMain by getting
        val bukkitMain by getting {
            dependencies {
                // PaperAPI
                compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
                // ProtocolLib
                compileOnly("com.comphenix.protocol:ProtocolLib:4.6.0")
                // FlyLib (コマンド用, 廃止予定)
                implementation("dev.kotx:flylib-reloaded:0.5.0")
                // Netty (WirePacket用)
                compileOnly("io.netty:netty-all:4.0.23.Final")
            }
        }
        val commonTest by getting {
            dependencies {
                // Kotest
                implementation("io.kotest:kotest-runner-junit5-jvm:4.6.1")
                // Kotlinx Benchmark
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.5")
            }
        }
    }
}

benchmark {
    // ベンチマーク設定
    configurations {
        // main構成(sourceSetのmainとは別物)を設定
        val main by getting {
            // 15秒間実行する
            iterationTime = 15
            iterationTimeUnit = "sec"
        }
    }

    targets {
        // bukkitTest構成(sourceSetのもの)を設定
        register("bukkitTest")
    }
}

tasks {
    // 単体テスト (Kotest)
    withType<Test> {
        useJUnitPlatform()
    }

    jar {
        // デフォルトのjarタスクを削除
        enabled = false
    }

    val allMetadataJar by getting(Jar::class) {
        // -metadata.jarを生成するタスクを削除
        enabled = false
    }

    val bukkitJar by getting(Jar::class) {
        // -bukkitを除く
        archiveAppendix.set("")
        // 依存関係を埋め込んでいないjarは末尾に-originalを付与する
        archiveClassifier.set("original")
    }

    // 依存関係をcom.yourgroup.lib以下に埋め込むためにリロケートする
    val relocateShadow by registering(ConfigureShadowRelocation::class) {
        target = shadowJar.get()
        prefix = "${project.group}.${project.name}.lib"
    }

    // デバッグ用のfatJarを生成する (リロケートあり版)
    shadowJar {
        // jarタスクの出力を依存関係に追加
        dependsOn(bukkitJar)
        // リロケートする
        dependsOn(relocateShadow)
        // 依存関係を埋め込んだjarは末尾なし
        archiveClassifier.set("")
    }

    // デバッグ用のfatJarを生成する (ホットリロードできるようにリロケートを行わない)
    val shadowJarDev by registering(ShadowJar::class) {
        // jarタスクの出力を依存関係に追加
        dependsOn(bukkitJar)
        // デバッグ用は末尾に-devを付与する
        archiveClassifier.set("dev")
    }

    // ShadowJarタスクを新規作成する際の設定を行う (ShadowJavaPlugin.groovyを参照)
    // https://github.com/johnrengelman/shadow/issues/108#issuecomment-62418005
    withType<ShadowJar> {
        // 必要な変数を取得
        val bukkit by kotlin.targets.getting
        val main by bukkit.compilations.getting
        val bukkitRuntimeClasspath by project.configurations.getting
        // ShadowJarタスクを新規作成する際の設定を行う (ShadowJavaPlugin.groovyを参照)
        // https://github.com/johnrengelman/shadow/issues/108#issuecomment-62418005
        from(main.output)
        configurations = listOf(bukkitRuntimeClasspath)
        exclude("META-INF/INDEX.LIST", "META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "module-info.class")
    }

    // Javadocを出力する
    val javadocJar by registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(dokkaHtml)
        dependsOn(dokkaHtml)
    }

    // ソースjarを生成する
    val sourcesJar by registering(Jar::class) {
        archiveClassifier.set("sources")
        val commonMain by kotlin.sourceSets.getting
        val bukkitMain by kotlin.sourceSets.getting
        from(commonMain.kotlin.srcDirs)
        from(bukkitMain.kotlin.srcDirs)
    }

    // アーティファクトを登録する
    artifacts {
        // 依存関係なしオリジナルjarをビルドする (-original)
        add("archives", bukkitJar)
        // 依存関係を埋め込んだリロケート済みjarをビルドする
        add("archives", shadowJar)
        // 依存関係を埋め込んだリロケートなしjarをビルドする (-dev)
        add("archives", shadowJarDev)
        // Javadocを出力する (-javadoc)
        add("archives", javadocJar)
        // ソースjarを生成する (-sources)
        add("archives", sourcesJar)
    }

    // plugin.ymlの中にバージョンを埋め込む
    @Suppress("UnstableApiUsage")
    withType<ProcessResources> {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
