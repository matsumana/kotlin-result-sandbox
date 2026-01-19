dependencies {
    implementation(project(":sandbox-domain"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    implementation(libs.ulid)
    testImplementation(libs.sqlite)
    testImplementation("org.springframework.boot:spring-boot-starter-flyway")
}
