import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.litote.kmongo.getCollection
import java.io.File
import java.io.FileWriter
import kotlin.system.exitProcess

val mStudents = mongoDatabase.getCollection<Student>().apply { drop() }
val mGroups = mongoDatabase.getCollection<Group>().apply { drop() }
val mSubject = mongoDatabase.getCollection<Subject>().apply { drop() }
val mKoef = mongoDatabase.getCollection<Koef>()


fun main(){
    insertData()
    val groups = mGroups.find().toList()
    val students = mStudents.find().toList()
    val subjects = mSubject.find().toList()

    val tableStyle = "border: 1px solid"
    File("indexxx.html").writeText(
        StringBuilder().appendHTML()
            .html {
        style = "background-color: ##FFFFF0; color: #000000;"
        groups.toList().map { group ->
            body {
                h2{
                    +"Рейтинг ${group.name}"
                }
                table {
                    style = "text-align:center; border-collapse:collapse; border: 1px solid"
                    thead {
                        tr{
                            td{
                                style = tableStyle
                                +"Студент"
                            }

                            subjects.map {
                                td{
                                    style = tableStyle
                                    +it.name
                                }
                            }
                            td{
                                style = tableStyle
                                +"Итог"
                            }
                        }
                    }
                    tbody {
                        students.map{ student ->
                            tr{
                                td {
                                    style = tableStyle
                                    +student.fio
                                }
                                subjects.map { sub->
                                    val st = student.grades.filter { it.subject == sub.name}
                                    st.map{
                                        td {
                                            style = tableStyle
                                            + (it.grade * 100 / sub.maximumMark).toString()
                                        }
                                    }

                                }
                                td {
                                    style = tableStyle
                                    +"${student.grades.map { it.grade }.sum() * 100/subjects.map{ it.maximumMark }.sum()}"
                                }
                            }
                        }
                        }
                    }
                }
            }
        }

    .toString())
}

fun choiceKoef(): Koef{
    println("Выбирайте, по каким коэффициентам рассчитать рейтинг\n" +
            "Для выбора введите нужную цифру.")
    val koefs = mKoef.find().toList()

    koefs.mapIndexed{index, koef ->
        println("$index - ${koef }\n")
    }
    var choice: Int
    try{
        do{
            choice = readLine()!!.toInt()

        }while(choice > koefs.lastIndex || choice < 0)
    }catch (exception:Exception){
        println("Вы нарушили правила ввода.")
        exitProcess(1)
    }

    return koefs[choice]
}

fun insertData(){
    val groupsFile = Group::class.java.getResource("group.json").readText()
    mGroups.insertMany(Json.decodeFromString(ListSerializer((Group.serializer())), groupsFile))
    val subjectFile = Subject::class.java.getResource("subject.json").readText()
    val subjects = Json.decodeFromString(ListSerializer((Subject.serializer())), subjectFile)

    var choice: String?
    do{
        println("Для создания таблицы необходимо выбрать способ расчета рейтинга\n"+
            "Вы хотите загрузить вес оценок из файла  или взять из базы данных?\n" +
                "Если хотите из базы данных, то введите БД. Если из файла, то введите ФАЙЛ\n")
        choice = readLine()
    }while(choice == null && (choice != "БД" || choice != "ФАЙЛ"))

    val koefs: Koef?
    if(choice == "БД"){
        println("Порядок выведения коэффициентов по предметам\n")
        subjects.mapIndexed{index, name ->
            println( "$index - ${name}\n")
        }

        koefs = choiceKoef()
        if(subjects.size != koefs.value.size){
            println("Простите, но вы допустили ошибку в файлах. Кол-во предметов и кол-во коэффициентов должны совпадать")
            exitProcess(101)
        }
    }else{
        val keofFile = Koef::class.java.getResource("koef.json").readText()
        koefs = Json.decodeFromString(ListSerializer((Koef.serializer())), keofFile).first()
        if(subjects.size != koefs.value.size && koefs.value.all { it >= 1.0 }){
            println("Простите, но вы допустили ошибку в файлах. Кол-во предметов и кол-во коэффициентов должны совпадать")
            exitProcess(101)
        }
        mKoef.insertOne(koefs)
    }
    subjects.mapIndexed { index, subject ->
        subject.maximumMark = koefs.value[index]*subject.maximumMark
    }

    mSubject.insertMany(subjects)

    val studentsFile = Student::class.java.getResource("students.json").readText()
    mStudents.insertMany(Json.decodeFromString(ListSerializer((Student.serializer())), studentsFile))
}