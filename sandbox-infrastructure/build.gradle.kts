dependencies {
    implementation(project(":sandbox-domain"))
    implementation(libs.ulid)
    implementation(libs.mybatisSpringBootStarter)
    implementation(libs.sqlite)
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    testImplementation(libs.mybatisSpringBootStarterTest)
}
