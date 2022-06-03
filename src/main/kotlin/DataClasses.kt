import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Student(
    val fio: String, //ФИО студента
    val groupId: Int,
    // айди студента
    val id: Int,
    var grades: List<Completed> = emptyList() // оценки студента
)

@Serializable
data class Group(
    val name: String,// Имя группы
    val id: Int
)

@Serializable
data class Subject(
    val name: String, // предмета
    var maximumMark: Double
)

@Serializable
data class Completed (
    val student: String, // чья оценка
    val subject: String,
    var grade : Double // какую оценку получил
)

// Вес оценок
@Serializable
data class Koef(
    val value: List<Double> = emptyList()
)