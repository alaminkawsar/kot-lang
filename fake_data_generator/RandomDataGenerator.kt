import kotlin.random.Random
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.primaryConstructor

object RandomDataGenerator {
    fun <T: Any> generate(clazz: KClass<T>): T? {
        val constructor = clazz.primaryConstructor ?: return null
        val args = constructor.parameters.associateWith { param ->
            generateRandomValue(param.type)
        }
        return constructor.callBy(args)
    }

    private fun generateRandomValue(type: KType): Any? {
        return when (type.classifier) {
            String::class -> randomString()
            Int::class -> Random.nextInt(0, 10000)
            Long::class -> Random.nextLong(0, 1000000000L)
            Float::class -> Random.nextFloat() * 1000000
            Double::class -> Random.nextDouble(0.0, 1000000.0)
            Boolean::class -> Random.nextBoolean()
            else -> null
        }

    }

    private fun randomString(length: Int = 3): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+<>?"
        return (1..length)
            .map { characters.random() }
            .joinToString("")
    }
}