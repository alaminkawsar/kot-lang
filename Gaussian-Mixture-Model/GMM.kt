import kotlin.math.*
import kotlin.random.Random

class GMM(
    private val nComponents: Int,
    private val nIters: Int = 100,
    private val tol: Double = 1e-4
) {
    private lateinit var pi: DoubleArray         // Mixing coefficients
    private lateinit var mu: Array<DoubleArray>  // Means (k x d)
    private lateinit var var_: Array<DoubleArray> // Variances (k x d)
    private var dim: Int = 1

    private fun initialize(X: Array<DoubleArray>) {
        val n = X.size
        dim = X[0].size
        pi = DoubleArray(nComponents) { 1.0 / nComponents }

        // Randomly select initial means
        val indices = X.indices.shuffled().take(nComponents)
        mu = Array(nComponents) { i -> X[indices[i]].copyOf() }

        // Initialize variances
        var_ = Array(nComponents) { DoubleArray(dim) { 1.0 } }
    }

    private fun gaussian(X: Array<DoubleArray>): Array<DoubleArray> {
        val n = X.size
        val probs = Array(n) { DoubleArray(nComponents) }

        for (i in 0 until n) {
            for (k in 0 until nComponents) {
                var prob = 1.0
                for (d in 0 until dim) {
                    val x = X[i][d]
                    val m = mu[k][d]
                    val v = var_[k][d] + 1e-6
                    val coeff = 1 / sqrt(2 * Math.PI * v)
                    val expTerm = exp(-0.5 * (x - m).pow(2) / v)
                    prob *= coeff * expTerm
                }
                probs[i][k] = prob
            }
        }

        return probs
    }

    fun fit(X: Array<DoubleArray>) {
        val n = X.size
        val d = X[0].size
        initialize(X)

        var prevLogLikelihood = Double.NEGATIVE_INFINITY

        for (iter in 0 until nIters) {
            // E-step
            val probs = gaussian(X)
            val weighted = Array(n) { i -> DoubleArray(nComponents) { k -> probs[i][k] * pi[k] } }

            val responsibilities = Array(n) { i ->
                val rowSum = weighted[i].sum() + 1e-10
                DoubleArray(nComponents) { k -> weighted[i][k] / rowSum }
            }

            // M-step
            val Nk = DoubleArray(nComponents) { k -> responsibilities.sumOf { it[k] } }

            for (k in 0 until nComponents) {
                // Update means
                for (j in 0 until d) {
                    mu[k][j] = (0 until n).sumOf { i -> responsibilities[i][k] * X[i][j] } / Nk[k]
                }

                // Update variances
                for (j in 0 until d) {
                    var_[k][j] = (0 until n).sumOf { i ->
                        val diff = X[i][j] - mu[k][j]
                        responsibilities[i][k] * diff * diff
                    } / Nk[k]
                }

                // Update pi
                pi[k] = Nk[k] / n
            }

            // Compute log-likelihood
            val logLikelihood = (0 until n).sumOf { i ->
                ln((0 until nComponents).sumOf { k -> pi[k] * probs[i][k] } + 1e-10)
            }

            if (abs(logLikelihood - prevLogLikelihood) < tol) {
                println("Converged at iteration $iter")
                break
            }

            prevLogLikelihood = logLikelihood
        }
    }

    fun predict(X: Array<DoubleArray>): IntArray {
        val probs = gaussian(X)
        val weighted = Array(X.size) { i -> DoubleArray(nComponents) { k -> probs[i][k] * pi[k] } }

        return IntArray(X.size) { i ->
            weighted[i].indices.maxByOrNull { k -> weighted[i][k] } ?: 0
        }
    }

    fun predictProba(X: Array<DoubleArray>): Array<DoubleArray> {
        val probs = gaussian(X)
        val weighted = Array(X.size) { i -> DoubleArray(nComponents) { k -> probs[i][k] * pi[k] } }

        return Array(X.size) { i ->
            val total = weighted[i].sum() + 1e-10
            DoubleArray(nComponents) { k -> weighted[i][k] / total }
        }
    }
}
