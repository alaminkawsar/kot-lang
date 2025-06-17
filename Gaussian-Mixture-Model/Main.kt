import GMM

fun main() {
    // Generate synthetic data
    val group1 = DoubleArray(300) { Random.nextGaussian() * 5 + 120 }
    val group2 = DoubleArray(300) { Random.nextGaussian() * 5 + 150 }
    val group3 = DoubleArray(300) { Random.nextGaussian() * 5 + 170 }

    val data = (group1 + group2 + group3).map { doubleArrayOf(it) }.toTypedArray()

    // Train GMM
    val gmm = GMM(nComponents = 3)
    gmm.fit(data)

    // Predict labels
    val labels = gmm.predict(data)

    // Print first 10 labels
    println("First 10 predicted labels:")
    println(labels.take(10).joinToString(", "))
}
