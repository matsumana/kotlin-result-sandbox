package com.example.sandbox

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.example.sandbox.application.helper"]
)
class TestApplication
