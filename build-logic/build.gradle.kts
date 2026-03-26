tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
