plugins {
    base
    id("jacoco-report-aggregation")
}

dependencies {
    jacocoAggregation(project(":platform"))
    jacocoAggregation(project(":server"))
    jacocoAggregation(project(":tic-tac-toe"))
    jacocoAggregation(project(":nekahoot"))
}

reporting {
    reports {
        val testCodeCoverageReport by creating(JacocoCoverageReport::class) { // <.>
            testType = TestSuiteType.UNIT_TEST
        }
    }
}

tasks.check {
    dependsOn(tasks.named<JacocoReport>("testCodeCoverageReport")) // <.>
}