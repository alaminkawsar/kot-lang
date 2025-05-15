package fake_data_generator

import RandomDataGenerator

fun main() {
    val randomGen = RandomDataGenerator.generate(SampleDataModel::class)
    println(randomGen)
}