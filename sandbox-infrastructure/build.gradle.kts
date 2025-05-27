dependencies {
    implementation(project(":sandbox-domain"))
    implementation(project(":sandbox-application"))
    implementation(libs.ulid)
    implementation(libs.mybatisSpringBootStarter)
    implementation(libs.sqlite)
    implementation("org.flywaydb:flyway-core")
    testImplementation(libs.mybatisSpringBootStarterTest)
}
