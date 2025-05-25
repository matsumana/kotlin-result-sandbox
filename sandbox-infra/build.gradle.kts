dependencies {
    implementation(project(":sandbox-domain"))
    implementation(libs.mybatisSpringBootStarter)
    implementation(libs.sqlite)
    implementation("org.flywaydb:flyway-core")
    testImplementation(libs.mybatisSpringBootStarterTest)
}
