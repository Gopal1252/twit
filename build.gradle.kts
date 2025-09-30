plugins {
    id("java")
    id("application")
}

group = "com.gopal.twit"
version = "1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass.set("com.gopal.twit.Twit")
}

tasks.test {
    useJUnitPlatform()
}
