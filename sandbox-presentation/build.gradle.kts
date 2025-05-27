dependencies {
    implementation(project(":sandbox-application"))
    runtimeOnly(project(":sandbox-infrastructure"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework:spring-jdbc")
}
