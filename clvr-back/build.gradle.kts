plugins {
    jacoco
}

subprojects {
    repositories {
        mavenCentral()
    }

    group = "com.clvr.platform"
    version = "0.0.1"

    apply {
        plugin("jacoco")
    }

    tasks.withType<JacocoReport> {
        reports {
            xml.required = true
            csv.required = true
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}